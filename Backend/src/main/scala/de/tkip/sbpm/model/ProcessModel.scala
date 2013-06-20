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

package de.tkip.sbpm.model

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import StateType.StateType
import de.tkip.sbpm.application.subject.behavior.Transition

object StateType extends Enumeration { // TODO just use a string?
  type StateType = Value
  // The string identifier in the graph
  val ActStateString = "action"
  val SendStateString = "send"
  val ReceiveStateString = "receive"
  val EndStateString = "end"

  // the internal enums
  val ActStateType = Value(ActStateString)
  val SendStateType = Value(SendStateString)
  val ReceiveStateType = Value(ReceiveStateString)
  val EndStateType = Value(EndStateString)

  // for marshalling and unmarshalling:
  def fromStringtoStateType(stateType: String): StateType = try {
    StateType.withName(stateType)
  } // TODO exceptionhandling

  def fromStateTypetoString(stateType: StateType): String = stateType.toString
}

trait SubjectLike {
  def id: SubjectID
  def inputPool: Int
  def multi: Boolean
  def external: Boolean
}
// name raus ist ws in id
case class State(id: StateID, text: String, stateType: StateType, startState: Boolean, transitions: Array[Transition])
case class Subject(id: SubjectID, inputPool: Int, states: Array[State], multi: Boolean = false) extends SubjectLike {
  lazy val external = false
}
case class ExternalSubject(id: SubjectID, inputPool: Int, multi: Boolean = false) extends SubjectLike {
  lazy val external = true
}
case class ProcessGraph(subjects: Map[String, SubjectLike])
