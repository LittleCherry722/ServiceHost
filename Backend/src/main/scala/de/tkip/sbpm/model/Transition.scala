package de.tkip.sbpm.model

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

/**
 * models references between certain BehaviourStates
 */
case class Transition(val messageType: MessageType,
                      val subjectName: SubjectName,
                      val successorID: SuccessorID = "") {
}

object ActTransition {
  def apply(actionType: MessageType, successorID: SuccessorID = "") =
    Transition(actionType, "Do", successorID)
}

object StartTransition {
  def apply(successorID: SuccessorID) = Transition("Start", "Go", successorID)
}
