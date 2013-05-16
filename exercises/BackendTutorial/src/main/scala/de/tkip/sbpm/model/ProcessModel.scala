package de.tkip.sbpm.model

import de.tkip.sbpm._

object TestData {

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
}

trait ProcessModel {

}

case class TestPair(subject1: Subject, subject2: Subject)

// first state in states is start state
case class Subject(subjectID: SubjectID, states: Array[State]) {
  private val stateMap = (states map (s => (s.stateId, s))).toMap
  def state(id: Int) = stateMap(id)
}

//states are referenced by their StateID
case class State(stateId: StateID, stateType: StateType, transitions: Array[StateID])
