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

import de.tkip.vasec._
import de.tkip.vasec.VasecJsonProtocol._
import spray.json._

class RouterServiceActor extends ServiceActor {
  override protected val INPUT_POOL_SIZE: Int = 20
  
  override protected val serviceID: ServiceID = "Subj5435:5c7aaa4d-343e-4a5c-bdc2-e43162bd10a1"
  override protected val subjectID: SubjectID = "Subj5435:5c7aaa4d-343e-4a5c-bdc2-e43162bd10a1"
  
  
  override protected def states: List[State] = List(
      ReceiveState(0,"exitcondition",Map("m8" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m8" -> 6),"Router: receive Destination Points"),
      storeStartEnd(5,"exitcondition",Map(),Map("5" -> 0),"store StartEnd"),
      CalculateRoutes(1,"exitcondition",Map(),Map("1" -> 2),"Calculate Routes"),
      storeDestinationPoints(6,"exitcondition",Map(),Map("6" -> 1),"store Destination Points"),
      SendState(2,"exitcondition",Map("m6" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m6" -> 3),"VAR:m6"),
      ExitState(3,null,Map(),Map(),null),
      ReceiveState(4,"exitcondition",Map("m5" -> Target("Subj5435:11c66071-867c-4dae-8fa0-640a4e5a22f9",-1,-1,false,"")),Map("m5" -> 5),"Router: receive StartEnd")
    )

  // start with first state
  // TODO: that is not always the start state!
  def getStartState(): State = {
    getState(4)
  }

  
  private val messages: Map[MessageType, MessageText] = Map(
      "Red Points" -> "m7",
      "m2" -> "m2",
      "Orange Points" -> "m4",
      "Destination Points" -> "m8",
      "m1" -> "m1",
      "Point Type" -> "m9",
      "Routes" -> "m6",
      "Green Points" -> "m3",
      "StartEnd" -> "m5"
    )

  private val inputPool: scala.collection.mutable.Map[Tuple2[MessageType, SubjectID], Queue[SubjectToSubjectMessage]] = scala.collection.mutable.Map()

  private var start_end: VStartEnd = null
  private var destinations: List[VSinglePoint] = List()

  // Subject default values
  private var target = -1
  private var messageContent: String = "" // will be used in getResult

  override def reset = {
    start_end = null
    destinations = List()

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


  case class storeStartEnd(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      if (Array("", "[]", "[empty message]").contains(messageContent)) {
        log.warning("startend is empty")
        start_end = VStartEnd(VSinglePoint(0.0, 0.0), VSinglePoint(1.0, 1.0))
      }
      else
        start_end = messageContent.parseJson.convertTo[VStartEnd]

      actor.changeState()
    }
  }

  case class storeDestinationPoints(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      if (Array("", "[]", "[empty message]").contains(messageContent)) {
        log.warning("destinations is empty")
        destinations = List()
      }
      else
        destinations = messageContent.parseJson.convertTo[List[VSinglePoint]]

      actor.changeState()
    }
  }

  case class CalculateRoutes(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      val sorted: Seq[VSinglePoint] = geometric.tsp(start_end.start, start_end.end, destinations)

      val metric = geometric.distance(sorted)

      val route: VRoute = VRoute(sorted, metric)

      actor.setMessage(List(route).toJson.compactPrint)

      actor.changeState()
    }
  }
}
