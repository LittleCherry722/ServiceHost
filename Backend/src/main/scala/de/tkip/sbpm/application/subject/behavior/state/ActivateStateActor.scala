package de.tkip.sbpm.application.subject.behavior.state

import de.tkip.sbpm.application.miscellaneous.BlockUser
import de.tkip.sbpm.application.miscellaneous.UnBlockUser
import de.tkip.sbpm.application.subject.misc.ExecuteAction
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.behavior.ActivateState

protected class ActivateStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  override def preStart() {
    // Block the user while prestart!
    log.debug("TRACE: from " + this.self + " to " + blockingHandlerActor + " " + BlockUser(userID))
    blockingHandlerActor ! BlockUser(userID)
    super.preStart()
    // activate the state
    activateState()
    // and change to the next one
    changeState(exitTransitions.head.successorID, data, null)
    log.debug("TRACE: from " + this.self + " to " + blockingHandlerActor + " " + UnBlockUser(userID))
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

  private def activateState() {
    log.debug(s"Activate state ${data.stateModel.options.stateId}")
    if (stateOptions.stateId.isDefined) {
      log.debug("TRACE: from " + this.self + " to " + internalBehaviorActor + " " + ActivateState(stateOptions.stateId.get))
      internalBehaviorActor ! ActivateState(stateOptions.stateId.get)
    } else {
      log.error("State to activate is not defined")
    }
    //TODO
  }
}
