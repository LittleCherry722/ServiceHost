package de.tkip.sbpm.verification.succ.state.srv

import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.verification.lts.LtsState
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.succ._

object variableManipulationSuccessors extends ServiceSuccessorFunction[VariableManipulation] {
  def apply(global: GlobalFunctions,
            ltsState: LtsState,
            subject: SubjectStatus,
            state: ExtendedState,
            operation: VariableManipulation): Set[Successor] = {
    val subjectMap = ltsState.subjectMap
    val channel = subject.channel
    operation match {

      case VariableManipulation(v1, op, v2opt, target) => {
        (op, v2opt) match {
          case (Selection, None) => {
            val messages = subject.variables.getMessageList(v1).messages
            val successorSubj = subject.fireTransitionOf(state)

            val m =
              if (messages.isEmpty) messages
              else List(messages.head)
            val res =
              for (message <- m) yield {
                val newSubj =
                  successorSubj.addVar(target, MessageList(List(message)))
                ltsState.successor(newSubj, state.singleTransition)
              }
            res.toSet +
              ltsState.successor(
                successorSubj.addVar(target, MessageList(messages)),
                state.singleTransition)
          }

          case (Difference, Some(v2)) => {
            val m1 = subject.variables.getMessageList(v1).messages
            val m2 = subject.variables.getMessageList(v2).messages.map(_.channel)

            val messages =
              m1.filterNot(m2 contains _.channel)

            val successorSubj =
              subject.fireTransitionOf(state).addVar(target, MessageList(messages))

            ltsState.successorSet(successorSubj, state.singleTransition)
          }

          case (ExtractMessageContent, None) => {
            val m1 = subject.variables.getMessageList(v1).messages

            val messages = m1.flatMap { m =>
              m.content match {
                case mc: MessageList => mc.messages
                case _ => List(m)
              }
            }

            val successorSubj =
              subject.fireTransitionOf(state).addVar(target, MessageList(messages))

            ltsState.successorSet(successorSubj, state.singleTransition)
          }
        }
      }
    }
  }
}