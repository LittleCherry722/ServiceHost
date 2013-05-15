package de.tkip.sbpm.application

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.PoisonPill

import de.tkip.sbpm._
import de.tkip.sbpm.model.Subject
import de.tkip.sbpm.model.State
import de.tkip.sbpm.application.state._

class SubjectActor(subject: Subject) extends Actor {

  // we start with the state 0
  private var currentStateId: StateID = 0
  private var currentState = createStateActor(subject.state(currentStateId))

  def receive = {
    // we only allow to read this subject!
    case ReadSubject(subject.subjectID) => sender ! readSubject
    case ChangeState(id) => changeState(id)
    case _ => println("unsupported operation")
  }

  private def readSubject: SubjectAnswer = {
    val state = subject.state(currentStateId)
    SubjectAnswer(
      subject.subjectID,
      state.stateType,
      // TODO this is only for the act state
      state.transitions map (_.toString))
  }

  private def changeState(id: StateID) {
    // kill the currentstate actor
    currentState ! PoisonPill
    // update the id
    currentStateId = id
    // create a new current state actor
    currentState = createStateActor(subject.state(id))
  }

  private def createStateActor(state: State): ActorRef = state match {
    case State(id, Act, trans) => context.actorOf(Props(new ActStateActor(state)))
    case State(id, Send, trans) => context.actorOf(Props(new SendStateActor(state)))
    case State(id, Receive, trans) => context.actorOf(Props(new ReceiveStateActor(state)))
  }
}
