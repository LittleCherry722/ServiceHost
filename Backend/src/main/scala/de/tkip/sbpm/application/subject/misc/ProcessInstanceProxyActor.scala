package de.tkip.sbpm.application.subject.misc

import akka.actor.Actor
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.ProcessGraph
import de.tkip.sbpm.model.ExternalSubject

class ProcessInstanceProxyActor(graph: ProcessGraph) extends Actor {

  // this map maps the external subjects of this process to the related subject id
  private val subjectIdMap: Map[(ProcessID, SubjectID), SubjectID] =
    graph.subjects collect {
      case (subjectId, external: ExternalSubject) =>
        (external.relatedProcessId, external.relatedSubjectId) -> subjectId
    } toMap

  def receive = {
    case message: SubjectToSubjectMessage => {
      // Exchange the sending subject id
      message.from =
        subjectIdMap.getOrElse((message.processID, message.from), message.from)
      context.parent forward message
    }

    case message => {
      context.parent forward message
    }
  }
}