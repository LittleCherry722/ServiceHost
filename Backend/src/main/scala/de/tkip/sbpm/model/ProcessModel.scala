package de.tkip.sbpm.model

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

object StateType extends Enumeration {
  type StateType = Value
  val StartStateType = Value("Start")
  val ActStateType = Value("Action")
  val SendStateType = Value("Send")
  val ReceiveStateType = Value("Receive")
  val EndStateType = Value("End")
}

import StateType._
case class State(id: String, name: String, stateType: StateType, transitions: Array[Transition])

case class Subject(subjectName: String, states: Array[State])

case class ProcessModel(processID: ProcessID, name: String, subjects: Array[Subject])