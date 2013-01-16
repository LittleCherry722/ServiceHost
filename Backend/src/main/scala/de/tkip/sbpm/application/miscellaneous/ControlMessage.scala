package de.tkip.sbpm.application.miscellaneous

import de.tkip.sbpm.rest._
import ProcessAttributes._
import akka.actor._
import de.tkip.sbpm.application.SubjectInformation
import de.tkip.sbpm.model.BehaviourState
import de.tkip.sbpm.model.Transition
import de.tkip.sbpm.application.SubjectMessageRouting

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

case object GetMessage extends ControlMessage
case class RequestForMessages(exitConds: Array[SubjectMessageRouting]) extends ControlMessage
case object Stored extends ControlMessage
case class Successor(nextState: String) extends ControlMessage

case object End extends ControlMessage

case class CreateSubjectProvider() extends ControlMessage
case class CreateProcess(userID: UserID) extends ControlMessage with AnswerMessage // Tells the processManager to create a new process

case class ExecuteRequest(userID: UserID, processID: ProcessID) extends ControlMessage with AnswerMessage
case class AddState(userID: UserID, processID: ProcessID, subjectName: SubjectName, behaviourState: BehaviourState) extends ControlMessage

case class KillProcess(processInstanceID: ProcessInstanceID) extends ControlMessage

// userid request
case class RequestUserID(subjectInformation: SubjectInformation, generateAnswer: UserID => ControlMessage) extends ControlMessage

// answers
case class ProcessCreated(cp: CreateProcess, processID: ProcessID) 
case class SubjectProviderCreated(csp: CreateSubjectProvider, userID: UserID)
case class ProcessInstanceCreated(er: ExecuteRequest, instanceID: ProcessInstanceID)

//import de.tkip.sbpm.model.Subject
case class AddSubject(userID: UserID, processID: ProcessID, subjectName: String) extends ControlMessage