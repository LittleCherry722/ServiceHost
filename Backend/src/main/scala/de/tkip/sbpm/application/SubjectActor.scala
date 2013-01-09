package de.tkip.sbpm.application

import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._

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
      processInstanceRef ! End; context.stop(self)
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
