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
import spray.http.{StatusCodes, HttpResponse}
import de.tkip.sbpm.repo.RepoActor._
import scala.concurrent.duration._
import akka.util.Timeout


object Boot extends App with SimpleRoutingApp {

  implicit val system = ActorSystem("repo")
  implicit val timeout = Timeout(5 seconds)
  implicit val executionContext = system.dispatcher
  val repoActor = system.actorOf(Props[RepoActor])


  startServer(interface = "localhost", port = 8181) {
    pathPrefix("repo") {
      path("reset") {
        (get | put) {
          ctx =>
            repoActor ! Reset
            ctx.complete(HttpResponse(status = StatusCodes.OK))
        }
      } ~ pathPrefix("implementations") {
        get {
          path("") {
            complete {
              (repoActor ? GetAllImplementations).mapTo[String]
            }
          } ~ path(IntNumber) {
            id =>
              complete {
                (repoActor ? GetImplementation(id)).mapTo[String]
              }
          }
        } ~ post {
          path("") {
            entity(as[String]) {
              entry =>
                val future = (repoActor ? AddImplementation(entry)).mapTo[Option[String]]
                onSuccess(future) {
                  case Some(s) => complete(s)
                  case None => complete(HttpResponse(status = StatusCodes.InternalServerError))
                }
            }
          }
        }
      } ~ pathPrefix("offers") {
        get {
          path("") {
            complete {
              (repoActor ? GetOffers).mapTo[String]
            }
          } ~ pathPrefix(IntNumber) {
            offerId =>
              path("") {
                val future = (repoActor ? GetOffer(offerId)).mapTo[Option[String]]
                onSuccess(future) {
                  case Some(s) => complete(s)
                  case None => complete(HttpResponse(status = StatusCodes.NotFound))
                }
              } ~ path("implementations") {
                val future = (repoActor ? GetOfferImplementations(offerId)).mapTo[Option[String]]
                onSuccess(future) {
                  case Some(s) => complete(s)
                  case None => complete(HttpResponse(status = StatusCodes.NotFound))
                }
              }
          }
        } ~ post {
          entity(as[String]) {
            entry =>
              val future = (repoActor ? AddOffer(entry)).mapTo[Option[String]]

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
