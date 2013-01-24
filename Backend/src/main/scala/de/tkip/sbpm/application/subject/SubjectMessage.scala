package de.tkip.sbpm.application.subject

import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.application.miscellaneous.AnswerMessage
import de.tkip.sbpm.application.miscellaneous.SubjectProviderMessage
import de.tkip.sbpm.application.miscellaneous.SubjectMessage

// switch state messages 
case class StartSubjectExecution() extends SubjectBehaviorRequest
protected case class NextState(state: StateID) extends SubjectBehaviorRequest

// internal subject messages
sealed trait MessageObject
protected case class SubjectInternalMessage(from: SubjectName, to: SubjectName, messageType: MessageType, messageContent: MessageContent) extends MessageObject
protected case class TransportMessage(from: SubjectName, messageType: MessageType, messageContent: MessageContent) extends MessageObject
protected case object Stored extends MessageObject
protected case class RequestForMessages(exitConds: Array[SubjectMessageRouting])

// TODO richtig einordnern
case class SubjectTerminated(userID: UserID, subjectID: SubjectID)

// external subject interaction messages
sealed trait SubjectBehaviorRequest
// The Request to get the available action from a single subject
case class GetAvailableAction(processInstanceID: ProcessInstanceID)
  extends SubjectBehaviorRequest // TODO eigentlich auch subject message

// Answer to the GetAvailable Action request
case class AvailableAction(userID: UserID,
                           processInstanceID: ProcessInstanceID,
                           subjectID: SubjectID,
                           stateID: StateID,
                           stateType: String,
                           actionData: Array[String])
    extends SubjectProviderMessage

// The Execution command from the user
case class ExecuteAction(userID: UserID,
                         processInstanceID: ProcessInstanceID,
                         subjectID: SubjectID,
                         stateID: StateID,
                         stateType: String,
                         actionInput: String)
    extends AnswerAbleMessage
    with SubjectBehaviorRequest
    with SubjectMessage
    with SubjectProviderMessage

// TODO ExecuteActionAnswer genauer spezifizieren, zB naechste verfuegbare action
case class ExecuteActionAnswer(request: ExecuteAction) extends AnswerMessage

object ExecuteAction {
  def apply(available: AvailableAction, actionInput: String): ExecuteAction =
    ExecuteAction(available.userID,
      available.processInstanceID,
      available.subjectID,
      available.stateID,
      available.stateType,
      actionInput)
}
