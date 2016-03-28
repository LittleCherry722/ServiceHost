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

import akka.actor.ActorRef

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
import de.tkip.sbpm.model.Graph
import de.tkip.sbpm.rest.google.GDriveControl.GDriveFileInfo

/*
 * Variable, message, message content definitions etc. are mainly as an example
 * of how variables could and should be structured to allow
 * sending to variables, sending variables, etc.
 *
 * The Main obstacles in using regular SubjectToSubjectMessages as a means of channel
 * transmissions are:
 *   - Current variables implementation is not compatible
 *   - Sending to Variables / Channels is not currently supported (sending to the sender of a message),
 *     in order to send to someone, this exact subject has to be in the graph, a subjectContainer has
 *     to be created etc. Ideally, sending to a graph subject that has not been instantiated, sending
 *     to an already existing graph subject, sending to a channel extracted from a message / variable
 *     and sending to an new or existing external subject should just consist of sending the same
 *     SubjectToSubjectMessage to an actorRef.
 *   - Variable manipulation states have to be implemented for recursively defined variables
 *   - Frontend needs support for sending variables to a subject, not only sending a message to a
 *     variable. This also needs support from the Backend though, as the Send state could and should
 *     just be automatically executed without user interaction.
 */
sealed trait MessageContent {
  def channels : Set[Channel] = Set.empty
}
case class SingleMessage(message: SubjectToSubjectMessage) extends MessageContent {
  override def channels : Set[Channel] = Set(message.fromChannel)
}
case class MessageSet(messages: Set[SubjectToSubjectMessage]) extends MessageContent {
  override def channels : Set[Channel] = messages.map(_.fromChannel)
}
case class TextContent(content: String) extends MessageContent
case object EmptyContent extends MessageContent

case class Channel(actor: ActorRef, agent: Agent)

// AgentMapping trait and AgentCandidates are not currently used, but might
// be necessary for the blackbox / service host implementation
sealed trait AgentMapping
case class AgentCandidates(candidates: Set[Agent]) extends AgentMapping
case class Agent(processId: ProcessID,
                 address: AgentAddress,
                 subjectId: SubjectID) extends AgentMapping

case class AgentAddress(ip: String, port: Int) {
  def toUrl = "@" + ip + ":" + port
}

/*
 * Original Subject Message content
 */

// switch state messages
case object StartSubjectExecution extends SubjectBehaviorRequest

// internal subject messages TODO besserer trait name, braucht man den trait ueberhaupt?
sealed trait MessageObject
// message from subject to subject
case class SubjectToSubjectMessage(messageID: MessageID,
                                   processID: ProcessID,
                                   userID: UserID,
                                   from: SubjectID,
                                   fromChannel: Channel,
                                   target: Target,
                                   messageName: MessageName,
                                   messageContent: MessageContent,
                                   fileID: Option[String] = None,
                                   var fileInfo: Option[GDriveFileInfo] = None) extends MessageObject {

  def to = target.subjectID

}

//protected case class TryTransportMessages extends MessageObject
//protected case class SubjectToSubjectMessageReceived(message: SubjectToSubjectMessage) extends MessageObject

// acknowledge, that a message is stored in the input pool
case class Stored(messageID: MessageID) extends MessageObject
// acknowledge, that the message was rejected by the input pool
case class Rejected(messageID: MessageID) extends MessageObject

// TODO richtig einordnern
case class SubjectTerminated(userID: UserID, subjectID: SubjectID)

protected[subject] case class MacroTerminated(macroID: String)
protected[subject] case object KillNonObserverStates
protected[subject] case object DisableNonObserverStates

// external subject interaction messages
sealed trait SubjectBehaviorRequest
// The Request to get the available action from a single subject
case class GetAvailableAction(processInstanceID: ProcessInstanceID)
  extends SubjectBehaviorRequest // TODO eigentlich auch subject message

case object GetProcessInstanceManager

// TODO vllt in controlmessage verschieben, da sie jetzt direkt mit dem FE interagieren
case class MessageData(messageID: MessageID,
                       userID: UserID,
                       messageContent: String,
                       title: Option[String] = None,
                       url: Option[String] = None,
                       iconLink: Option[String] = None)

case class TargetUser(min: Int, max: Int, external: Boolean, targetUsers: Array[UserID])

case class ActionData(
  text: String, // = messagetype
  var executeAble: Boolean, // VAR?!
  transitionType: String, // exitcondition or timeout
  targetUsersData: Option[TargetUser] = None, // target user of a send message
  relatedSubject: Option[String] = None, // the related subject of a send-/receive state
  messageContent: Option[String] = None, // for the send state: the message
  fileId: Option[String] = None, // for the send state: google drive id
  selectedAgent: Option[Agent] = None,
  possibleAgents: Option[Seq[Agent]] = None,
  messages: Option[Array[MessageData]] = None) // for the receive state

// Answer to the GetAvailable Action request
case class AvailableAction(
  id: Int,
  userID: UserID,
  processInstanceID: ProcessInstanceID,
  subjectID: SubjectID,
  macroID: String,
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
  macroID: String,
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
      action.macroID,
      action.stateID,
      action.stateType,
      action.actionData,
      action.googleId) with AnswerAbleMessage with SubjectProviderMessage with SubjectMessage with SubjectBehaviorRequest
}
