package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

class ReceiveStateActor(s: State) extends AbstractBeviorStateActor(s) {

  private var msgReceived = false

  def receive = {
    case SubjectToSubjectMessage(_, s.stateId, content) => {
      println("ReceiveState[%s] received: " + content)
      sender ! Ack
      msgReceived = true
    }
    case ExecuteAction(_, succ) if msgReceived && (s.transitions contains succ) => {
      context.parent ! ChangeState(succ)
    }
  }
}
