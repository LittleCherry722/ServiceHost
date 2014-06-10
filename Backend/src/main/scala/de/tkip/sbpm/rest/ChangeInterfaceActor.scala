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

package de.tkip.sbpm.rest

import akka.actor.Actor
import de.tkip.sbpm._
import de.tkip.sbpm.application.history._
import spray.routing.HttpService
import de.tkip.sbpm.logging.DefaultLogging
import de.tkip.sbpm.rest.JsonProtocol._
import de.tkip.sbpm.rest.SprayJsonSupport._
import akka.util.Timeout
import scala.concurrent.duration._
import de.tkip.sbpm.application.change._
import spray.json._
import de.tkip.sbpm.model._
import akka.pattern.ask
import scala.util.{ Success, Failure }

class ChangeInterfaceActor extends AbstractInterfaceActor {

  import context.dispatcher
  implicit val timeout = Timeout(15 seconds)
  private lazy val processManagerActor = ActorLocator.processManagerActor
  private lazy val changeActor = ActorLocator.changeActor
  def actorRefFactory = context

  def routing = runRoute {
    get {
      // frontend request
      pathPrefix("") {
        parameter("t") { (time) =>
            complete {
              //          log.debug(s"${getClass.getName} received polling request with timestemp: $time")
              val historyMsg = GetHistorySince(time.toLong)
              val processMsg = GetProcessChange(time.toLong)
              val actionMsg = GetActionChange(time.toLong)
              val processInstanceMsg = GetProcessInstanceChange(time.toLong)
              val messageMsg = GetMessageChange(time.toLong, userId)

              log.debug("TRACE: from " + this.self + " to " + processManagerActor + " " + historyMsg)
              log.debug("TRACE: from " + this.self + " to " + changeActor + " " + processMsg)
              log.debug("TRACE: from " + this.self + " to " + changeActor + " " + actionMsg)
              log.debug("TRACE: from " + this.self + " to " + changeActor + " " + processInstanceMsg)
              log.debug("TRACE: from " + this.self + " to " + changeActor + " " + messageMsg)

              val historyFuture = (processManagerActor ? historyMsg).mapTo[Option[HistoryRelatedChange]]
              val processFuture = (changeActor ? processMsg).mapTo[Option[ProcessRelatedChange]]
              val actionFuture = (changeActor ? actionMsg).mapTo[Option[ActionRelatedChange]]
              val processInstanceFuture = (changeActor ? processInstanceMsg).mapTo[Option[ProcessInstanceRelatedChange]]
              val messageFuture = (changeActor ? messageMsg).mapTo[Option[MessageRelatedChange]]

              val future = 
                for {
                  history <- historyFuture
                  process <- processFuture
                  action <- actionFuture
                  processInstance <- processInstanceFuture
                  message <- messageFuture

                  result = ChangeRelatedData(process, processInstance, action, history, message)	  
                } yield result

              future.map(result => result)
            }
        }
      }
    }
  }
}
