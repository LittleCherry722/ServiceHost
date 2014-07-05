package de.tkip.servicehost.serviceactor.stubgen

import akka.actor.Actor
import de.tkip.servicehost.serviceactor.ServiceActor
import de.tkip.servicehost.serviceactor.ServiceAttributes._
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.servicehost.Messages._
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.miscellaneous._
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.PoisonPill
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import java.util.Date
import de.tkip.sbpm.application.subject.misc.Stored
import de.tkip.servicehost.ActorLocator
import scala.collection.immutable.Map
import scala.collection.mutable.Queue
import de.tkip.sbpm.application.subject.misc.Rejected

class $TemplateServiceActor extends ServiceActor {
  private val MAX_SIZE: Int = 20
  
  private implicit val service = this
  
  private val states: List[State] = List(
      //$EMPTYSTATE$//
      )
  
  private val messages: Map[MessageType, MessageText] = Map(
      //$EMPTYMESSAGE$//
      )
      
   // start with first state
  private var state: State = getState(0)
  private var inputPool: scala.collection.mutable.Map[Tuple2[String, String], Queue[Tuple2[ActorRef, Any]]] = scala.collection.mutable.Map()
  private var tosender: ActorRef = null

  private val serviceID: String = "Staples"

  // Subject default values
  private var userID = -1
  private var processID = -1
  private var thisID = -1;
  private var manager: Option[ActorRef] = null
  private var subjectID: String = ""
  private var messageType: String = ""
  private var target = -1

  def processMsg() {
    log.debug("processMsg")

    var targetID = "";
      
      for (msgType <- messages.keySet) {
        if (messages(msgType) == this.branchCondition) {
          messageType = msgType;

        }
      }
    targetID = state.targets(this.branchCondition).target.subjectID

    val key = (messageType, targetID)
    val tuple: Tuple2[ActorRef, SubjectToSubjectMessage] = (inputPool(key).dequeue).asInstanceOf[Tuple2[ActorRef, SubjectToSubjectMessage]];
    val message = tuple._2
    tosender = tuple._1

    log.debug("processMsg: message = " + message)

    state match {
      case rs: ReceiveState =>
        rs.handle(message)
      case _ =>
        log.warning("unable to handle message, need to be in ReceiveState. Current state is: " + state)
    }
  }

  def receive: Actor.Receive = {
    case message: SubjectToSubjectMessage => {
      // TODO forward /set variables?
      log.debug("receive message: " + message)
      storeMsg(message, sender)
      tosender = sender

      state match {
        case rs: ReceiveState =>
          processMsg()
          rs.handle(message)
        case _ =>
          log.info("message will be handled when state changes to ReceiveState. Current state is: " + state)
      }
    }
    case message: ExecuteServiceMessage => {
      tosender = sender
    }
    case GetProxyActor => {
      sender ! self
    }

    case update: UpdateProcessData => {
      this.userID = update.userID
      this.processID = update.remoteProcessID
      this.thisID = update.processID
      this.manager = update.manager
    }
  }

  def changeState {
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
         state.process
      }
    }
    log.debug("changeState: new state: " + state)
  }

  def getState(id: Int): State = {
    states.find(x => x.id == id).getOrElse(null)
  }

  def storeMsg(message: Any, tosender: ActorRef): Unit = {
    log.debug("storeMsg: " + message + " from " + tosender)
    message match {
      case message: SubjectToSubjectMessage => {
        val targetID = state.targets(messages(message.messageType))
        val key = (message.messageType.toString(), targetID.target.subjectID)
        if (inputPool.contains(key)) {
          if (inputPool(key).size < MAX_SIZE) {
            (inputPool(key)).enqueue(Tuple2(tosender, message))
            log.debug("storeMsg: Stored")
            tosender ! Stored(message.messageID)
          } else {
            log.debug("storeMsg: Rejected")
            tosender ! Rejected(message.messageID)
          }

        } else {
          inputPool(key) = Queue(Tuple2(tosender, message))
          log.debug("storeMsg: Stored")
          tosender ! Stored(message.messageID)
        }
        if (state.targetIds.size > 1) 
          this.branchCondition = getBranchIDforType(message.messageType).asInstanceOf[String]
        else 
          this.branchCondition = state.targetIds.head._1
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
    ActorLocator.serviceActorManager ! KillProcess(serviceID, thisID)
  }

  def getUserID(): Int = {
    userID
  }

  def getProcessID(): Int = {
    processID
  }

  def getSubjectID(): String = {
    serviceID
  }
  //$ACTIONSTATESIMPLEMENTATION$//
}
