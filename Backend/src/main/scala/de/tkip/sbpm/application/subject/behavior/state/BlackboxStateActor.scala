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
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application.subject.behavior._
import de.tkip.sbpm.application.subject.CallMacroStates
import de.tkip.sbpm.application.subject.misc.MacroTerminated
import de.tkip.sbpm.application.miscellaneous.UnBlockUser
import akka.event.Logging

case class BlackboxStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  // TODO: mehrere exits?
  var internalState = "a"
  val internalStates = Array("exit", "callmacro", "a", "b")

  val actionIDs = Map("a" -> ActionIDProvider.nextActionID, "b" -> ActionIDProvider.nextActionID, "callmacro" -> ActionIDProvider.nextActionID)

  protected def stateReceive = {
    case mt: MacroTerminated => {
      log.info("Macro terminated: " + mt.macroID)

      internalState = "a"

      blockingHandlerActor ! UnBlockUser(userID)

      actionChanged()
    }
  }

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

        if (internalState == "callmacro") {
          //                      TransType(messagetype,    target), SuccessorID, priority
          val t_s1_1 = Transition(ExitCond("Gehe zum Ende", None),   42,          1)
          val t_s1_2 = Transition(ExitCond("1338",          None),   1338,        1)
          val t_s1 = Array(t_s1_1, t_s1_2)

          val t_s2: Array[Transition] = Array()

          val t_s3_1 = Transition(ExitCond("Nochmal",       None),   1337,        1)
          val t_s3 = Array(t_s3_1)

          //             id    text      type          autoexe start  observ callmacro options     (msgtype, subjid, corrid, conversat, StateID
          val s1 = State(1337, "Anfang", ActStateType, false,  true,  false, None,     StateOptions(None,    None,   None,   None,      Some(1337)), t_s1)
          val s2 = State(42,   "Ende",   EndStateType, false,  false, false, None,     StateOptions(None,    None,   None,   None,      Some(42)),   t_s2)
          val s3 = State(1338, "Mitte",  ActStateType, false,  false, false, None,     StateOptions(None,    None,   None,   None,      Some(1338)), t_s3)
          val states = Array(s1, s2, s3)

          // TODO: BlockUser ?
          context.parent ! CallMacroStates(this.self, "test", states)
          // TODO: UnBlockUser ?
        }

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
    if (internalState == "callmacro") {
      Array()
    }
    else {
      internalStates.filter((s: String) => !s.equals(internalState)).map((s: String) => ActionData(s, true, exitCondLabel))
    }
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
