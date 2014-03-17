package de.tkip.sbpm.polling

import akka.actor._

object PollingApplication extends App{
  val system = ActorSystem("PollingApp")
  val config = system.settings.config
  val receiveActor = "receive"
  val eventBusRemotePublishActor = system.actorOf(Props[ReceiveActor], receiveActor)
}