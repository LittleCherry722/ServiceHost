package de.tkip.sbpm.rest

import akka.actor.Actor
import spray.routing.HttpService
import akka.actor.ActorLogging

class GoogleResponseActor extends Actor with HttpService with ActorLogging {
  
  def actorRefFactory = context
  
  def receive = runRoute({
    post {
      path("") {
        // send json response to GoogleAuthActor
        complete("")
      }
    }
    
  })
}