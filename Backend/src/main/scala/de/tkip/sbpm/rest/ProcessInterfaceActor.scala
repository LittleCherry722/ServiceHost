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

import akka.actor.Actor
import spray.http._
import akka.pattern.ask
import de.tkip.sbpm.model._
import spray.httpx.SprayJsonSupport._
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.rest.JsonProtocol._
import spray.json._
import scala.concurrent.{Future, Await}
import de.tkip.sbpm.persistence.query._

/**
 * This Actor is only used to process REST calls regarding "process"
 */
class ProcessInterfaceActor extends Actor with PersistenceInterface {
  private lazy val persistanceActor = ActorLocator.persistenceActor
  import context.dispatcher

  private implicit lazy val roles: Map[String, Role] = {
    val rolesFuture = persistanceActor ? Roles.Read.All
    val roles = Await.result(rolesFuture.mapTo[Seq[Role]], timeout.duration)
    roles.map(r => (r.name, r)).toMap
  }

  /**
   *
   * usually a REST Api should at least implement the following functions:
   * - GET without parameter => list of entity
   * - GET with id => specific entity
   * - PUT without id => new entity
   * - PUT with id => update entity
   * - DELETE with id => delete entity
   *
   * For more information about how to design a RESTful API see:
   * http://ajaxpatterns.mrg/RESTful_Service#RESTful_Principles
   *
   * Nevertheless: If an URL does not represent a resource, like the "execution" API
   * it makes sense to step away from this general template
   *
   */
  def receive = runRoute({
    get {
      /**
       * get a list of all loadable or loaded processes
       * or load a process
       *
       * e.g. GET http://localhost:8080/process
       */
      // LIST
      path("") {
        // Anfrage an den Persisence Actor liefert eine Liste von Graphen zurück
        completeWithQuery[Seq[Process]](Processes.Read())
      } ~
        // READ
        pathPrefix(IntNumber) { id =>
          val processFuture = (persistenceActor ? Processes.Read.ById(id)).mapTo[Option[Process]]
          onSuccess(processFuture) {
            processResult =>
              if (processResult.isDefined) {
                val graphFuture = if (processResult.get.activeGraphId.isDefined) {
                  (persistenceActor ? Graphs.Read.ById(processResult.get.activeGraphId.get)).mapTo[Option[Graph]]
                } else {
                  Future.successful(None)
                }

                onSuccess(graphFuture) {
                  graphResult =>
                    complete(GraphHeader(
                      processResult.get.name,
                      graphResult,
                      processResult.get.isCase, processResult.get.id))
                }
              } else {
                complete(StatusCodes.NotFound, "Process with id " + id + " not found")
              }
          }
        }
    } ~
      post {
        /**
         * create a new process
         *
         * e.g. POST http://localhost:8080/process?graph=GraphAsJSON&subjects=SubjectsAsJSON
         */
        // CREATE
        path("") {
          entity(as[GraphHeader]) { json =>
            save(None, json)
          }
        }
      } ~
      delete {
        /**
         * delete a process
         *
         * e.g. http://localhost:8080/process/12
         */
        // DELETE
        path(IntNumber) { processID =>
          completeWithDelete(
            Processes.Delete.ById(processID),
            "Process could not be deleted. Entitiy with id %d not found.",
            processID)
        }
      } ~
      put {
        /**
         * update an existing process
         *
         * e.g. PUT http://localhost:8080/process/12?graph=GraphAsJSON&subjects=SubjectsAsJSON
         */
        //UPDATE
        pathPrefix(IntNumber) { id =>
          path("") {
            entity(as[GraphHeader]) { json =>
              save(Some(id), json)
            }
          }
        }
      }
  })

  private def save(id: Option[Int], json: GraphHeader) = {
    val processFuture = (persistanceActor ? Processes.Read.ByName(json.name))
    val processResult = Await.result(processFuture, timeout.duration).asInstanceOf[Option[Process]]

    validate(!processResult.isDefined || processResult.get.id == id, "The process names have to be unique.") {
      validate(json.name.length() >= 3, "The name has to contain three or more letters.") {
        if (!json.graph.isDefined) {
          val future = (persistanceActor ? Processes.Save(Process(id, json.name, json.isCase)))
          val result = Await.result(future.mapTo[Option[Int]], timeout.duration)
          complete(JsObject("id" -> result.getOrElse(id.getOrElse(-1)).toJson))
        } else {
          val future = (persistanceActor ? Processes.Save.WithGraph(Process(id, json.name, json.isCase),
            json.graph.get.copy(date = new java.sql.Timestamp(System.currentTimeMillis()), id = None, processId = None)))
          val result = Await.result(future, timeout.duration).asInstanceOf[(Option[Int], Option[Int])]
          complete(JsObject("id" -> result._1.getOrElse(id.getOrElse(-1)).toJson, "graphId" -> result._2.toJson))
        }
      }
    }
  }
}
