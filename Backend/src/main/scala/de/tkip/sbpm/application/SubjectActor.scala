package de.tkip.sbpm.application

import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._

/**
 * contains and manages an InputPoolActor(Mailbox) and an InternalBehaviourActor
 */
class SubjectActor(processInstanceRef: ProcessInstanceRef,
                   subjectName: SubjectName) extends Actor {

  case object JobDone
  private val inputPoolActor: ActorRef =
    context.actorOf(Props(new InputPoolActor(10)), name = "IP@" + subjectName)
  private val internalBehaviourActor =
    context.actorOf(Props[InternalBehaviorActor])

  def receive = {
    //    case ip: IPRef => ipRef = ip.ipRef
    case JobDone => processInstanceRef ! End; context.stop(self)
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

}