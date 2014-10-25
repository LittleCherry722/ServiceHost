package de.tkip.sbpm.verification.succ.state

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.succ._
import de.tkip.sbpm.verification.succ.state.srv._
import de.tkip.sbpm.verification.lts.LtsState

/**
 * Creates the Successors for Service/FunctionStates
 */
object serviceSuccessors extends StateSuccessorFunction {

  def apply(global: GlobalFunctions,
            ltsState: LtsState,
            subject: SubjectStatus,
            state: ExtendedState): Set[Successor] = {

    def call[A](fct: ServiceSuccessorFunction[A], a: A) = fct(global, ltsState, subject, state, a)

    val channel = subject.channel

    state.serviceParams match {
      case openIP: OpenIP => call(openIPSuccessors, openIP)
      case closeIP: CloseIP => call(closeIPSuccessors, closeIP)
      case cns: NewSubjectInstances => call(createNewSubjectsSuccessors, cns)
      case vm: VariableManipulation => call(variableManipulationSuccessors, vm)
      case ActivateState(id) => call(activateStateSuccessors, id)
      case DeactivateState(id) => call(deactivateStateSuccessors, id)
      case ExecuteMacro(name) => call(macroSuccessors, name)
    }
  }
}