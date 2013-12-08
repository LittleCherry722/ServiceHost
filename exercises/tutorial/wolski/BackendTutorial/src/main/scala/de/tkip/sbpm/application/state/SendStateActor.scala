package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

class SendStateActor(s: State) extends AbstractBeviorStateActor(s) {
  def receive = {
    case ExecuteAction(subjectId, action) => {
      println("SendStateActor.receive ExecuteAction("+subjectId+", "+action+")")
      val otherSubject = ((subjectId % 2) + 1)
      context.parent ! SubjectToSubjectMessage(subjectId, otherSubject, "myMessage")
    }
    case Ack => {
      context.parent ! ChangeState(s.transitions(0))
    }
    case x @ _ => println("SendStateActor.receive invalid message: " + x)
  }
}
 
