package de.tkip.sbpm.application.miscellaneous

import ProcessAttributes._

/**
 * models references between certain BehaviourStates
 */
case class Transition(val messageType: MessageType,
                      val subjectName: SubjectName,
                      val successorID: SuccessorID = "") {
  val exitCond: ExitCond = ExitCond(messageType, subjectName)
}