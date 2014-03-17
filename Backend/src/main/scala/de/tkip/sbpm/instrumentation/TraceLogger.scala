package de.tkip.sbpm.instrumentation

import akka.event.Logging
import akka.actor.ActorLogging

/**
 * Log every incoming message
 */
trait TraceLogger extends ActorStack with ActorLogging {
  override def receive: Receive = {
    case msg =>
      log.debug("TRACE: from " + sender + " to " + this.self + " " + msg.toString)
      super.receive(msg)
  }
}
