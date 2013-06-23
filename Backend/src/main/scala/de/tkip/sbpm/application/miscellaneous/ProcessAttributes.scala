/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.application.miscellaneous

import akka.actor.ActorRef

object ProcessAttributes {

  type UserID = Int; val AllUser = -1
  type ProcessID = Int
  type ProcessInstanceID = Int; val AllProcessInstances = -1

  type SubjectID = String; val AllSubjects = ""
  type SubjectName = String
  type SubjectSessionID = Int // This ID is used to differ the Subjects in a Multisubject,
  // although also SingeSubject has this id 
  type StateID = Int
  type SuccessorID = StateID // TODO SuccessorID als extra attribut?
  type StateAction = String

  type MessageType = String; val AllMessages = ""
  type MessageContent = String
  type MessageID = Int

  type SubjectProviderManagerRef = ActorRef
  type SubjectProviderRef = ActorRef
  type ProcessManagerRef = ActorRef
  type ProcessInstanceRef = ActorRef
  type SubjectRef = ActorRef
  type InterfaceRef = ActorRef

  type InternalBehaviorRef = ActorRef
  type BehaviorStateRef = ActorRef
}
