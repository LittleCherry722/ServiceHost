package de.tkip.serviceupdate

import akka.actor.Actor
import akka.actor.ActorRef
import de.tkip.servicehost.Messages.UpdateRepository

class ServiceUpdateActor extends Actor{
  
//  val serviceHost = context.actorSelection("akka.tcp://sbpm@127.0.0.1:2553/user/service-actor-manager")
   
  def receive:Actor.Receive = {
//    case msg: UpdateRepository => 
//      println(self + "Got Message: " + msg + "from sender: " + sender)
    case msg: UpdateRepository => {
      println(self + " Got UpdateRepository " + sender)
      println("Sending to ServiceHost")
      val serviceHost = context.actorFor("akka.tcp://sbpm@"+ msg.host + ":" + msg.port + "/user/subject-provider-manager")
  
      serviceHost ! UpdateRepository
    }
    case anything => println("sth else: " + anything)
  }

  def sendUpdateTo(ref: ActorRef) {
    ref ! UpdateRepository
  }
  
}