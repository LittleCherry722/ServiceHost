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
import akka.actor.{ActorSystem, Props, ActorLogging}
import akka.pattern.ask
import spray.http.{HttpIp, StatusCodes, HttpResponse}
import de.tkip.sbpm.repo.RepoActor._
import scala.concurrent.duration._
import akka.util.Timeout


object Boot extends App with SimpleRoutingApp {

  implicit val system = ActorSystem("repo")
  implicit val timeout = Timeout(5 seconds)
  implicit val executionContext = system.dispatcher
  val repoActor = system.actorOf(Props[RepoActor])


  startServer(interface = "localhost", port = 8181) {
    logRequest("MARK 1") {
      clientIP { ip =>
        pathPrefix("repo") {
          path("reset") {
            (get | put) {
              ctx =>
                repoActor ! Reset
                ctx.complete(HttpResponse(status = StatusCodes.OK))
            }
          } ~ pathPrefix("implementations") {
            get {
              path(Rest) {
                subjectId =>
                  complete {
                    (repoActor ? GetImplementations(subjectId)).mapTo[String]
                  }
              }
            }
          } ~ pathPrefix("interfaces") {
            get {
              path("") {
                complete {
                  (repoActor ? GetAllInterfaces).mapTo[String]
                }
              } ~ path(IntNumber) {
                id =>
                  complete {
                    (repoActor ? GetInterface(id)).mapTo[String]
                  }
              }
            } ~ post {
              path("") {
                entity(as[String]) {
                  entry =>
                    val future = (repoActor ? AddInterface(ip, entry)).mapTo[Option[String]]
                    onSuccess(future) {
                      case Some(s) => complete(s)
                      case None => complete(HttpResponse(status = StatusCodes.InternalServerError))
                    }
                }
              }
            }
          }
        }
      }
    }
  }
}
