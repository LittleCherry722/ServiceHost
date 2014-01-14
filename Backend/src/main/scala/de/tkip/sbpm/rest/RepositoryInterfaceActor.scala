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

import scala.collection.Seq
import spray.httpx.marshalling.Marshaller
import spray.http.Uri
import de.tkip.sbpm.logging.DefaultLogging
import spray.httpx.encoding.Decoder
import scala.util.matching.Regex
import scala.collection.immutable.Map
import spray.routing.directives.AuthMagnet
import spray.httpx.unmarshalling.Deserializer
import spray.routing.ExceptionHandler
import akka.actor.ActorRefFactory
import spray.util.LoggingContext
import scala.reflect.ClassTag
import akka.event.LoggingAdapter
import spray.http._
import scala.concurrent._
import spray.client.pipelining._
import spray.routing._
import spray.json._
import ExecutionContext.Implicits.global
import de.tkip.sbpm.application.miscellaneous.SystemProperties
import akka.actor.{ ActorRef, Actor, Props }
import spray.routing._
import spray.http._
import spray.client.pipelining._
import de.tkip.sbpm.rest.auth.CookieAuthenticator
import de.tkip.sbpm.rest.auth.SessionDirectives._
import de.tkip.sbpm.logging.DefaultLogging
import de.tkip.sbpm.logging.LoggingResponseActor
import spray.json._
import spray.httpx.SprayJsonSupport._
import de.tkip.sbpm.bir._
import de.tkip.sbpm.application.history._
import de.tkip.sbpm.rest._
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
      val pipeline: HttpRequest => Future[String] = (
        sendReceive
        ~> unmarshal[String])
      //TODO: forward error codes (such as 404) instead of delivering a 500 response
      get {
        requestContext =>
          requestContext.complete {
            val response = pipeline(Get(repoLocation + requestContext.unmatchedPath.toString))
          }
            response
      } ~
        post {
          requestContext =>
            requestContext.complete {
              val jsWithAddress = attachExternalAddress(requestContext)

              val response = pipeline(Post(repoLocation, jsWithAddress))
              response
            }
        }
    }
  })

  private def attachExternalAddress(requestContext: RequestContext): String = {
    val jsObject: JsObject = requestContext.request.entity.asString.asJson.asJsObject

    val url = SystemProperties.akkaRemoteUrl(context.system.settings.config)
    jsObject.copy(Map("url" -> (url).toJson) ++ jsObject.fields).toString
  }
}
