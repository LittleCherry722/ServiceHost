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

package de.tkip.sbpm.application.subject.behavior

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

sealed trait TransitionType
case class ExitCond(messageType: MessageType, target: Option[Target] = None) extends TransitionType {
  def actionType = messageType

  def subjectID = if (target.isDefined) target.get.subjectID else "None"
}
case class TimeoutCond(manual: Boolean, duration: Int) extends TransitionType
case class ErrorCond() extends TransitionType

case class Target(
  var subjectID: SubjectID,
  min: Int,
  max: Int,
  createNew: Boolean, // TODO we dont need create new
  variable: Option[String],
  private val defaultValues: Boolean) {

  val toVariable = variable.isDefined && variable.get != ""
  val toAll = defaultValues && !createNew && !toVariable // Dont need to all, always set users?

  private var _vars: Array[(SubjectID, UserID)] = Array()
  private var _targetUsers = Array[UserID]()

  def varSubjects = _vars
  def targetUsers: Array[UserID] = _targetUsers
  def toUnknownUsers = !toVariable && _targetUsers.isEmpty

  def insertVariable(v: Variable) {
    _vars = for (m <- v.messages) yield ((m.from, m.userID))
  }

  def insertTargetUsers(userIDs: Array[UserID]) {
    if (min <= userIDs.length && userIDs.length <= max) {
      _targetUsers = userIDs

    } else {
      throw new RuntimeException("Cant target more users than given in the range")
    }
  }
}

/**
 * models references between certain BehaviorStates
 */
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

object ActTransition {
  def apply(actionType: MessageType, successorID: SuccessorID) =
    Transition(ExitCond(actionType), successorID, 1)
}

object TimeoutTransition {
  def apply(manual: Boolean, successorID: SuccessorID) = {
    if (!manual)
      throw new RuntimeException("A timeout which is not manual needs a duration.")
    Transition(TimeoutCond(manual, -1), successorID, -1)
  }
  def apply(manual: Boolean, duration: Int, successorID: SuccessorID) =
    Transition(TimeoutCond(manual, duration), successorID, -1)
}
