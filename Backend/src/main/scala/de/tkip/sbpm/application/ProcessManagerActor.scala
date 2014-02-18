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
import de.tkip.sbpm.persistence.query._
import akka.pattern.ask
import java.io._
import akka.util.Timeout
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Success

protected case class RegisterSubjectProvider(userID: UserID,
  subjectProviderActor: SubjectProviderRef)

/**
 * manages all processes and creates new ProcessInstance's on demand
 * information expert for relations between SubjectProviderActor/ProcessInstanceActor
 */
class ProcessManagerActor extends Actor {
  private case class ProcessInstanceData(processID: ProcessID, processName: String, name: String, processInstanceActor: ProcessInstanceRef)
  implicit val ec = context.dispatcher
  implicit val timeout = Timeout(4 seconds)
  val logger = Logging(context.system, this)
  // the process instances aka the processes in the execution
  private val processInstanceMap = collection.mutable.Map[ProcessInstanceID, ProcessInstanceData]()
  private val history = new NewHistory

  // used to map answer messages back to the subjectProvider who sent a request
  private val subjectProviderMap = collection.mutable.Map[UserID, SubjectProviderRef]()

  private lazy val changeActor = ActorLocator.changeActor
  
  override def preStart() {
    try{
      val getAllHistoryFuture = (ActorLocator.persistenceActor ? Histories.Read.All).mapTo[Seq[History]]
      val result = Await.result(getAllHistoryFuture, timeout.duration)
      println("--------------------------------------------------------------------")
      for(r <- result){
        if(r.historyMessageId.isEmpty){
          val entry = NewHistoryEntry(r.timestamp, Some(r.userId), 
          NewHistoryProcessData(r.processName, r.processInstanceId, r.processInstanceName), 
          Some(r.subjectId), Some(NewHistoryTransitionData(NewHistoryState(r.fromStateText, r.fromStateType), r.transitionText, 
          r.transitionType, NewHistoryState(r.toStateText, r.toStateType), None)),None)
          history.entries += entry
        }else{
          val messageId = r.historyMessageId.get
          val message = (ActorLocator.persistenceActor ? HistoryMessages.Read.ById(messageId)).mapTo[HistoryMessage]
          val mResult = Await.result(message, timeout.duration)
          val entry = NewHistoryEntry(r.timestamp, Some(r.userId), 
          NewHistoryProcessData(r.processName, r.processInstanceId, r.processInstanceName), 
          Some(r.subjectId), Some(NewHistoryTransitionData(NewHistoryState(r.fromStateText, r.fromStateType), r.transitionText, 
          r.transitionType, NewHistoryState(r.toStateText, r.toStateType), 
          Some(NewHistoryMessage(mResult.id, mResult.fromSubject, mResult.toSubject, mResult.messageType, mResult.text)))),None)
          history.entries += entry
        }
      }
    }catch{
      case e: NoSuchElementException => {
        e.printStackTrace()
      }
    }
  }
  
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

    case message: GetNewHistory => {
      sender ! NewHistoryAnswer(message, history)
    }

    case cp: CreateProcessInstance => {
      // create the process instance
      context.actorOf(Props(new ProcessInstanceActor(cp)),"ProcessInstanceActor____"+UUID.randomUUID().toString())
    }

    case pc: ProcessInstanceCreated => {
      if (pc.sender != null) {
        pc.sender ! pc
      } else {
        logger.error("Processinstance created: " + pc.processInstanceID + " but sender is unknown")
      }
      processInstanceMap +=
        pc.processInstanceID -> ProcessInstanceData(pc.request.processID, pc.answer.processName, pc.request.name, pc.processInstanceActor)
      val entry = createHistoryEntry(Some(pc.request.userID), pc.processInstanceID, "created")
      history.entries += entry
      val p = ProcessInstanceData(pc.request.processID, pc.answer.processName, pc.request.name, pc.processInstanceActor)
      println("new processInstance has been added: "+p)
      changeActor ! ProcessInstanceChange(pc.processInstanceID, p.processID, p.processName, p.name, "insert", new java.util.Date())
      entry.transitionEvent.isDefined match { 
        case true =>
          ActorLocator.persistenceActor ? Histories.Save(History(None, entry.process.processName, entry.process.processInstanceId, 
          entry.process.processInstanceName, new java.sql.Timestamp(entry.timeStamp.getTime()), None, None, entry.userId.get, entry.subject.get, 
          entry.transitionEvent.get.transitionType, entry.transitionEvent.get.text, entry.transitionEvent.get.fromState.stateType, 
          entry.transitionEvent.get.fromState.text, entry.transitionEvent.get.toState.stateType, entry.transitionEvent.get.toState.text, None))
      
        case false => 
          ActorLocator.persistenceActor ? Histories.Save(History(None, entry.process.processName, entry.process.processInstanceId, 
          entry.process.processInstanceName, new java.sql.Timestamp(entry.timeStamp.getTime()), None, None, entry.userId.get, "", 
          "", "", "", "", "", "", None))
      }
    }

