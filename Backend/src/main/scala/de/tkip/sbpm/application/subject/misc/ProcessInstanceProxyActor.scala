package de.tkip.sbpm.application.subject.misc

import akka.actor.Actor

class ProcessInstanceProxyActor extends Actor {

  def receive = {

    // TODO
    case m => context.parent forward m
  }
}