package de.tkip.sbpm.application.subject.behavior.state

import de.tkip.sbpm.application.subject.misc.ExecuteAction
import de.tkip.sbpm.application.subject.misc.ActionExecuted
import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.behavior.{ Transition, ExitCond, AskForJoinStateID }
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes._
import akka.actor.Status.Failure
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await

protected case class SplitGuardStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  val joinText = "nein"
  implicit val timeout = Timeout(3 seconds)

  protected def stateReceive = {

    case action: ExecuteAction => {
      val f = internalBehaviorActor ? AskForJoinStateID(id)
      val joinStateId = Await.result(f, 3 seconds).asInstanceOf[Int]
      val input = action.actionData
      if (input.text.equals(joinText)) {
        if (joinStateId != -1) {
          changeState(joinStateId, data, null)
          log.debug("TRACE: from " + this.self + " to " + blockingHandlerActor + " " + ActionExecuted(action))
          blockingHandlerActor ! ActionExecuted(action)
        }else{
          log.error("Modal Join State is not found")
        }
      } else {
        val index = indexOfInput(input.text)
        if (index != -1) {
          changeState(exitTransitions(index).successorID, data, null)
          log.debug("TRACE: from " + this.self + " to " + blockingHandlerActor + " " + ActionExecuted(action))
          blockingHandlerActor ! ActionExecuted(action)
        } else {
          val receiver = action.asInstanceOf[AnswerAbleMessage].sender
          val message = Failure(new IllegalArgumentException(
            "Invalid Argument: " + input.text + " is not a valid action."))
          log.debug("TRACE: from " + this.self + " to " + receiver + " " + message)
          receiver ! message
        }
      }
    }

  }

  override protected def getAvailableAction: Array[ActionData] = {
    val actions = exitTransitions.map((t: Transition) => ActionData(t.messageType, true, exitCondLabel))
    actions :+ ActionData(joinText, true, exitCondLabel)
  }

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
