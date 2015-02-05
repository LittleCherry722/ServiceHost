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

package de.tkip.sbpm.application.subject.behavior

import scala.collection.mutable.{ Map => MutableMap, Set => MutableSet, MutableList, Queue }
import akka.actor._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.SubjectData
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import akka.event.Logging
import com.typesafe.config.ConfigFactory
import akka.event.LoggingAdapter
import de.tkip.sbpm.instrumentation.InstrumentedActor

protected case class SubscribeIncomingMessages(
  stateID: StateID, // the ID of the receive state
  fromSubject: SubjectID,
  messageType: MessageType) { // the number of messages the state want to receive

  private var stateActor: ActorRef = null

  def setStateActor(sender: ActorRef) {
    if (stateActor == null) stateActor = sender
  }

  def !(message: Any) {
    if (stateActor != null) {
      stateActor ! message
    }
  }
}

// returns the messages the input pool holds from the subject with the messagetype
// returns: Array[SubjectToSubjectMessage]
private[subject] case class GetInputPoolMessage(fromSubject: SubjectID, messageType: MessageType)
// deletes the messages from the InputPool
private[subject] case class DeleteInputPoolMessages(fromSubject: SubjectID, messageType: MessageType, messages: Array[MessageID])

private[subject] case class InputPoolMessagesChanged(subject: SubjectID, messageType: MessageType, messages: Array[SubjectToSubjectMessage])

// message to inform the input pool that the state does not subscribe anything anymore
protected case class UnSubscribeIncomingMessages(stateID: StateID)

// message to inform the receive state that the input pool has no messages for him
protected case object InputPoolSubscriptionPerformed

// message to inform the input pool that it should close the given channel(s)
protected case class CloseInputPool(channelId: ChannelID)

// message to inform the receive state that the input pool close request succeeded
protected case object InputPoolClosed

// message to inform the input pool, that it should open the given channel(s)
protected case class OpenInputPool(channelId: ChannelID)

// message to inform the receive state, that the input pool open request succeeded
protected case object InputPoolOpened

// message to ask the input pool whether it is empty for the given channel ID
protected case class IsIPEmpty(channelId: ChannelID)

// message to tell the receive state whether the input pool is empty
protected case class IPEmpty(empty: Boolean)

// message to tell the blocked send state that the state is reopened
protected case object Reopen

class InputPoolActor(data: SubjectData) extends InstrumentedActor with ActorLogging {
  // extract the information from the data
  val userID = data.userID
  val messageLimit = data.subject.inputPool
  val blockingHandlerActor = data.blockingHandlerActor

  // this map holds the queue of the income messages for a channel
  private val messageQueueMap =
    MutableMap[ChannelID, Queue[SubjectToSubjectMessage]]()
  // this map holds the overflow queue of the income messages for a channel
  private val messageOverflowQueueMap =
    MutableMap[ChannelID, Queue[SubjectToSubjectMessage]]()
  // this map holds the states which are subscribing a channel
  private val waitingStatesMap =
    //  MutableMap[ChannelID, WaitingStateList]()
    MutableMap[ChannelID, WaitingStateSet]()
  //this map stores the send states which are blocked
  private val blockedSendStatesMap : MutableMap[ChannelID, ActorRef] = 
    MutableMap[ChannelID, ActorRef]()
    
  private val closedChannels = new ClosedChannels()

