package de.tkip.sbpm.verification.succ

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.succ.state._
import de.tkip.sbpm.verification.lts.LtsState
import de.tkip.sbpm.verification.lts.Tau

object stateSuccessors extends StateSuccessorFunction {

  def apply(global: GlobalFunctions,
            ltsState: LtsState,
            subject: SubjectStatus,
            state: ExtendedState): Set[Successor] = {
    recursive_successors(global, ltsState, subject, state: ExtendedState, Set())
  }

  private def recursive_successors(global: GlobalFunctions,
                                   ltsState: LtsState,
                                   subject: SubjectStatus,
                                   state: ExtendedState,
                                   visited: Set[SubjectSuccessor]): Set[Successor] = {
    def call(fct: StateSuccessorFunction) = fct(global, ltsState, subject, state)

    // get the state specific successors
    // select the right successor function 
    val successors: Set[Successor] = state.stateType match {
      case Act | SplitGuard => call(actSuccessors)
      case Observer | Receive => call(receiveSuccessors)
      case Send => call(sendSuccessors)
      case End => call(endSuccessors)
      case Join => call(modalJoinSuccessors)
      case Split => call(modalSplitSuccessors)
      case Function => call(serviceSuccessors)
      //      case MacroEnd => call(endMacroSuccessors)
    }

    // filter transition priorities

    // Split and Breakup Transitions
    val optionalSuccessors: Set[Successor] =
      for (transition <- state.cancelTransitions) yield {
        ltsState
          .successor(subject.fireTransition(state, transition), transition)
          .copy(optional = true)
      }

    // get the max value of the used transitions
    // only main transitions are used for this priority
    val max = successors.foldLeft(-100)((i, s) =>
      if (s.optional) i else Math.max(i, s.transitionPriority))

    // all successors are the state specific and the optional
    val allSuccessors = successors | optionalSuccessors

    // filter the successors
    // filter for the highest priority transition
    val filteredSuccessors: Set[Successor] =
      for (
        successor <- allSuccessors collect {
          case s: Successor if (s.transitionPriority >= max) => s
        }
      ) yield successor

    // optimization:
    import de.tkip.sbpm.verification.succ.optimize._
    if (global.optimized) {
      val (remove, newStates) =
        (for {
          x @ SubjectSuccessor(subject, Tau, _, state, _) <- filteredSuccessors
          //          x @ SubjectSuccessor(subject, _, _, state, _) <- filteredSuccessors
          if (state == UsualState)
          // avoid infinity recursion
          if (!visited.contains(x))
          sub = subject
          st <- sub.activeStates
          if (isNonEffectiveState(global, sub, st))
          ns <- recursive_successors(global, ltsState.exchange(sub), sub, st, visited + x)
        } yield (x, ns)).unzip

      if (newStates.size > 0) filteredSuccessors -- remove ++ newStates
      else filteredSuccessors
    } else {
      filteredSuccessors
    }
  }
}