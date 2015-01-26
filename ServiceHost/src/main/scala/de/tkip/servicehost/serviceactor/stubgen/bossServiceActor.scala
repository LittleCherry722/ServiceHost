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

class bossServiceActor extends ServiceActor {
  override protected val INPUT_POOL_SIZE: Int = 20
  override protected val serviceID: ServiceID = "Subj8:a29d5f2c-3a15-4683-941c-1ceb16011822"
  override protected val subjectID: SubjectID = "Subj8:a29d5f2c-3a15-4683-941c-1ceb16011822"
  protected val serviceInstanceMap = Map[SubjectID, ServiceActorRef]()
  val tempAgentsMap = collection.mutable.Map[String, ProcessInstanceActor.Agent]()
  var from: SubjectID = null
  var processInstanceIdentical: String = ""
  var managerURL: String = ""
  val startNodeIndex: String = "0"
  var receivedMessageType: String = ""

  override protected def states: List[State] = List(
    ReceiveState(0, "exitcondition", Map("m2" -> Target("Subj7:3182f0e9-a0a6-4fab-b073-73267056bf23", -1, -1, false, "")), Map("m2" -> 1), "receive", ""),
    process(1, "exitcondition", Map(), Map("1" -> 2), "process", "v0"),
    SendState(2, "exitcondition", Map("m3" -> Target("Subj7:3182f0e9-a0a6-4fab-b073-73267056bf23", -1, -1, false, "")), Map("m3" -> 3), "", "v0"),
    ExitState(3, null, Map(), Map(), null, null)
  )

  // different received messageType -> different outgoing messageType like: m1 -> m2, m3 -> m4
  val inputAndOutputMap: Map[String, String] = Map(

  )

  // start with first state
  def getStartState(): State = {
    getState("0".toInt)
  }

  private val messages: Map[MessageType, MessageText] = Map(
    "Reply" -> "m1", "collectionResult" -> "m2", "checkResult" -> "m3"
  )
  private val variablesOfSubject: Map[String, String] = Map(
    "EinladungResult" -> "v0"
  )

  private val inputPool: scala.collection.mutable.Map[Tuple2[MessageType, SubjectID], Queue[SubjectToSubjectMessage]] = scala.collection.mutable.Map()
  // Subject default values
  // varialbesMap means the current subject collects all variable which from different subjects.
  private val variablesMap = scala.collection.mutable.Map[String, ListBuffer[Variable]]()
  private val inputPoolVariable = Map[Tuple2[String, SubjectID], Queue[Variable]]()
  // if a variable or a varialbeList has been already processed, it should be stored to sendingVariable. It will be sent to another subject.
  private val sendingVariable = Map[String, Variable]()
  private val vDepthMap = Map[String, Int]()
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
            val key = (msg.vName, msg.from)
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
    val message = SubjectToSubjectMessage(
      messageID,
      processID,
      1,
      subjectID,
      target,
      messageType,
      getMessage(),
      None,
      fileInfo,
      Some(processInstanceIdentical)
    )
    if (state.variableId == "") {
      // send normal SubjectToSubjectMessage
      determineReceiver(targetSubjectID, message)

    } else {
      var vrName = ""
      for ((vName, vType) <- variablesOfSubject) {
        if (vType == state.variableId)
          vrName = vName
      }
      if (!sendingVariable.contains(vrName)) {
        vEncapsulation(vrName, ListBuffer(message))
      }
      determineReceiver(targetSubjectID, sendingVariable(vrName))
    }
  }

  def stateReceive = {
    case message: SubjectToSubjectMessage => {
      log.debug("receive message: " + message)
      from = message.from
      storeMsg(message, sender)

      state match {
        case rs: ReceiveState => {
          if (rs.variableId != "") {
            // collect all subjectToSubjectMessage and create a new Variable, the depth is 0.
            var variableName = ""
            for ((vName, vType) <- variablesOfSubject) {
              if (vType == rs.variableId) {
                variableName = vName
              }
            }
            if (sendingVariable.contains(variableName)) {
              sendingVariable(variableName).messagesSet.append(message)
            } else {
              sendingVariable += variableName -> Variable(variableName, 0, getSubjectID(), ListBuffer(message), null)
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
      from = subjectToSubjectVariable.from
      storeMsg(subjectToSubjectVariable, sender())

      state match {
        case rs: ReceiveState => {
          if (variablesMap.contains(subjectToSubjectVariable.vName) && vDepthMap(subjectToSubjectVariable.vName) == subjectToSubjectVariable.depth) {
            variablesMap(subjectToSubjectVariable.vName).append(subjectToSubjectVariable)
            processMsg(subjectToSubjectVariable)
          } else if (variablesMap.contains(subjectToSubjectVariable.vName) && vDepthMap(subjectToSubjectVariable.vName) != subjectToSubjectVariable.depth) {
            log.error("Please check the process. The different depth variables can't be merged!")
          } else {
            variablesMap += subjectToSubjectVariable.vName -> ListBuffer(subjectToSubjectVariable)
            vDepthMap += subjectToSubjectVariable.vName -> subjectToSubjectVariable.depth
            processMsg(subjectToSubjectVariable)
          }
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
        val key = (message.vName, message.from)
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
  first, merge different variables with same depth and variableName.
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
      Variable(key, vDepth, null, null, resultOfMerge) // when the depth isn't 0, the messageSet always is null.
    } else {
      variablesMap(key).foreach(variable => {
        variable.messagesSet.foreach(msg => {
          newMessageList.append(msg)
        })
      })
      Variable(key, vDepth, null, newMessageList, null) // when the depth is 0, the lastVariable always is null.
    }


  }

  // merge all subjectToSubjectMessage which have the same variableName and the same depth.
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
        val tempSplitVariable = Variable(vr.vName, vr.depth, vr.from, null, ListBuffer(v))
        splitVariable.append(tempSplitVariable)
      })
    } else {
      vr.messagesSet.foreach(msg => {
        val tempSplitVariable = Variable(vr.vName, vr.depth, vr.from, ListBuffer(msg), null)
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

    if (variableA.vName == variableB.vName && variableA.depth == variableB.depth) {
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
    sendingVariable += variables.head.vName -> Variable(variables.head.vName, variables.head.depth + 1, getSubjectID(), null, lastVariables)
    sendingVariable(variables.head.vName)
  }

  /*
  encapsulate subjectToSubjectMessage, the default depth is 0.
   */
  def vEncapsulation(variableName: String, messageSet: ListBuffer[SubjectToSubjectMessage]): Variable = {
    sendingVariable += variableName -> Variable(variableName, 0, getSubjectID(), messageSet, null)
    sendingVariable(variableName)
  }

  def vReplace(variableList: ListBuffer[Variable],newVariable: Variable): Unit ={
    variableList.foreach(oldVariable => if(oldVariable.vName == newVariable.vName && oldVariable.depth == newVariable.depth && oldVariable.from == newVariable.from){
      oldVariable.messagesSet.clear()
      newVariable.messagesSet.foreach(msg => oldVariable.messagesSet.append(msg))
      oldVariable.lastVariable.clear()
      newVariable.lastVariable.foreach(v => oldVariable.lastVariable.append(v))
    })
  }


  case class process(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State("action", id, exitType, targets, targetIds, text, variableId) {

    def process()(implicit actor: ServiceActor) {
      val newMessageSet = variablesMap("Einladung").head.messagesSet.drop(1)
      var newVrName = ""
      for ((vName, vType) <- variablesOfSubject) {
        if (vType == state.variableId)
          newVrName = vName
      }
      vEncapsulation(newVrName, newMessageSet)
      actor.changeState()

    }
  }

}
