package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

import de.tkip.sbpm.model._

class SendStateActor(s: State) extends AbstractBeviorStateActor(s) {
  var ack_received = false
  
  def receive = {
    
    case Ack => ack_received = true
    case ExecuteAction(id, succ) if (s.transitions contains succ) => {
      context.parent ! SubjectToSubjectMessage(id, TestData(2).subject2.subjectID, "data")
      if(ack_received)
    	  context.parent ! ChangeState(succ)
      println(("SendState[%s] received: " + succ).format(s.stateType))
    }
    case _ => println(("SendState[%s] received unknown object").format(s.stateType))
  }
}
 