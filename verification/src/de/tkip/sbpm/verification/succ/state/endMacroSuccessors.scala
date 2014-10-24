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
object endMacroSuccessors extends StateSuccessorFunction {
  def apply(global: GlobalFunctions,
            ltsState: LtsState,
            subject: SubjectStatus,
            state: ExtendedState): Set[Successor] = {
    ltsState.successorSet(subject.leaveMacro(state), Transition(NoExitParams, 0, 0))
  }
} 