package de.tkip.sbpm.application.subject.behavior.state

import de.tkip.sbpm.application.subject.misc.ExecuteAction
import de.tkip.sbpm.application.subject.misc.ActionExecuted
import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.behavior.Transition
import de.tkip.sbpm.application.subject.behavior.ExitCond
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes._
import akka.actor.Status.Failure
import akka.event.Logging

protected case class SplitGuardStateActor(data: StateData)
  extends BehaviorStateActor(data) {
  
    protected def stateReceive = {

    case action: ExecuteAction => {
      println("GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG")
      val input = action.actionData
      val index = indexOfInput(input.text)
      val traceLogger = Logging(context.system, this)
      if (index != -1) {
        changeState(exitTransitions(index).successorID, data, null)
        traceLogger.debug("TRACE: from " + this.self + " to " + blockingHandlerActor + " " + ActionExecuted(action).toString)
        blockingHandlerActor ! ActionExecuted(action)
      } else {
        val receiver = action.asInstanceOf[AnswerAbleMessage].sender
        val message = Failure(new IllegalArgumentException(
            "Invalid Argument: " + input.text + " is not a valid action."))
        traceLogger.debug("TRACE: from " + this.self + " to " + receiver + " " + message.toString)
        receiver ! message
          
      }
    }
    case somethingelse => {
      println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
      println("somethingelse: " + somethingelse)
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