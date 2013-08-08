package de.tkip.sbpm.application.subject.behavior.state

import de.tkip.sbpm.application.subject.misc.ExecuteAction
import de.tkip.sbpm.application.subject.misc.ActionExecuted
import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.behavior.Transition
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes._
import akka.actor.Status.Failure

protected case class ModalJoinStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  protected def stateReceive = {

    case action: ExecuteAction => {
      // TODO only 1 exitTransition is allowed!
      changeState(exitTransitions.head.successorID, null)
      blockingHandlerActor ! ActionExecuted(action)
    }
  }

  override protected def getAvailableAction: Array[ActionData] = {
    val possible = true
    exitTransitions.map((t: Transition) => ActionData(t.messageType, possible, exitCondLabel))
  }
}