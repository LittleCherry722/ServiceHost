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

import de.tkip.sbpm.instrumentation.InstrumentedActor
import akka.actor._
import akka.pattern.ask
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.application.subject._
import de.tkip.sbpm.application._
import de.tkip.sbpm.ActorLocator
import akka.event.Logging
import de.tkip.sbpm.application.subject.misc.AvailableAction
import java.util.UUID
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

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

class SubjectProviderActor(userID: UserID) extends InstrumentedActor {

  val logger = Logging(context.system, this)

  private type Subject = SubjectCreated

  private var subjects = Set[Subject]()

  private lazy val processManagerActor = ActorLocator.processManagerActor

  processManagerActor ! RegisterSubjectProvider(userID, self)

  def wrappedReceive = {
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
        val msg =
          AvailableActionsAnswer(get, DebugActionData.generateActions(get.userID, get.processInstanceID))
        sender !! msg
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
      log.error("SubjectProvider not yet implemented: " + s)
    }
  }

  private def askSubjectsForAvailableActions(processInstanceID: ProcessInstanceID,
    subjectID: SubjectID,
    generateAnswer: Array[AvailableAction] => Any)(returnAdress: ActorRef = self) {

    val collectSubjects: Iterable[SubjectRef] =
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
                  subjectID == s.subjectID))).map(_.ref)

    implicit val timeout = akka.util.Timeout(3 seconds) // TODO how long the timeout?

    val actionFutureSeq: Seq[Future[Seq[Seq[AvailableAction]]]] =
      for (subject <- collectSubjects.filterNot(_.isTerminated).toArray)
        yield (subject ? GetAvailableAction(processInstanceID)).mapTo[Seq[Seq[AvailableAction]]]
    val nestedActionFutures = Future.sequence(actionFutureSeq)
    // flatten the actions
    val actionFutures =
      for (outer <- nestedActionFutures)
        yield for (middle <- outer; inner <- middle; action <- inner) yield action

    // Await the result
    // TODO can be done smarter, but at the moment this actor has a single run
    val actions =
      Await.result(actionFutures, timeout.duration)
    logger.debug("Collected: " + actions)

    val message= generateAnswer(actions.toArray)

    // collect actions and generate answer for the filtered subject list

    val msg = CollectAvailableActions(message)
    
    context.actorOf(Props(new SubjectActionsCollector), "SubjectActionsCollector____" + UUID.randomUUID().toString()).!(msg)(returnAdress)
  }
}
