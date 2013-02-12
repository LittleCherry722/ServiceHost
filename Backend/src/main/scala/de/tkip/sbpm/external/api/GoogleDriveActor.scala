package de.tkip.sbpm.external.api

import akka.actor.Actor

class GoogleDriveActor extends Actor {
  
  def receive = {
    case _ => sender ! "not implemented yet"
  }

}