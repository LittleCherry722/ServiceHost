/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2014 Telecooperation Group @ TU Darmstadt
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

case class BlackboxStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  // TODO: mehrere exits?
  var internalState = "a"
  val internalStates = Array("exit", "a", "b")

  protected def stateReceive = {

    case action: ExecuteAction => {
      
      val nextInternalState = action.actionData.text

      if (nextInternalState.equals("exit")) {
        changeState(exitTransitions(0).successorID, data, null)
        blockingHandlerActor ! ActionExecuted(action)
      }
      else if (internalStates.contains(nextInternalState)) {
        internalState = nextInternalState
      } else {
        val receiver = action.asInstanceOf[AnswerAbleMessage].sender
        val message = Failure(new IllegalArgumentException(
            "Invalid Argument: " + nextInternalState + " is not a valid action."))
        receiver ! message
      }
    }
  }

  override protected def getAvailableAction: Array[ActionData] = {
    internalStates.map((s: String) => ActionData(s, true, exitCondLabel))
  }

}
