package de.tkip.sbpm.eventbus

abstract class SbpmEventBusMessage
case class SbpmEventBusTextMessage(text: String) extends SbpmEventBusMessage
case class SbpmEventBusTrafficFlowMessage(sensorId: Int, count: Int) extends SbpmEventBusMessage
case class AskForTrafficJam()
