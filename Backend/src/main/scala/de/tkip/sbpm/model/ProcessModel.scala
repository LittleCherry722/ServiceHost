package de.tkip.sbpm.model

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

object StateType extends Enumeration {
  type StateType = Value
  val StartStateType = Value("start")
  val ActStateType = Value("action")
  val SendStateType = Value("send")
  val ReceiveWaitingStateType = Value("receivewaiting") // This type is to signalize that its a receive state, which is waiting for a message
  val ReceiveStateType = Value("receive")
  val EndStateType = Value("end")

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
