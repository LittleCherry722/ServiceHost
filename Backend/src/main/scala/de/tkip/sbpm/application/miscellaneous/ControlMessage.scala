package de.tkip.sbpm.application.miscellaneous

import de.tkip.sbpm.rest._
import ProcessAttributes._
import akka.actor._
import de.tkip.sbpm.application.SubjectInformation
import de.tkip.sbpm.model.BehaviourState

sealed trait ControlMessage // For system control tasks
trait AnswerMessage {
  private var _sender: ProcessInterfaceRef = null

  def sender = _sender

  def sender_=(sender: ProcessInterfaceRef) {
    if (_sender == null) {
      _sender = sender
    }
  }
}

case object GetMessage extends ControlMessage
case class RequestForMessages(exitConds: Array[ExitCond]) extends ControlMessage
case object Stored extends ControlMessage
case class Successor(nextState: String) extends ControlMessage
case class TransportMessage(fromCond: ExitCond, messageContent: MessageContent) extends ControlMessage
case object End extends ControlMessage

case class CreateSubjectProvider() extends ControlMessage
case class CreateProcess(userID: UserID) extends ControlMessage with AnswerMessage // Tells the processManager to create a new process

case class ExecuteRequest(userID: UserID, processID: ProcessID) extends ControlMessage
case class AddState(userID: UserID, processID: ProcessID, subjectName: SubjectName, behaviourState: BehaviourState) extends ControlMessage

case class KillProcess(processInstanceID: ProcessInstanceID) extends ControlMessage
// Message to tell IntervalBehaviorActor to process his states
case class ProcessBehaviour(processManager: ProcessManagerRef, subjectName: SubjectName, subjectProviderName: SubjectName, inputPool: ActorRef) extends ControlMessage

// userid anforderung
case class RequestUserID(subjectInformation: SubjectInformation, generateAnswer: UserID => ControlMessage)

// answers
case class ProcessCreated(cp: CreateProcess, processID: ProcessID) 
case class SubjectProviderCreated(csp: CreateSubjectProvider, userID: UserID)

import de.tkip.sbpm.model.Subject
case class AddSubject(userID: UserID, processID: ProcessID, subject: Subject) extends ControlMessage