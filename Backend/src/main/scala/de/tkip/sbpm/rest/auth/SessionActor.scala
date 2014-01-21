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
import java.util.UUID
import scala.collection.mutable.Map
import java.util.Date
import akka.actor.Cancellable
import scala.concurrent.duration._
import akka.event.Logging

/**
 * Message for creating a new session, optionally with a user id.
 * Created session object is returned.
 */
case class CreateSession(userId: Option[Int] = None)
/**
 * Message for retrieving a session.
 * Some(Session) or None (if not found) is returned.
 */
case class GetSession(sessionId: UUID)
/**
 * Message for updating existing session. Resets expiration date.
 * Some(Session) or None (if not found) is returned.
 */
case class UpdateSession(sessionId: UUID, userId: Option[Int] = None)
/**
 * Message for deleting existing session.
 */
case class DeleteSession(sessionId: UUID)

/**
 * Represents a session in the store.
 */
case class Session(id: UUID, expires: Date = defaultSessionExpiry, userId: Option[Int] = None)

/**
 * Provides an in-memory session store for associating
 * user ids with sessions (e.g. by using cookies).
 */
class SessionActor extends Actor {
  // save session in memory
  private val sessions = Map[UUID, Session]()
  // create new session id
  private def newSessionId = UUID.randomUUID
  import context.dispatcher

  val traceLogger = Logging(context.system, this)

  def receive = {
    case CreateSession(userId) => {
      updateSession(newSessionId, userId)
    }

    case GetSession(sessionId) => {

      val msg = (sessions.get(sessionId) match {
        case s @ Some(Session(_, expires, _)) =>
          // return none for expired session 
          if (expires.before(new Date)) None
          else s
        case None => None
      })

      traceLogger.debug("TRACE: from " + this.self + " to " + sender + " " + msg)
      sender ! msg
      cleanup()
    }

    case UpdateSession(sessionId, userId) =>
      updateSession(sessionId, userId)

    case DeleteSession(sessionId) => {
      val session = sessions.get(sessionId)
      if (session.isDefined) {
        sessions -= sessionId
        traceLogger.debug("TRACE: from " + this.self + " to " + sender + " " + true)

        sender ! true
      } else {
        traceLogger.debug("TRACE: from " + this.self + " to " + sender + " " + false)

        sender ! false
      }
      cleanup()
    }
  }

  /**
   * Delete expired session from store.
   */
  private def cleanup() = {
    val now = new Date
    val expired = sessions.filter(_._2.expires.before(now)).map(_._1)
    if (expired.size > 0)
      sessions --= expired
  }

  /**
   * Updates session store.
   */
  private def updateSession(sessionId: UUID, userId: Option[Int]) = {
    val session = Session(sessionId, userId = userId)
    sessions += (sessionId -> session)
    sender ! session
    cleanup()
  }
}