package de.tkip.sbpm.instrumentation

import akka.actor.ActorRef
import akka.pattern.ask
import de.tkip.sbpm.instrumentation.InstrumentationLogger.LogMessage
import akka.util.Timeout
import scala.concurrent.duration._
import de.tkip.sbpm.instrumentation.TraceLogger.WrappedAsk

/**
 * Created by arne on 21.03.14.
 */
trait ClassTraceLogger {
  private implicit val timeout = Timeout(30 seconds)

  private def klass = this.getClass

  implicit class ActorRefWrapper(val a: ActorRef) {
    def !!(msg: Any) = {
      InstrumentationLogger.logClassMessage(LogMessage(msg, klass.getSimpleName, a.toString()))
      a ! msg
    }
    def ??(msg: Any) = a ? {
      WrappedAsk(msg, klass.getSimpleName)
    }
  }
}
