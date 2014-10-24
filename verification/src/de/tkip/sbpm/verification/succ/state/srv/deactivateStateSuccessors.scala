package de.tkip.sbpm.verification.succ.state.srv

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.succ._
import de.tkip.sbpm.verification.lts.LtsState

object deactivateStateSuccessors extends ServiceSuccessorFunction[StateId] {
  def apply(global: GlobalFunctions,
            ltsState: LtsState,
            subject: SubjectStatus,
            state: ExtendedState,
            id: StateId): Set[Successor] = {

    // create the successor set
    ltsState.successorSet(
      // deactivate the target state and fire the transition
      subject.deactivateState(id).fireTransitionOf(state),
      state.singleTransition)
  }
}