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
import akka.pattern.ask
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject._
import de.tkip.sbpm.application._
import de.tkip.sbpm.ActorLocator
import akka.event.Logging
import de.tkip.sbpm.application.subject.misc.AvailableAction

protected case class SubjectCreated(userID: UserID,
  processID: ProcessID,
  processInstanceID: ProcessInstanceID,
  subjectID: SubjectID,
  ref: SubjectRef)
  extends SubjectProviderMessage

case class AskSubjectsForAvailableActions(userID: UserID,
  processInstanceID: ProcessInstanceID = AllProcessInstances,
  subjectID: SubjectID = AllSubjects,
  generateAnswer: Array[AvailableAction] => Any)
  extends SubjectProviderMessage

class SubjectProviderActor(userID: UserID) extends Actor {

  val logger = Logging(context.system, this)

  private type Subject = SubjectCreated

  private var subjects = Set[Subject]()

  private lazy val processManagerActor = ActorLocator.processManagerActor

  processManagerActor ! RegisterSubjectProvider(userID, self)

  def receive = {
    case subject: SubjectCreated => {
      subjects += subject
    }

    case get: GetAvailableActions => {
      // TODO increase performance
      // remove the subjects the user is not interested about:
      // - terminated
      // - different process instance id
      // - different subject id
      if (get.isInstanceOf[Debug]) {
        sender !
          AvailableActionsAnswer(get, DebugActionData.generateActions(get.userID, get.processInstanceID))
      } else {
        askSubjectsForAvailableActions(
          get.processInstanceID,
          get.subjectID,
          (actions: Array[AvailableAction]) =>
            AvailableActionsAnswer(get, actions))()
      }
    }

    // TODO momentan ist es nicht moeglich den sender zu verwalten
    case AskSubjectsForAvailableActions(_, processInstanceID, subjectID, generateAnswer) => {
      askSubjectsForAvailableActions(
        processInstanceID,
        subjectID,
        generateAnswer)(sender)
    }

    // general matching
    // Route processInstance messages to the process manager
    case message: ProcessInstanceMessage => {
      processManagerActor ! message
    }

    // send subject messages direct to the subject
    case message: SubjectMessage => {
      // TODO muss performanter gehen weils nur ein subject ist
      for (
        subject <- subjects.filter({
          s: Subject =>
            s.processInstanceID == message.processInstanceID &&
              s.subjectID == message.subjectID
        })
      ) {
        subject.ref ! message
      }
    }

    case message: AnswerMessage => {
      // send the Answermessages to the SubjectProviderManager
      context.parent ! message // TODO forward oder tell?
    }

    case message: AnswerAbleMessage => {
      // just forward all messages from the frontend which are not
      // required in this Actor
      processManagerActor.forward(message)
    }

    case s => {
      logger.error("SubjectProvider not yet implemented: " + s)
    }
  }

  private def askSubjectsForAvailableActions(processInstanceID: ProcessInstanceID,
    subjectID: SubjectID,
    generateAnswer: Array[AvailableAction] => Any)(returnAdress: ActorRef = self) {

    val collectSubjects: Set[Subject] =
      subjects.filter(
        (s: Subject) =>
          !s.ref.isTerminated &&
            (if (processInstanceID == AllProcessInstances)
              true
            else
              (if (subjectID == AllSubjects)
                processInstanceID == s.processInstanceID
              else
                processInstanceID == s.processInstanceID &&
                  subjectID == s.subjectID)))

    // collect actions and generate answer for the filtered subject list
    context.actorOf(Props(new SubjectActionsCollector)).!(
      CollectAvailableActions(
        collectSubjects.map(_.ref),
        processInstanceID,
        generateAnswer))(returnAdress)
  }
}
