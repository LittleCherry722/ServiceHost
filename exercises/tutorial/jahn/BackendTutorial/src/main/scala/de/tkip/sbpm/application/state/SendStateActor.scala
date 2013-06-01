package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application.{SubjectToSubjectMessage, ExecuteAction, ChangeState, Ack}
import de.tkip.sbpm.ActorLocator

class SendStateActor(s: State) extends AbstractBeviorStateActor(s) {

  private var msgSent = false

  def receive = {
    case ExecuteAction(subjectId, succ) if (s.transitions contains succ) => {
      ActorLocator.processManagerActor ! createMessage(subjectId)
      msgSent = true
    }
    case Ack if msgSent => {
      context.parent ! ChangeState(s.transitions(0))
    }
  }

  private def createMessage(myId: Int) = {
    val receiverId = if(myId == 1) 2 else 1
    SubjectToSubjectMessage(myId, receiverId, "Test Message from %s to %s".format(myId, receiverId))
  }
}
 