package de.tkip.sbpm.application.subject.behavior.state

import de.tkip.sbpm.application.subject.misc.MacroTerminated
import de.tkip.sbpm.application.miscellaneous.BlockUser
import de.tkip.sbpm.application.miscellaneous.UnBlockUser
import de.tkip.sbpm.application.subject.CallMacro

class MacroStateActor(data: StateData) extends BehaviorStateActor(data) {

  override def preStart {
    blockingHandlerActor ! BlockUser(userID)
    super.preStart()
    if (data.stateModel.callMacro.isDefined) {
      val msg =  CallMacro(self, data.stateModel.callMacro.get)
      context.parent ! msg
    } else {
      log.error("No MacroName is not defined")
    }
    blockingHandlerActor ! UnBlockUser(userID)
  }

  def stateReceive = {
    case terminated: MacroTerminated => {
      log.debug("MacroState done")
      blockingHandlerActor ! UnBlockUser(userID)
      // if the macro is terminated, this state is done
      changeState(exitTransitions.head.successorID, data, null)
    }
  }

  protected def getAvailableAction = Array()
}
