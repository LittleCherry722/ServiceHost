package de.tkip.sbpm.verification.succ.state.srv

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.succ._
import de.tkip.sbpm.verification.lts.LtsState

object closeIPSuccessors extends ServiceSuccessorFunction[CloseIP] {
  def apply(global: GlobalFunctions,
            ltsState: LtsState,
            subject: SubjectStatus,
            state: ExtendedState,
            closeIP: CloseIP): Set[Successor] = {
    // create the channels which should be closed
    val subjectIds = closeIP.subjectId match {
      case Some(subjectId) => Set(subjectId)
      case None => global.subjectIds
    }
    val messageTypes = closeIP.subjectId match {
      case Some(messageType) => Set(messageType)
      case None => global.messageTypes
    }
    val close =
      for {
        subjectId <- subjectIds
        messageType <- messageTypes
      } yield (subjectId, messageType)

    ltsState.successorSet(
      subject.close(close).fireTransitionOf(state),
      state.singleTransition)
  }
}