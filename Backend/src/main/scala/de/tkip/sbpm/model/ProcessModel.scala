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
import de.tkip.sbpm.application.subject.behavior.Transition
import StateType.StateType
import scala.collection.immutable.Map

object StateType extends Enumeration { // TODO just use a string?
  type StateType = Value
  // The string identifier in the graph
  val ActStateString = "action"
  val SendStateString = "send"
  val ReceiveStateString = "receive"
  val EndStateString = "end"
  val OpenIPStateString = "$openip"
  val CloseIPStateString = "$closeip"
  val IsIPEmptyStateString = "$isipempty"
  val ActivateStateString = "$activatestate"
  val DeactivateStateString = "$deactivatestate"
  val ModalSplitStateString = "modalsplit"
  val ModalJoinStateString = "modaljoin"
  val ArchiveStateString = "$archive"
  val MacroStateString = "macro"

  // the internal enums
  val ActStateType = Value(ActStateString)
  val SendStateType = Value(SendStateString)
  val ReceiveStateType = Value(ReceiveStateString)
  val EndStateType = Value(EndStateString)
  val OpenIPStateType = Value(OpenIPStateString)
  val CloseIPStateType = Value(CloseIPStateString)
  val IsIPEmptyStateType = Value(IsIPEmptyStateString)
  val ActivateStateType = Value(ActivateStateString)
  val DeactivateStateType = Value(DeactivateStateString)
  val ModalSplitStateType = Value(ModalSplitStateString)
  val ModalJoinStateType = Value(ModalJoinStateString)
  val MacroStateType = Value(MacroStateString)
  val ArchiveStateType = Value(ArchiveStateString)

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
  def variablesMap:Map[String,String]
}
// name raus ist ws in id
case class State(
  id: StateID,
  text: String,
  stateType: StateType,
  startState: Boolean,
  observerState: Boolean,
  callMacro: Option[String],
  options: StateOptions,
  transitions: Array[Transition])
case class StateOptions(
  messageType: Option[MessageType],
  subjectId: Option[SubjectID],
  correlationId: Option[String],
  conversation: Option[String],
  stateId: Option[StateID])

case class ProcessMacro(name: String, states: Array[State])
case class Subject(
  id: SubjectID,
  inputPool: Int,
  // TODO macroName -> states?
  //  macros: Map[String, Array[State]],
  macros: Map[String, ProcessMacro],
  //  states: Array[State],
  multi: Boolean,
  variablesMap:Map[String,String]) extends SubjectLike {
  lazy val external = false
  // TODO remove this function?
  def states: Array[State] = mainMacro.states
  def mainMacro = macros(mainMacroName)
  //TODO aendern
  def mainMacroName = "##main##"
}
case class ExternalSubject(
  id: SubjectID,
  inputPool: Int,
  multi: Boolean,
  relatedProcessId: Option[ProcessID],
  relatedSubjectId: Option[SubjectID],
  relatedInterfaceId: Option[SubjectID],
  url: Option[String],
  variablesMap:Map[String,String]) extends SubjectLike {
  lazy val external = true
}
case class ProcessGraph(subjects: Map[String, SubjectLike])