package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State

class SendStateActor(s: State) extends AbstractBeviorStateActor(s) {
  def receive = {
    // TODO implement ExecuteAction
    case ExecuteAction(subjectId, action) => {
      println("sending SubjectToSubjectMessage("+subjectId+", "+action+")")
      context.parent ! SubjectToSubjectMessage(subjectId, action, "")
    }
    case Ack => {
      context.parent ! ChangeState(s.transitions(0))
    }
  }

}
 