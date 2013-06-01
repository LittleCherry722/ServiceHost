package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application.{SubjectToSubjectMessage, ChangeState, Ack}

class SendStateActor(s: State) extends AbstractBeviorStateActor(s) {
  def receive = {
    case message: SubjectToSubjectMessage => {

    }
    case Ack => {
      context.parent ! ChangeState(s.transitions(0))
    }
  }
}
 