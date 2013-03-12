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
class SubjectActor(
  userID: UserID,
  sessionID: Int,
  processInstanceActor: ProcessInstanceRef,
  subject: Subject) extends Actor {

  private val subjectID: SubjectID = subject.id
  private val subjectName: String = subject.id
  private val inputPoolActor: ActorRef =
    context.actorOf(Props(new InputPoolActor(userID, subject.inputPool)))
  private val internalBehaviorActor =
    context.actorOf(
      Props(
        new InternalBehaviorActor(
          processInstanceActor,
          subject.id,
          sessionID,
          userID,
          inputPoolActor)))

  // add all states in the internal behavior
  for (state <- subject.states) {
    internalBehaviorActor ! state
  }

  def receive = {
    case sm: SubjectToSubjectMessage => {
      inputPoolActor.forward(sm)
    }

    case message: SubjectInternalMessageProcessed => {
      context.parent.forward(message)
    }

    case history.Transition(from, to, msg) => {
      // forward history entries from internal behavior up to instance actor
      context.parent !
        history.Entry(new Date(), subjectName, from, to, if (msg != null) Some(msg) else None)
    }

    case terminated: SubjectTerminated => {
      context.parent ! terminated
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
