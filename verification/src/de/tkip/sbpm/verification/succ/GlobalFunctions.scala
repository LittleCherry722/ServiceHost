package de.tkip.sbpm.verification.succ

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.succ.state._

class GlobalFunctions(val model: ProcessModel, val optimized: Boolean = false) {
  def createSubject(channel: Channel): SubjectStatus = {
    val subject = model.subject(channel.subjectId)
    SubjectStatus(subject, channel, InputPool.empty, Variables.empty,
      Set(ExtendedState(subject.state(subject.startState))))
  }

  lazy val messageTypes: Set[MessageType] = {
    val messageTypes =
      for {
        subject <- model.subjects
        state <- subject.states
        Transition(comParam: CommunicationParams, _, _) <- state.transitions
      } yield comParam.messageType
    messageTypes.toSet
  }

  lazy val subjectIds: Set[SubjectId] = {
    model.subjects.map(_.id)
  }
}