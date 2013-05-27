package de.tkip.sbpm.model

import de.tkip.sbpm._

object TestData {

  def apply(id: Int): TestPair = testMap(id) 

  val test1 = TestPair(
    Subject(
      1,
      Array(
        State(0, Act, Array(1)),
        State(1, Act, Array(3, 2)),
        State(2, Act, Array(4)),
        State(3, Act, Array(0)),
        State(4, Act, Array(3)))),
    Subject(
      2,
      Array(
        State(0, Act, Array(1)),
        State(1, Act, Array(2)),
        State(2, Act, Array(0)))))

  val test2 = TestPair(
    Subject(
      1,
      Array(
        State(0, Act, Array(3, 1)),
        State(1, Send, Array(2)),
        State(2, Receive, Array(3)),
        State(3, Act, Array(0)))),
    Subject(
      2,
      Array(
        State(0, Receive, Array(1)),
        State(1, Send, Array(2)),
        State(2, Act, Array(0)))))

  private val testMap = Map(1 -> test1, 2 -> test2)
}

trait ProcessModel {

}

case class TestPair(subject1: Subject, subject2: Subject)

// first state in states is start state
case class Subject(subjectID: SubjectID, states: Array[State]) {

  // requirements
  require(states.size > 0, "A subject musst contain at least one state")

  // helping functions
  private val stateMap = (states map (s => (s.stateId, s))).toMap
  def state(id: Int) = stateMap(id)
}

//states are referenced by their StateID
case class State(stateId: StateID, stateType: StateType, transitions: Array[StateID]) {
  // requirements
  require(transitions.size > 0, "Every state needs an outgoing transition")
  if (stateType == Send || stateType == Receive) {
    require(
      transitions.size == 1,
      "Send- and Receivestates are only allowed to have one outgoing egde")
  }

}
