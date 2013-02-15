package de.tkip.sbpm.external.api

import akka.actor.Actor
import akka.actor.ActorLogging

class GoogleDriveActor extends Actor with ActorLogging {
  
  def receive = {
    case _ => sender ! "not implemented yet"
  }

}