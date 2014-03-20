package de.tkip.sbpm.application.subject.behavior.state

import akka.actor.actorRef2Scala
import de.tkip.sbpm.application.subject.behavior.IPEmpty
import de.tkip.sbpm.application.subject.behavior.IsIPEmpty
import de.tkip.sbpm.application.subject.misc.ActionData

protected case class IsIPEmptyStateActor(data: StateData) extends BehaviorStateActor(data) {
  val msg = IsIPEmpty((stateOptions.subjectId.get, stateOptions.messageType.get))
  inputPoolActor ! msg

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
