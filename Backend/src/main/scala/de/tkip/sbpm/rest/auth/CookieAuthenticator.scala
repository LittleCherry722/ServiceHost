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

import de.tkip.sbpm.rest.auth._
import spray.routing.AuthenticationFailedRejection.CredentialsRejected

import scala.concurrent.ExecutionContext
import spray.routing.RequestContext
import spray.http._
import spray.util._
import spray.http.HttpHeaders._
import spray.routing.authentication._
import scala.concurrent.Future
import spray.routing.AuthenticationFailedRejection
import de.tkip.sbpm.model.User
import java.util.UUID
import akka.actor.ActorContext
import de.tkip.sbpm.ActorLocator
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.Actor
import spray.routing.Rejection
import de.tkip.sbpm.instrumentation.ClassTraceLogger

/**
 * Spray rejection if there was a unauthenticated request.
 * Includes supported schemes, app realm and session id
 * to send specific Authorization headers and cookies to
 * the browser.
 * If session id is set to None the cookie should be deleted.
 */
case class AuthenticationRejection(supportedSchemes: Seq[String], realm: String, sessionId: Option[UUID] = None) extends Rejection

/**
 * Spray authenticator for authenticating users.
 * If user session cookie can be found access is granted,
 * otherwise Authorization header is checked for valid
 * credentials. Multiple authentication schemes can be supported
 * (define handling actors in package object).
 */
class CookieAuthenticator(implicit val executionContext: ExecutionContext, implicit val actorContext: ActorContext)
  extends ContextAuthenticator[Session] with ClassTraceLogger {
  private implicit val timeout = Timeout(10 seconds)
  private lazy val sessionActor = ActorLocator.sessionActor

  /**
   * Called when Spray authenticates the request.
   */
  def apply(ctx: RequestContext) = {
    // check if session cookie is sent
    val cookie = ctx.request.cookies.find(_.name == defaultRealm)
    if (cookie.isDefined) {
      try {
        // check if a session exists for the cookie value
        val sessionFuture =
          sessionActor ?? GetSession(UUID.fromString(cookie.get.content))
        sessionFuture.flatMap {
          // no session -> new auth required
          case None => checkAuthorizationHeader(ctx)
          // session but no user -> new auth required
          case Some(Session(id, _, None)) => checkAuthorizationHeader(ctx, Some(id))
          // session with user id -> update session and return cookie
          case Some(Session(id, _, userId)) => saveSession(Some(id), userId)
        }
      } catch {
        // if invalid session id in cookie
        case _: IllegalArgumentException => checkAuthorizationHeader(ctx)
      }
    } else {
      // no cookie -> new auth required
      checkAuthorizationHeader(ctx)
    }
  }

  /**
   * Check if credentials are given in Authorization header and
   * check its validity.
   * Saves the user id to the session and returns the session cookie
   * if authentication was successful.
   */
  def checkAuthorizationHeader(ctx: RequestContext, sessionId: Option[UUID] = None): Future[Authentication[Session]] = {
    val authHeader = ctx.request.headers.findByType[`Authorization`]
    if (authHeader.isDefined) {
      val challenge = `WWW-Authenticate`(HttpChallenge("Basic", defaultRealm))
      authenticate(authHeader.get.credentials) flatMap {
        // could not authenticate -> reject request with 401
        case None => Future(Left { AuthenticationFailedRejection(CredentialsRejected, List(challenge)) })
        // auth successful -> save user id to session
        case id => saveSession(sessionId, id)
      }
    } else {
      // no auth header -> reject request with WWW-Authenticate
      Future(Left { AuthenticationRejection(defaultSchemes.keys.toSeq, defaultRealm, sessionId) })
    }
  }

  /**
   * Authenticate user according to requested auth scheme.
   * Returns user id if auth was successful.
   */
  def authenticate(credentials: HttpCredentials): Future[Option[Int]] = {
    // extract scheme from header
    val scheme = credentials.value.takeWhile(_ != ' ')
    // get handling actor for scheme
    val authActor = defaultSchemes.get(scheme)
    if (!authActor.isDefined)
      // no matching actor found -> fail
      Future(None)
    else
      // redirect auth request to actor
      (ActorLocator.actor(authActor.get) ?? credentials).mapTo[Option[User]].map { u =>
        if (u.isDefined) u.get.id
        else None
      }
  }

  /**
   * Saves the given user id to the session with given id.
   * If id = None, a new session is created.
   * Returns the session cookie.
   */
  def saveSession(id: Option[UUID], userId: Option[Int]) = {
    // send request to session actor
    sessionActor ?? {
      if (id.isDefined)
        UpdateSession(id.get, userId)
      else
        CreateSession(userId)
    }
  }.mapTo[Session].map(s => Right { s })
}
