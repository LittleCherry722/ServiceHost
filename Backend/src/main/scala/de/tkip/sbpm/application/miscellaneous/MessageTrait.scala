package de.tkip.sbpm.application.miscellaneous

import akka.actor.ActorRef
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.InterfaceRef
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

/**
 * Extend this trait if you want to send a message to / over a subject provider
 */
trait SubjectProviderMessage {
  def userID: UserID
}

/**
 * Extend this trait if you want to send a message to the persistence actor
 */
trait PersistenceMessage

/**
 * Extend this trait if you want to send a message to / over a process instance 
 */
trait ProcessInstanceMessage {
  def processInstanceID: ProcessInstanceID
}

/**
 * Extend this trait if you want to send a message to a subject
 */
trait SubjectMessage {
  def processInstanceID: ProcessInstanceID
  def subjectID: SubjectID
}

/**
 * This trait is for messages which are send from the frontend (and want an answer)
 */
trait AnswerAbleMessage {
  private var _sender: InterfaceRef = null

  def sender = _sender

  def sender_=(sender: InterfaceRef) {
    if (_sender == null) {
      _sender = sender
    }
  }

  /**
   * This methods sets the sender and returns this AnswerAbleMessage itself
   */
  def withSender(sender: InterfaceRef): AnswerAbleMessage = {
    this.sender = sender
    this
  }
}

/**
 * This trait is for the answers of the messages with the previous trait
 */
trait AnswerMessage {
  def request: AnswerAbleMessage
  def sender: InterfaceRef = if (request == null) null else request.sender
}