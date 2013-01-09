package de.tkip.sbpm.application

import akka.actor.Actor
import akka.actor.actorRef2Scala
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model._

case class SubjectInformation(subjectName: String)

/**
 * resolves the context of the subjects
 */
class ContextResolverActor extends Actor {

  val userID: UserID = 1

  def receive = {

    case ruid: RequestUserID =>
      sender ! ruid.generateAnswer(userID)

      // TODO zum testen, passt nur auf den testfall
      val as = ruid.generateAnswer(userID).asInstanceOf[AddSubject]
      val superiorName = "Superior"
      val superiorStates = Array(
        new ReceiveState("sup", Array(Transition("BT Application", "Employee"))),
        new ActState("sup.br1", "Check Application", Array(Transition("Approval", "Do"), Transition("Denial", "Do"))),
        new SendState("sup.br1.br1", Array(Transition("Approval", "Employee", "The End"))),
        new SendState("sup.br1.br2", Array(Transition("Denial", "Employee", "The End"))),
        new EndState("The End"))
      for (state <- superiorStates)
        sender ! AddState(1, as.processID, superiorName, state)
      sender ! ExecuteRequest(1, as.processID)

    case _ => "not yet implemented"
  }

}