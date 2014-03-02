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

import de.tkip.sbpm.model.ProcessAttributes._
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
  val DecisionStateString = "$decision"
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
  val DecisionStateType = Value(DecisionStateString)
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

case class Target(
                   var subjectID: SubjectID,
                   min: Int,
                   max: Int,
                   createNew: Boolean, // TODO we dont need create new
                   variable: Option[String],
                   toExternal: Boolean,
                   private val defaultValues: Boolean) {

  val toVariable = variable.isDefined && variable.get != ""
  val toAll = defaultValues && !createNew && !toVariable // Dont need to all, always set users?

  private var _vars: Array[(SubjectID, UserID)] = Array()
  private var _targetUsers = Array[UserID]()

  def varSubjects = _vars
  def targetUsers: Array[UserID] = _targetUsers
  def toUnknownUsers = !toVariable && _targetUsers.isEmpty
}

sealed trait TransitionType
case class ExitCond(messageType: MessageType, target: Option[Target] = None) extends TransitionType {
  def actionType = messageType
  def subjectID = if (target.isDefined) target.get.subjectID else "None"
}
case class TimeoutCond(manual: Boolean, duration: Int) extends TransitionType
case class ErrorCond() extends TransitionType

trait SubjectLike {
  def id: SubjectID
  def inputPool: Int
  def multi: Boolean
  def external: Boolean
  def variablesMap:Map[String,String]
}

case class Transition(
                       myType: TransitionType,
                       successorID: SuccessorID,
                       priority: Int,
                       storeVar: Option[String] = None) {

  // boolean type check functions
  def isExitCond = myType.isInstanceOf[ExitCond]
  def isTimeout = myType.isInstanceOf[TimeoutCond]
  def isErrorCond = myType.isInstanceOf[ErrorCond]

  def messageType = if (myType.isInstanceOf[ExitCond]) myType.asInstanceOf[ExitCond].messageType else ""
  def target = if (myType.isInstanceOf[ExitCond]) myType.asInstanceOf[ExitCond].target else None
  def subjectID = if (myType.isInstanceOf[ExitCond]) myType.asInstanceOf[ExitCond].subjectID else ""

  def storeToVar: Boolean = storeVar != ""
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
  relatedSubjectId: Option[SubjectID],
  relatedProcessId: Option[Int],
  relatedInterfaceId: Option[SubjectID],
  isImplementation: Option[Boolean],
  url: Option[String],
  variablesMap:Map[String,String]) extends SubjectLike {
  lazy val external = true
}
case class ProcessGraph(subjects: Map[String, SubjectLike])