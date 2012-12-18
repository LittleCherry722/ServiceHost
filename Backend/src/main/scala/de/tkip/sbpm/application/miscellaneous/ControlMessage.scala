package de.tkip.sbpm.application.miscellaneous

import ProcessAttributes._
import akka.actor._

sealed trait ControlMessage // For system control tasks
case class AddSubject(processID: ProcessID, subjectName: SubjectName) extends ControlMessage // Used by ProcessManager actor
case object GetMessage extends ControlMessage
case class RequestForMessages(exitConds: Array[ExitCond]) extends ControlMessage
case object Stored extends ControlMessage
case class Successor(nextState: String) extends ControlMessage
case class TransportMessage(fromCond: ExitCond, messageContent: MessageContent) extends ControlMessage
case object End extends ControlMessage

case class CreateSubjectProvider(userID: UserID) extends ControlMessage
case class CreateProcess(processID: ProcessID) extends ControlMessage // Tells the processManager to create a new process

case class StatusRequest(userID: UserID, processID: ProcessID) extends ControlMessage
case class AddState(userID: UserID, processID: ProcessID, subjectName: SubjectName, behaviourState: BehaviourState) extends ControlMessage

// Message to tell IntervalBehaviorActor to process his states
case class ProcessBehaviour(processManager: ProcessManagerRef, subjectName: SubjectName, subjectProviderName: SubjectName, inputPool: ActorRef) extends ControlMessage