package de.tkip.sbpm.logging

import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingAdapter

trait DefaultLogging extends ActorLogging {
  this: Actor =>
  implicit val implicitLogger: LoggingAdapter = log
  override def preStart() { log.debug("actor starting...") }
  override def postStop() { log.debug("actor stopped.") }
}
