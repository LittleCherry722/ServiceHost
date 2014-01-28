package de.tkip.sbpm.eventbus

import akka.actor.Actor

class RemotePublishActor extends Actor {
  def receive: Actor.Receive = {
    case e: SbpmEventBusEvent => {
      println("got event: " + e)
      SbpmEventBus.publish(e)
    }
    case tfm: SbpmEventBusTrafficFlowMessage => {
      println("got EventBusTrafficFlowMessage, sensor id: " + tfm.sensorId + " count: " + tfm.count)
    }
    case somethingElse => println("got something else: " + somethingElse)
  }
}