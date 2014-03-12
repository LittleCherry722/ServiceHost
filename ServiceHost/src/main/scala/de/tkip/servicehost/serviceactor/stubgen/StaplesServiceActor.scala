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

class StaplesServiceActor extends ServiceActor {

  // TODO implement inputpoolActor
//  private val inputPoolActor: ActorRef = null
//    context.actorOf(Props(new InputPoolActor(data)),"InputPoolActor____"+UUID.randomUUID().toString())
  
  private implicit val service = this
  
  private val states: List[State] = List(
      ExitState(2,null,null,Map("" -> -1)),
      ReceiveState(0,"exitcondition",Map("m1" -> Target("Großunternehmen",-1,-1,false,""), "m3" -> Target("Großunternehmen",-1,-1,false,"")),Map("m1" -> 5, "m3" -> 3)),
      SendState(1,"exitcondition",Map("m2" -> Target("Großunternehmen",-1,-1,false,"")),Map("m2" -> 2)),
      ActionState5(5,"exitcondition",Map("" -> null),Map("" -> 1)),
      ActionState3(3,"exitcondition",Map("" -> null),Map("" -> 1))
      )
      
  private val messages: Map[MessageType, MessageText] = Map(
	"Bestellung" -> "m1",
	"Lieferdatum" -> "m2",
	"Expressbestellung" -> "m3"
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
      println(message)
      tosender = sender
      state match {
        case rs: ReceiveState => 
          rs.handle(message)
        case _=>
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
    
    // TODO implement other messages
  }  
  
  def changeState {
    if (state.targetIds.size > 1) {
      if (this.branchCondition != null) {
    	  state = getState(state.targetIds(this.branchCondition))
    	  
      } else println("no branchcodition defined")
     
    } else state = getState(state.targetIds.head._2) 
    
    println(state.id)
    state.process
      
  }
  
  def getState(id: Int): State = {
    states.find (x => x.id == id).getOrElse(null)
  }
  
  def storeMsg(message: Any): Unit = {
    message match {
      case message: SubjectToSubjectMessage => {
        tosender ! Stored(message.messageID) 
        this.message = message
        if (state.targetIds.size > 1) this.branchCondition = getBranchIDforType(message.messageType).asInstanceOf[String]
        else this.branchCondition = null
      }
      case _ =>
      	this.message = message
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
  
}

case class ActionState5(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int]) extends State("action", id, exitType, targets, targetIds) {

  val stateName = "Bestellung erhalten"
  
  def process()(implicit actor: ServiceActor) {
	  actor.setMessage("Bestellung erhalten. Lieferung in drei Tagen")
	  actor.changeState()
  }
  
}

case class ActionState3(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int]) extends State("action", id, exitType, targets, targetIds) {

  val stateName = "Expressbestellung erhalten"
  
  def process()(implicit actor: ServiceActor) {
	  actor.setMessage("Expressbestellung erhalten. Lieferung morgen")
	  actor.changeState()
  }
}