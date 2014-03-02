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
import akka.util._
import scala.concurrent.duration._
import spray.json._
import de.tkip.sbpm.rest.JsonProtocol._
import scalaj.http.{Http, HttpOptions}
import scala.concurrent.{ExecutionContext, Future}
import de.tkip.sbpm.persistence.query.Roles
import ExecutionContext.Implicits.global
import de.tkip.sbpm.model.Role
import scala.concurrent.{Await}
import de.tkip.sbpm.ActorLocator
import akka.pattern.ask
import akka.event.Logging

case class SaveInterface(json: GraphHeader)
case class DeleteInterface(interfaceId: Int)

class RepositoryPersistenceActor extends Actor with DefaultLogging {

  private val logger = Logging(context.system, this)
  // akka config prefix
  protected val configPath = "sbpm."

  // read string from akka config
  protected def configString(key: String) =
    context.system.settings.config.getString(configPath + key)

  private val repoLocation = configString("repo.address")
  private lazy val persistanceActor = ActorLocator.persistenceActor


  implicit val timeout = Timeout(1 seconds)

  def actorRefFactory = context

  def receive = {
    case SaveInterface(gHeader) => {
      logger.debug("[SAVE INTERFACE] save message received")
      val roles = Await.result((persistanceActor ? Roles.Read.All).mapTo[Seq[Role]], 2 seconds)
      logger.debug("[SAVE INTERFACE] role mapping received")
      implicit val roleMap = roles.map(r => (r.name, r)).toMap
      val jsObject = gHeader.toJson(createGraphHeaderFormat(roleMap)).asJsObject()

      val port = SystemProperties.akkaRemotePort(context.system.settings.config)
      val interface = jsObject.copy(Map("port" -> port.toJson) ++ jsObject.fields).toString()
      logger.debug("[SAVE INTERFACE] sending message to repository... " + repoLocation + "interfaces")
      val result = Http.postData(repoLocation + "interfaces", interface)
        .header("Content-Type", "application/json")
        .header("Charset", "UTF-8")
        .option(HttpOptions.readTimeout(10000))
        .asString
      logger.debug("[SAVE INTERFACE] repository says: " + result)
      sender ! Some(result.toInt)
      logger.debug("[SAVE INTERFACE] sent repository answer to sender.a")
    }
    case DeleteInterface(interfaceId) => {
      logger.debug("[DELETE INTERFACE] delete message received")
      val result = Http(repoLocation + "interfaces/" + interfaceId)
        .method("DELETE")
        .header("Content-Type", "application/json")
        .header("Charset", "UTF-8")
        .option(HttpOptions.readTimeout(10000))
        .responseCode
      logger.debug("[SAVE INTERFACE] repository says: " + result)
    }
    case _ =>
      logger.debug("[INTERFACE PERSISTENCE ACTOR] invalid message received")
  }
}
