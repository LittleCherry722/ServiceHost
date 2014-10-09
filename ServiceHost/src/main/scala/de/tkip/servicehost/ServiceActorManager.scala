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
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.instrumentation.InstrumentedActor
import de.tkip.servicehost.ServiceAttributes._
import de.tkip.servicehost.Messages._
import akka.remote.Remoting
import sun.security.util.HostnameChecker

class ServiceActorManager extends InstrumentedActor {

  private val referenceXMLActor = ActorLocator.referenceXMLActor
  private implicit val timeout = Timeout(5 seconds)
  private val processServiceMap = collection.mutable.Map[ProcessKey, ServiceActorRef]()

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
      processServiceMap((message.target.subjectID, 0)) forward message
    }
    case request: CreateProcessInstance => {
      log.debug("got CreateProcessInstance: " + request)

      val processInstanceID: ProcessInstanceID = nextProcessInstanceID

      // TODO: what of both is request.processID ?
      val processID: ProcessID = request.processID
      val remoteProcessID: ProcessID = request.processID

      var subjectID: SubjectID = null
      for (agent <- request.agentsMap.values) { // through IP get subjectID
        if(agent.address.toUrl.equals("@" + main.hostname + ":" + main.port )){
          subjectID = agent.subjectId
        }
      }
      val serviceID: ServiceID = subjectID // TODO !!!

      // TODO: load reference via processID, and retrieve subjectID from it
      val future: Future[Any] = referenceXMLActor ?? GetClassReferenceMessage(subjectID)
      val classRef: ClassReferenceMessage = Await.result(future, timeout.duration).asInstanceOf[ClassReferenceMessage]
      val actorInstance = this.context.actorOf(Props.create(classRef.classReference), serviceID + "_" + processInstanceID )

      // TODO: map via processID
      processServiceMap += (serviceID, processInstanceID) -> actorInstance
      
      val manager = request.manager

      val persistenceGraph = null
      val processName = ""
      val startedAt = new Date()
      val actions = null
      
      val processInstanceData = ProcessInstanceData(processInstanceID, request.name, request.processID, processName, persistenceGraph, false, startedAt, request.userID, actions)
      sender !! ProcessInstanceCreated(request, actorInstance, processInstanceData)

      actorInstance !! UpdateProcessData(processInstanceID, remoteProcessID, manager)
    }

    case x => log.warning("not implemented: " + x)

  }

}
