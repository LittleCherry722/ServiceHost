package de.tkip.sbpm.model

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.miscellaneous.ExitCond

/**
 * models references between certain BehaviourStates
 */
case class Transition(val messageType: MessageType,
                      val subjectName: SubjectName,
                      val successorID: SuccessorID = "") {
  val exitCond: ExitCond = ExitCond(messageType, subjectName)
}

object ActTransition {
  def apply(actionType: MessageType, successorID: SuccessorID = "") =
    Transition(actionType, "Do", successorID)
}

object StartTransition {
  def apply(successorID: SuccessorID = "") = Transition("Start", "Go", successorID)
}
