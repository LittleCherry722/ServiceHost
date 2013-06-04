package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

class SendStateActor(s: State) extends AbstractBeviorStateActor(s) {
  def receive = {
    case ExecuteAction(_, succ) if (s.transitions contains succ) => {
      //context.parent ! SubjectToSubjectMessage(context.self, )
    }
  }
}
 