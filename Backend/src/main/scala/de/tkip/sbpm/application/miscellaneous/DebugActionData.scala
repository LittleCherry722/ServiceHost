package de.tkip.sbpm.application.miscellaneous

import de.tkip.sbpm.application.subject.AvailableAction
import scala.collection.mutable.ArrayBuffer
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.StateType._

object DebugActionData {
  def generateActions(userID: UserID, processInstanceID: ProcessInstanceID): Array[AvailableAction] = {
    val receive = ReceiveStateString
    val act = ActStateString
    val send = SendStateString
    val waiting = WaitingStateString

    val buffer = ArrayBuffer[AvailableAction]()
    buffer += AvailableAction(userID, processInstanceID, "Employee", 1, act, Array("Fill out application for vacations."))
    buffer += AvailableAction(userID, processInstanceID, "Superior", 2, receive, Array("I want to go to holidays."))
    buffer += AvailableAction(userID, processInstanceID, "The Manager", 1, act, Array("Approval", "Denial"))
    buffer += AvailableAction(userID, processInstanceID, "The Person, who does nothing", 3, waiting, Array())
    buffer += AvailableAction(userID, processInstanceID, "Manager", 7, receive, Array("This is a message."))
    buffer += AvailableAction(userID, processInstanceID, "Warehouse", 1, send, Array())
    buffer += AvailableAction(userID, processInstanceID, "Warehouse2", 1, waiting, Array())
    buffer += AvailableAction(userID, processInstanceID, "Many Actions", 27, act, Array("First Action", "2nd Action", "3", "4. Action", "five", "6", "seven", "last action"))

    buffer.toArray
  }
}
