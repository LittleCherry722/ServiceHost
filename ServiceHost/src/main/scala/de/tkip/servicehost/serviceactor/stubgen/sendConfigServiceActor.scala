package de.tkip.servicehost.serviceactor.stubgen

import akka.actor.Actor
import de.tkip.sbpm.application.ProcessInstanceActor
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
import scala.collection.mutable.{ListBuffer, Queue, Map}
import de.tkip.sbpm.application.subject.misc.Rejected
import scala.concurrent.{ExecutionContext, Await}
import ExecutionContext.Implicits.global

import scala.concurrent.Await

class sendConfigServiceActor extends ServiceActor {
  override protected val INPUT_POOL_SIZE: Int = 20
  override protected val serviceID: ServiceID = "Subj3:738bfec0-40a6-44b7-8e8f-7f2449e2f1b4"
  override protected val subjectID: SubjectID = "Subj3:738bfec0-40a6-44b7-8e8f-7f2449e2f1b4"
  protected val serviceInstanceMap = Map[SubjectID, ServiceActorRef]()
  val tempAgentsMap = collection.mutable.Map[String, ProcessInstanceActor.Agent]()
  var from: SubjectID = null
  var processInstanceIdentical: String = ""
  var managerURL: String = ""
  val startNodeIndex: String = "0"
  var receivedMessageType: String = ""
  var continue = false

  override protected def states: List[State] = List(
    ReceiveState(0,"exitcondition",Map("m1" -> Target("Subj2:f1358b4e-ffeb-49e8-826f-269c3f35a304",-1,-1,false,"")),Map("m1" -> 1),"receiveCommand",""),SendState(1,"exitcondition",Map("m2" -> Target("Subj4:f161330e-6a7c-4ba3-81d9-cd4e3bf49cca",-1,-1,false,"")),Map("m2" -> 2),"sendConfig",""),ReceiveState(2,"exitcondition",Map("m3" -> Target("Subj4:f161330e-6a7c-4ba3-81d9-cd4e3bf49cca",-1,-1,false,"")),Map("m3" -> 3),"receiveConfig1",""),ReceiveState(3,"exitcondition",Map("m4" -> Target("Subj4:f161330e-6a7c-4ba3-81d9-cd4e3bf49cca",-1,-1,false,"")),Map("m4" -> 4),"receiveConfig2",""),ExitState(4,null,Map(),Map(),null,null)
  )

  // different received messageType -> different outgoing messageType like: m1 -> m2, m3 -> m4
  val inputAndOutputMap: Map[String, String] = Map(

  )

  // start with first state
  def getStartState(): State = {
    getState("0".toInt)
  }

  private val messages: Map[MessageType, MessageText] = Map(
    "Start" -> "m1","Config" -> "m2","ConfigPOI" -> "m3","ConfigROI" -> "m4"
  )
  private val variablesOfSubject: Map[String, String] = Map(
    
  )

  private val inputPool: scala.collection.mutable.Map[Tuple2[MessageType, SubjectID], Queue[SubjectToSubjectMessage]] = scala.collection.mutable.Map()
  // Subject default values
  private val collectorOfMessage = scala.collection.mutable.Map[Tuple2[String, Int], ListBuffer[SubjectToSubjectMessage]]()
  private val variablesMap = scala.collection.mutable.Map[String, ListBuffer[Variable]]()
  private val inputPoolVariable = Map[Tuple2[String, SubjectID], Queue[Variable]]()
  private val sendingVariable = Map[String, Variable]()
  private var target = -1
  private var messageContent: String = "" // will be used in getResult

  override def reset = {
    // TODO: reset custom properties
    super.reset
  }

