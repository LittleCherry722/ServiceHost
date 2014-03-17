package de.tkip.sbpm.eventbus

case class SbpmEventBusTextMessage(text: String)
case class SbpmEventBusTrafficFlowMessage(sensorId: Int, count: Int)