  def wrappedReceive = {

    //    case TryTransportMessages => {
    //      for ((key, queue) <- this.messageQueueMap) {
    //        for (message <- queue) {
    //          tryTransportMessage(message)
    //        }
    //      }
    //    }

    //    case SubjectToSubjectMessageReceived(sm) => {
    //      val channel = (sm.from, sm.messageType)
    //      val queue = this.messageQueueMap.get(channel)
    //      val newQueue = queue.filterNot(_ == sm).asInstanceOf[Queue[SubjectToSubjectMessage]]
    //      messageQueueMap.put((sm.from, sm.messageType), newQueue)
    //    }

    case registerAll: Array[SubscribeIncomingMessages] => {
      handleSubscribers(registerAll)
    }

    case register: SubscribeIncomingMessages => {
      handleSubscribers(Array(register))
    }

    case UnSubscribeIncomingMessages(stateID) => {
      // unregister the waiting states
      // TODO increase performance
      //      waitingStatesMap.map(_._2.remove(stateID))
      waitingStatesMap.map(_._2.remove(stateID))
    }

    
    
    //this case will be executed, if the inputpoo receives a disabled message and the input pool is not full.
    //the incomming message will be stored and a reply (stored message) will be sent back to the sender
    case message: SubjectToSubjectMessage if (spaceAvailableInMessageQueue(message) && !message.enabled) => {
      //store reservation message
      log.debug("InputPool received disabled message from " + sender + " which message.messageID = " +message.messageID)
      //send stored notification back to sender
      sender !! Stored(message.messageID)
      // store the reservation
      enqueueMessage(message)
      log.debug("reservation is done!")
    }
    
    //this case will be executed, if a reable-request is received
    //of so, the message which has to be enabled will be searched in the qeueu and will be enabled responding with a enabled-message
    //if there is no message to enable, the response will be a rejected message
    case message: SubjectToSubjectMessage if (message.enabled) => {
      log.debug("InputPool received enable request from " + sender)
      //replace reservation with real message
      
      if(enableMessage(message)){
    	  //send enabled notification back to sender
    	  sender !! Enabled(message.messageID)
    	  log.debug("Message enabled")
      }else{
        //no reservation found for thei message! Send reject message
        log.warning("message rejected, no message to enable: {}", message)
        sender !! Rejected(message.messageID)
      }
    }
    
    //this case will be executed, if a disabled message was received and the input queue is full
    //the message will be stored in the overflow queue and a oferflow-message will be sent to the sender
    case message: SubjectToSubjectMessage if (!spaceAvailableInMessageQueue(message) && !message.enabled) => {
      //store reservation message
      log.debug("InputPool received reservation from " + sender + " -> but message queue is full! Save sender id and reject reservation")
      //send stored notification back to sender
      sender !! Overflow(message.messageID)
      // store the sender informations
      
      //put message into the overflowQueue
      enqueueOverflowMessage(message)
      
    }

  
 
    
    case message: SubjectToSubjectMessage if closedChannels.isChannelClosedAndNotReOpened((message.from, message.messageType)) => {
      // Unlock the sender
      sender !! Rejected(message.messageID)
      val channelID = new ChannelID(message.from, message.messageType)
      blockedSendStatesMap(channelID) = sender

      log.warning("message rejected: {}", message)
      // unblock this user
      blockingHandlerActor ! UnBlockUser(userID)
    }

    /*
    case message: SubjectToSubjectMessage => {
      log.debug("InputPool received: " + message + " from " + sender)
      // Unlock the sender
      sender !! Stored(message.messageID)
      // store the message
      enqueueMessage(message)
      log.debug("Inputpool has: " +
        getMessageArray(message.from, message.messageType).mkString("{", ", ", "}"))
      // inform the states about this change
      broadcastChangeFor((message.from, message.messageType))
      // unblock this user
      blockingHandlerActor ! UnBlockUser(userID)
    }
*/
    case DeleteInputPoolMessages(fromSubject, messageType, messages) => {
      val result =
        dequeueMessages((fromSubject, messageType), messages)
      if (result)
      {
        // take out message from overflow queue and put it in main inputpool
        enqueueMessage(Overflow(message.messageID))
        log.debug("Message from Overflow moved to InputPool!")
      }
      if (!result) {
        // TODO error, delete failed
      }
      broadcastChangeFor((fromSubject, messageType))
    }

    case CloseInputPool(channelId) => {
      closedChannels.close(channelId)
      sender !! InputPoolClosed
    }

    case OpenInputPool(channelId) => {
      closedChannels.open(channelId)
      closedChannels.reopen(channelId, blockedSendStatesMap)
      sender !! InputPoolOpened
    }

    case IsIPEmpty((subjectId, messageType)) => {
      if (subjectId == ProcessAttributes.AllSubjects || messageType == ProcessAttributes.AllMessages) {
        val filtered = filterQueueMap(subjectId, messageType)
        val isEmpty = (filtered.values map (_.isEmpty)).foldLeft(true)(_ && _)
        sender !! IPEmpty(isEmpty)
      } // single subject, single message type
      else {
        val msg = IPEmpty(messageQueueIsEmpty(subjectId, messageType))
        sender !! msg
      }

    }
  }

