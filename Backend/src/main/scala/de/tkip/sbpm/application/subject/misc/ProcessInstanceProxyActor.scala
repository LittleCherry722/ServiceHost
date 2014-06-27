package de.tkip.sbpm.application.subject.misc

import akka.actor.{ ActorRef }
import akka.util.Timeout
import akka.pattern.{ ask, pipe }
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.instrumentation.InstrumentedActor
import de.tkip.sbpm.model.ProcessGraph
import de.tkip.sbpm.model.ExternalSubject
import de.tkip.sbpm.application.{ SubjectInformation, RequestUserID }
import de.tkip.sbpm.ActorLocator
import scala.concurrent.duration._
import de.tkip.sbpm.logging.DefaultLogging
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import akka.event.Logging

class ProcessInstanceProxyActor(id: ProcessInstanceID, processId: ProcessID, graph: ProcessGraph, createMessage: CreateProcessInstance) extends InstrumentedActor with DefaultLogging {

  import context.dispatcher

  private lazy val contextResolver = ActorLocator.contextResolverActor
  implicit val timeout = Timeout(4 seconds)

  private case class RandomUsersLoaded(message: SubjectToSubjectMessage, from: ActorRef, userIds: Array[UserID])

  def wrappedReceive = {
    case message: SubjectToSubjectMessage => {
      log.debug("got {} from {}", message, sender)
      val localizedMessage = message.copy(target = message.target.copy(toExternal = false))
      log.debug("localized message: {}", localizedMessage)
      if (message.target.toUnknownUsers) {
        loadRandomUsers(localizedMessage)
      } else {
        log.debug("Just forwarding message {} to {}", localizedMessage, context.parent)
        context.parent forward localizedMessage
      }
    }

    case RandomUsersLoaded(message, from, userIds) => {
      log.debug("random users: {}", userIds.mkString(","))
      val selectedUsers = selectRandomUsers(message, userIds)
      log.debug("selected users: {}", userIds.mkString(","))
      message.target.insertTargetUsers(selectedUsers)
      context.parent.tell(message, from)
    }

    case message => {
      context.parent forward message
    }
  }

  private def loadRandomUsers(message: SubjectToSubjectMessage) {
    log.debug("load random users...")

    val request = RequestUserID(SubjectInformation(processId, id, message.to), userIds => userIds)
    val result = (contextResolver ?? request).mapTo[Array[UserID]]
    val from = context.sender
    result.map(userIds => RandomUsersLoaded(message, from, userIds)) pipeTo self
  }

  // for better testing, use always the first users for now
  private def selectRandomUsers(message: SubjectToSubjectMessage, userIds: Array[UserID]): Array[UserID] = {
    if (userIds.isEmpty) {
      throw new IllegalStateException("no user found")
    }

    userIds.take(1)
  }
}
