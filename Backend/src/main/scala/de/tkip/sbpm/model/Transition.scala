package de.tkip.sbpm.model

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.Variable

sealed trait TransitionType
case class ExitCond(messageType: MessageType, target: Option[Target] = None) extends TransitionType {
  def actionType = messageType

  def subjectID = if (target.isDefined) target.get.subjectID else "None"
}
case class TimeoutCond(manual: Boolean, duration: Int) extends TransitionType
case class ErrorCond() extends TransitionType

case class Target(
  subjectID: SubjectID,
  min: Int,
  max: Int,
  createNew: Boolean,
  variable: Option[String],
  private val defaultValues: Boolean) {

  val toVariable = variable.isDefined && variable.get != ""
  val toAll = defaultValues && !createNew && !toVariable

  private var _vars: Array[(SubjectID, SubjectSessionID)] = Array()
  private var _targetUsers = Array[UserID]()

  def varSubjects = _vars
  def targetUsers: Array[UserID] = _targetUsers

  def insertVariable(v: Variable) {
    _vars = for (m <- v.messages) yield ((m.from, m.fromSession))
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
  storeVar: String = "") {

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
