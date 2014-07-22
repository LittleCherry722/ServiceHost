package de.tkip.servicehost

import java.util.Date

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.Future

import akka.actor._
import akka.util.Timeout

import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.miscellaneous.ProcessInstanceData
import de.tkip.sbpm.application.miscellaneous.ProcessInstanceCreated
import de.tkip.sbpm.instrumentation.InstrumentedActor
import de.tkip.servicehost.serviceactor.ServiceAttributes._

import Messages._

class ServiceActorManager extends InstrumentedActor {
  
  private val referenceXMLActor = ActorLocator.referenceXMLActor
  private implicit val timeout = Timeout(5 seconds)
  private var processInstanceCount = 998;
  private val processServiceMap = collection.mutable.Map[ProcessKey, ServiceActorRef]()
  
  private val serviceMap =
    collection.mutable.Map[ServiceID, ServiceActorRef]()
  
  def wrappedReceive = {
    case execute: ExecuteServiceMessage => {
      // TODO
    }
    case classReferenceResponse: ClassReferenceMessage =>{
      //TODO implement
    }
    case message: SubjectToSubjectMessage => {      
//      serviceActor((message.target.subjectID,message.processID)) forward message
      serviceActor((message.target.subjectID,0)) forward message
    }
    case request: CreateProcessInstance => {
      log.debug("got CreateProcessInstance: " + request)
//      val actorInstance = serviceActor("Staples") //forward request
      
      val future: Future[Any] = referenceXMLActor ?? GetClassReferenceMessage("Staples")
      val classRef: ClassReferenceMessage = Await.result(future, timeout.duration).asInstanceOf[ClassReferenceMessage]
      val actorInstance = this.context.actorOf(Props.create(classRef.classReference), "Staples")
      
      // add to serviceMap supposed to be like:
      //processServiceMap += ("Staples", processInstanceCount) -> actorInstance
      //
      // for now: hardcoded processID:
      processServiceMap += ("Staples", 0) -> actorInstance
      
      

      val userID = request.userID
      val processID = request.processID
      val manager = request.manager

      // TODO implement

      // fake ProcessInstanceActor:

      val persistenceGraph = null
      val processName = ""
      val startedAt = new Date()
      val actions = null
      //val processInstanceData = ProcessInstanceData(processInstanceCount, request.name, request.processID, processName, persistenceGraph, false, startedAt, request.userID, actions)
      val processInstanceData = ProcessInstanceData(0, request.name, request.processID, processName, persistenceGraph, false, startedAt, request.userID, actions)
      
      sender !! ProcessInstanceCreated(request, actorInstance, processInstanceData)
      
//      actorInstance ! UpdateProcessData(userID, processInstanceCount, processID, manager)
      actorInstance !! UpdateProcessData(userID, 0, processID, manager)
      
      processInstanceCount += 1
    }
    case GetProxyActor => {
      log.info("received GetProxyActor")
      // TODO implement
      // fake ProcessInstanceProxyActor:
      serviceActor(("Staples",0)) forward GetProxyActor
    }
    
    case process: KillProcess => {
      processServiceMap((process.serviceID, process.processID)) !! PoisonPill
      processServiceMap.remove((process.serviceID, process.processID))
    }
    
  }
  
  def serviceActor(key: ProcessKey): akka.actor.ActorRef = {
//    serviceMap.getOrElse(serviceID, {
//      val future: Future[Any] = referenceXMLActor ?? GetClassReferenceMessage(serviceID)
//      val classRef: ClassReferenceMessage = Await.result(future, timeout.duration).asInstanceOf[ClassReferenceMessage]
//      val actor = this.context.actorOf(new Props(classRef.classReference), serviceID)
//      serviceMap += serviceID -> actor
//      actor
//    })
    
    processServiceMap(key)
//    
//    processServiceMap.getOrElse(key, {
//      val future: Future[Any] = referenceXMLActor ?? GetClassReferenceMessage(serviceID)
//      val classRef: ClassReferenceMessage = Await.result(future, timeout.duration).asInstanceOf[ClassReferenceMessage]
//      val actor = this.context.actorOf(new Props(classRef.classReference), serviceID)
//      serviceMap += key -> actor
//      actor
//    })
  }
  
  def killService(serviceID: String) = {
    if (serviceMap.contains(serviceID)) {
      context.stop(serviceMap(serviceID))
      serviceMap -= serviceID
    }   
    
    
  }
  

}
