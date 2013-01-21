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
  // the process descriptions
  private var processCount = 0
  private val processDescritionMap = collection.mutable.Map[ProcessID, ProcessModel]()

  // the process instances aka the processes in the execution
  private var processInstanceCount = 0
  private val processInstanceMap = collection.mutable.Map[ProcessInstanceID, ProcessInstanceRef]()

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
      forwardControlMessageToProcessInstance(hi.processID, hi)

    case sra: ExecuteRequestAll =>
      sra.sender ! processInstanceMap.keys

    case rp: ReadProcess =>
      rp.sender ! processDescritionMap

    case sr: ExecuteRequest => // request the status of the process
      forwardControlMessageToProcessInstance(sr.processID, sr)

    case spc: SubjectProviderCreated =>
      subjectProviderMap += spc.userID -> sender

    // modeling
    case cp: CreateProcess =>
      val processModel: ProcessModel = ProcessModel(processCount, cp.processName, cp.processGraph)
      processDescritionMap += processCount -> processModel
      sender ! ProcessCreated(cp, processCount)
      processCount += 1

    case up: UpdateProcess =>
      processDescritionMap(up.processID) = up.processModel
    // TODO was mit den laufen processinstanzen machen?

    case ra: RequestAnswer =>
      //wo muss die entscheidung festgehalten werden
      println("not yet implemnted")

    // execution
    case cp: CreateProcessInstance =>
      createNewProcessInstance(processInstanceCount)
      sender ! ProcessInstanceCreated(cp, processInstanceCount)
      processInstanceCount += 1

    case kill: KillProcess =>
      killProcessInstance(kill.processInstanceID)

    // a process instance informs the subject provider that a subject has been created
    case (userID: UserID, sc: SubjectCreated) =>
      if (subjectProviderMap.contains(userID)) {
        // forward the message to the correct subject provider
        subjectProviderMap(userID).forward(sc)
      } else {
        println("SubjectProvider does not exists: " + userID)
      }

    // for the Testcase
    case (id: Int, as: AddSubject) =>
      processInstanceMap(id).forward(as)
  }

  // forward persistence messages to persistenceActors
  private def forwardToTestPersistenceActor(pa: PersistenceAction) {
    testPersistenceActor.forward(pa)
  }

  private def forwardToPersistenceActor(pa: PersistenceAction) {
    persistenceActor.forward(pa)
  }

  // forward control message to processInstance with a given processID
  // TODO braucht man überhaupt noch?
  private def forwardControlMessageToProcessInstance(processInstanceID: ProcessInstanceID,
                                                     controlMessage: ControlMessage) {
    if (processInstanceMap.contains(processInstanceID))
      processInstanceMap(processInstanceID) ! controlMessage
  }

  // creates a new processInstanceActor and registers it with the given processID (overrides the old entry)
  private def createNewProcessInstance(processID: ProcessID) = {
    // TODO wenn processId nicht ovrhanden gibt es einen Fehler
    val process = context.actorOf(Props(new ProcessInstanceActor(processInstanceCount, processDescritionMap(processID))))
    processInstanceMap += processInstanceCount -> process
    process
  }

  // kills the processInstanceActor with the given processID and unregisters it
  private def killProcessInstance(processInstanceID: ProcessInstanceID) = {
    if (processInstanceMap.contains(processInstanceID)) {
      context.stop(processInstanceMap(processInstanceID))
      processInstanceMap -= processInstanceID
    }
  }
}
