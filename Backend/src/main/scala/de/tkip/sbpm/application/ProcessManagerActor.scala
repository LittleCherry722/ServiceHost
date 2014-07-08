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
import java.util.UUID
import de.tkip.sbpm.ActorLocator
import akka.actor.Status.Failure
import de.tkip.sbpm.application.history._
import java.util.Date
import scala.concurrent.Future
import akka.pattern.pipe
import scala.collection.mutable.ArrayBuffer
import de.tkip.sbpm.model._
import de.tkip.sbpm._
import de.tkip.sbpm.instrumentation.InstrumentedActor

import java.io._

protected case class RegisterSubjectProvider(userID: UserID,
  subjectProviderActor: SubjectProviderRef)

/**
 * manages all processes and creates new ProcessInstance's on demand
 * information expert for relations between SubjectProviderActor/ProcessInstanceActor
 */
class ProcessManagerActor extends InstrumentedActor {
  private case class ProcessInstanceData(processID: ProcessID, processName: String, name: String, processInstanceActor: ProcessInstanceRef)
  implicit val ec = context.dispatcher
  // the process instances aka the processes in the execution
  private val processInstanceMap = collection.mutable.Map[ProcessInstanceID, ProcessInstanceData]()
  private val history = new NewHistory

  // used to map answer messages back to the subjectProvider who sent a request
  private val subjectProviderMap = collection.mutable.Map[UserID, SubjectProviderRef]()

  private lazy val changeActor = ActorLocator.changeActor

  def wrappedReceive = {

    case register: RegisterSubjectProvider => {
      subjectProviderMap += register.userID -> register.subjectProviderActor
    }

    // execution
    case getAll: GetAllProcessInstances => {
      val msg = AllProcessInstancesAnswer(
        getAll,
        processInstanceMap.map(
          s => ProcessInstanceInfo(s._1, s._2.name, s._2.processID)).toArray.sortBy(_.id))

      sender !! msg
    }

    case message: GetNewHistory => {
      sender !! NewHistoryAnswer(message, history)
    }

    case cp: CreateProcessInstance => {
      // create the process instance
      context.actorOf(Props(new ProcessInstanceActor(cp)), "ProcessInstanceActor____" + UUID.randomUUID().toString())
    }

    case pc: ProcessInstanceCreated => {
      if (pc.sender != null) {
        // sender == remote ProcessInstanceProxyManagerActor
        pc.sender !! pc
      } else {
        log.error("Processinstance created: " + pc.processInstanceID + " but sender is unknown")
      }
      val p = ProcessInstanceData(pc.request.processID, pc.answer.processName, pc.request.name, pc.processInstanceActor)
      processInstanceMap +=
        pc.processInstanceID -> p
      history.entries += createHistoryEntry(Some(pc.request.userID), pc.processInstanceID, "created")
      log.info("new processInstance has been added: " + p)
      changeActor ! ProcessInstanceChange(pc.processInstanceID, p.processID, p.processName, p.name, "insert", new java.util.Date())
    }

    case kill: KillAllProcessInstances => {
      log.debug("Killing all process instances")
      for ((id, _) <- processInstanceMap) {
        context.stop(processInstanceMap(id).processInstanceActor)
        history.entries += createHistoryEntry(None, id, "killed")
        changeActor ! ProcessInstanceDelete(id, new java.util.Date())
      }
      processInstanceMap.clear()
      // TODO delete the history, in future the history should be in a database,
      // so there is no extra message for it
      history.entries.clear()

      kill.sender !! ProcessInstancesKilled
    }

    case kill @ KillProcessInstance(id) => {
      if (processInstanceMap.contains(id)) {
        processInstanceMap(id).processInstanceActor ! PoisonPill
        history.entries += createHistoryEntry(None, id, "killed")
        processInstanceMap -= id
        kill.sender !! KillProcessInstanceAnswer(kill)
        log.debug("Killed process instance " + id)

        changeActor ! ProcessInstanceDelete(id, new java.util.Date())
      } else {
        log.error("Process Manager - can't kill process instance: " +
          id + ", it does not exists")

        kill.sender !! Failure(new IllegalArgumentException(
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

    case entry: NewHistoryEntry => {
      history.entries += entry
    }

    case GetHistorySince(t) => {
      Future { getHistoryChange(t) } pipeTo sender
    }

    case message => {
      log.error("Not impemented: " + message)
    }
  }

  // to forward a message to the process instance it needs a function to
  // get the processinstance id
  private type ForwardProcessInstanceMessage = { def processInstanceID: ProcessInstanceID }

  private def createHistoryEntry(userId: Option[UserID],
    processInstanceId: ProcessInstanceID,
    event: String): NewHistoryEntry =
    NewHistoryEntry(
      new Date(),
      userId,
      NewHistoryProcessData(processInstanceMap(processInstanceId).processName, processInstanceId, processInstanceMap(processInstanceId).name),
      None,
      None,
      Some(event))

  /**
   * Forwards a message to a processinstance
   */
  private def forwardMessageToProcessInstance(message: ForwardProcessInstanceMessage) {
    if (processInstanceMap.contains(message.processInstanceID)) {
      processInstanceMap(message.processInstanceID).processInstanceActor.forward(message)
    } else if (message.isInstanceOf[AnswerAbleMessage]) {
      message.asInstanceOf[AnswerAbleMessage].sender !!
        Failure(new Exception("Target process instance does not exists."))

      log.error("ProcessManager - message for " + message.processInstanceID +
        " but does not exist, " + message)
    }
  }

  private def getHistoryChange(t: Long) = {
    val lastUpdate = new java.util.Date().getTime() - t * 1000
    val changes = history.entries.filter(_.timeStamp.getTime() > lastUpdate)
    val temp = ArrayBuffer[HistoryRelatedChangeData]()
    for (i <- 0 until changes.length) {
      val entry = changes(i)
      temp += HistoryRelatedChangeData(entry.userId, entry.process, entry.subject, entry.transitionEvent, entry.lifecycleEvent, new java.sql.Timestamp(entry.timeStamp.getTime()))
    }
    Some(HistoryRelatedChange(Some(temp.toArray)))

  }
}
