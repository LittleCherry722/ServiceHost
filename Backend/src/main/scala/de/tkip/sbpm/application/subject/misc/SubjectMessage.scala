/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.application.subject.misc

import scala.collection.mutable.ArrayBuffer

import de.tkip.sbpm.application.subject.behavior.Target
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.miscellaneous.{
  AnswerAbleMessage,
  AnswerMessage,
  SubjectProviderMessage,
  SubjectMessage,
  ProcessInstanceData
}
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.model.Graph

import de.tkip.sbpm.rest.google.GDriveControl.GDriveFileInfo

// switch state messages 
case class StartSubjectExecution() extends SubjectBehaviorRequest

// internal subject messages TODO besserer trait name, braucht man den trait ueberhaupt?
sealed trait MessageObject
// message from subject to subject
protected case class SubjectToSubjectMessage(
  messageID: MessageID,
  processID: ProcessID,
  var userID: UserID, // TODO why is this a var?
  var from: SubjectID,
  target: Target,
  messageType: MessageType,
  messageContent: MessageContent,
  fileID: Option[String] = None,
  var fileInfo: Option[GDriveFileInfo] = None
) extends MessageObject {

  def to = target.subjectID

}

protected case class TryTransportMessages extends MessageObject
protected case class SubjectToSubjectMessageReceived(message: SubjectToSubjectMessage) extends MessageObject

// acknowledge, that a message is stored in the input pool
protected case class Stored(messageID: MessageID) extends MessageObject
// acknowledge, that the message was rejected by the input pool
protected case class Rejected(messageID: MessageID) extends MessageObject

// TODO richtig einordnern
case class SubjectTerminated(userID: UserID, subjectID: SubjectID)

// external subject interaction messages
sealed trait SubjectBehaviorRequest
// The Request to get the available action from a single subject
case class GetAvailableAction(processInstanceID: ProcessInstanceID)
  extends SubjectBehaviorRequest // TODO eigentlich auch subject message

// TODO vllt in controlmessage verschieben, d sie jetzt direkt mit dem FE interagieren
case class MessageData(
  userID: UserID,
  messageContent: String,
  title: Option[String] = None,
  url: Option[String] = None,
  iconLink: Option[String] = None
)

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
  actionData: ActionData,
  googleId: Option[String] = None) // the google id of the person, who executes the action
// The response to an ExecuteAction Message
case class ActionExecuted(ea: ExecuteAction)

case class ExecuteActionAnswer(
  execute: ExecuteAction,
  answer: ProcessInstanceData) extends AnswerMessage {
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
      action.actionData,
      action.googleId) with AnswerAbleMessage with SubjectProviderMessage with SubjectMessage with SubjectBehaviorRequest
}
