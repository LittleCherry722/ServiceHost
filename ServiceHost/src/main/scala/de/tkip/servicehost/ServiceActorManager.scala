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
import de.tkip.sbpm.application.miscellaneous.ProcessInstanceData
import java.util.Date
import de.tkip.sbpm.application.miscellaneous.ProcessInstanceCreated

class ServiceActorManager extends Actor{
  
  private val referenceXMLActor = ActorLocator.referenceXMLActor
  private implicit val timeout = Timeout(5 seconds)
  private var processInstanceCount = 998;
  private val processServiceMap = collection.mutable.Map[ProcessID, ServiceActorRef]()
  
  private val serviceMap =
    collection.mutable.Map[ServiceID, ServiceActorRef]()
  
  def receive = {
    case execute: ExecuteServiceMessage => {
      // TODO
    }
    case classReferenceResponse: ClassReferenceMessage =>{
      //TODO implement
    }
    case message: SubjectToSubjectMessage => {      
      serviceActor(message.target.subjectID) forward message
    }
    case request: CreateProcessInstance => {
      println(request.subjectMapping)
      val actorInstance = serviceActor("Staples") //forward request
      processServiceMap += processInstanceCount -> actorInstance
      
      val userID = request.userID
      val processID = request.processID
      val manager = request.manager

      // TODO implement

      // fake ProcessInstanceActor:

      val persistenceGraph = null
      val processName = ""
      val startedAt = new Date()
      val actions = null
      val processInstanceData = ProcessInstanceData(processInstanceCount, request.name, request.processID, processName, persistenceGraph, false, startedAt, request.userID, actions)
      sender ! ProcessInstanceCreated(request, actorInstance, processInstanceData)
      
      actorInstance ! UpdateProcessData(userID, processInstanceCount, processID, manager)
      processInstanceCount += 1
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