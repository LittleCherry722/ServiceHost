package de.tkip.sbpm.verification.succ.state
import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.succ._
import de.tkip.sbpm.verification.lts.LtsState

/**
 * Creates the Successors for Act- and SplitGuardStates
 */
object actSuccessors extends StateSuccessorFunction {
  def apply(global: GlobalFunctions,
            ltsState: LtsState,
            subject: SubjectStatus,
            state: ExtendedState) = {

    for (transition <- state.exitTransitions)
      yield ltsState.successor(subject.fireTransition(state, transition), transition)
  }
}
