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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.event.Logging
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout

import spray.routing.authentication.UserPass

import com.github.t3hnar.bcrypt.Password

import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.logging.DefaultLogging
import de.tkip.sbpm.model.UserIdentity
import de.tkip.sbpm.persistence.query.Users
import de.tkip.sbpm.instrumentation.InstrumentedActor

/**
 * Provides support for form/json based or basic authentication.
 * Validates user name and password against the database
 * and returns corresponding user id.
 */
class UserPassAuthActor extends InstrumentedActor with DefaultLogging {
  private lazy val persistenceActor = ActorLocator.persistenceActor
  private implicit val timeout = Timeout(10 seconds)

  def wrappedReceive = {
    // valid basic auth header given -> check credentials
    case UserPass(user, pass) => checkCredentials(user, pass, sender)
    // invalid header -> fail
    case _ => sender !! None
  }

  /**
   * Checks if user name and password are valid according
   * to the database and sends user back to sender.
   */
  private def checkCredentials(user: String, pass: String, receiver: ActorRef) = {
    val future = (persistenceActor ?? Users.Read.Identity("sbpm", user)).map {
      // return none if user not found, no password in identity or failure
      case None                              => None
      case Some(UserIdentity(_, _, _, None)) => None
      case Some(identity: UserIdentity) =>
        if (validPass(pass, identity.password.get))
          Some(identity.user)
        else
          None
      case akka.actor.Status.Failure(e) => {
        log.error(e, "Error checking user identity.")
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
