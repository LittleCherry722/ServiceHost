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

import akka.event.Logging
import de.tkip.sbpm.model._
import spray.httpx.SprayJsonSupport._
import spray.routing.SimpleRoutingApp
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import spray.http.{StatusCodes, HttpResponse}
import de.tkip.sbpm.repo.InterfaceActor._
import de.tkip.sbpm.repo.IntermediateInterfaceActor.ConvertToInterface
import scala.concurrent.duration._
import akka.util.Timeout
import de.tkip.sbpm.model.GraphJsonProtocol._
import reflect.ClassTag
import de.tkip.sbpm.persistence.DatabaseAccess



object Boot extends App with SimpleRoutingApp {
  import de.tkip.sbpm.repo.InterfaceActor.MyJsonProtocol._

  implicit val system = ActorSystem("repo")
  implicit val timeout = Timeout(30 seconds)
  implicit val executionContext = system.dispatcher
  val interfaceActor = system.actorOf(Props[InterfaceActor])
  val intermediateInterfaceActor = system.actorOf(Props[IntermediateInterfaceActor])

  DatabaseAccess.init()

  startServer(interface = "localhost", port = 8181) {
    logRequest("MARK 1") {
      clientIP { ip =>
        pathPrefix("repo") {
          path("reset") {
            (get | put) {
              ctx =>
                interfaceActor ! Reset
                ctx.complete(HttpResponse(status = StatusCodes.OK))
            }
          } ~ pathPrefix("implementations") {
            get {
              pathEnd {
                parameter("subjectIds") {
                  (subjectIdsString) =>
                    complete {
                      (interfaceActor ? GetImplementations(subjectIdsString.split("::"))).mapTo[Map[String, Seq[InterfaceImplementation]]]
                    }
                }
              }
            }
          } ~ pathPrefix("blackbox") {
            get {
              path(Segment / Segment) {
                (subjectId, blackboxname) => complete {
                  (interfaceActor ? GetBlackbox(subjectId, blackboxname)).mapTo[Option[Interface]] // TODO: list or single one?
                }
              }
            }
          } ~ pathPrefix("interfaces") {
            get {
              pathEnd {
                complete {
                  (interfaceActor ? GetAllInterfaces).mapTo[Seq[Interface]]
                }
              } ~ path(IntNumber) {
                id =>
                  complete {
                    (interfaceActor ? GetInterface(id)).mapTo[Option[Interface]]
                  }
              }
            } ~ post {
              pathEnd {
                entity(as[IntermediateInterface]) { iInterface =>
                  val future = for {
                    interface <- (intermediateInterfaceActor ? ConvertToInterface(iInterface, ip)).mapTo[Interface]
                    response <- (interfaceActor ? AddInterface(interface)).mapTo[Option[String]]
                  } yield response
                  onSuccess(future) {
                    case Some(s) => complete(s)
                    case None => complete(HttpResponse(status = StatusCodes.InternalServerError))
                  }
                }
              }
            } ~ delete {
              path(IntNumber) { interfaceId =>
                interfaceActor ! DeleteInterface(interfaceId)
                complete(StatusCodes.OK)
              }
            }
          }
        }
      }
    }
  }
}
