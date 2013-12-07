package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

class SendStateActor(s: State) extends AbstractBeviorStateActor(s) {
  def receive = {
    // TODO implement ExecuteAction
    case ExecuteAction(subjectId, action) => {
      println("SendStateActor.receive ExecuteAction("+subjectId+", "+action+")")
      val otherSubject = ((subjectId % 2) + 1)
      context.parent ! SubjectToSubjectMessage(subjectId, otherSubject, "myMessage")
    }
    case _ => println("SendStateActor.receive invalid")
  }
}
 
