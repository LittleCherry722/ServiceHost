package de.tkip.sbpm.verification.succ.state

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.succ._
import de.tkip.sbpm.verification.lts.LtsState

/**
 * Creates the Successors for ModalJoinStates
 */
object modalJoinSuccessors extends StateSuccessorFunction {
  def apply(global: GlobalFunctions,
            ltsState: LtsState,
            subject: SubjectStatus,
            state: ExtendedState): Set[Successor] = {
    state.data match {
      case Some(ModulJoinStateData(count)) if {
        count == state.modalSplitStack.head
      } =>
        // modalJoin must pop the stack, which was filled by the modal Split
        val updatedState = state.popModal
        ltsState.successorSet(
          subject
            .updateState(state, updatedState)
            .fireTransitionOf(updatedState), state.singleTransition)
      case _ => Set()
    }
  }
}