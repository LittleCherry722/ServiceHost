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

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.Actor
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.AvailableAction
import de.tkip.sbpm.model._
import de.tkip.sbpm.rest.JsonProtocol._
import de.tkip.sbpm.rest.SprayJsonSupport._
import de.tkip.sbpm.rest.GraphJsonProtocol.graphJsonFormat
import spray.http.MediaTypes._
import spray.http.StatusCodes
import spray.httpx.marshalling.Marshaller
import spray.json._
import spray.routing._
import spray.util.LoggingContext
import akka.actor.Props
import de.tkip.sbpm.application.subject.ExecuteAction
import de.tkip.sbpm.application.subject.ExecuteActionAnswer
import scala.concurrent.Await
import de.tkip.sbpm.application.subject.mixExecuteActionWithRouting
import scala.concurrent.ExecutionContext
import de.tkip.sbpm.persistence.query.Processes
import de.tkip.sbpm.persistence.query.ProcessInstances
import de.tkip.sbpm.persistence.query.Graphs

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
  private lazy val persistanceActor = ActorLocator.persistenceActor

  def routing = runRoute({
    // TODO: aus cookie auslesen
    //    formField("userid") { userID =>
    get {
      //READ
      path(IntNumber) { processInstanceID =>

        implicit val timeout = Timeout(5 seconds)
        val composedFuture = for {
          processInstanceFuture <- (persistanceActor ? ProcessInstances.Read.ById(processInstanceID.toInt)).mapTo[Option[ProcessInstance]]
          graphFuture <- {
            if (processInstanceFuture.isDefined)
              (persistanceActor ? Graphs.Read.ById(processInstanceFuture.get.graphId)).mapTo[Option[Graph]]
            else
              throw new Exception("Processinstance '" + processInstanceID + "' does not exist.")
          }
          historyFuture <- (subjectProviderManager ? {
            GetHistory(userId, processInstanceID)
          }).mapTo[HistoryAnswer]
          availableActionsFuture <- (subjectProviderManager ? {
            GetAvailableActions(userId, processInstanceID)
          }).mapTo[AvailableActionsAnswer]
        } yield JsObject(
          "processId" -> JsNumber(processInstanceFuture.get.processId),
          "graph" -> {
            if (graphFuture.isDefined)
              graphFuture.get.toJson
            else
              JsNull
          },
          // TODO make isTerminated nicer
          "isTerminated" -> JsBoolean(historyFuture.history.processEnded.isDefined),
          "history" -> historyFuture.history.toJson,
          "actions" -> availableActionsFuture.availableActions.toJson)
        complete(composedFuture)
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
          val future = (subjectProviderManager ? GetAllProcessInstances(userId.toInt)).mapTo[AllProcessInstancesAnswer]
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
              complete(
                JsObject(
                  "processId" -> result.processID.toJson,
                  "graph" -> result.graph.toJson,
                  "isTerminated" -> result.isTerminated.toJson,
                  "history" -> result.history.toJson,
                  "actions" -> result.availableActions.toJson))
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
              val future = subjectProviderManager ? CreateProcessInstance(userId.toInt, json.processId)
              val result = Await.result(future, timeout.duration).asInstanceOf[ProcessInstanceCreated]
              complete(result.answer)
            }
          }
        }
      }
  })
}
