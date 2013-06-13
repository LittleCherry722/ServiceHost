package de.tkip.sbpm.application

import de.tkip.sbpm._

trait ControlMessage
trait SubjectMessage {
  def subjectId: SubjectID
}

// request to get the information of the subject
case class ReadSubject(subjectId: SubjectID) extends SubjectMessage
// the subject information
case class SubjectAnswer(id: SubjectID, state: StateType, actions: Array[StateID])

// executes the specified action
case class ExecuteAction(subjectId: SubjectID, action: StateID) extends SubjectMessage

// all subjects switch to the startstate and restart
case object RestartExecution

// internal change the current subjectstate
case class ChangeState(id: StateID)

case class ChangeSubject(subjectId: SubjectID)

case class SubjectToSubjectMessage(from: SubjectID, to: SubjectID, message: String)
// Acknowledge receive
case object Ack