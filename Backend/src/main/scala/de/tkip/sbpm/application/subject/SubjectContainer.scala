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
import de.tkip.sbpm.application.{ MappingInfo, SubjectCreated, RegisterSingleSubjectInstance }
import akka.event.LoggingAdapter
import akka.actor.ActorRef
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.model.SubjectLike
import de.tkip.sbpm.application.miscellaneous.BlockUser
import de.tkip.sbpm.application.miscellaneous.UnBlockUser
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.util.Timeout
import akka.event.Logging

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
  mapping: Option[MappingInfo],
  increaseSubjectCounter: () => Unit,
  decreaseSubjectCounter: () => Unit)(implicit context: ActorContext) {
  import scala.collection.mutable.{ Map => MutableMap }

  implicit val timeout = Timeout(30 seconds)

  private val multi = subject.multi
  private val single = !multi
  private val external = subject.external

  private val subjects = MutableMap[UserID, SubjectInfo]()

  /**
   * Adds a Subject to this multisubject
   */
  // TODO ueberarbeiten
  def createSubject(userID: UserID) {
    log.debug("SubjectContainer.createSubject: " + userID);
    val registerMsg = RegisterSingleSubjectInstance(processID, processInstanceID, subject.id, userID)
    log.debug("Created: " + registerMsg);
    if (single) {
      if (subjects.size > 0) {
        log.error("Single subjects cannot be created twice")
        return
      }
      // register this subject at the context resolver so other subject dont
      // try to send to wrong instances
      log.debug("TRACE: from SubjectContainer to " + ActorLocator.contextResolverActor + " " + registerMsg)
      ActorLocator.contextResolverActor ! registerMsg
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
    if (!external) {
      // create subject
      val subjectRef =
        context.actorOf(Props(new SubjectActor(subjectData)), "SubjectActor____" + UUID.randomUUID().toString())
      // and store it in the map
      subjects += userID -> SubjectInfo(Future.successful(subjectRef), userID, log)

      val msg = SubjectCreated(userID, processID, processInstanceID, subject.id, subjectRef)
      // inform the subject provider about his new subject
      log.debug("TRACE: from SubjectContainer to " + context.parent + " " + msg)
      context.parent ! msg
        

      reStartSubject(userID)
    } else {
      log.debug("CREATE: {}", subjectData.subject)

      // process schon vorhanden?
      // TODO mit futures
      val getProxyMsg = GetProcessInstanceProxy(mapping.get.processId, mapping.get.address)
      log.debug("TRACE: from SubjectContainer to " + processInstanceManager + " " +  getProxyMsg)
      val processInstanceRef = (processInstanceManager ? getProxyMsg).mapTo[ActorRef]

      log.debug("CREATE: processInstanceRef = {}", processInstanceRef)

      // TODO we need this unblock!
      log.debug("TRACE: from SubjectContainer to " + blockingHandlerActor + " " + UnBlockUser(userID))
      blockingHandlerActor ! UnBlockUser(userID)

      subjects += userID -> SubjectInfo(processInstanceRef, userID, log)
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
      log.debug("TRACE: from SubjectContainer to " + subjects(message.userID) + " " + message)
      subjects(message.userID).tell(message, context.sender)
    }
  }

  /**
   * Forwards the message to the array of subjects
   */
  private def sendTo(targetSubjects: Array[UserID],
    message: SubjectToSubjectMessage) {

    for (userID <- targetSubjects) {
      if (!subjects.contains(userID)) {
        createSubject(userID)
      } else if (!subjects(userID).running) {
        reStartSubject(userID)
      }

      log.debug("SEND: {}", message)

      if (external) {
        // exchange the target subject id
        message.target.subjectID = mapping.get.subjectId
        log.debug("SEND (target exchanged): {}", message)

        // TODO we need this unblock!
        val unblockMsg = UnBlockUser(userID)
        log.debug("TRACE: from SubjectContainer to " + blockingHandlerActor + " " + unblockMsg)
        blockingHandlerActor ! unblockMsg
      }

      //        blockingHandlerActor ! BlockUser(userID)
      log.debug("TRACE: from SubjectContainer to " + subjects(userID) + " " + message)
      subjects(userID).tell(message, context.sender)
    }
  }

  def sendToExternal(message: SubjectToSubjectMessage) {
    sendTo(Array(ExternalUser), message)
  }

  private def reStartSubject(userID: UserID) {
    if (subjects.contains(userID)) {
      val blockMsg = BlockUser(userID)
      log.debug("TRACE: from SubjectContainer to " + blockingHandlerActor + " " + blockMsg)
      blockingHandlerActor ! blockMsg

      increaseSubjectCounter()
      subjects(userID).running = true

      // start the execution
      val startMsg = StartSubjectExecution()
      log.debug("TRACE: from SubjectContainer to " + subjects(userID) + " " + startMsg)
      subjects(userID) ! startMsg
    } else {
      log.error("User %i unknown for subject %s, (re)start failed!"
        .format(userID, subject.id))
    }
  }

  private case class SubjectInfo(
    ref: Future[SubjectRef],
    userID: UserID,
    log: LoggingAdapter,
    var running: Boolean = true) {

    def tell(message: Any, from: ActorRef) {
      log.debug("FORWARD: {} TO {} FROM {}", message, ref, from)
      log.debug("subject creation completed: {}", ref.isCompleted)

      ref.onComplete {
        case r =>
          log.debug("ref.onComplete: r = {}", r)
          log.debug("ref.onComplete: ref = {}", ref)
          log.debug("subject creation completed: {}", ref.isCompleted)
          if (r.isSuccess) {
            log.debug("TRACE: from " + this + " to " + r.get + " " + message)
            r.get.tell(message, from)
          }
          // TODO exception or logg?
          else throw new Exception("Subject Creation failed for " +
            processInstanceID + "/" + subject.id + "@" + userID + "\nreason" + r)
      }
    }

    def !(message: Any) {
      tell(message, context.self)
    }
  }
}
