package de.tkip.sbpm.application

import akka.actor._
import miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import java.util.Date
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.SubjectMessage
import de.tkip.sbpm.application.miscellaneous.ExecuteRequest
import de.tkip.sbpm.application.miscellaneous.GetAvailableActions
import de.tkip.sbpm.application.subject.SubjectBehaviorRequest

// sub package for history related classes
package history {
  // message to report a transition in the internal behavior
  // to the corresponding subject actor
  case class Transition(from: State, to: State, message: Message)
}

/**
 * contains and manages an InputPoolActor(Mailbox) and an InternalBehaviourActor
 */
class SubjectActor(userID: UserID,
  processInstanceRef: ProcessInstanceRef,
  subject: Subject) extends Actor {

  private val subjectID: SubjectID = subject.id
  private val subjectName: String = subject.id

  case object JobDone
  private val inputPoolActor: ActorRef =
    context.actorOf(Props(new InputPoolActor(10)), name = "IP@" + subjectName)
  private val internalBehaviorActor =
    context.actorOf(
      Props(
        new InternalBehaviorActor(
          processInstanceRef,
          subject.id,
          userID,
          inputPoolActor)))

  // add all states in the internal behavior
  for (state <- subject.states) {
    internalBehaviorActor ! state
  }

  def receive = {
    //    case ip: IPRef => ipRef = ip.ipRef
    case JobDone =>
      processInstanceRef ! End
      context.stop(self)
    case sm: SubjectMessage => inputPoolActor.forward(sm)

    case sr: ExecuteRequest =>
      if (sr.isInstanceOf[Debug])
        internalBehaviorActor ! new ExecuteStartState() with Debug
      else
        internalBehaviorActor ! ExecuteStartState()

    case ess: ExecuteStartState =>
      if (ess.isInstanceOf[Debug])
        internalBehaviorActor ! new ExecuteStartState() with Debug
      else
        internalBehaviorActor ! ess

    case bsa: BehaviorStateActor => internalBehaviorActor ! bsa

    case gaa: GetAvailableActions => if (gaa.userID == userID) internalBehaviorActor ! gaa

    // forward history entries from internal behavior up to instance actor
    case history.Transition(from, to, msg) =>
      context.parent ! history.Entry(new Date(), subjectName, from, to, msg)

    case br: SubjectBehaviorRequest =>
      internalBehaviorActor.forward(br)
  }
}
