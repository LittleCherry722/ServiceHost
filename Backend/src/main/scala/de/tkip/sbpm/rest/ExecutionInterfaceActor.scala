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
import scala.concurrent.ExecutionContext
import de.tkip.sbpm.application.history._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.logging.DefaultLogging

/**
 * This Actor is only used to process REST calls regarding "execution"
 */
class ExecutionInterfaceActor extends AbstractInterfaceActor with DefaultLogging {
  import context.dispatcher
  implicit val timeout = Timeout(10 seconds)

  implicit def exceptionHandler(implicit log: LoggingContext) =
    ExceptionHandler {
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
        complete {
          val future = (subjectProviderManager ? ReadProcessInstance(userId, processInstanceID)).mapTo[ReadProcessInstanceAnswer]
          future.map(result => result.answer)
        }
      } ~
        // Show Actions
        path("action") {
          complete {
            val availableActionsFuture =
              (subjectProviderManager ? GetAvailableActions(userId))
                .mapTo[AvailableActionsAnswer]
            availableActionsFuture.map(result => result.availableActions)
          }
        } ~
        path("history") {
          //you cannot run statements inside the path-block like above, instead, put them into a block inside the complete statement
          complete {
            val getHistoryFuture = (ActorLocator.processManagerActor ? GetNewHistory()).mapTo[NewHistoryAnswer]
            getHistoryFuture.map(result => result.history.entries.filter(x => x.userId == Some(userId) || x.userId == None))
          }
        } ~
        //LIST
        path("") {
          complete {
            val future = (subjectProviderManager ? GetAllProcessInstances(userId)).mapTo[AllProcessInstancesAnswer]
            future.map(result => result.processInstanceInfo)
          }
        }

    } ~
      delete {
        //DELETE
        path(IntNumber) { processInstanceID =>
          //stop and delete given process instance
          // error gets caught automatically by the exception handler
          complete {
            val future = (subjectProviderManager ? KillProcessInstance(processInstanceID))
            future.map(_ => StatusCodes.NoContent)
          }
        }
      } ~
      put {
        //UPDATE
        pathPrefix(IntNumber) { processInstanceID =>
          path("") {
            entity(as[ExecuteAction]) { json =>
              //execute next step
              complete {
                val future = (subjectProviderManager ? mixExecuteActionWithRouting(json)).mapTo[ExecuteActionAnswer]
                future.map(result => result.answer)
              }
            }
          }
        }
      } ~
      post {
        //CREATE
        pathPrefix("") {
          path("") {
            entity(as[ProcessIdHeader]) { json =>
              complete {
                val name = json.name.getOrElse("Unnamed")// TODO not as an Option
                val future = (subjectProviderManager ? CreateProcessInstance(userId, json.processId, name)).mapTo[ProcessInstanceCreated]
                future.map(result => result.answer)
              }
            }
          }
        }
      }
  })
}
