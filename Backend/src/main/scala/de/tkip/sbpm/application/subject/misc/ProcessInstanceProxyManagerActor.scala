package de.tkip.sbpm.application.subject.misc

import akka.actor.Actor
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.miscellaneous._
import akka.actor.ActorRef
import scala.collection.mutable
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
import de.tkip.sbpm.application.miscellaneous.GetSubjectMapping
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.MappingInfo

case object GetProxyActor

case class GetProcessInstanceProxy(processId: ProcessID, url: String)

class ProcessInstanceProxyManagerActor(processId: ProcessID, url: String, actor: ProcessInstanceRef) extends Actor with DefaultLogging {
  implicit val timeout = Timeout(30 seconds)

  private class ProcessInstanceProxy(val instance: ProcessInstanceRef, val proxy: ActorRef)

  log.debug("register initial process instance proxy for: {}", url)
  private val processInstanceMap: mutable.Map[(ProcessID, String), Future[ProcessInstanceProxy]] =
    mutable.Map(
      (processId, url) -> (
          for {
            proxy <- {
              log.debug("TRACE: from " + this.self + " to " + actor + " " + GetProxyActor)
              (actor ? GetProxyActor)
            }.mapTo[ActorRef]
          } yield new ProcessInstanceProxy(actor, proxy)
        )
    )

  def receive = {
    // TODO exchange GetSubjectAddr -> GetProcessInstanceAddr
    case GetProcessInstanceProxy(processId, targetAddress) => {

      // TODO we only use tcp protocol?
      val protocol = if (targetAddress == "") "" else ".tcp"

      val targetManager =
        context.actorFor("akka" + protocol + "://sbpm" + targetAddress +
          "/user/" + ActorLocator.subjectProviderManagerActorName)

      log.debug("ProcessInstanceProxyManagerActor.GetProcessInstanceProxy: path = {}", "akka" + protocol + "://sbpm" + targetAddress +
                "/user/" + ActorLocator.subjectProviderManagerActorName)
      log.debug("ProcessInstanceProxyManagerActor.GetProcessInstanceProxy: targetManager = {}", targetManager)

      val processInstanceInfo =
        processInstanceMap
          .getOrElseUpdate(
            (processId, targetAddress),
            createProcessInstanceEntry(processId, targetAddress, targetManager))

      // create the answer
      val answer = for {
        info <- processInstanceInfo
      } yield info.proxy

      // send the answer to the sender, when its ready
      answer pipeTo sender
    }

    case message: SubjectToSubjectMessage => {
      log.debug("got SubjectToSubjectMessage {} from {}", message, sender)
      log.debug("forward SubjectToSubjectMessage to {}", actor)
      log.debug("TRACE: from " + this.self + " to " + actor + " " + message)
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

    val getMappingMsg = GetSubjectMapping(processId, targetAddress)

    val futures = for (processInstanceFuture <- processInstanceMap.values) yield {
      val resultFuture = processInstanceFuture flatMap {
        processInstance => {
          log.debug("TRACE: from " + this.self + " to " + processInstance.proxy + " " + getMappingMsg)
          processInstance.proxy ? getMappingMsg
        }
      }
      resultFuture.mapTo[SubjectMappingResponse]
    }

    val resultsFuture = Future.sequence(futures)
    val results = Await.result(resultsFuture, timeout.duration)

    val mapping = results.map(_.subjectMapping).foldLeft(Map[SubjectID, MappingInfo]())(_ ++ _)

    // create the message which is used to create a process instance
    val createMessage = CreateProcessInstance(ExternalUser, processId, newProcessInstanceName, Some(self), mapping)

    // create the processinstance
    log.debug("TRACE: from " + this.self + " to " + targetManager + " " + createMessage)
    val createdFuture = (targetManager ? createMessage).mapTo[ProcessInstanceCreated]
    val created = Await.result(createdFuture, timeout.duration)

    val instanceRef = created.processInstanceActor

    // ask for the proxy actor
    log.debug("TRACE: from " + this.self + " to " + instanceRef + " " + GetProxyActor)
    val proxyFuture = (instanceRef ? GetProxyActor).mapTo[ActorRef]

    for {
      proxy <- proxyFuture
    } yield new ProcessInstanceProxy(instanceRef, proxy)
  }
}
