package de.tkip.sbpm.application.miscellaneous

import akka.actor._

object ProcessAttributes {
  type UserID = Int
  type ProcessID = Int
  type ProcessInstanceID = Int
  type SubjectName = String
  type MessageType = String
  type MessageContent = String
  type StateAction = String
  type StateID = String

  type SuccessorID = StateID

  type SubjectProviderManagerRef = ActorRef
  type SubjectProviderRef = ActorRef
  type ProcessManagerRef = ActorRef
  type ProcessInstanceRef = ActorRef
  type SubjectRef = ActorRef
  type ProcessInterfaceRef = ActorRef

}
