package de.tkip.sbpm.application.miscellaneous

import ProcessAttributes._
import de.tkip.sbpm.application.SubjectMessageRouting

sealed trait MessageObject
//case class SubjectMessage(fromCond: ExitCond, toCond: ExitCond, messageContent: MessageContent) extends MessageObject
case class SubjectMessage(from: SubjectName, to: SubjectName, messageType: MessageType, messageContent: MessageContent) extends MessageObject
case class TransportMessage(from: SubjectName, messageType: MessageType, messageContent: MessageContent) extends MessageObject
