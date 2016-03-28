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
import de.tkip.sbpm.application.{ SubjectCreated, RegisterSingleSubjectInstance }
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
  messageMap: Map[Int, Map[MessageID, MessageID]],
  processID: ProcessID,
  processInstanceID: ProcessInstanceID,
  processInstanceManager: ActorRef,
  log: LoggingAdapter,
  blockingHandlerActor: ActorRef,
  increaseSubjectCounter: () => Unit,
  decreaseSubjectCounter: () => Unit)(implicit context: ActorContext) extends  ClassTraceLogger {

  import scala.collection.mutable.{ Map => MutableMap }

  private implicit val timeout = Timeout(30 seconds)
  implicit val traceName = this.getClass.getSimpleName

  private val multi = subject.multi
  private val single = !multi
  private val subjects = MutableMap[UserID, SubjectInfo]()

  /**
   * Adds a Subject to this multisubject
   */
  def createSubject(userID: UserID) {
    log.debug("SubjectContainer.createSubject: " + userID)
    log.debug("Created: " + RegisterSingleSubjectInstance(processID, processInstanceID, subject.id, userID))
    if (single) {
      if (subjects.nonEmpty) {
        log.error("Single subjects cannot be created twice")
        return
      }
      // register this subject at the context resolver so other subjects dont
      // try to send to wrong instances
      val msg = RegisterSingleSubjectInstance(processID, processInstanceID, subject.id, userID)
      ActorLocator.contextResolverActor !! msg
    }

    if (!subject.external) {
      val subjectData =
        SubjectData(
          userID,
          processID,
          processInstanceID,
          context.self,
          blockingHandlerActor,
          subject)
      // create subject
      val subjectRef = context.actorOf(Props(new SubjectActor(subjectData)), "SubjectActor____" + UUID.randomUUID().toString)
      // and store it in the map
      subjects += userID -> SubjectInfo(Future.successful(subjectRef), userID)

      // inform the subject provider about his new subject
      context.parent !! SubjectCreated(userID, processID, processInstanceID, subject.id, subjectRef)
      reStartSubject(userID)
    }
    log.debug("Processinstance [" + processInstanceID + "] created Subject " +
      subject.id + " for user " + userID)
  }

  def handleSubjectTerminated(message: SubjectTerminated) {

    log.debug("Processinstance [" + processInstanceID + "] Subject " + subject.id + "[" +
      message.userID + "] terminated")

    // decrease the subject counter
    decreaseSubjectCounter()

    subjects(message.userID).running = false
  }

  /**
   * Forwards a message to all Subjects associated with this SubjectContainer.
   * This could be multiple subjects in case of a multi subject, or just one
   * for the regular case of the subject being a single subject.
   */
  def send(message: SubjectToSubjectMessage) {
    val target = message.target
    if (target.toVariable) {
      log.info("Sending message to variable.")
      target.varSubjects.foreach {v =>
        v.messages.foreach {m =>
          log.info(s"sending message: ${message.messageName.name}")
          val newTarget = message.target.copy(subjectID = m.fromChannel.agent.subjectId)
          val newMessage = message.copy(target = newTarget)
          m.fromChannel.actor.tell(newMessage, context.sender)
        }
      }
    } else {
      for (userID <- target.targetUsers) {
        log.info("Sending message to user: {}", userID)
        if (!subjects.contains(userID)) {
          log.info("Subject Container creating new subject for user ID: {}", userID)
          createSubject(userID)
        } else if (!subjects(userID).running) {
          log.info("Subject Container restarting subject for user ID: :{}", userID)
          reStartSubject(userID)
        }

        log.debug("SEND: {}", message)

        // blockingHandlerActor !! BlockUser(userID)
        subjects(userID).tell(message, context.sender)
      }
    }
  }

  def send(message: SubjectMessage) {
    if (subjects.contains(message.userID)) {
      subjects(message.userID).tell(message, context.sender)
    }
  }

  private def reStartSubject(userID: UserID) {
    if (subjects.contains(userID)) {
      blockingHandlerActor !! BlockUser(userID)
      increaseSubjectCounter()
      subjects(userID).running = true
      // start the execution
      val msg = StartSubjectExecution
      subjects(userID) ! msg
    } else {
      log.error(s"User $userID unknown for subject ${subject.id}, (re)start failed!")
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
