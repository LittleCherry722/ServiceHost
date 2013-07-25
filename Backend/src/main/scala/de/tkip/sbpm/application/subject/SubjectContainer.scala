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

import de.tkip.sbpm.model.Subject
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import akka.actor.ActorContext
import akka.actor.Props
import akka.pattern.ask
import de.tkip.sbpm.application.miscellaneous.SubjectMessage
import de.tkip.sbpm.application.SubjectCreated
import akka.event.LoggingAdapter
import akka.actor.ActorRef
import de.tkip.sbpm.application.miscellaneous.BlockUser
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.RegisterSingleSubjectInstance
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.model.SubjectLike
import scala.concurrent.Await
import de.tkip.sbpm.model.ExternalSubject
import de.tkip.sbpm.model.ExternalSubject
import de.tkip.sbpm.application.miscellaneous.BlockUser
import de.tkip.sbpm.application.miscellaneous.UnBlockUser

/**
 * This class is responsible to hold a subjects, and can represent
 * a single subject or a multisubject
 */
class SubjectContainer(
  subject: SubjectLike,
  processID: ProcessID,
  processInstanceID: ProcessInstanceID,
  processInstanceManager: ActorRef,
  logger: LoggingAdapter,
  blockingHandlerActor: ActorRef,
  increaseSubjectCounter: () => Unit,
  decreaseSubjectCounter: () => Unit)(implicit context: ActorContext) {
  import scala.collection.mutable.{ Map => MutableMap }

  private val multi = subject.multi
  private val single = !multi
  private val external = subject.external

  private val subjects = MutableMap[UserID, SubjectInfo]()

  /**
   * Adds a Subject to this multisubject
   */
  // TODO ueberarbeiten
  def createSubject(userID: UserID) {
    System.err.println("CREATED: " + RegisterSingleSubjectInstance(processID, processInstanceID, subject.id, userID));
    if (single) {
      if (subjects.size > 0) {
        logger.error("Single subjects cannot be created twice")
        return
      }
      // register this subject at the context resolver so other subject dont
      // try to send to wrong instances
      ActorLocator.contextResolverActor !
        RegisterSingleSubjectInstance(processID, processInstanceID, subject.id, userID)
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
    if (!subjectData.subject.external) {

      // create subject
      val subjectRef =
        context.actorOf(Props(new SubjectActor(subjectData)))

      // and store it in the map
      subjects += userID -> SubjectInfo(subjectRef, userID)

      // inform the subject provider about his new subject
      context.parent !
        SubjectCreated(userID, processID, processInstanceID, subject.id, subjectRef)

      reStartSubject(userID)
    } else {
      System.err.println("CREATE: " + subjectData.subject);

      // process schon vorhanden?
      // TODO ohne ask!
      implicit val timeout = akka.util.Timeout(3500)
      val ext = subjectData.subject.asInstanceOf[ExternalSubject]

      val subjectRef =
        Await.result(
          (processInstanceManager ?
            GetSubjectAddr(userID, ext.relatedProcessId, ext.relatedSubjectId))
            .mapTo[ActorRef],
          timeout.duration)
      // TODO we need this unblock!
      blockingHandlerActor ! UnBlockUser(userID)

      subjects += userID -> SubjectInfo(subjectRef, userID)
    }

    logger.debug("Processinstance [" + processInstanceID + "] created Subject " +
      subject.id + " for user " + userID)

    // TODO raus
    System.err.println("Processinstance [" + processInstanceID + "] created Subject " +
      subject.id + " for user " + userID)
  }

  def handleSubjectTerminated(message: SubjectTerminated) {

    logger.debug("Processinstance [" + processInstanceID + "] Subject " + subject.id + "[" +
      message.userID + "] terminated")

    // decrease the subject counter
    decreaseSubjectCounter()

    subjects(message.userID).running = false
  }

  /**
   * Forwards a message to all Subjects of this MultiSubject
   */
  def send(message: SubjectToSubjectMessage) {

    if (message.target.toVariable) {
      // TODO why not targetUsers = var subjects?
      sendTo(message.target.varSubjects.map(_._2), message)
    } else {
      sendTo(message.target.targetUsers, message)
    }
  }

  def send(message: SubjectMessage) {
    if (subjects.contains(message.userID)) {
      subjects(message.userID).ref.forward(message)
    }
  }

  def send(sender: ActorRef, message: GetSubjectAddr) {
    if (!subjects.contains(message.userId)) {
      createSubject(message.userId)
    }

    sender ! subjects(message.userId).ref
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

      System.err.println("SEND: " + message);
      if (subject.external) {
        // exchange the target subject id
        message.target.subjectID = subject.asInstanceOf[ExternalSubject].relatedSubjectId
        
        // TODO we need this unblock!
        blockingHandlerActor ! UnBlockUser(userID)
      }
      println("SEND: " + message);

      //        blockingHandlerActor ! BlockUser(userID)
      subjects(userID).ref.forward(message)
    }
  }

  private def reStartSubject(userID: UserID) {
    if (subjects.contains(userID)) {
      blockingHandlerActor ! BlockUser(userID)
      increaseSubjectCounter()
      subjects(userID).running = true
      // start the execution
      subjects(userID).ref ! StartSubjectExecution()
    } else {
      logger.error("User %i unknown for subject %s, (re)start failed!"
        .format(userID, subject.id))
    }
  }

  private case class SubjectInfo(
    ref: SubjectRef,
    userID: UserID,
    var running: Boolean = true)
}