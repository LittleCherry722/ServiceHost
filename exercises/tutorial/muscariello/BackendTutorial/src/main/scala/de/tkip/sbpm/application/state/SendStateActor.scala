package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

class SendStateActor(s: State) extends AbstractBeviorStateActor(s) {
  def receive = {
    case ExecuteAction(subjectId, action) => {
      val otherId = if (subjectId == 1) { 2 } else { 1 }
      context.parent ! SubjectToSubjectMessage(subjectId, otherId, "hi there")
    }
    case Ack => {
      context.parent ! ChangeState(s.transitions(0))
    }
  }
}
