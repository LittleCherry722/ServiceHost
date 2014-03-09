package de.tkip.sbpm.eventbus

case class SbpmEventBusTextMessage(text: String)
case class SbpmEventBusTrafficFlowMessage(sensorId: Int, count: Int)
<<<<<<< HEAD
case class AskForTrafficJam()
case class ReplyForTrafficJam(result: String)
=======
case class AskForTrafficJam()
>>>>>>> a99a2a4113d06175036c66f7f498139299dc0c85
