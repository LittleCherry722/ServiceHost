package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.model._
import de.tkip.sbpm.application._
import de.tkip.sbpm._

class SendStateActor(s: State) extends AbstractBeviorStateActor(s) {
  
  private lazy val processInstanceActor =
    ActorLocator.actor(ActorLocator.processInstanceActorName)
  
  private var succ = 0
    
  def receive = {
    // TODO implement ExecuteAction
//    case m @ SubjectToSubjectMessage(from, to, message) => {
//      println("sending...")
//      context.actorFor("akka://de-tkip-sbpm-Boot/user/process-instance/$d/receive") ! m
//    }
    
    case ExecuteAction(subjectID, succ) if (s.transitions contains succ) => {
      this.succ = succ
      processInstanceActor ! SubjectToSubjectMessage(subjectID,f(subjectID),"hello")
    }
    
    case Ack => {
      context.parent ! ChangeState(succ)
    }
  }
  
  def f(n: Int) = if (n == 1) 2 else 1
}
 