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

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.parsing.json.JSONObject

import akka.actor.{Actor, ActorLogging}
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import spray.routing.HttpService
import spray.json.JsonFormat
import spray.http.StatusCodes
import spray.http.MediaTypes._

import de.tkip.sbpm

class GoogleResponseActor extends Actor with HttpService with ActorLogging {
  

  implicit val timeout = Timeout(15 seconds)

  private lazy val googleAuthActor = sbpm.ActorLocator.googleAuthActor
    
  def actorRefFactory = context
  
  override def preStart() {
    log.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    log.debug(getClass.getName + " stopped.")
  }
  
  
  def receive = runRoute({
    
    post {
      // frontend request for authentication of SBPM app gainst Google account
      pathPrefix("init_auth") {
        formFields("id") { (id) => {
          log.debug(getClass.getName + " received authentication init post from user: " + id)
          
          val f = googleAuthActor ? sbpm.external.auth.InitUser(id)
          val result = Await.result(f.mapTo[String], timeout.duration)

          log.debug(getClass.getName + " Received state for user: " + id + " State: " + result)
          
          if (result == "AUTHENTICATED")
            complete(StatusCodes.NoContent)  // return OK with no content
          else // not authenticated or token expired
            complete(StatusCodes.OK, result) // return OK with google authentication URL
        }}
      }
     }~
    // callback endpoint called by Google after an authentication request
    get {
      path("") {
        parameters("code", "state") {(code, state) => {
          log.debug(getClass.getName + " received from google response: " + "name: " + state + ", code: " + code)
          googleAuthActor ! sbpm.external.auth.GoogleResponse(state, code)
          respondWithMediaType(`text/html`) {
            complete("<!DOCTYPE html>\n<html><head><script>window.close();</script></head><body></body></html>")
          }
        }
        }
      }
    }
  })
}
