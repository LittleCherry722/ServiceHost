package de.tkip.sbpm.application.miscellaneous

import de.tkip.sbpm.rest._
import ProcessAttributes._
import akka.actor._
import de.tkip.sbpm.application.SubjectInformation
import de.tkip.sbpm.model.BehaviourStateActor
import de.tkip.sbpm.model.Transition
import de.tkip.sbpm.application.SubjectMessageRouting
import de.tkip.sbpm.model.ProcessModel
import de.tkip.sbpm.application.History

sealed trait ControlMessage // For system control tasks
// This trait is for messages which are send from the frontend
// TODO man könnte hier (wenn immer vorhanden) oder in einem 2ten trait die userID wie beim
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

// modeling
// request
case class CreateProcess(userID: UserID, processName: String, processModel: ProcessModel) extends ControlMessage with AnswerAbleMessage
case class UpdateProcess(processID: ProcessID, processName: String, processModel: ProcessModel) extends ControlMessage with AnswerAbleMessage
//answers
case class ProcessCreated(request: CreateProcess, processID: ProcessID) extends ControlMessage with AnswerMessage[ProcessCreated]

// execution
// request
case class CreateProcessInstance(userID: UserID) extends ControlMessage with AnswerAbleMessage
case class GetAvailableActions(userID: UserID, processID: ProcessID) extends ControlMessage with AnswerAbleMessage
//answers
case class ProcessInstanceCreated(request: CreateProcessInstance, processInstanceID: ProcessInstanceID) extends ControlMessage with AnswerMessage[ProcessInstanceCreated]
case class AvailableActionsAnswer(request: GetAvailableActions) extends ControlMessage with AnswerMessage[AvailableActionsAnswer]

// TODO nochmal drueber schaun 
case object GetMessage extends ControlMessage
case class RequestForMessages(exitConds: Array[SubjectMessageRouting]) extends ControlMessage
case object Stored extends ControlMessage
case class Successor(nextState: String) extends ControlMessage

case object End extends ControlMessage

case class ExecuteRequest(userID: UserID, processID: ProcessID) extends ControlMessage with AnswerAbleMessage
case class AddState(userID: UserID, processID: ProcessID, subjectName: SubjectName, behaviourState: BehaviourStateActor) extends ControlMessage
case class CreateSubjectProvider() extends ControlMessage
case class SubjectProviderCreated(csp: CreateSubjectProvider, userID: UserID) extends ControlMessage

//request
case class KillProcess(processInstanceID: ProcessInstanceID) extends ControlMessage
case class ReadProcess(userID: UserID, processID: ProcessID) extends ControlMessage with AnswerAbleMessage
case class GetHistory(userID: UserID, processID: ProcessInstanceID) extends ControlMessage with AnswerAbleMessage
case class ExecuteRequestAll(userID: UserID) extends ControlMessage with AnswerAbleMessage
case class RequestAnswer(processID: ProcessID, actionID: StateID) extends ControlMessage with AnswerAbleMessage
//answers
case class LoadedProcessesList(era: ExecuteRequestAll)
case class ReadProcessAnswer(rp: ReadProcess, pm: ProcessModel)
case class HistoryAnswer(hi: GetHistory, h: History)
case class ExecutedListAnswer(era: ExecuteRequestAll, li: Iterable[de.tkip.sbpm.application.miscellaneous.ProcessAttributes.ProcessInstanceID])

//import de.tkip.sbpm.model.Subject
case class AddSubject(userID: UserID, processID: ProcessID, subjectName: String) extends ControlMessage
