package de.tkip.sbpm.application.history

import java.util.Date
import akka.actor.ActorRef
 
// represents an entry in the history (a state transition inside a subject)
case class Entry(timestamp: Date, // time transition occurred
                 subject: String, // respective subject
                 fromState: State = null, // transition initiating state (null if start state)
                 toState: State, // end state of transition
                 message: Message = null) // message that was sent in transition (null if none)
// describes properties of a state
case class State(name: String, stateType: String)
// message exchanged in a state transition
case class Message(id: Int,
                   messageType: String,
                   from: String, // sender subject of message
                   to: String, // receiver subject of message 
                   data: String, // link to msg payload
                   files: Seq[MessagePayloadLink] = null) // link to file attachments
// represents a link to a message payload which contains a actor ref 
// and a payload id that is needed by that actor to identify payload
case class MessagePayloadLink(actor: ActorRef, payloadId: String)
// this message can be sent to message payload providing actors referenced in
// message payload link to retrieve actual payload
case class GetMessagePayload(messageId: Int, payloadId: String)

// message to report a transition in the internal behavior
// to the corresponding subject actor
case class Transition(from: State, to: State, message: Message)