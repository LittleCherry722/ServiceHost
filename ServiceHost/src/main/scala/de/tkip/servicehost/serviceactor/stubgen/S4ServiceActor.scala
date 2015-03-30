package de.tkip.servicehost.serviceactor.stubgen

import akka.actor.Actor
import de.tkip.sbpm.application.ProcessInstanceActor
import de.tkip.sbpm.application.ProcessInstanceActor._
import de.tkip.sbpm.{ActorLocator => BackendActorLocator}
import de.tkip.sbpm.repository.RepositoryPersistenceActor.{AgentsMappingResponse, GetAgentsMapMessage}
import de.tkip.servicehost.ActorLocator._
import de.tkip.servicehost.serviceactor.ServiceActor
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
import scala.collection.mutable.{ListBuffer, Queue, Map}
import de.tkip.sbpm.application.subject.misc.Rejected
import scala.concurrent.{ExecutionContext, Await}
import ExecutionContext.Implicits.global

import scala.concurrent.Await

class S4ServiceActor extends ServiceActor {
  override protected val INPUT_POOL_SIZE: Int = "100".toInt
  override protected val serviceID: ServiceID = "Subj5:787528ad-3d4e-4582-b63d-3b3921a100cc"
  override protected val subjectID: SubjectID = "Subj5:787528ad-3d4e-4582-b63d-3b3921a100cc"
  protected val serviceInstanceMap = Map[SubjectID, ServiceActorRef]()
  val tempAgentsMap = collection.mutable.Map[String, ProcessInstanceActor.Agent]()
  var from: SubjectID = null
  var processInstanceIdentical: String = ""
  var managerURL: String = ""
  val startNodeIndex: String = "0"
  var receivedMessageType: String = ""
  var variablesOfService = collection.mutable.Map[String, ListBuffer[SubjectToSubjectMessage]]()
  var sendingVariables = collection.mutable.Map[String, Variable]()

  override protected def states: List[State] = List(
    ReceiveState(0,"exitcondition",Map("m1" -> Target("Subj4:dcffa542-f1e5-493c-b734-c173791122aa",-1,-1,false,"")),Map("m1" -> 1),"new",""),SendState(1,"exitcondition",Map("m2" -> Target("Subj2:b7e7dc76-8e1f-4c34-81a5-66d9c5b8e631",-1,-1,false,"")),Map("m2" -> 2),"",""),ExitState(2,null,Map(),Map(),null,null)
  )

  // different received messageType -> different outgoing messageType like: m1 -> m2, m3 -> m4
  val inputAndOutputMap: Map[String, String] = Map(

  )

  // start with first state
  def getStartState(): State = {
    getState("0".toInt)
  }

  private val messages: Map[MessageType, MessageText] = Map(
    "msg1" -> "m1","msg2" -> "m2"
  )
  private val variablesOfSubject: Map[String, String] = Map(
    
  )

  private val inputPool: scala.collection.mutable.Map[Tuple2[MessageType, SubjectID], Queue[SubjectToSubjectMessage]] = scala.collection.mutable.Map()
  private var target = -1
  private var messageContent: String = "" // will be used in getResult

