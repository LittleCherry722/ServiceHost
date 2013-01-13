package de.tkip.sbpm.application

import akka.actor._
import miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import java.util.Date
import de.tkip.sbpm.application.miscellaneous.End
import de.tkip.sbpm.application.miscellaneous.SubjectMessage
import de.tkip.sbpm.application.miscellaneous.ExecuteRequest
import de.tkip.sbpm.application.miscellaneous.ProcessBehaviour

// sub package for history related classes
package history {
  // message to report a transition in the internal behavior
  // to the corresponding subject actor
  case class Transition(from: State, to: State, message: Message)
}

/**
 * contains and manages an InputPoolActor(Mailbox) and an InternalBehaviourActor
 */
class SubjectActor(processInstanceRef: ProcessInstanceRef,
                   subject: Subject) extends Actor {

  val subjectName = subject.subjectName

  case object JobDone
  private val inputPoolActor: ActorRef =
    context.actorOf(Props(new InputPoolActor(10)), name = "IP@" + subjectName)
  private val internalBehaviourActor =
    context.actorOf(Props[InternalBehaviorActor])

  for (state <- subject.states) {
    internalBehaviourActor ! parseState(state)
  }

  def receive = {
    //    case ip: IPRef => ipRef = ip.ipRef
    case JobDone =>
      processInstanceRef ! End
      context.stop(self)
    case sm: SubjectMessage => inputPoolActor forward sm

    case sr: ExecuteRequest =>
      //      val ip = context.actorOf(Props(new InputPoolActor(10)), name = "IP@" + sr.userID)
      //      context.parent ! IPRef(ip)
      //      internalBehaviourActor ! ProcessBehaviour(processManagerRef, subjectName, sr.userID.toString(), ip)
      //      val ip = context.actorOf(Props(new InputPoolActor(10)), name = "IP@" + sr.userID)
      //      context.parent ! IPRef(ipRef)
      internalBehaviourActor ! ProcessBehaviour(processInstanceRef, subjectName, sr.userID.toString(), inputPoolActor)

    //      context.parent ! JobDone
    //      context.stop(self)

    case b: BehaviourState => internalBehaviourActor ! b
    
    // forward history entries from internal behavior up to instance actor
    case history.Transition(from, to, msg) => 
      context.parent ! history.Entry(new Date(), subjectName, from, to, msg)
  }

  def parseState(state: State) =
    state.stateType match {
      case StartStateType => if (state.transitions.size == 1) {
        StartState(state.id, state.transitions(0))
      } else {
        throw new IllegalArgumentException("Startstates may only have 1 Transition")
      }
      // TODO state action?
      case ActStateType     => ActState(state.id, state.name, state.transitions)

      case SendStateType    => SendState(state.id, state.transitions)

      case ReceiveStateType => ReceiveState(state.id, state.transitions)

      case EndStateType     => EndState(state.id)
    }
}
