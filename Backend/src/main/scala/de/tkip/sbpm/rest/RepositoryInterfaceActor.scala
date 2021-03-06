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

import scala.concurrent._
import ExecutionContext.Implicits.global
import de.tkip.sbpm.application.miscellaneous.SystemProperties
import akka.actor.{ ActorRef, Actor, Props }
import spray.routing._
import spray.http._
import spray.client.pipelining._
import de.tkip.sbpm.logging.DefaultLogging
import spray.json._
import scala.concurrent.Future
import DefaultJsonProtocol._

class RepositoryInterfaceActor extends AbstractInterfaceActor with DefaultLogging {

  // akka config prefix
  protected val configPath = "sbpm."

  // read string from akka config
  protected def configString(key: String) =
    context.system.settings.config.getString(configPath + key)

  private val repoLocation = configString("repo.address")

  def actorRefFactory = context

  def routing = runRoute({
    pathPrefix("") {
      val pipeline: HttpRequest => Future[String] = sendReceive ~> unmarshal[String]

      //TODO: forward error codes (such as 404) instead of delivering a 500 response
      get {
        requestContext =>
          requestContext.complete {
            val response = pipeline(Get(repoLocation + requestContext.unmatchedPath.toString))
            response
          }
      } ~
        post {
          requestContext =>
            requestContext.complete {
              val jsWithAddress = attachExternalAddress(requestContext)

              println("\n\n\n\n\n\n")
              println(jsWithAddress)
              println("\n\n\n\n\n\n")
              val response = pipeline(Post(repoLocation, jsWithAddress))
              response
            }
        }
    }
  })

  private def attachExternalAddress(requestContext: RequestContext): String = {
    val jsObject: JsObject = requestContext.request.entity.asString.asJson.asJsObject

    val port = SystemProperties.akkaRemotePort(context.system.settings.config)
    jsObject.copy(Map("port" -> port.toJson) ++ jsObject.fields).toString()
  }
}
