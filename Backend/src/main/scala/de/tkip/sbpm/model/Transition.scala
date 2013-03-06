package de.tkip.sbpm.model

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

sealed trait TransitionType
case class ExitCond(messageType: MessageType, target: Option[Target] = None) extends TransitionType {
  def actionType = messageType

  def subjectID = if (target.isDefined) target.get.subjectID else "None"
}
case class TimeoutCond(manual: Boolean, duration: Int) extends TransitionType
case class ErrorCond() extends TransitionType

case class Target(
  subjectID: SubjectID,
  min: Int = -1,
  max: Int = -1,
  createNew: Boolean = false,
  variable: Option[String] = None) {
  // TODO validate
  val toAll = min == -1 && max == -1 && !createNew
  // TODO fill in variable
}

/**
 * models references between certain BehaviorStates
 */
case class Transition(
  myType: TransitionType,
  successorID: SuccessorID,
  storeVar: String = "") {

  // boolean type check functions
  def isExitCond = myType.isInstanceOf[ExitCond]
  def isTimeout = myType.isInstanceOf[TimeoutCond]
  def isErrorCond = myType.isInstanceOf[ErrorCond]

  def messageType = if (myType.isInstanceOf[ExitCond]) myType.asInstanceOf[ExitCond].messageType else ""
  def target = if (myType.isInstanceOf[ExitCond]) myType.asInstanceOf[ExitCond].target else None
  def subjectID = if (myType.isInstanceOf[ExitCond]) myType.asInstanceOf[ExitCond].subjectID else ""

  def storeToVar: Boolean = storeVar == ""
}

object ActTransition {
  def apply(actionType: MessageType, successorID: SuccessorID) =
    Transition(ExitCond(actionType), successorID)
}

object TimeoutTransition {
  def apply(manual: Boolean, successorID: SuccessorID) = {
    if (!manual)
      throw new RuntimeException("A timeout which is not manual needs a duration.")
    Transition(TimeoutCond(manual, -1), successorID)
  }
  def apply(manual: Boolean, duration: Int, successorID: SuccessorID) =
    Transition(TimeoutCond(manual, duration), successorID)
}
