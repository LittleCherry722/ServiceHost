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

import akka.actor.Actor
import akka.actor.actorRef2Scala
import akka.event.Logging

import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model._

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
class ContextResolverActor extends Actor {

  val logger = Logging(context.system, this)

  val subjectInstanceMap =
    mutable.Map.empty[(ProcessID, ProcessInstanceID, SubjectID), UserID]

  def receive = {
    // register SingleSubjectInstance
    // nur enie SSInstance pro PI erlaubt
    // SingleSubjectInstanceInfo(ProcessInstanceId, SubjectId, UserId)
    case RegisterSingleSubjectInstance(processId, processInstanceId, subjectId, userId) =>
      subjectInstanceMap += (processId, processInstanceId, subjectId) -> userId

    case ruid: RequestUserID =>
      sender ! ruid.generateAnswer(evaluateUserID(ruid.subjectInformation))

    case ss => logger.error("ContextResolver not yet implemented Message: " + ss)
  }

  private def evaluateUserID(subjectInformation: SubjectInformation): Array[UserID] = {
    subjectInformation match {
      // Registered singleSubject
      case SubjectInformation(processId, processInstanceId, subjectId) if {
        subjectInstanceMap contains ((processId, processInstanceId, subjectId))
      } => Array(subjectInstanceMap((processId, processInstanceId, subjectId)))
      case _ => Array(1)
    }
    //    Array(1)
  }
}