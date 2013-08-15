package de.tkip.sbpm.application.subject.behavior.state

import de.tkip.sbpm.application.subject.misc.ExecuteAction
import de.tkip.sbpm.application.subject.misc.ActionExecuted
import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.behavior.Transition
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes._
import akka.actor.Status.Failure

protected case class ModalSplitStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  protected def stateReceive = {

    case action: ExecuteAction => {
      // change to all States
      for (transition <- exitTransitions) {
        changeState(transition.successorID, null)
      }
      blockingHandlerActor ! ActionExecuted(action)
    }
  }

  override protected def getAvailableAction: Array[ActionData] = {
    // TODO welcher text?
    Array(ActionData("SPLIT", true, exitCondLabel))
  }
}