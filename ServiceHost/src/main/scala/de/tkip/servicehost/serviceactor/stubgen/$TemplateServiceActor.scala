package de.tkip.servicehost.serviceactor.stubgen

import akka.actor.Actor
import de.tkip.sbpm.{ActorLocator => BackendActorLocator}
import de.tkip.sbpm.repository.RepositoryPersistenceActor.{AgentsMappingResponse, GetAgentsMapMessage}
import de.tkip.servicehost.ActorLocator._
import de.tkip.servicehost.serviceactor.ServiceActor
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.servicehost.Messages._
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import akka.actor.ActorRef
import scala.concurrent.duration._
import akka.actor.Props
import akka.actor.PoisonPill
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import java.util.Date
import de.tkip.sbpm.application.subject.misc.Stored
import de.tkip.servicehost.{main, ActorLocator}
import de.tkip.servicehost.ServiceAttributes._
import scala.collection.immutable.List
import scala.collection.mutable.{Queue, Map}
import de.tkip.sbpm.application.subject.misc.Rejected
import scala.concurrent.{ExecutionContext, Await}
import ExecutionContext.Implicits.global

import scala.concurrent.Await

class $TemplateServiceActor extends ServiceActor {
  override protected val INPUT_POOL_SIZE: Int = 20
  
  override protected val serviceID: ServiceID = "$SERVICEID"
  override protected val subjectID: SubjectID = "$SERVICEID"
  protected val serviceInstanceMap = Map[SubjectID, ServiceActorRef]()
  
  
  override protected def states: List[State] = List(
      //$EMPTYSTATE$//
      )

  // start with first state
  // TODO: that is not always the start state!
  def getStartState(): State = {
    getState(0)
  }

  
  private val messages: Map[MessageType, MessageText] = Map(
      //$EMPTYMESSAGE$//
      )

  private val inputPool: scala.collection.mutable.Map[Tuple2[MessageType, SubjectID], Queue[SubjectToSubjectMessage]] = scala.collection.mutable.Map()

  // Subject default values
  private var target = -1
  private var messageContent: String = "" // will be used in getResult

  override def reset = {
    // TODO: reset custom properties
    super.reset
  }

  def processMsg() {
    log.debug("processMsg")

    state match {
      case rs: ReceiveState => {
        var message: SubjectToSubjectMessage = null

        for ((branch, target) <- state.targets) {
          val messageType: MessageType = branch
          val fromSubjectID: SubjectID = target.target.subjectID
          val key = (messageType, fromSubjectID)
          log.debug("processMsg: key = " + key)

          if (inputPool.contains(key) && inputPool(key).length > 0) {
            message = inputPool(key).dequeue;
          }
        }

        log.debug("processMsg: message = " + message)

        if (message != null) {
          this.messageContent = message.messageContent

          this.branchCondition = message.messageType

          rs.handle(message) // calls changeState
        }
        else log.info("ReceiveState could not find any matching message. ReceiveState will wait until it arrivies")
      }
      case _ =>
        log.info("unable to handle message now, needs to be in ReceiveState. Current state is: " + state)
    }
  }

