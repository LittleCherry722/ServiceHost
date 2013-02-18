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

class GoogleResponseActor extends Actor with HttpService with ActorLogging {
  
    private lazy val googleAuthActor = ActorLocator.googleAuthActor
    
  def actorRefFactory = context
  
  override def preStart() {
    log.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    log.debug(getClass.getName + " stopped.")
  }
  
  
  // just forward the post from google to googleAuthActor
  def receive = runRoute({
    get {
      path("") {
        parameters("code", "state") {(code, state) => {
          log.debug(getClass.getName + " received: " + "name: " + state + ", code: " + code)
          googleAuthActor ! GoogleResponse(state, code)
          complete("")
        } 
        }   
      }
    } 
    /**
    post {
      path("") {
    	  entity(as[String]) { json =>
    	    log.debug(getClass.getName + " received: " + json)
            googleAuthActor ! new GoogleResponse(json)
            complete("")
    	  }
      }
    }
    */
  })
}