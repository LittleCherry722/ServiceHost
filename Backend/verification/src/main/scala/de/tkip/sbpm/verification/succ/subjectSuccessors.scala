package de.tkip.sbpm.verification.succ

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.lts.LtsState

object subjectSuccessors extends SubjectSuccessorFunction {
  def apply(global: GlobalFunctions,
            ltsState: LtsState,
            subject: SubjectStatus): Set[Successor] = {

    val endState = subject.activeStates.exists(_.stateType == End)
    
    val filteredStates =
      if (endState) subject.currentStates.filter(_.stateType == End)
      else if (subject.observerPossible) subject.currentStates.filter(_.stateType == Observer)
      else subject.currentStates

    // create all possible successors by creating the successors for every states
    val successors = for {
      state <- filteredStates
      successor <- stateSuccessors(global, ltsState, subject, state)
    } yield successor

    // filter state Priorities 
    val filteredSuccessors: Set[Successor] = {
      // end states have the highest priority
      if (successors exists (s => !s.optional && s.endState)) {
        successors filter (_.endState)
        // observer states have a higher priority than normal states
      } else if (successors exists (s => !s.optional && s.obsState)) {
        successors filter (_.obsState)
        // if there is nothing to filter, don't filter
      } else successors

      successors
    }

    // return the filtered successors
    filteredSuccessors
  }
} 