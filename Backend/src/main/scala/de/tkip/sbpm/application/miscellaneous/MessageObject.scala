package de.tkip.sbpm.application.miscellaneous

import ProcessAttributes._

sealed trait MessageObject
case class SubjectMessage(fromCond: ExitCond, toCond: ExitCond, messageContent: MessageContent) extends MessageObject
