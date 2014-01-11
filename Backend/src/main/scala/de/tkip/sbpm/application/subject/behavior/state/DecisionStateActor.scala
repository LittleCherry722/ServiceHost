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
import de.tkip.sbpm.application.subject.behavior.Transition
import de.tkip.sbpm.application.subject.behavior.ExitCond
import de.tkip.sbpm.application.subject.behavior.ErrorCond
//import de.tkip.sbpm.application.subject.behavior.Transition._
//import de.tkip.sbpm.application.subject.behavior.OpenInputPool
import de.tkip.sbpm.application.subject.misc.ActionData


protected case class DecisionStateActor(data: StateData) extends BehaviorStateActor(data) {
  private var trueTransition: Transition = null
  private var falseTransition: Transition = null
    
  println("####################################################")
  println("####################################################")
  
  println("DecisionStateActor -> Init")
  
  println("DecisionStateActor -> exitTransitions: " + exitTransitions.mkString(","))

  prepareTransitions

  try {
    val res: Boolean = evaluateTransition
    applyTransition(res)
  }
  catch {
    case ex : Throwable => {
      println("DecisionStateActor EXEPTION!!! " + ex);
    }
  }

  
  
  println("####################################################")
  println("####################################################")
 
  protected def prepareTransitions = {
    for(transition <- exitTransitions) {
      transition match {
        case Transition(ExitCond("true",_),_,_,_) => {trueTransition = transition}
        case Transition(ExitCond("false",_),_,_,_) => {falseTransition = transition}
        case _ => println("DecisionStateActor -> unexpected Transition: " + transition)
      }
    }

    println("DecisionStateActor -> trueTransition:" + trueTransition)
    println("DecisionStateActor -> falseTransition:" + falseTransition)
  }
  
  
  protected def evaluateTransition: Boolean = {
    return true; // TODO: evaluate
  }
  
  protected def applyTransition(res: Boolean) = {
    if(res) changeState(trueTransition.successorID, data, null)
    else changeState(falseTransition.successorID, data, null)
  }

  // needed as this extends BehaviorStateActor

  override protected def getAvailableAction: Array[ActionData] = Array()
  
  protected def stateReceive = {
    // execute an action
    case _ @ x=> println("DecisionStateActor -> unexpected stateReceive: " + x)
  }
}
