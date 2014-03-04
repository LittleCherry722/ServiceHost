package de.tkip.servicehost.serviceactor

import akka.actor._
import de.tkip.servicehost.Messages._
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage

abstract class ServiceActor extends Actor {
  
//  protected val inputPoolActor: ActorRef =
//    context.actorOf(Props(new InputPoolActor(data)),"InputPoolActor____"+UUID.randomUUID().toString())
  
//  def receive: Actor.Receive = {
//    case message: ExecuteServiceMessage =>
//      handleExecuteServiceMessage(message, sender)
//    case request: CreateProcessInstance =>
//      handleCreateProcessInstance(request, sender)
//    case message: SubjectToSubjectMessage =>
//      handleSubjectToSubjectMessage(message, sender)
//    case GetProxyActor => {
//      GetProxyActor
//      handleGetProxyActor(GetProxyActor, sender)
//    }
//      
//    case other =>
//      handleServiceSpecificMessage(other, sender)
//  }
//  
//  def handleExecuteServiceMessage(message: ExecuteServiceMessage, sender: ActorRef)
//  def handleCreateProcessInstance(request: CreateProcessInstance, sender: ActorRef)
//  def handleSubjectToSubjectMessage(message: SubjectToSubjectMessage, sender: ActorRef)
//  def handleGetProxyActor(proxy: Any, sender: ActorRef)
//  def handleServiceSpecificMessage(message: Any, sender: ActorRef)
//  
  
}

object ServiceAttributes {
  
  type ServiceID = String; val AllServices = ""
  type ServiceActorRef = ActorRef
}