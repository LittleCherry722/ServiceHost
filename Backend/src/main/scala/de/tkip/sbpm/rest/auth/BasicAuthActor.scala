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
import akka.event.Logging
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.logging.DefaultLogging
import spray.http.BasicHttpCredentials
import spray.routing.authentication.UserPass

/**
 * Provides support for HTTP basic authentication.
 * Validates user name and password and returns corresponding user id.
 */
class BasicAuthActor extends Actor with DefaultLogging {
  private lazy val userPassActor = ActorLocator.userPassAuthActor

  def receive = {
    // valid basic auth header given -> check credentials
    // with user pass auth actor
    case BasicHttpCredentials(user, pass) => {
      val traceLogger = Logging(context.system, this)
      traceLogger.debug("TRACE: from " + this.self + " to " + userPassActor + " " + UserPass(user, pass).toString)
      userPassActor.forward(UserPass(user, pass))
    }
    // invalid header -> fail
    case _ =>
      val traceLogger = Logging(context.system, this)
      traceLogger.debug("TRACE: from " + this.self + " to " + sender + " " + None)

      sender ! None
  }

}
