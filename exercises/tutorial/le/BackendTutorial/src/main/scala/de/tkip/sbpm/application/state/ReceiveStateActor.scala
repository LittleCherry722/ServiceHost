package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

class ReceiveStateActor(s: State) extends AbstractBeviorStateActor(s) {
  var msg_received = false
  
  def receive = {
    case SubjectToSubjectMessage(_, _, content) => {
      println(("ReceiveState[%s] received: " + content).format(s.stateType))
      sender ! Ack
      msg_received = true
    }
    
    case ExecuteAction(_, succ) if (s.transitions contains succ) => {
      if(msg_received)
    	  context.parent ! ChangeState(succ)
      println(("ReceiveState[%s] received: " + succ).format(s.stateType))
    }
    case _ => println("ReceiveStateActor received Unknown message")
  }
}
