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

package de.tkip.sbpm.application.subject

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import akka.actor.ActorContext
import akka.actor.Props
import java.util.UUID
import akka.pattern.ask
import de.tkip.sbpm.application.miscellaneous.SubjectMessage
import de.tkip.sbpm.application.{ SubjectCreated, SubjectTerminated, RegisterSingleSubjectInstance }
import de.tkip.sbpm.application.ProcessInstanceActor.{Agent}
import akka.event.LoggingAdapter
import akka.actor.ActorRef
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.model.{SubjectLike}
import de.tkip.sbpm.application.miscellaneous.BlockUser
import de.tkip.sbpm.application.miscellaneous.UnBlockUser
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.util.Timeout
import akka.event.Logging
import de.tkip.sbpm.instrumentation.{ClassTraceLogger, TraceLogger}

/**
 * This class is responsible to hold a subjects, and can represent
 * a single subject or a multisubject
 */
class SubjectContainer(
  subject: SubjectLike,
  processID: ProcessID,
  processInstanceID: ProcessInstanceID,
  processInstanceManager: ActorRef,
  log: LoggingAdapter,
  blockingHandlerActor: ActorRef,
  agent: Option[Agent],
  increaseSubjectCounter: () => Unit,
  decreaseSubjectCounter: () => Unit)(implicit context: ActorContext) extends  ClassTraceLogger {

  import scala.collection.mutable.{ Map => MutableMap, Set => MutableSet }

  private implicit val timeout = Timeout(30 seconds)
  implicit val traceName = this.getClass.getSimpleName

  private val multi = subject.multi
  private val single = !multi
  private val external = subject.external

  private val subjects = MutableMap[UserID, SubjectInfo]()
  private val nonProperSubjects = MutableSet[UserID]()

  /**
   * Adds a Subject to this multisubject
   */
  // TODO ueberarbeiten
  def createSubject(userID: UserID) {
    log.debug("SubjectContainer.createSubject: " + userID)
    log.debug("Created: " + RegisterSingleSubjectInstance(processID, processInstanceID, subject.id, userID))
    if (single) {
      if (subjects.size > 0) {
        log.error("Single subjects cannot be created twice")
        return
      }
      // register this subject at the context resolver so other subject dont
      // try to send to wrong instances
      val msg = RegisterSingleSubjectInstance(processID, processInstanceID, subject.id, userID)
      ActorLocator.contextResolverActor !! msg
    }

    val subjectData =
      SubjectData(
        userID,
        processID,
        processInstanceID,
        context.self,
        blockingHandlerActor,
        subject)

    // TODO hier external einfuegen
    if (external) {
      log.debug("CREATE EXTERNAL: {}", subjectData.subject)

      // process schon vorhanden?
      // TODO mit futures
      // TODO make sure the agent is available (Some) and not None
      val processInstanceRef =
        (processInstanceManager ??
          GetProcessInstanceProxy(agent.get))
          .mapTo[ActorRef]

      log.debug("CREATE: processInstanceRef = {}", processInstanceRef)

      // TODO we need this unblock!
      blockingHandlerActor !! UnBlockUser(userID)

      subjects += userID -> SubjectInfo(processInstanceRef, userID)
    } else {
      // create subject
      val subjectRef =
        context.actorOf(Props(new SubjectActor(subjectData)), "SubjectActor____" + UUID.randomUUID().toString())
      // and store it in the map
      subjects += userID -> SubjectInfo(Future.successful(subjectRef), userID)

      val msg = SubjectCreated(userID, processID, processInstanceID, subject.id, subjectRef)
      // inform the subject provider about his new subject
      context.parent !! msg

      reStartSubject(userID)
    }

    log.debug("Processinstance [" + processInstanceID + "] created Subject " +
      subject.id + " for user " + userID)
  }

  def handleSubjectTerminated(message: SubjectTerminated) {

    log.debug("Processinstance [" + processInstanceID + "] Subject " + subject.id + "[" +
      message.userID + "] terminated proper [" + message.proper.toString + "]")

    val userID = message.userID

    if (!message.proper) {
      nonProperSubjects += userID
    }

    // decrease the subject counter
    decreaseSubjectCounter()
    subjects -= userID

    // inform the subject provider about his terminated subject
    context.parent !! message
  }

  /**
   * Forwards a message to all Subjects of this MultiSubject
   */
  def send(message: SubjectToSubjectMessage) {
    val target = message.target

    if (target.toVariable) {
      // TODO why not targetUsers = var subjects?
      sendTo(target.varSubjects.map(_._2), message)
    } else if (target.toExternal && target.toUnknownUsers) {
      sendToExternal(message)
    } else {
      sendTo(target.targetUsers, message)
    }
  }

  def send(message: SubjectMessage) {
    if (subjects.contains(message.userID)) {
      subjects(message.userID).tell(message, context.sender)
    }
  }

  /**
   * Forwards the message to the array of subjects
   */
  private def sendTo(targetSubjects: Array[UserID], message: SubjectToSubjectMessage) {
    for (userID <- targetSubjects) {
      log.info("Sending message to user: {}", userID)
      if (!subjects.contains(userID)) {
        if(nonProperSubjects.contains(userID)) {
          log.info("Subject is not allowed to restart")
          return
        } else {
          log.info("Subject Container creating new subject for user ID: {}", userID)
          createSubject(userID)
        }
      } else if (!subjects(userID).running) {
        log.info("Subject Container restarting subject for user ID: :{}", userID)
        reStartSubject(userID)
      }

      log.debug("SEND: {}", message)
      log.debug("Target user: {}", message.target.targetUsers)
      for (userId <- subjects) {
        log.debug("userID: {}", userId)
      }

      val newMessage = if (external) {
        // exchange the target subject id
        val newMessage = message.copy(target = message.target.copy(subjectID = agent.get.subjectId))
        log.debug("SEND (target exchanged): {}", newMessage)
        // TODO we need this unblock! Why?
        blockingHandlerActor !! UnBlockUser(userID)
        newMessage
      } else {
        message
      }
      // blockingHandlerActor !! BlockUser(userID)
      subjects(userID).tell(newMessage, context.sender)

    }
  }

  def sendToExternal(message: SubjectToSubjectMessage) {
    log.info("Sending message to external subject: {}", message)
    sendTo(Array(ExternalUser), message)
  }

  private def reStartSubject(userID: UserID) {
    if (subjects.contains(userID)) {
      blockingHandlerActor !! BlockUser(userID)
      increaseSubjectCounter()
      subjects(userID).running = true
      // start the execution
      val msg = StartSubjectExecution()
      subjects(userID) ! msg
    } else {
      log.error("User %i unknown for subject %s, (re)start failed!"
        .format(userID, subject.id))
    }
  }

  private case class SubjectInfo(
    ref: Future[SubjectRef],
    userID: UserID,
    var running: Boolean = true) extends ClassTraceLogger {

    def tell(message: Any, from: ActorRef) {
      log.debug("FORWARD: {} TO {} FROM {}", message, ref, from)
      log.debug("subject creation completed: {}", ref.isCompleted)

      ref.onComplete {
        case r =>
          log.debug("ref.onComplete: r = {}", r)
          log.debug("ref.onComplete: ref = {}", ref)
          log.debug("subject creation completed: {}", ref.isCompleted)
          if (r.isSuccess) {
            log.info("sending: {} to subject: {}", message, r.get)
            r.get.tell(message, from)
          } else {
            // TODO exception or log?
            throw new Exception("Subject Creation failed for " +
              processInstanceID + "/" + subject.id + "@" + userID + "\nreason" + r)
          }
      }
    }

    def !(message: Any) {
      tell(message, context.self)
    }
  }
}
