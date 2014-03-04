package de.tkip.servicehost.serviceactor.stubgen

import akka.actor.Actor
import de.tkip.servicehost.serviceactor.ServiceActor
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.servicehost.Messages._
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.miscellaneous._
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.PoisonPill
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage

class $TemplateServiceActor extends ServiceActor {

  // TODO implement inputpoolActor
//  private val inputPoolActor: ActorRef = null
//    context.actorOf(Props(new InputPoolActor(data)),"InputPoolActor____"+UUID.randomUUID().toString())
  
  private implicit val service = this
  
  private val states: List[State] = List(
      //$EMPTYSTATE$//
      )
  
  // start with first state
  private var state: State = getState(0)
  private var message: Any = null
  private var tosender: ActorRef = null
  
  private val serviceID: String = "$SERVICEID"
  
  // Subject default values
  private var userID = -1
  private var processID = -1
  private var subjectID:String = ""
  private var messageType: String = ""
  private var target = -1
  
  
  def receive: Actor.Receive = {
    case message: SubjectToSubjectMessage => {
      // TODO forward /set variables?
      print(message)
      tosender = sender
      state match {
        case rs: ReceiveState => 
          rs.handle(message)
      }
    }
    case message: ExecuteServiceMessage => {
    	tosender = sender
    }
    case GetProxyActor => {
      sender ! self
    }
    
    // TODO implement other messages
  }  
  
  def changeState {
    state = getState(state.targetId)
    state.process
  }
  
  def getState(id: Double): State = {
    for (s <- states) if (s.id == id) return s
    null
  }
  
  def storeMsg(message: Any): Unit = {
    message match {
      case message: SubjectToSubjectMessage => {
        // TODO 
        this.message = message
      }
      case _ =>
      	this.message = message
    }    
  }
  
  def getSender(): ActorRef = {
    tosender
  }
  
  def terminate() {
    self ! PoisonPill
  }
  
  def getUserID(): Int = {
    userID
  }
  
  def getProcessID(): Int = {
    processID
  }
  
  def getSubjectID(): String = {
    subjectID
  }
}