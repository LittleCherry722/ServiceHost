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
import de.tkip.sbpm.model.Graph

// switch state messages 
case class StartSubjectExecution() extends SubjectBehaviorRequest

// internal subject messages TODO besserer trait name, braucht man den trait ueberhaupt?
sealed trait MessageObject
// message from subject to subject
protected case class SubjectToSubjectMessage(
  messageID: MessageID,
  var userID: UserID,// TODO why is this a var?
  from: SubjectID,
  target: Target,
  messageType: MessageType,
  messageContent: MessageContent,
  fileID: Option[String] = None) extends MessageObject {

  def to = target.subjectID

}

// acknowledge, that a message is stored in the input pool
protected case class Stored(messageID: MessageID) extends MessageObject

// TODO richtig einordnern
case class SubjectInternalMessageProcessed(userID: UserID)
case class SubjectTerminated(userID: UserID, subjectID: SubjectID)
case class SubjectStarted(userID: UserID, subjectID: SubjectID)

// external subject interaction messages
sealed trait SubjectBehaviorRequest
// The Request to get the available action from a single subject
case class GetAvailableAction(processInstanceID: ProcessInstanceID)
  extends SubjectBehaviorRequest // TODO eigentlich auch subject message

// TODO vllt in controlmessage verschieben, d sie jetzt direkt mit dem FE interagieren
case class MessageData(userID: UserID, messageContent: String, fileId: Option[String] = None)

case class TargetUser(min: Int, max: Int, targetUsers: Array[UserID])

case class ActionData(
  text: String, // = messagetype
  executeAble: Boolean,
  transitionType: String, // exitcondition or timeout
  targetUsersData: Option[TargetUser] = None, // target user of a send message
  relatedSubject: Option[String] = None, // the related subject of a send-/receive state
  messageContent: Option[String] = None, // for the send state: the message
  fileId: Option[String] = None, // for the send state: google drive id
  messages: Option[Array[MessageData]] = None) // for the receive state

// Answer to the GetAvailable Action request
case class AvailableAction(
  userID: UserID,
  processInstanceID: ProcessInstanceID,
  subjectID: SubjectID,
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
  stateID: StateID,
  stateType: String,
  actionData: ActionData)
// The response to an ExecuteAction Message
case class ActionExecuted(ea: ExecuteAction)

case class ExecuteActionAnswer(
  execute: ExecuteAction,
  processID: ProcessID,
  isTerminated: Boolean,
  graph: Graph,
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
      action.stateID,
      action.stateType,
      action.actionData) with AnswerAbleMessage with SubjectProviderMessage with SubjectMessage with SubjectBehaviorRequest
}