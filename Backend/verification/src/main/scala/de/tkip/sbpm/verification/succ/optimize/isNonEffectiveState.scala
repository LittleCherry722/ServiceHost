package de.tkip.sbpm.verification.succ.optimize

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.succ._
import de.tkip.sbpm.verification.lts.LtsState

object isNonEffectiveState extends IsStateExecuteAbleFunction2 {
  def apply(global: GlobalFunctions,
            subject: SubjectStatus,
            state: ExtendedState): Boolean = {

    val observerPossible = subject.observerPossible

    def receiveExhausting() = {
      assert(
        state.stateType == Receive || state.stateType == Observer,
        "Cant Receive outside Receive State")
      state.transitions
        .collect { case Transition(c: CommunicationParams, _, _) => c }
        .forall {
          c =>
            c.max match {
              case Number(n) =>
                n <= subject.countMessages(c.messageType, c.subject) &&
                  subject.hasInputPoolSpace(c.messageType, Channel(c.subject, 0))
              case _ => false
            }
        }
    }

    def variableNotWritten(varName: String): Boolean = {
      true
    }
    state.stateType match {
      // End State is always possible
      case End => if (state.macroStates == Nil) !observerPossible else true
      // Observer state blocks other states
      case Act | SplitGuard | Split => !observerPossible
      case Join => false
      case Function => state.serviceParams match {
        case _: ExecuteMacro => true
        case v: VariableManipulation => variableNotWritten(v.target)

        case ActivateState(id) => !observerPossible
        case DeactivateState(id) => !observerPossible
        case _ => false

      }
      case Receive =>
        !observerPossible && receiveExhausting()
      case Observer => false
      case Send => false
    }
  }

}