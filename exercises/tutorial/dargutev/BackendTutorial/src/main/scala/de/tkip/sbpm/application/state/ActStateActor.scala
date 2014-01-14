package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

class ActStateActor(s: State) extends AbstractBeviorStateActor(s) {
  def receive = {
    case ExecuteAction(id, succ) if (s.transitions contains succ) => {
      context.parent ! ChangeState(s.transitions(s.transitions.indexOf(succ)))
    }
  }
}
