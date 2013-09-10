package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application.{ExecuteAction, ControlMessage, Ack, ChangeState, SubjectToSubjectMessage}
import de.tkip.sbpm.ActorLocator

class SendStateActor(s: State) extends AbstractBeviorStateActor(s) {
  var msg = false;
  
  def receive = {
    case ExecuteAction(subjectId, succ) if (s.transitions contains succ) => {
      ActorLocator.processManagerActor ! createMessage(subjectId)
      msg = true
    }
    case Ack if msg => {
      context.parent ! ChangeState(s.transitions(0))
    }
    case _ =>
  }
  
  def createMessage(subId: Int) = {
    val otherId = if (subId == 1) 2 else 1
    SubjectToSubjectMessage(subId, otherId, "Message")
  }
}
 