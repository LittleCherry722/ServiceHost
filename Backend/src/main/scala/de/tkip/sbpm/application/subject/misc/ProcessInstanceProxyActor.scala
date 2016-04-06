package de.tkip.sbpm.application.subject.misc

import akka.actor.ActorRef
import akka.pattern.pipe
import akka.util.Timeout
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.{RequestUserID, SubjectInformation}
import de.tkip.sbpm.instrumentation.InstrumentedActor

import scala.concurrent.duration._

class ProcessInstanceProxyActor(id: ProcessInstanceID
                               ,processId: ProcessID
                               ,inSubjectMap: Map[String, String]
                               ,createMessage: CreateProcessInstance) extends InstrumentedActor {

  import context.dispatcher

  private lazy val contextResolver = ActorLocator.contextResolverActor
  implicit val timeout = Timeout(4 seconds)

  private case class RandomUsersLoaded(message: SubjectToSubjectMessage, from: ActorRef, userIds: Array[UserID])

  /* TODO: Describe the message flow
   *
   */
  def wrappedReceive = {
    case message: SubjectToSubjectMessage =>
      log.debug("got {} from {}", message, sender)
      val target = message.target
      val localTarget = target.copy(toExternal = false, variable = None, subjectID = inSubjectMap.getOrElse(target.subjectID, target.subjectID))
      val localizedMessage = message.copy(target = localTarget, from = inSubjectMap.getOrElse(message.from, message.from))
      log.debug("localized message: {}", localizedMessage)
      if (message.target.toUnknownUsers) {
        loadRandomUsers(localizedMessage)
      } else {
        log.debug("Just forwarding message {} to {}", localizedMessage, context.parent)
        context.parent.forward(localizedMessage)
      }

    case RandomUsersLoaded(message, from, userIds) =>
      log.debug("random users: {}", userIds.mkString(","))
      val selectedUsers = selectRandomUsers(message, userIds)
      log.debug("selected users: {}", userIds.mkString(","))
      message.target.insertTargetUsers(selectedUsers)
      context.parent.tell(message, from)

    case message => context.parent.forward(message)
  }

  private def loadRandomUsers(message: SubjectToSubjectMessage) {
    log.debug("load random users...")

    val request = RequestUserID(SubjectInformation(processId, id, message.to), identity)
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
