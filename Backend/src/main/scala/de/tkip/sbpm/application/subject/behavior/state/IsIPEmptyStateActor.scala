package de.tkip.sbpm.application.subject.behavior.state

import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.behavior.IsIPEmpty
import de.tkip.sbpm.application.subject.behavior.IPEmpty

protected case class IsIPEmptyStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  inputPoolActor ! IsIPEmpty((stateOptions.subjectId.get, stateOptions.messageType.get))

  override protected def stateReceive = {
    case IPEmpty(true) => {
      //TODO: exit transition for true
      changeState(exitTransition.successorID, data, null)
    }
    case IPEmpty(false) => {
      //TODO: exit transition for false
      changeState(exitTransition.successorID, data, null)
    }
  }

  private def exitTransition = exitTransitions(0) //TODO use the right transitions

  override protected def getAvailableAction: Array[ActionData] = Array()
}