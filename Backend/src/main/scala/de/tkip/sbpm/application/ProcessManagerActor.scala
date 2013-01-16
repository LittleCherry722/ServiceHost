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
  private var processInstanceCount = 0
  private val processInstanceMap = collection.mutable.Map[ProcessInstanceID, ProcessInstanceRef]()

  private var processCount = 0
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

	case hi: GetHistory => 
      forwardControlMessageToProcess(hi.processID, hi)
      
    case sra: ExecuteRequestAll => 
      sra.sender ! processInstanceMap.keys

     case rp: ReadProcess =>
      rp.sender ! processDescritionMap

    case as: AddSubject =>
      forwardControlMessageToProcess(as.processID, as)

    case sr: ExecuteRequest => // request the status of the process
      forwardControlMessageToProcess(sr.processID, sr)

    case spc: SubjectProviderCreated =>
      subjectProviderMap += spc.userID -> sender

    case as: AddState => // forwards an AddState request to the process that corresponds to the given processID
      forwardControlMessageToProcess(as.processID, as)

    // modeling
    case cp: CreateProcess =>
      processDescritionMap += processCount -> cp.processModel
      sender ! ProcessCreated(cp, processCount)
      processCount += 1

    case up: UpdateProcess =>
      processDescritionMap(up.processID) = up.processModel

    // execution
    case cp: CreateProcessInstance =>
      createNewProcessInstance(processInstanceCount)
      sender ! ProcessInstanceCreated(cp, processInstanceCount)
      processInstanceCount += 1

    case kill: KillProcess =>
      killProcess(kill.processInstanceID)

    // for the Testcase
    case (id: Int, as: AddSubject) =>
      processInstanceMap(id) forward as
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
    val process = context.actorOf(Props(new ProcessInstanceActor(processInstanceCount, processDescritionMap(processID))))
    processInstanceMap += processInstanceCount -> process
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
