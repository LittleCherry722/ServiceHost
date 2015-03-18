/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2015 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package de.tkip.servicehost.rest

import akka.actor.{ Actor, ActorRef, Props }

import spray.routing._
import spray.http._
import spray.http.Uri._
import spray.json._

import de.tkip.sbpm.logging.DefaultLogging
import de.tkip.sbpm.instrumentation.InstrumentedActor

class FrontendInterfaceActor extends InstrumentedActor with DefaultLogging with HttpService {
  private val frontendBaseUrl = "servicehost";
  private val frontendBaseDir = "../ProcessManagement/";
  private val frontendIndexFile = "index.html";

  private val receiver = runRoute({
    pathPrefix("isalive") {
      get {
        complete(StatusCodes.OK)
        // TODO do some health check stuff and return StatusCodes.OK
      }
    } ~
    get {
      serveStaticFiles
    }
  })

  def actorRefFactory = context
  def wrappedReceive = receiver

  def serveStaticFiles: Route = {
    // root folder -> redirect to frontendBaseUrl
    pathEnd {
      dynamic {
        log.error("Received GET to base")
        redirect("/" + frontendBaseUrl + "/", StatusCodes.MovedPermanently)
      }
    } ~
      // server other static content from dir
      pathPrefix(frontendBaseUrl) {
        pathPrefix(Rest) {
          case "" =>
            val file = new java.io.File(frontendBaseDir + frontendIndexFile)
            getFromFile(file)
          case str =>
            val path = new java.io.File(frontendBaseDir + str).getAbsolutePath
            getFromFile(path)
        }
      }
  }
}
