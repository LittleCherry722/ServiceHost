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
import scala.collection.mutable.Queue
import de.tkip.sbpm.application.subject.misc.Rejected

import de.tkip.sbpm.application.subject.behavior.state._
import de.tkip.sbpm.application.subject.behavior.state.VasecJsonProtocol._
import spray.json._

class RouterServiceActor extends ServiceActor {
  override protected val INPUT_POOL_SIZE: Int = 20
  
  override protected val serviceID: ServiceID = "Subj5435:5c7aaa4d-343e-4a5c-bdc2-e43162bd10a1"
  override protected val subjectID: SubjectID = "Subj5435:5c7aaa4d-343e-4a5c-bdc2-e43162bd10a1"
  
  
  override protected def states: List[State] = List(
      ReceiveState(0,"exitcondition",Map("m3" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m3" -> 6),"Router: receive green points"),
      storestart_end(5,"exitcondition",Map(),Map("5" -> 0),"store start_end"),
      generateroute(1,"exitcondition",Map(),Map("1" -> 2),"generate route"),
      storegreenpoints(6,"exitcondition",Map(),Map("6" -> 1),"store green points"),
      SendState(2,"exitcondition",Map("m6" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m6" -> 3),"send route"),
      ExitState(3,null,Map(),Map(),null),
      ReceiveState(4,"exitcondition",Map("m5" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m5" -> 5),"Router: receive start_end")
      )

  // start with first state
  // TODO: that is not always the start state!
  def getStartState(): State = {
    getState(4)
  }

  
  private val messages: Map[MessageType, MessageText] = Map(
      "start_end" -> "m5","green" -> "m3","route" -> "m6"
      )

  private val inputPool: scala.collection.mutable.Map[Tuple2[MessageType, SubjectID], Queue[SubjectToSubjectMessage]] = scala.collection.mutable.Map()

  private var start_end: VStartEnd = null
  private var green: Array[VGreenPoint] = Array()

  // Subject default values
  private var target = -1
  private var messageContent: String = "" // will be used in getResult

  override def reset = {
    start_end = null
    green = Array()

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

  def getResult(msg: String): String = {   // handle the messageContent
    msg
  }


  case class storestart_end(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "storestart_end" //TODO state name

    def process()(implicit actor: ServiceActor) {
      start_end = messageContent.parseJson.convertTo[VStartEnd]
      actor.changeState()
    }
  }

  case class storegreenpoints(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "storegreenpoints" //TODO state name

    def process()(implicit actor: ServiceActor) {
      green = messageContent.parseJson.convertTo[Array[VGreenPoint]]
      actor.changeState()
    }
  }

  case class generateroute(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "generateroute" //TODO state name

    def compare(a: VPoint, b: VPoint): Int = {
      import scala.math.Ordered.orderingToOrdered
      (a.x, a.y) compare (b.x, b.y)
    }

    def distance(a: VPoint, b: VPoint): Double = {
      val aa = math.abs(a.x - b.x)
      val bb = math.abs(a.y - b.y)
      math.sqrt(aa*aa + bb*bb)
    }

    def distance(points: Array[VSinglePoint]): Double = {
      points.sliding(2).foldLeft(0.0) {
        (sum, pair) => sum + distance(pair(0), pair(1))
      }
    }

    def process()(implicit actor: ServiceActor) {
      // currently just sorting the points. TODO: generate a real route
      def convert(l: Array[VGreenPoint]): Array[VSinglePoint] = l map { a => a: VSinglePoint }

      val points: Array[VSinglePoint] = convert(green)

      val sorted: Array[VSinglePoint] = points.sortWith((a, b) => (compare(a, b) < 0))

      val allPoints: Array[VSinglePoint] = Array(start_end.start) ++ sorted ++ Array(start_end.end)

      val metric = distance(allPoints)

      val route: VRoute = VRoute(allPoints, metric)

      actor.setMessage(Array(route).toJson.compactPrint)

      actor.changeState()
    }
  }
}
