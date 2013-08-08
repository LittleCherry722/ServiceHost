/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.application

import akka.actor._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.persistence._
import akka.event.Logging
import de.tkip.sbpm.ActorLocator
import akka.actor.Status.Failure
import de.tkip.sbpm.application.history._
import java.util.Date

protected case class RegisterSubjectProvider(userID: UserID,
  subjectProviderActor: SubjectProviderRef)

/**
 * manages all processes and creates new ProcessInstance's on demand
 * information expert for relations between SubjectProviderActor/ProcessInstanceActor
 */
class ProcessManagerActor extends Actor {
  private case class ProcessInstanceData(processID: ProcessID, name: String, processInstanceActor: ProcessInstanceRef)

  val logger = Logging(context.system, this)
  // the process instances aka the processes in the execution
  private val processInstanceMap = collection.mutable.Map[ProcessInstanceID, ProcessInstanceData]()
  private val history = new NewHistory

  // used to map answer messages back to the subjectProvider who sent a request
  private val subjectProviderMap = collection.mutable.Map[UserID, SubjectProviderRef]()

  def receive = {
    case register: RegisterSubjectProvider => {
      subjectProviderMap += register.userID -> register.subjectProviderActor
    }

    // execution
    case getAll: GetAllProcessInstances => {
      sender !
        AllProcessInstancesAnswer(
          getAll,
          processInstanceMap.map(
            s => ProcessInstanceInfo(s._1, s._2.name, s._2.processID)).toArray.sortBy(_.id))
    }

    case cp: CreateProcessInstance => {
      // create the process instance
      context.actorOf(Props(new ProcessInstanceActor(cp)))
    }

    case pc: ProcessInstanceCreated => {
      if (pc.sender != null) {
        pc.sender ! pc
      } else {
        logger.error("Processinstance created: " + pc.processInstanceID + " but sender is unknown")
      }
      processInstanceMap +=
        pc.processInstanceID -> ProcessInstanceData(pc.request.processID, pc.request.name, pc.processInstanceActor)
      history.entries += NewHistoryEntry(new Date(), Some(pc.request.userID), NewHistoryProcessData(processInstanceMap(pc.processInstanceID).name, pc.processInstanceID), None, Some("created"))
      
    }

    case KillAllProcessInstances => {
      logger.debug("Killing all process instances")
      for((id,_) <- processInstanceMap) {
        context.stop(processInstanceMap(id).processInstanceActor)
        history.entries += NewHistoryEntry(new Date(), None, NewHistoryProcessData(processInstanceMap(id).name, id), None, Some("killed"))
      }
      processInstanceMap.clear()
      sender ! ProcessInstancesKilled
    }

    case kill @ KillProcessInstance(id) => {
      println("killed " + id)
      if (processInstanceMap.contains(id)) {
        context.stop(processInstanceMap(id).processInstanceActor)
        processInstanceMap -= id
        history.entries += NewHistoryEntry(new Date(), None, NewHistoryProcessData(processInstanceMap(id).name, id), None, Some("killed"))
        sender ! KillProcessInstanceAnswer(kill)
      } else {
        logger.error("Process Manager - can't kill process instance: " +
          id + ", it does not exists")
        kill.sender ! Failure(new IllegalArgumentException(
          "Invalid Argument: Can't kill a processinstance, which is not running."))
      }
      // TODO always try to delete it from the database?
      //      ActorLocator.persistenceActor ! DeleteProcessInstance(id)
    }

    // general matching

    // TODO muesste man auch zusammenfassenkoennen
    case message: ProcessInstanceMessage => {
      forwardMessageToProcessInstance(message)
    }

    case message: SubjectMessage => {
      forwardMessageToProcessInstance(message)
    }

    case message: SubjectProviderMessage => {
      subjectProviderMap
        .getOrElse(message.userID, ActorLocator.subjectProviderManagerActor)
        .forward(message)
    }

    case answer: AnswerMessage => {
      answer.sender.forward(answer)
    }

    case message: GetNewHistory => {
      sender ! NewHistoryAnswer(message, history)
    }

    case entry: NewHistoryEntry => {
      history.entries += entry
    }

    case message => {
      logger.error("Not impemented: " + message)
    }
  }

  // to forward a message to the process instance it needs a function to 
  // get the processinstance id
  private type ForwardProcessInstanceMessage = { def processInstanceID: ProcessInstanceID }

  /**
   * Forwards a message to a processinstance
   */
  private def forwardMessageToProcessInstance(message: ForwardProcessInstanceMessage) {
    if (processInstanceMap.contains(message.processInstanceID)) {
      processInstanceMap(message.processInstanceID).processInstanceActor.forward(message)
    } else if (message.isInstanceOf[AnswerAbleMessage]) {
      message.asInstanceOf[AnswerAbleMessage].sender !
        Failure(new Exception("Target process instance does not exists."))

      logger.error("ProcessManager - message for " + message.processInstanceID +
        " but does not exist, " + message)
    }
  }
}
