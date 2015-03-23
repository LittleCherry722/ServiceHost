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

import ProcessAttributes._
import akka.actor._
import de.tkip.sbpm.application.ProcessInstanceActor.{ AgentsMap, Agent }
import de.tkip.sbpm.application.history._
import de.tkip.sbpm.model.Graph
import de.tkip.sbpm.application.subject.misc.AvailableAction
import java.util.Date
import de.tkip.sbpm.application.subject.behavior.Transition
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage

/**
 * For system control tasks
 */
sealed trait ControlMessage

/**
 * For Administration Messages, eg create user
 */
sealed trait AdministrationMessage extends ControlMessage

/**
 * For the process execution, needs an userid
 * TODO vllt falscher name
 */
sealed trait ExecutionMessage extends ControlMessage with SubjectProviderMessage

/**
 * An answerable controlmessage
 */
sealed trait AnswerAbleControlMessage extends ControlMessage with AnswerAbleMessage

/**
 * An answer to a controlmessage
 */
sealed trait AnswerControlMessage extends ControlMessage with AnswerMessage

// Konvention answers:
// als erstes Attribut kommt die Anfrage (muss eine Answermessage sein)
// mit dem Namen "request"
// damit man zurueckrouten kann

// All messages are ordered by:
// request
// answer

// administration
case class CreateSubjectProvider(userID: UserID) extends AnswerAbleControlMessage
case class SubjectProviderCreated(request: CreateSubjectProvider,
                                  userID: UserID) extends AnswerControlMessage

// execution
case class ProcessInstanceInfo(id: ProcessInstanceID,
                               name: String, processId: ProcessID)
case class GetAllProcessInstances(userID: UserID = AllUser) extends AnswerAbleControlMessage
case class AllProcessInstancesAnswer(request: GetAllProcessInstances,
                                     processInstanceInfo: Array[ProcessInstanceInfo]) extends AnswerControlMessage

case class ProcessInstanceData(id: ProcessInstanceID,
                               name: String,
                               processId: ProcessID,
                               processName: String,
                               graph: Graph,
                               isTerminated: Boolean,
                               startedAt: Date,
                               owner: UserID,
                               actions: Array[AvailableAction])

case class AutoArchive(message: Transition) extends ArchiveMessage
case class AddVariable(variableName: String , message: SubjectToSubjectMessage)
case class ReadProcessInstance(userID: UserID,
                               processInstanceID: ProcessInstanceID) extends AnswerAbleControlMessage with ProcessInstanceMessage
case class ReadProcessInstanceAnswer(request: ReadProcessInstance,
                                     answer: ProcessInstanceData) extends AnswerControlMessage

case class GetAgentsList (processId: ProcessID, url: String)
case class GetAgentsListResponse(agentsMap:  AgentsMap)

case class CreateProcessInstance(userID: UserID,
                                 processID: ProcessID,
                                 name: String,
                                 manager: Option[ActorRef] = None,
                                 agentsMap: AgentsMap) extends AnswerAbleControlMessage

case class ProcessInstanceCreated(request: CreateProcessInstance,
                                  processInstanceActor: ProcessInstanceRef,
                                  answer: ProcessInstanceData) extends AnswerControlMessage {
  def processInstanceID: ProcessInstanceID = answer.id
}
case class CreateServiceInstance(userID: UserID,
                                 processID: ProcessID,
                                 name: String,
                                 target: List[SubjectID],
                                 processInstanceidentical: String,
                                 //TODO: get the whole agents.
                                 agentsMap: AgentsMap,
                                 manager: Option[ActorRef] = None,
                                 managerUrl: String) extends AnswerAbleControlMessage

case class ServiceInstanceCreated(request: CreateServiceInstance,
                                  ServiceInstanceActorMap: Map[SubjectID, SubjectRef],
                                  answer: Map[SubjectID, ProcessInstanceData]) extends AnswerControlMessage

case class KillAllProcessInstances() extends AnswerAbleControlMessage
case class KillProcessInstance(processInstanceID: ProcessInstanceID) extends AnswerAbleControlMessage
case class KillProcessInstanceAnswer(request: KillProcessInstance) extends AnswerControlMessage

case object ProcessInstancesKilled

case class GetAvailableActions(userID: UserID,
                               processInstanceID: ProcessInstanceID = AllProcessInstances,
                               subjectID: SubjectID = AllSubjects)
    extends AnswerAbleControlMessage with ExecutionMessage
case class AvailableActionsAnswer(request: GetAvailableActions, availableActions: Array[AvailableAction]) extends AnswerControlMessage

//case class GetProcessInstance(userID: UserID, processInstanceID: ProcessInstanceID) extends AnswerAbleControlMessage;
//case class ProcessInstanceAnswer(request: GetProcessInstance, graphs: Array[ProcessGraph]) extends AnswerAbleControlMessage;

// new history
case class GetNewHistory() extends AnswerAbleControlMessage
case class NewHistoryAnswer(request: GetNewHistory, history: NewHistory) extends AnswerControlMessage
