package de.tkip.sbpm.instrumentation

import akka.actor.ActorRef
import spray.routing.RequestContext
import scala.collection.mutable
import org.slf4j.LoggerFactory

/**
 * Created by arne on 18.03.14.
 */
object InstrumentationLogger {
  case class LogMessage(message: Any, from: String, to: String)
  case class AskMessage(message: Any, fromTemp: ActorRef, from: String, to: String)
  case class AnswerMessage(message: Any, from: String, to: ActorRef)

  private val log = LoggerFactory.getLogger("InstrumentationLogger")

  private val askMessages = mutable.Map[String,String]()

  def logMessage(m: LogMessage) = {
    this.synchronized {
      val from = getName(m.from)
      val to = getName(m.to.toString())
      log.info("TRACE: from " + from + " to " + to + " " + messageString(m.message))
    }
  }

  def logAsk(m: AskMessage) = {
    this.synchronized {
      askMessages(m.fromTemp.toString()) = m.from
      log.info("TRACE: from " + m.from + " to " + m.to + " " + messageString(m.message))
    }
  }

  def logAnswer(m: AnswerMessage) = {
    this.synchronized {
      val from = getName(m.from)
      val to = getName(m.to.toString())
      log.info("TRACE: from " + from + " to " + to + " " + messageString(m.message))
    }
  }

  private def getName(actor: String) = {
    if (actor.contains("temp")) {
      askMessages.getOrElse(actor, actor)
    } else {
      actor
    }
  }

  private def messageString(msg: Any) = {
    if (msg.isInstanceOf[RequestContext]) {
      "RequestContext"
    } else {
      msg.toString
    }
  }
}