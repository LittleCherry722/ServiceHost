package de.tkip.sbpm.application.subject

import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.application.miscellaneous.AnswerMessage
import de.tkip.sbpm.application.miscellaneous.SubjectProviderMessage
import de.tkip.sbpm.application.miscellaneous.SubjectMessage
import de.tkip.sbpm.application.History
import de.tkip.sbpm.model.Target
import scala.collection.mutable.ArrayBuffer

// switch state messages 
case class StartSubjectExecution() extends SubjectBehaviorRequest

// internal subject messages TODO besserer trait name, braucht man den trait ueberhaupt?
sealed trait MessageObject
// message from subject to subject
protected case class SubjectToSubjectMessage(
  messageID: MessageID,
  var userID: UserID,
  from: SubjectID,
  fromSession: SubjectSessionID,
  target: Target,
  messageType: MessageType,
  messageContent: MessageContent) extends MessageObject {

  def to = target.subjectID

}

// acknowledge, that a message is stored in the input pool
protected case class Stored(messageID: MessageID) extends MessageObject

// TODO richtig einordnern
case class SubjectInternalMessageProcessed(subjectID: SubjectID)
case class SubjectTerminated(userID: UserID, subjectID: SubjectID, subjectSessionID: SubjectSessionID)
case class SubjectStarted(userID: UserID, subjectID: SubjectID, subjectSessionID: SubjectSessionID)

// external subject interaction messages
sealed trait SubjectBehaviorRequest
// The Request to get the available action from a single subject
case class GetAvailableAction(processInstanceID: ProcessInstanceID)
  extends SubjectBehaviorRequest // TODO eigentlich auch subject message

// TODO vllt in controlmessage verschieben, d sie jetzt direkt mit dem FE interagieren
case class ActionData(
  text: String, // = messagetype
  executeAble: Boolean,
  relatedSubject: Option[String] = None,
  messageContent: Option[String] = None)

// Answer to the GetAvailable Action request
case class AvailableAction(
  userID: UserID,
  processInstanceID: ProcessInstanceID,
  subjectID: SubjectID,
  subjectSessionID: SubjectSessionID,
  stateID: StateID,
  stateText: String,
  stateType: String,
  actionData: Array[ActionData])
  extends SubjectProviderMessage

// The Execution command from the user
case class ExecuteAction(
  userID: UserID,
  processInstanceID: ProcessInstanceID,
  subjectID: SubjectID,
  subjectSessionID: SubjectSessionID,
  stateID: StateID,
  stateType: String,
  actionData: ActionData)
// The response to an ExecuteAction Message
case class ActionExecuted(ea: ExecuteAction)

// TODO ExecuteActionAnswer genauer spezifizieren, zB naechste verfuegbare action
// TODO keine defaultparameter
case class ExecuteActionAnswer(
  execute: ExecuteAction,
  processID: ProcessID,
  isTerminated: Boolean,
  graphJson: String,
  history: History,
  availableActions: Array[AvailableAction]) extends AnswerMessage {
  def request = execute.asInstanceOf[AnswerAbleMessage]
}

object mixExecuteActionWithRouting {
  def apply(action: ExecuteAction): ExecuteAction =
    new ExecuteAction(
      action.userID,
      action.processInstanceID,
      action.subjectID,
      action.subjectSessionID,
      action.stateID,
      action.stateType,
      action.actionData) with AnswerAbleMessage with SubjectProviderMessage with SubjectMessage with SubjectBehaviorRequest
}
