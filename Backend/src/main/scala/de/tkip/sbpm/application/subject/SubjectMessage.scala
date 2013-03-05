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

// range
// (+)bestimmtes ziel
// createnew
// toall
protected case class Target(subjectID: SubjectID) {

}

protected case class SubjectToMultiSubjectMessage(message: SubjectToSubjectMessage) {
  def toAll = true
  def min = 1
  def max = 5
  def createNew = true
  def toVar: Array[String] = null
  // variabeln?
}
// stored message in the inputpool
protected case class TransportMessage(messageID: MessageID, from: SubjectID, messageType: MessageType, messageContent: MessageContent) extends MessageObject
// message to inform the receive state, that the inputpool has no messages for him
protected case object InputPoolEmpty
// acknowledge, that a message is stored in the input pool
protected case class Stored(messageID: MessageID) extends MessageObject
// request for the input pool that a state want to know his messages
protected case class RequestForMessages(exitConds: Array[SubjectMessageRouting])
// request for the input pool that a state terminated and the inputpool should
// delete the remaining message requests
protected case class RemoveMessageRequests(exitConds: Array[SubjectMessageRouting])

// TODO richtig einordnern
case class SubjectTerminated(userID: UserID, subjectID: SubjectID, subjectSessionID: SubjectSessionID)

// external subject interaction messages
sealed trait SubjectBehaviorRequest
// The Request to get the available action from a single subject
case class GetAvailableAction(processInstanceID: ProcessInstanceID)
  extends SubjectBehaviorRequest // TODO eigentlich auch subject message

// TODO vllt in controlmessage verschieben, d sie jetzt direkt mit dem FE interagieren
case class ActionData(text: String, // = messagetype
  executeAble: Boolean = false,
  relatedSubject: Option[String] = None,
  messageContent: Option[String] = None)

// Answer to the GetAvailable Action request
case class AvailableAction(userID: UserID,
  processInstanceID: ProcessInstanceID,
  subjectID: SubjectID,
  stateID: StateID,
  stateText: String,
  stateType: String,
  actionData: Array[ActionData])
  extends SubjectProviderMessage

// The Execution command from the user
case class ExecuteAction(userID: UserID,
  processInstanceID: ProcessInstanceID,
  subjectID: SubjectID,
  stateID: StateID,
  stateType: String,
  actionData: ActionData)

// TODO ExecuteActionAnswer genauer spezifizieren, zB naechste verfuegbare action
// TODO keine defaultparameter
case class ExecuteActionAnswer(execute: ExecuteAction,
  processID: ProcessID,
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
      action.stateID,
      action.stateType,
      action.actionData) with AnswerAbleMessage with SubjectProviderMessage with SubjectMessage with SubjectBehaviorRequest
}
