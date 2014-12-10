package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

class ActStateActor(s: State) extends AbstractBeviorStateActor(s) {
  def receive = {
    case ExecuteAction(_, succ) if (s.transitions contains succ) => {
      // go to the state given in succ
      context.parent ! ChangeState(s.transitions(succ))
    }
  }
}
