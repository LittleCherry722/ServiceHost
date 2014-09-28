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

import scala.collection.mutable

import de.tkip.sbpm.instrumentation.InstrumentedActor
import akka.actor.actorRef2Scala
import akka.event.Logging
import akka.util.Timeout
import akka.pattern._

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.persistence.query.Users
import de.tkip.sbpm.model.User
import scala.concurrent.Await
import scala.concurrent.duration._
import de.tkip.sbpm.logging.DefaultLogging

// this are the information which are required to evaluate the user id
case class SubjectInformation(
  processId: ProcessID,
  processInstanceId: ProcessInstanceID,
  subjectId: SubjectID)

case class RegisterSingleSubjectInstance(
  processId: ProcessID,
  processInstanceId: ProcessInstanceID,
  subjectId: SubjectID,
  userId: UserID)

// this message is to Request the user id and will be answered
// using generateAnswer with the userID
case class RequestUserID(subjectInformation: SubjectInformation, generateAnswer: Array[UserID] => Any)

/**
 * resolves the context of the subjects
 */
class ContextResolverActor extends InstrumentedActor {

  val subjectInstanceMap =
    mutable.Map.empty[(ProcessID, ProcessInstanceID, SubjectID), UserID]

  implicit val timeout = Timeout(1 seconds)

  def wrappedReceive = {
    // register SingleSubjectInstance
    // nur enie SSInstance pro PI erlaubt
    // SingleSubjectInstanceInfo(ProcessInstanceId, SubjectId, UserId)
    case RegisterSingleSubjectInstance(processId, processInstanceId, subjectId, userId) =>
      subjectInstanceMap += (processId, processInstanceId, subjectId) -> userId

    case ruid: RequestUserID =>
      val answer = ruid.generateAnswer(evaluateUserID(ruid.subjectInformation))
      sender !! answer

    case ss => log.error("ContextResolver not yet implemented Message: {}", ss)
  }

  private def evaluateUserID(subjectInformation: SubjectInformation): Array[UserID] = {
    subjectInformation match {
      case SubjectInformation(processId, processInstanceId, subjectId) if {
        subjectInstanceMap contains ((processId, processInstanceId, subjectId))
      } => {
        val userId = subjectInstanceMap((processId, processInstanceId, subjectId))
        log.debug("using registered user {} for lookup {}", userId, subjectInformation)
        Array(userId)
      }
      case SubjectInformation(processId, processInstanceId, subjectId) => {
        log.debug("searching users for {}", subjectInformation)
        val future = ActorLocator.persistenceActor ?? Users.Read.BySubject(subjectId, processInstanceId, processId)
        val users = Await.result(future, timeout.duration).asInstanceOf[Seq[User]]
        log.info("found {}", users)
        users.map(_.id.get).toArray
      }
      case si => {log.error("ContextResolver unable to find userIDs for: {}", si); Array()}
    }
  }
}
