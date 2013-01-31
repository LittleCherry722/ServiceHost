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

// administration
// request
case class CreateSubjectProvider() extends AnswerAbleControlMessage
// answer
case class SubjectProviderCreated(request: CreateSubjectProvider, userID: UserID) extends AnswerControlMessage

// execution
// request
case class GetAllProcessInstanceIDs() extends AnswerAbleControlMessage
case class CreateProcessInstance(userID: UserID, processID: ProcessID) extends AnswerAbleControlMessage
// TODO => KillProcessInstance
case class KillProcess(processInstanceID: ProcessInstanceID) extends AnswerAbleControlMessage
// GetAvailableActions collects all actions for the user if subjectID == null, else the action for this subject
case class GetAvailableActions(userID: UserID,
                               processInstanceID: ProcessInstanceID = AllProcessInstances,
                               subjectID: SubjectID = AllSubjects)
    extends AnswerAbleControlMessage with ExecutionMessage
case class GetProcessInstance(userID: UserID, processID: ProcessID) extends AnswerAbleControlMessage;
//answers
case class AllProcessInstanceIDsAnswer(request: GetAllProcessInstanceIDs, processInstanceIDs: Array[ProcessInstanceID]) extends AnswerControlMessage
case class ProcessInstanceCreated(request: CreateProcessInstance, processInstanceID: ProcessInstanceID) extends AnswerControlMessage
// availableActions: Array[(Int, AvailableAction)] = (actionID, AvailableAction)
// => (userID:Int, actionID:Int, actionInput:String?) 
case class AvailableActionsAnswer(request: GetAvailableActions, availableActions: Array[AvailableAction]) extends AnswerControlMessage
case class KillProcessAnswer(request: KillProcess, sucess: Boolean) extends AnswerControlMessage
case class ProcessInstanceAnswer(request: GetProcessInstance, graphs: Array[ProcessGraph]) extends AnswerAbleControlMessage;


// history
// request
case class GetHistory(userID: UserID, processInstanceID: ProcessInstanceID) extends AnswerAbleControlMessage with ExecutionMessage with ProcessInstanceMessage
// answer
case class HistoryAnswer(request: GetHistory, h: History) extends AnswerControlMessage

// TODO nochmal drueber schaun 
//request
// = SubjectMessage.ExecuteAction, wo wandelt man es um?
case class UpdateRequest(processID: ProcessID, actionID: String) extends AnswerAbleControlMessage

//answers
case class UpdateAnswer(request: UpdateRequest, sucess: Boolean) extends AnswerControlMessage

// !!! kommt bald raus / wird nicht unterstuetzt:
// modeling TODO modeling ist eigentlich in der Datenbank(persistance actor)
// request
case class ReadProcess(userID: UserID, processID: ProcessID) extends AnswerAbleControlMessage
case class CreateProcess(userID: UserID, processName: String, processGraph: ProcessGraph) extends AnswerAbleControlMessage
case class UpdateProcess(processID: ProcessID, processName: String, processModel: ProcessModel) extends AnswerAbleControlMessage
//answers
case class ReadProcessAnswer(request: ReadProcess, pm: ProcessModel) extends AnswerControlMessage
case class ProcessCreated(request: CreateProcess, processID: ProcessID) extends AnswerControlMessage
case class UpdateSucess(request: UpdateProcess, sucess: Boolean) extends AnswerControlMessage

// wurde unbenannt:
// ExecuteRequestAll =rename> GetAllProcessInstanceIDs, was genau zurueckgeben? alle processinstancen?, alle wo der user beteiligt ist?
case class ExecuteRequestAll(userID: UserID) extends AnswerAbleControlMessage
case class ExecutedListAnswer(era: ExecuteRequestAll, li: Iterable[de.tkip.sbpm.application.miscellaneous.ProcessAttributes.ProcessInstanceID]) extends AnswerAbleControlMessage
