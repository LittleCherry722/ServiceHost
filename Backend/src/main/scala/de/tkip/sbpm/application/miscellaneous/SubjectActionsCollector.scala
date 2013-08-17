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

package de.tkip.sbpm.application.miscellaneous

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.event.Logging
import akka.actor._
import akka.pattern.ask
import scala.concurrent.Future
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.SubjectActor
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

case class CollectAvailableActions(subjects: Iterable[SubjectRef],
  processInstanceID: ProcessInstanceID,
  generateAnswer: Array[AvailableAction] => Any)

/**
 * This class is responsible to collect the available actions of a set of subjects
 */
class SubjectActionsCollector extends Actor {

  val logger = Logging(context.system, this)

  def receive = {
    case CollectAvailableActions(subjects, processInstanceID, generateAnswer) => {
      implicit val timeout = akka.util.Timeout(3 seconds) // TODO how long the timeout?

      val actionFutureSeq: Seq[Future[Seq[Seq[AvailableAction]]]] =
        for (subject <- subjects.filterNot(_.isTerminated).toArray)
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

      // results ready -> generate answer -> return
      sender ! generateAnswer(actions.toArray)

      // actions collected -> stop this actor
      context.stop(self)
    }
  }
}