  def processSendState() {
    //find or create the target service actor
    val sTarget = if(state.targets.size > 1){
      state.targets(branchCondition).target
    }else state.targets.head._2.target
    val targetSubjectID = sTarget.subjectID
    var serviceInstance: ServiceActorRef = null

    if(!serviceInstanceMap.contains(targetSubjectID)){
      lazy val repositoryPersistenceActor = ActorLocator.repositoryPersistenceActor
      val getAgentsMapMessage = GetAgentsMapMessage(Seq(targetSubjectID))
      val newAgentsMapFuture = (repositoryPersistenceActor ?? getAgentsMapMessage).mapTo[AgentsMappingResponse]
      val newAgentsMap = Await.result(newAgentsMapFuture, (4 seconds))
      if (newAgentsMap.possibleAgents.contains(targetSubjectID)) {
        val newProcessInstanceName = "Unnamed"
        val agent = newAgentsMap.possibleAgents(targetSubjectID).head
        val agentAddr = agent.address.toUrl
        val path = "akka.tcp" + "://sbpm" + agentAddr + "/user/" + BackendActorLocator.subjectProviderManagerActorName
        val agentManagerSelection = context.actorSelection(path)
        val processInstanceidenticalFuture = (ActorLocator.serviceActorManager ?? AskForProcessInstanceidentical((serviceID, getProcessInstanceID()))).mapTo[String]
        val processInstanceidentical = Await.result(processInstanceidenticalFuture, (4 seconds))
        val createMessage = CreateServiceInstance(
          userID = ExternalUser,
          processID = getProcessID(),
          name = newProcessInstanceName,
          target = List().::(targetSubjectID),// TODO: multi subjects
          processInstanceidentical,
          None)
        val processInstanceCreatedAnswer = (agentManagerSelection ?? createMessage).mapTo[ProcessInstanceCreated]

        processInstanceCreatedAnswer onComplete{
          case processInstanceCreated =>
            log.debug("processInstanceCreated.onComplete: processInstanceCreated = {}", processInstanceCreated)
            if(processInstanceCreated.isSuccess){
              serviceInstanceMap += targetSubjectID -> processInstanceCreated.get.processInstanceActor
              serviceInstance = processInstanceCreated.get.processInstanceActor
            }else {
              // TODO exception or log?
              throw new Exception("processInstance Created failed for " +
                targetSubjectID + "\nreason" + processInstanceCreated)
            }
        }

      }
    }else{
      serviceInstance = serviceInstanceMap(targetSubjectID)
    }

    //send message
    val messageID = 100 //TODO change if needed
    val messageType = state.targetIds.head._1
    val userID = 1
    val processID = getProcessID()
    val subjectID = getSubjectID()
    val sender = getDestination()
    val fileInfo = None
    val target = sTarget
    target.insertTargetUsers(Array(1))
    val message = SubjectToSubjectMessage(
      messageID,
      processID,
      1,
      subjectID,
      target,
      messageType,
      getMessage(),
      fileInfo)
    serviceInstance !! message
  }

  def stateReceive = {
    case message: SubjectToSubjectMessage => {
      // TODO forward /set variables?
      log.debug("receive message: " + message)
      storeMsg(message, sender)

      state match {
        case rs: ReceiveState =>
          processMsg()
        case _ =>
          log.info("message will be handled when state changes to ReceiveState. Current state is: " + state)
      }
    }
  }

  def changeState() {
    log.debug("changeState: old state: " + state)
    state match {
      case s: ExitState => {
        log.warning("already in ExitState, can not change state")
      }
      case _ => {
        if (state.targetIds.size > 1) {
          if (this.branchCondition != null) {
            state = getState(state.targetIds(this.branchCondition))

          } else log.warning("no branchcodition defined")

        } else state = getState(state.targetIds.head._2)

        // TODO: state könnte null sein, oder auch der alte..
        state.process()
      }
    }
    log.debug("changeState: new state: " + state)
  }

  def getState(id: Int): State = {
    states.find(x => x.id == id).getOrElse(null)
  }

  def storeMsg(message: Any, sender: ActorRef): Unit = {
    log.debug("storeMsg: " + message + " from " + sender)
    message match {
      case message: SubjectToSubjectMessage => {
        val key = (message.messageType, message.from)
        log.debug("storeMsg: key = " + key)

        if (inputPool.contains(key)) {
          if (inputPool(key).size < INPUT_POOL_SIZE) {
            (inputPool(key)).enqueue(message)
            log.debug("storeMsg: Stored")
            sender !! Stored(message.messageID)
          } else {
            log.debug("storeMsg: Rejected")
            sender !! Rejected(message.messageID)
          }
        } else {
          inputPool(key) = Queue(message)
          log.debug("storeMsg: Stored")
          sender !! Stored(message.messageID)
        }
      }
      case message => log.warning("unable to store message: " + message)
    }
  }

  def getBranchIDforType(messageType: String): MessageText = {
    messages(messageType)
  }

  def getDestination(): ActorRef = {
    manager.get
  }

  def terminate() {
    ActorLocator.serviceActorManager !! KillProcess(serviceID, processInstanceID)
  }

  def getProcessID(): ProcessID = {
    processID
  }

  def getSubjectID(): String = {
    serviceID
  }

  def getProcessInstanceID(): ProcessInstanceID = {
    processInstanceID
  }

  def getResult(msg: String): String = {   // handle the messageContent
    msg
  }

  //$ACTIONSTATESIMPLEMENTATION$//
}
