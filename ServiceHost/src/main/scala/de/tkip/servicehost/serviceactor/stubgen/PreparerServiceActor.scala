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
import de.tkip.vasec._
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

import de.tkip.vasec._
import de.tkip.vasec.VasecJsonProtocol._
import spray.json._

class PreparerServiceActor extends ServiceActor {
  override protected val INPUT_POOL_SIZE: Int = 20
  override protected val serviceID: ServiceID = "Subj2:c60d4f03-2110-4eeb-9db4-19db79ed5433"
  override protected val subjectID: SubjectID = "Subj2:c60d4f03-2110-4eeb-9db4-19db79ed5433"
  protected val serviceInstanceMap = Map[SubjectID, ServiceActorRef]()
  val tempAgentsMap = collection.mutable.Map[String, ProcessInstanceActor.Agent]()
  var from: SubjectID = null
  var processInstanceIdentical: String = ""
  var managerURL: String = ""
  val startNodeIndex: String = "11"


  override protected def states: List[State] = List(
    SendState(0, "exitcondition", Map("m5" -> Target("Subj5435:5c7aaa4d-343e-4a5c-bdc2-e43162bd10a1", -1, -1, false, "")), Map("m5" -> 17), "VAR:m5", ""),
    loadStartEnd(5, "exitcondition", Map(), Map("5" -> 0), "load StartEnd", ""),
    loadRoutes(10, "exitcondition", Map(), Map("10" -> 15), "load Routes", ""),
    SelectnextDestinationPointsKombination(14, "exitcondition", Map(), Map("14" -> 7), "Select next Destination Points Kombination", ""),
    storePOIs(1, "exitcondition", Map(), Map("1" -> 2), "store POIs", ""),
    storeROIs(6, "exitcondition", Map(), Map("6" -> 16), "store ROIs", ""),
    RemoveRoutesintersectingRedPointsincreasemetricforOrangePointintersections(9, "exitcondition", Map(), Map("9" -> 3), "Remove Routes intersecting Red Points; increase metric for Orange Point intersections", ""),
    SendState(13, "exitcondition", Map("m6" -> Target("Subj2:6ade7atg-d3c2-46df-a3d0-c7f328e9af457", -1, -1, false, "")), Map("m6" -> 8), "VAR:m6", ""),
    ReceiveState(2, "exitcondition", Map("m4" -> Target("Subj2:6ade7atg-d3c2-46df-a3d0-c7f328e9af457", -1, -1, false, "")), Map("m4" -> 6), "Preparer: receive ROIs", ""),
    ReceiveState(17, "exitcondition", Map("m6" -> Target("Subj5435:5c7aaa4d-343e-4a5c-bdc2-e43162bd10a1", -1, -1, false, "")), Map("m6" -> 12), "Preparer: receive Routes", ""),
    storeRoutes(12, "exitcondition", Map(), Map("12" -> 9), "store Routes", ""),
    SendState(7, "exitcondition", Map("m8" -> Target("Subj5435:5c7aaa4d-343e-4a5c-bdc2-e43162bd10a1", -1, -1, false, "")), Map("m8" -> 5), "VAR:m8", ""),
    alternativeDestinationPointcombinationavailable(3, "exitcondition", Map(), Map("no" -> 10, "yes" -> 14), "alternative Destination Point combination available?", ""),
    storeStartEnd(18, "exitcondition", Map(), Map("18" -> 4), "store StartEnd", ""),
    determineDestinationPointscombinations(16, "exitcondition", Map(), Map("16" -> 14), "determine Destination Points combinations", ""),
    ReceiveState(11, "exitcondition", Map("m5" -> Target("Subj2:6ade7atg-d3c2-46df-a3d0-c7f328e9af457", -1, -1, false, "")), Map("m5" -> 18), "Preparer: receive StartEnd", "v1"),
    ExitState(8, null, Map(), Map(), null, null),
    ReceiveState(4, "exitcondition", Map("m3" -> Target("Subj2:6ade7atg-d3c2-46df-a3d0-c7f328e9af457", -1, -1, false, "")), Map("m3" -> 1), "Preparer: receive POIs", ""),
    ListofRoutesempty(15, "exitcondition", Map(), Map("15" -> 13), "List of Routes empty ?", "")
  )


