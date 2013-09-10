package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

class ReceiveStateActor(s: State) extends AbstractBeviorStateActor(s) {
  var hasRecvMsg = false
  def receive = {
    case SubjectToSubjectMessage(_, _, content) => {
      println(s"ReceiveState[$s] received: " + content)
      sender ! Ack
      hasRecvMsg = true
    }
    case ExecuteAction(_, succ) if (s.transitions contains succ) => {
      if (hasRecvMsg)
      	context.parent ! ChangeState(succ)
      else
        println("waiting for message")
    }
  }
}
