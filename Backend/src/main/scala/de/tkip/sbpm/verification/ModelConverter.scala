package de.tkip.sbpm.verification

import de.tkip.sbpm.model.{Graph, GraphMessage, GraphSubject}
import de.tkip.sbpm.newmodel._

/**
 * Created by Arne Link on 24.10.14.
 */
object ModelConverter {

  def convertForVerification(model: Graph) : ProcessModel = {
    val subjects: Set[SubjectLike] = model.subjects.values.map(subjectToVerification).toSet
    val messageTypes = model.messages.map((messagesToVerification _).tupled)

    ProcessModel(
      id =  model.id.get,
      name = "Prozess",
      subjects = subjects,
      messageTypes = null // fix this!
    )
  }

  private def messagesToVerification(name: String, message: GraphMessage): ((String, MessageContentType)) = {
    null
  }

  private def subjectToVerification(sub: GraphSubject) : SubjectLike = {
    if (sub.subjectType == "external" && sub.externalType == Some("external")) {
      ExternalSubject(
        id = "",
        name = "",
        relatedProcess = 0,
        multi = false
      )
    } else if (sub.subjectType == "external" && sub.externalType == Some("interface")) {
      InterfaceSubject(
        id = "",
        name = "",
        multi = false,
        ipSize = 1,
        states = Set(),
        startState = 0
      )
    } else {
      Subject(
        id = "",
        name = "",
        startSubject = false,
        multi = false,
        ipSize = 1,
        states = Set(),
        startState = 0,
        macros = Set()
      )
    }
  }
}
