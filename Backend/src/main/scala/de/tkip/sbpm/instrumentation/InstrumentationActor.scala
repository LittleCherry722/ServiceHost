package de.tkip.sbpm.instrumentation

import akka.actor.{ActorRef, ActorLogging, Actor}
import spray.routing.RequestContext
import scala.collection.mutable.Map

/**
 * Created by arne on 18.03.14.
 */
object InstrumentationActor {
  case class LogMessage(message: Any, from: String, to: String)
  case class AskMessage(message: Any, fromTemp: ActorRef, from: String, to: String)
  case class AnswerMessage(message: Any, from: String, to: ActorRef)
}

class InstrumentationActor extends Actor with ActorLogging {
  import InstrumentationActor._

  private var askMessages: Map[String,String] = Map()

  def receive = {
    case m @ LogMessage(msg, from, toTemp) => {
      log.debug("INSTRUMENTATION: " + m)
      val to = if (toTemp.contains("temp")) {
        askMessages.getOrElse(toTemp, toTemp)
      } else {
        toTemp
      }
      log.debug("TRACE: from " + from + " to " + to + " " + messageString(msg))
    }
    case m @ AskMessage(msg, fromTemp, from, to) => {
      log.debug("INSTRUMENTATION: " + m)
      askMessages += (fromTemp.toString() -> from)
      log.debug("TRACE: from " + from + " to " + to + " " + messageString(msg))
    }
    case m @ AnswerMessage(msg, from, toTemp) => {
      log.debug("INSTRUMENTATION: " + m)
      val to = if (toTemp.toString().contains("temp")) {
         askMessages.getOrElse(toTemp.toString(), toTemp.toString())
      } else {
        toTemp.toString()
      }
      log.debug("TRACE: from " + from + " to " + to + " " + messageString(msg))
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
