package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

class SendStateActor(s: State) extends AbstractBeviorStateActor(s) {
  def receive = {
    case ExecuteAction(id, _) => {
      var targetID = 1
      if(id == 1) targetID = 2
      val msg = new SubjectToSubjectMessage(id, targetID, "test message")
      context.parent ! msg
    }
    case Ack => {
      context.parent ! ChangeState(s.transitions(0))
      println("Ack is received")
    }
  }
}
 