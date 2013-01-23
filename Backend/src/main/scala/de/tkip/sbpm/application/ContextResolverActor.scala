package de.tkip.sbpm.application

import akka.actor.Actor
import akka.actor.actorRef2Scala
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model._

// this are the information which are required to evaluate the user id
case class SubjectInformation(subjectName: String)

// this message is to Request the user id and will be answered
// using generateAnswer with the userID
case class RequestUserID(subjectInformation: SubjectInformation, generateAnswer: UserID => Any)

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
    0
  }
}