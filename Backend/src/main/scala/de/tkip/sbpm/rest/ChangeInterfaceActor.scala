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

import scala.concurrent.duration._

import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.change._
import de.tkip.sbpm.application.history._
import de.tkip.sbpm.logging.DefaultLogging
import de.tkip.sbpm.model._
import de.tkip.sbpm.rest.JsonProtocol._
import spray.httpx.SprayJsonSupport._

class ChangeInterfaceActor extends AbstractInterfaceActor with DefaultLogging {

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
              val future = 
                for {
                  history <- (processManagerActor ? GetHistorySince(time.toLong)).mapTo[Option[HistoryRelatedChange]]
                  process <- (changeActor ? GetProcessChange(time.toLong)).mapTo[Option[ProcessRelatedChange]]
                  action <- (changeActor ? GetActionChange(time.toLong)).mapTo[Option[ActionRelatedChange]]
                  processInstance <- (changeActor ? GetProcessInstanceChange(time.toLong)).mapTo[Option[ProcessInstanceRelatedChange]]
                  message <- (changeActor ? GetMessageChange(time.toLong, userId)).mapTo[Option[MessageRelatedChange]]
                  result = ChangeRelatedData(process, processInstance, action, history, message)	  
                } yield result
                
                future.map(result => result)
            }
            }
      }
    }
  }
}
