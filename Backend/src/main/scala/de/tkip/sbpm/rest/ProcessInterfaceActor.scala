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

import akka.event.Logging
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.miscellaneous.RoleMapper
import de.tkip.sbpm.instrumentation.InstrumentedActor
import de.tkip.sbpm.model._
import de.tkip.sbpm.persistence.query._
import de.tkip.sbpm.repository.RepositoryPersistenceActor.{DeleteInterface, SaveInterface}
import de.tkip.sbpm.rest.JsonProtocol._
import de.tkip.sbpm.verification.ModelConverter._
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing._

import scala.concurrent.Future

/**
 * This Actor is only used to process REST calls regarding "process"
 */
class ProcessInterfaceActor extends InstrumentedActor with PersistenceInterface {

  // This array is used to filter for the processes, which are shown in the showcase
  // if this array is empty all processes will be shown
  val showProcesses = Array[Int]()
  private val logger = Logging(context.system, this)

  private lazy val subjectProviderManagerActor = ActorLocator.subjectProviderManagerActor

  private lazy val persistanceActor = ActorLocator.persistenceActor
  private lazy val repositoryPersistenceActor = ActorLocator.repositoryPersistenceActor

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
  val route = runRoute({
    get {
      /**
       * get a list of all loadable or loaded processes
       * or load a process
       *
       * e.g. GET http://localhost:8080/process
       */
      // LIST
      pathEnd {
        dynamic {
          // Anfrage an den Persisence Actor liefert eine Liste von Graphen zurück'
          complete(for {
            processes <- (persistanceActor ?? Processes.Read()).mapTo[Seq[Process]]
            filtered = if (showProcesses.isEmpty) processes
            else processes filter (showProcesses contains _.id.getOrElse(-1))
          } yield filtered)
        }
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
        pathEnd {
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
          processID => {
            // also delete interface
            sendDeleteInterfaceForProcessId(processID)
            completeWithDelete(
              Processes.Delete.ById(processID),
              "Process could not be deleted. Entitiy with id %d not found.",
              processID)
          }
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

  override def wrappedReceive = {
    case ctx: RequestContext => {
      log.debug(s"received requestContext: $ctx")
      route(ctx)
    }
    case msg => log.error(s"Unhandled message: $msg")
  }

  private def sendDeleteInterfaceForProcessId(id: Int) = {
    log.debug("[DELETE INTERFACE] starting to get process information")
    val processFuture = (persistenceActor ?? Processes.Read.ById(id)).mapTo[Option[Process]]
    processFuture.onSuccess {
      case processResult =>
        log.debug("[DELETE INTERFACE] Got process Information")
        if (processResult.isDefined) {
          log.debug("[DELETE INTERFACE] Process available")
          val interfaceId = processResult.get.interfaceId
          if (interfaceId.isDefined) {
            log.debug("[DELETE INTERFACE] Sending delete interface request")
            repositoryPersistenceActor ! DeleteInterface(interfaceId.get)
          }
        }
    }
  }

  /**
   * Reads the process and its connected graph.
   *
   */
  private def read(id: Int): Route = {
    val processFuture = (persistenceActor ?? Processes.Read.ById(id)).mapTo[Option[Process]]
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
    val roleFuture = (persistanceActor ?? Roles.Read.All).mapTo[Seq[Role]]
    val graphFuture = if (process.activeGraphId.isDefined) {
      (persistenceActor ?? Graphs.Read.ById(process.activeGraphId.get)).mapTo[Option[Graph]]
    } else {
      Future.successful(None)
    }

    val result = graphFuture.map {
      graphResult =>
        GraphHeader(
          process.name,
          process.interfaceId,
          process.publishInterface,
          Seq.empty,  // verification errors
          graphResult,
          process.isCase,
          process.id)
    }

    onSuccess(roleFuture) {
      roles =>
        // implicite value for marshalling
        val roleMap = roles.map(r => (r.name, r)).toMap
        implicit val roleMapper: RoleMapper = RoleMapper.createRoleMapper(roleMap)
        complete(result)
    }
  }

  /**
   * Saves the given process and its connected graph, if available. Validates, if the process name is unique and if the
   * process name contains three or more letters.
   *
   */
  private def save(id: Option[Int], json: GraphHeader): Route = {
    val processFuture = (persistanceActor ?? Processes.Read.ByName(json.name)).mapTo[Option[Process]]
    onSuccess(processFuture) {
      processResult =>
        validate(processResult.isEmpty || processResult.get.id == id, "The process names have to be unique.") {
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
    val process = Process(id, json.interfaceId, json.verificationErrors, json.publishInterface, json.name, json.isCase)
    val future = (persistanceActor ?? Processes.Save(process)).mapTo[Option[Int]]
    val result = future.map(resultId => JsObject("id" -> resultId.getOrElse(id.getOrElse(-1)).toJson))
    complete(result)
  }

  /**
   * Saves the given process with its graph.
   */
  private def saveWithGraph(id: Option[Int], json: GraphHeader): Route = {
    val currentInterfaceId: Option[Int] = json.interfaceId
    log.info(s"CURRENT INTERFACEID DEFINED: ${currentInterfaceId.isDefined}")
    lazy val oldProcessFuture = (persistanceActor ?? Processes.Read.ById(id.getOrElse(-1))).mapTo[Option[Process]]

    val graph = json.graph.get.copy(date = new java.sql.Timestamp(System.currentTimeMillis()), id = None, processId = None)
    val verificationErrors = verifyGraph(graph).left.getOrElse(Seq.empty)
    val interfaceIdFuture: Future[Either[Seq[String], Option[Int]]] = if (json.publishInterface) {
      log.debug("[SAVE INTERFACE] Sending save interface request")
      (repositoryPersistenceActor ?? SaveInterface(json)).mapTo[Either[Seq[String], Option[Int]]]
    } else {
      for {
        oldProcess <- oldProcessFuture
        interfaceId <- if (oldProcess.map { op => op.publishInterface }.isDefined && currentInterfaceId.isDefined) {
          (repositoryPersistenceActor ?? DeleteInterface(currentInterfaceId.get)).mapTo[Either[Seq[String], Option[Int]]]
        } else {
          Future(Right(json.interfaceId))
        }
      } yield interfaceId
    }
    val resultFuture = for {
      interfaceId <- interfaceIdFuture
      oldProcess <- oldProcessFuture
      oldPublishFlag = oldProcess.fold(false)(_.publishInterface)
      publishFlag = if (interfaceId.isLeft) { oldPublishFlag } else { json.publishInterface }
      process = Process(id, interfaceId.right.toOption.flatten, verificationErrors, publishFlag, json.name, json.isCase)
      (processId, graphId) <- (persistanceActor ?? Processes.Save.WithGraph(process, graph)).mapTo[(Option[Int], Option[Int])]
      result = JsObject("id" -> processId.getOrElse(id.getOrElse(-1)).toJson, "graphId" -> graphId.toJson)
      roles <- (persistanceActor ?? Roles.Read.All).mapTo[Seq[Role]]
      savedProcess = interfaceId.right.map { _ =>
        GraphHeader(
          name = process.name,
          interfaceId = process.interfaceId,
          verificationErrors = verificationErrors,
          publishInterface = process.publishInterface,
          graph = Some(graph.copy(id = graphId, processId = processId)),
          isCase = process.isCase,
          id = processId)
      }
    } yield (savedProcess, roles)
    onSuccess(resultFuture) { res =>
      val (eGraphHeader, roles) = res
      // implicit value for marshalling
      val roleMap = roles.map(r => (r.name, r)).toMap
      implicit val roleMapper: RoleMapper = RoleMapper.createRoleMapper(roleMap)
      eGraphHeader match {
        case Right(gh) => complete(gh)
        case Left(errors) => complete(StatusCodes.InternalServerError, errors)
      }
    }
  }

  /**
   * Parses a GraphHeader from request.
   */
  private def parseGraphHeader(op: GraphHeader => Route): Route = {
    dynamic {
      onSuccess((persistanceActor ?? Roles.Read.All).mapTo[Seq[Role]]) {
        roles =>
          // implicite value for marshalling
          val roleMap = roles.map(r => (r.name, r)).toMap
          implicit val roleMapper: RoleMapper = RoleMapper.createRoleMapper(roleMap)

          entity(as[GraphHeader]) {
            json =>
              op(json)
          }
      }
    }
  }
}
