package de.tkip.sbpm.application

trait ControlMessage

// request to get the information of the subject
case class ReadSubject(id: SubjectID)
// the subject information
case class SubjectAnswer(id: SubjectID, state: StateType, actions: Array[String])

// executes the specified action
case class ExecuteAction(id: SubjectID, action: String)
// signals, that the action has been executed successfully
case object ActionExecuted

// all subjects switch to the startstate and restart
case object RestartExecution
