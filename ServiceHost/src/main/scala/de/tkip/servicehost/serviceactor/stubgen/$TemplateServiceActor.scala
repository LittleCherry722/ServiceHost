package de.tkip.servicehost.serviceactor.stubgen

import java.text.SimpleDateFormat

import akka.actor.Actor
import de.tkip.sbpm.application.ProcessInstanceActor
import de.tkip.sbpm.application.ProcessInstanceActor._
import de.tkip.sbpm.application.subject.behavior.{TimeoutCond, Reopen}
import de.tkip.sbpm.{ActorLocator => BackendActorLocator}
import de.tkip.sbpm.repository.RepositoryPersistenceActor.{AgentsMappingResponse, GetAgentsMapMessage}
import de.tkip.servicehost.ActorLocator._
import de.tkip.servicehost.serviceactor.ServiceActor
import de.tkip.servicehost.Messages._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import akka.actor.ActorRef
import scala.concurrent.duration._
import akka.actor.Props
import akka.actor.PoisonPill
import java.util.Date
import de.tkip.servicehost.{main, ActorLocator}
import de.tkip.servicehost.ServiceAttributes._
import scala.collection.immutable.List
import scala.collection.mutable.{ListBuffer, Queue, Map}
import scala.concurrent.{DelayedLazyVal, ExecutionContext, Await}
import ExecutionContext.Implicits.global
import scala.util.control.Breaks._
import concurrent._

import scala.concurrent.Await

class $TemplateServiceActor extends ServiceActor {
  override protected val INPUT_POOL_SIZE: Int = "$INPUTPOOL".toInt
  override protected val serviceName: String = "$SERVICENAME"
  override protected val serviceID: ServiceID = "$SERVICEID"
  override protected val subjectID: SubjectID = "$SERVICEID"
  protected val serviceInstanceMap = Map[SubjectID, ServiceActorRef]()
  private var from: SubjectID = ""
  private var processInstanceIdentical: String = ""
  private var managerURL: String = ""
  private val startNodeIndex: String = "$STARTNODEINDEX"
  private var receivedMessageType: String = ""
  //var variablesOfService = collection.mutable.Map[String, ListBuffer[SubjectToSubjectMessage]]()
  private var sendingVariables = collection.mutable.Map[String, Variable]()
  private var closeIPMap = Map[Int, ChannelID]()
  private var openIPMap = Map[Int, ChannelID]()
  private var isCloseIP = false
  private var currentCloseIPChannelID = ListBuffer[ChannelID]()
  private val blockedSendStatesMap = Map[ChannelID, ActorRef]()

  private var activateStateMap = Map[Int, Int]()
  private var deactivateStateMap = Map[Int, Int]()
  // this map holds the queue of the income messages for a channel
  private var messageQueueMap = Map[ChannelID, Queue[SubjectToSubjectMessage]]()
  // this map holds the overflow queue of the income messages for a channel
  private var messageOverflowQueueMap = Map[ChannelID, Queue[(ActorRef, SubjectToSubjectMessage)]]()
  private val reserveMessageAndReceiver = Map[Int, (ActorRef, SubjectToSubjectMessage)]()
  private var target = -1
  private var messageContent: String = ""
  private var currentMessageID = 0
//  private var isReceiveStateEnabled = false
//  private var unfinishedChannelID = ListBuffer[ChannelID]()
  private var isTimeoutTransitionDefined = true
  private var acknowledgement = Map[Int, Int]()
  private var observerStatesMap = Map[Int, Int]()
  private var messageInformation = List[Tuple4[String, String, String, String]]()
  //private var suspendStatesMap = Map[Int, State]()

  override protected def states: List[State] = List(
    //$EMPTYSTATE$//
  )

  observerStatesMap = Map(
    //$OBSERVERSTATES$//
  )

  // different received messageType -> different outgoing messageType like: m1 -> m2, m3 -> m4
  val inputAndOutputMap: Map[String, String] = Map(

  )

  // start with first state
  def getStartState(): State = {
    getState("$STARTNODEINDEX".toInt)
  }

  messages = Map(
    //$EMPTYMESSAGE$//
  )

  //$EMPTYVARIABLES$//
  //$EMPTYCLOSEIP$//
  //$EMPTYOPENIP$//
  //$EMPTYACTIVATESTATE//
  //$EMPTYDEACTIVATESTATE//

