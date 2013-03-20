package de.tkip.sbpm.model

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

object StateType extends Enumeration {
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
case class ProcessGraph(subjects: Array[Subject]) {

  // This map matches SubjectIDs to their indexes in the subjectarray
  private val subjectToIndexMap: Map[SubjectID, Int] =
    (subjects.map(_.id)).zipWithIndex.toMap

  /**
   * Returns whether this graph has a subjects
   */
  def hasSubject(id: SubjectID): Boolean = subjectToIndexMap.contains(id)

  /**
   * Returns the Subject with this id,
   * returns null, if this subject does not exists
   */
  def subject(id: SubjectID): Subject =
    if (subjectToIndexMap.contains(id)) subjects(subjectToIndexMap(id)) else null
}