  def processMsg(msg: Any) {
    log.debug("PROCESS MESSAGE")
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
              this.messageContent = message.messageContent

              this.branchCondition = message.messageType
              receivedMessageType = message.messageType

              rs.handle(message) // calls changeState
            }
            else log.info("ReceiveState could not find any matching message. ReceiveState will wait until it arrivies")
          }

          case msg: Variable => {
            println("========= match Variable =========")
            val key = (msg.vId, msg.from)
            var variable: Variable = null
            if (inputPoolVariable.contains(key)) {
              variable = inputPoolVariable(key).dequeue()
            }
            if (variable != null) {
              this.branchCondition = rs.targets.keys.head
              rs.handle(msg)
            } else
              log.info("ReceiveState could not find any matching message. ReceiveState will wait until it arrives")
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
    val message = SubjectToSubjectMessage(
      messageID,
      processID,
      1,
      subjectID,
      target,
      messageType,
      "config",
      None,
      fileInfo,
      Some(processInstanceIdentical)
    )
    if (state.variableId == "") {

      determineReceiver(targetSubjectID, message)

    } else {
      if (!sendingVariable.contains(state.variableId)) {
        vEncapsulation(state.variableId, ListBuffer(message))
      }
      determineReceiver(targetSubjectID, sendingVariable(state.variableId))
    }
  }

  def stateReceive = {
    case message: SubjectToSubjectMessage => {
      log.debug("receive message: " + message)
      from = message.from
      storeMsg(message, sender)

      state match {
        case rs: ReceiveState => {
          if (rs.variableId != "") { // collect all subjectToSubjectMessage and create a new Variable, the depth is 0.
            if (variablesMap.contains(rs.variableId)) {
             variablesMap(rs.variableId).head.messagesSet.append(message)
            } else {
              variablesMap += rs.variableId -> ListBuffer(Variable(rs.variableId, 0, getSubjectID(), ListBuffer(message), null))
            }
          }
          processMsg(message)
        }
        case _ =>
          log.info("message will be handled when state changes to ReceiveState. Current state is: " + state)
      }
    }

    case subjectToSubjectVariable: Variable => {
      log.debug("receive Variable:  " + subjectToSubjectVariable)
      storeMsg(subjectToSubjectVariable, sender())
      state match {
        case rs: ReceiveState => {
          if (variablesMap.contains(subjectToSubjectVariable.vId)) {
            variablesMap(subjectToSubjectVariable.vId).append(subjectToSubjectVariable)
          } else {
            variablesMap += subjectToSubjectVariable.vId -> ListBuffer(subjectToSubjectVariable)
          }
          println("all received variable:   " + variablesMap)
          processMsg(subjectToSubjectVariable)
        }
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
      case message: Variable => {
        log.debug("store variable")
        val key = (message.vId, message.from)
        if (inputPoolVariable.contains(key)) {
          inputPoolVariable(key).enqueue(message)
        } else {
          inputPoolVariable(key) = Queue(message)
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

  def determineReceiver(targetSubjectID: String, msg: Any): Unit = {
    if (targetSubjectID.contains(from)) {
      if (tempAgentsMap(targetSubjectID).address.toUrl == managerURL) {
        receiver = manager
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
          // targetSubjectId isn't in tempAgentsMap.The service need to ask repository about targetAgent.
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

  /*
  first, merge different variables with same depth and variableId.
  second, merge every variable's lastvariable.
  third, m : 1.
   */
  def vMerge(key: String): Variable = {
    val resultOfMerge = ListBuffer[Variable]()
    val newMessageList = ListBuffer[SubjectToSubjectMessage]()
    val vDepth = variablesMap(key).head.depth
    if (vDepth != 0) {
      variablesMap(key).foreach(variable => {
        variable.lastVariable.foreach(v => {
          resultOfMerge.append(v)
        })
      })
      Variable(key, vDepth, null, null, resultOfMerge)
    } else {
      variablesMap(key).foreach(variable => {
        variable.messagesSet.foreach(msg => {
          newMessageList.append(msg)
        })
      })
      Variable(key, vDepth, null, newMessageList, null)
    }


  }

  def mergeMessage(variable: Variable): ListBuffer[SubjectToSubjectMessage] = {
    val resultOfMerge = ListBuffer[SubjectToSubjectMessage]()
    var vDepth = variable.depth
    var upperList = ListBuffer[Variable]()
    var lowerList = ListBuffer[Variable]()
    lowerList.append(variable)
    while (vDepth != 0) {
      upperList = lowerList
      lowerList.clear()
      vDepth = upperList.head.depth
      upperList.foreach(v => {
        lowerList.++=(v.lastVariable)
      })
    }

    lowerList.foreach(v => {
      resultOfMerge ++= (v.messagesSet)
    })
    resultOfMerge
  }

  /*
      split variable (1 : m)

   */
  def vSplit(vr: Variable): List[Variable] = {
    val splitVariable = ListBuffer[Variable]()
    if (vr.depth != 0) {
      vr.lastVariable.foreach(v => {
        val tempSplitVariable = Variable(vr.vId, vr.depth, vr.from, null, ListBuffer(v))
        splitVariable.append(tempSplitVariable)
      })
    } else {
      vr.messagesSet.foreach(msg => {
        val tempSplitVariable = Variable(vr.vId, vr.depth, vr.from, ListBuffer(msg), null)
        splitVariable.append(tempSplitVariable)
      })
    }
    splitVariable.toList

  }

  /*
  different variables
  variableA - variableB
   */
  def vDifference(variableA: Variable, variableB: Variable): List[String] = {
    var differentVariable: List[Variable] = Nil
    var differentMessage: List[SubjectToSubjectMessage] = Nil

    if (variableA.vId == variableB.vId && variableA.depth == variableB.depth) {
      var messageContentListA: List[String] = Nil
      var messageContentListB: List[String] = Nil
      mergeMessage(variableA).foreach(msg => messageContentListA = msg.messageContent :: messageContentListA)
      mergeMessage(variableB).foreach(msg => messageContentListB = msg.messageContent :: messageContentListB)
      messageContentListA.diff(messageContentListB)
    } else {
      log.debug("The two variables can not use the manipulation")
      null
    }

  }

  /*
  select subvariables
   */
  def vSelection(variables: Variable) = {
    //custom condition,
    // this is just an example
    if (variables.depth != 0) {
      // select subVariables from lastVariable
      variables.lastVariable.toList.drop(2)
    } else {
      // according to condition select different subMessageSet
      variables.messagesSet.toList
    }
  }


  /*
  extract  variables, reduce variableDepth, 1 : m
   */

  def vExtraction(variables: Variable) = {
    if (variables.depth != 0) {
      // return lastVariables
      variables.lastVariable.toList
    } else {
      variables.messagesSet.toList // return SubjectToSubjectMessage
    }
  }

  /*
encapsulate variable,increase variableDepth, m : 1
   */
  def vEncapsulation(variables: List[Variable]): Variable = {
    val lastVariables = ListBuffer[Variable]()
    variables.foreach(v => {
      lastVariables.append(v)
    })
    sendingVariable += variables.head.vId -> Variable(variables.head.vId, variables.head.depth + 1, getSubjectID(), null, lastVariables)
    sendingVariable(variables.head.vId)
  }

  /*
  encapsulate variable, the depth is 0.
   */
  def vEncapsulation(variableId: String, messageSet: ListBuffer[SubjectToSubjectMessage]): Variable = {
    sendingVariable += variableId -> Variable(variableId, 0, getSubjectID(), messageSet, null)
    sendingVariable(variableId)
  }

  
}
