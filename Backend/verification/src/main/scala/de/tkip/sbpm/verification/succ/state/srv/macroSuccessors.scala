package de.tkip.sbpm.verification.succ.state.srv

import de.tkip.sbpm.verification.lts.LtsState
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.succ._

object macroSuccessors extends ServiceSuccessorFunction[String] {
  def apply(global: GlobalFunctions,
            ltsState: LtsState,
            subject: SubjectStatus,
            state: ExtendedState,
            macroName: String): Set[Successor] = {

    val sMacro = global.model.subject(subject.id).sMacro(macroName)

    ltsState.successorSet(
      if (state.data == Some(MacroDone)) subject.fireTransitionOf(state)
      else subject.enterMacro(state, macroName), state.singleTransition)
  }
}