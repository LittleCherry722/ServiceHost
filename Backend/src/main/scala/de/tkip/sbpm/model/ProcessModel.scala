package de.tkip.sbpm.model

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

object StateType extends Enumeration {
  type StateType = Value
  val StartStateString = "start"
  val ActStateString = "action"
  val SendStateString = "send"
  val ReceiveStateString = "receive"
  val WaitingStateString = "waiting"
  val EndStateString = "end"

  val StartStateType = Value(StartStateString)
  val ActStateType = Value(ActStateString)
  val SendStateType = Value(SendStateString)
  val ReceiveStateType = Value(ReceiveStateString)
  val WaitingStateType = Value(WaitingStateString) // This type is to signalize that its a state, which is waiting for something and user can not interact
  val EndStateType = Value(EndStateString)

  // for marshalling and unmarshalling:
  def fromStringtoStateType(stateType: String): StateType = try {
    StateType.withName(stateType)
  } // TODO exceptionhandling

  def fromStateTypetoString(stateType: StateType): String = stateType.toString
}

import StateType.StateType
// name raus ist ws in id
case class State(id: StateID, name: String, stateType: StateType, transitions: Array[Transition])
case class Subject(id: String, states: Array[State])
case class ProcessGraph(subjects: Array[Subject])
case class ProcessModel(processID: ProcessID, name: String, graph: ProcessGraph) {
  def subjects = graph.subjects
}
