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
import de.tkip.sbpm.external.api.ListGDriveFiles
import com.google.api.services.drive.model.File


class GoogleDriveInterfaceActor extends Actor with HttpService with ActorLogging {
  

  implicit val timeout = Timeout(15 seconds)

  private lazy val googleDriveActor = ActorLocator.googleDriveActor
    
  def actorRefFactory = context
  
  override def preStart() {
    log.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    log.debug(getClass.getName + " stopped.")
  }
      
  def receive = runRoute({
    // user can ask /googledrive/getFiles?id=... to get a list of his files stored in his google drive
    get {
      path("get_files") {
        parameters("id") {(id) => {
          log.debug(getClass.getName + " received get request for google drive files from user: " + id)
          
          val future = googleDriveActor ? ListGDriveFiles(id)
          val result = Await.result(future.mapTo[String], timeout.duration)

          complete(result)
        } 
        }   
      }
    }
  
  })
}