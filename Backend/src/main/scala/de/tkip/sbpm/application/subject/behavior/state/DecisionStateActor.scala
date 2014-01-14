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


protected case class DecisionStateActor(data: StateData) extends BehaviorStateActor(data) {
  private var trueTransition: Transition = null
  private var falseTransition: Transition = null
  private val travel_request_string = extractVariable(variables)

  prepareTransitions

  logger.debug("DecisionStateActor initialized: exitTransactions=" + exitTransitions.mkString(",") +
    ", variables="+variables.mkString(",")+",travel_request_string="+travel_request_string+", trueTransition="+trueTransition+
    ", falseTransition=" + falseTransition)

  try {
    val res: Boolean = evaluateDecision(travel_request_string)
    applyDecision(res)
  }
  catch {
    case ex : Throwable => {
      logger.error("DecisionStateActor exception during evaluation")
    }
  }

  
  
  protected def extractVariable(variables: scala.collection.mutable.Map[String,de.tkip.sbpm.application.subject.behavior.Variable]): String = {
    var ret = "Variable not found!!"
    for((key,variable) <- variables) {
      for(value <- variable.messages) {
        value match {
          case SubjectToSubjectMessage(_,_,_,_,_,"Travel Application",msg,_,_) => {ret = msg; logger.debug("DecisionStateActor extractVariable: found with key '"+key+"': " + value)}
          case x => {logger.debug("DecisionStateActor extractVariable: it is not '"+key+"' with value: " + x)}
        }
      }
    }
    ret
  }

  protected def prepareTransitions = {
    for(transition <- exitTransitions) {
      transition match {
        case Transition(ExitCond("true",_),_,_,_) => {trueTransition = transition}
        case Transition(ExitCond("false",_),_,_,_) => {falseTransition = transition}
        case _ => logger.error("DecisionStateActor unexpected transition: " + transition)
      }
    }
  }
  
  
  protected def evaluateDecision(input: String): Boolean = {
    // Only get one String and parse it as following: "x TAGE? y" with x count of days and y destination
    val in = input.toLowerCase()
    return ((in contains "1 tag") || (in contains "2 tage") || (in contains "3 tage")) && ((in contains "münchen") || (in contains "berlin"))
  }
  
  protected def applyDecision(res: Boolean) = {
    if(res) changeState(trueTransition.successorID, data, null)
    else changeState(falseTransition.successorID, data, null)
  }

  // needed as this extends BehaviorStateActor

  override protected def getAvailableAction: Array[ActionData] = Array()
  
  protected def stateReceive = {
    // execute an action
    case _ @ x=> logger.warning("DecisionStateActor -> unexpected stateReceive: " + x)
  }
}
