package de.tkip.sbpm.verification.lts

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._

sealed trait LtsLabel
case object Tau extends LtsLabel
case class SendLabel(from: SubjectId, fromAgent: AgentId, messageType: MessageType, to: SubjectId, toAgents: Set[AgentId]) extends LtsLabel {
  override def toString = s"$from sends $messageType to $to"
}
case class ReceiveLabel(target: SubjectId, targetAgent: AgentId, messageType: MessageType, sender: SubjectId, senderAgents: Set[AgentId]) extends LtsLabel {
  override def toString = s"$target receives $messageType from $sender"
}

case class LtsTransition(from: LtsState, label: LtsLabel, to: LtsState)