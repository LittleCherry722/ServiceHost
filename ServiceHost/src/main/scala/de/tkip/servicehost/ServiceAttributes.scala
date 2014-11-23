package de.tkip.servicehost

import akka.actor.ActorRef

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

object ServiceAttributes {
  type ServiceID = String;
  type ProcessKey = (ServiceID, ProcessInstanceID)

  type BranchID = String;

  type ServiceActorRef = ActorRef

  type MessageText = String
  type MessageType = String
}

