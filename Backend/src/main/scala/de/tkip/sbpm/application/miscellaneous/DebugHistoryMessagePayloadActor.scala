package de.tkip.sbpm.application.miscellaneous

import de.tkip.sbpm.application.history._
import akka.actor.Actor

class DebugHistoryMessagePayloadActor extends Actor {

  def receive = { 
    // example message payloads for an order process
    case GetMessagePayload(_, "req") => sender ! "152876(2),4547984(3),546847(1),541754(1)"
    case GetMessagePayload(_, "avail") => sender ! "152876(1),4547984(3),546847(0),541754(1)"
    case GetMessagePayload(_, "order") => sender ! "152876(1),4547984(3),541754(1)"
    case GetMessagePayload(_, "invoice") => sender ! "Content of invoice file."
  }
}