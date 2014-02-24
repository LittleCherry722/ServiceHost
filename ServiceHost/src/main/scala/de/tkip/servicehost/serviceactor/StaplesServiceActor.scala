package de.tkip.servicehost.serviceactor

import akka.actor.Actor
import de.tkip.servicehost.Messages._
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.servicehost.serviceactor._
import akka.actor.Props

class StaplesServiceActor extends ServiceActor {
	
  def receive: Actor.Receive = {
    case ExecuteServiceMessage => 
      println("here")
    case message: SubjectToSubjectMessage => {
      val actor = this.context.actorOf(new Props(classOf[AddServiceActor]))
      actor ! AddService(Integer.valueOf(message.messageContent), sender)
    }
  }
}