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

package de.tkip.sbpm.rest

import java.util.Date
import de.tkip.sbpm.ActorLocator

package object auth {
  // default expiry time for a user session  
  def defaultSessionExpiry = new Date(System.currentTimeMillis() + 3600 * 1000)

  // supported authentication schemes with corresponding actors
  // which handke authentication requests
  val defaultSchemes: Map[String, String] = Map(
    "Basic" -> ActorLocator.basicAuthActorName, // for basic auth
    "Bearer" -> ActorLocator.oAuth2ActorName) // for OAuth 2.0

  // realm used in authenticate header
  // and as cookie name
  val defaultRealm = "sbpm"
}