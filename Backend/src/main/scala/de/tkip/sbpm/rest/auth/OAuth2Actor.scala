package de.tkip.sbpm.rest.auth

import akka.actor.Actor
import spray.http.OAuth2BearerToken
import akka.event.Logging

/**
 * Provides OAuth2 authentication support.
 * Validates token and returns corresponding user id. 
 */
class OAuth2Actor extends Actor {
  val logger = Logging(context.system, this)

  override def preStart() {
    logger.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    logger.debug(getClass.getName + " stopped.")
  }
  
  def receive = {
    // if credentials are oauth token -> verify it and check if
    // a user can be found in the database for it
    case OAuth2BearerToken(token) => sender ! None // TODO: Verify token here
    case _ => sender ! None
  }
}