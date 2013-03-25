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

import akka.actor.Actor
import spray.routing.HttpService
import akka.actor.ActorLogging
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.external.auth.GoogleResponse
import scala.util.parsing.json.JSONObject
import scala.util.parsing.json.JSONObject
import spray.json.JsonFormat
import de.tkip.sbpm.external.auth.GoogleResponse
import de.tkip.sbpm.external.auth.GetAuthenticationState
import de.tkip.sbpm.external.auth.InitUser
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import spray.http.StatusCodes


class GoogleResponseActor extends Actor with HttpService with ActorLogging {
  

  implicit val timeout = Timeout(15 seconds)

  private lazy val googleAuthActor = ActorLocator.googleAuthActor
    
  def actorRefFactory = context
  
  override def preStart() {
    log.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    log.debug(getClass.getName + " stopped.")
  }
  
  
  def receive = runRoute({
    
     // a user posts his id on /initAuth in case he wants to authenticate the app against his google account
     post {
      pathPrefix("init_auth") {
        parameters("id") {(id) => {
          log.debug(getClass.getName + " received authentication init post from user: " + id)
          
          val future = googleAuthActor ? InitUser(id)
          val result = Await.result(future.mapTo[String], timeout.duration)

          log.debug(getClass.getName + " Received state for user: " + id + " State: " + result)
          
          if (result != "AUTHENTICATED") {
            // send back http ok with google authentication url in case user is not authenticated or
            // token is not valid any more
            complete(StatusCodes.OK, result)
          } else {
            // send back http ok with no content in case the user is already authenticated
            complete(StatusCodes.NoContent)
          }
        }
        }
      }
     }~
    // just forward the query parameters from google to googleAuthActor
    get {
      path("") {
        parameters("code", "state") {(code, state) => {
          log.debug(getClass.getName + " received from google response: " + "name: " + state + ", code: " + code)
          googleAuthActor ! GoogleResponse(state, code)
          complete("")
        } 
        }   
      }
    }


    
  })
}