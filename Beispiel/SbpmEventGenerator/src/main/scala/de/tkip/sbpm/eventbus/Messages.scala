package de.tkip.sbpm.eventbus

case class SbpmEventBusTextMessage(text: String)
case class SbpmEventBusTrafficFlowMessage(sensorId: Int, count: Int)
case class AskForTrafficJam()
case class ReplyForTrafficJam(result: String)