  private def messageID: Int = {
    currentMessageID += 1
    currentMessageID
  }

  override def preStart {
    log.debug("*******************  You are using the service: {}  *******************", serviceName)
    context.parent ! ServiceNameToServiceIdMap(serviceName + "ServiceActor", serviceID)
    currentStatesMap += getStartState().id -> getStartState() // initialize the first state
    getStartState().process()
  }

  override def postStop {

  }

  override def preRestart(reason: Throwable, message: Option[Any]) {

  }

  override def postRestart(reason: Throwable) {

  }

  def stateReceive = {

    //this case will be executed, if the inputPool receives a disabled message and the input pool is not full.
    //the incoming message will be stored and a reply (stored message) will be sent back to the sender
    case message: SubjectToSubjectMessage if (spaceAvailableInMessageQueue(message.from, message.messageType) && !message.enabled) => {
      log.debug("receive message: " + message)
      // Is inputPool already closed ? YES
      if (isCloseIP) {
        val currentSubjID = message.from
        val currentMsgTy = message.messageID
        val isAllSubj = if (currentCloseIPChannelID.find(closeChannelID => closeChannelID._1.equals("##all##")) == None) {
          false
        } else {
          true
        }

        val isAllMsg = if (currentCloseIPChannelID.find(closeChannelID => closeChannelID._2.equals("##all##")) == None) {
          false
        } else {
          true
        }

        if (currentCloseIPChannelID.contains(("##all##", "##all##")) || isAllSubj || isAllMsg || currentCloseIPChannelID.contains((currentSubjID, currentMsgTy))) {
          log.debug("InputPool reject to receive all messages.")
          sender !! Rejected(message.messageID)
          val channelID = new ChannelID(message.from, message.messageType)
          blockedSendStatesMap(channelID) = sender
        } else if (currentCloseIPChannelID.contains(("", currentMsgTy)) || currentCloseIPChannelID.contains((currentSubjID, ""))) {
          log.debug("InputPool reject to receive current message! Because the current ChannelID is {}", (currentSubjID, currentMsgTy))
          sender !! Rejected(message.messageID)
          val channelID = new ChannelID(message.from, message.messageType)
          blockedSendStatesMap(channelID) = sender
        } else if (currentCloseIPChannelID.contains(currentSubjID, currentMsgTy)) {
          log.debug("InputPool reject to receive current message! Because the current ChannelID is {}", (currentSubjID, currentMsgTy))
          sender !! Rejected(message.messageID)
          val channelID = new ChannelID(message.from, message.messageType)
          blockedSendStatesMap(channelID) = sender
        } else {
          log.debug("CURRENTLY INPUTPOOL IS CLOSED, BUT THIS MESSAGE CAN BE RECEIVED! ")
          from = message.from
          sender !! Stored(message.messageID)
          enqueueMessage(message) //reserve message
          log.debug("The current message will be received and it's reservation is done!")
        }
      }
      //InputPool is open
      else {
        from = message.from
        sender !! Stored(message.messageID)
        enqueueMessage(message) //reserve message
        log.debug("reservation is done!")
      }
    }
    //this case will be executed, if a enable-request is received
    //of so, the message which has to be enabled will be searched in the queue and will be enabled responding with a enabled-message
    //if there is no message to enable, the response will be a rejected message
    case message: SubjectToSubjectMessage if (message.enabled) => {
      log.debug("InputPool received enable request from " + message)

      if (!enableMessage(message)) {
        log.warning("message rejected, no message to enable: {}", message)
        sender !! Rejected(message.messageID)

      }
      else if (message.correlationId == "0") {
        sender !! Enabled(message.messageID) // normal SubjectToSubjectMessage
        log.debug("Message enabled; id (" + message.messageID + ")" + "Message has been received by receiver!")
        val currentStatesList = currentStatesMap.values.toList
        currentStatesList.foreach(s => {
          s match {
            case rs: ReceiveState => {
              processMsg(s.id)
            }
            case _ =>
              log.info("message will be handled when state changes to ReceiveState. Current state is: " + s)
          }
        })
      }
      else {
        log.debug("message needs to be further processed!") // check SubjectToSubjectMessage with correlationId
        // Service has ActivateState.
        val currentStatesList = currentStatesMap.values.toList
        currentStatesList.foreach(s => {
          s match {
            case rs: ReceiveState => {
              processMsg(s.id)
            }
            case _ =>
              log.info("message will be handled when state changes to ReceiveState. Current state is: " + s)
          }
        })
      }
    }

    //this case will be executed, if a disabled message was received and the input queue is full
    //the message will be stored in the overflow queue and a overflow-message will be sent to the sender
    case message: SubjectToSubjectMessage if (!spaceAvailableInMessageQueue(message.from, message.messageType) && !message.enabled) => {
      //store reservation message
      log.debug("InputPool received reservation from " + sender + " -> but message queue is full! Save sender id and reject reservation")
      //send stored notification back to sender
      sender !! Overflow(message.messageID)
      //put message into the overflowQueue
      enqueueOverflowMessage(message)
    }

    case msg: UpdateServiceInstanceDate => {
      for ((subjectId, agents) <- msg.agentsMap) {
        tempAgentsMap += subjectId -> agents
      }
      processInstanceIdentical = msg.processInstanceIdentical
      managerURL = msg.managerUrl
    }

    case TimeoutExpired => {
      if (isTimeoutTransitionDefined) {
        executeTimeout()
      }
    }

    case Stored(messageID) => {
      if (reserveMessageAndReceiver.contains(messageID)) {
        (reserveMessageAndReceiver(messageID)._1) !! (reserveMessageAndReceiver(messageID)._2)
      }
    }

    case x: Rejected => {
      log.debug("Receiver's inputPool is full. " + x)
    }

    case Enabled(messageId) => {
      log.debug("message with id {} is enabled. Current state will be changed! ", messageId)
      val acknowledgeSendStateId = acknowledgement(messageId)

      getState(acknowledgeSendStateId) match {
        case ss: SendState => {
          acknowledgement -= messageId
          changeState(acknowledgeSendStateId)
        }
        case _ => {
          log.debug(" State will be changed when the state is SendState. Current state is {}.", state)
        }
      }
    }

    case Overflow(messageID) => {
      log.debug("receiver's inputPool is full. sender must wait.")
    }

    case StateManager(stateId) => {
      if (suspendStatesMap.contains(stateId)) {
        log.debug("state; {}, is suspended !", getState(stateId))
        suspendStatesMap -= stateId
        sender !! false
      } else {
        log.debug("This current state normal running !")
        sender !! true
      }
    }

    case Info => {
      val description = "Test Service"
      var statesList = List[State]()
      currentStatesMap.foreach(state => {
        statesList = state._2 :: statesList
      })
      val detailedInfo = ServiceInstanceDetailedInfo(
        description,
        serviceName,
        serviceID,
        INPUT_POOL_SIZE,
        tempAgentsMap(serviceID).address,
        None,
        statesList,
        messageInformation
      )
      sender ! detailedInfo
    }

    case _ => log.debug("no defined ")

  }

