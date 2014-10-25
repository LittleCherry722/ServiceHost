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
object endSuccessors extends StateSuccessorFunction {
  def apply(global: GlobalFunctions,
            ltsState: LtsState,
            subject: SubjectStatus,
            state: ExtendedState): Set[Successor] = {
    if (state.macroStates != Nil) {
      // if its inside a macro get the macro successors
      endMacroSuccessors(global, ltsState, subject, state)
    } else if (!subject.ip.isEmpty) {
      System.err.println("InputPool is not Empty for: " + subject + "; " + subject.ip);
      Set()
    } else
      Set(ltsState - subject.channel)
  }
} 