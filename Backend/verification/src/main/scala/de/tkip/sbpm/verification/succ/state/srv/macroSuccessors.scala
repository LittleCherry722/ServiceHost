package de.tkip.sbpm.verification.succ.state.srv

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.succ._
import de.tkip.sbpm.verification.lts.LtsState

object macroSuccessors extends ServiceSuccessorFunction[String] {
  def apply(global: GlobalFunctions,
            ltsState: LtsState,
            subject: SubjectStatus,
            state: ExtendedState,
            macroName: String): Set[Successor] = {

    val macro = global.model.subject(subject.id).macro(macroName)

    ltsState.successorSet(
      if (state.data == Some(MacroDone)) subject.fireTransitionOf(state)
      else subject.enterMacro(state, macroName), state.singleTransition)
  }
}