package de.tkip.sbpm.model

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

sealed trait TransitionType
case class ExitCond(messageType: MessageType, subjectID: SubjectID) extends TransitionType {
  def actionType = messageType
}
case class Errorcond() extends TransitionType
case class Timeout(duration: Int) extends TransitionType
/**
 * models references between certain BehaviorStates
 */
case class Transition(
    myType: TransitionType,
    successorID: SuccessorID) {
  def messageType = if (myType.isInstanceOf[ExitCond]) myType.asInstanceOf[ExitCond].messageType else ""
  def subjectID = if (myType.isInstanceOf[ExitCond]) myType.asInstanceOf[ExitCond].subjectID else ""
}

object ActTransition {
  def apply(actionType: MessageType, successorID: SuccessorID) =
    Transition(ExitCond(actionType, "None"), successorID)
}

object TimeoutTransition {
  def apply(duration: Int, successorID: SuccessorID) =
    Transition(Timeout(duration), successorID)
}
