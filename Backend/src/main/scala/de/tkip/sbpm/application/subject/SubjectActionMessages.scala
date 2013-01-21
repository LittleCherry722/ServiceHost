package de.tkip.sbpm.application.subject

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.StateType._

sealed trait BehaviorRequest

case class GetAvailableAction(processInstanceID: ProcessInstanceID) extends BehaviorRequest

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
                         actionInput: String) extends BehaviorRequest

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