  def stateReceive = {

    case message: SubjectToSubjectMessage => {
      log.debug("receive message: " + message)
      from = message.from
      storeMsg(message, sender)
      state match {
        case rs: ReceiveState => {
          if (rs.variableId != "") {
            var variableName = ""
            for ((vName, vType) <- variablesOfSubject) {
              if (vType == rs.variableId) {
                variableName = vName
              }
            }
            addMessage(message, variableName) // collect SubjectToSubjectMessage
          }
          processMsg(message)
        }
        case _ =>
          log.info("message will be handled when state changes to ReceiveState. Current state is: " + state)
      }
    }

    case msg: UpdateServiceInstanceDate => {
      for ((subjectId, agents) <- msg.agentsMap) {
        tempAgentsMap += subjectId -> agents
      }
      processInstanceIdentical = msg.processInstanceIdentical
      managerURL = msg.managerUrl
    }

    case x: Stored => {
      log.debug(" message has been stored.  " + x)
    }

    case x: Rejected => {
      log.debug("  Receiver's inputPool is full. " + x)
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
        } else {
          state = getState(state.targetIds.head._2)
          log.debug("changeState: new state: " + state)
        }
        state.process()
      }
    }
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
      case _ => log.warning("unable to store message: " + message)
    }
  }

  def getBranchIDforType(messageType: String): MessageText = {
    messages(messageType)
  }

  def getDestination(): ActorRef = {
    manager
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

  def getResult(msg: String): String = {
    // handle the messageContent
    msg
  }

  override def reset = {
    // TODO: reset custom properties
    super.reset
  }

  def processMsg(msg: Any) {
    log.debug("PROCESS RECEIVED MESSAGE")
    state match {
      case rs: ReceiveState => {
        msg match {
          case msg: SubjectToSubjectMessage => {
            var message: SubjectToSubjectMessage = null
            for ((branch, target) <- state.targets) {
              val messageType: MessageType = branch // received messageType
              val fromSubjectID: SubjectID = target.target.subjectID
              val key = (messageType, fromSubjectID)
              log.debug("processMsg: key = " + key)

              if (inputPool.contains(key) && inputPool(key).length > 0) {
                message = inputPool(key).dequeue
              }
            }

            log.debug("processMsg: message = " + message)

            if (message != null) {
              this.messageContent = message.messageContent.toString

              this.branchCondition = message.messageType
              receivedMessageType = message.messageType

              rs.handle(message) // calls changeState
            }
            else log.info("ReceiveState could not find any matching message. ReceiveState will wait until it arrivies")
          }
          case _ => log.debug("receive other messageType !!!")
        }
      }
      case _ =>
        log.info("unable to handle message now, needs to be in ReceiveState. Current state is: " + state)
    }
  }

  def processSendState() {
    //find or create the target service actor
    log.debug("=== PROCESS SEND STATE ===")
    val sTarget = if (state.targets.size > 1) {
      state.targets(inputAndOutputMap(receivedMessageType)).target
    } else state.targets.head._2.target
    val targetSubjectID = sTarget.subjectID
    var serviceInstance: ServiceActorRef = null
    val messageID = 100 //TODO change if needed
    val messageType = state.targetIds.head._1
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
    val message = SubjectToSubjectMessage(
      messageID,
      processID,
      1,
      subjectID,
      target,
      messageType,
      msgContent,
      None,
      fileInfo,
      Some(processInstanceIdentical)
    )
    if (state.variableId != "") {
      val newMsgContent = MessageSet(sendingVariables(state.variableId))
      message.copy(messageContent = newMsgContent)
    }
    println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&")
    println(message)
    determineReceiver(targetSubjectID, message)
  }

  def determineReceiver(targetSubjectID: String, msg: SubjectToSubjectMessage): Unit = {
    if (targetSubjectID.contains(from)) {
      if (tempAgentsMap(targetSubjectID).address.toUrl == managerURL) {
        receiver = manager // directly return to Backend
        receiver !! msg
      }
      else {
        receiver = sender
        receiver !! msg
      }
    } else {
      if (!serviceInstanceMap.contains(targetSubjectID)) {
        if (tempAgentsMap.contains(targetSubjectID)) {
          if (tempAgentsMap(targetSubjectID).address.toUrl == managerURL) {
            receiver = manager
            receiver !! msg
          } else {
            // if targetSubjectID is in tempAgentsMap, this means the service has been created. The SubjectToSubject can be sent to serviceActorManager
            val agentsAddr = tempAgentsMap(targetSubjectID).address.toUrl
            val path = "akka.tcp" + "://sbpm" + agentsAddr + "/user/" + BackendActorLocator.subjectProviderManagerActorName
            val agentManagerSelection = context.actorSelection(path)
            //agentManagerSelection !! message // serviceActorManager will forward message to correspond service.
            val future = agentManagerSelection ?? AskForServiceInstance(processInstanceIdentical, targetSubjectID) // get targetServiceInstance
            val serviceInstance = Await.result(future, (5 seconds)).asInstanceOf[ActorRef]
            receiver = serviceInstance
            receiver !! msg
          }
        } else {
          // If targetSubjectId isn't in tempAgentsMap, this means the targetServiceInstance is not created. The service need to ask repository about targetAgent.
          lazy val repositoryPersistenceActor = ActorLocator.repositoryPersistenceActor
          val getAgentsMapMessage = GetAgentsMapMessage(Seq(targetSubjectID))
          val newAgentsMapFuture = (repositoryPersistenceActor ?? getAgentsMapMessage).mapTo[AgentsMappingResponse]
          val newAgentsMap = Await.result(newAgentsMapFuture, (4 seconds))
          for ((subjectId, agents) <- newAgentsMap.possibleAgents) {
            tempAgentsMap += subjectId -> agents.head
          }

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
        receiver !! msg
      }
    }
  }

  def addMessage(msg: SubjectToSubjectMessage, variableName: String): Unit = {
    if (variablesOfService.contains(variableName)) {
      variablesOfService(variableName).append(msg)
    } else {
      variablesOfService += variableName -> ListBuffer(msg)
    }
  }

  def vMerge(vName: String): Variable = {
    var newMsgContent = Set[Message]()
    var vDepth = 0 //todo
    if (variablesOfService.contains(vName)) {
      variablesOfService(vName).foreach(message => message.messageContent match {
        case msgContent: TextContent => log.debug("TextContent can't be merged!")
        case msgContent: MessageSet => {
          newMsgContent = newMsgContent ++ msgContent.messages
        }
        case _ => log.debug("Other Types will be processed in future!")
      })
    }
    newMsgContent
  }

  def vSplit(variables: Variable): Array[Message] = {  //  type Variable = Set[Message]
    variables.toArray
  }

  def vSelection(variable: Variable): Unit = {
    // custom condition
    /*
    this is a just an example
     */

    variable.foreach(message => if (message.depth == 1) {
      message
    } else {
      message.depth
    })

  }

  def vDifference(variableA: Variable, variableB: Variable): Set[Message] = {
    if ((variableA.head.vName == variableB.head.vName) && (variableA.head.depth == variableB.head.depth)) {
      variableA -- variableB
    } else {
      null
    }
  }

  def vEncapsulation(variableName: String): Variable = {
    var newMessageSet = Set[Message]()
    variablesOfService(variableName).foreach(message => {
      val currentAgent = Agent(message.processID, tempAgentsMap(message.from).address, message.from)
      val currentChannel = Channel(subjectID, currentAgent)

      message.messageContent match {
        case msgContent: TextContent => {
          val currentMessage = Message(variableName, currentChannel, 1, message.messageType, message.messageContent)
          newMessageSet = newMessageSet + currentMessage // create a new MessageSet
        }

        case msgContent: MessageSet => {
          val currentMessage = Message(variableName, currentChannel, msgContent.messages.head.depth + 1, message.messageType, msgContent)
          newMessageSet = newMessageSet + currentMessage
        }
      }
    })
    newMessageSet // will be used in SubjectTOSubjectMessage
  }

  def vExtraction(variables: Variable, vDepth: Int): Unit = {
    if(variables.head.depth > vDepth){

    }else if (variables.head.depth == vDepth){
      variables
    }else{
      log.debug("Messages can't be extracted!")
    }

  }

  
}
