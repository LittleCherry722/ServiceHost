package de.tkip.sbpm.application.miscellaneous

import akka.actor.ActorRef

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.InterfaceRef
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.UserID

/**
 * This trait is for messages which are send from the frontend
 */
trait AnswerAbleMessage {
  private var _sender: InterfaceRef = null

  def sender = _sender

  def sender_=(sender: InterfaceRef) {
    if (_sender == null) {
      _sender = sender
    }
  }
}

/**
 * This trait is for the answers of the messages with the previous trait
 */
trait AnswerMessage[A <: MessageType.Answer] {
  self: A =>
  def sender: ActorRef = self.request.sender
}

/**
 * extend this trait if you want to send a message to the subject provider
 */
trait SubjectProviderMessage[A <: MessageType.User] {
  self: A =>
  def subjectProviderID = userID
}

/**
 * The types for the message traits
 */
protected object MessageType {
  type Answer = { def request: AnswerAbleMessage }
  type User = { def userID: UserID }
}