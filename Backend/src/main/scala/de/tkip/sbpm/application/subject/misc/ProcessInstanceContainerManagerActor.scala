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

case object GetProxyActor
case class GetProcessInstanceAddr(userId: UserID, processId: ProcessID)

class ProcessInstanceContainerManagerActor(userId: UserID, processId: ProcessID, actor: ProcessInstanceRef) extends Actor {
  implicit val timeout = Timeout(2000)
  //  import context.dispatcher

  protected val logger = Logging(context.system, ProcessInstanceContainerManagerActor.this)

  private class ProcessInstanceProxy(val instance: ProcessInstanceRef, val proxy: ActorRef)
  private val processInstanceMap: mutable.Map[(UserID, ProcessID), Future[ProcessInstanceProxy]] =
    mutable.Map((userId, processId) -> (for {
      proxy <- (actor ? GetProxyActor).mapTo[ActorRef]
    } yield new ProcessInstanceProxy(actor, proxy)))

  private val targetMap =
    Map(
      1 -> "@ec2-54-229-92-171.eu-west-1.compute.amazonaws.com:2552",
      2 -> "@ec2-54-229-82-150.eu-west-1.compute.amazonaws.com:2552",
      3 -> "@ec2-54-229-91-177.eu-west-1.compute.amazonaws.com:2552")

  def receive = {
    // TODO exchange GetSubjectAddr -> GetProcessInstanceAddr
    case GetProcessInstanceAddr(userId, processId) => {
      val processInstanceInfo =
        processInstanceMap
          .getOrElseUpdate(
            (userId, processId),
            createProcessInstanceEntry(userId, processId, ActorLocator.subjectProviderManagerActor))

      // create the answer
      val answer = for {
        info <- processInstanceInfo
      } yield info.proxy

      // send the answer to the sender, when its ready
      answer pipeTo sender
    }

    case s => {
      logger.error("got, but cant use " + s)
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
