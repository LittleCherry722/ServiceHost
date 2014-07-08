package de.tkip.sbpm.application.subject.behavior.state

import de.tkip.sbpm.application.subject.misc.ExecuteAction
import de.tkip.sbpm.application.subject.misc.ActionExecuted
import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.behavior.Transition
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes._
import akka.actor.Status.Failure

case object TransitionJoined

case class ModalJoinStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  // TODO how to calculate that?
  private val numberOfJoins = data.visitedModalSplit.pop._2
  private var remaining = numberOfJoins - 1

  protected def stateReceive = {

    case TransitionJoined => {
      if (remaining == 0) {
        log.error("ModalJoinStateActor got more joins than possible!")
      } else {
        remaining -= 1
      }
      tryChangeState()
    }

    case action: ExecuteAction => {
      log.debug(s"Got $action, but cannot execute")
    }
  }

  override protected def getAvailableAction: Array[ActionData] = {
    //    val possible = remaining == 0
    //    exitTransitions.map((t: Transition) => ActionData(t.messageType, possible, exitCondLabel))
    // the modal join has no actions for the user
    Array()
  }

  private def tryChangeState() {
    if (remaining == 0) {
      changeState(exitTransitions.head.successorID, data, null)
    }
  }
}
