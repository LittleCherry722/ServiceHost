/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.rest.auth

import akka.actor.Actor
import spray.http.HttpCredentials
import spray.http.BasicHttpCredentials
import de.tkip.sbpm.ActorLocator
import akka.pattern._
import de.tkip.sbpm.persistence._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import de.tkip.sbpm.model.User
import akka.event.Logging
import akka.actor.ActorRef
import spray.routing.authentication.UserPass
import de.tkip.sbpm.model.UserIdentity
import scala.util.{ Try, Success, Failure }
import com.github.t3hnar.bcrypt._
import scala.concurrent.Await
import de.tkip.sbpm.persistence.query.Users

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
    case UserPass(user, pass) => checkCredentials(user, pass, sender)
    // invalid header -> fail
    case _ => sender ! None
  }

  /**
   * Checks if user name and password are valid according
   * to the database and sends user back to sender.
   */
  private def checkCredentials(user: String, pass: String, receiver: ActorRef) = {
    val future = (persistenceActor ? Users.Read.Identity("sbpm", user)).map {
    // return none if user not found, no password in identity or failure 
    case None => None
      case Some(UserIdentity(_, _, _, None)) => None
      case Some(identity: UserIdentity) =>
        if (validPass(pass, identity.password.get))
          Some(identity.user)
        else
          None
      case akka.actor.Status.Failure(e) => {
          logger.error(e, "Error checking user identity.")
          None   
      }
    }.pipeTo(receiver)
  }

  /**
   * Check if password is valid bcrypt hash.
   */
  private def validPass(toTest: String, reference: String) = {
    toTest.isBcrypted(reference)
  }
}