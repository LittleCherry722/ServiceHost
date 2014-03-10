package de.tkip.sbpm.polling

import akka.actor._
import de.tkip.sbpm.eventbus._

class ReceiveActor extends Actor{
  
  override def preStart() {
    println("test actor started!")
  }
  
  def receive = {
    case traffic: AskForTrafficJam => {
      sender ! ReplyForTrafficJam("no traffic jam.")
    }
    case s: Any => {
      println("receive: " + s.toString)
      sender ! "Message Type Error."
    }
  }
}