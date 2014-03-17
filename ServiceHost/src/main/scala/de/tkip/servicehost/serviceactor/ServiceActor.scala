package de.tkip.servicehost.serviceactor

import akka.actor._

abstract class ServiceActor extends Actor {
  
//  protected val inputPoolActor: ActorRef =
//    context.actorOf(Props(new InputPoolActor(data)),"InputPoolActor____"+UUID.randomUUID().toString())
  
}

object ServiceAttributes {
  
  type ServiceID = String; val AllServices = ""
  type ServiceActorRef = ActorRef
}