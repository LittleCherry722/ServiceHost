package de.tkip.serviceupdate

import akka.actor.Actor
import akka.actor.ActorRef
import de.tkip.servicehost.Messages._

class ServiceUpdateActor extends Actor{
  
  val serviceHost = context.actorSelection("akka.tcp://sbpm@localhost:2553/user/service-actor-manager")
  
  
  def receive:Actor.Receive = {
    case msg: UpdateRepository => 
      println(self + "Got Message: " + msg + "from sender: " + sender)
    case UpdateRepository => {
      println(self + " Got UpdateRepository " + sender)
      println("Sending to ServiceHost")
      serviceHost ! UpdateRepository
    }
    case anything => println("sth else: " + anything)
  }

  def sendUpdateTo(ref: ActorRef) {
    ref ! UpdateRepository
  }
  
}