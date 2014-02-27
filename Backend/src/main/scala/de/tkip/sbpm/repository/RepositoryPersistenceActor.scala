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

package de.tkip.sbpm.repository

import scala.collection.immutable.Map
import de.tkip.sbpm.application.miscellaneous.SystemProperties
import de.tkip.sbpm.logging.DefaultLogging
import akka.actor.{ ActorRef, Actor, Props }
import spray.json._
import DefaultJsonProtocol._
import de.tkip.sbpm.rest.JsonProtocol.GraphHeader
import spray.http._
import spray.client.pipelining._
import scala.concurrent.Future

case class SaveInterface(json: GraphHeader)
case class DeleteInterface(interfaceId: Int)

class RepositoryPersistenceActor extends Actor with DefaultLogging {

  // akka config prefix
  protected val configPath = "sbpm."

  // read string from akka config
  protected def configString(key: String) =
    context.system.settings.config.getString(configPath + key)

  private val repoLocation = configString("repo.address")

  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  def actorRefFactory = context

  def receive = {
    case SaveInterface(json) => {
      pipeline(Post(repoLocation + "interfaces", attachExternalAddress(json)))
    }
    case DeleteInterface(interfaceId) => {
      pipeline(Delete(repoLocation + "interfaces/" + interfaceId))
    }
  }

  private def attachExternalAddress(json: GraphHeader): JsObject = {
    val jsObject = json.toJson.asJsObject

    val port = SystemProperties.akkaRemotePort(context.system.settings.config)
    jsObject.copy(Map("port" -> port.toJson) ++ jsObject.fields)
  }
}
