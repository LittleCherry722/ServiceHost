package de.tkip.sbpm.instrumentation

import akka.actor._
import de.tkip.sbpm.instrumentation.InstrumentationActor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

/**
 * Log every incoming message
 */
object TraceLogger {
  private implicit val timeout = Timeout(30 seconds)

  case class WrappedMessage(message: Any, sender: String)
  case class WrappedAsk(message: Any, sender: String)

  implicit class ActorRefClassWrapper(val a: ActorRef) {
    def !!(msg: Any) = a ! WrappedMessage(msg, this.getClass.getSimpleName)
    def ??(msg: Any) = a ? WrappedAsk(msg, this.getClass.getSimpleName)
  }
}
trait TraceLogger extends ActorStack with ActorLogging {
  import TraceLogger.{WrappedAsk, WrappedMessage}

  private implicit val timeout = Timeout(30 seconds)

  private val instrumentationActor = context.actorOf(Props[InstrumentationActor], "instrumentation")

  implicit class ActorRefWrapper(val a: ActorRef) {
    def !!(msg: Any) = {
      instrumentationActor ! AnswerMessage(msg, context.self.toString(), a)
      a ! msg
    }
    def ??(msg: Any) = a ? WrappedAsk(msg, context.self.toString())
  }

  override def receive: Receive = {
    case WrappedAsk(msg, sender) =>
      instrumentationActor ! AskMessage(msg, context.sender, sender, context.self.toString)
      super.receive(msg)
    case WrappedMessage(msg, sender) =>
      instrumentationActor ! LogMessage(msg, sender, context.self.toString)
      super.receive(msg)
    case msg =>
      instrumentationActor ! LogMessage(msg, sender.toString, context.self.toString)
      super.receive(msg)
  }
}
