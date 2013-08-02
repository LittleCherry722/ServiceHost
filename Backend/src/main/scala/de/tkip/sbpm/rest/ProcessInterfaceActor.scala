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
import spray.routing._
import akka.pattern.ask
import de.tkip.sbpm.model._
import spray.httpx.SprayJsonSupport._
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.rest.JsonProtocol._
import spray.json._
import scala.concurrent.Future
import de.tkip.sbpm.persistence.query._

/**
 * This Actor is only used to process REST calls regarding "process"
 */
class ProcessInterfaceActor extends Actor with PersistenceInterface {
  private lazy val persistanceActor = ActorLocator.persistenceActor
  import context.dispatcher


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
        pathPrefix(IntNumber) {
          id =>
            read(id)
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
          parseGraphHeader {
            json =>
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
        path(IntNumber) {
          processID =>
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
        pathPrefix(IntNumber) {
          id =>
            parseGraphHeader {
              json =>
                save(Some(id), json)
            }
        }
      }
  })

  /**
   * Reads the process and its connected graph.
   *
   */
  private def read(id: Int): Route = {
    val processFuture = (persistenceActor ? Processes.Read.ById(id)).mapTo[Option[Process]]
    onSuccess(processFuture) {
      processResult =>
        if (processResult.isDefined) {
          readProcess(processResult.get)
        } else {
          complete(StatusCodes.NotFound, "Process with id " + id + " not found")
        }
    }
  }

  /**
   * Reads the process and its connected graph.
   */
  private def readProcess(process: Process): Route = {
    val roleFuture = (persistanceActor ? Roles.Read.All).mapTo[Seq[Role]]
    val graphFuture = if (process.activeGraphId.isDefined) {
      (persistenceActor ? Graphs.Read.ById(process.activeGraphId.get)).mapTo[Option[Graph]]
    } else {
      Future.successful(None)
    }

    val result = graphFuture.map {
      graphResult => GraphHeader(
        process.name,
        graphResult,
        process.isCase,
        process.id)
    }

    onSuccess(roleFuture) {
      roles =>
        // implicite value for marshalling
        implicit val roleMap = roles.map(r => (r.name, r)).toMap
        complete(result)
    }
  }

  /**
   * Saves the given process and its connected graph, if available. Validates, if the process name is unique and if the
   * process name contains three or more letters.
   *
   */
  private def save(id: Option[Int], json: GraphHeader): Route = {
    val processFuture = (persistanceActor ? Processes.Read.ByName(json.name)).mapTo[Option[Process]]
    onSuccess(processFuture) {
      processResult =>
        validate(!processResult.isDefined || processResult.get.id == id, "The process names have to be unique.") {
          validate(json.name.length() >= 3, "The name has to contain three or more letters.") {
            if (json.graph.isDefined) {
              saveWithGraph(id, json)
            } else {
              saveWithoutGraph(id, json)
            }
          }
        }
    }
  }

  /**
   * Saves the given process without its graph.
   */
  private def saveWithoutGraph(id: Option[Int], json: GraphHeader): Route = {
    val process = Process(id, json.name, json.isCase)
    val future = (persistanceActor ? Processes.Save(process)).mapTo[Option[Int]]
    val result = future.map(resultId => JsObject("id" -> resultId.getOrElse(id.getOrElse(-1)).toJson))
    complete(result)
  }

  /**
   * Saves the given process with its graph.
   */
  private def saveWithGraph(id: Option[Int], json: GraphHeader): Route = {
    val process = Process(id, json.name, json.isCase)
    val graph = json.graph.get.copy(date = new java.sql.Timestamp(System.currentTimeMillis()), id = None, processId = None)
    val future = (persistanceActor ? Processes.Save.WithGraph(process, graph)).mapTo[(Option[Int], Option[Int])]
    val result = future.map(result => JsObject("id" -> result._1.getOrElse(id.getOrElse(-1)).toJson, "graphId" -> result._2.toJson))
    complete(result)
  }

  /**
   * Parses a GraphHeader from request.
   */
  private def parseGraphHeader(op: GraphHeader => Route): Route = {
    onSuccess((persistanceActor ? Roles.Read.All).mapTo[Seq[Role]]) {
      roles =>
        // implicite value for marshalling
        implicit val roleMap = roles.map(r => (r.name, r)).toMap

        entity(as[GraphHeader]) {
          json =>
            op(json)
        }
    }
  }
}
