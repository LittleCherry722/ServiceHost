package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

class ReceiveStateActor(s: State) extends AbstractBeviorStateActor(s) {
  def receive = {
    case SubjectToSubjectMessage(_, _, content) => {
      println("ReceiveState[%s] received: " + content)
      sender ! Ack
    }
    // TODO implement ExecuteAction
    case ExecuteAction(_, succ) if (s.transitions contains succ) => {
      // go to the requested state
      context.parent ! ChangeState(succ)
    }
    case x @ _ => println("ReceiveStateActor.receive invalid message: " + x)
  }
}
