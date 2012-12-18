package de.tkip.sbpm.application

import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._

/**
 * contains and manages an InputPoolActor(Mailbox) and an InternalBehaviourActor
 */
class SubjectActor(processManagerRef: ProcessManagerRef, subjectName: SubjectName) extends Actor {

  private var ipRef: ActorRef = _ // Ref to the ip of the subjectprovider

  case object JobDone
  case class IPRef(ipRef: ActorRef)

  private val internalBehaviourActor = context.actorOf(Props[InternalBehaviorActor]) // create InternalBehaviorActor

  def receive = {
    case ip: IPRef => ipRef = ip.ipRef
    case JobDone => processManagerRef ! End; context.stop(self)
    case sm: SubjectMessage => ipRef forward sm

    case sr: StatusRequest =>
      val ip = context.actorOf(Props(new InputPoolActor(10)), name = "IP@" + sr.userID)
      context.parent ! IPRef(ip)
      internalBehaviourActor ! ProcessBehaviour(processManagerRef, subjectName, sr.userID.toString(), ip)
      context.parent ! JobDone
      context.stop(self)

    case b: BehaviourState => internalBehaviourActor ! b
    
    case aac: AddActState => internalBehaviourActor ! ActState(aac.id, aac.stateAction, aac.transitions)
    case aes: AddEndState => internalBehaviourActor ! EndState(aes.StateID)
    case ars: AddReceiveState => internalBehaviourActor ! ReceiveState(ars.s, ars.transitions)
    case ass: AddSendState => internalBehaviourActor ! SendState(ass.s, ass.transitions)
  }

}