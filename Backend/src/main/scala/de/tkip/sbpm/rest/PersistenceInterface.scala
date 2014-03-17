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

import scala.language.postfixOps
import akka.actor.Actor
import akka.util.Timeout
import scala.concurrent.Future
import akka.pattern._
import scala.concurrent.duration._
import spray.routing.HttpService
import spray.http.HttpHeaders
import spray.http.StatusCodes
import spray.routing.StandardRoute
import spray.httpx.marshalling.Marshaller
import spray.util.LoggingContext
import spray.routing.ExceptionHandler
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.persistence.EntityNotFoundException
import de.tkip.sbpm.persistence.query.BaseQuery
import de.tkip.sbpm.logging.DefaultLogging
import scala.reflect.ClassTag
import akka.event.Logging

/**
 * Inheriting actors have simplified access to persistence actor.
 */
trait PersistenceInterface extends HttpService with DefaultLogging {
  self: Actor =>

  def actorRefFactory = context

  protected val persistenceActor = ActorLocator.persistenceActor
  protected implicit val timeout = Timeout(10 seconds)
  import context.dispatcher


  // spray exception handler: turns exceptions that occur while
  // processing the request into internal server error response
  // with exception message as payload (also logs the exception)
  implicit def exceptionHandler(implicit log: LoggingContext) =
    ExceptionHandler {
      case e: EntityNotFoundException => ctx =>
        ctx.complete(StatusCodes.NotFound, e.getMessage)
      case e: Exception => ctx =>
        log.error(e, e.getMessage)
        ctx.complete(StatusCodes.InternalServerError, e.getMessage)
    }

  /**
   * Sends a message to the persistence actor and waits
   * for the result of type A.
   */
  protected def request[A](action: BaseQuery)(implicit tag: ClassTag[A]): Future[A] =
    (persistenceActor ? action).mapTo[A]

  /**
   * Completes the request with the result of a query to the
   * persistence actor. Responses with code 404 and given
   * message if result from persistence actor is None.
   */
  protected def completeWithQuery[A](action: BaseQuery, notFoundMsgFormat: String, notFoundMsgArgs: Any*)(implicit marshaller: Marshaller[A]) = {
    dynamic {
      onSuccess(request[Option[A]](action)) {
        res =>
          if (res.isDefined)
           complete(res.get)
         else
            notFound(notFoundMsgFormat, notFoundMsgArgs: _*)
      }
    }
  }

  /**
   * Completes the request with the result of a query to the
   * persistence actor.
   */
  protected def completeWithQuery[A](action: BaseQuery)(implicit marshaller: Marshaller[A], tag: ClassTag[A]) = {
    complete(request[A](action))
  }

  /**
   * Completes the request with the result of a save operation
   * sent to the persistence actor. Responses with code 201 if
   * new entity was created otherwise 200 (entity as payload).
   * Location path is used as base for created resource in Location header.
   * idSetter function is used to inject generated id into entity.
   */
  protected def completeWithSave[A, B](action: BaseQuery,
                                       entity: A,
                                       locationPath: String,
                                       idSetter: (A, B) => A = (a: A, b: B) => a,
                                       idFormatArgs: (B) => Array[Any] = (b: B) => Array[Any](b))
                                      (implicit marshaller: Marshaller[A]) = {
    onSuccess(request[Option[B]](action)) {
      id =>
        if (id.isDefined) {
          val newEntity = idSetter(entity, id.get)
          created(newEntity, locationPath, idFormatArgs(id.get): _*)
        } else {
          complete(entity)
        }
    }
  }

  /**
   * Completes the request with the result of a delete operation
   * sent to the persistence actor. Responses with code 404 and given
   * message if not entity was deleted otherwise 204 (No Content).
   */
  protected def completeWithDelete(action: BaseQuery, notFoundMsgFormat: String, notFoundMsgArgs: Any*) = {
    onSuccess(request[Int](action)) {
      res =>
        if (res == 0)
          notFound(notFoundMsgFormat, notFoundMsgArgs: _*)
        else
          noContent()
    }
  }

  /**
   * Executes the action without waiting for a result.
   */
  protected def execute(action: BaseQuery) = {
      val traceLogger = Logging(context.system, this.self)
      traceLogger.debug("TRACE: from " + this.self + " to " + persistenceActor  + " " + action.toString)

    persistenceActor ! action
  }

  /**
   * Completes the request with 201 Created status code and
   * Location to given location.
   */
  protected def created[A](entity: A, locationFormat: String, args: Any*)(implicit marshaller: Marshaller[A]) =
    respondWithHeader(HttpHeaders.Location(locationFormat.format(args: _*))) {
      _.complete(StatusCodes.Created, entity)
    }

  /**
   * Completes the request with 202 Accepted status code.
   */
  protected def accepted() = complete(StatusCodes.Accepted)

  /**
   * Completes the request with 404 Not Found status code and
   * given error message.
   */
  protected def notFound(msgFormat: String, args: Any*) =
    complete(StatusCodes.NotFound, msgFormat.format(args: _*))

  /**
   * Completes the request with 500 Internal Server Error
   * status code and the message from the exception.
   */
  protected def serverError(ex: Exception): StandardRoute =
    serverError(ex.getMessage)

  /**
   * Completes the request with 500 Internal Server Error
   * status code and the given message.
   */
  protected def serverError(msg: String, args: Any*): StandardRoute =
    complete(StatusCodes.InternalServerError, msg.format(args: _*))

  /**
   * Completes the request with 204 No Content status code.
   */
  protected def noContent() = complete(StatusCodes.NoContent)

  /**
   * Generate the url path for a created entity.
   * @param name entity name e.g. "group"
   * @param format string format for primary key e.g. "%d"
   */
  protected def pathForEntity(name: String, format: String) =
    "/%s/%s".format(name, format)

}

