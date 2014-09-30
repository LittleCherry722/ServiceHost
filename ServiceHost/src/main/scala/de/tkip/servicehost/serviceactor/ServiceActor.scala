package de.tkip.servicehost.serviceactor

import akka.actor._
import de.tkip.servicehost.Messages._
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.sbpm.instrumentation.InstrumentedActor
import de.tkip.servicehost.serviceactor.stubgen.State

abstract class ServiceActor extends InstrumentedActor {
  
  var branchCondition: String = null
  var returnMessageContent: String = "received message"
    
  def processMsg():Unit
  
  def changeState() 
  
  def getState(id: Int): State
  
  def storeMsg(message: Any, tosender : ActorRef): Unit
  
  def getDestination(): ActorRef
 
  def terminate(): Unit
 
  def getUserID(): Int
  
  def getProcessID(): Int
  
  def getSubjectID(): String
  
  def getMessage(): String = returnMessageContent
  
  def getBranchCondition() = branchCondition
  
  def setMessage(message: String) = returnMessageContent = message
  
  
}

object ServiceAttributes {
  
  type ServiceID = String; val AllServices = ""
  type BranchID = String;
  type ProcessID = Int
  type ProcessKey = (ServiceID, ProcessID)
  type ServiceActorRef = ActorRef
  type MessageText = String
  type MessageType = String
}
