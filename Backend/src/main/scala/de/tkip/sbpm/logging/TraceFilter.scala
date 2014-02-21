package de.tkip.sbpm.logging

import ch.qos.logback.core.filter.Filter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.spi.FilterReply
import ch.qos.logback.classic.spi.ILoggingEvent

class TraceFilter extends Filter[ILoggingEvent] {

  override def decide(event: ILoggingEvent) = {
    if (event.getFormattedMessage().startsWith("TRACE:")) {
      FilterReply.ACCEPT
    } else {
      FilterReply.DENY
    }
  }

}
class NoTraceFilter extends Filter[ILoggingEvent] {

  override def decide(event: ILoggingEvent) = {
    if (event.getFormattedMessage().startsWith("TRACE:")) {
      FilterReply.DENY
    } else {
      FilterReply.ACCEPT
    }
  }
}