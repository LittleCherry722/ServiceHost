package de.tkip.servicehost

import akka.actor.ActorRef

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

object ServiceAttributes {
  type ServiceID = String;
  //type ProcessInstanceKey = (ServiceID, ProcessInstanceID)
  //type ProcessInstanceKey = (String, ProcessInstanceID)
  type ServiceInstanceKey = (String, ServiceID)
  type BranchID = String;

  type ServiceActorRef = ActorRef

  type MessageText = String
  type MessageType = String

  type VariableType = String
  type VariableId = String
}

