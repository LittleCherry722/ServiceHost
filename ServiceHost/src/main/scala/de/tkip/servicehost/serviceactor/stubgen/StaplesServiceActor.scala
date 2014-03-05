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
import java.util.Date

class StaplesServiceActor extends ServiceActor {

  // TODO implement inputpoolActor
//  private val inputPoolActor: ActorRef = null
//    context.actorOf(Props(new InputPoolActor(data)),"InputPoolActor____"+UUID.randomUUID().toString())
  
  private implicit val service = this
  
  private val states: List[State] = List(
      ExitState("exit",2,null,null,-1),ReceiveState("receive",0,"exitcondition",Target("Großunternehmen",-1,-1,false,""),1),SendState("send",1,"exitcondition",Target("Großunternehmen",-1,-1,false,""),2)
      )
  
  // start with first state
  private var state: State = getState(0)
  private var message: Any = null
  private var tosender: ActorRef = null
  
  private val serviceID: String = "Staples"
  
  // Subject default values
  private var userID = -1
  private var processID = -1
  private var thisID = -1;
  private var manager: Option[ActorRef] = null 
  private var subjectID:String = ""
  private var messageType: String = ""
  private var target = -1
  
  
  def receive: Actor.Receive = {
    case message: SubjectToSubjectMessage => {
      // TODO forward /set variables?
      print(message)
      tosender = sender
      state match {
        case receive: ReceiveState =>
          receive.handle(message)
        case _ =>
          println("\n" + state + " no match")
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
    
    // TODO implement other messages
  }  
  
  def changeState {
    state = getState(state.targetId)
    println("changed State to " + state.id)
    state.process
  }
  
  def getState(id: Int): State = {
    states.find (x => x.id == id).getOrElse(null)
  }
  
  def storeMsg(message: Any): Unit = {
    message match {
      case message: SubjectToSubjectMessage => {
        // TODO 
        println("Stored")
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