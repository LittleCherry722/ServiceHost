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

      // TODO we only use tcp protocol?
      val protocol = ".tcp"
      val address = agent.address.toUrl

      val agentManager =
        context.actorFor("akka" + protocol + "://sbpm" + address +
          "/user/" + ActorLocator.subjectProviderManagerActorName)

      log.debug("ProcessInstanceProxyManagerActor.GetProcessInstanceProxy: path = {}", "akka" + protocol + "://sbpm" + address +
                "/user/" + ActorLocator.subjectProviderManagerActorName)
      log.debug("ProcessInstanceProxyManagerActor.GetProcessInstanceProxy: targetManager = {}", agentManager)


      val (newProcessInstanceMap, processInstanceInfo: Future[ProcessInstanceProxy])=
        processInstanceMap.get((processId, address)) match {
        case x: Some[_] => {
          (processInstanceMap, x)
        }
        case None => {
          val processInstanceInfo = createProcessInstanceEntry(processId, address, agentManager)
          (processInstanceMap + ((processId, address) -> processInstanceInfo), processInstanceInfo)
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

  private def createProcessInstanceEntry(processId: ProcessID, targetAddress: String,
    targetManager: ProcessManagerRef): Future[ProcessInstanceProxy] = {
    // TODO name?
    val newProcessInstanceName = "Unnamed"

    val getMappingMsg = GetAgentsList(processId, targetAddress)

    val futures = for (processInstanceFuture <- processInstanceMap.values) yield {
      val resultFuture = processInstanceFuture flatMap {processInstance => processInstance.proxy ?? getMappingMsg}
      resultFuture.mapTo[GetAgentsListResponse]
    }

    val resultsFuture = Future.sequence(futures)
    val mapping = Await.result(resultsFuture, timeout.duration).map(_.subjectMapping)

    // create the message which is used to create a process instance
    val createMessage = CreateProcessInstance(ExternalUser, processId, newProcessInstanceName, Some(self), mapping)

    for {
      // create the processinstance
      created <- (targetManager ?? createMessage).mapTo[ProcessInstanceCreated]
      instanceRef = created.processInstanceActor
      // ask for the proxy actor
      proxy <- (instanceRef ?? GetProxyActor).mapTo[ActorRef]
    } yield new ProcessInstanceProxy(instanceRef, proxy)
  }
}
