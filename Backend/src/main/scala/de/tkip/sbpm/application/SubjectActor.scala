package de.tkip.sbpm.application

import akka.actor._
import miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import java.util.Date
import de.tkip.sbpm.application.miscellaneous.End
import de.tkip.sbpm.application.miscellaneous.SubjectMessage
import de.tkip.sbpm.application.miscellaneous.ExecuteRequest

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

  val subjectName: String = subject.subjectName

  case object JobDone
  private val inputPoolActor: ActorRef =
    context.actorOf(Props(new InputPoolActor(10)), name = "IP@" + subjectName)
  private val internalBehaviourActor =
    context.actorOf(
      Props(
        new InternalBehaviorActor(
          processInstanceRef,
          subjectName,
          userID,
          inputPoolActor)))

  // add all states in the internal behavior
  for (state <- subject.states) {
    internalBehaviourActor ! state
  }

  def receive = {
    //    case ip: IPRef => ipRef = ip.ipRef
    case JobDone =>
      processInstanceRef ! End
      context.stop(self)
    case sm: SubjectMessage => inputPoolActor.forward(sm)

    case sr: ExecuteRequest =>
      //internalBehaviourActor ! ProcessBehaviour(processInstanceRef, subjectName, sr.userID.toString(), inputPoolActor)
      internalBehaviourActor ! ExecuteStartState()

    case e: ExecuteStartState =>
      internalBehaviourActor ! ExecuteStartState()
      
    case b: BehaviourState => internalBehaviourActor ! b

    // forward history entries from internal behavior up to instance actor
    case history.Transition(from, to, msg) =>
      context.parent ! history.Entry(new Date(), subjectName, from, to, msg)
  }
}
