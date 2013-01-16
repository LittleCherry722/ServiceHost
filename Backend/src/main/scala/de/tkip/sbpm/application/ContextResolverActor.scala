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

  def receive = {

    case ruid: RequestUserID =>
      sender ! ruid.generateAnswer(evaluateUserID(ruid.subjectInformation))

    case ss => println("ContextResolver not yet implemented Message: " + ss)
  }

  private def evaluateUserID(subjectInformation: SubjectInformation): UserID = {
    2
  }
}