  /**
   * Filters all messages out of the queue map that don't belong to the given channel ID
   */
  private def filterQueueMap(channelId: ChannelID) = {
    val (subjectId, messageType) = channelId

    // 'all subjects' and 'all message types'
    if (subjectId == ProcessAttributes.AllSubjects && messageType == ProcessAttributes.AllMessages) {
      messageQueueMap
    } // 'all subjects'
    else if (subjectId == ProcessAttributes.AllSubjects) {
      messageQueueMap filterKeys (_._2 == messageType)
    } // 'all message types'
    else {
      messageQueueMap filterKeys (_._1 == subjectId)
    }
  }

  /**
   * Handles the subscription for one or more messages, which means:
   * - Set all actors to the sender in the class instances
   * - try to transport all messages to the requesting state
   * - inform the sender, that the subscription has been performed
   */
  private def handleSubscribers(registerAll: Array[SubscribeIncomingMessages]) {
    // set all state actors to the sender
    registerAll.map(_.setStateActor(sender))

    for (register <- registerAll) {
      // try to transport all messages
      sendChangeTo(register)
      getWaitingStatesSet(register.fromSubject, register.messageType).add(register)
    }

    // inform the sender, that this subscription has been performed
    sender !! InputPoolSubscriptionPerformed
  }

  private def sendChangeTo(state: SubscribeIncomingMessages) {
    val messages = getMessageArray(state.fromSubject, state.messageType)
    val msg = InputPoolMessagesChanged(state.fromSubject, state.messageType, messages)
    state ! msg
  }

  private def broadcastChangeFor(channelID: ChannelID) {
    val messages = getMessageArray(channelID._1, channelID._2)
    getWaitingStatesSet(channelID).sendToAll(InputPoolMessagesChanged(channelID._1, channelID._2, messages))
  }

  /**
   * Tries to transport the messages, which are already in the pool
   * to the state described by the input
   * Will also register the state as waiting in the map, if needed
   * //
   */
  //  private def tryTransportMessagesTo(state: SubscribeIncomingMessages) {
  //    val key = (state.fromSubject, state.messageType)
  //    // while it is needed and it is possible, send the message to the request state
  //    while (state.count > 0 && !messageQueueIsEmpty(key)) {
  //      // get the message
  //      val message = dequeueMessage(key)
  //      // transport the message
  //      state ! message
  //    }
  //
  //    // if its still needed, register the state into the waiting list
  //    if (state.count > 0) {
  //      getWaitingStatesList(key).add(state)
  //    }
  //  }
  //
  //  /**
  //   * Tries to transport the message to the waiting state.
  //   * Stores the message in the pool, if no state is waiting for the message,
  //   */
  //  private def tryTransportMessage(message: SubjectToSubjectMessage) {
  //    val state =
  //      getWaitingStatesList((message.from, message.messageType)).get
  //    if (state != null) {
  //      state ! message
  //    } else {
  //      enqueueMessage(message)
  //    }
  //  }

  /**
   * Returns the WaitingStateList for the key
   * Creates and returns the list, if it does not exists
   */
  private def getWaitingStatesSet(key: (SubjectID, MessageType)) =
    waitingStatesMap.getOrElseUpdate(key, new WaitingStateSet(log))

  private def getMessageArray(subjectID: SubjectID, messageType: MessageType): Array[SubjectToSubjectMessage] =
    messageQueueMap.getOrElse((subjectID, messageType), Queue[SubjectToSubjectMessage]()).toArray

  /**
   * returns true or false if for the condition messageQueue.size < messageLimit
   * return true if messageLimit is -1 (no limit set)
   */
  private def spaceAvailableInMessageQueue(message: SubjectToSubjectMessage) : Boolean = {
     log.debug("Checking for queue space!")
    
    // get or create the message queue
    val messageQueue =
      messageQueueMap.getOrElseUpdate(
        (message.from, message.messageType),
        Queue[SubjectToSubjectMessage]())

    // check the queue size
    if (messageQueue.size < messageLimit || messageLimit == -1) {
      true
    } else {
      false
    }
  }
  
