package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application.ExecuteAction
import de.tkip.sbpm.model.TestData
import de.tkip.sbpm.application.SubjectToSubjectMessage
import de.tkip.sbpm.application.ChangeState
import de.tkip.sbpm.application.ExecuteAction
import de.tkip.sbpm.application.Ack

class SendStateActor(s: State) extends AbstractBeviorStateActor(s) {
  var ackDone = false
  def receive = {
    case ExecuteAction(id, succ) => {
      if (TestData(2).subject1.subjectID == id) {
        context.parent ! SubjectToSubjectMessage(id, TestData(2).subject2.subjectID, "data")
      } else {
        context.parent ! SubjectToSubjectMessage(id, TestData(2).subject1.subjectID, "data")
      }
      if(ackDone)
         context.parent ! ChangeState(s.transitions(s.transitions.indexOf(succ)))
    }
    case Ack => ackDone = true
  }
}
 