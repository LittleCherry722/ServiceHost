package de.tkip.sbpm.instrumentation

import akka.actor._
import de.tkip.sbpm.instrumentation.InstrumentationLogger._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

/**
 * Log every incoming message
 */
object TraceLogger {
  case class WrappedAsk(message: Any, sender: String)
}
trait TraceLogger extends ActorStack with ActorLogging {
  import TraceLogger.WrappedAsk

  private implicit val timeout = Timeout(30 seconds)

  implicit class ActorRefWrapper(val a: ActorRef) {
    def !!(msg: Any) = {
      InstrumentationLogger.logAnswer(AnswerMessage(msg, context.self.toString(), a))
      a ! msg
    }
    def ??(msg: Any) = a ? WrappedAsk(msg, context.self.toString())
  }

  implicit class ActorSelectionRefWrapper(val a: ActorSelection) {
    def !!(msg: Any) = {
      InstrumentationLogger.logMessage(LogMessage(msg, context.self.toString(), a.toString))
      a ! msg
    }
    def ??(msg: Any) = a ? WrappedAsk(msg, context.self.toString())
  }

  override def receive: Receive = {
    case WrappedAsk(msg, sender) =>
      InstrumentationLogger.logAsk(AskMessage(msg, context.sender, sender, context.self.toString))
      super.receive(msg)
    case msg =>
      InstrumentationLogger.logMessage(LogMessage(msg, sender.toString, context.self.toString))
      super.receive(msg)
  }
}
