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
  private var inputPool: scala.collection.mutable.Map[Tuple2[String, String], Queue[Tuple2[ActorRef,Any]]] = scala.collection.mutable.Map()
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
    val key: Tuple2[String, String] = null //TODO find current key
    val tuple: Tuple2[ActorRef,SubjectToSubjectMessage] =(inputPool(key).dequeue).asInstanceOf[Tuple2[ActorRef,SubjectToSubjectMessage]];
    val message=tuple._2
    tosender=tuple._1
    state match {
      case rs: ReceiveState =>
        rs.handle(message)
      case _ =>
        println(state + " no match")
    }
  }

  def receive: Actor.Receive = {
    case message: SubjectToSubjectMessage => {
      // TODO forward /set variables?
      println(message)
      storeMsg(message,sender)
      tosender = sender
      state match {
        case rs: ReceiveState =>
          rs.handle(message)
        case _ =>
          println(state + " no match")
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
    if (state.targetIds.size > 1) {
      if (this.branchCondition != null) {
        state = getState(state.targetIds(this.branchCondition))

      } else println("no branchcodition defined")

    } else state = getState(state.targetIds.head._2)

    println(state.id)
    state match {
      case rs: ReceiveState =>
        processMsg()
      case _ => 
    }
    state.process

  }

  def getState(id: Int): State = {
    states.find(x => x.id == id).getOrElse(null)
  }

  def storeMsg(message: Any, tosender: ActorRef): Unit = {
    message match {
      case message: SubjectToSubjectMessage => {
        val targetID = state.targets(messages(message.messageType))
        val key = (message.messageType.toString(), targetID.toString())
        if (inputPool.contains(key)) {
          if (inputPool(key).size < MAX_SIZE) {
            (inputPool(key)).enqueue(Tuple2(tosender,message))
            tosender ! Stored(message.messageID)
          } else {
            tosender ! Rejected(message.messageID)
          }

        } else {
          inputPool(key) = Queue(Tuple2(tosender,message))
          tosender ! Stored(message.messageID)
        }
        if (state.targetIds.size > 1) this.branchCondition = getBranchIDforType(message.messageType).asInstanceOf[String]
        else this.branchCondition = null
      }
      case _ =>
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