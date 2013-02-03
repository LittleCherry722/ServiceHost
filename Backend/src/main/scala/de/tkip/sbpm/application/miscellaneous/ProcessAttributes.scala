package de.tkip.sbpm.application.miscellaneous

import akka.actor.ActorRef

object ProcessAttributes {

  type UserID = Int; val AllUser = -1
  type ProcessID = Int
  type ProcessInstanceID = Int; val AllProcessInstances = -1

  type SubjectID = String; val AllSubjects = ""
  type SubjectName = String
  type StateID = Int
  type SuccessorID = StateID
  type StateAction = String

  type MessageType = String
  type MessageContent = String

  type SubjectProviderManagerRef = ActorRef
  type SubjectProviderRef = ActorRef
  type ProcessManagerRef = ActorRef
  type ProcessInstanceRef = ActorRef
  type SubjectRef = ActorRef
  type InterfaceRef = ActorRef

  type InternalBehaviorRef = ActorRef
  type BehaviorStateRef = ActorRef
}
