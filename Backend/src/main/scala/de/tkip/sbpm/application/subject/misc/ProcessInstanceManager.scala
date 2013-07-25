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

case class GetSubjectAddr(userId: UserID, processId: ProcessID, subjectId: SubjectID)
case class ReturnSubjectAddr(request: GetSubjectAddr, addr: ActorRef)
// new case classes: TODO
case object GetProxyActor

class ProcessInstanceManagerActor(userId: UserID, processId: ProcessID, actor: ProcessInstanceRef) extends Actor {
  implicit val timeout = Timeout(2000)
  //  import context.dispatcher

  protected val logger = Logging(context.system, this)

  private lazy val processManagerActor = ActorLocator.processManagerActor

  //  private def proxyOf(instance: ProcessInstanceRef) = {
  //    implicit val timeout = Timeout(4000)
  //    val future = (instance ? GetProxyActor).mapTo[ActorRef]
  //    new ProcessInstanceProxy(instance, future)
  //  }
  private class ProcessInstanceProxy(val instance: ProcessInstanceRef, val proxy: ActorRef)
  private val processInstanceMapTEST: mutable.Map[(UserID, ProcessID), Future[ProcessInstanceProxy]] =
    // TODO nciht actor actor
    mutable.Map((userId, processId) -> Future.successful(new ProcessInstanceProxy(actor, actor)))

  private val processInstanceMap: mutable.Map[(UserID, ProcessID), ProcessInstanceRef] =
    mutable.Map[(UserID, ProcessID), ActorRef]((userId, processId) -> actor)

  //  private var waitingList: (Any, ActorRef) = null

  private val waitingMessages: mutable.Map[ProcessID, mutable.Queue[(ActorRef, GetSubjectAddr)]] =
    mutable.Map[ProcessID, mutable.Queue[(ActorRef, GetSubjectAddr)]]()

  private val targetMap =
    Map(
      1 -> "@127.0.0.1:2552",
      2 -> "@127.0.0.1:2552",
      3 -> "@127.0.0.1:2552")

  def receive = {
    case message @ GetSubjectAddr(userId, processId, _) => {
      val processInstanceInfo =
        processInstanceMapTEST
          .getOrElseUpdate(
            (userId, processId),
            // TODO target sinnvoll setzen
            createProcessInstanceEntry(userId, processId, ActorLocator.subjectProviderManagerActor))


      // create the answer
      val answer = for {
        info <- processInstanceInfo
      } yield info.proxy

      // send the answer to the sender, when its ready
      answer pipeTo sender
    }
    //    case x: Int => {
    //      val k = processInstanceMapTEST(x, x)
    //      implicit val timeout = Timeout(4000)
    //
    //      case class A(pi: ActorRef, proxy: ActorRef)
    //
    //      val r = createProcessInstanceEntry(x, x, processManagerActor)
    //
    //      val n = r map {
    //        a => (x, a.proxy)
    //      }
    //      n pipeTo sender
    //    }

    // the processinstance exists
    case message @ GetSubjectAddr(userId, processId, subjectId) if (processInstanceMap.contains((userId, processId))) => {
      processInstanceMap((userId, processId)) forward message
    }
    // the processinstaces does not exists yet but a create request has been send
    case message @ GetSubjectAddr(userId, processId, subjectId) if (waitingMessages.contains(processId)) => {
      waitingMessages(processId) += ((sender, message))
    }
    // the processinstance does not exists
    case message @ GetSubjectAddr(userId, processId, subjectId) => {

      val createMessage = CreateProcessInstance(userId, processId, Some(self))
      createMessage.sender = self

      logger.debug("creating Process Instance: " + createMessage)
      val targetAddress = targetMap.getOrElse(processId, "")
      val targetProcessManager =
        context.actorFor("akka://de-tkip-sbpm-Boot" + targetAddress +
          "/user/" + ActorLocator.processManagerActorName)

      logger.debug("sending message to " + targetProcessManager)

      targetProcessManager ! createMessage

      waitingMessages(processId) = mutable.Queue((sender, message))
    }

    //    case pc: ProcessInstanceCreated => {
    //      logger.debug("received: " + pc)
    //
    //      // register the process instane
    //      processInstanceMap +=
    //        (pc.request.userID, pc.request.processID) -> pc.processInstanceActor
    //
    //      // forward all stored messages for this process instance
    //      for ((actor, message) <- waitingMessages(pc.request.processID)) {
    //        pc.processInstanceActor.tell(message, actor)
    //      }
    //      // clear the queue of the sent messages
    //      waitingMessages(processId) = mutable.Queue()
    //    }
    //
    case s => {
      logger.error("got, but cant use " + s)
    }
  }

  //  override implicit def executionContext = ExecutionContext.Implicits.global
  private def createProcessInstanceEntry(userId: UserID,
    processId: ProcessID,
    targetManager: ProcessManagerRef): Future[ProcessInstanceProxy] = {
    // crea the message which is used to create a process instance
    val createMessage = CreateProcessInstance(userId, processId, Some(self))
    //    createMessage.sender = self
    // the creation and question for a proxy actor
    // TODO test
    //    implicit val context: ExecutionContext = ExecutionContext.global
    //    import ExecutionContext.Implicits.global

    val info =
      for {
        // creat the processinstance
        created <- (targetManager ? createMessage).mapTo[ProcessInstanceCreated]
        instanceRef = created.processInstanceActor
        // ask for the proxy actor
        proxy <- (instanceRef ? GetProxyActor).mapTo[ActorRef]
      } yield new ProcessInstanceProxy(instanceRef, proxy)
    info
  }
}