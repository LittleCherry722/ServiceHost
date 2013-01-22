package de.tkip.sbpm.application

import akka.actor._
import scala.collection.mutable.ArrayBuffer
import miscellaneous._
import miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.Transition

case class SubjectMessageRouting(from: SubjectName, messageType: MessageType)

object SubjectMessageRouting {
  // TODO passt nur fuer receivestate
  def apply(to: SubjectName, transition: Transition): SubjectMessageRouting =
    SubjectMessageRouting(transition.subjectName,
      transition.messageType)
  def apply(sm: SubjectMessage): SubjectMessageRouting =
    SubjectMessageRouting(sm.from, sm.messageType)
}

/**
 * Mailbox of SubjectActor (FIFO)
 * capacity can be limited
 */
class InputPoolActor(messageLimit: Int) extends Actor {

  def receive = {

    // a receive asked before a send
    case sm: SubjectMessage if subjectIsWaitingForMessageIn(SubjectMessageRouting(sm)) =>
      sender ! Stored // unlock Sender
      println(self + "Inputpool: Message transported: " + sm.from + ", " +
        sm.messageType + ", \"" +
        sm.messageContent + "\"")
      // transport it to waiting receive message of the internal behavior
      getWaitingSubject(SubjectMessageRouting(sm)) !
        TransportMessage(sm.from, sm.messageType, sm.messageContent)

    // input pool limit is high enough to store message
    case sm: SubjectMessage if messagesStoredFor(SubjectMessageRouting(sm)) < messageLimit =>
      storeMessageContent(sm)
      println(self + "Inputpool: Message stored: " + sm.from + ", " +
        sm.messageType + ", \"" +
        sm.messageContent + "\"")
      sender ! Stored // unlock Sender

    case sm: SubjectMessage =>
      putInWaitForSend(sm, sender)
      println(self + "Message putInWaitForSend: " + sm.from + ", " +
        sm.messageType + ", \"" + sm.messageContent + "\"")

    case RequestForMessages(exitConds) =>
      var break = false

      for (e <- exitConds if break == false) {
        if (tryTransport(e, sender)) {
          break = true
        }
      }

      if (break == false) {
        for (e <- exitConds) {
          putInWaitForMessage(e, sender)
        }
      }

    case sw => println("Inputpool hat sonst was erhalten " + sw)
  }

  private def tryTransport(smr: SubjectMessageRouting, s: ActorRef): Boolean = {
    if (messageIsWaitingForSendIn(smr)) {
      moveMessageToStor(smr) ! Stored // re lock sender
    }
    if (messagesStoredFor(smr) > 0) {
      val content = getMessageContentOf(smr)
      println("Inputpool: e, getMessageContentOf(e) : " + smr + " " + content)
      s ! TransportMessage(smr.from, smr.messageType, content)
      return true
    }
    false
  }

  // TODO exitcond namen ueberarbeiten
  private class FIFO(exitCond: SubjectMessageRouting) {
    private val storrage = new ArrayBuffer[MessageContent]()
    private val waitForSend = new ArrayBuffer[(MessageContent, ActorRef)]()
    private val waitForMessageStorrage = new ArrayBuffer[ActorRef]()

    def put(content: MessageContent) { storrage += content }

    def get(): MessageContent = storrage.remove(0)

    def putInWaitForSend(m: MessageContent, _sender: ActorRef) {
      waitForSend += ((m, _sender))
    }

    def messagesStored = storrage.length

    def messageIsWaitingForSend = (waitForSend.length > 0)

    def moveMessageToStor: ActorRef = {
      storrage += waitForSend(0)._1
      waitForSend.remove(0)._2
    }

    def putInWaitForMessage(_sender: ActorRef) {
      waitForMessageStorrage += _sender
    }

    def waitForMessage = (waitForMessageStorrage.length > 0)

    def getWaitingSubject = waitForMessageStorrage.remove(0)
  } // Class FIFO

  private val exitcond_to_FIFOs = collection.mutable.Map[SubjectMessageRouting, FIFO]()

  private def messagesStoredFor(exitCond: SubjectMessageRouting): Int = {
    if (exitcond_to_FIFOs.contains(exitCond) == false) {
      return 0
    }
    exitcond_to_FIFOs(exitCond).messagesStored
  }

  private def storeMessageContent(sm: SubjectMessage) {
    val smr = SubjectMessageRouting(sm)
    if (exitcond_to_FIFOs.contains(smr) == false) {
      exitcond_to_FIFOs += smr -> new FIFO(smr)
    }
    exitcond_to_FIFOs(smr).put(sm.messageContent)
  }

  private def putInWaitForSend(sm: SubjectMessage,
                               sender: ActorRef) {
    val smr = SubjectMessageRouting(sm)
    if (exitcond_to_FIFOs.contains(smr) == false) {
      exitcond_to_FIFOs += smr -> new FIFO(smr)
    }
    exitcond_to_FIFOs(smr).putInWaitForSend(sm.messageContent, sender)
  }

  private def getMessageContentOf(e: SubjectMessageRouting): MessageContent =
    exitcond_to_FIFOs(e).get()

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

  private def moveMessageToStor(e: SubjectMessageRouting): ActorRef =
    exitcond_to_FIFOs(e).moveMessageToStor

  private def putInWaitForMessage(e: SubjectMessageRouting, _sender: ActorRef) {
    if (exitcond_to_FIFOs.contains(e) == false) {
      exitcond_to_FIFOs += e -> new FIFO(e)
    }
    exitcond_to_FIFOs(e).putInWaitForMessage(_sender)
  }

  private def getWaitingSubject(e: SubjectMessageRouting): ActorRef =
    exitcond_to_FIFOs(e).getWaitingSubject

} // class Inputpool