package de.tkip.sbpm.verification.succ.state.srv

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.succ._
import de.tkip.sbpm.verification.lts.LtsState

object openIPSuccessors extends ServiceSuccessorFunction[OpenIP] {
  def apply(global: GlobalFunctions,
            ltsState: LtsState,
            subject: SubjectStatus,
            state: ExtendedState,
            openIP: OpenIP): Set[Successor] = {
    ltsState.successorSet(
      subject.open(openIP).fireTransitionOf(state),
      state.singleTransition)
  }
}