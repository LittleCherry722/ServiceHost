package de.tkip.sbpm.newmodel

import ProcessModelTypes._

// Variables:
// CorrelationID speichern
// MessageContent speichern
// ChannelSet speichern

// StoreToVar beim send moeglich
// SubjectInstanceId: SubjectId, SubjectProviderId, CorrelationId
// Channel = SubjectInstanceId
// Var = MessageSet: MessageContent, Set[SubjectInstanceId]

// Send: MessageContent, SubjectInstanceId, MessageType, Set[SubjectInstanceId] -> MessageList
// Receive: Set[SubjectInstanceId], MessageType, SubjectInstanceId -> MessageList
// MessageContent: Text | Vars

// classes which inherit this trait can be sent as the content of a message
trait MessageContent

// a Message contains the MessageContent and the related subject instance
case class Message(content: MessageContent, channel: Channel)
case class MessageList(messages: List[Message]) extends MessageContent {
  def channels: Set[Channel] = messages map (_.channel) toSet

  // concatenation
  def ++(other: MessageList) = MessageList(messages ++ other.messages distinct)

  /**
   * Extract the message lists from the message content, so a variable can
   * be assigned to this message content
   */
  def extractMessageContent: MessageList =
    messages.map(_.content)
      .collect({ case ml: MessageList => ml })
      .foldLeft(MessageList(Nil))(_ ++ _)
}

// several messagecontents are possible
case object NoContent extends MessageContent
case class TextContent(content: String) extends MessageContent
case class FileContent(content: Array[Byte], fileType: String) extends MessageContent
