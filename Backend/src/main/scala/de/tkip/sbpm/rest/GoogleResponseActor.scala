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

class GoogleResponseActor extends Actor with HttpService with ActorLogging {
  

  implicit val timeout = Timeout(5 seconds)

  private lazy val googleAuthActor = ActorLocator.googleAuthActor
    
  def actorRefFactory = context
  
  override def preStart() {
    log.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    log.debug(getClass.getName + " stopped.")
  }
  
  
  // just forward the query parameters from google to googleAuthActor
  def receive = runRoute({
    get {
      path("") {
        parameters("code", "state") {(code, state) => {
          log.debug(getClass.getName + " received from google response: " + "name: " + state + ", code: " + code)
          googleAuthActor ! GoogleResponse(state, code)
          complete("")
        } 
        }   
      }
    }~
    post {
      path("initAuth") {
        parameters("id") {(id) => {
          log.debug(getClass.getName + " received authentication init post from user: " + id)
          googleAuthActor ! InitUser(id)
          // TODO add http response -> authentication url or in case the user is already authenticated send back a error code
          val future = googleAuthActor ? InitUser(id)
          val result = Await.result(future.mapTo[String], timeout.duration)

          // HTTP 204 wenn verbunden 
          
          // HTTP 200 mit url als body für weiterleitung  
          complete("")
        }
        }
      }
     }
  })
}