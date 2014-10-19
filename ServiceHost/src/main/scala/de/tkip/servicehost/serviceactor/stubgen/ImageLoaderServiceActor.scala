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

class ImageLoaderServiceActor extends ServiceActor {
  override protected val INPUT_POOL_SIZE: Int = 20
  
  override protected val serviceID: ServiceID = "Subj2:ff9bacbf-bb0c-4316-9cd5-5328e1246557"
  override protected val subjectID: SubjectID = "Subj2:ff9bacbf-bb0c-4316-9cd5-5328e1246557"
  
  
  override protected def states: List[State] = List(
      ExitState(0,null,Map(),Map(),null),
      SendState(5,"exitcondition",Map("m3" -> Target("Subj2:6ade7af8-d3c2-4608-a3d0-c7f328e9afeb",-1,-1,false,"")),Map("m3" -> 0),""),
      ReceiveState(1,"exitcondition",Map("m9" -> Target("Subj2:6ade7af8-d3c2-4608-a3d0-c7f328e9afeb",-1,-1,false,"")),Map("m9" -> 3),""),
      SendState(2,"exitcondition",Map("m7" -> Target("Subj2:6ade7af8-d3c2-4608-a3d0-c7f328e9afeb",-1,-1,false,"")),Map("m7" -> 0),""),
      loaddataandpreparesend(3,"exitcondition",Map(),Map("green" -> 5, "red" -> 2, "orange" -> 4),"load data and prepare send"),
      SendState(4,"exitcondition",Map("m4" -> Target("Subj2:6ade7af8-d3c2-4608-a3d0-c7f328e9afeb",-1,-1,false,"")),Map("m4" -> 0),"")
    )

  // start with first state
  // TODO: that is not always the start state!
  def getStartState(): State = {
    getState(1)
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

  // Subject default values
  private var target = -1
  private var messageContent: String = "" // will be used in getResult

  override def reset = {
    // TODO: reset custom properties
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

  def getProcessID(): ProcessID = {
    processID
  }

  def getSubjectID(): String = {
    serviceID
  }

  def getResult(msg: String): String = {   // handle the messageContent
    msg
  }

  

  case class loaddataandpreparesend(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      val args = messageContent.split("\\|")
      
      if (args(0) == "green") {
        branchCondition = "green"
        
        val list1 = List(VSinglePoint(1.0, 8.0))
        val group1 = VGreenGroup(1, list1)

        val list2 = List(VSinglePoint(2.0, 9.0), VSinglePoint(3.0, 3.0))
        val group2 = VGreenGroup(1, list2)

        val list3 = List(VSinglePoint(7.0, 3.0), VSinglePoint(6.0, 9.0), VSinglePoint(5.0, 3.0))
        val group3 = VGreenGroup(2, list3)

        val gg = List(group1, group2, group3)
  
        actor.setMessage(gg.toJson.compactPrint)
      }
      else if (args(0) == "red") {
        branchCondition = "red"
        
        val r1 = List(VRedPoint(2.0, 9.0, 1.0), VRedPoint(3.0, 3.0, 1.0))
  
        actor.setMessage(r1.toJson.compactPrint)
      }
      else if (args(0) == "orange") {
        branchCondition = "orange"
        
        val o1 = List(VOrangePoint(2.0, 7.0, 1.0), VOrangePoint(3.0, 5.0, 1.0))
  
        actor.setMessage(o1.toJson.compactPrint)
      }
      else {
        log.error("unknown: '" + messageContent + "'")
        log.error("args: '" + args.mkString(",") + "'")
        log.info("falling back to green branch")
        branchCondition = "green"
      }
      
      actor.changeState()
    }
  }
}
