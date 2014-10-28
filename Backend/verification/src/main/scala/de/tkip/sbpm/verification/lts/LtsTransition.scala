package de.tkip.sbpm.verification.lts

import de.tkip.sbpm.newmodel.ProcessModelTypes._

sealed trait LtsLabel {
  val simple: String
}
case object Tau extends LtsLabel {
  val simple = "Tau"
}
case class SendLabel(from: SubjectId, fromAgent: AgentId, messageType: MessageType, to: SubjectId, toAgents: Set[AgentId]) extends LtsLabel {
  override def toString = s"$from sends $messageType to $to"
  val simple = "send"
}
case class ReceiveLabel(target: SubjectId, targetAgent: AgentId, messageType: MessageType, sender: SubjectId, senderAgents: Set[AgentId]) extends LtsLabel {
  override def toString = s"$target receives $messageType from $sender"
  val simple = "receive"
}

case class LtsTransition(fromState: LtsState, label: LtsLabel, toState: LtsState)