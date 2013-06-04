package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

import akka.pattern.ask
import scala.concurrent.{Await,Future}
import scala.concurrent.duration._
import akka.util.Timeout

class SendStateActor(s: State, subjID: Int) extends AbstractBeviorStateActor(s) {

  // send message on init:
  implicit val askTimeout = Timeout(1.second)
  val otherSubjID = if (subjID==1) 2 else 1
  val future = context.parent ? SubjectToSubjectMessage(subjID, otherSubjID, s"$subjID: hi")
  println(s"SendState[$s] message sent!")
  Await.result(future, 1.seconds) match {
    case Ack =>
      println(s"SendState[$s] Ack received!")
      context.parent ! ChangeState(s.transitions(0))
  }

  def receive = {
    case ExecuteAction(_, succ) if (s.transitions contains succ) => {
      context.parent ! ChangeState(succ)
    }
    case _ =>
  }

}