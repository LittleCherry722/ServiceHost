package de.tkip.sbpm.application.subject.behavior.state

import de.tkip.sbpm.application.subject.misc.MacroTerminated
import de.tkip.sbpm.application.miscellaneous.BlockUser
import de.tkip.sbpm.application.miscellaneous.UnBlockUser
import de.tkip.sbpm.application.subject.CallMacro

protected class MacroStateActor(data: StateData) extends BehaviorStateActor(data) {

  override def preStart {
    log.debug("TRACE: from " + this.self + " to " + blockingHandlerActor + " " + BlockUser(userID).toString)
    blockingHandlerActor ! BlockUser(userID)
    super.preStart()
    if (data.stateModel.callMacro.isDefined) {
      val msg =  CallMacro(self, data.stateModel.callMacro.get)
      log.debug("TRACE: from " + this.self + " to " + context.parent + " " + msg.toString)
      context.parent ! msg
    } else {
      logger.error("No MacroName is not defined")
    }
    log.debug("TRACE: from " + this.self + " to " + blockingHandlerActor + " " + UnBlockUser(userID).toString)
    blockingHandlerActor ! UnBlockUser(userID)
  }

  def stateReceive = {
    case terminated: MacroTerminated => {
      logger.debug("MacroState done")
      logger.debug("TRACE: from " + this.self + " to " + blockingHandlerActor + " " + UnBlockUser(userID).toString)
      blockingHandlerActor ! UnBlockUser(userID)
      // if the macro is terminated, this state is done
      changeState(exitTransitions.head.successorID, data, null)
    }
  }

  protected def getAvailableAction = Array()
}