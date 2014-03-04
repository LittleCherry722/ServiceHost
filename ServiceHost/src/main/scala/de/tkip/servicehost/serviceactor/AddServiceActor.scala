package de.tkip.servicehost.serviceactor

import akka.actor._
import de.tkip.servicehost.Messages._
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage

class AddServiceActor extends ServiceActor {
  
  override def receive: Actor.Receive = {
    case message: AddService =>
      val res = message.n * 2;
    
      println(res)
      message.sender ! new ServiceResultMessage(res)
  }
  
  
    def handleExecuteServiceMessage(message: ExecuteServiceMessage, sender: ActorRef) = {}
    def handleCreateProcessInstance(request: CreateProcessInstance, sender: ActorRef) = {}
    def handleSubjectToSubjectMessage(message: SubjectToSubjectMessage, sender: ActorRef) = {}
    def handleGetProxyActor(proxy: Any, sender: ActorRef) = {}
    def handleServiceSpecificMessage(message: Any, sender: ActorRef) = {}
}