package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

class ReceiveStateActor(s: State) extends AbstractBeviorStateActor(s) {
  var msg = false;
  
  def receive = {
    case SubjectToSubjectMessage(_, _, content) => {
      println("ReceiveState[%s] received: " + content)
      sender ! Ack
      msg = true
    }
    case ExecuteAction(_, succ) => {
      if (msg && (s.transitions contains succ)) {
        context.parent ! ChangeState(succ)
      }
    }
  }
}
