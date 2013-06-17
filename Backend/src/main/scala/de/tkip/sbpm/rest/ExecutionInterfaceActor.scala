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

import scala.concurrent.duration._
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.rest.JsonProtocol._
import de.tkip.sbpm.rest.SprayJsonSupport._
import spray.http.StatusCodes
import spray.routing._
import spray.util.LoggingContext
import de.tkip.sbpm.application.subject.ExecuteAction
import de.tkip.sbpm.application.subject.ExecuteActionAnswer
import scala.concurrent.Await
import de.tkip.sbpm.application.subject.mixExecuteActionWithRouting
import scala.concurrent.ExecutionContext

/**
 * This Actor is only used to process REST calls regarding "execution"
 */
class ExecutionInterfaceActor extends AbstractInterfaceActor {
  implicit val timeout = Timeout(5 seconds)
  override implicit def executionContext = ExecutionContext.Implicits.global
  val logger = Logging(context.system, this)

  override def preStart() {
    logger.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    logger.debug(getClass.getName + " stopped.")
  }

  implicit def exceptionHandler(implicit log: LoggingContext) =
    ExceptionHandler.fromPF {
      case e: IllegalArgumentException => ctx => {
        ctx.complete(StatusCodes.BadRequest, e.getMessage)
      }
      case e: Exception => ctx => {
        log.error(e, e.getMessage)
        ctx.complete(StatusCodes.InternalServerError, e.getMessage)
      }
    }

  def actorRefFactory = context

  private lazy val subjectProviderManager = ActorLocator.subjectProviderManagerActor

  def routing = runRoute({
    // TODO: aus cookie auslesen
    //    formField("userid") { userID =>
    get {
      //READ
      path(IntNumber) { processInstanceID =>
        implicit val timeout = Timeout(5 seconds)
        val future = subjectProviderManager ? ReadProcessInstance(userId, processInstanceID)
        val result = Await.result(future, timeout.duration).asInstanceOf[ReadProcessInstanceAnswer]
        complete(result.answer)
      } ~
        // Show Actions
        path("action") {
          val availableActionsFuture =
            (subjectProviderManager ? GetAvailableActions(userId))
              .mapTo[AvailableActionsAnswer]
          val result = Await.result(availableActionsFuture, timeout.duration)
          complete(result.availableActions)
        } ~
        //LIST
        path("") {
          implicit val timeout = Timeout(5 seconds)
          val future = (subjectProviderManager ? GetAllProcessInstances(userId)).mapTo[AllProcessInstancesAnswer]
          val result = Await.result(future, timeout.duration)

          complete(result.processInstanceInfo)
        }

    } ~
      delete {
        //DELETE
        path(IntNumber) { processInstanceID =>
          //stop and delete given process instance
          implicit val timeout = Timeout(5 seconds)
          // error gets caught automatically by the exception handler
          val future = subjectProviderManager ? KillProcessInstance(processInstanceID)
          val result = Await.result(future, timeout.duration).asInstanceOf[KillProcessInstanceAnswer]
          complete(StatusCodes.NoContent)
        }
      } ~
      put {
        //UPDATE
        pathPrefix(IntNumber) { processInstanceID =>
          path("^$"r) { regex =>
            entity(as[ExecuteAction]) { json =>
              //execute next step
              implicit val timeout = Timeout(5 seconds)
              val future = (subjectProviderManager ? mixExecuteActionWithRouting(json))
              val result = Await.result(future, timeout.duration).asInstanceOf[ExecuteActionAnswer]
              complete(result.answer)
            }
          }
        }
      } ~
      post {
        //CREATE
        pathPrefix("") {
          path("^$"r) { regex =>
            entity(as[ProcessIdHeader]) { json =>
              implicit val timeout = Timeout(5 seconds)
              val future = subjectProviderManager ? CreateProcessInstance(userId, json.processId)
              val result = Await.result(future, timeout.duration).asInstanceOf[ProcessInstanceCreated]
              complete(result.answer)
            }
          }
        }
      }
  })
}
