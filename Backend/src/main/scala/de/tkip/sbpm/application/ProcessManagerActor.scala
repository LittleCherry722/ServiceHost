package de.tkip.sbpm.application

import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._

/**
 * manages all processes and creates new ProcessInstance's on demand
 * information expert for relations between SubjectProviderActor/ProcessInstanceActor/SubjectActor (TODO)
 */
class ProcessManagerActor(private val name: String) extends Actor {
  private var processCount = 0
  private val processMap = collection.mutable.Map[ProcessID, ProcessInstanceRef]()

  // used to map answermessages back to the subjectProvider who sent a request
  private val subjectProviderMap = collection.mutable.Map[UserID, SubjectProviderRef]()

  def receive = {
    case as: AddSubject =>
      forwardControlMessageToProcess(as.processID, as)

    case sr: ExecuteRequest => // request the status of the process
      forwardControlMessageToProcess(sr.processID, sr)

    case spc: SubjectProviderCreated =>
      subjectProviderMap += spc.userID -> sender

    case as: AddState => // forwards an AddState request to the process that corresponds to the given processID
      forwardControlMessageToProcess(as.processID, as)

    case cp: CreateProcess =>
      createNewProcessInstance(processCount)
      sender ! ProcessCreated(cp, processCount)
      processCount += 1

    case kill: KillProcess =>
      killProcess(kill.processID)

  }

  // forward control message to processInstance with a given processID
  private def forwardControlMessageToProcess(processID: ProcessID,
    controlMessage: ControlMessage) {
    if (processMap.contains(processID))
      processMap(processID) ! controlMessage
  }

  // creates a new processInstanceActor and registers it with the given processID (overrides the old entry)
  private def createNewProcessInstance(processID: ProcessID) = {
    val process = context.actorOf(Props(new ProcessInstanceActor(processID)))
    processMap += processID -> process
    process
  }

  // kills the processInstanceActor with the given processID and unregisters it
  private def killProcess(processID: ProcessID) = {
    if (processMap.contains(processID)) {
      context.stop(processMap(processID))
      processMap -= processID
    }
  }
}