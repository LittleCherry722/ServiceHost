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

import de.tkip.sbpm.application.subject.behavior.{Transition, ExitCond, ErrorCond, Variable}
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.model.GraphVarMan

class VariableManipulationStateActor(data: StateData) extends BehaviorStateActor(data) {

  data.stateModel.varMan match {
    case None => ()
    case Some(GraphVarMan(var1Id, var2Id, varOp, storeVarId)) =>
      varOp match {
        case "extract" =>
          variables.get(var1Id) match {
            case Some(var1) =>
              var1.messages.headOption match {
                case Some(outerMsg) =>
                  lazy val storeVar = variables.getOrElseUpdate(storeVarId, Variable(storeVarId))
                  outerMsg.messageContent match {
                    case SingleMessage(innerMsg) => storeVar.addMessage(innerMsg)
                    case MessageSet(messages) => messages.headOption match {
                      case Some(innerMsg) =>storeVar.addMessage(innerMsg)
                      case None => throw new Exception(s"[VARIABLE MANIPULATION] Variable $var1Id contains empty message Set.")
                    }
                    case _ => throw new Exception(s"[VARIABLE MANIPULATION] Variable $var1Id message content is not a message.")
                  }
                case None => throw new Exception(s"[VARIABLE MANIPULATION] Variable $var1Id contains no stored messages.")
              }
            case None => throw new Exception(s"[VARIABLE MANIPULATION] Variable $var1Id does not exist.")
          }
          varManDone()
        case _ =>
          log.error("[VARIABLE MANIPULATION] Unsupported variable manipulation")
          varManDone()
      }
  }

  private def varManDone() = {
    changeState(exitTransitions(0).successorID, data, null)
  }


  override protected def getAvailableAction: Array[ActionData] = Array()
  protected def stateReceive = {
    // execute an action
    case _@x => log.warning("DecisionStateActor -> unexpected stateReceive: " + x)
  }
}
