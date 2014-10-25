package de.tkip.sbpm.newmodel

import ProcessModelTypes._

sealed trait SubjectLike {
  def name: String
  def id: SubjectId
  def multi: Boolean

  def startState: StateId
  def states: Set[State]
}

// the id of the related subject must equal the id of this subject
case class ExternalSubject(relatedProcess: ProcessId,
                           id: SubjectId,
                           name: String,
                           multi: Boolean) extends SubjectLike {
  def startState = 0
  def states = Set()
}

// an interface also has a behavior, but cannot be the start subject
case class InterfaceSubject(id: SubjectId,
                            name: String,
                            multi: Boolean,
                            ipSize: Int,
                            startState: StateId,
                            states: Set[State]) extends SubjectLike

// an instant interface has no behavior an no reference
case class InstantInterface(id: SubjectId,
                            name: String) extends SubjectLike {
  // multi InstantInterfaces are possible?
  def multi = false
  def startState = 0
  def states = Set()
}

case class Subject(id: SubjectId,
                   name: String,
                   startSubject: Boolean,
                   multi: Boolean,
                   ipSize: Int,
                   startState: StateId,
                   states: Set[State],
                   macros: Set[Macro]) extends SubjectLike {

  // requirements
  private val stateIds = states map (_.id)
  require(
    stateIds.size == states.size,
    "State id's must be unique")
  require(
    !(startSubject && multi),
    "The start-subject cannot be a multi-subject")
  require(
    stateIds contains startState,
    "The start-state musst be a state of this subject")
  require(
    ipSize > 0,
    "Inputpool-size musst be greater than zero")
  for (
    s <- states;
    transition <- s.transitions
  ) {
    require(
      stateIds contains transition.successor,
      "Every transition musst end in a state of the subject, failed for id: \""
        + transition.successor + "\"")
  }

  // helping methods
  final def single = !multi
  private val stateMap = states map (s => (s.id, s)) toMap
  def state(id: StateId) = stateMap(id)

  private val macroMap = macros map (m => (m.name, m)) toMap
  def macro(name: String) = macroMap(name)
}