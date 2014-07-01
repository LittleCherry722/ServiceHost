package de.tkip.sbpm.application.subject.behavior.state

import de.tkip.sbpm.application.miscellaneous.BlockUser
import de.tkip.sbpm.application.miscellaneous.UnBlockUser
import de.tkip.sbpm.application.subject.misc.ExecuteAction
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.behavior.DeactivateState
import akka.event.Logging

class DeactivateStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  override def preStart() {
    // Block the user while prestart!
    blockingHandlerActor ! BlockUser(userID)
    super.preStart()
    // deactivate state
    deactivateState()
    // and chante to the next one
    changeState(exitTransitions.head.successorID, data, null)
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

  private def deactivateState() {
    log.debug(s"Deactivate state ${data.stateModel.options.stateId}")
    if (stateOptions.stateId.isDefined) {
      val msg = DeactivateState(stateOptions.stateId.get)
      internalBehaviorActor ! msg
    } else {
      log.error("State to deactivate is not defined")
    }
  }
}
