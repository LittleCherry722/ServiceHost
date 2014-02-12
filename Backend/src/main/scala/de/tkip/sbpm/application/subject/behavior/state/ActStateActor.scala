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

package de.tkip.sbpm.application.subject.behavior.state

import scala.Array.canBuildFrom
import akka.actor.Status.Failure
import akka.actor.actorRef2Scala
import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes.exitCondLabel
import de.tkip.sbpm.application.subject.behavior.Transition
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.misc.ActionExecuted
import de.tkip.sbpm.application.subject.misc.ExecuteAction
import akka.event.Logging

protected case class ActStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  protected def stateReceive = {

    case action: ExecuteAction => {
      val input = action.actionData
      val index = indexOfInput(input.text)
      val traceLogger = Logging(context.system, this)
      if (index != -1) {
        changeState(exitTransitions(index).successorID, data, null)
        traceLogger.debug("TRACE: from " + this.self + " to " + blockingHandlerActor + " " + ActionExecuted(action).toString)
        blockingHandlerActor ! ActionExecuted(action)
      } else {
        val receiver = action.asInstanceOf[AnswerAbleMessage].sender
        val message = Failure(new IllegalArgumentException(
            "Invalid Argument: " + input.text + " is not a valid action."))
        traceLogger.debug("TRACE: from " + this.self + " to " + receiver + " " + message.toString)
        receiver ! message
          
      }
    }
  }

  override protected def getAvailableAction: Array[ActionData] =
    exitTransitions.map((t: Transition) => ActionData(t.messageType, true, exitCondLabel))

  private def indexOfInput(input: String): Int = {
    var i = 0
    for (t <- exitTransitions) {
      if (t.messageType.equals(input)) {
        return i
      }
      i += 1
    }
    -1
  }
}