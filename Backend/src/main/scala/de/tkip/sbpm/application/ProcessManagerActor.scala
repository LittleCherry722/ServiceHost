package de.tkip.sbpm.application

import akka.actor._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.ProcessModel
import de.tkip.sbpm.persistence._
import akka.event.Logging
import de.tkip.sbpm.ActorLocator

protected case class RegisterSubjectProvider(userID: UserID,
                                             subjectProviderActor: SubjectProviderRef)

/**
 * manages all processes and creates new ProcessInstance's on demand
 * information expert for relations between SubjectProviderActor/ProcessInstanceActor
 */
class ProcessManagerActor extends Actor {
  private case class ProcessInstanceData(processID: ProcessID, processInstanceActor: ProcessInstanceRef)

  val logger = Logging(context.system, this)
  // the process instances aka the processes in the execution
  private val processInstanceMap = collection.mutable.Map[ProcessInstanceID, ProcessInstanceData]()

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
    case getAll: GetAllProcessInstances => {

      sender !
        AllProcessInstancesAnswer(getAll, processInstanceMap.map(s => ProcessInstanceInfo(s._1, s._2.processID)).toArray.sortBy(_.id))
    }

    case cp: CreateProcessInstance => {
      // create the process instance
      context.actorOf(Props(new ProcessInstanceActor(cp)))
    }

    case pc: ProcessInstanceCreated => {
      if (pc.sender != null) {
        pc.sender ! pc
      } else {
        logger.error("Processinstance created: " + pc.processInstanceID + " but sender is unknown")
      }
      processInstanceMap +=
        pc.processInstanceID -> ProcessInstanceData(pc.request.processID, pc.processInstanceActor)
    }

    case kill @ KillProcessInstance(id) => {
      if (processInstanceMap.contains(id)) {
        context.stop(processInstanceMap(id).processInstanceActor)
        processInstanceMap -= id
        sender ! KillProcessInstanceAnswer(kill, true)
      } else {
        logger.info("Process Manager - can't kill process instance: " +
          id + ", it does not exists")
        sender ! KillProcessInstanceAnswer(kill, false)
      }
      // TODO always try to delete it from the database?
      //      ActorLocator.persistenceActor ! DeleteProcessInstance(id)
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
      // TODO besser damit umgehen wenn subjectProvider nicht existiert
      val userID = message.userID
      if (subjectProviderMap.contains(userID)) {
        subjectProviderMap(userID).forward(message)
      } else {
        // TODO testweise
        // if the subjectprovider does not exist forward the message to
        // the subjectprovidermanager, so he can create the user dynamicly
        ActorLocator.subjectProviderManagerActor.forward(message)

        logger.info("Process Manager - User unknown: " + userID + " message: " + message)
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
      processInstanceMap(message.processInstanceID).processInstanceActor.!(message) // TODO mit forwards aber erstmal testen
    } else {
      if (message.isInstanceOf[AnswerAbleMessage]) {
        // TODO create an answertrait for this error
        message.asInstanceOf[AnswerAbleMessage].sender ! None
      }
      logger.error("ProcessManager - message for " + message.processInstanceID +
        " but does not exist, " + message)
    }
  }
}
