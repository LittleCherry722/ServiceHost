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
trait AnswerMessage {
  private var _sender: InterfaceRef = null

  def sender = _sender

  def sender_=(sender: InterfaceRef) {
    if (_sender == null) {
      _sender = sender
    }
  }
}

// Konvention answers:
// als erstes Attribut kommt die Anfrage (muss eine Answermessage sein)
// damit man zurueckrouten kann

// modeling
// request
case class CreateProcess(userID: UserID, processName: String, processModel: ProcessModel) extends ControlMessage with AnswerMessage
case class UpdateProcess(processID: ProcessID, processName: String, processModel: ProcessModel) extends ControlMessage with AnswerMessage
//answers
case class ProcessCreated(cp: CreateProcess, processID: ProcessID) extends ControlMessage

// execution
// request
case class CreateProcessInstance(userID: UserID) extends ControlMessage with AnswerMessage // Tells the processManager to create a new process
//answers
case class ProcessInstanceCreated(cp: CreateProcessInstance, processInstanceID: ProcessInstanceID) extends ControlMessage

// TODO nochmal drueber schaun 
case object GetMessage extends ControlMessage
case class RequestForMessages(exitConds: Array[SubjectMessageRouting]) extends ControlMessage
case object Stored extends ControlMessage
case class Successor(nextState: String) extends ControlMessage

case object End extends ControlMessage

case class ExecuteRequest(userID: UserID, processID: ProcessID) extends ControlMessage with AnswerMessage
case class AddState(userID: UserID, processID: ProcessID, subjectName: SubjectName, behaviourState: BehaviourStateActor) extends ControlMessage
case class KillProcess(processInstanceID: ProcessInstanceID) extends ControlMessage
case class CreateSubjectProvider() extends ControlMessage
case class SubjectProviderCreated(csp: CreateSubjectProvider, userID: UserID)

//request
case class ReadProcess(userID: UserID, processID: ProcessID) extends ControlMessage with AnswerMessage
case class GetHistory(userID: UserID, processID: ProcessInstanceID) extends ControlMessage with AnswerMessage
case class ExecuteRequestAll(userID: UserID) extends ControlMessage with AnswerMessage
case class RequestAnswer(processID: ProcessID, actionID: StateID) extends ControlMessage with AnswerMessage
//answers
case class LoadedProcessesList(era: ExecuteRequestAll)
case class ReadProcessAnswer(rp: ReadProcess, pm: ProcessModel)
case class HistoryAnswer(hi: GetHistory, h: History)
case class ExecutedListAnswer(era: ExecuteRequestAll, li: Iterable[de.tkip.sbpm.application.miscellaneous.ProcessAttributes.ProcessInstanceID])

//import de.tkip.sbpm.model.Subject
case class AddSubject(userID: UserID, processID: ProcessID, subjectName: String) extends ControlMessage
