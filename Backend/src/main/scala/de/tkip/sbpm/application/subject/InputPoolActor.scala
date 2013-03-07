package de.tkip.sbpm.application.subject

import scala.collection.mutable.{ ArrayBuffer, Map => MutableMap }
import akka.actor._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.Transition
import akka.event.Logging
import scala.collection.mutable.Queue

case class SubjectInternalMessageProcessed(subjectID: SubjectID)
// TODO in eine message buendeln
protected case class SubscribeIncomingMessages(
  stateID: StateID, // the ID of the receive state
  fromSubject: SubjectID,
  messageType: MessageType,
  var count: Int = 1) { // the number of messages the state want to receive

  private var _stateActor: ActorRef = null
  def stateActor = _stateActor
  def stateActor_=(sender: ActorRef) {
    if (_stateActor == null) _stateActor = sender
  }
}
protected case class UnSubscribeIncomingMessages(stateID: StateID)

class InputPoolActor(private val messageLimit: Int) extends Actor {

  // this map holds the queue of the income messages for a channel
  private val messageQueueMap =
    MutableMap[(SubjectID, MessageType), Queue[SubjectToSubjectMessage]]()
  // this map holds the states which are subscribing a channel
  private val waitingStatesMap =
    MutableMap[(SubjectID, MessageType), WaitingStateList]()

  def receive = {

    case register: SubscribeIncomingMessages => {
      // Set the state actor in the request
      register.stateActor = sender
      // try to transport the messages to the state
      tryTransportMessagesTo(register)
    }

    case UnSubscribeIncomingMessages(stateID) => {
      // unregister the waiting states
      // TODO increase performance
      waitingStatesMap.map(_._2.remove(stateID))
    }

    case message: SubjectToSubjectMessage => {
      // Unlock the sender
      sender ! Stored(message.messageID)
      // try to transport the message
      tryTransportMessage(message)
      // inform the processinstance, that this message has been processed
      context.parent ! SubjectInternalMessageProcessed(message.to)
    }
  }

  /**
   * Tries to transport the messages, which are already in the pool
   * to the state described by the input
   * Will also register the state as waiting in the map, if needed
   */
  private def tryTransportMessagesTo(sub: SubscribeIncomingMessages) {
    val key = (sub.fromSubject, sub.messageType)
    while (sub.count > 0 && !messageQueueIsEmpty(key)) {
      // get the message
      val message = dequeueMessage(key)
      // transport the message
      sub.stateActor ! message
      // decrease the remaining message count
      sub.count -= 1
    }

    // if its still needed, register the state into the waiting list
    if (sub.count > 0) {
      getWaitingStatesList(key).add(sub)
    }
  }

  /**
   * Tries to transport the message to the waiting state.
   * Stores the message in the pool, if no state is waiting for the message,
   */
  private def tryTransportMessage(message: SubjectToSubjectMessage) {
    val state =
      getWaitingStatesList((message.from, message.messageType)).get
    if (state != null) {
      state.stateActor ! message
      state.count -= 1
    } else {
      enqueueMessage(message)
    }
  }

  /**
   * Returns the WaitingStateList for the key
   * Creates and returns the list, if it does not exists
   */
  private def getWaitingStatesList(key: (SubjectID, MessageType)) =
    waitingStatesMap.getOrElseUpdate(key, new WaitingStateList())

  /**
   * Enqueue a message, add it to the correct queue
   */
  private def enqueueMessage(message: SubjectToSubjectMessage) = {
    // get or create the message queue
    val messageQueue =
      messageQueueMap.getOrElseUpdate(
        (message.from, message.messageType),
        Queue[SubjectToSubjectMessage]())

    // if the queue is not to big, enqueue the message
    if (messageQueue.size < messageLimit) {
      messageQueue.enqueue(message)
    } else {
      // TODO log error?
    }
  }

  /**
   * Dequeue a message, remove and return the first message
   * TODO sonst null?, oder muss man vorher abfragen
   */
  private def dequeueMessage(key: (SubjectID, MessageType)) =
    messageQueueMap(key).dequeue()

  /**
   * Returns if the message queue for the key is empty.
   * (A not existing queue is seen as empty)
   */
  private def messageQueueIsEmpty(key: (SubjectID, MessageType)) =
    !messageQueueMap.contains(key) || messageQueueMap(key).isEmpty

}

/**
 * This list is responsible to hold and manage the ordering for the waiting
 * states of a MessageChannel
 * /Currently only one state will be hold in this list, but will be usefull for modal split
 */
private class WaitingStateList {
  private var queue = Queue[SubscribeIncomingMessages]()

  def add(state: SubscribeIncomingMessages) {
    // TODO might check if this state is listening and remove the old one
    queue.dequeueAll(_ == state) // TODO this removes the contained equal states, improve!
    queue.enqueue(state)
  }

  def get = {
    // remove disused
    removeDisused()
    // return the first element
    if (queue.isEmpty) null else queue.head
  }

  def isEmpty = {
    removeDisused()
    queue.isEmpty
  }

  def remove(id: StateID) {
    queue = queue.filterNot(_.stateID == id)
  }

  private def removeDisused() {
    queue = queue.filter(_.count > 0)
  }
}

/*
case class SubjectMessageRouting(from: SubjectID, messageType: MessageType)

object SubjectMessageRouting {
  // TODO passt nur fuer receivestate
  def apply(to: SubjectID, transition: Transition): SubjectMessageRouting =
    SubjectMessageRouting(transition.subjectID, transition.messageType)

  def apply(sm: SubjectToSubjectMessage): SubjectMessageRouting =
    SubjectMessageRouting(sm.from, sm.messageType)
}

/**
 * Mailbox of SubjectActor (FIFO)
 * capacity can be limited
 */
class YInputPoolActor(messageLimit: Int) extends Actor {

  val logger = Logging(context.system, this)

  def receive = {

    // a receive asked before a send
    case sm: SubjectToSubjectMessage if subjectIsWaitingForMessageIn(SubjectMessageRouting(sm)) =>
      sender ! Stored(sm.messageID) // unlock Sender
      logger.debug(self + "Inputpool: Message transported: " + sm.from + ", " +
        sm.messageType + ", \"" + sm.messageContent + "\"")
      // transport it to waiting receive message of the internal behavior
      // TODO just test multisubj
      //      while(subjectIsWaitingForMessageIn(SubjectMessageRouting(sm))) 
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

    def getWaitingSubject = {
      waitForMessageStorage.remove(0)
    }
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
*/