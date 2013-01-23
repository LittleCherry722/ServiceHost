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

sealed trait ControlMessage // For system control tasks
// This trait is for messages which are send from the frontend
// TODO man koennte hier (wenn immer vorhanden) oder in einem 2ten trait die userID wie beim
// AnswerMessage reinmixen und somit den subjectprovidermanager (und vllt andere) lesbarer machen
trait AnswerAbleMessage {
  private var _sender: InterfaceRef = null

  def sender = _sender

  def sender_=(sender: InterfaceRef) {
    if (_sender == null) {
      _sender = sender
    }
  }
}

// This trait is for the answers of the messages with the previous trait
trait AnswerMessage[A <: MessageType.Answer] {
  self: A =>
  def sender: ActorRef = self.request.sender
}
protected object MessageType {
  type Answer = { def request: AnswerAbleMessage }
}

// Konvention answers:
// als erstes Attribut kommt die Anfrage (muss eine Answermessage sein)
// mit dem Namen "request"
// damit man zurueckrouten kann

// modeling TODO modeling ist eigentlich in der Datenbank(persistance actor)
// request
case class ReadProcess(userID: UserID, processID: ProcessID) extends ControlMessage with AnswerAbleMessage
case class CreateProcess(userID: UserID, processName: String, processGraph: ProcessGraph) extends ControlMessage with AnswerAbleMessage
case class UpdateProcess(processID: ProcessID, processName: String, processModel: ProcessModel) extends ControlMessage with AnswerAbleMessage
//answers
case class ReadProcessAnswer(request: ReadProcess, pm: ProcessModel) extends ControlMessage with AnswerMessage[ReadProcessAnswer]
case class ProcessCreated(request: CreateProcess, processID: ProcessID) extends ControlMessage with AnswerMessage[ProcessCreated]

// administration
// request
case class CreateSubjectProvider() extends ControlMessage with AnswerAbleMessage
// answer
case class SubjectProviderCreated(request: CreateSubjectProvider, userID: UserID) extends ControlMessage with AnswerMessage[SubjectProviderCreated]

// execution
// request
case class CreateProcessInstance(userID: UserID) extends ControlMessage with AnswerAbleMessage
case class GetAvailableActions(userID: UserID, processInstanceID: ProcessInstanceID) extends ControlMessage with AnswerAbleMessage
case class KillProcess(processInstanceID: ProcessInstanceID) extends ControlMessage //TODO vllt anwswer?
//answers
case class ProcessInstanceCreated(request: CreateProcessInstance, processInstanceID: ProcessInstanceID) extends ControlMessage with AnswerMessage[ProcessInstanceCreated]
case class AvailableActionsAnswer(request: GetAvailableActions, availableActions: Array[AvailableAction]) extends ControlMessage with AnswerMessage[AvailableActionsAnswer]

// history
// request
case class GetHistory(userID: UserID, processID: ProcessInstanceID) extends ControlMessage with AnswerAbleMessage
// answer
case class HistoryAnswer(request: GetHistory, h: History) extends ControlMessage with AnswerMessage[HistoryAnswer]

// TODO nochmal drueber schaun 
case class ExecuteRequest(userID: UserID, processID: ProcessID) extends ControlMessage with AnswerAbleMessage
//request
case class ExecuteRequestAll(userID: UserID) extends ControlMessage with AnswerAbleMessage
case class RequestAnswer(processID: ProcessID, actionID: StateID) extends ControlMessage with AnswerAbleMessage
//answers
case class LoadedProcessesList(era: ExecuteRequestAll)
case class ExecutedListAnswer(era: ExecuteRequestAll, li: Iterable[de.tkip.sbpm.application.miscellaneous.ProcessAttributes.ProcessInstanceID])
