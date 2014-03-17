package de.tkip.sbpm

import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.InternalActorRef
import akka.actor.Props
import akka.actor.ActorPath
import akka.event.Logging
import de.tkip.sbpm.instrumentation.InstrumentedActor

abstract class TraceableActor extends InstrumentedActor {
  def !(message: Any)(implicit sender: ActorRef = Actor.noSender):Unit = {
    val traceLogger = Logging(context.system, this)
    traceLogger.debug("TRACE: from "+ this+" to "+ sender+" "+message.toString)
    sender.!(message)(this.self)
  }
}
