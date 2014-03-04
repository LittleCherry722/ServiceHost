package de.tkip.servicehost

import akka.actor._
import Messages._
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.pattern.ask
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.servicehost.serviceactor.ServiceAttributes._
import de.tkip.sbpm.application.subject.misc.GetProxyActor

class ServiceActorManager extends Actor{
  
  val referenceXMLActor = ActorLocator.referenceXMLActor
  implicit val timeout = Timeout(5 seconds)
  
  private val serviceMap =
    collection.mutable.Map[ServiceID, ServiceActorRef]()
  
  def receive = {
    case execute: ExecuteServiceMessage => {
      // hardcoded example
      serviceActor(execute.serviceID) ! AddService(1, sender)
    }
    case classReferenceResponse: ClassReferenceMessage =>{
      //TODO implement
    }
    case message: SubjectToSubjectMessage => {      
      serviceActor(message.target.subjectID) forward message
    }
    case request: CreateProcessInstance => {
      println(request.subjectMapping)
      serviceActor("Staples") //forward request
    }
    case GetProxyActor => {
      println("received GetProxyActor")
      // TODO implement
      // fake ProcessInstanceProxyActor:
      serviceActor("Staples") forward GetProxyActor
    }
    
  }
  
  def serviceActor(serviceID: String): akka.actor.ActorRef = {
    serviceMap.getOrElse(serviceID, {
      val future: Future[Any] = ask(referenceXMLActor, GetClassReferenceMessage(serviceID))
      val classRef: ClassReferenceMessage = Await.result(future, timeout.duration).asInstanceOf[ClassReferenceMessage]
      val actor = this.context.actorOf(new Props(classRef.classReference), serviceID)
      serviceMap += serviceID -> actor
      actor
    })
  }
  
  def killService(serviceID: String) = {
    if (serviceMap.contains(serviceID)) {
      context.stop(serviceMap(serviceID))
      serviceMap -= serviceID
    }   
    
    
  }
  

}