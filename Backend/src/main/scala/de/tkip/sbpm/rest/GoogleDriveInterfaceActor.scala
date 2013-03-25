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
import spray.http.StatusCodes


class GoogleDriveInterfaceActor extends Actor with HttpService with ActorLogging {
  

  implicit val timeout = Timeout(15 seconds)

  private lazy val googleDriveActor = ActorLocator.googleDriveActor
  private lazy val googleAuthActor = ActorLocator.googleAuthActor
    
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
          
          // ask authentication manager if user has a valid google credential
          val auth_future = googleAuthActor ? InitUser(id)
          val auth_result = Await.result(auth_future.mapTo[String], timeout.duration)

          // if auth_result is positive ask google drive aktor for files in user google drive, if not send him back the
          // authentication url
          if (auth_result == "AUTHENTICATED") {
            val drive_future = googleDriveActor ? ListGDriveFiles(id)
            val drive_result = Await.result(drive_future.mapTo[String], timeout.duration)
            complete(drive_result)
          } else {
            complete(StatusCodes.Forbidden, auth_result)
          }
        }
      }
    }
    }
  })
}