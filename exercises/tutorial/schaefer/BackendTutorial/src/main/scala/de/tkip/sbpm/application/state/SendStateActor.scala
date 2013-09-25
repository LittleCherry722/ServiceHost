package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application.SubjectToSubjectMessage
import de.tkip.sbpm.application.ExecuteAction
import de.tkip.sbpm.application.ChangeState
import de.tkip.sbpm.application.Ack
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.SubjectToSubjectMessage

class SendStateActor(s: State) extends AbstractBeviorStateActor(s) {
  def receive = {
    case ExecuteAction(ownId, succ) => {
      val otherId_ = otherId(ownId)
      ActorLocator.processManagerActor ! SubjectToSubjectMessage(ownId, otherId_, "Hello "+otherId_)
    }
    case Ack => {
      context.parent ! ChangeState(s.transitions(0))
    }
  }
  
  private def otherId (ownId: Int): Int = {
    if (ownId == 1) 2
    else 1
  }
}
 