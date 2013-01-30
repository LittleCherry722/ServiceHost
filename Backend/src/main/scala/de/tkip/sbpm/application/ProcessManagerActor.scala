package de.tkip.sbpm.application

import akka.actor._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.ProcessModel
import de.tkip.sbpm.persistence._

protected case class RegisterSubjectProvider(userID: UserID,
                                             subjectProviderActor: SubjectProviderRef)

/**
 * manages all processes and creates new ProcessInstance's on demand
 * information expert for relations between SubjectProviderActor/ProcessInstanceActor
 */
class ProcessManagerActor extends Actor {
  // the process instances aka the processes in the execution
  private var processInstanceCount = 0
  private val processInstanceMap = collection.mutable.Map[ProcessInstanceID, ProcessInstanceRef]()

  // used to map answer messages back to the subjectProvider who sent a request
  private val subjectProviderMap = collection.mutable.Map[UserID, SubjectProviderRef]()

  // initialize persistence actors
  private lazy val testPersistenceActor = context.actorOf(Props[TestPersistenceActor], "testPersistenceActor")
  private lazy val persistenceActor = context.actorOf(Props[PersistenceActor], "persistenceActor")

  def receive = {
    case register: RegisterSubjectProvider => {
      subjectProviderMap += register.userID -> register.subjectProviderActor
    }

    // execution
    case getAll: GetAllProcessInstanceIDs => {
      sender !
        AllProcessInstanceIDsAnswer(getAll, processInstanceMap.keys.toArray)
    }

    case cp: CreateProcessInstance => {
      // TODO daten aus der datenbank holen
      // TODO hier checken ob der process existiert?
      if (true) {
        // if the process exists create the process instance
        // set the id to a val to avoid errors
        val processInstanceID = processInstanceCount
        processInstanceMap +=
          processInstanceID ->
          context.actorOf(
            Props(
              new ProcessInstanceActor(
                processInstanceID,
                cp.processID)))
        sender ! ProcessInstanceCreated(cp, processInstanceID)
        // increase the count, so the next process instance gets a new unique id
        processInstanceCount += 1
      } else {
        println("Process Manager - cant start process " + cp.processID +
          ", it does not exist")
      }
    }

    case kill: KillProcess => {
      if (processInstanceMap.contains(kill.processInstanceID)) {
        context.stop(processInstanceMap(kill.processInstanceID))
        processInstanceMap -= kill.processInstanceID
        sender ! KillProcessAnswer(kill, true)
      } else {
        println("Process Manager - can't kill process instance: " +
          kill.processInstanceID + ", it does not exists")
        sender ! KillProcessAnswer(kill, false)

      }
    }

    // general matching
    // persistence router - in case the debug flag is set, forward the message to
    // test persistence actor
    case message: PersistenceMessage => {
      if (message.isInstanceOf[Debug]) {
        forwardToTestPersistenceActor(message)
      } else {
        forwardToPersistenceActor(message)
      }
    }

    // TODO muesste man auch zusammenfassenkoennen
    case message: ProcessInstanceMessage => {
      forwardMessageToProcessInstance(message)
    }

    case message: SubjectMessage => {
      forwardMessageToProcessInstance(message)
    }

    case message: SubjectProviderMessage => {
      val userID = message.userID
      if (subjectProviderMap.contains(userID)) {
        subjectProviderMap(userID).forward(message)
      } else {
        println("Process Manager - User unknown: " + userID + " message: " + message)
      }
    }

    case answer: AnswerMessage => {
      answer.sender.forward(answer)
    }
  }

  private def forwardToPersistenceActor(pa: PersistenceMessage) {
    persistenceActor.forward(pa)
  }

  // forward persistence messages to persistenceActors
  private def forwardToTestPersistenceActor(pa: PersistenceMessage) {
    testPersistenceActor.forward(pa)
  }

  private type ForwardProcessInstanceMessage = { def processInstanceID: ProcessInstanceID }

  private def forwardMessageToProcessInstance(message: ForwardProcessInstanceMessage) {
    if (processInstanceMap.contains(message.processInstanceID)) {
      processInstanceMap(message.processInstanceID).!(message) // TODO mit forwards aber erstmal testen
    } else {
      println("ProcessManager - message for " + message.processInstanceID +
        " but does not exist, " + message)
    }
  }
}
