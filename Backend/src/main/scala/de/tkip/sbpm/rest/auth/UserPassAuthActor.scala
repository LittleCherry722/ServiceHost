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
 * Provides support for form/json based or basic authentication.
 * Validates user name and password against the database
 * and returns corresponding user id. 
 */
class UserPassAuthActor extends Actor {
  private lazy val persistenceActor = ActorLocator.persistenceActor
  private implicit val timeout = Timeout(10 seconds)
   val logger = Logging(context.system, UserPassAuthActor.this)

  override def preStart() {
    logger.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    logger.debug(getClass.getName + " stopped.")
  }

  def receive = {
    // valid basic auth header given -> check credentials
    case UserPass(user, pass) => checkCredentials(user, pass)
    // invalid header -> fail
    case _ => sender ! None
  }

  /**
   * Checks if user name and password are valid according
   * to user n the database and sends user id back to sender.
   */
  private def checkCredentials(user: String, pass: String) = {
    // TODO check password too (currently only user name)
    persistenceActor.forward(GetUser(None, Some(user)))
  }
}