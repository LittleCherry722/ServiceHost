package de.tkip.sbpm.application.subject

import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.application.miscellaneous.AnswerMessage
import de.tkip.sbpm.application.miscellaneous.SubjectProviderMessage
import de.tkip.sbpm.application.miscellaneous.SubjectMessage
import de.tkip.sbpm.application.History

// switch state messages 
case class StartSubjectExecution() extends SubjectBehaviorRequest
protected case class NextState(state: StateID) extends SubjectBehaviorRequest

// internal subject messages TODO besserer trait name, braucht man den trait ueberhaupt?
sealed trait MessageObject
// message from subject to subject
protected case class SubjectInternalMessage(from: SubjectName, to: SubjectName, messageType: MessageType, messageContent: MessageContent) extends MessageObject
// stored message in the inputpool
protected case class TransportMessage(from: SubjectName, messageType: MessageType, messageContent: MessageContent) extends MessageObject
// acknowledge, that a message is stored in the input pool
protected case object Stored extends MessageObject
// request for the input pool that a state want to know his messages
protected case class RequestForMessages(exitConds: Array[SubjectMessageRouting])

// TODO richtig einordnern
case class SubjectTerminated(userID: UserID, subjectID: SubjectID)

// external subject interaction messages
sealed trait SubjectBehaviorRequest
// The Request to get the available action from a single subject
case class GetAvailableAction(processInstanceID: ProcessInstanceID)
  extends SubjectBehaviorRequest // TODO eigentlich auch subject message

// TODO vllt in controlmessage verschieben, d sie jetzt direkt mit dem FE interagieren
// Answer to the GetAvailable Action request
case class AvailableAction(userID: UserID,
                           processInstanceID: ProcessInstanceID,
                           subjectID: SubjectID,
                           stateID: StateID,
                           stateName: String,
                           stateType: String,
                           actionData: Array[String])
    extends SubjectProviderMessage

// The Execution command from the user
case class ExecuteAction(userID: UserID,
                         processInstanceID: ProcessInstanceID,
                         subjectID: SubjectID,
                         stateID: StateID,
                         stateType: String,
                         actionData: String)

// TODO ExecuteActionAnswer genauer spezifizieren, zB naechste verfuegbare action
// TODO keine defaultparameter
case class ExecuteActionAnswer(execute: ExecuteAction,
                               processID: ProcessID = -1,
                               graphJson: String = null,
                               history: History = null,
                               availableActions: Array[AvailableAction] = null) extends AnswerMessage {
  def request = execute.asInstanceOf[AnswerAbleMessage]
}

object mixExecuteActionWithRouting {
  def apply(action: ExecuteAction): ExecuteAction =
    new ExecuteAction(
      action.userID,
      action.processInstanceID,
      action.subjectID,
      action.stateID,
      action.stateType,
      action.actionData) with AnswerAbleMessage with SubjectProviderMessage with SubjectMessage with SubjectBehaviorRequest
}
