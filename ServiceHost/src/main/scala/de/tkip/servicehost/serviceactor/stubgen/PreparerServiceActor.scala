package de.tkip.servicehost.serviceactor.stubgen

import akka.actor.Actor
import de.tkip.servicehost.serviceactor.ServiceActor
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.servicehost.Messages._
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.PoisonPill
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import java.util.Date
import de.tkip.sbpm.application.subject.misc.Stored
import de.tkip.servicehost.ActorLocator
import de.tkip.servicehost.ServiceAttributes._
import scala.collection.immutable.Map
import scala.collection.mutable.{ Queue }
import de.tkip.sbpm.application.subject.misc.Rejected

import de.tkip.vasec._
import de.tkip.vasec.VasecJsonProtocol._
import spray.json._

class PreparerServiceActor extends ServiceActor {
  override protected val INPUT_POOL_SIZE: Int = 20
  
  override protected val serviceID: ServiceID = "Subj2:c60d4f03-2110-4eeb-9db4-19db79ed5433"
  override protected val subjectID: SubjectID = "Subj2:c60d4f03-2110-4eeb-9db4-19db79ed5433"
  
  
  override protected def states: List[State] = List(
      ReceiveState(1,"exitcondition",Map("m4" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m4" -> 15),"Preparer: receive ROIs"),
      SelectnextDestinationPoints(2,"exitcondition",Map(),Map("2" -> 5),"Select next Destination Points"),
      storeStartEnd(3,"exitcondition",Map(),Map("3" -> 11),"store StartEnd"),
      alternativeDestinationPointcombinationavailable(4,"exitcondition",Map(),Map("no" -> 19, "yes" -> 2),"alternative Destination Point combination available?"),
      SendState(5,"exitcondition",Map("m8" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m8" -> 8),"VAR:m8"),
      prepareDestinationPointcombinations(6,"exitcondition",Map(),Map("6" -> 2),"prepare Destination Point combinations"),
      ReceiveState(7,"exitcondition",Map("m6" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m6" -> 13),"Preparer: receive Routes"),
      loadStartEnd(8,"exitcondition",Map(),Map("8" -> 9),"load StartEnd"),
      SendState(9,"exitcondition",Map("m5" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m5" -> 7),"VAR:m5"),
      ExitState(10,null,Map(),Map(),null),
      ReceiveState(11,"exitcondition",Map("m3" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m3" -> 14),"Preparer: receive POIs"),
      storeRoutes(13,"exitcondition",Map(),Map("13" -> 18),"store Routes"),
      storeGreenPointGroups(14,"exitcondition",Map(),Map("14" -> 1),"store POIs"),
      storeOrangePoints(15,"exitcondition",Map(),Map("15" -> 6),"store ROIs"),
      ReceiveState(16,"exitcondition",Map("m5" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m5" -> 3),"Preparer: receive StartEnd"),
      SendState(17,"exitcondition",Map("m6" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m6" -> 10),"VAR:m6"),
      RemoveRoutesintersectingRedPointsAndIncreaseMetricForOrangePoints(18,"exitcondition",Map(),Map("18" -> 4),"Remove Routes intersecting Red Points"),
      loadroutes(19,"exitcondition",Map(),Map("19" -> 17),"load routes")
    )

  def getStartState(): State = {
    getState(16)
  }

  
  private val messages: Map[MessageType, MessageText] = Map(
      "m7" -> "m7",
      "m2" -> "m2",
      "ROIs" -> "m4",
      "Destination Points" -> "m8",
      "m1" -> "m1",
      "Point Type" -> "m9",
      "Routes" -> "m6",
      "POIs" -> "m3",
      "StartEnd" -> "m5"
    )

  private val inputPool: scala.collection.mutable.Map[Tuple2[MessageType, SubjectID], Queue[SubjectToSubjectMessage]] = scala.collection.mutable.Map()

  // Subject default values
  private var target = -1
  private var messageContent: String = "" // will be used in getResult


  private var start_end: VStartEnd = null
  private var pois: Seq[VPOIGroup] = Nil
  private var rois: Seq[VROIGroup] = Nil
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
    log.debug("changeState: current state: " + state)
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

        debug()

        log.debug("changeState: process state: " + state)

        // TODO: state könnte null sein, oder auch der alte..
        state.process()
      }
    }
    log.debug("changeState: completed state: " + state)
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

  def getResult(msg: String): String = {   // handle the messageContent
    msg
  }


  case class storeGreenPointGroups(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      if (Seq("", "[]", "[empty message]").contains(messageContent)) {
        log.warning("POIs are empty")
        pois = Nil
      }
      else
        pois = messageContent.parseJson.convertTo[Seq[VPOIGroup]]

      actor.changeState()
    }
  }

  case class prepareDestinationPointcombinations(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def listTimes[T](x: T, times: Int): List[T] = {
      var l: List[T] = Nil
      for (i <- 0 until times) l = x :: l
      l
    }

    def cartesianProduct[T](xss: List[List[T]]): List[List[T]] = xss match {
      case Nil => List(Nil)
      case h :: t => (for(xh <- h; xt <- cartesianProduct(t)) yield (xh :: xt))
    }


    def cartesianProductTimesFiltered[T](x: List[T], times: Int): Set[Set[T]] = {
      val a: List[List[T]] = listTimes(x, times)
      val b: List[List[T]] = cartesianProduct(a)
      // eliminate inner duplicates; filter only without duplicates
      val c: List[Set[T]] = b.map(_.toSet).filter(_.size == times)
      // eliminate outer duplicates and return
      c.toSet
    }


    def cartesianProductWithTimes[T](xss: List[(Int, List[T])]): List[List[T]] = xss match {
      case Nil => List(Nil)
      case (times, h) :: t => (for(xh <- cartesianProductTimesFiltered(h, times).toList; xt <- cartesianProductWithTimes(t)) yield (xh.toList ++ xt))
    }


    def process()(implicit actor: ServiceActor) {
      var lists: List[(Int,List[VSinglePoint])] = pois.map(group => (group.num, group.points.toList)).toList
      log.info("prepareDestinationPointcombinations: lists = " + lists.mkString(","))
      // TODO: was passiert, wenn num > 0 und metric < 3 ?
      // TODO: filter anpassen wenn diskret
      lists = lists ++ rois.filterNot(group => group.num == 0).map(group => (group.num, group.points.filter(roi => roi.metricFactor <= 1.0).map(roi => roi.getBoundary).foldLeft(List[VSinglePoint]())((a,b)=>a++b))).toList
      log.info("prepareDestinationPointcombinations: lists = " + lists.mkString(","))

      remainingDestinations = cartesianProductWithTimes(lists)

      log.info("prepareDestinationPointcombinations: remainingDestinations = " + remainingDestinations.mkString(","))

      actor.changeState()
    }
  }

  case class storeRoutes(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      if (Seq("", "[]", "[empty message]").contains(messageContent)) {
        log.warning("route is empty")
        routetmp = Nil
      }
      else
        routetmp = messageContent.parseJson.convertTo[Seq[VRoute]]

      actor.changeState()
    }
  }

  case class SelectnextDestinationPoints(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      log.info("SelectnextDestinationPoints..")
      val tmp: List[VSinglePoint] = remainingDestinations match {
        case Nil => Nil
        case h :: t => { remainingDestinations = t; h }
      }

      log.info("SelectnextDestinationPoints. tmp = " + tmp.mkString(", "))

      actor.setMessage(tmp.toJson.compactPrint)
      actor.changeState()
    }
  }

  case class storeStartEnd(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      if (Seq("", "[]", "[empty message]").contains(messageContent)) {
        log.warning("startend is empty")
        start_end = VStartEnd(VSinglePoint(0.0, 0.0), VSinglePoint(1.0, 1.0))
      }
      else
        start_end = messageContent.parseJson.convertTo[VStartEnd]

      actor.changeState()
    }
  }

  case class RemoveRoutesintersectingRedPointsAndIncreaseMetricForOrangePoints(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def additionalMetric(pair: Seq[VSinglePoint], gs: Seq[VROIGroup]): Double = {
      val a: VPoint = pair(0)
      val b: VPoint = pair(1)

      gs.foldLeft(0.0)( (diff, g) => diff + g.points.foldLeft(0.0)( (diff2, roi) => (roi.metricFactor - 1.0) * roi.intersectLength(a, b)))
      // TODO: filter metric
    }

    def addAdditionalMetric(rs: Seq[VRoute], gs: Seq[VROIGroup]): Seq[VRoute] = rs.map( r =>
      r.copy(
        metric = r.metric + r.points.sliding(2).foldLeft(0.0)( (sum, pair) => {
            sum + additionalMetric(pair, gs)
          }
        )
      )
    )

    /*
    def intersects(pair: Seq[VSinglePoint], reds: Array[VRedPoint]): Boolean = {
      val a: VPoint = pair(0)
      val b: VPoint = pair(1)

      reds.exists( r => (intersectLength(a, b, r) > 0.0))
    }

    def filterRed(rs: Array[VRoute], reds: Array[VRedPoint]): Array[VRoute] = rs.filterNot( r => {
      r.points.sliding(2).exists( pair => {
        intersects(pair, reds)
      })
    })
    */

    def process()(implicit actor: ServiceActor) {
      //val filtered = filterRed(routetmp, red)
      val filtered = routetmp

      val addedMetric = addAdditionalMetric(filtered, rois)

      route ++= addedMetric

      actor.changeState()
    }
  }

  case class loadStartEnd(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      actor.setMessage(start_end.toJson.compactPrint)
      actor.changeState()
    }
  }

  case class loadroutes(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      actor.setMessage(route.toJson.compactPrint)
      actor.changeState()
    }
  }

  case class alternativeDestinationPointcombinationavailable(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      log.info("remainingDestinations.length: " + remainingDestinations.length)
      Thread.sleep(1000)

      if (remainingDestinations.isEmpty) {
        branchCondition = "no"
      }
      else {
        branchCondition = "yes"
      }

      actor.changeState()
    }
  }

  case class storeOrangePoints(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      if (Seq("", "[]", "[empty message]").contains(messageContent)) {
        log.warning("ROIs is empty")
        rois = Nil
      }
      else
        rois = messageContent.parseJson.convertTo[Seq[VROIGroup]]

      actor.changeState()
    }
  }
}
