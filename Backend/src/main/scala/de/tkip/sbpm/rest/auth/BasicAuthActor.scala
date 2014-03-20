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

import de.tkip.sbpm.instrumentation.InstrumentedActor
import spray.http.HttpCredentials
import spray.http.BasicHttpCredentials
import de.tkip.sbpm.ActorLocator
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import de.tkip.sbpm.model.User
import akka.event.Logging
import akka.actor.ActorRef
import spray.routing.authentication.UserPass
import de.tkip.sbpm.logging.DefaultLogging
import spray.routing.authentication.UserPass

/**
 * Provides support for HTTP basic authentication.
 * Validates user name and password and returns corresponding user id.
 */
class BasicAuthActor extends InstrumentedActor with DefaultLogging {
  private lazy val userPassActor = ActorLocator.userPassAuthActor

  def wrappedReceive = {
    // valid basic auth header given -> check credentials
    // with user pass auth actor
    case BasicHttpCredentials(user, pass) => {
      userPassActor.forward(UserPass(user, pass))
    }
    // invalid header -> fail
    case _ => sender !! None
  }

}
