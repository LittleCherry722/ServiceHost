package de.tkip.sbpm.application.subject.state

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import scala.Array.canBuildFrom
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.history.{
  Transition => HistoryTransition,
  Message => HistoryMessage,
  State => HistoryState
}
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.SubjectInformation
import de.tkip.sbpm.application.RequestUserID
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes._
import akka.event.Logging
import scala.collection.mutable.ArrayBuffer
import de.tkip.sbpm.application.subject.StateData
import de.tkip.sbpm.application.subject.ExecuteAction
import de.tkip.sbpm.application.subject.BehaviorStateActor
import de.tkip.sbpm.application.subject.ActionExecuted
import de.tkip.sbpm.application.subject.ActionData
import akka.actor.Status.Failure

protected case class ActStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  protected def stateReceive = {

    case action: ExecuteAction => {
      val input = action.actionData
      val index = indexOfInput(input.text)
      if (index != -1) {
        changeState(exitTransitions(index).successorID, null)
        blockingHandlerActor ! ActionExecuted(action)
      } else {
        action.asInstanceOf[AnswerAbleMessage].sender !
          Failure(new IllegalArgumentException(
            "Invalid Argument: " + input.text + " is not a valid action."))
      }
    }
  }

  override protected def getAvailableAction: Array[ActionData] =
    exitTransitions.map((t: Transition) => ActionData(t.messageType, true, exitCondLabel))

  private def indexOfInput(input: String): Int = {
    var i = 0
    for (t <- exitTransitions) {
      if (t.messageType.equals(input)) {
        return i
      }
      i += 1
    }
    -1
  }
}