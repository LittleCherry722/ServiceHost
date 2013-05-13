package de.tkip.sbpm.application

import akka.actor.Actor

class SubjectActor extends Actor {
  def receive = {
    case _ => println("unsupported operation")
  }
}
