package de.tkip.sbpm.eventbus

import akka.actor.Actor

class RemotePublishActor extends Actor {
  override def preStart() {
//      println("RemotePublishActor started!")
//      val selection = context.actorSelection("akka.tcp://eg@127.0.0.1:6666/user/test")
//      selection ! "Pretty awesome feature"
  }
  def receive: Actor.Receive = {
    case e: SbpmEventBusEvent => {
      println("got event: " + e)
      SbpmEventBus.publish(e)
    }
    case somethingElse => println("got something else: " + somethingElse)
  }
}