package de.tkip.sbpm.application.subject

import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.application.miscellaneous.AnswerMessage

// switch state messages 
case class StartSubjectExecution() extends SubjectBehaviorRequest
protected case class NextState(state: StateID) extends SubjectBehaviorRequest

// internal subject messages
sealed trait MessageObject
protected case class SubjectMessage(from: SubjectName, to: SubjectName, messageType: MessageType, messageContent: MessageContent) extends MessageObject
protected case class TransportMessage(from: SubjectName, messageType: MessageType, messageContent: MessageContent) extends MessageObject
protected case object Stored extends MessageObject
protected case class RequestForMessages(exitConds: Array[SubjectMessageRouting])

// TODO richtig einordnern
case class SubjectTerminated(userdID: UserID, subjectID: SubjectID)

// external subject interaction messages 
sealed trait SubjectBehaviorRequest
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
// TODO ExecuteActionAnswer genauer spezifizieren, zB naechste verfuegtbare action
case class ExecuteActionAnswer(request: ExecuteAction) extends AnswerMessage[ExecuteActionAnswer]

object ExecuteAction {
  def apply(available: AvailableAction, actionInput: String): ExecuteAction =
    ExecuteAction(available.userID,
      available.processInstanceID,
      available.subjectID,
      available.stateID,
      available.stateType,
      actionInput)
}
