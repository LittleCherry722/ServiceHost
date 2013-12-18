package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

class SendStateActor(s: State) extends AbstractBeviorStateActor(s) {
  def receive = {
    case ExecuteAction(subjectId, action) => {
      // valid SubjectIds are 1 and 2
      val silbingId = subjectId % 2 + 1
      context.parent ! SubjectToSubjectMessage(subjectId, silbingId, "how are you?")
    }
    case Ack => {
      context.parent ! ChangeState(s.transitions(0))
    }
    case _ => println("ReceiveStateActor received invalid message!")
  }
}
