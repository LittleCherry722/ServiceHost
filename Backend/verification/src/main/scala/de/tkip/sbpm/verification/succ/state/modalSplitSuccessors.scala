package de.tkip.sbpm.verification.succ.state

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.succ._
import de.tkip.sbpm.verification.lts.LtsState

/**
 * Creates the Successors for ModalSplitStates
 */
object modalSplitSuccessors extends StateSuccessorFunction {
  def apply(global: GlobalFunctions,
            ltsState: LtsState,
            subject: SubjectStatus,
            state: ExtendedState): Set[Successor] = {

    // write the number of splits into the state
    val newState = state.appendModal(state.transitions.size)

    val successors =
      subject.updateState(state, newState).fireModalSplit(newState)
    ltsState.successorSet(successors, state.transitions.head)
  }
}