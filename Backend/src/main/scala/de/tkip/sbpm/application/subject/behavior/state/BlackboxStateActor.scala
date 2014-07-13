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
import akka.actor.Actor
import akka.actor.Status.Failure
import akka.actor.actorRef2Scala
import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes.exitCondLabel
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes.timeoutLabel
import de.tkip.sbpm.application.subject.behavior.Transition
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.misc.ActionExecuted
import de.tkip.sbpm.application.subject.misc.AvailableAction
import de.tkip.sbpm.application.subject.misc.ExecuteAction
import de.tkip.sbpm.application.subject.misc.ActionIDProvider
import akka.event.Logging

case class BlackboxStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  // TODO: mehrere exits?
  var internalState = "a"
  val internalStates = Array("exit", "a", "b")

  protected def stateReceive = Actor.emptyBehavior

  override protected def interceptReceive: Receive = {
    case action: ExecuteAction if (
      action.stateType == "action"
    )=> {
      
      val nextInternalState = action.actionData.text

      if (nextInternalState.equals("exit")) {
        changeState(exitTransitions(0).successorID, data, null)
        blockingHandlerActor ! ActionExecuted(action)
      }
      else if (internalStates.contains(nextInternalState)) {
        internalState = nextInternalState
        blockingHandlerActor ! ActionExecuted(action)
      } else {
        val receiver = action.asInstanceOf[AnswerAbleMessage].sender
        val message = Failure(new IllegalArgumentException(
            "Invalid Argument: " + nextInternalState + " is not a valid action."))
        receiver ! message
      }
    }
  }

  override protected def getAvailableAction: Array[ActionData] = {
    internalStates.filter((s: String) => !s.equals(internalState)).map((s: String) => ActionData(s, true, exitCondLabel))
  }


  override protected def createAvailableAction: AvailableAction = {
    var actionData = getAvailableAction
    log.info("_createAvailableAction.actionData#1: " + actionData.mkString(","))
    if (timeoutTransition.isDefined) {
      actionData ++= Array(ActionData("timeout", true, timeoutLabel))
    }
    log.info("_createAvailableAction.actionData#2: " + actionData.mkString(","))
    //if (disabled) {
      // if disabled, disable all action
      //actionData.foreach(_.executeAble = false)
    //}
    log.info("_createAvailableAction.actionData#3: " + actionData.mkString(","))
    val aa = AvailableAction(
      actionIDs(internalState),
      userID,
      processInstanceID,
      subjectID,
      macroID,
      id,
      stateText,
      "action", //stateType.toString(),
      actionData)
    log.info("_createAvailableAction.aa: " + aa)
    aa
  }

}
