package de.tkip.sbpm.application

import de.tkip.sbpm._
import akka.actor.Actor
import de.tkip.sbpm.model.Subject
import de.tkip.sbpm.model.State
import akka.actor.ActorRef
import de.tkip.sbpm.application.state.ActStateActor
import akka.actor.Props

class SubjectActor(subject: Subject) extends Actor {

  private val currentState = createStateActor(subject.state(0))

  def receive = {
    case ChangeState(id) =>
    case _ => println("unsupported operation")
  }

  def createStateActor(state: State): ActorRef = state match {

    case State(id, Act, trans) => context.actorOf(Props(new ActStateActor(state)))
    case State(id, Send, trans) => context.actorOf(Props(new SendStateActor(state)))
    case State(id, Receive, trans) => context.actorOf(Props(new ReceiveStateActor(state)))
  }
}
