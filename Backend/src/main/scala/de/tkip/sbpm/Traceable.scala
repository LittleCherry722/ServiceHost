package de.tkip.sbpm

import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.InternalActorRef
import akka.actor.Props
import akka.actor.ActorPath
import akka.event.Logging

abstract class TraceableActor extends Actor {
  def !(message: Any)(implicit sender: ActorRef = Actor.noSender):Unit = {
    val traceLogger = Logging(context.system, this)
    traceLogger.debug("TRACE: from "+ this+" to "+ sender+" "+message.toString)
    sender.!(message)(this.self)
  }
}