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

import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.MappingInfo
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.logging.DefaultLogging
import de.tkip.sbpm.rest.JsonProtocol._
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport._
import spray.routing.ExceptionHandler
import spray.util.LoggingContext

/**
 * This Actor is only used to process REST calls regarding "execution"
 */
class ExecutionInterfaceActor extends AbstractInterfaceActor {
  import context.dispatcher
  implicit val timeout = Timeout(30 seconds)

  implicit def exceptionHandler =
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
          val msg = ReadProcessInstance(userId, processInstanceID)
          log.debug("TRACE: from " + this.self + " to " + subjectProviderManager + " " + msg)
          val future = (subjectProviderManager ? msg).mapTo[ReadProcessInstanceAnswer]
          future.map(result => result.answer)
        }
      } ~
        // Show Actions
        path("action") {
          complete {
            val msg = GetAvailableActions(userId)
            log.debug("TRACE: from " + this.self + " to " + subjectProviderManager + " " + msg)
            val availableActionsFuture = (subjectProviderManager ? msg).mapTo[AvailableActionsAnswer]
            availableActionsFuture.map(result => result.availableActions)
          }
        } ~
        path("history") {
          //you cannot run statements inside the path-block like above, instead, put them into a block inside the complete statement
          complete {
            val processManagerActor = ActorLocator.processManagerActor
            val msg = GetNewHistory()
            log.debug("TRACE: from " + this.self + " to " + processManagerActor + " " + msg)
            val getHistoryFuture = (processManagerActor ? msg).mapTo[NewHistoryAnswer]
            getHistoryFuture.map(result => result.history.entries.filter(x => x.userId == Some(userId) || x.userId == None))
          }
        } ~
        //LIST
        path("") {
          complete {
            val msg = GetAllProcessInstances(userId)
            log.debug("TRACE: from " + this.self + " to " + subjectProviderManager + " " + msg)
            val future = (subjectProviderManager ? msg).mapTo[AllProcessInstancesAnswer]
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
            val msg = KillProcessInstance(processInstanceID)
            log.debug("TRACE: from " + this.self + " to " + subjectProviderManager + " " + msg)
            val future = (subjectProviderManager ? msg)
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
                val msg = mixExecuteActionWithRouting(json)
                log.debug("TRACE: from " + this.self + " to " + subjectProviderManager + " " + msg)
                val future = (subjectProviderManager ? msg).mapTo[ExecuteActionAnswer]
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
                val msg = CreateProcessInstance(userId, json.processId, name, None, Map[SubjectID, MappingInfo]())
                log.debug("TRACE: from " + this.self + " to " + subjectProviderManager + " " + msg)
                val future = (subjectProviderManager ? msg).mapTo[ProcessInstanceCreated]
                future.map(result => result.answer)
              }
            }
          }
        }
      }
  })
}
