package de.tkip.sbpm.application.subject.misc

import akka.actor.Actor
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import akka.actor.ActorRef
import scala.collection.mutable
import de.tkip.sbpm.application.miscellaneous.ProcessInstanceCreated
import scala.concurrent.Await
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Future
import de.tkip.sbpm.application.miscellaneous.ProcessInstanceCreated

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import akka.pattern.pipe
import de.tkip.sbpm.logging.DefaultLogging

case object GetProxyActor

case class GetProcessInstanceProxy(userId: UserID, processId: ProcessID, url: String)

class ProcessInstanceProxyManagerActor(userId: UserID, processId: ProcessID, url: String, actor: ProcessInstanceRef) extends Actor with DefaultLogging {
  implicit val timeout = Timeout(2000)

  log.debug("register initial process instance proxy for: {}", url)

  private class ProcessInstanceProxy(val instance: ProcessInstanceRef, val proxy: ActorRef)
  private val processInstanceMap: mutable.Map[(ProcessID, String), Future[ProcessInstanceProxy]] =
    mutable.Map((processId, url) -> (for {
      proxy <- (actor ? GetProxyActor).mapTo[ActorRef]
    } yield new ProcessInstanceProxy(actor, proxy)))

  def receive = {
    // TODO exchange GetSubjectAddr -> GetProcessInstanceAddr
    case GetProcessInstanceProxy(userId, processId, targetAddress) => {

      // TODO we only use tcp protocol?
      val protocol = if (targetAddress == "") "" else ".tcp"

      val targetManager =
        context.actorFor("akka" + protocol + "://sbpm" + targetAddress +
          "/user/" + ActorLocator.subjectProviderManagerActorName)

      val processInstanceInfo =
        processInstanceMap
          .getOrElseUpdate(
            (processId, targetAddress),
            createProcessInstanceEntry(userId, processId, targetManager))

      // create the answer
      val answer = for {
        info <- processInstanceInfo
      } yield info.proxy

      // send the answer to the sender, when its ready
      answer pipeTo sender
    }

    case s => {
      log.error("got, but cant use {}", s)
    }
  }

  private def createProcessInstanceEntry(userId: UserID,
    processId: ProcessID,
    targetManager: ProcessManagerRef): Future[ProcessInstanceProxy] = {
    // TODO name?
    val newProcessInstanceName = "Unnamed"
    // create the message which is used to create a process instance
    val createMessage = CreateProcessInstance(userId, processId, newProcessInstanceName, Some(self))

    for {
      // create the processinstance
      created <- (targetManager ? createMessage).mapTo[ProcessInstanceCreated]
      instanceRef = created.processInstanceActor
      // ask for the proxy actor
      proxy <- (instanceRef ? GetProxyActor).mapTo[ActorRef]
    } yield new ProcessInstanceProxy(instanceRef, proxy)
  }
}
