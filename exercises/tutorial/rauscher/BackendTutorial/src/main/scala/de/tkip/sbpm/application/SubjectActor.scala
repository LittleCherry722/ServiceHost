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
    case ea @ ExecuteAction(subject.subjectID, action) => currentState forward ea
    case s2s @ SubjectToSubjectMessage(_, subject.subjectID, _) => currentState forward s2s
    case s2s @ SubjectToSubjectMessage(subject.subjectID, _, _) => context.parent forward s2s
    case ChangeState(id) => changeState(id)
    case x @ _ => println("unsupported operation: " + subject.subjectID + " | " + x)
  }

  private def readSubject: SubjectAnswer = {
    val state = subject.state(currentStateId)
    SubjectAnswer(
      subject.subjectID,
      state.stateType,
      state.transitions)
  }

  private def changeState(id: StateID) {
    // kill the currentstate actor
    currentState ! PoisonPill
    // update the id
    println(
      "S@%s changes from State %s to State %s"
        .format(subject.subjectID, currentStateId, id))
    currentStateId = id
    // create a new current state actor
    currentState = createStateActor(subject.state(id))
  }

  private def createStateActor(state: State): ActorRef = state match {
    // Tipp:
    // Scala case matching matcht Vals, die den ersten Buchstaben
    // grossgeschrieben haben
    // und erstellt fuer kleingeschriebene ein neues val
    // will man eine eigene val matchen kann man das ueber `myVal`
    // oder ueber ReadSubject(subject.subjectID) (siehe oben)
    // oder ueber this.myVal (nicht in actoren!!!) bewerkstelligen
    // also zB.: case State(`id`, ...
    case State(_, Act, trans) => context.actorOf(Props(new ActStateActor(state)))
    case State(_, Send, trans) => context.actorOf(Props(new SendStateActor(state)))
    case State(_, Receive, trans) => context.actorOf(Props(new ReceiveStateActor(state)))
  }
}
