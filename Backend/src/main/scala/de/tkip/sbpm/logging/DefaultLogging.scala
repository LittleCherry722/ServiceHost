package de.tkip.sbpm.logging

import akka.actor.{Actor, ActorLogging}

trait DefaultLogging extends ActorLogging {
  this: Actor =>
  override def preStart() { log.debug(getClass.getName + " starting...") }
  override def postStop() { log.debug(getClass.getName + " stopped.") }
}