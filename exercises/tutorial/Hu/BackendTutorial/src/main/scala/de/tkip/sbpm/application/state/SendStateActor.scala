package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

class SendStateActor(s: State) extends AbstractBeviorStateActor(s) {
  def receive = {
    // TODO implement ExecuteAction
    case ExecuteAction(id,succ) => 
      context.parent ! new SubjectToSubjectMessage (id, 2, "Hello Subject!")
    case Ack => 
      context.parent ! ChangeState(s.transitions(0))
  }
}
 