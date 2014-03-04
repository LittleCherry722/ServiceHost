package de.tkip.servicehost.serviceactor

import akka.actor._
import de.tkip.servicehost.Messages._
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage

abstract class ServiceActor extends Actor {
  
  
  def changeState()
  
  def getState(id: Double)
  
  def storeMsg(message: Any)
  
  def getSender(): ActorRef
 
  def terminate()
}

object ServiceAttributes {
  
  type ServiceID = String; val AllServices = ""
  type ServiceActorRef = ActorRef
}