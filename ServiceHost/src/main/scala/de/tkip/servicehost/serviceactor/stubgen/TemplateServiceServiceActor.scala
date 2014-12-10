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
import de.tkip.vasec.{VROI, VPOIGroup}
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

class TemplateServiceServiceActor extends ServiceActor {
  override protected val INPUT_POOL_SIZE: Int = 20
  override protected val serviceID: ServiceID = "Subj2:ff9bacbf-bb0c-4316-9cd5-5328e1246557"
  override protected val subjectID: SubjectID = "Subj2:ff9bacbf-bb0c-4316-9cd5-5328e1246557"
  protected val serviceInstanceMap = Map[SubjectID, ServiceActorRef]()
  val tempAgentsMap = collection.mutable.Map[String, ProcessInstanceActor.Agent]()
  var from: SubjectID = null
  var processInstanceIdentical: String = ""
  var managerURL: String = ""
  val startNodeIndex: String = "2"


  override protected def states: List[State] = List(
    ExitState(0,null,Map(),Map(),null,null),
    prepareROImessage(5,"exitcondition",Map(),Map("5" -> 4),"prepare ROI message",""),
    SendState(1,"exitcondition",Map("m3" -> Target("Subj2:6ade7atg-d3c2-46df-a3d0-c7f328e9af457",-1,-1,false,"")),Map("m3" -> 5),"",""),
    internalaction(6,"exitcondition",Map(),Map("6" -> 3),"internal action",""),
    ReceiveState(2,"exitcondition",Map("m10" -> Target("Subj2:6ade7atg-d3c2-46df-a3d0-c7f328e9af457",-1,-1,false,"")),Map("m10" -> 6),"receive configuration",""),
    preparePOImessage(3,"exitcondition",Map(),Map("3" -> 1),"prepare POI message",""),
    SendState(4,"exitcondition",Map("m4" -> Target("Subj2:6ade7atg-d3c2-46df-a3d0-c7f328e9af457",-1,-1,false,"")),Map("m4" -> 0),"","")
  )

//  private val branchMap: Map[Int, List[Tuple2[String, Int]]] = Map(
//    //$EMPTYBRANCHMAP$//
//  )

  // start with first state
  def getStartState(): State = {
    getState("2".toInt)
  }

  private val messages: Map[MessageType, MessageText] = Map(
    "m7" -> "m7",
    "m2" -> "m2",
    "ROIs" -> "m4",
    "Destination Points" -> "m8",
    "start service" -> "m1",
    "m9" -> "m9",
    "Routes" -> "m6",
    "configuration" -> "m10",
    "POIs" -> "m3",
    "StartEnd" -> "m5"
  )

  private val inputPool: scala.collection.mutable.Map[Tuple2[MessageType, SubjectID], Queue[SubjectToSubjectMessage]] = scala.collection.mutable.Map()
  // Subject default values
  private val variablesOfSubject = scala.collection.mutable.Map[String, Variable]()
  private var target = -1
  protected var messageContent: String = "" // will be used in getResult

  var pois: Seq[VPOIGroup] = Nil
  var rois: Seq[VROI] = Nil

  override def reset = {
    pois = Nil
    rois = Nil
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
            message = inputPool(key).dequeue
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
    log.debug("processSendState")
    val sTarget = if (state.targets.size > 1) {
      state.targets(branchCondition).target
    } else state.targets.head._2.target
    val targetSubjectID = sTarget.subjectID
    var serviceInstance: ServiceActorRef = null
    val messageID = 100 //TODO change if needed
    val messageType = state.targetIds.head._1
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
    // detemine receiver
    if (targetSubjectID.contains(from)) {
     // sender !! message
      manager !! message
    } else {
      if (!serviceInstanceMap.contains(targetSubjectID)) {
        if (tempAgentsMap.contains(targetSubjectID)) {
          if (tempAgentsMap(targetSubjectID).address.toUrl == managerURL) {
            // manager !! message // return to Backend
            receiver = manager
          } else {
            // if targetSubjectID is in tempAgentsMap, this means the service has been created. The SubjectToSubject can be sent to serviceActorManager
            val agentsAddr = tempAgentsMap(targetSubjectID).address.toUrl
            val path = "akka.tcp" + "://sbpm" + agentsAddr + "/user/" + BackendActorLocator.subjectProviderManagerActorName
            val agentManagerSelection = context.actorSelection(path)
            //agentManagerSelection !! message // serviceActorManager will forward message to correspond service.
            val future = agentManagerSelection ?? AskForServiceInstance(processInstanceIdentical, targetSubjectID) // get targetServiceInstance
            val serviceInstance = Await.result(future, (5 seconds)).asInstanceOf[ActorRef]
            //serviceInstance !! message
            receiver = serviceInstance
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
                //serviceInstance !! message
                receiver = serviceInstance
              } else {
                // TODO exception or log?
                throw new Exception("processInstance Created failed for " +
                  targetSubjectID + "\nreason" + processInstanceCreated)
              }
          }
        }
      } else {
        serviceInstance = serviceInstanceMap(targetSubjectID)
        //serviceInstance !! message
        receiver = serviceInstance
      }
    }

//    if (state.variableId == null) {
//      println("333333333333333333333333333" + message)
//      receiver !! message //  Normal SubjectToSubjectMessage
//    } else if (state.variableId != null && variablesOfSubject.contains(state.variableId)) {
//      val newMessage = message.copy(messageContent = variablesOfSubject(state.variableId).toString())
//      receiver !! newMessage // send Variable
//
//    } else
//      log.error("wrong message!!!!")

  }

  def stateReceive = {
    case message: SubjectToSubjectMessage => {
      // TODO forward /set variables?
      log.debug("receive message: " + message)
      from = message.from
      storeMsg(message, sender)

      state match {
        case rs: ReceiveState => {
          if (rs.variableId != null) {
            if (variablesOfSubject.contains(rs.variableId)) {
              variablesOfSubject(rs.variableId).addMessage(sender, message)
            } else {
              variablesOfSubject += (rs.variableId) -> Variable(rs.variableId)
              variablesOfSubject(rs.variableId).addMessage(sender, message)
            }
          }

          processMsg()
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

    case variable: Variable => {

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
        // TODO: state könnte null sein, oder auch der alte..
      }
    }
  }

  def changeState(Index: Int) {
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
        // TODO: state könnte null sein, oder auch der alte..
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
      case message => log.warning("unable to store message: " + message)
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

  
  case class prepareROImessage(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State("action", id, exitType, targets, targetIds, text, variableId) {

    def process()(implicit actor: ServiceActor) {
      actor.setMessage("112233445566778899") //TODO set message
      actor.changeState()

    	}
  }
  case class internalaction(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State("action", id, exitType, targets, targetIds, text, variableId) {

    def process()(implicit actor: ServiceActor) {
      actor.setMessage("") //TODO set message
      actor.changeState()

    	}
  }
  case class preparePOImessage(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State("action", id, exitType, targets, targetIds, text, variableId) {

    def process()(implicit actor: ServiceActor) {
      actor.setMessage("9999988888887777777666666") //TODO set message
      actor.changeState()

    	}
  }
}
