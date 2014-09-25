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

import akka.actor.actorRef2Scala
import de.tkip.sbpm.application.subject.behavior.{Transition, ExitCond, ErrorCond, Variable}
import de.tkip.sbpm.application.subject.misc.{ActionData, SubjectToSubjectMessage}


case class DecisionStateActor(data: StateData) extends BehaviorStateActor(data) {
  private var trueTransition: Transition = null
  private var falseTransition: Transition = null

  val args: Array[String] = data.stateModel.text.split(":")

  private val condition = args(0)
  private val variable = getVariable(args(1))

  prepareTransitions

  log.debug("DecisionStateActor initialized: exitTransactions=" + exitTransitions.mkString(",") +
    ", condition="+condition+", variable="+variable+", trueTransition="+trueTransition+
    ", falseTransition=" + falseTransition)

  try {
    val res: Boolean = evaluateDecision(condition, variable, args)
    applyDecision(res)
  }
  catch {
    case ex : Throwable => {
      log.error("DecisionStateActor exception during evaluation")
    }
  }


  protected def prepareTransitions = {
    for(transition <- exitTransitions) {
      transition match {
        case Transition(ExitCond("true",_),_,_,_) => {trueTransition = transition}
        case Transition(ExitCond("false",_),_,_,_) => {falseTransition = transition}
        case _ => log.error("DecisionStateActor unexpected transition: " + transition)
      }
    }
  }


  protected def evaluateDecision(condition: String, variable: Option[Variable], args: Array[String]): Boolean = {
    val value: String = variable.get.messages(variable.get.messages.length-1).messageContent

    if (condition == "EMPTY") {
      (value == "")
    }
    else if (condition == "EQUALS") {
      (value == args(2))
    }
    else {
      false
    }
  }

  protected def applyDecision(res: Boolean) = {
    if(res) changeState(trueTransition.successorID, data, null)
    else changeState(falseTransition.successorID, data, null)
  }

  // needed as this extends BehaviorStateActor

  override protected def getAvailableAction: Array[ActionData] = Array()

  protected def stateReceive = {
    // execute an action
    case _ @ x=> log.warning("DecisionStateActor -> unexpected stateReceive: " + x)
  }
}
