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

package de.tkip.sbpm.application.history

import java.util.Date
import akka.actor.ActorRef
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Buffer
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

// represents an entry in the history (a state transition inside a subject)
case class Entry(timestamp: Date, // time transition occurred
                 subject: String, // respective subject
                 fromState: State, // transition initiating state
                 toState: State, // end state of transition
                 message: Option[Message] = None) // message that was sent in transition (None if none)
// describes properties of a state
case class State(name: String, stateType: String)
// message exchanged in a state transition
case class Message(id: Int,
                   messageType: String,
                   from: String, // sender subject of message
                   to: String, // receiver subject of message 
                   data: String, // link to msg payload
                   files: Option[Seq[MessagePayloadLink]] = None) // link to file attachments
// represents a link to a message payload which contains a actor ref 
// and a payload id that is needed by that actor to identify payload
case class MessagePayloadLink(actor: ActorRef, payloadId: String)
// this message can be sent to message payload providing actors referenced in
// message payload link to retrieve actual payload
case class GetMessagePayload(messageId: Int, payloadId: String)

// message to report a transition in the internal behavior
// to the corresponding subject actor
case class Transition(from: State, to: State, message: Message)

case class NewEntry(
  var id: String, //"<INT_UNIQUE_ID>"
  var processName: String, //"Travel Request"
  var processInstanceId: ProcessInstanceID, //0
  processStarted: Option[Date], //System.currentTimeMillis
  processEnd: Option[Date],
  userId: UserID, //13
  subjectId: SubjectID, //"Employee"
//  fromState: NewState, //hier kann man eventuell den alten State weiter verwenden
  transition: NewTransition,
//  toState: NewState,
  messages: Option[Seq[NewMessage]]
)

case class NewState(text: String, stateType: String)

case class NewTransition(fromState: NewState, text: String, transitionType: String, toState: NewState)

case class NewMessage(
  messageIds: Seq[MessageID],
  fromUserId: UserID,
  toUserIds: Seq[UserID],
  messageType: MessageType,
  text: MessageContent
)

case class NewHistory(
  entries: Buffer[NewEntry] = ArrayBuffer[NewEntry]() // recorded state transitions in the history
)
