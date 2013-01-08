package de.tkip.sbpm.application

import akka.actor._
import scala.collection.mutable.ArrayBuffer
import miscellaneous._
import miscellaneous.ProcessAttributes._

/**
 * Mailbox of SubjectActor (FIFO)
 * capacity can be limited
 */
class InputPoolActor(messageLimit: Int) extends Actor {

  def receive = {

    // a receive asked before a send
    case SubjectMessage(fromCond, _, messageContent) if subjectIsWaitingForMessageIn(fromCond) =>
      sender ! Stored // unlock Sender
      println(self + "Inputpool: Message transported: " + fromCond + ", \"" +
        messageContent + "\"")
      // transport it to waiting receive message of the internal behavior
      getWaitingSubject(fromCond) ! TransportMessage(fromCond, messageContent)

    case SubjectMessage(fromCond, _, messageContent) if messagesStoredFor(fromCond) < messageLimit =>
      storeMessageContent(fromCond, messageContent)
      println(self + "Inputpool: Message stored: " + fromCond + ", \"" +
        messageContent + "\"")
      sender ! Stored // unlock Sender

    case SubjectMessage(fromCond, _, messageContent) =>
      putInWaitForSend(fromCond, messageContent, sender)
      println(self + "Message putInWaitForSend: " + fromCond + ", \"" +
        messageContent + "\"")

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

    case sw => println("Inputpool hat sonst was erhalten " + sw.toString)
  }

  private def tryTransport(e: ExitCond, s: ActorRef): Boolean = {
    if (messageIsWaitingForSendIn(e)) {
      moveMessageToStor(e) ! Stored // re lock sender
    }
    if (messagesStoredFor(e) > 0) {
      val content = getMessageContentOf(e)
      println("Inputpool: e, getMessageContentOf(e) : " + e + " " + content)
      s ! TransportMessage(e, content)
      return true
    }
    false
  }

  private class FIFO(exitCond: ExitCond) {
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

  private val exitcond_to_FIFOs = collection.mutable.Map[ExitCond, FIFO]()

  private def messagesStoredFor(exitCond: ExitCond): Int = {
    if (exitcond_to_FIFOs.contains(exitCond) == false) {
      return 0
    }
    exitcond_to_FIFOs(exitCond).messagesStored
  }

  private def storeMessageContent(e: ExitCond, m: MessageContent) {
    if (exitcond_to_FIFOs.contains(e) == false) {
      exitcond_to_FIFOs += e -> new FIFO(e)
    }
    exitcond_to_FIFOs(e).put(m)
  }

  private def putInWaitForSend(e: ExitCond, m: MessageContent,
                               sender: ActorRef) {
    if (exitcond_to_FIFOs.contains(e) == false) {
      exitcond_to_FIFOs += e -> new FIFO(e)
    }
    exitcond_to_FIFOs(e).putInWaitForSend(m, sender)
  }

  private def getMessageContentOf(e: ExitCond): MessageContent =
    exitcond_to_FIFOs(e).get()

  private def subjectIsWaitingForMessageIn(e: ExitCond): Boolean = {
    if (exitcond_to_FIFOs.contains(e) == false) return false
    exitcond_to_FIFOs(e).waitForMessage
  }
  private def messageIsWaitingForSendIn(e: ExitCond): Boolean = {
    if (exitcond_to_FIFOs.contains(e) == false) return false
    exitcond_to_FIFOs(e).messageIsWaitingForSend
  }

  private def moveMessageToStor(e: ExitCond): ActorRef =
    exitcond_to_FIFOs(e).moveMessageToStor

  private def putInWaitForMessage(e: ExitCond, _sender: ActorRef) {
    if (exitcond_to_FIFOs.contains(e) == false) {
      exitcond_to_FIFOs += e -> new FIFO(e)
    }
    exitcond_to_FIFOs(e).putInWaitForMessage(_sender)
  }

  private def getWaitingSubject(e: ExitCond): ActorRef =
    exitcond_to_FIFOs(e).getWaitingSubject

} // class Inputpool