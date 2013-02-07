package de.tkip.sbpm.application.miscellaneous

import de.tkip.sbpm.application.subject.AvailableAction
import scala.collection.mutable.ArrayBuffer
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.StateType._

object DebugActionData {
  def generateActions(userID: UserID, processInstanceID: ProcessInstanceID): Array[AvailableAction] = {
    val receive = ReceiveStateType.toString()
    val act = ActStateType.toString()
    val send = SendStateType.toString()

    val buffer = ArrayBuffer[AvailableAction]()
    buffer += AvailableAction(userID, processInstanceID, "Employee", 1, act, Array("Fill out application for leave"))
    buffer += AvailableAction(userID, processInstanceID, "Superior", 2, receive, Array("I want to go to holidays."))
    buffer += AvailableAction(userID, processInstanceID, "Another Employee", 3, send, Array())

    buffer.toArray
  }
}
