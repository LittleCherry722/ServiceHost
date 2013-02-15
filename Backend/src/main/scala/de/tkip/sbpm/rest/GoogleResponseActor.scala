package de.tkip.sbpm.rest

import akka.actor.Actor
import spray.routing.HttpService
import akka.actor.ActorLogging
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.external.auth.googleResponse
import scala.util.parsing.json.JSONObject
import scala.util.parsing.json.JSONObject
import spray.json.JsonFormat

class GoogleResponseActor extends Actor with HttpService with ActorLogging {
  
    private lazy val googleAuthActor = ActorLocator.googlAuthActor
    
  def actorRefFactory = context
  
  override def preStart() {
    log.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    log.debug(getClass.getName + " stopped.")
  }
  
  def receive = runRoute({
    post {
      path("") {
    	  entity(as[String]) { json =>
    	    log.debug(getClass.getName + " received: " + json)
            googleAuthActor ! new googleResponse(json)
            complete("")
    	  }
      }
    }
  })
}