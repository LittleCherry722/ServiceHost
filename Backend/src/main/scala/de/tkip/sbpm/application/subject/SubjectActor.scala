package de.tkip.sbpm.application.subject

import java.util.Date

import akka.actor._

import de.tkip.sbpm.application._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._

/**
 * contains and manages an InputPoolActor(Mailbox) and an InternalBehaviourActor
 */
class SubjectActor(userID: UserID,
                   processInstanceRef: ProcessInstanceRef,
                   subject: Subject) extends Actor {

  private val subjectID: SubjectID = subject.id
  private val subjectName: String = subject.id
  private val inputPoolActor: ActorRef =
    context.actorOf(Props(new InputPoolActor(10)), name = "IP@" + subjectName)
  private val internalBehaviorActor =
    context.actorOf(
      Props(
        new InternalBehaviorActor(
          processInstanceRef,
          subject.id,
          userID,
          inputPoolActor)))

  // add all states in the internal behavior
  for (state <- subject.states) {
    internalBehaviorActor ! state
  }

  def receive = {
    case sm: SubjectInternalMessage => {
      inputPoolActor.forward(sm)
    }

    // forward history entries from internal behavior up to instance actor
    case history.Transition(from, to, msg) => {
      context.parent ! history.Entry(new Date(), subjectName, from, to, msg)
    }

    case terminated: SubjectTerminated => {
      context.parent ! terminated
      // TODO terminate?
      context.stop(self)
    }

    case gaa: GetAvailableActions => {
      if (gaa.userID == userID) {
        internalBehaviorActor ! gaa
      }
    }

    case br: SubjectBehaviorRequest => {
      internalBehaviorActor.forward(br)
    }

    case message: SubjectProviderMessage => {
      context.parent ! message
    }

    case s => {
      println("SubjectActor " + userID + " does not support: " + s)
    }
  }
}
