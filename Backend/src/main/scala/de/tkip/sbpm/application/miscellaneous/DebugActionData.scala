/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.application.miscellaneous

import de.tkip.sbpm.application.subject.misc.AvailableAction
import scala.collection.mutable.ArrayBuffer
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.StateType._

object DebugActionData {
  def generateActions(userID: UserID, processInstanceID: ProcessInstanceID): Array[AvailableAction] = {
    val receive = ReceiveStateString
    val act = ActStateString
    val send = SendStateString

    val buffer = ArrayBuffer[AvailableAction]()
    //    buffer += AvailableAction(userID, processInstanceID, "Employee", 1, "Fill out Application", act, Array("Fill out application for vacations."))
    //    buffer += AvailableAction(userID, processInstanceID, "Superior", 2, "Wait for Application", receive, Array("I want to go to holidays."))
    //    buffer += AvailableAction(userID, processInstanceID, "The Manager", 1, "Decide", act, Array("Approval", "Denial"))
    //    buffer += AvailableAction(userID, processInstanceID, "The Person, who does nothing", 3, "Chill", waiting, Array())
    //    buffer += AvailableAction(userID, processInstanceID, "Manager", 7, "Receive a message.", receive, Array("This is a message."))
    //    buffer += AvailableAction(userID, processInstanceID, "Warehouse", 1, "Send a message", send, Array())
    //    buffer += AvailableAction(userID, processInstanceID, "Warehouse2", 1, "Wait", waiting, Array())
    //    buffer += AvailableAction(userID, processInstanceID, "Many Actions", 27, "Choose an action", act, Array("First Action", "2nd Action", "3", "4. Action", "five", "6", "seven", "last action"))

    buffer.toArray
  }
}