  def getState(id: Int): State = {
    states.find(x => x.id == id).getOrElse(null)
  }

  def getBranchIDForType(messageType: String): MessageText = {
    messages(messageType)
  }

  def getDestination(): ActorRef = {
    manager
  }

  def terminate() {
    // terminate self
    context.parent !! ServiceTerminate(processInstanceIdentical, serviceID, serviceName)
  }

  def terminate(processInstanceIdentical: String, serviceId: String, serviceName: String) {
    // terminate other service
    context.parent !! ServiceTerminate(processInstanceIdentical, serviceId, serviceName)
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

  def getResult(msg: String): String = {
    // handle the messageContent
    msg
  }

  override def reset = {
    // TODO: reset custom properties
    currentStatesMap.clear()
    terminate()
  }

  def executeTimeout() {
    changeState(state.id)
  }

  def getMessages(): Map[MessageType, MessageText] = {
    val currentMessages = messages
    currentMessages
  }


  def getVariableOfSubject(): Map[String, String] = {
    val currentVariableOfSubject = variablesOfSubject
    currentVariableOfSubject
  }

  def processMsg(id: Int) {
    val currentState = getState(id)
    var isBreak = false
    log.debug("==========    PROCESS RECEIVED MESSAGE    ==========")
    currentState match {
      case rs: ReceiveState => {
        val unfinishedChannelID = ListBuffer[ChannelID]()
        var timeoutEdge = "" // receiveState includes Timeout edge?
        rs.targetIds.foreach(edge => {
          if (edge._1.startsWith("#")) {
            timeoutEdge = edge._1
          }
        })

        if (rs.targetIds.size > 1 && !timeoutEdge.equals("")) {
          // yes, the current receiveState includes Timeout edge.
          this.branchCondition = timeoutEdge
          val timeout = timeoutEdge.substring(timeoutEdge.lastIndexOf("#") + 1, timeoutEdge.length).toInt
          val stateTimeout = TimeoutCond(false, timeout)
          if (!stateTimeout.manual) {
            context.system.scheduler.scheduleOnce(FiniteDuration(stateTimeout.duration, "s"), self, TimeoutExpired)
          }
        }

        for ((branch, target) <- currentState.targets) {
          val messageType: MessageType = branch
          val fromSubjectID: SubjectID = target.target.subjectID
          val key = (fromSubjectID, messageType)
          unfinishedChannelID.append(key)
        }
        var message: SubjectToSubjectMessage = null

        breakable {
          // find the matching message
          for (key <- 0 until unfinishedChannelID.size) {

            if (messageQueueMap.contains(unfinishedChannelID(key))) {
              message = dequeueMessage(unfinishedChannelID(key));
              if (message != null) {
                log.debug("InputPool has received matching messages!!!")
                message match {
                  case msg: SubjectToSubjectMessage if (rs.variableId != "") => {
                    log.debug("use Variable to collect SubjectToSubjectMessage")
                    var variableName = ""
                    for ((vName, vType) <- variablesOfSubject) {
                      if (vType == rs.variableId) {
                        variableName = vName
                      }
                    }
                    if (!tempAgentsMap.contains(message.from)) {
                      getAgent(message.from)
                    }
                    addMessage(message, variableName) // collect SubjectToSubjectMessage
                    this.messageContent = message.messageContent.toString
                    this.branchCondition = message.messageType
                    if (!timeoutEdge.equals("")) {
                      isTimeoutTransitionDefined = false
                    }
                    isBreak = true
                    break() // matching message is found, break the loop.
                  }

                  case msg: SubjectToSubjectMessage if (rs.variableId.equals("") && rs.correlationId.equals("0") && msg.correlationId.equals("0")) => {
                    log.debug("normal SubjectToSubjectMessage")
                    this.messageContent = message.messageContent.toString
                    this.branchCondition = message.messageType
                    if (!timeoutEdge.equals("")) {
                      isTimeoutTransitionDefined = false
                    }
                    isBreak = true
                    break() // matching message is found, break the loop.
                  }

                  case msg: SubjectToSubjectMessage if (rs.variableId.equals("") && !rs.correlationId.equals("0") && rs.correlationId.equals(msg.correlationId)) => {
                    log.debug("Use correlationId to keep the synchronization!")
                    sender !! Enabled(message.messageID)
                    this.messageContent = message.messageContent.toString
                    this.branchCondition = message.messageType
                    if (!timeoutEdge.equals("")) {
                      isTimeoutTransitionDefined = false
                    }
                    isBreak = true
                    break() // matching message is found, break the loop.
                  }
                  case msg: SubjectToSubjectMessage => {
                    sender !! Rejected(message.messageID)
                  }
                }
              } else {
                // Message's ChannelID is in MessageQueueMap, but message is null..
                log.debug("ReceiveState could not find any matching message. Message is null. ReceiveState will wait until it arrives. It's ChannelID's {}", unfinishedChannelID(key))
              }
            } else {
              // Message is not in MessageQueueMap.The current ReceiveState must wait.
              log.debug("ReceiveState could not find any matching message. ReceiveState will wait until it arrives. It's ChannelID's {}", unfinishedChannelID(key))
            }
          }
        }
        if (isBreak) {
          isBreak = false
          isObserverState(id)
          rs.handle(Some(this.messageContent))
        }
      }
      case _ =>
        log.info("unable to handle message now, needs to be in ReceiveState. Current state is: " + currentState)
    }
  }

  def processSendState(id: Int) {
    //find or create the target service actor
    log.debug("=== PROCESS SEND STATE ===")
    val currentSendState = getState(id)
    val sTarget = if (currentSendState.targets.size > 1) {
      currentSendState.targets(inputAndOutputMap(receivedMessageType)).target
    } else currentSendState.targets.head._2.target
    val targetSubjectID = sTarget.subjectID
    var serviceInstance: ServiceActorRef = null
    val messageType = currentSendState.targetIds.head._1
    /*
    if sentState has multi edges,messageType can be determined according to inputAndOutputMap
     */
    //    val messageType = inputAndOutputMap(receivedMessageType)
    //    this.branchCondition = inputAndOutputMap(receivedMessageType)
    val userID = 1
    val processID = getProcessID()
    val subjectID = getSubjectID()
    val manager = getDestination()
    val fileInfo = None
    val target = sTarget
    target.insertTargetUsers(Array(1))
    val msgContent: MessageContent = TextContent(getMessage())
    val sendingMessageId = messageID
    //TEST CORRELATION
    var currentCorrelationId = "0"
    if (currentSendState.correlationId != "0") {
      currentCorrelationId = currentSendState.correlationId
    } else currentCorrelationId
    // TEST CORRELATION
    val message = SubjectToSubjectMessage(
      sendingMessageId,
      processID,
      1,
      subjectID,
      target,
      messageType,
      msgContent,
      None,
      false,
      currentCorrelationId, // TEST CORRELATION
      fileInfo,
      Some(processInstanceIdentical)
    )
    val now = new Date
    val dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
    val date = dateFormat.format(now)
    val element = (date, msgContent.toString, "to", targetSubjectID)
    messageInformation = element :: messageInformation
    if (currentSendState.variableId != "") {
      val newMsgContent = MessageSet(sendingVariables(getVariableName(currentSendState.variableId)))
      val newMessage = message.copy(messageContent = newMsgContent)
      determineReceiver(targetSubjectID, newMessage)
      acknowledgement += sendingMessageId -> id
    } else {
      determineReceiver(targetSubjectID, message)
      acknowledgement += sendingMessageId -> id
    }

  }

  def determineReceiver(targetSubjectID: String, msg: SubjectToSubjectMessage): Unit = {
    if (targetSubjectID.equals(from)) {
      if (tempAgentsMap(targetSubjectID).address.toUrl == managerURL) {
        receiver = manager // directly return to Backend
        reserve(msg, receiver)
        receiver !! msg
      }
      else {
        receiver = sender
        reserve(msg, receiver)
        receiver !! msg
      }
    } else {
      if (!serviceInstanceMap.contains(targetSubjectID)) {
        if (tempAgentsMap.contains(targetSubjectID)) {
          if (tempAgentsMap(targetSubjectID).address.toUrl == managerURL) {
            receiver = manager
            reserve(msg, receiver)
            receiver !! msg
          } else {
            /*

            If targetSubjectID is in tempAgentsMap, this means the service has been created.
            If all services are in the same server(127.0.0.1), the current service can ask its parent what is address of the matching service.
            If these services are not in the same server(eg. 127.0.0.1 and 127.0.0.3),
            the current service must ask the ServiceActorManager of the matching service what is address.

             */
            val future = context.parent ?? AskForServiceInstance(processInstanceIdentical, targetSubjectID) // get targetServiceInstance
            val serviceInstance = Await.result(future, (5 seconds)).asInstanceOf[ActorRef]
            future onComplete {
              case serviceActor =>
                if (serviceActor.isSuccess) {
                  receiver = serviceInstance
                  reserve(msg, receiver)
                  receiver !! msg
                } else {

                  throw new Exception("The matching service isn't exist in local ServiceActorManager ! ")

                  val agentsAddr = tempAgentsMap(targetSubjectID).address.toUrl
                  val path = "akka.tcp" + "://sbpm" + agentsAddr + "/user/" + BackendActorLocator.subjectProviderManagerActorName
                  val agentManagerSelection = context.actorSelection(path)
                  //agentManagerSelection !! message // serviceActorManager will forward message to correspond service.
                  val future = agentManagerSelection ?? AskForServiceInstance(processInstanceIdentical, targetSubjectID) // get targetServiceInstance
                  val remoteServiceInstance = Await.result(future, (5 seconds)).asInstanceOf[ActorRef]
                  future onComplete {
                    case remoteServiceActor =>
                      if (remoteServiceActor.isSuccess) {
                        receiver = remoteServiceInstance
                        reserve(msg, receiver)
                        receiver !! msg
                      } else {
                        throw new Exception("The matching service isn't exist !")
                      }
                  }
                }
            }
          }
        } else {
          // If targetSubjectId isn't in tempAgentsMap, this means the targetServiceInstance is not created. The service need to ask repository about targetAgent.

          val newAgentsMap = getAgent(targetSubjectID)
          val newProcessInstanceName = "Unnamed"
          val agent = newAgentsMap.possibleAgents(targetSubjectID).head
          val agentAddr = agent.address.toUrl
          val remoteProcessID = agent.processId
          val path = "akka.tcp" + "://sbpm" + agentAddr + "/user/" + BackendActorLocator.subjectProviderManagerActorName
          val agentManagerSelection = context.actorSelection(path)

          val createMessage = CreateServiceInstance(
            userID = ExternalUser,
            remoteProcessID,
            name = newProcessInstanceName,
            target = List().::(targetSubjectID), // TODO: multi subjects
            processInstanceIdentical,
            tempAgentsMap.toMap,
            Some(manager),
            managerURL)
          val future = agentManagerSelection ?? createMessage
          val processInstanceCreatedAnswer = Await.result(future, (5 seconds)).asInstanceOf[ProcessInstanceCreated]
          future onComplete {
            case processInstanceCreated =>
              log.debug("processInstanceCreated.onComplete: processInstanceCreated = {}", processInstanceCreated)
              if (processInstanceCreated.isSuccess) {
                serviceInstanceMap += targetSubjectID -> processInstanceCreatedAnswer.processInstanceActor
                serviceInstance = processInstanceCreatedAnswer.processInstanceActor
                receiver = serviceInstance
                reserve(msg, receiver)
                receiver !! msg
              } else {
                // TODO exception or log?
                throw new Exception("processInstance Created failed for " +
                  targetSubjectID + "\nreason" + processInstanceCreated)
              }
          }
        }
      } else {
        serviceInstance = serviceInstanceMap(targetSubjectID)
        receiver = serviceInstance
        reserve(msg, receiver)
        receiver !! msg
      }
    }
  }

  def getAgent(targetSubjectID: String): AgentsMappingResponse = {
    lazy val repositoryPersistenceActor = ActorLocator.repositoryPersistenceActor
    val getAgentsMapMessage = GetAgentsMapMessage(Seq(targetSubjectID))
    val newAgentsMapFuture = (repositoryPersistenceActor ?? getAgentsMapMessage).mapTo[AgentsMappingResponse]
    val newAgentsMap = Await.result(newAgentsMapFuture, (4 seconds))
    for ((subjectId, agents) <- newAgentsMap.possibleAgents) {
      tempAgentsMap += subjectId -> agents.head
    }
    newAgentsMap
  }

  /**
   * returns true or false if for the condition messageQueue.size < INPUT_POOL_SIZE
   * return true if INPUT_POOL_SIZE is -1 (no limit set)
   */
  private def spaceAvailableInMessageQueue(subjectID: SubjectID, messageType: MessageType): Boolean = {
    log.debug("Checking for queue space!")
    // get or create the message queue
    val messageQueue =
      messageQueueMap.getOrElseUpdate(
        (subjectID, messageType),
        Queue[SubjectToSubjectMessage]())

    log.debug("Limit: " + INPUT_POOL_SIZE + "; current size: " + messageQueue.size)

    // check the queue size
    if (messageQueue.size < INPUT_POOL_SIZE || INPUT_POOL_SIZE < 0) {
      true
    } else {
      false
    }
  }

  /**
   * Enqueue a message, add it to the correct queue
   */
  private def enqueueMessage(message: SubjectToSubjectMessage) = {
    val now = new Date
    val dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
    val date = dateFormat.format(now)
    val element = (date, message.messageContent.toString, "from", message.from)
    messageInformation = element :: messageInformation
    // get or create the message queue
    val messageQueue =
      messageQueueMap.getOrElseUpdate(
        (message.from, message.messageType),
        Queue[SubjectToSubjectMessage]())

    messageQueue.enqueue(message)
    log.debug("message has been queued!")
    // current messageQueue's size
    spaceAvailableInMessageQueue(message.from, message.messageType);
  }

  /**
   * enables previously received message
   */
  private def enableMessage(message: SubjectToSubjectMessage): Boolean = {
    // get or create the message queue
    val newMessageQueue = Queue[SubjectToSubjectMessage]()
    val messageQueue =
      messageQueueMap.getOrElseUpdate(
        (message.from, message.messageType),
        Queue[SubjectToSubjectMessage]())

    //loop over queue and enable message if found
    var counter = 0
    for (element <- messageQueue) {
      if (element.messageID == message.messageID && !element.enabled) {
        //element.enabled = true
        //element.copy(enabled = true)
        val newElement = element.copy(enabled = true)
        newMessageQueue.enqueue(newElement)
        counter = counter + 1
      }
    }
    messageQueueMap = messageQueueMap - ((message.from, message.messageType))
    messageQueueMap += ((message.from, message.messageType)) -> newMessageQueue

    if (counter != 0) {
      true
    } else {
      false
      //throw new exception
    }
  }

  /**
   * Enqueue a message in the overflow queue,
   */
  private def enqueueOverflowMessage(message: SubjectToSubjectMessage) = {
    // get or create the message queue
    val messageOverflowQueue =
      messageOverflowQueueMap.getOrElseUpdate(
        (message.from, message.messageType),
        Queue[(ActorRef, SubjectToSubjectMessage)]())

    messageOverflowQueue.enqueue((sender, message))
    log.debug("message has been queued to overflow queue!")
  }


  private def dequeueMessage(key: (SubjectID, MessageType)): SubjectToSubjectMessage = {
    log.debug("Dequeue message from normal queue! key: " + key)
    val tempQueue = Queue[SubjectToSubjectMessage]()
    for (i: Int <- 1 to messageQueueMap(key).size) {
      var msg = messageQueueMap(key).dequeue()
      if (!msg.enabled) {
        tempQueue.enqueue(msg)
      } else {
        messageQueueMap(key) = tempQueue ++ messageQueueMap(key)
        //enabled message has been found and removed from the queue
        //copy message from the overflow to the main queue which has space again
        if (spaceAvailableInMessageQueue(key._1, key._2)) {
          if ((messageOverflowQueueMap contains key) && messageOverflowQueueMap(key).size > 0) {
            log.debug("Dequeue message from overflow queue! size: " + messageOverflowQueueMap(key).size)
            var msg_from_overflow = messageOverflowQueueMap(key).dequeue()
            enqueueMessage(msg_from_overflow._2);

            //inform sender, that his message has been moved from overflow to the normal queue and is whaiting for enabed message
            msg_from_overflow._1 !! Stored(msg_from_overflow._2.messageID)
          } else {
            log.debug("No message in overflow queue!")
          }
        }
        return msg
      }
    }
    null
  }

  def reserve(message: SubjectToSubjectMessage, receiver: ActorRef): Unit = {
    val newSubjectToSubjectMsg = message.copy(messageContent = TextContent(""), enabled = true)
    val currentMessageId = message.messageID
    if (reserveMessageAndReceiver.contains(currentMessageId)) {
      reserveMessageAndReceiver(currentMessageId)
    } else {
      reserveMessageAndReceiver += currentMessageId ->(receiver, newSubjectToSubjectMsg)
    }
  }

  def processCloseIP(id: Int) {
    getState(id) match {
      case s: CloseIPState => {
        log.debug("InputPool will stop to receive ChannelID {}'s message.", closeIPMap(s.id))

        val processingChannelID = closeIPMap(s.id)
        processingChannelID match {
          case (subj, msg) if (subj.equals("") && msg.equals("")) => {
            isCloseIP = false
          }
          case (subj, msg) => {
            isCloseIP = true
            if (!currentCloseIPChannelID.contains((subj, msg))) {
              currentCloseIPChannelID.append((subj, msg))
            }
          }
        }
        changeState(id)
      }
      case _ => {
        log.debug("InputPool will be processed, if and only if the current state is closeIPState!")
      }
    }
  }

  def processOpenIP(id: Int) {
    getState(id) match {
      case s: OpenIPState => {
        log.debug("InputPool will  receive ChannelID {}'s message.", openIPMap(s.id))
        val openingIPChannelID = openIPMap(s.id)
        openingIPChannelID match {
          case (subj, msg) if (openingIPChannelID._1.equals("") && openingIPChannelID._2.equals("")) => {
            log.debug("InputPool has no change!")
          }

          case (subj, msg) if (openingIPChannelID._1.equals("##all##") || openingIPChannelID._2.equals("##all##")) => {
            log.debug(" InputPool will receive all messages!")
            currentCloseIPChannelID.clear()
            blockedSendStatesMap.foreach(bss => {
              bss._2 !! ReopenFromServiceHost(bss._1)
            })
          }

          case (subj, msg) if ((!openingIPChannelID._1.equals("")) && openingIPChannelID._2.equals("")) => {
            log.debug("InputPool will receive message from Subject: {} ", subj)
            val removeList = ListBuffer[ChannelID]()
            currentCloseIPChannelID.foreach(channelId => {
              if (channelId._1.equals(subj)) {
                removeList.append(channelId)
              }
            })

            currentCloseIPChannelID = currentCloseIPChannelID -- (removeList)

            blockedSendStatesMap.foreach(bss => {
              // unblock sendState
              if (bss._1._1.equals(subj)) {
                bss._2 !! ReopenFromServiceHost(bss._1)
              }
            })

          }

          case (subj, msg) if (openingIPChannelID._1.equals("") && (!openingIPChannelID._2.equals(""))) => {
            log.debug("InputPool will receive message from MessageType: {}", msg)
            var removeList = ListBuffer[ChannelID]()
            currentCloseIPChannelID.foreach(channelId => {
              if (channelId._2.equals(msg)) {
                removeList.append(channelId)
              }
            })
            currentCloseIPChannelID = currentCloseIPChannelID -- removeList

            blockedSendStatesMap.foreach(bss => {
              if (bss._1._2.equals(msg)) {
                bss._2 !! ReopenFromServiceHost(bss._1)
              }
            })
          }

          case (subj, msg) if (!openingIPChannelID._1.equals("") && (!openingIPChannelID._2.equals(""))) => {
            log.debug("InputPool will receive message from Subject: {}, MessageType: {}", subj, msg)
            currentCloseIPChannelID = currentCloseIPChannelID - ((subj, msg))

            blockedSendStatesMap.foreach(bss => {
              if (bss._1._1.equals(subj) && bss._1._2.equals(msg)) {
                bss._2 !! ReopenFromServiceHost(bss._1)
              }
            })
          }
        }

        if (currentCloseIPChannelID.isEmpty) {
          isCloseIP = false
        }

        changeState(id)
      }
      case _ => {
        log.debug("InputPool will be processed, if and only if the current state is openIPState!")
      }
    }

  }

  def addState(id: Int) {
    log.debug("ActivateState will activate a state {} . ", getState(activateStateMap(id)))
    currentStatesMap += activateStateMap(id) -> getState(activateStateMap(id))
    changeState(id)
    getState(activateStateMap(id)).process() // activate
  }

  def killState(id: Int) {
    // delete the state which the ActivateState activates.
    if (currentStatesMap.contains(deactivateStateMap(id))) {
      currentStatesMap = currentStatesMap - deactivateStateMap(id)
    } else {
      log.debug("The state which needs to be deactivated does not exist !")
    }
    changeState(id)
  }

  def isObserverState(id: Int) {
    if (observerStatesMap.contains(id)) {
      if (observerStatesMap(id) > 1) {
        // hang up the states which are in currentStatesMap except self
        for (elem <- currentStatesMap if (elem._1 != id)) yield {
          suspendStatesMap += elem._1 -> elem._2
          currentStatesMap - elem._1
        }
      }
    } else {
      log.debug("This ReceiveState is not observer.")
    }
  }

  def isNormalRunning(id: Int) {
    val future = self ?? StateManager(id)
    val isNormalRunning = Await.result(future, (0.5 seconds)).asInstanceOf[Boolean]
    if (isNormalRunning) {
      changeState(id)
    } else log.debug("the current state is not normal running !")
  }

  /*
  ActionState's process() is custom condition
   */

  //$ACTIONSTATESIMPLEMENTATION$//
}
