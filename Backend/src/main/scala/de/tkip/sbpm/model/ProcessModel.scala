package de.tkip.sbpm.model

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

object StateType extends Enumeration {
  type StateType = Value
  val StartStateType = Value("Start")
  val ActStateType = Value("Action")
  val SendStateType = Value("Send")
  val ReceiveStateType = Value("Receive")
  val EndStateType = Value("End")

  // for marshalling and unmarshalling:
  def fromStringtoStateType(stateType: String): StateType = {
    // TODO
    StartStateType
  }

  def fromStateTypetoString(stateType: StateType): String = {
    // TODO
    ""
  }
}

import StateType.StateType

//case class Actions(stateType: StateType, actions: Array[String])

// name raus ist ws in id
case class State(id: String, name: String, stateType: StateType, transitions: Array[Transition])

case class Subject(id: String, states: Array[State])
case class ProcessGraph(subjects: Array[Subject])
case class ProcessModel(processID: ProcessID, name: String, graph: ProcessGraph) {
  def subjects = graph.subjects
}