  // start with first state
  def getStartState(): State = {
    getState("11".toInt)
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
  private var messageContent: String = "" // will be used in getResult

  private var start_end: VStartEnd = null
  private var pois: Seq[VPOIGroup] = Nil
  private var rois: Seq[VROI] = Nil
  private var destinations: Seq[VSinglePoint] = Nil
  private var route: Seq[VRoute] = Nil
  private var routetmp: Seq[VRoute] = Nil

  private var remainingDestinations: List[List[VSinglePoint]] = Nil

  def debug(): Unit = {
    log.debug("#### DEBUG ####")
    log.debug("#### start_end: {}", start_end)
    log.debug("#### POIs: {}", pois)
    log.debug("#### ROIs: {}", rois)
    log.debug("#### destinations: {}", destinations)
    log.debug("#### route: {}", route)
    log.debug("#### routetmp: {}", routetmp)
    log.debug("#### remainingDestinations: {}", remainingDestinations)
    log.debug("#### DEBUG ####")
  }

  override def reset = {
    start_end = null
    pois = Nil
    rois = Nil
    destinations = Nil
    route = Nil
    routetmp = Nil

    remainingDestinations = Nil

    super.reset
  }

  def processMsg(msg: Any) {
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
      if (tempAgentsMap(targetSubjectID).address.toUrl == managerURL)
        manager !! message
      else sender !! message
    } else {
      if (!serviceInstanceMap.contains(targetSubjectID)) {
        if (tempAgentsMap.contains(targetSubjectID)) {
          if (tempAgentsMap(targetSubjectID).address.toUrl == managerURL) {
            manager !! message // return to Backend

          } else {
            // if targetSubjectID is in tempAgentsMap, this means the service has been created. The SubjectToSubject can be sent to serviceActorManager
            val agentsAddr = tempAgentsMap(targetSubjectID).address.toUrl
            val path = "akka.tcp" + "://sbpm" + agentsAddr + "/user/" + BackendActorLocator.subjectProviderManagerActorName
            val agentManagerSelection = context.actorSelection(path)
            //agentManagerSelection !! message // serviceActorManager will forward message to correspond service.
            val future = agentManagerSelection ?? AskForServiceInstance(processInstanceIdentical, targetSubjectID) // get targetServiceInstance
            val serviceInstance = Await.result(future, (5 seconds)).asInstanceOf[ActorRef]
            serviceInstance !! message
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
                serviceInstance !! message
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
        serviceInstance !! message
        receiver = serviceInstance
      }
    }

    //    if (state.variableId == null) {
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
      log.debug("********************* receive message *********************: " + message)
      from = message.from
      storeMsg(message, sender)

      state match {
        case rs: ReceiveState => {
//          if (rs.variableId != null) {
//            if (variablesOfSubject.contains(rs.variableId)) {
//              variablesOfSubject(rs.variableId).addMessage(sender, message)
//            } else {
//              variablesOfSubject += (rs.variableId) -> Variable(rs.variableId)
//              variablesOfSubject(rs.variableId).addMessage(sender, message)
//            }
//          }

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


  case class loadStartEnd(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State("action", id, exitType, targets, targetIds, text, variableId) {

    def process()(implicit actor: ServiceActor) {
//      if (Seq("", "[]", "[empty message]").contains(messageContent)) {
//        log.warning("startend is empty")
//        start_end = VStartEnd(VSinglePoint(0.0, 0.0), VSinglePoint(1.0, 1.0))
//      }
//      else
//        start_end = messageContent.parseJson.convertTo[VStartEnd]
      actor.setMessage("hallo hallo")
      actor.changeState()
    }
  }

  case class loadRoutes(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State("action", id, exitType, targets, targetIds, text, variableId) {

    def process()(implicit actor: ServiceActor) {
      //actor.setMessage("") //TODO set message
      actor.setMessage("hallo hallo")
      actor.changeState()

    }
  }

  case class SelectnextDestinationPointsKombination(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State("action", id, exitType, targets, targetIds, text, variableId) {

    def process()(implicit actor: ServiceActor) {
      log.info("SelectnextDestinationPoints..")
//      val tmp: List[VSinglePoint] = remainingDestinations match {
//        case Nil => Nil
//        case h :: t => {
//          remainingDestinations = t; h
//        }
//      }
//
//      log.info("SelectnextDestinationPoints. tmp = " + tmp.mkString(", "))
//
//      actor.setMessage(tmp.toJson.compactPrint)
      actor.setMessage("hallo hallo")
      actor.changeState()
    }
  }

  case class storePOIs(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State("action", id, exitType, targets, targetIds, text, variableId) {

    def process()(implicit actor: ServiceActor) {
//      if (Seq("", "[]", "[empty message]").contains(messageContent)) {
//        log.warning("POIs are empty")
//        pois = Nil
//      }
//      else
//        pois = messageContent.parseJson.convertTo[Seq[VPOIGroup]]
      actor.setMessage("hallo hallo")
      actor.changeState()
    }
  }

  case class storeROIs(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State("action", id, exitType, targets, targetIds, text, variableId) {

    def process()(implicit actor: ServiceActor) {
//      if (Seq("", "[]", "[empty message]").contains(messageContent)) {
//        log.warning("ROIs is empty")
//        rois = Nil
//      }
//      else
//        rois = messageContent.parseJson.convertTo[Seq[VROI]]
      actor.setMessage("hallo hallo")
      actor.changeState()
    }
  }

  case class RemoveRoutesintersectingRedPointsincreasemetricforOrangePointintersections(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State("action", id, exitType, targets, targetIds, text, variableId) {

//    def additionalMetric(a: VSinglePoint, b: VSinglePoint, gs: Seq[VROI]): Double = {
//      gs.foldLeft(0.0) {
//        (diff, roi) => {
//          val l = roi.intersectLength(a, b)
//          val f = (roi.getMetricFactor - 1.0)
//          diff + f * l
//        }
//      }
//    }

//    def addAdditionalMetric(rs: Seq[VRoute], gs: Seq[VROI]): Seq[VRoute] = rs.map(r =>
//      r.copy(
//        metric = r.metric + r.points.sliding(2).foldLeft(0.0) {
//          (sum, pair) => {
//            sum + additionalMetric(pair(0), pair(1), gs)
//          }
//        }
//      )
//    )
//
//    def filterMetric1(rs: Seq[VRoute]): Seq[VRoute] = rs.filterNot(r => {
//      r.points.sliding(2).exists(pair => {
//        geometric.intersects(pair(0), pair(1), rois)
//      })
//    })

    def process()(implicit actor: ServiceActor) {
//      val filtered = filterMetric1(routetmp)
//
//      val addedMetric = addAdditionalMetric(filtered, rois)
//
//      route ++= addedMetric
      actor.setMessage("hallo hallo")
      actor.changeState()
    }
  }

  case class storeRoutes(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State("action", id, exitType, targets, targetIds, text, variableId) {

    def process()(implicit actor: ServiceActor) {
//      if (Seq("", "[]", "[empty message]").contains(messageContent)) {
//        log.warning("route is empty")
//        routetmp = Nil
//      }
//      else
//        routetmp = messageContent.parseJson.convertTo[Seq[VRoute]]
      actor.setMessage("hallo hallo")
      actor.changeState()
    }
  }

  case class alternativeDestinationPointcombinationavailable(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State("action", id, exitType, targets, targetIds, text, variableId) {

    def process()(implicit actor: ServiceActor) {
//      log.info("remainingDestinations.length: " + remainingDestinations.length)
//      Thread.sleep(1000)
//
//      if (remainingDestinations.isEmpty) {
//        branchCondition = "no"
//      }
//      else {
//        branchCondition = "yes"
//      }
      branchCondition = "no"
      actor.setMessage("hallo hallo")
      actor.changeState()
    }
  }

  case class storeStartEnd(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State("action", id, exitType, targets, targetIds, text, variableId) {

    def process()(implicit actor: ServiceActor) {
//      actor.setMessage(start_end.toJson.compactPrint)
      actor.changeState()
    }
  }

  case class determineDestinationPointscombinations(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State("action", id, exitType, targets, targetIds, text, variableId) {


//    def listTimes[T](x: T, times: Int): List[T] = {
//      var l: List[T] = Nil
//      for (i <- 0 until times) l = x :: l
//      l
//    }
//
//    def cartesianProduct[T](xss: List[List[T]]): List[List[T]] = xss match {
//      case Nil => List(Nil)
//      case h :: t => (for (xh <- h; xt <- cartesianProduct(t)) yield (xh :: xt))
//    }
//
//
//    def cartesianProductTimesFiltered[T](x: List[T], times: Int): Set[Set[T]] = {
//      val a: List[List[T]] = listTimes(x, times)
//      val b: List[List[T]] = cartesianProduct(a)
//      // eliminate inner duplicates; filter only without duplicates
//      val c: List[Set[T]] = b.map(_.toSet).filter(_.size == times)
//      // eliminate outer duplicates and return
//      c.toSet
//    }
//
//
//    def cartesianProductWithTimes[T](xss: List[(Int, List[T])]): List[List[T]] = xss match {
//      case Nil => List(Nil)
//      case (times, h) :: t => (for (xh <- cartesianProductTimesFiltered(h, times).toList; xt <- cartesianProductWithTimes(t)) yield (xh.toList ++ xt))
//    }


    def process()(implicit actor: ServiceActor) {
//      val lists: List[(Int, List[VSinglePoint])] = pois.map(group => (group.num, group.points.toList)).toList
//
//      log.info("prepareDestinationPointcombinations: lists = " + lists.mkString(","))
//
//      remainingDestinations = cartesianProductWithTimes(lists)
//
//      log.info("prepareDestinationPointcombinations: remainingDestinations = " + remainingDestinations.mkString(","))
      actor.setMessage("hallo hallo")
      actor.changeState()
    }
  }

  case class ListofRoutesempty(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State("action", id, exitType, targets, targetIds, text, variableId) {

    def process()(implicit actor: ServiceActor) {
      actor.setMessage("hallo hallo")
      actor.changeState()
    }
  }

}
