package de.tkip.sbpm.application.miscellaneous

import de.tkip.sbpm.rest._
import ProcessAttributes._
import akka.actor._
import de.tkip.sbpm.application.SubjectInformation
import de.tkip.sbpm.application.subject.BehaviorStateActor
import de.tkip.sbpm.model.Transition
import de.tkip.sbpm.model.ProcessModel
import de.tkip.sbpm.application.History
import de.tkip.sbpm.application.subject._
import de.tkip.sbpm.model.ProcessGraph
import de.tkip.sbpm.model.ProcessGraph

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
case class CreateSubjectProvider() extends AnswerAbleControlMessage
case class SubjectProviderCreated(request: CreateSubjectProvider, userID: UserID) extends AnswerControlMessage

// execution
case class GetAllProcessInstanceIDs(userID: UserID = AllUser) extends AnswerAbleControlMessage
case class AllProcessInstanceIDsAnswer(request: GetAllProcessInstanceIDs, processInstanceIDs: Array[ProcessInstanceID]) extends AnswerControlMessage

case class CreateProcessInstance(userID: UserID, processID: ProcessID) extends AnswerAbleControlMessage
case class ProcessInstanceCreated(request: CreateProcessInstance, processInstanceID: ProcessInstanceID) extends AnswerControlMessage

case class KillProcessInstance(processInstanceID: ProcessInstanceID) extends AnswerAbleControlMessage
case class KillProcessInstanceAnswer(request: KillProcessInstance, success: Boolean) extends AnswerControlMessage

case class GetAvailableActions(userID: UserID,
                               processInstanceID: ProcessInstanceID = AllProcessInstances,
                               subjectID: SubjectID = AllSubjects)
    extends AnswerAbleControlMessage with ExecutionMessage
case class AvailableActionsAnswer(request: GetAvailableActions, availableActions: Array[AvailableAction]) extends AnswerControlMessage

case class GetProcessInstance(userID: UserID, processInstanceID: ProcessInstanceID) extends AnswerAbleControlMessage;
case class ProcessInstanceAnswer(request: GetProcessInstance, graphs: Array[ProcessGraph]) extends AnswerAbleControlMessage;

// history
case class GetHistory(userID: UserID, processInstanceID: ProcessInstanceID) extends AnswerAbleControlMessage with ExecutionMessage with ProcessInstanceMessage
case class HistoryAnswer(request: GetHistory, h: History) extends AnswerControlMessage
