package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

class ReceiveStateActor(s: State) extends AbstractBeviorStateActor(s) {
  var succ  = s.stateId
  
  def receive = {
    case SubjectToSubjectMessage(_, _, content) => {
      println(("ReceiveState[%s] received: " + content).format(s.stateType))
      sender ! Ack
      context.parent ! ChangeState(succ)
    }
    // TODO implement ExecuteAction
    case ExecuteAction(_, succ) if (s.transitions contains succ) => {
      this.succ = succ
      println(("ReceiveState[%s] received: " + succ).format(s.stateType))
    }
    case _ => println("ReceiveStateActor received Unknown message")
  }
}
