package de.tkip.sbpm.application.subject.behavior.state

import de.tkip.sbpm.application.subject.misc.MacroTerminated
import de.tkip.sbpm.application.miscellaneous.BlockUser
import de.tkip.sbpm.application.miscellaneous.UnBlockUser
import de.tkip.sbpm.application.subject.CallMacro

protected class MacroStateActor(data: StateData) extends BehaviorStateActor(data) {

  override def preStart {
    blockingHandlerActor ! BlockUser(userID)
    super.preStart()
    if (data.stateModel.callMacro.isDefined) {
      context.parent ! CallMacro(self, data.stateModel.callMacro.get)
    } else {
      logger.error("No MacroName is not defined")
    }
    blockingHandlerActor ! UnBlockUser(userID)
  }

  def stateReceive = {
    case terminated: MacroTerminated => {
      logger.debug("MacroState done")
      blockingHandlerActor ! UnBlockUser(userID)
      // if the macro is terminated, this state is done
      changeState(exitTransitions.head.successorID, data, null)
    }
  }

  protected def getAvailableAction = Array()
}