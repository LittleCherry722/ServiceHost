package de.tkip.servicehost

import akka.actor.ActorRef

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

object ServiceAttributes {
  type ServiceID = String;
  type ServiceName = String
  type ServiceInstanceKey = (String, ServiceID)
  type BranchID = String;
  type ServiceActorRef = ActorRef
  type MessageText = String
  type MessageType = String
  type SubjectID = String

  type VariableType = String
  type VariableId = String
  type CorrelationId = String
  //type ChannelID = (Option[String], Option[String])
}

