package de.tkip.sbpm.rest.auth

import akka.actor.Actor
import spray.http.HttpCredentials
import spray.http.BasicHttpCredentials
import de.tkip.sbpm.ActorLocator
import akka.pattern._
import de.tkip.sbpm.persistence.GetUser
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import de.tkip.sbpm.model.User
import akka.event.Logging
import akka.actor.ActorRef
import spray.routing.authentication.UserPass

/**
 * Provides support for HTTP basic authentication.
 * Validates user name and password and returns corresponding user id.
 */
class BasicAuthActor extends Actor {
  private lazy val userPassActor = ActorLocator.userPassAuthActor
  val logger = Logging(context.system, this)

  override def preStart() {
    logger.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    logger.debug(getClass.getName + " stopped.")
  }

  def receive = {
    // valid basic auth header given -> check credentials
    // with user pass auth actor
    case BasicHttpCredentials(user, pass) =>
      userPassActor.forward(UserPass(user, pass))
    // invalid header -> fail
    case _ => sender ! None
  }

}