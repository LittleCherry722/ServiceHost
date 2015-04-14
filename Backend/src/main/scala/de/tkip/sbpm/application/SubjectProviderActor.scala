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
import akka.pattern.{ ask, pipe }
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

case class SubjectTerminated(userID: UserID, subjectID: SubjectID, processInstanceID: ProcessInstanceID, proper: Boolean) extends SubjectProviderMessage

case class AskSubjectsForAvailableActions(userID: UserID,
  processInstanceID: ProcessInstanceID = AllProcessInstances,
  subjectID: SubjectID = AllSubjects,
  generateAnswer: Array[AvailableAction] => Any)
  extends SubjectProviderMessage

class SubjectProviderActor(userID: UserID) extends InstrumentedActor {

  val logger = Logging(context.system, this)

  private var subjects = Map[(ProcessInstanceID, SubjectID), SubjectRef]()

  private val processManagerActor = ActorLocator.processManagerActor

  processManagerActor ! RegisterSubjectProvider(userID, self)

  def wrappedReceive = {
    case subjectCreated: SubjectCreated => {
      if (subjectCreated.userID != userID) log.warning("userID mismatch!")
      subjects += (((subjectCreated.processInstanceID, subjectCreated.subjectID), subjectCreated.ref))
    }

    case st: SubjectTerminated => {
      log.info("SubjectTerminated: " + st)
      subjects -= ((st.processInstanceID, st.subjectID))
    }

    case get: GetAvailableActions => {
      if (get.isInstanceOf[Debug]) {
        val msg =
          AvailableActionsAnswer(get, DebugActionData.generateActions(get.userID, get.processInstanceID))
        sender !! msg
      } else {
        val f = askSubjectsForAvailableActions(
          get.processInstanceID,
          get.subjectID,
          (actions: Array[AvailableAction]) =>
            AvailableActionsAnswer(get, actions))
        f pipeTo sender
      }
    }

    case AskSubjectsForAvailableActions(_, processInstanceID, subjectID, generateAnswer) => {
      val f = askSubjectsForAvailableActions(
        processInstanceID,
        subjectID,
        generateAnswer)
      f pipeTo sender
    }

    // send subject messages direct to the subject
    case message: SubjectMessage => {
      val ref = subjects((message.processInstanceID, message.subjectID))
      ref ! message
    }

    // general matching
    // Route processInstance messages to the process manager
    case message: ProcessInstanceMessage => {
      processManagerActor ! message
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
    generateAnswer: Array[AvailableAction] => Any): Future[Any] = {

    val collectedSubjectRefs: Iterable[SubjectRef] =
      if (processInstanceID != AllProcessInstances && subjectID != AllSubjects) {
        Seq(subjects((processInstanceID, subjectID)))
      }
      else {
        subjects.filter{
          case ((pID, sID),_) => {
            (pID == processInstanceID || processInstanceID == AllProcessInstances) &&
              (sID == subjectID || subjectID == AllSubjects)
          }
        }.map(_._2)
      }

    logger.debug("collectedSubjectsRefs: " + collectedSubjectRefs)

    implicit val timeout = akka.util.Timeout(5 seconds) // TODO how long the timeout?

    val actionFutureSeq: Seq[Future[Seq[Seq[AvailableAction]]]] =
      for (subject <- collectedSubjectRefs.toArray)
        yield (subject ? GetAvailableAction(processInstanceID)).mapTo[Seq[Seq[AvailableAction]]]
    val nestedActionFutures = Future.sequence(actionFutureSeq)
    // flatten the actions
    val actionFutures = for (outer <- nestedActionFutures)
        yield for {
          middle <- outer
          inner <- middle
          action <- inner
        } yield action

    // return result as Future
    actionFutures.map(actions => generateAnswer(actions.toArray))
  }
}
