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

import spray.http.HttpHeaders.`WWW-Authenticate`
import spray.routing.AuthenticationFailedRejection.CredentialsRejected
import spray.routing.directives._
import spray.routing.Directive
import de.tkip.sbpm.ActorLocator
import akka.actor.ActorContext
import akka.pattern._
import java.util.UUID
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import shapeless._
import spray.routing._
import spray.http.HttpCookie
import spray.util._
import shapeless._
import spray.http._
import akka.actor.ActorRefFactory
import akka.actor.ActorSystem
import de.tkip.sbpm.model.User
import spray.routing.authentication.UserPass
import de.tkip.sbpm.persistence.query.Users
import akka.event.Logging
import de.tkip.sbpm.instrumentation.{ClassTraceLogger, TraceLogger}

case class MissingSessionRejection(sessionId: String) extends Rejection
case object MissingUserRejection extends Rejection

/**
 * Provides Spray directive for session handling.
 */
trait SessionDirectives extends ClassTraceLogger {
  import BasicDirectives._
  import CookieDirectives._
  import RouteDirectives._
  import MiscDirectives._

  private implicit val timeout = Timeout(10 seconds)

  /**
   * Reject request with "missing session" error.
   */
  private def rejectSession(sessionId: String): StandardRoute =
    reject(MissingSessionRejection(sessionId))

  /**
   * Reject request with "missong user" error.
   */
  private def rejectUser: StandardRoute =
    reject(MissingUserRejection)

  /**
   * Retrieve session, referenced in the cookie from SessionActor.
   */
  private def getSession(sessionId: UUID)(implicit refFactory: ActorRefFactory): Option[Session] = {
    val sessionFuture = ActorLocator.sessionActor ?? GetSession(sessionId)
    Await.result(sessionFuture.mapTo[Option[Session]], timeout.duration)
  }

  /**
   * Directive to read the session with the given id.
   */
  def session(id: UUID)(implicit refFactory: ActorRefFactory): Directive[Session :: HNil] = {
    val session = getSession(id)
    if (session.isDefined)
      provide(session.get)
    else
      rejectSession(id.toString)
  }

  /**
   * Directive to read the session.
   * Requires a valid session cookie.
   */
  def session(implicit refFactory: ActorRefFactory): Directive[Session :: HNil] = {
    cookie(defaultRealm) flatMap { implicit cookie =>
      try {
        val sessionId = UUID.fromString(cookie.content)
        val session = getSession(sessionId)
        if (session.isDefined)
          provide(session.get)
        else
          rejectSession(cookie.content)
      } catch {
        // UUID could not be parsed
        case _: IllegalArgumentException => rejectSession(cookie.content)
      }
    }
  }

  /**
   * Directive to read the session.
   * In case session could not be found or an invalid session cookie,
   * Nothing is returned.
   */
  def optionalSession(implicit refFactory: ActorRefFactory): Directive1[Option[Session]] = {
    optionalCookie(defaultRealm) flatMap {
      case None => provide(None)
      case Some(cookie) =>
        try {
          val sessionId = UUID.fromString(cookie.content)
          provide(getSession(sessionId))
        } catch {
          // UUID could not be parsed
          case _: IllegalArgumentException => provide(None)
        }
    }
  }

  /**
   * Saves the given user id into a session.
   * The session is created if no valid could be found.
   * Returnes the current session.
   */
  def saveSession(userId: Option[Int])(implicit refFactory: ActorRefFactory): Directive1[Session] = {
    optionalSession(refFactory) map {
      case None =>
        (ActorLocator.sessionActor ?? CreateSession(userId)).mapTo[Session]
      case Some(s) =>
        (ActorLocator.sessionActor ?? UpdateSession(s.id, userId)).mapTo[Session]
    } map {
      Await.result(_, timeout.duration)
    }
  }

  /**
   * Sets a session cookie with given session id.
   */
  def setSessionCookie(session: Session)(implicit refFactory: ActorRefFactory): Directive0 = {
    setCookie(HttpCookie(defaultRealm, session.id.toString, path = Some("/"))) & {
      if (session.userId.isDefined)
        setCookie(HttpCookie(defaultRealm + "-userId", session.userId.get.toString, path = Some("/")))
      else
        deleteCookie(HttpCookie(defaultRealm + "-userId", "", path = Some("/")))
    }
  }


  /**
   * Delete current session if it exists.
   */
  def deleteSession(implicit refFactory: ActorRefFactory): Directive0 = {
    optionalSession flatMap { session =>
      if (session.isDefined) {
        ActorLocator.sessionActor ! DeleteSession(session.get.id)
      }
      deleteCookie(HttpCookie(defaultRealm, "", path = Some("/"))) &
        deleteCookie(HttpCookie(defaultRealm + "-userId", "", path = Some("/")))
    }
  }

  /**
   * Directive to get user currently logged in.
   * Rejects if either session or user could not be found.
   */
  def user(implicit refFactory: ActorRefFactory): Directive[User :: HNil] = {
    userId flatMap { id =>
      val userFuture = ActorLocator.persistenceActor ?? Users.Read.ById(id)
      val user = Await.result(userFuture.mapTo[Option[User]], timeout.duration)
      if (user.isDefined)
        provide(user.get)
      else
        rejectUser
    }
  }

  /**
   * Directive to get id of user from current session.
   * Rejects if either session could not be found.
   * For performance reasons there's not lookup in the database
   * if user really exists.
   */
  def userId(implicit refFactory: ActorRefFactory): Directive[Int :: HNil] = {
    session flatMap { sess =>
      if (sess.userId.isDefined)
        provide(sess.userId.get)
      else
        rejectUser
    }
  }

  /**
   * Directive for user login using username and password.
   * Rejects if authentication fails.
   */
  def login(userPass: UserPass)(implicit refFactory: ActorRefFactory): Directive1[User] = {
    val authFuture = ActorLocator.userPassAuthActor ?? userPass
    Await.result(authFuture.mapTo[Option[User]], timeout.duration) match {
      case None => {
        val challenge = `WWW-Authenticate`(HttpChallenge("Basic", defaultRealm))
        reject(AuthenticationFailedRejection(CredentialsRejected, List(challenge)))
      }
      case Some(user) => {
        saveSession(user.id) flatMap { s =>
          setSessionCookie(s) & provide(user)
        }
      }
    }
  }

}

object SessionDirectives extends SessionDirectives
