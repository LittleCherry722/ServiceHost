package de.tkip.sbpm.application.subject.misc

import akka.actor.{ActorRef, ActorSelection}
import akka.pattern.pipe
import akka.util.Timeout
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.miscellaneous.{CreateProcessInstance, ProcessInstanceCreated}
import de.tkip.sbpm.instrumentation.InstrumentedActor
import de.tkip.sbpm.logging.DefaultLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}

case object GetProxyActor

case class GetProcessInstanceProxy(agent: Agent)

class ProcessInstanceProxyManagerActor(processId: ProcessID, url: String, actor: ProcessInstanceRef) extends InstrumentedActor with DefaultLogging {
  implicit val timeout = Timeout(3 seconds)

  log.debug("register initial process instance proxy for: {}", url)

  private class ProcessInstanceProxy(val instance: ProcessInstanceRef, val proxy: ActorRef)
  private var processInstanceMap: Map[(ProcessID, String), Future[ProcessInstanceProxy]] =
    Map((processId, url) -> (for {
      proxy <- (actor ?? GetProxyActor).mapTo[ActorRef]
    } yield new ProcessInstanceProxy(actor, proxy)))

  def wrappedReceive = {
    // TODO exchange GetSubjectAddr -> GetProcessInstanceAddr
    case GetProcessInstanceProxy(agent) =>
      log.info("GetProcessInstanceProxy for agent " + agent)

      val address = agent.address.toUrl
      val path = s"akka.tcp://sbpm$address/user/${ActorLocator.subjectProviderManagerActorName}"
      val agentManagerSelection = context.actorSelection(path)

      log.debug("ProcessInstanceProxyManagerActor.GetProcessInstanceProxy: path = {}", path)
      log.debug("ProcessInstanceProxyManagerActor.GetProcessInstanceProxy: targetManagerSelection = {}", agentManagerSelection)


      val agentProcessId = agent.processId

      val (newProcessInstanceMap, processInstanceInfo: Future[ProcessInstanceProxy]) =
        processInstanceMap.get((agentProcessId, address)) match {
          case Some(x) =>
            log.info("PROCESS INSTANCE PROXY MANAGER: Found mapping for {}, returning {}", (agentProcessId, address), x)
            (processInstanceMap, x)
          case None =>
            log.info("PROCESS INSTANCE PROXY MANAGER: Did not find mapping for {}", (agentProcessId, address))
            val processInstanceInfo = createProcessInstanceEntry(agentProcessId, address, agentManagerSelection)
            (processInstanceMap + ((agentProcessId, address) -> processInstanceInfo), processInstanceInfo)
        }
      processInstanceMap = newProcessInstanceMap

      // create the answer
      val answer = processInstanceInfo.map(_.proxy)

      // send the answer to the sender
      answer pipeTo sender

    case message: SubjectToSubjectMessage =>
      log.debug("got SubjectToSubjectMessage {} from {}", message, sender)
      log.debug("forward SubjectToSubjectMessage to {}", actor)
      actor.forward(message)

    case s => log.error("got, but cant use {}", s)
  }

  private def createProcessInstanceEntry(targetProcessId: ProcessID,
                                         targetAddress: String,
                                         targetManagerSelection: ActorSelection): Future[ProcessInstanceProxy] = {
    log.info("Creating new unnamed Process Instance")
    val newProcessInstanceName = "Unnamed from external"
    val processInstanceFuture = processInstanceMap((processId, url))
    // create the message which is used to create a process instance
    val createMessage = CreateProcessInstance(
      userID = ExternalUser,
      processID = targetProcessId,
      name = newProcessInstanceName,
      manager = Some(self))

    val instanceProxy = for {
      // create the process instance
      created <- (targetManagerSelection ?? createMessage).mapTo[ProcessInstanceCreated]
      instanceRef = created.processInstanceActor
      // ask for the proxy actor
      proxy <- (instanceRef ?? GetProxyActor).mapTo[ActorRef]
    } yield new ProcessInstanceProxy(instanceRef, proxy)
    instanceProxy.onComplete {
      case Success(proxy) => log.info("PROCESS INSTANCE PROXY MANAGER: Remote Process Instance Successfully created: {}", proxy)
      case Failure(e) => log.error("PROCESS INSTANCE PROXY MANAGER: Error during Remote Process Instance Creation: ", e.getMessage)
    }
    instanceProxy
  }
}
