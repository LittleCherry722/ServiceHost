package de.tkip.sbpm.polling

import akka.actor._

object PollingApplication extends App{
  val system = ActorSystem("PollingApp")
  val config = system.settings.config
  val receiveActor = "receive"
  val eventBusRemotePublishActor = system.actorOf(Props[ReceiveActor], receiveActor)
  
//  val tmpSubscriber = system.actorOf(Props(new Actor {
//    def receive = {
//      case PollingEventBusTrafficFlowMessage(sensorId, count) => println("SUBSCRIBER GOT message, id: " + sensorId + " count: " + count)
//    }
//  }))
//  SbpmEventBus.subscribe(tmpSubscriber, "/traffic/darmstadt/flow")
}