package de.tkip.servicehost.serviceactor

import akka.actor._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage

import de.tkip.sbpm.instrumentation.InstrumentedActor

import de.tkip.servicehost.Messages._
import de.tkip.servicehost.ServiceAttributes._
import de.tkip.servicehost.serviceactor.stubgen.State

abstract class ServiceActor extends InstrumentedActor {
  protected implicit val service = this

  protected def INPUT_POOL_SIZE: Int = 100
  protected def serviceID: ServiceID
  protected def subjectID: SubjectID

  protected def states: List[State]
  protected var state: State = getStartState

  protected var processID: ProcessID = -1
  protected var processInstanceID: ProcessInstanceID = -1
  protected var remoteProcessID: ProcessInstanceID = -1;
  protected var manager: Option[ActorRef] = null

  var branchCondition: String = null
  var returnMessageContent: String = "received message"

  def reset(): Unit = {
    state = getStartState
  }
    
  def processMsg(): Unit
  
  def changeState() 
  
  def getStartState(): State

  def getState(id: Int): State
  
  def storeMsg(message: Any, tosender : ActorRef): Unit
  
  def getDestination(): ActorRef
 
  def terminate(): Unit
 
  def getProcessID(): ProcessID
  
  def getSubjectID(): String
  
  def getMessage(): String = returnMessageContent
  
  def getBranchCondition() = branchCondition
  
  def setMessage(message: String) = returnMessageContent = message
  
  def stateReceive: Receive

  def wrappedReceive: Receive = generalReceive orElse stateReceive orElse errorReceive

  def generalReceive: Receive = {
    case GetProxyActor => {
      sender !! self
    }

    case update: UpdateProcessData => {
      this.processInstanceID = update.processInstanceID
      this.remoteProcessID = update.remoteProcessID
      this.manager = update.manager
    }

    case message: ExecuteServiceMessage => {
      log.info("received {}", message)
    }
  }

  private def errorReceive: Receive = {
    case x => {
      log.error("unsupported: {}", x)
    }
  }

}

