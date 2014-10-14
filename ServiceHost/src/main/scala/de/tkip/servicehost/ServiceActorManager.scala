package de.tkip.servicehost

import java.util.Date
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor._
import akka.util.Timeout
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.sbpm.application.miscellaneous.{CreateServiceInstance, CreateProcessInstance, ProcessInstanceData, ProcessInstanceCreated}
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.instrumentation.InstrumentedActor
import de.tkip.servicehost.ServiceAttributes._
import de.tkip.servicehost.Messages._
import akka.remote.Remoting
import sun.security.util.HostnameChecker

class ServiceActorManager extends InstrumentedActor {

  private val referenceXMLActor = ActorLocator.referenceXMLActor
  private implicit val timeout = Timeout(5 seconds)
  private val processKeyMap = collection.mutable.Map[String, ProcessInstanceKey]()
  private val identicalServiceMap = collection.mutable.Map[String, ServiceActorRef]()

  var currentProcessInstanceID = 1
  private def nextProcessInstanceID: ProcessInstanceID = {
    val id = currentProcessInstanceID
    currentProcessInstanceID += 1
    id
  }

  def wrappedReceive = {
    case execute: ExecuteServiceMessage => {
      // TODO
    }
    case classReferenceResponse: ClassReferenceMessage => {
      //TODO implement
    }
    case message: SubjectToSubjectMessage => {
      identicalServiceMap(message.processInstanceidentical.get) forward message
    }
    case request: CreateServiceInstance => {

      if(!identicalServiceMap.contains(request.processInstanceidentical)){
        log.debug("got CreateProcessInstance: " + request)

        val processInstanceID: ProcessInstanceID = nextProcessInstanceID

        // TODO: what of both is request.processID ?
        val processID: ProcessID = request.processID
        val remoteProcessID: ProcessID = request.processID

        // TODO: change to mutilService
        var subjectID: SubjectID = request.target.head

        val serviceID: ServiceID = subjectID // TODO !!!

        // TODO: load reference via processID, and retrieve subjectID from it
        val future: Future[Any] = referenceXMLActor ?? GetClassReferenceMessage(subjectID)
        val classRef: ClassReferenceMessage = Await.result(future, timeout.duration).asInstanceOf[ClassReferenceMessage]
        val actorInstance = this.context.actorOf(Props.create(classRef.classReference), serviceID + "_" + processInstanceID + "_" + request.processInstanceidentical )

        // TODO: map via processID
        processKeyMap += request.processInstanceidentical -> (serviceID, processInstanceID)
        identicalServiceMap += request.processInstanceidentical -> actorInstance

        //      val manager = request.manager

        val persistenceGraph = null
        val processName = request.name + "_" + processInstanceID
        val startedAt = new Date()
        val actions = null

        val processInstanceData = ProcessInstanceData(processInstanceID, request.name, request.processID, processName, persistenceGraph, false, startedAt, request.userID, actions)
        val createProcessInstance = CreateProcessInstance(request.userID, request.processID, request.name, None, request.agentsMap.get)
        sender !! ProcessInstanceCreated(createProcessInstance, actorInstance, processInstanceData)

        actorInstance !! UpdateProcessData(processInstanceID, remoteProcessID, None)
      }else{
        log.debug("ProcessInstance already created: " + request)
        val processInstanceID: ProcessInstanceID = processKeyMap(request.processInstanceidentical)._2
        val persistenceGraph = null
        val processName = request.name + "_" + processInstanceID
        val startedAt = new Date()
        val actions = null
        val actorInstance = identicalServiceMap(request.processInstanceidentical)
        val processInstanceData = ProcessInstanceData(processInstanceID, request.name, request.processID, processName, persistenceGraph, false, startedAt, request.userID, actions)
        val createProcessInstance = CreateProcessInstance(request.userID, request.processID, request.name, None, request.agentsMap.get)
        sender !! ProcessInstanceCreated(createProcessInstance, actorInstance, processInstanceData)
      }

    }

    case x => log.warning("not implemented: " + x)

  }

}
