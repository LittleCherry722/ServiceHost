package de.tkip.sbpm.verification.subject

import scala.collection.immutable.Queue
import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import scala.collection.mutable.ListBuffer

object InputPool {
  def empty: InputPool = InputPool(Map(), ClosedChannels())
}

case class ClosedChannels(closed: Set[(SubjectId, MessageType)] = Set()) {
  def open(openIP: OpenIP): ClosedChannels = this

  def close(closeIP: Set[(SubjectId, MessageType)]): ClosedChannels =
    ClosedChannels(closed ++ closeIP)

  def isOpen(content: MessageContent,
             messageType: MessageType,
             channel: Channel): Boolean = {
    !closed.contains((channel.subjectId, messageType))
  }
}

/**
 * the InputPool Stores the messages for the subject
 */
case class InputPool(messages: Map[(MessageType, SubjectId), Queue[Message]],
                     blocked: ClosedChannels) {
  /**
   * Returns whether the InputPool is empty or not
   */
  def isEmpty = messages.isEmpty

  /**
   * Retuns if there is a message from the subject with the messsagetype
   */
  def hasMessage(messageType: MessageType,
                 subjectId: SubjectId) =
    messages contains (messageType, subjectId)

  /**
   * Returns the number of messages stored from the specific subject
   * with the messagetype
   */
  def countMessages(messageType: MessageType,
                    subjectId: SubjectId): Int =
    messages.getOrElse((messageType, subjectId), Queue()).length

  /**
   * Creates a message with the content and the channel and puts it
   * into this inputpool
   */
  def putMessage(content: MessageContent,
                 messageType: MessageType,
                 channel: Channel): InputPool = {
    if (blocked.isOpen(content, messageType, channel)) {
      val key = (messageType, channel.subjectId)

      val queue =
        messages.getOrElse(key, Queue[Message]())
      val newQueue = queue.enqueue(Message(content, channel))

      val newMessages = messages + (key -> newQueue)

      copy(messages = newMessages)
    } else {
      // if the message is blocked, don't store it in the inputpool
      this
    }
  }

  def pullUsers(messageType: MessageType,
                subjectId: SubjectId,
                count: Int): List[AgentId] = {
    if(count == 0) return Nil
    val key = (messageType, subjectId)

    val queue = messages(key)
    assert(
      queue.length >= count,
      "it is not possible to pull more messages than hold, " +
        "pulling %s*(%s, %s)"
        .format(count, messageType, subjectId))

    val listBuffer = ListBuffer[Message]()

    val agents =
      for (i <- 0 until count) yield {
        queue(i).channel.agentId
      }

    agents.toList
  }

  def pullMessage(messageType: MessageType,
                  subjectId: SubjectId,
                  count: Int): (MessageList, InputPool) = {
    val key = (messageType, subjectId)

    val queue = messages(key)

    assert(
      queue.length >= count,
      "it is not possible to pull more messages than hold, " +
        "pulling %s*(%s, %s)"
        .format(count, messageType, subjectId))

    val listBuffer = ListBuffer[Message]()
    var newQueue: Queue[Message] = queue

    for (_ <- 0 until count) {
      val (e, q) = newQueue.dequeue
      listBuffer += e
      newQueue = q
    }

    val newMessages =
      if (newQueue.isEmpty) messages - key
      else messages + (key -> newQueue)

    (MessageList(listBuffer.toList), copy(messages = newMessages))
  }

  def pullMessage(messageType: MessageType,
                  subjectId: SubjectId): (MessageList, InputPool) = {
    pullMessage(messageType, subjectId, 1)
  }

  /**
   * Opens the InputPool
   */
  def open(openIP: OpenIP): InputPool =
    copy(blocked = blocked.open(openIP))

  /**
   * Closes the InputPool
   */
  def close(closeIP: Set[(SubjectId, MessageType)]): InputPool =
    copy(blocked = blocked.close(closeIP))

  def mkString = {
    messages
      .map(m =>
        if (m._2.length == 1) m._1.toString
        else "(%s -> %s)".format(m._1, m._2.length))
      .mkString("{", ", ", "}")
  }
}