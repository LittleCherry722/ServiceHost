package de.tkip.sbpm.model

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

object StateType extends Enumeration {// TODO just use a string?
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

import StateType.StateType
// name raus ist ws in id
case class State(id: StateID, text: String, stateType: StateType, startState: Boolean, transitions: Array[Transition])
case class Subject(id: SubjectID, inputPool: Int, states: Array[State], multi: Boolean = false, external: Boolean = false)
case class ProcessGraph(subjects: Map[String, Subject])
