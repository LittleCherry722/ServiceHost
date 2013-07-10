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

import scala.collection.mutable.{Map => MutableMap, Set => MutableSet, MutableList, Queue}
import akka.actor._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.SubjectData
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.application.subject.misc.TryTransportMessages
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessageReceived
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage

protected case class SubscribeIncomingMessages(
  stateID: StateID, // the ID of the receive state
  fromSubject: SubjectID,
  messageType: MessageType,
  private var remainingCount: Int = 1) { // the number of messages the state want to receive

  private var stateActor: ActorRef = null

  def count = remainingCount

  def setStateActor(sender: ActorRef) {
    if (stateActor == null) stateActor = sender
  }

  def !(message: Any) {
    if (stateActor != null) {
      stateActor ! message
      remainingCount -= 1
    }
  }
}

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

class InputPoolActor(data: SubjectData) extends Actor with ActorLogging {
  // extract the information from the data
  val userID = data.userID
  val messageLimit = data.subject.inputPool
  val blockingHandlerActor = data.blockingHandlerActor

  // this map holds the queue of the income messages for a channel
  private val messageQueueMap =
    MutableMap[ChannelID, Queue[SubjectToSubjectMessage]]()
  // this map holds the states which are subscribing a channel
  private val waitingStatesMap =
    MutableMap[ChannelID, WaitingStateList]()

  private val closedChannels = new ClosedChannels()

  def receive = {

    case TryTransportMessages => {
      for ((key, queue) <- this.messageQueueMap) {
        for (message <- queue) {
          this.tryTransportMessage(message);
        }
      }
    }

    case SubjectToSubjectMessageReceived(sm) => {
      val queue = this.messageQueueMap.get((sm.from, sm.messageType))
      val newQueue = queue.filterNot(_ == sm).asInstanceOf[Queue[SubjectToSubjectMessage]]
      this.messageQueueMap.put((sm.from, sm.messageType), newQueue)
    }

    case registerAll: Array[SubscribeIncomingMessages] => {
      handleSubscribers(registerAll)
    }

    case register: SubscribeIncomingMessages => {
      handleSubscribers(Array(register))
    }

    case UnSubscribeIncomingMessages(stateID) => {
      // unregister the waiting states
      // TODO increase performance
      waitingStatesMap.map(_._2.remove(stateID))
    }

    case message: SubjectToSubjectMessage if closedChannels.isChannelClosed((message.from, message.messageType)) => {
      // Unlock the sender
      sender ! Rejected(message.messageID)

      log.warning("message rejected: {}", message)

      // unblock this user
      blockingHandlerActor ! UnBlockUser(userID)
    }

    case message: SubjectToSubjectMessage => {
      // Unlock the sender
      sender ! Stored(message.messageID)
      // try to transport the message
      tryTransportMessage(message)
      // unblock this user
      blockingHandlerActor ! UnBlockUser(userID)
    }

    case CloseInputPool(channelId) => {
      closedChannels.close(channelId)
      sender ! InputPoolClosed
    }

    case OpenInputPool(channelId) => {
      closedChannels.open(channelId)
      sender ! InputPoolOpened
    }

    case IsIPEmpty((subjectId, messageType)) => {
      if (subjectId == ProcessAttributes.AllSubjects || messageType == ProcessAttributes.AllMessages) {
        val filtered = filterQueueMap(subjectId, messageType)
        val isEmpty = (filtered.values map (_.isEmpty)).foldLeft(true)(_ && _)

        sender ! IPEmpty(isEmpty)
      } // single subject, single message type
      else {
        sender ! IPEmpty(messageQueueIsEmpty(subjectId, messageType))
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
      tryTransportMessagesTo(register)
    }

    // inform the sender, that this subscription has been performed
    sender ! InputPoolSubscriptionPerformed
  }

  /**
   * Tries to transport the messages, which are already in the pool
   * to the state described by the input
   * Will also register the state as waiting in the map, if needed
   */
  private def tryTransportMessagesTo(state: SubscribeIncomingMessages) {
    val key = (state.fromSubject, state.messageType)
    // while it is needed and it is possible, send the message to the request state
    while (state.count > 0 && !messageQueueIsEmpty(key)) {
      // get the message
      val message = dequeueMessage(key)
      // transport the message
      state ! message
    }

    // if its still needed, register the state into the waiting list
    if (state.count > 0) {
      getWaitingStatesList(key).add(state)
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
      state ! message
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
 * The same state cannot register twice (adding will remove old registration)
 * /Currently only one state will be hold in this list, but will be usefull for modal split
 */
private class WaitingStateList {
  private val queue = Queue[SubscribeIncomingMessages]()

  def add(state: SubscribeIncomingMessages) {
    // a state can not register twice
    remove(state.stateID)
    // enqueue the state at the back of the queue
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
    queue.dequeueAll(_.stateID == id)
  }

  private def removeDisused() {
    queue.dequeueAll(_.count <= 0)
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
    rules = Rule(channelId, Close)  :: rules
  }

  def open(channelId: ChannelID) {
    removeOldRules(channelId)
    rules = Rule(channelId, Open) :: rules
  }

  def isChannelClosed(channelId: ChannelID) :Boolean = {
    def channelFilter(rule: Rule) = (rule.channelId._1 == channelId._1 || rule.channelId._1 == AllSubjects) &&
      (rule.channelId._2 == channelId._2 || rule.channelId._2 == AllMessages)

    val rule = rules.find(channelFilter)
    rule.map(_.ruleType == Close).getOrElse(false)
  }
}
