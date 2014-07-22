package de.tkip.sbpm.application.subject.misc

import de.tkip.sbpm.instrumentation.InstrumentedActor
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.miscellaneous._
import akka.actor.ActorRef
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import akka.pattern.pipe
import de.tkip.sbpm.logging.DefaultLogging
import de.tkip.sbpm.application.miscellaneous.ProcessInstanceCreated
import scala.Some
import de.tkip.sbpm.application.miscellaneous.GetAgentsList
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.ProcessInstanceActor.{ AgentsMap, MappingInfo }
import de.tkip.sbpm.model.Agent
import scala.util.{Success, Failure}

case object GetProxyActor

case class GetProcessInstanceProxy(agent: Agent)

class ProcessInstanceProxyManagerActor(processId: ProcessID, url: String, actor: ProcessInstanceRef) extends InstrumentedActor with DefaultLogging {
  implicit val timeout = Timeout(30 seconds)

  log.debug("register initial process instance proxy for: {}", url)

  private class ProcessInstanceProxy(val instance: ProcessInstanceRef, val proxy: ActorRef)
  private var processInstanceMap: Map[(ProcessID, String), Future[ProcessInstanceProxy]] =
    Map((processId, url) -> (for {
      proxy <- (actor ?? GetProxyActor).mapTo[ActorRef]
    } yield new ProcessInstanceProxy(actor, proxy)))

  def wrappedReceive = {
    // TODO exchange GetSubjectAddr -> GetProcessInstanceAddr
    case GetProcessInstanceProxy(agent) => {
      log.info("GetProcessInstanceProxy for agent " + agent)

      val address = agent.address.toUrl
      val path = "akka.tcp" + "://sbpm" + address + "/user/" + ActorLocator.subjectProviderManagerActorName
      val agentManager = context.actorFor(path)

      log.debug("ProcessInstanceProxyManagerActor.GetProcessInstanceProxy: path = {}", path)
      log.debug("ProcessInstanceProxyManagerActor.GetProcessInstanceProxy: targetManager = {}", agentManager)


      val agentProcessId = agent.processId

      val (newProcessInstanceMap, processInstanceInfo: Future[ProcessInstanceProxy]) =
        processInstanceMap.get((agentProcessId, address)) match {
          case Some(x) => {
            log.info("PROCESS INSTANCE PROXY MANAGER: Found mapping for {}, returning {}", (agentProcessId, address), x)
            (processInstanceMap, x)
          }
          case None => {
            log.info("PROCESS INSTANCE PROXY MANAGER: Did not find mapping for {}", (agentProcessId, address))
            val processInstanceInfo = createProcessInstanceEntry(agentProcessId, address, agentManager)
            (processInstanceMap + ((agentProcessId, address) -> processInstanceInfo), processInstanceInfo)
          }
        }
      processInstanceMap = newProcessInstanceMap

      // create the answer
      val answer = processInstanceInfo.map(_.proxy)

      // send the answer to the sender
      answer pipeTo sender
    }

    case message: SubjectToSubjectMessage => {
      log.debug("got SubjectToSubjectMessage {} from {}", message, sender)
      log.debug("forward SubjectToSubjectMessage to {}", actor)
      actor.forward(message)
    }

    case s => {
      log.error("got, but cant use {}", s)
    }
  }

  private def createProcessInstanceEntry(targetProcessId: ProcessID, targetAddress: String,
    targetManager: ProcessManagerRef): Future[ProcessInstanceProxy] = {
    // TODO name?
    log.info("Creating new unnamed Process Instance")
    val newProcessInstanceName = "Unnamed"

    val getMappingMsg = GetAgentsList(processId, targetAddress)

    val processInstanceFuture = processInstanceMap((processId, url))
    val mappingFuture = for {
      pi <- processInstanceFuture
      response <- (pi.proxy ?? getMappingMsg).mapTo[GetAgentsListResponse]
    } yield response
    val mapping = Await.result(mappingFuture, timeout.duration).agentsMap
    log.info("PROCESS INSTANCE PROXY MANAGER: Received Mapping info! {}", mapping)

    // create the message which is used to create a process instance
    val createMessage = CreateProcessInstance(ExternalUser, targetProcessId, newProcessInstanceName, Some(self), mapping)

    val instanceProxy = for {
      // createma the processinstance
      created <- (targetManager ?? createMessage).mapTo[ProcessInstanceCreated]
      instanceRef = created.processInstanceActor
      // ask for the proxy actor
      proxy <- (instanceRef ?? GetProxyActor).mapTo[ActorRef]
    } yield (new ProcessInstanceProxy(instanceRef, proxy), created)
    instanceProxy.onComplete {
      case Success(proxy) => log.info("PROCESS INSTANCE PROXY MANAGER: Remote Process Instance Successfully created: {}", proxy)
      case Failure(e) => log.error("PROCESS INSTANCE PROXY MANAGER: Error during Remote Process Instance Creation: ", e.getMessage)
    }
    instanceProxy.map(_._1)
  }
}
