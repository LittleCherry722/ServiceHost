package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

import scala.concurrent._

import akka.util.Timeout
import akka.pattern.ask

class SendStateActor(s: State) extends AbstractBeviorStateActor(s) {
  implicit val timeout = Timeout(3000)
  def receive = {
    // TODO implement ExecuteAction

    case ExecuteAction(subjectId, succ) => {
      val future = (sender ? SubjectToSubjectMessage(0, subjectId, "A message")).mapTo[Ack]
      val result = Await.result(future, timeout.duration)
      sender ! ExecuteAction(result.to, succ)
    }
  }
}
 