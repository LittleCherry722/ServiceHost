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
      // TODO might check if some subjects has terminated

      // ask every subjects for the available action
      val futures: Array[Future[AvailableAction]] =
        for (subject <- subjects.filterNot(_.isTerminated).toArray)
          yield (subject ? GetAvailableAction(processInstanceID)).asInstanceOf[Future[AvailableAction]]

      // await all question results parallel
      val actions = for (future <- futures.par)
        yield Await.result(future, timeout.duration)

      // results ready -> generate answer -> return
      // TODO for the moment filter endstatetype, later think about a better idea
      sender ! generateAnswer(actions.toArray)

      // actions collected -> stop this actor
      context.stop(self)
    }
  }
}
