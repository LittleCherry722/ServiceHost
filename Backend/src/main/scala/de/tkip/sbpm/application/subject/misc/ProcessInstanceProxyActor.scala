package de.tkip.sbpm.application.subject.misc

import akka.actor.Actor
import akka.util.Timeout
import akka.pattern.{ask, pipe}
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.ProcessGraph
import de.tkip.sbpm.model.ExternalSubject
import de.tkip.sbpm.application.{SubjectInformation, RequestUserID}
import de.tkip.sbpm.ActorLocator
import scala.concurrent.duration._
import de.tkip.sbpm.logging.DefaultLogging

class ProcessInstanceProxyActor(id: ProcessInstanceID, graph: ProcessGraph) extends Actor with DefaultLogging {

  import context.dispatcher

  // this map maps the external subjects of this process to the related subject id
  private val subjectIdMap: Map[(ProcessID, SubjectID), SubjectID] =
    graph.subjects collect {
      case (subjectId, external: ExternalSubject) =>
        (external.relatedProcessId, external.relatedSubjectId) -> subjectId
    } toMap

  private lazy val contextResolver = ActorLocator.contextResolverActor
  implicit val timeout = Timeout(4 seconds)

  private case class RandomUsersLoaded(message: SubjectToSubjectMessage, userIds: Array[UserID])

  def receive = {
    case message: SubjectToSubjectMessage => {
      // Exchange the sending subject id
      message.from =
        subjectIdMap.getOrElse((message.processID, message.from), message.from)

      if(message.target.toUnknownUsers) {
        loadRandomUsers(message)
      } else {
        context.parent forward message
      }
    }

    case RandomUsersLoaded(message, userIds) => {
      log.info("random users: "+userIds)
      val selectedUsers = selectRandomUsers(message, userIds)
      log.info("selected users: "+userIds)
      message.target.insertTargetUsers(selectedUsers)
      context.parent forward message
    }

    case message => {
      context.parent forward message
    }
  }

  private def loadRandomUsers(message: SubjectToSubjectMessage) {
    log.info("load random users...")
    val request =  RequestUserID(SubjectInformation(message.processID, id, message.to), userIds => userIds)
    val result = (contextResolver ? request).mapTo[Array[UserID]]
    result.map(userIds => RandomUsersLoaded(message, userIds)) pipeTo self
  }

  // for better testing, use always the first users for now
  private def selectRandomUsers(message: SubjectToSubjectMessage, userIds: Array[UserID]) :Array[UserID] = {
    if(userIds.size < message.target.min) {
      throw new IllegalStateException("at least "+message.target.min+" users required, but only "+userIds.size+" found")
    }

    userIds.take(message.target.min)
  }
}