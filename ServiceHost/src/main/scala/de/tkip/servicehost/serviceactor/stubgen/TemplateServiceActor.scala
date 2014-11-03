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

import java.io.File

import java.awt.image.BufferedImage

import javax.imageio
import javax.imageio.ImageIO


class TemplateServiceActor extends ServiceActor {
  override protected val INPUT_POOL_SIZE: Int = 20
  
  override protected val serviceID: ServiceID = "Subj2:ff9bacbf-bb0c-4316-9cd5-5328e1246557"
  override protected val subjectID: SubjectID = "Subj2:ff9bacbf-bb0c-4316-9cd5-5328e1246557"
  
  
  override protected def states: List[State] = List(
      ExitState(0,null,Map(),Map(),null),
      fetchROIs(5,"exitcondition",Map(),Map("5" -> 4),"fetch ROIs"),
      SendState(1,"exitcondition",Map("m3" -> Target("Subj2:6ade7af8-d3c2-4608-a3d0-c7f328e9afeb",-1,-1,false,"")),Map("m3" -> 5),""),
      internalaction(6,"exitcondition",Map(),Map("6" -> 3),"internal action"),
      ReceiveState(2,"exitcondition",Map("m10" -> Target("Subj2:6ade7af8-d3c2-4608-a3d0-c7f328e9afeb",-1,-1,false,"")),Map("m10" -> 6),"receive configuration"),
      fetchPOIs(3,"exitcondition",Map(),Map("3" -> 1),"fetch POIs"),
      SendState(4,"exitcondition",Map("m4" -> Target("Subj2:6ade7af8-d3c2-4608-a3d0-c7f328e9afeb",-1,-1,false,"")),Map("m4" -> 0),"")
    )

  // start with first state
  // TODO: that is not always the start state!
  def getStartState(): State = {
    getState(2)
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

  case class internalaction(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) = ???
  }

  case class fetchPOIs(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      actor.setMessage(pois.toJson.compactPrint)
      actor.changeState()
    }
  }

  case class fetchROIs(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      actor.setMessage(rois.toJson.compactPrint)
      actor.changeState()
    }
  }
}
