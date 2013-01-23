package de.tkip.sbpm.application.subject

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.application.miscellaneous.AnswerMessage

trait SubjectBehaviorRequest

// The Request to get the available action from a single subject
case class GetAvailableAction(processInstanceID: ProcessInstanceID) extends SubjectBehaviorRequest

// Answer to the GetAvailable Action request
case class AvailableAction(userID: UserID,
                           processInstanceID: ProcessInstanceID,
                           subjectID: SubjectID,
                           stateID: StateID,
                           stateType: StateType,
                           actionData: Array[String])
// The Execution command from the user
case class ExecuteAction(userID: UserID,
                         processInstanceID: ProcessInstanceID,
                         subjectID: SubjectID,
                         stateID: StateID,
                         stateType: StateType,
                         actionInput: String)
    extends AnswerAbleMessage
    with SubjectBehaviorRequest

object ExecuteAction {
  def apply(available: AvailableAction, actionInput: String): ExecuteAction =
    ExecuteAction(available.userID,
      available.processInstanceID,
      available.subjectID,
      available.stateID,
      available.stateType,
      actionInput)

}

case class ActionExecuted(stateID: StateID)
case class ExecuteActionAnswer(request: ExecuteAction) extends AnswerMessage[ExecuteActionAnswer]
                         
// GetAction:
//subjectinformation = processinstanceid, subjectid
//type (act, send, receive..)
// typeaction
//  act => zB approval denial
//  receive => Nachrichteinhalt
//  send => '

// ExecuteAction
// subjectinformation
// type
// executeString 
//  act => action
// receive => '
// send => Nachrichteninhalt