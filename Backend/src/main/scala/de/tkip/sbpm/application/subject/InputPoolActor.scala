package de.tkip.sbpm.application.subject

import scala.collection.mutable.ArrayBuffer
import akka.actor._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.Transition
import akka.event.Logging

case class SubjectInternalMessageProcessed(subjectID: SubjectID)
case class SubjectMessageRouting(from: SubjectID, messageType: MessageType)

object SubjectMessageRouting {
  // TODO passt nur fuer receivestate
  def apply(to: SubjectID, transition: Transition): SubjectMessageRouting =
    SubjectMessageRouting(
      transition.subjectID,
      transition.messageType)
  def apply(sm: SubjectToSubjectMessage): SubjectMessageRouting =
    SubjectMessageRouting(sm.from, sm.messageType)
}

/**
 * Mailbox of SubjectActor (FIFO)
 * capacity can be limited
 */
class InputPoolActor(messageLimit: Int) extends Actor {

  val logger = Logging(context.system, this)

  def receive = {

    // a receive asked before a send
    case sm: SubjectToSubjectMessage if subjectIsWaitingForMessageIn(SubjectMessageRouting(sm)) =>
      sender ! Stored(sm.messageID) // unlock Sender
      logger.debug(self + "Inputpool: Message transported: " + sm.from + ", " +
        sm.messageType + ", \"" +
        sm.messageContent + "\"")
      // transport it to waiting receive message of the internal behavior
      getWaitingSubject(SubjectMessageRouting(sm)) ! sm
      //        TransportMessage(sm.messageID, sm.from, sm.messageType, sm.messageContent)
      context.parent ! SubjectInternalMessageProcessed(sm.to)

    // input pool limit is high enough to store message
    case sm: SubjectToSubjectMessage if messagesStoredFor(SubjectMessageRouting(sm)) < messageLimit =>
      storeMessage(sm)
      logger.debug(self + "Inputpool: Message stored: " + sm.from + ", " +
        sm.messageType + ", \"" +
        sm.messageContent + "\"")
      sender ! Stored(sm.messageID) // unlock Sender
      context.parent ! SubjectInternalMessageProcessed(sm.to)

    case sm: SubjectToSubjectMessage =>
      putInWaitForSend(sm, sender)
      logger.debug(self + "Message putInWaitForSend: " + sm.from + ", " +
        sm.messageType + ", \"" + sm.messageContent + "\"")
      context.parent ! SubjectInternalMessageProcessed(sm.to)

    case RequestForMessages(exitConds) => {
      var break = false

      // TODO nochmal ueberarbeiten
      for (e <- exitConds if break == false) {
        if (tryTransport(e, sender)) {
          break = true
        }
      }

      if (break == false) {
        for (e <- exitConds) {
          putInWaitForMessage(e, sender)
        }
        sender ! InputPoolEmpty
      }
    }

    case RemoveMessageRequests(exitConds) => {
      for (e <- exitConds) {
        exitcond_to_FIFOs(e).popWaiting() // TODO man muss den actor poppen nicht die message?
      }
    }

    case sw => logger.error("Inputpool got message but can't use: " + sw)
  }

  private def tryTransport(smr: SubjectMessageRouting, s: ActorRef): Boolean = {
    if (messageIsWaitingForSendIn(smr)) {
      val (id, actor) = moveMessageToStor(smr)
      actor ! Stored(id) // re lock sender TODO
    }
    if (messagesStoredFor(smr) > 0) {
      val message = popMessageOf(smr)
      logger.debug("Inputpool: e, getMessageContentOf(e) : " + smr + " " + message.messageContent)
      s ! message
      return true
    }
    false
  }

  // TODO exitcond namen ueberarbeiten
  private class FIFO(exitCond: SubjectMessageRouting) {
    // TODO was ist storage, was wait for send ueberpruefen
    private val storage = new ArrayBuffer[SubjectToSubjectMessage]()
    private val waitForSend = new ArrayBuffer[(SubjectToSubjectMessage, ActorRef)]()
    private val waitForMessageStorage = new ArrayBuffer[ActorRef]()

    def put(message: SubjectToSubjectMessage) { storage += message }

    def pop(): SubjectToSubjectMessage = storage.remove(0)

    def popWaiting(): ActorRef = waitForMessageStorage.remove(0)

    def putInWaitForSend(message: SubjectToSubjectMessage, _sender: ActorRef) {
      waitForSend += ((message, _sender))
    }

    def messagesStored = storage.length

    def messageIsWaitingForSend = (waitForSend.length > 0)

    def moveMessageToStor: (MessageID, ActorRef) = {
      val (message, actor) = waitForSend.remove(0)
      storage += message
      (message.messageID, actor)
    }

    def putInWaitForMessage(_sender: ActorRef) {
      waitForMessageStorage += _sender
    }

    def waitForMessage = (waitForMessageStorage.length > 0)

    def getWaitingSubject = waitForMessageStorage.remove(0)
  }

  private val exitcond_to_FIFOs = collection.mutable.Map[SubjectMessageRouting, FIFO]()

  private def messagesStoredFor(exitCond: SubjectMessageRouting): Int = {
    if (exitcond_to_FIFOs.contains(exitCond) == false) {
      return 0
    }
    exitcond_to_FIFOs(exitCond).messagesStored
  }

  private def storeMessage(sm: SubjectToSubjectMessage) {
    val smr = SubjectMessageRouting(sm)
    if (exitcond_to_FIFOs.contains(smr) == false) {
      exitcond_to_FIFOs += smr -> new FIFO(smr)
    }
    exitcond_to_FIFOs(smr).put(sm)
  }

  private def putInWaitForSend(sm: SubjectToSubjectMessage, sender: ActorRef) {
    val smr = SubjectMessageRouting(sm)
    if (exitcond_to_FIFOs.contains(smr) == false) {
      exitcond_to_FIFOs += smr -> new FIFO(smr)
    }
    exitcond_to_FIFOs(smr).putInWaitForSend(sm, sender)
  }

  private def popMessageOf(e: SubjectMessageRouting): SubjectToSubjectMessage =
    exitcond_to_FIFOs(e).pop()

  private def subjectIsWaitingForMessageIn(e: SubjectMessageRouting): Boolean = {
    if (exitcond_to_FIFOs.contains(e) == false) {
      return false
    }
    exitcond_to_FIFOs(e).waitForMessage
  }

  private def messageIsWaitingForSendIn(e: SubjectMessageRouting): Boolean = {
    if (exitcond_to_FIFOs.contains(e) == false) return false
    exitcond_to_FIFOs(e).messageIsWaitingForSend
  }

  private def moveMessageToStor(e: SubjectMessageRouting): (MessageID, ActorRef) =
    exitcond_to_FIFOs(e).moveMessageToStor

  private def putInWaitForMessage(e: SubjectMessageRouting, _sender: ActorRef) {
    if (exitcond_to_FIFOs.contains(e) == false) {
      exitcond_to_FIFOs += e -> new FIFO(e)
    }
    exitcond_to_FIFOs(e).putInWaitForMessage(_sender)
  }

  private def getWaitingSubject(e: SubjectMessageRouting): ActorRef =
    exitcond_to_FIFOs(e).getWaitingSubject
}
