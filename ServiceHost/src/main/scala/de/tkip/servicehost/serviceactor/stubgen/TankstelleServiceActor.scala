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

class TankstelleServiceActor extends ServiceActor {
  override protected val INPUT_POOL_SIZE: Int = 20
  
  override protected val serviceID: ServiceID = "Subj2:1ca1b32f-af47-4951-882a-19c9766b38dd"
  override protected val subjectID: SubjectID = "Subj2:1ca1b32f-af47-4951-882a-19c9766b38dd"
  
  
  override protected def states: List[State] = List(
      ReceiveState(0,"exitcondition",Map("m5" -> Target("Subj2:6ade7af8-d3c2-4608-a3d0-c7f328e9afeb",-1,-1,false,"")),Map("m5" -> 2),"Tankstellen in Tankstellen-Blackbox: receive startziel"),
      SendState(5,"exitcondition",Map("m3" -> Target("Subj2:6ade7af8-d3c2-4608-a3d0-c7f328e9afeb",-1,-1,false,"")),Map("m3" -> 8),"VAR:m3"),
      ExitState(1,null,Map(),Map(),null),
      GENERATE_blue(6,"exitcondition",Map(),Map("6" -> 4),"GENERATE:m5:m4:2:blue"),
      storestart_end(2,"exitcondition",Map(),Map("2" -> 7),"store start_end"),
      GENERATE_green(7,"exitcondition",Map(),Map("7" -> 5),"GENERATE:m5:m3:2:green"),
      SendState(3,"exitcondition",Map("m7" -> Target("Subj2:6ade7af8-d3c2-4608-a3d0-c7f328e9afeb",-1,-1,false,"")),Map("m7" -> 6),"VAR:m7"),
      GENERATE_red(8,"exitcondition",Map(),Map("8" -> 3),"GENERATE:m5:m7:1:red"),
      SendState(4,"exitcondition",Map("m4" -> Target("Subj2:6ade7af8-d3c2-4608-a3d0-c7f328e9afeb",-1,-1,false,"")),Map("m4" -> 1),"VAR:m4")
    )

  // start with first state
  // TODO: that is not always the start state!
  def getStartState(): State = {
    getState(0)
  }

  
  private val messages: Map[MessageType, MessageText] = Map(
      "m7 (red)" -> "m7",
      "m2 (pong)" -> "m2",
      "m4 (blue)" -> "m4",
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

  override def reset = {
    start_end = null

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

  def rnd(a: Int, b: Int, off: Int = 0): Int = {
    if (a > b) log.error("rnd: invalid input")
    a + scala.util.Random.nextInt(b-a+2*off) - off
  }

  def generate(typ: String, num: Int): String = {
    val data = scala.collection.mutable.ArrayBuffer[VPoint]()

    for (i <- 1 to num) {
      val x = rnd(start_end.start.x, start_end.end.x, 10)
      val y = rnd(start_end.start.y, start_end.end.y, 10)

      if (typ == "green") {
        data += VGreenPoint(x, y)
      }
      else if (typ == "red") {
        val r = rnd(1, 10)
        data += VRedPoint(x, y, r)
      }
      else if (typ == "blue") {
        data += VBluePoint(x, y)
      }
    }

    var out: String = ""
    
    if (typ == "green") {
      out = data.toList.asInstanceOf[List[VGreenPoint]].toJson.compactPrint
    }
    else if (typ == "red") {
      out = data.toList.asInstanceOf[List[VRedPoint]].toJson.compactPrint
    }
    else if (typ == "blue") {
      val group = VBlueGroup(1, data.toList.asInstanceOf[List[VBluePoint]])
      out = Array(group).toJson.compactPrint
    }

    out
  }
  

  case class storestart_end(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "storestart_end"

    def process()(implicit actor: ServiceActor) {
      start_end = messageContent.parseJson.convertTo[VStartEnd]
      actor.changeState()
    }
  }

  case class GENERATE_green(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      actor.setMessage(generate("green", 2))
      actor.changeState()
    }
  }

  case class GENERATE_red(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      actor.setMessage(generate("red", 1))
      actor.changeState()

    }
  }

  case class GENERATE_blue(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      actor.setMessage(generate("blue", 2))
      actor.changeState()

    }
  }
}
