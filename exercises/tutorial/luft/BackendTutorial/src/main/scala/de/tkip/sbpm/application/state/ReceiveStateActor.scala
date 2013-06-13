package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

class ReceiveStateActor(s: State) extends AbstractBeviorStateActor(s) {
	var messageRecieved = false
  def receive = {
    case SubjectToSubjectMessage(_, _, content) => {
      println("ReceiveState[%s] received: " + content)
      sender ! Ack
      messageRecieved = true
    }
    case ExecuteAction(_, succ) if (s.transitions contains succ) => {
    	if (messageRecieved) context.parent ! ChangeState(succ) else println("cannot change state, did not recieve a message first!")
    }
  }
}
