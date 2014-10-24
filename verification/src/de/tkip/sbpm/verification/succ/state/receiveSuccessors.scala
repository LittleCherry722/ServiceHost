package de.tkip.sbpm.verification.succ.state

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.succ._
import de.tkip.sbpm.verification.lts.LtsState
import de.tkip.sbpm.verification.lts.LtsLabel
import de.tkip.sbpm.verification.lts.ReceiveLabel
import de.tkip.sbpm.verification.lts.SendLabel

/**
 * Creates the Successors for Receive- and ObserverStates
 */
object receiveSuccessors extends StateSuccessorFunction {
  def apply(global: GlobalFunctions,
            ltsState: LtsState,
            subject: SubjectStatus,
            state: ExtendedState): Set[Successor] = {

    val isObserver = state.stateType == Observer

    val successors: Set[Successor] =
      for (transition <- state.communicationTransitions) yield {
        val Transition(params: CommunicationParams, _, _) = transition

        val otherModel = global.model.subjectOrInterFace(params.subject)

        // the killstates only matter if this is an observer!
        val killStates: Set[ExtendedState] =
          if (isObserver) subject.currentStates.filterNot(_.stateType == Observer)
          else Set()
        otherModel match {
          case s: Subject => {

            val messageCount =
              subject.countMessages(params.messageType, params.subject)

            val (min, max) = (params.min, params.max) match {
              case (Number(min), Number(max)) => (min, max)
              case (Number(min), AllMessages) => (min, messageCount)
              case (AllMessages, AllMessages) => (messageCount, messageCount)
              case (m, n) => throw new IllegalArgumentException("min/max cant be %s/%s".format(m, n))
            }

            if (messageCount >= min) {
              val receiveCount = Math.min(max, messageCount)

              val newSubject =
                subject
                  // receive the messages
                  .receiveMessages(params.messageType, params.subject, receiveCount, params.storeVar)
                  // kill the states (empty if no observer)
                  .killStates(killStates)
                  // and fire the transition
                  .fireTransition(state, transition)


              val nums = subject.ip.pullUsers(params.messageType, params.subject, receiveCount).toSet

              ltsState.successor(newSubject, transition, if (isObserver) ObserverState else UsualState)
                .copy(label = ReceiveLabel(subject.id, subject.channel.agentId, params.messageType, params.subject, nums))
            } 
            else LtsSuccessor(null)
          }

          case i: InstantInterface => {

            val newSubject =
              subject
                // kill the states (empty if no observer)
                .killStates(killStates)
                // and fire the transition
                .fireTransition(state, transition)

            ltsState.successor(newSubject, transition, if (isObserver) ObserverState else UsualState)
              //                  .copy(label = SendLabel(params.subject, params.messageType, subject.id))
              .copy(label = ReceiveLabel(subject.id, subject.channel.agentId, params.messageType, params.subject, Set(0)))
          }
        }
      }
    successors.filter { case s: LtsSuccessor => s.state != null; case _ => true }
  }
}