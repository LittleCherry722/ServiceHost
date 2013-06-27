package de.tkip.sbpm.application.subject.misc

import akka.actor.Actor
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import akka.actor.ActorRef
import scala.collection.mutable
import de.tkip.sbpm.application.miscellaneous.ProcessInstanceCreated

case class GetSubjectAddr(userId: UserID, processId: ProcessID, subjectId: SubjectID)
case class ReturnSubjectAddr(request: GetSubjectAddr, addr: ActorRef)

class ProcessInstanceManagerActor(userId: UserID, processId: ProcessID, actor: ProcessInstanceRef) extends Actor {

  private lazy val processManagerActor = ActorLocator.processManagerActor

  private val processInstanceMap: mutable.Map[(UserID, ProcessID), ActorRef] =
    mutable.Map[(UserID, ProcessID), ActorRef]((userId, processId) -> actor)

  //  private var waitingList: (Any, ActorRef) = null

  private val waitingMessages: mutable.Map[ProcessID, mutable.Queue[(ActorRef, GetSubjectAddr)]] =
    mutable.Map[ProcessID, mutable.Queue[(ActorRef, GetSubjectAddr)]]()

  //  private var block: GetSubjectAddr = null
  // map: UserID, processID -> ActorAddr

  // create process instance
  // create external subject
  // register process instance

  // =>
  // GetSubjectRef(UserID, ProcessID, SubjectId)
  // 

  def receive = {
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

      //      block = message
      // TODO self einpacken
      val createMessage = CreateProcessInstance(userId, processId, Some(self))
      createMessage.sender = self

      processManagerActor ! createMessage

      waitingMessages(processId) = mutable.Queue((sender, message))

      //      waitingList = (message, sender)
      // TODO waiting queue
    }

    case pc: ProcessInstanceCreated => {

      // register the process instane
      processInstanceMap +=
        (pc.request.userID, pc.request.processID) -> pc.processInstanceActor

      // forward all stored messages for this process instance
      for ((actor, message) <- waitingMessages(processId)) {
        pc.processInstanceActor.tell(message, actor)
      }
      // clear the queue of the sent messages
      waitingMessages(processId) = mutable.Queue()

      //      pc.processInstanceActor.tell(waitingList._1, waitingList._2)
      //
      //      block = null
    }

    case _ => {
      // TODO
    }
  }
}