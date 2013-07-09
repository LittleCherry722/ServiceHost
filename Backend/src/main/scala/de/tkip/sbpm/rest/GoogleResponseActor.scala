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
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.parsing.json.JSONObject
import scala.util.{Try, Success, Failure}

import akka.actor.{Actor, ActorLogging}
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import spray.routing.HttpService
import spray.json.JsonFormat
import spray.http.StatusCodes
import spray.http.MediaTypes._

import de.tkip.sbpm.rest.google.{DriveActor, DriveControl}
import DriveActor.{FindFiles, InitCredentials, RetrieveCredentials}
import DriveControl.{NoCredentialsException}

import de.tkip.sbpm
import de.tkip.sbpm.logging.DefaultLogging

class GoogleResponseActor extends Actor with HttpService with DefaultLogging {

  implicit val timeout = Timeout(15 seconds)
  private lazy val driveActor = sbpm.ActorLocator.googleDriveActor
  def actorRefFactory = context
  
  def receive = runRoute {
    post {
      // frontend request for authentication of SBPM app gainst Google account
      pathPrefix("init_auth") {
        formFields("id") { (userId) => ctx =>
          log.debug(s"${getClass.getName} received authentication init post from user: $userId")
          (driveActor ? RetrieveCredentials(userId))
            .onComplete {
              case Success(files) => ctx.complete(StatusCodes.NoContent)
              case Failure(NoCredentialsException(auth_url)) =>
                ctx.complete(StatusCodes.OK, auth_url)
              case Failure(e) => ctx.complete(e)
            }
        }
      }
    } ~
    get {
      // callback endpoint called by Google after an authentication request
      path("") {
        parameters("code", "state") { (code, userId) =>
          respondWithMediaType(`text/html`) { ctx =>
            log.debug(s"${getClass.getName} received from google response: name: $userId, code: $code")
            (driveActor ? InitCredentials(userId, code))
              .onComplete {
                case Success(_) =>
                  ctx.complete("<!DOCTYPE html>\n<html><head><script>window.close();</script></head><body>OK</body></html>")
                case Failure(e) => ctx.complete(e)
              }
          }
        }
      } ~
      path("get_files") {
        parameters("id") { id => ctx =>
          log.debug(s"${getClass.getName} received get request for google drive files from user: $id")
          (driveActor ? FindFiles(id, "sharedWithMe", "items(title)"))
            .mapTo[String]
            .onComplete {
              case Success(files) => ctx.complete(files)
              case Failure(NoCredentialsException(auth_url)) =>
                ctx.complete(StatusCodes.Forbidden, auth_url)
              case Failure(e) => ctx.complete(e)
            }
        }
      }
    }
  }
}
