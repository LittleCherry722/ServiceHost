package de.tkip.servicehost.serviceactor

import akka.actor._
import de.tkip.servicehost.Messages._

class AddServiceActor extends ServiceActor {
  
  def receive: Actor.Receive = {
    case message: AddService =>
      val res = message.n * 2;
    
      println(res)
      message.sender ! new ServiceResultMessage(res)
  }
}