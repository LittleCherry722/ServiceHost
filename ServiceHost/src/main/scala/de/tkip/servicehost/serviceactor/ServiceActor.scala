package de.tkip.servicehost.serviceactor

import akka.actor._
import de.tkip.servicehost.Messages._
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.servicehost.serviceactor.stubgen.State

abstract class ServiceActor extends Actor {
  
  
  def changeState()
  
  def getState(id: Int): State
  
  def storeMsg(message: Any): Unit
  
  def getSender(): ActorRef
 
  def terminate(): Unit
 
  def getUserID(): Int
  
  def getProcessID(): Int
  
  def getSubjectID(): String
}

object ServiceAttributes {
  
  type ServiceID = String; val AllServices = ""
  type ProcessID = Int
  type ServiceActorRef = ActorRef
}