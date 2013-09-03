package de.tkip.sbpm.application.subject.misc

import akka.actor.{ActorRef, Actor}
import akka.util.Timeout
import akka.pattern.{ask, pipe}
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.ProcessGraph
import de.tkip.sbpm.model.ExternalSubject
import de.tkip.sbpm.application.{SubjectInformation, RequestUserID}
import de.tkip.sbpm.ActorLocator
import scala.concurrent.duration._
import de.tkip.sbpm.logging.DefaultLogging

class ProcessInstanceProxyActor(id: ProcessInstanceID, processId: ProcessID, graph: ProcessGraph) extends Actor with DefaultLogging {

  import context.dispatcher

  // this map maps the external subjects of this process to the related subject id
  private val subjectIdMap: Map[(ProcessID, SubjectID), SubjectID] =
    graph.subjects collect {
      case (subjectId, external: ExternalSubject) =>
        (external.relatedProcessId, external.relatedSubjectId) -> subjectId
    } toMap

  private lazy val contextResolver = ActorLocator.contextResolverActor
  implicit val timeout = Timeout(4 seconds)

  private case class RandomUsersLoaded(message: SubjectToSubjectMessage, from: ActorRef, userIds: Array[UserID])

  def receive = {
    case message: SubjectToSubjectMessage => {
      log.debug("S2SMsg " + message + " from " + sender)
      // Exchange the sending subject id
      message.from =
        subjectIdMap.getOrElse((message.processID, message.from), message.from)

      if(message.target.toUnknownUsers) {
        loadRandomUsers(message)
      } else {
        context.parent forward message
      }
    }

    case RandomUsersLoaded(message, from, userIds) => {
      log.info("random users: "+userIds.mkString)
      val selectedUsers = selectRandomUsers(message, userIds)
      log.info("selected users: "+userIds.mkString)
      message.target.insertTargetUsers(selectedUsers)
      context.parent.tell(message, from)
    }

    case message => {
      context.parent forward message
    }
  }

  private def loadRandomUsers(message: SubjectToSubjectMessage) {
    log.info("load random users...")
    val request =  RequestUserID(SubjectInformation(processId, id, message.to), userIds => userIds)
    val result = (contextResolver ? request).mapTo[Array[UserID]]
    result.map(userIds => RandomUsersLoaded(message, sender, userIds)) pipeTo self
  }

  // for better testing, use always the first users for now
  private def selectRandomUsers(message: SubjectToSubjectMessage, userIds: Array[UserID]) :Array[UserID] = {
    if(userIds.isEmpty) {
      throw new IllegalStateException("no user found")
    }

    userIds.take(1)
  }
}