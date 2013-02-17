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