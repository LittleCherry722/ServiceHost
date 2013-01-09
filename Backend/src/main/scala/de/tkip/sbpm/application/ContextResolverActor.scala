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

  val userID: UserID = 2

  def receive = {

    case ruid: RequestUserID =>
      sender ! ruid.generateAnswer(userID)

    case ss => println("ContextResolver: not yet implemented Message: " + ss)
  }

}