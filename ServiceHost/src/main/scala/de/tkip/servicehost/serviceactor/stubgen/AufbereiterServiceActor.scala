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
import scala.collection.mutable.{ ArrayBuffer, Queue }
import de.tkip.sbpm.application.subject.misc.Rejected

import de.tkip.sbpm.application.subject.behavior.state._
import de.tkip.sbpm.application.subject.behavior.state.VasecJsonProtocol._
import spray.json._

class AufbereiterServiceActor extends ServiceActor {
  override protected val INPUT_POOL_SIZE: Int = 20
  
  override protected val serviceID: ServiceID = "Subj2:c60d4f03-2110-4eeb-9db4-19db79ed5433"
  override protected val subjectID: SubjectID = "Subj2:c60d4f03-2110-4eeb-9db4-19db79ed5433"
  
  
  override protected def states: List[State] = List(
      JOIN_m3_greentmp_greentmp(0,"exitcondition",Map(),Map("0" -> 4),"JOIN:m3:greentmp:greentmp"),
      ReceiveState(5,"exitcondition",Map("m7" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m7" -> 17),"Aufbereiter: receive Red-Points"),
      SendState(10,"exitcondition",Map("m6" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m6" -> 3),"VAR:m6"),
      EQUALS_m4(14,"exitcondition",Map(),Map("true" -> 6, "false" -> 1),"EQUALS:m4:[]"),
      storeblue(20,"exitcondition",Map(),Map("20" -> 1),"store blue"),
      CLEAR_greentmp(1,"exitcondition",Map(),Map("1" -> 11),"CLEAR:greentmp"),
      Blah(6,"exitcondition",Map(),Map("6" -> 10),"new"),
      ReceiveState(9,"exitcondition",Map("m5" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m5" -> 7),"Aufbereiter: receive startziel"),
      storeroute(13,"exitcondition",Map(),Map("13" -> 12),"store route"),
      ReceiveState(2,"exitcondition",Map("m6" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m6" -> 13),"Aufbereiter: receive Route"),
      storered(17,"exitcondition",Map(),Map("17" -> 15),"store red"),
      RemoveintersectingRoutes(12,"exitcondition",Map(),Map("12" -> 14),"Remove intersecting Routes"),
      storestart_end(7,"exitcondition",Map(),Map("7" -> 18),"store start_end"),
      ExitState(3,null,Map(),Map(),null),
      ReceiveState(18,"exitcondition",Map("m3" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m3" -> 16),"Aufbereiter: receive green"),
      storegreen(16,"exitcondition",Map(),Map("16" -> 5),"store green"),
      SELECT_m4_greentmp(11,"exitcondition",Map(),Map("11" -> 0),"SELECT:m4:greentmp"),
      SendState(8,"exitcondition",Map("m5" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m5" -> 2),"VAR:m5"),
      loadstart_end(19,"exitcondition",Map(),Map("19" -> 8),"load start_end"),
      SendState(4,"exitcondition",Map("m3" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m3" -> 19),"VAR:greentmp"),
      ReceiveState(15,"exitcondition",Map("m4" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m4" -> 20),"Aufbereiter: receive Blue-Points")
    )

  def getStartState(): State = {
    getState(9)
  }

  
  private val messages: Map[MessageType, MessageText] = Map(
      "red" -> "m7",
      "m2 (pong)" -> "m2",
      "m4 (blue)" -> "m4",
      "m8 (greentmp)" -> "m8",
      "m1 (ping)" -> "m1",
      "m6 (route)" -> "m6",
      "m3 (green)" -> "m3",
      "m5 (startziel)" -> "m5"
    )

  private val inputPool: scala.collection.mutable.Map[Tuple2[MessageType, SubjectID], Queue[SubjectToSubjectMessage]] = scala.collection.mutable.Map()

  // Subject default values
  private var target = -1
  private var messageContent: String = "" // will be used in getResult


  private var start_end: VStartEnd = null
  private var green: Array[VGreenPoint] = Array()
  private var greentmp: Array[VGreenPoint] = Array()
  private var red: Array[VRedPoint] = Array()
  private var blue: ArrayBuffer[List[VGreenPoint]] = ArrayBuffer()
  private var route: Array[VRoute] = Array()
  private var routetmp: Array[VRoute] = Array()

  def debug(): Unit = {
    log.debug("#### DEBUG ####")
    log.debug("#### start_end: {}", start_end)
    log.debug("#### green: {}", green)
    log.debug("#### greentmp: {}", greentmp)
    log.debug("#### red: {}", red)
    log.debug("#### blue: {}", blue)
    log.debug("#### route: {}", route)
    log.debug("#### routetmp: {}", routetmp)
    log.debug("#### DEBUG ####")
  }



  override def reset = {
    start_end = null
    green = Array()
    greentmp = Array()
    red = Array()
    blue = ArrayBuffer()
    route = Array()
    routetmp = Array()

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

  var qqqq = 0

  def changeState() {
    val q = qqqq
    qqqq += 1
    log.debug("changeState(" + q + "): old state: " + state)

    debug

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
    log.debug("changeState(" + q + "): new state: " + state)
    qqqq -= 1
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

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      start_end = messageContent.parseJson.convertTo[VStartEnd]
      actor.changeState()
    }
  }

  case class storegreen(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      green = messageContent.parseJson.convertTo[Array[VGreenPoint]]
      actor.changeState()
    }
  }

  case class storered(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      red = messageContent.parseJson.convertTo[Array[VRedPoint]]
      actor.changeState()
    }
  }

  case class storeblue(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def cartesianProduct[T](xss: List[List[T]]): List[List[T]] = xss match {
      case Nil => List(Nil)
      case h :: t => for(xh <- h; xt <- cartesianProduct(t)) yield xh :: xt
    }

    def convert(l: Seq[VBluePoint]): Seq[VGreenPoint] = l map { a => a: VGreenPoint }

    def process()(implicit actor: ServiceActor) {
      val groups = messageContent.parseJson.convertTo[Array[VBlueGroup]]

      val lists: List[List[VGreenPoint]] = groups.foldLeft(List[List[VGreenPoint]]()) {
        (l: List[List[VGreenPoint]], group: VBlueGroup) => {
          val p: List[VGreenPoint] = convert(group.points).toList
          // TODO: num
          l ++ List(p)
        }
      }

      blue = cartesianProduct(lists).to[ArrayBuffer]

      actor.changeState()
    }
  }

  case class storeroute(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      routetmp = messageContent.parseJson.convertTo[Array[VRoute]]
      actor.changeState()
    }
  }

  case class JOIN_m3_greentmp_greentmp(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      val tmp: Array[VGreenPoint] = green ++ greentmp
      actor.setMessage(tmp.toJson.compactPrint)
      actor.changeState()
    }
  }

  case class EQUALS_m4(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      if (blue.length == 0) {
        branchCondition = "true"
      }
      else {
        branchCondition = "false"
      }

      actor.changeState()
    }
  }

  case class CLEAR_greentmp(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      greentmp = Array()
      actor.changeState()
    }
  }

  case class Blah(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      actor.setMessage(route.toJson.compactPrint) //TODO set message
      actor.changeState()
    }
  }

  case class RemoveintersectingRoutes(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    // based on http://keith-hair.net/blog/2008/08/05/line-to-circle-intersection-data/
    def intersects(A: VPoint, B: VPoint, C: VRedPoint): Boolean = {
      var result = false

      val aa: Double = (B.x - A.x) * (B.x - A.x) + (B.y - A.y) * (B.y - A.y)
      val bb: Double = 2 * ((B.x - A.x) * (A.x - C.x) + (B.y - A.y) * (A.y - C.y))
      val cc: Double = C.x * C.x + C.y * C.y + A.x * A.x + A.y * A.y - 2 * (C.x * A.x + C.y * A.y) - C.r * C.r
      val deter: Double = bb * bb - 4 * aa * cc

      if (deter > 0) {
        val e: Double = math.sqrt(deter);
        val u1: Double = ( - bb + e) / (2 * aa);
        val u2: Double = ( - bb - e) / (2 * aa);
        if (!((u1 < 0 || u1 > 1) && (u2 < 0 || u2 > 1))) {
          result = true
        }
      }

      result
    }

    def intersects(pair: Seq[VSinglePoint], reds: Array[VRedPoint]): Boolean = {
      val a: VPoint = pair(0)
      val b: VPoint = pair(1)

      reds.exists( r => intersects(a, b, r))
    }

    def filterRed(rs: Array[VRoute], reds: Array[VRedPoint]): Array[VRoute] = rs.filterNot( r => {
      r.points.sliding(2).exists( pair => {
        intersects(pair, reds)
      })
    })

    def process()(implicit actor: ServiceActor) {
      val filtered = filterRed(routetmp, red)
      route ++= filtered
      actor.changeState()
    }
  }

  case class SELECT_m4_greentmp(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      if (blue.length > 0) {
        greentmp = blue.remove(0).toArray
      }
      actor.changeState()
    }
  }

  case class loadstart_end(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      actor.setMessage(start_end.toJson.compactPrint)
      actor.changeState()
    }
  }
}
