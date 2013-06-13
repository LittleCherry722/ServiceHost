package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application.ChangeState
import de.tkip.sbpm.application.ExecuteAction
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.SubjectToSubjectMessage

class SendStateActor(s: State) extends AbstractBeviorStateActor(s) {
  def receive = {
    case ExecuteAction(_, succ) if (s.transitions contains succ) => {
      context.parent ! ChangeState(succ)
      ActorLocator.processManagerActor ! SubjectToSubjectMessage(s.stateId, succ, "hallo")
    }
  }
}
