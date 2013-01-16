package de.tkip.sbpm.application

import akka.actor._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.ProcessModel
import de.tkip.sbpm.persistence._

/**
 * manages all processes and creates new ProcessInstance's on demand
 * information expert for relations between SubjectProviderActor/ProcessInstanceActor/SubjectActor (TODO)
 */
class ProcessManagerActor(private val name: String) extends Actor {
  private var processCount = 0
  private val processInstanceMap = collection.mutable.Map[ProcessInstanceID, ProcessInstanceRef]()
  
  private val processDescritionMap = collection.mutable.Map[ProcessID, ProcessModel]()

  // used to map answermessages back to the subjectProvider who sent a request
  private val subjectProviderMap = collection.mutable.Map[UserID, SubjectProviderRef]()

   // initialize persistence actors
  private lazy val testPersistenceActor = context.actorOf(Props[TestPersistenceActor], "testPersistenceActor")
  private lazy val persistenceActor = context.actorOf(Props[PersistenceActor], "persistenceActor") 
  
  def receive = {
    
    // persistence router - in case the debug flag is set, forward the message to
    // test persistence actor
    case pa: PersistenceAction => 
      if (pa.isInstanceOf[Debug]) {
        forwardToTestPersistenceActor(pa)
      } else {
        forwardToPersistenceActor(pa)
      }
    
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
      killProcess(kill.processInstanceID)

  }

  // forward persistence messages to persistenceActors
  private def forwardToTestPersistenceActor(pa: PersistenceAction) {
    testPersistenceActor.forward(pa)
  }
    
  private def forwardToPersistenceActor(pa: PersistenceAction) {
    persistenceActor.forward(pa)
  }
  
  // forward control message to processInstance with a given processID
  private def forwardControlMessageToProcess(processID: ProcessID,
    controlMessage: ControlMessage) {
    if (processInstanceMap.contains(processID))
      processInstanceMap(processID) ! controlMessage
  }

  // creates a new processInstanceActor and registers it with the given processID (overrides the old entry)
  private def createNewProcessInstance(processID: ProcessID) = {
    // TODO wenn processId nicht ovrhanden gibt es einen Fehler
    val process = context.actorOf(Props(new ProcessInstanceActor(processCount, processDescritionMap(processID))))
    processInstanceMap += processCount -> process
    process
  }

  // kills the processInstanceActor with the given processID and unregisters it
  private def killProcess(processID: ProcessInstanceID) = {
    if (processInstanceMap.contains(processID)) {
      context.stop(processInstanceMap(processID))
      processInstanceMap -= processID
    }
  }
}