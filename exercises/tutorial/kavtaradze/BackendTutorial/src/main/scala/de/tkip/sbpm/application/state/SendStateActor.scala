package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State

class SendStateActor(s: State) extends AbstractBeviorStateActor(s) {
  def receive = {
    case ExecuteAction(subjectID, action){
      context.parent ! SubjectToSubjectMessage(subjectID, subjectID + 1, "Message from subject")
    }
    case _ =>
  }
}
 