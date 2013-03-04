package de.tkip.sbpm.model

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

sealed trait TransitionType
case class ExitCond(messageType: MessageType, subjectID: SubjectID) extends TransitionType {
  def actionType = messageType
}
case class ErrorCond() extends TransitionType
case class TimeoutCond(manual: Boolean, duration: Int) extends TransitionType
/**
 * models references between certain BehaviorStates
 */
case class Transition(
  myType: TransitionType,
  successorID: SuccessorID) {

  // boolean type check functions
  def isExitCond = myType.isInstanceOf[ExitCond]
  def isTimeout = myType.isInstanceOf[TimeoutCond]
  def isErrorCond = myType.isInstanceOf[ErrorCond]

  def messageType = if (myType.isInstanceOf[ExitCond]) myType.asInstanceOf[ExitCond].messageType else ""
  def subjectID = if (myType.isInstanceOf[ExitCond]) myType.asInstanceOf[ExitCond].subjectID else ""
}

object ActTransition {
  def apply(actionType: MessageType, successorID: SuccessorID) =
    Transition(ExitCond(actionType, "None"), successorID)
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