  /**
   * returns true or false if for the condition messageQueue.size < messageLimit
   */
  private def containsReservationForMessage(message: SubjectToSubjectMessage) : Boolean = {
    // get or create the message queue
    val messageQueue =
      messageQueueMap.getOrElseUpdate(
        (message.from, message.messageType),
        Queue[SubjectToSubjectMessage]())

    // check if the queue contains a reservation
    if (messageQueue.size < messageLimit) {
      true
    } else {
      false
    }
  }
    
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
    //if (messageQueue.size < messageLimit) {
      messageQueue.enqueue(message)
      log.debug("message has be queued!")
    //} else {
      // TODO log error?
    //}
  }
  
  /**
   * Enqueue a message in the overflow queue, 
   */
  private def enqueueOverflowMessage(message: SubjectToSubjectMessage) = {
    // get or create the message queue
    val messageOverflowQueue =
      messageOverflowQueueMap.getOrElseUpdate(
        (message.from, message.messageType),
        Queue[SubjectToSubjectMessage]())

      messageOverflowQueue.enqueue(message)
      log.debug("message has been queued to overflow queue!")
  }
  

  /**
   * enables previously received message
   */
  private def enableMessage(message: SubjectToSubjectMessage) : Boolean = {
    // get or create the message queue
    var messageQueue =
      messageQueueMap.getOrElseUpdate(
        (message.from, message.messageType),
        Queue[SubjectToSubjectMessage]())

        //loop over queue and enable message if found
        var counter = 0
    	for (element <- messageQueue){
    	  if(element.messageID == message.messageID && !element.enabled){
    	    element.enabled = true
    	    counter = counter + 1
    	  }
    	}
      
    	//find message in queue
    	//var messageQueueFound = messageQueue.filterNot(a => a.messageID == message.messageID && a.enabled)
    	
    	//create temp queue with filter
    	//messageQueue = messageQueue.filterNot(a => a.messageID == message.messageID && !a.enabled)
		//append new message at the end of the queue

    	
    	
	if(counter != 0){ 
    	true
    }else{
      false
      //throw new exception
    }
    
    
  }
  
  private def dequeueMessages(key: (SubjectID, MessageType), messages: Array[MessageID]): Boolean = {
    if (messages forall (id => messageQueueMap(key).exists(_.messageID == id))) {
      // TODO might increase performance
      messages foreach (id => messageQueueMap(key).dequeueAll(_.messageID == id))
      true
    } else {
      false
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
 * The same state cannot register twice (adding will remove old registration)
 * /Currently only one state will be hold in this list, but will be usefull for modal split
 */
private class WaitingStateSet(log: LoggingAdapter) {
  val states = MutableSet[SubscribeIncomingMessages]()

  def add(state: SubscribeIncomingMessages) {
    // a state can not register twice
    remove(state.stateID)
    // enqueue the state at the back of the queue
    states += state
  }

  def sendToAll(message: Any) {
    for (state <- states) {
      state ! message
    }
  }

  def remove(id: StateID) {
    states --= states.filter(_.stateID == id)
  }
}

/**
 * This class keeps track of all closed channels.
 */
private[behavior] class ClosedChannels {

  private object RuleType extends Enumeration {
    type RuleType = Value
    val Close, Open = Value
  }

  import RuleType._

  private case class Rule(channelId: ChannelID, ruleType: RuleType)

  private var rules = List[Rule]()

  private def removeOldRules(channelId: ChannelID) {
    rules = rules.filter(_.channelId != channelId)
  }

  def close(channelId: ChannelID) {
    removeOldRules(channelId)
    rules = Rule(channelId, Close) :: rules  
  }

  def open(channelId: ChannelID) {
    removeOldRules(channelId)
    rules = Rule(channelId, Open) :: rules
  }
  
  def reopen(channelId: ChannelID, blockedSendStatesMap: MutableMap[ChannelID, ActorRef]){     
    if(blockedSendStatesMap.contains(channelId)){
      blockedSendStatesMap.get(channelId).get ! Reopen
      blockedSendStatesMap.remove(channelId)
    }
  }

  def isChannelClosedAndNotReOpened(channelId: ChannelID): Boolean = {
    def channelFilter(rule: Rule) = (rule.channelId._1 == channelId._1 || rule.channelId._1 == AllSubjects) &&
        (rule.channelId._2 == channelId._2 || rule.channelId._2 == AllMessages)
    val rule = rules.find(channelFilter)
    !rule.map(_.ruleType == Open).getOrElse(!rule.map(_.ruleType == Close).getOrElse(false))
  }
}
