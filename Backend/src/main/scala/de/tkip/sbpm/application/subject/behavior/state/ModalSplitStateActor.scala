package de.tkip.sbpm.application.subject.behavior.state

import de.tkip.sbpm.application.subject.misc.ExecuteAction
import de.tkip.sbpm.application.subject.misc.ActionExecuted
import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.behavior.Transition
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes._
import akka.actor.Status.Failure
import de.tkip.sbpm.application.miscellaneous.BlockUser
import de.tkip.sbpm.application.miscellaneous.UnBlockUser

protected case class ModalSplitStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  override def preStart() {
    // Block the user while prestart!
    blockingHandlerActor ! BlockUser(userID)
    super.preStart()
    // Fire Modal Split in the preStart (Dont wait for some interaction)
    fireModalSplit()
    blockingHandlerActor ! UnBlockUser(userID)
  }

  protected def stateReceive = {

    case action: ExecuteAction => {
      log.debug(s"Got $action, but cannot execute")
    }
  }

  override protected def getAvailableAction: Array[ActionData] = {
    // TODO welcher text?
    //    Array(ActionData("SPLIT", true, exitCondLabel))
    // the modal split has no actions for the user
    Array()
  }

  private def fireModalSplit() {
    data.visitedModalSplit.push((data.stateModel.id, exitTransitions.size))
    // change to all States
    for (transition <- exitTransitions) {
      changeState(transition.successorID, data, null)
    }
  }
}
