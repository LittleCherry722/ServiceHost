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

import de.tkip.sbpm.rest._
import ProcessAttributes._
import akka.actor._
import de.tkip.sbpm.application.SubjectInformation
import de.tkip.sbpm.application.History
import de.tkip.sbpm.application.history._
import de.tkip.sbpm.application.subject._
import de.tkip.sbpm.model.Graph
import de.tkip.sbpm.application.subject.misc.AvailableAction

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
case class SubjectProviderCreated(request: CreateSubjectProvider, userID: UserID) extends AnswerControlMessage

// execution
case class ProcessInstanceInfo(id: ProcessInstanceID, processId: ProcessID)
case class GetAllProcessInstances(userID: UserID = AllUser) extends AnswerAbleControlMessage
case class AllProcessInstancesAnswer(request: GetAllProcessInstances, processInstanceInfo: Array[ProcessInstanceInfo]) extends AnswerControlMessage

case class ProcessInstanceData(id: ProcessInstanceID,
                               processId: ProcessID,
                               graph: Graph,
                               isTerminated: Boolean,
                               history: History,
                               actions: Array[AvailableAction])

case class ReadProcessInstance(userID: UserID, processInstanceID: ProcessInstanceID) extends AnswerAbleControlMessage with ProcessInstanceMessage
case class ReadProcessInstanceAnswer(request: ReadProcessInstance, answer: ProcessInstanceData) extends AnswerControlMessage

case class CreateProcessInstance(userID: UserID, processID: ProcessID, manager: Option[ActorRef] = None) extends AnswerAbleControlMessage
case class ProcessInstanceCreated(request: CreateProcessInstance,
                                  processInstanceActor: ProcessInstanceRef,
                                  answer: ProcessInstanceData) extends AnswerControlMessage {
  def processInstanceID: ProcessInstanceID = answer.id
}

case class KillAllProcessInstances() extends AnswerAbleControlMessage
case class KillProcessInstance(processInstanceID: ProcessInstanceID) extends AnswerAbleControlMessage
case class KillProcessInstanceAnswer(request: KillProcessInstance) extends AnswerControlMessage

case class GetAvailableActions(userID: UserID,
                               processInstanceID: ProcessInstanceID = AllProcessInstances,
                               subjectID: SubjectID = AllSubjects)
    extends AnswerAbleControlMessage with ExecutionMessage
case class AvailableActionsAnswer(request: GetAvailableActions, availableActions: Array[AvailableAction]) extends AnswerControlMessage

//case class GetProcessInstance(userID: UserID, processInstanceID: ProcessInstanceID) extends AnswerAbleControlMessage;
//case class ProcessInstanceAnswer(request: GetProcessInstance, graphs: Array[ProcessGraph]) extends AnswerAbleControlMessage;

// history
case class GetHistory(userID: UserID, processInstanceID: ProcessInstanceID) extends AnswerAbleControlMessage with ExecutionMessage with ProcessInstanceMessage
case class HistoryAnswer(request: GetHistory, history: History) extends AnswerControlMessage
// new history
case class GetNewHistory extends AnswerAbleControlMessage
case class NewHistoryAnswer(request: GetNewHistory, history: NewHistory) extends AnswerControlMessage
