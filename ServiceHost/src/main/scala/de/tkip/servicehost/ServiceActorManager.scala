package de.tkip.servicehost

import java.util.Date
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor._
import akka.util.Timeout
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.instrumentation.InstrumentedActor
import de.tkip.servicehost.ServiceAttributes._
import de.tkip.servicehost.Messages._
import akka.remote.Remoting
import sun.security.util.HostnameChecker

class ServiceActorManager extends InstrumentedActor {

  private val referenceXMLActor = ActorLocator.referenceXMLActor
  private implicit val timeout = Timeout(5 seconds)
  //private val processKeyMap = collection.mutable.Map[String, ProcessInstanceKey]()
  //processINstanceidentical
  private val processkeyMap = collection.mutable.ListBuffer[String]()
  //(processInstanceidentical, processInstanceID)
  private val processInstanceKey = collection.mutable.Map[String, Int]()
  // private val identicalServiceMap = collection.mutable.Map[String, ServiceActorRef]()
  //((processInstanceidentical, SubjectID), aerviceActorref)
  private val identicalServiceMap = collection.mutable.Map[ServiceInstanceKey, ServiceActorRef]()

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
      val targetServiceID = message.target.subjectID
      identicalServiceMap(message.processInstanceidentical.get,targetServiceID)forward(message)
    }

    case msg: AskForServiceInstance => {
      sender !! identicalServiceMap(msg.processInstanceIdentical, msg.targetSubjectId)
    }
    case request: CreateServiceInstance => {
      log.debug("got CreateServiceInstance: " + request)

      for (targetIdNumber <- 0 until request.target.size) {
        val targetSubjectId = request.target(targetIdNumber)
        val serviceInstanceKey: ServiceInstanceKey = (request.processInstanceidentical, targetSubjectId)
        if (!processkeyMap.contains(request.processInstanceidentical)) {
          // create new ProcessInstanceID
          val processInstanceID: ProcessInstanceID = nextProcessInstanceID
          createProcessInstance(processInstanceID, request, serviceInstanceKey)
        } else if (!identicalServiceMap.contains(serviceInstanceKey)) {
          val processInstanceID: ProcessInstanceID = processInstanceKey(request.processInstanceidentical)
          createProcessInstance(processInstanceID, request, serviceInstanceKey)
        } else {
          log.debug("ProcessInstance already created: " + request)
          val processInstanceID: ProcessInstanceID = processInstanceKey(request.processInstanceidentical)
          val persistenceGraph = null
          val processName = request.name + "_" + processInstanceID
          val startedAt = new Date()
          val actions = null
          val actorInstance = identicalServiceMap(serviceInstanceKey)
          val processInstanceData = ProcessInstanceData(processInstanceID, request.name, request.processID, processName, persistenceGraph, false, startedAt, request.userID, actions)
          val createProcessInstance = CreateProcessInstance(request.userID, request.processID, request.name, request.manager, request.agentsMap)
          sender !! ProcessInstanceCreated(createProcessInstance, actorInstance, processInstanceData)
        }
      }
    }


    case x => log.warning("not implemented: " + x)

  }

  def createProcessInstance(processInstanceID: ProcessInstanceID, request: CreateServiceInstance, serviceInstanceKey: ServiceInstanceKey) = {
    val processID: ProcessID = request.processID
    val remoteProcessID: ProcessID = request.processID

    // TODO: change to mutilService

    var subjectID: SubjectID = serviceInstanceKey._2
    val serviceID: ServiceID = subjectID // TODO !!!

    // TODO: load reference via processID, and retrieve subjectID from it
    val future: Future[Any] = referenceXMLActor ?? GetClassReferenceMessage(serviceID)
    val classRef: ClassReferenceMessage = Await.result(future, timeout.duration).asInstanceOf[ClassReferenceMessage]
    val actorInstance = this.context.actorOf(Props.create(classRef.classReference), serviceID + "_" + processInstanceID + "_" + request.processInstanceidentical)
    // TODO: map via processID
    processkeyMap.append(request.processInstanceidentical)
    processInstanceKey += request.processInstanceidentical -> processInstanceID
    identicalServiceMap += serviceInstanceKey -> actorInstance
    val persistenceGraph = null
    val processName = request.name + "_" + processInstanceID
    val startedAt = new Date()
    val actions = null
    val processInstanceData = ProcessInstanceData(processInstanceID, request.name, request.processID, processName, persistenceGraph, false, startedAt, request.userID, actions)
    val createProcessInstance = CreateProcessInstance(request.userID, request.processID, request.name, request.manager, request.agentsMap)
    sender !! ProcessInstanceCreated(createProcessInstance, actorInstance, processInstanceData)
    actorInstance !! UpdateProcessData(processInstanceID, remoteProcessID,processID,request.manager.get)
    actorInstance !! UpdateServiceInstanceDate(request.agentsMap, request.processInstanceidentical,request.managerUrl)
  }
}
