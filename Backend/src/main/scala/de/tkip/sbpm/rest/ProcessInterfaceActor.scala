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
import de.tkip.sbpm.repository.RepositoryPersistenceActor.{InterfaceSaveResult, DeleteInterface, SaveInterface}
import de.tkip.sbpm.rest.GraphJsonProtocol._
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
    val processFuture = (persistenceActor ?? Processes.Read.ById(id)).mapTo[Option[Process]]
    processFuture.onSuccess {
      case processResult =>
        if (processResult.isDefined) {
          val interfaceId = processResult.get.interfaceId
          if (interfaceId.isDefined) {
            repositoryPersistenceActor !! DeleteInterface(interfaceId.get)
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
          name = process.name,
          interfaceId = process.interfaceId,
          publishInterface = process.publishInterface,
          verificationErrors = Seq.empty,  // verification errors
          graph = graphResult,
          incomingSubjectMap = process.incomingSubjectMap,
          outgoingSubjectMap = process.outgoingSubjectMap,
          implementationIds = process.implementationIds,
          isCase = process.isCase,
          id = process.id)
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
    val process = Process(id, json.interfaceId, json.verificationErrors, json.publishInterface, json.name, json.incomingSubjectMap, json.outgoingSubjectMap, json.implementationIds, json.isCase)
    val future = (persistanceActor ?? Processes.Save(process)).mapTo[Option[Int]]
    val result = future.map(resultId => JsObject("id" -> resultId.getOrElse(id.getOrElse(-1)).toJson))
    complete(result)
  }

  /**
   * Saves the given process with its graph.
   */
  private def saveWithGraph(id: Option[Int], gHeader: GraphHeader): Route = {
    val graph = gHeader.graph.get.copy(date = new java.sql.Timestamp(System.currentTimeMillis()), id = None, processId = None)
    val verificationErrors = verifyGraph(graph).left.getOrElse(Seq.empty)
    println(s"SAVING WITH GRAPH: ${(gHeader.publishInterface, gHeader.interfaceId)}")
    val interfaceIdFuture: Future[Either[Option[Int], Option[InterfaceSaveResult]]] = (gHeader.publishInterface, gHeader.interfaceId) match {
      case (true, Some(iId)) => Future(Left(Some(iId)))
      case (true, None) =>
        val respFuture = (repositoryPersistenceActor ?? SaveInterface(gHeader)).mapTo[Either[Seq[String], Option[InterfaceSaveResult]]]
        respFuture.map {
          case Left(_) => Right(None)
          case Right(r) => Right(r)
        }
      case (false, Some(iId)) =>
        repositoryPersistenceActor !! DeleteInterface(iId)
        Future(Left(None))
      case (false, None) => Future(Left(None))
    }
    val resultFuture = for {
      interfaceSaveResult <- interfaceIdFuture
      (publishFlag, interfaceId, outSubjectMap, inSubjectMap) = interfaceSaveResult match {
        case Left(None) => (false, None, gHeader.outgoingSubjectMap, gHeader.incomingSubjectMap)
        case Right(None) => (true, None, gHeader.outgoingSubjectMap, gHeader.incomingSubjectMap)
        case Left(Some(iId)) => (true, Some(iId), gHeader.outgoingSubjectMap, gHeader.incomingSubjectMap)
        case Right(Some(isr)) =>
          val newOutSubjMap = isr.outgoingSubjectMap ++ gHeader.outgoingSubjectMap
          val newInSubjMap = isr.incomingSubjectMap ++ gHeader.incomingSubjectMap
          (true, Some(isr.id), newOutSubjMap, newInSubjMap)
      }
      process = Process(id, interfaceId, verificationErrors, publishFlag, gHeader.name, outSubjectMap, inSubjectMap, gHeader.implementationIds, gHeader.isCase)
      (processId, graphId) <- (persistanceActor ?? Processes.Save.WithGraph(process, graph)).mapTo[(Int, Option[Int])]
      result = JsObject("id" -> processId.toJson, "graphId" -> graphId.toJson)
      roles <- (persistanceActor ?? Roles.Read.All).mapTo[Seq[Role]]
      savedProcess = GraphHeader(
        name = process.name,
        interfaceId = process.interfaceId,
        verificationErrors = verificationErrors,
        publishInterface = process.publishInterface,
        graph = Some(graph.copy(id = graphId, processId = Some(processId))),
        incomingSubjectMap = process.incomingSubjectMap,
        outgoingSubjectMap = process.outgoingSubjectMap,
        implementationIds = process.implementationIds,
        isCase = process.isCase,
        id = Some(processId))
    } yield (savedProcess, roles)
    onSuccess(resultFuture) { res =>
      val (graphHeader, roles) = res
      // implicit value for marshalling
      val roleMap = roles.map(r => (r.name, r)).toMap
      implicit val roleMapper: RoleMapper = RoleMapper.createRoleMapper(roleMap)
      complete(graphHeader)
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
