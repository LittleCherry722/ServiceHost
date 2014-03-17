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

package de.tkip.sbpm.repo

import spray.routing.SimpleRoutingApp
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import spray.http.{StatusCodes, HttpResponse}
import de.tkip.sbpm.repo.RepoActor._
import scala.concurrent.duration._
import akka.util.Timeout


object Boot extends App with SimpleRoutingApp {

  implicit val system = ActorSystem("repo")
  implicit val timeout = Timeout(30 seconds)
  implicit val executionContext = system.dispatcher
  val repoActor = system.actorOf(Props[RepoActor])


  startServer(interface = "localhost", port = 8181) {
    pathPrefix("repo") {
      get {
        path(IntNumber) {
          id =>
            val future = (repoActor ? GetEntry(id)).mapTo[Option[String]]

            onSuccess(future) {
              result =>
                result match {
                  case Some(s) => complete(s)
                  case None => complete(HttpResponse(status = StatusCodes.NotFound))
                }
            }
        } ~ path("reset") {
          ctx =>
            repoActor ! Reset
            ctx.complete(HttpResponse(status = StatusCodes.OK))
        } ~ path("") {
          complete {
            (repoActor ? GetEntries).mapTo[String]
          }
        }
      } ~
        post {
          entity(as[String]) {
            entry =>
              val future = (repoActor ? CreateEntry(entry)).mapTo[Option[String]]

              onSuccess(future) {
                result =>
                  result match {
                    case Some(s) => complete(s)
                    case None => complete(HttpResponse(status = StatusCodes.InternalServerError))
                  }
              }
          }
        }
    }
  }
}
