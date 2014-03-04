package de.tkip.servicehost.serviceactor.stubgen

import akka.actor.Actor
import de.tkip.servicehost.serviceactor.ServiceActor
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.servicehost.Messages._
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.miscellaneous._

class $TemplateServiceActor extends ServiceActor {

  val states = {
    //$EMPTYSTATE$//
  }
  
  var state: State = null
  
  def receive: Actor.Receive = {
    case message: SubjectToSubjectMessage => {
      if (state of ReceiveState) state.process
    }
    case message: ExecuteServiceMessage => {
      case state
    }
    case request: CreateProcessInstance => {
      
    }
    case GetProxyActor => {
      
    }
  }  
  
}