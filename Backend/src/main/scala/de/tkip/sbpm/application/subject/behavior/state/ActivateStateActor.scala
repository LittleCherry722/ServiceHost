package de.tkip.sbpm.application.subject.behavior.state

import de.tkip.sbpm.application.miscellaneous.BlockUser
import de.tkip.sbpm.application.miscellaneous.UnBlockUser
import de.tkip.sbpm.application.subject.misc.ExecuteAction
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.behavior.ActivateState

class ActivateStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  override def preStart() {
    // Block the user while prestart!
    blockingHandlerActor ! BlockUser(userID)
    super.preStart()
    // activate the state
    activateState()
    // and change to the next one
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

  private def activateState() {
    log.debug(s"Activate state ${data.stateModel.options.stateId}")
    if (stateOptions.stateId.isDefined) {
      internalBehaviorActor ! ActivateState(stateOptions.stateId.get)
    } else {
      log.error("State to activate is not defined")
    }
    //TODO
  }
}
