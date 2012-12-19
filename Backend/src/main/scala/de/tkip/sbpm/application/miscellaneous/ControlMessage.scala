package de.tkip.sbpm.application.miscellaneous

import de.tkip.sbpm.rest._
import ProcessAttributes._
import akka.actor._

sealed trait ControlMessage // For system control tasks
trait AnswerMessage {
  private var _sender: ProcessInterfaceRef = null
  def sender_=(interface: ProcessInterfaceRef) {
    if (_sender == null && interface != null)
      _sender = interface
  }

  def sender = _sender
}

case object GetMessage extends ControlMessage
case class RequestForMessages(exitConds: Array[ExitCond]) extends ControlMessage
case object Stored extends ControlMessage
case class Successor(nextState: String) extends ControlMessage
case class TransportMessage(fromCond: ExitCond, messageContent: MessageContent) extends ControlMessage
case object End extends ControlMessage

case class CreateSubjectProvider(userID: UserID) extends ControlMessage
case class CreateProcess(userID: UserID) extends ControlMessage with AnswerMessage // Tells the processManager to create a new process
case class AddSubject(processID: ProcessID, subjectName: SubjectName) extends ControlMessage

case class ExecuteRequest(userID: UserID, processID: ProcessID) extends ControlMessage
case class AddState(userID: UserID, processID: ProcessID, subjectName: SubjectName, behaviourState: BehaviourState) extends ControlMessage

// Message to tell IntervalBehaviorActor to process his states
case class ProcessBehaviour(processManager: ProcessManagerRef, subjectName: SubjectName, subjectProviderName: SubjectName, inputPool: ActorRef) extends ControlMessage

// answers
case class ProcessCreated(cp: CreateProcess, processID: ProcessID)