    case kill: KillAllProcessInstances => {
      logger.debug("Killing all process instances")
      for ((id, _) <- processInstanceMap) {
        context.stop(processInstanceMap(id).processInstanceActor)
        val entry = createHistoryEntry(None, id, "killed")
        history.entries += entry
        ActorLocator.persistenceActor ? Histories.Save(History(None, entry.process.processName, entry.process.processInstanceId, 
          entry.process.processInstanceName, new java.sql.Timestamp(entry.timeStamp.getTime()), None, None, entry.userId.get, "", 
          "", "", "", "", "", "", None))
        changeActor ! ProcessInstanceDelete(id, new java.util.Date())
      }
      processInstanceMap.clear()
      // TODO delete the history, in future the history should be in a database,
      // so there is no extra message for it
      history.entries.clear()
      
      kill.sender ! ProcessInstancesKilled
    }

    case kill @ KillProcessInstance(id) => {
      if (processInstanceMap.contains(id)) {
        processInstanceMap(id).processInstanceActor ! PoisonPill
        val entry = createHistoryEntry(None, id, "killed")
        history.entries += entry
        ActorLocator.persistenceActor ? Histories.Save(History(None, entry.process.processName, entry.process.processInstanceId, 
          entry.process.processInstanceName, new java.sql.Timestamp(entry.timeStamp.getTime()), None, None, entry.userId.get, "", 
          "", "", "", "", "", "", None))
        processInstanceMap -= id
        kill.sender ! KillProcessInstanceAnswer(kill)
        logger.debug("Killed process instance " + id)
        changeActor ! ProcessInstanceDelete(id, new java.util.Date())
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

    case entry: NewHistoryEntry => {
      history.entries += entry
      var messageId = None: Option[Int]
      if(!entry.transitionEvent.get.message.isEmpty){
        messageId = Some(entry.transitionEvent.get.message.get.messageId)
        ActorLocator.persistenceActor ? HistoryMessages.Save(HistoryMessage(messageId.get, 
          1, 1, entry.transitionEvent.get.message.get.fromSubject, entry.transitionEvent.get.message.get.toSubject, 
          entry.transitionEvent.get.message.get.messageType, entry.transitionEvent.get.message.get.text))
      }
      ActorLocator.persistenceActor ? Histories.Save(History(None, entry.process.processName, entry.process.processInstanceId, entry.process.processInstanceName, 
        new java.sql.Timestamp(entry.timeStamp.getTime()), None, None, entry.userId.get, entry.subject.get, 
        entry.transitionEvent.get.transitionType, entry.transitionEvent.get.text, entry.transitionEvent.get.fromState.stateType, 
        entry.transitionEvent.get.fromState.text, entry.transitionEvent.get.toState.stateType, entry.transitionEvent.get.toState.text, messageId))   
    }
    
    case GetHistorySince(t) => {
      Future { getHistoryChange(t) } pipeTo sender
    }

    case message => {
      logger.error("Not impemented: " + message)
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
      message.asInstanceOf[AnswerAbleMessage].sender !
        Failure(new Exception("Target process instance does not exists."))

      logger.error("ProcessManager - message for " + message.processInstanceID +
        " but does not exist, " + message)
    }
  }
  
  private def getHistoryChange(t: Long) = {
    val changes = history.entries.filter(_.timeStamp.getTime() > t * 1000)
    val temp = ArrayBuffer[HistoryRelatedChangeData]()
    for (i <- 0 until changes.length){
      val entry = changes(i)
      temp += HistoryRelatedChangeData(entry.userId, entry.process, entry.subject, entry.transitionEvent, entry.lifecycleEvent, new java.sql.Timestamp(entry.timeStamp.getTime()))
    }
    Some(HistoryRelatedChange(Some(temp.toArray)))
  
  }
}
