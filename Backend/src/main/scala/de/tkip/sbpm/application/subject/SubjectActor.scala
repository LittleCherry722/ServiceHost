package de.tkip.sbpm.application.subject

import java.util.Date

import akka.actor._

import de.tkip.sbpm.application._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._

case class SubjectData(
  userID: UserID,
  processInstanceID: ProcessInstanceID,
  processInstanceActor: ProcessInstanceRef,
  blockingHandlerActor: ActorRef,
  subject: Subject)

/**
 * contains and manages an InputPoolActor(Mailbox) and an InternalBehaviourActor
 */
class SubjectActor(data: SubjectData) extends Actor {

  // extract the information out of the input
  private val subject = data.subject
  private val userID = data.userID

  private val subjectID: SubjectID = subject.id
  private val subjectName: String = subject.id
  // create the inputpool
  private val inputPoolActor: ActorRef =
    context.actorOf(Props(new InputPoolActor(data)))
  // and the internal behavior
  private val internalBehaviorActor =
    context.actorOf(Props(new InternalBehaviorActor(data, inputPoolActor)))

  override def preStart() {
    // add all states in the internal behavior
    for (state <- subject.states) {
      internalBehaviorActor ! state
    }
  }

  def receive = {
    case sm: SubjectToSubjectMessage => {
      // a message from an other subject can be forwarded into the inputpool
      inputPoolActor.forward(sm)
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
        // forward the request to the inputpool actor
        internalBehaviorActor ! gaa
      }
    }

    case br: SubjectBehaviorRequest => {
      internalBehaviorActor.forward(br)
    }

    case message: SubjectProviderMessage => {
      // a message to the subject provider will be send over the process instance
      context.parent ! message
    }

    case s => {
      println("SubjectActor " + userID + " does not support: " + s)
    }
  }
}
