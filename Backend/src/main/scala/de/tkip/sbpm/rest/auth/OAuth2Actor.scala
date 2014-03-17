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
import spray.http.OAuth2BearerToken
import akka.event.Logging
import de.tkip.sbpm.logging.DefaultLogging

/**
 * Provides OAuth2 authentication support.
 * Validates token and returns corresponding user id.
 */
class OAuth2Actor extends InstrumentedActor with DefaultLogging {
  val traceLogger = Logging(context.system, this)

  def wrappedReceive = {
    // if credentials are oauth token -> verify it and check if
    // a user can be found in the database for it
    case OAuth2BearerToken(token) =>
      traceLogger.debug("TRACE: from " + this.self + " to " + sender + " " + None)
      sender ! None // TODO: Verify token here
    case _ =>
      traceLogger.debug("TRACE: from " + this.self + " to " + sender + " " + None)
      sender ! None
  }
}
