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

package de.tkip.sbpm.persistence

import java.util.UUID

import akka.actor.{ActorLogging, ActorRef, PoisonPill, Props}
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.instrumentation.InstrumentedActor
import de.tkip.sbpm.model._
import de.tkip.sbpm.persistence.mapping.ProcessMappings._
import de.tkip.sbpm.persistence.mapping.{ProcessSubjectMapping, VerificationError, ProcessMessageMapping}
import de.tkip.sbpm.persistence.query.{BaseQuery, Graphs}
import de.tkip.sbpm.persistence.query.Processes._
import de.tkip.sbpm.persistence.schema.VerificationErrorsSchema
import de.tkip.sbpm.persistence.schema.ProcessSubjectMappingSchema
import de.tkip.sbpm.persistence.schema.ProcessMessageMappingSchema

import scala.concurrent.duration._

private[persistence] class ProcessInspectActor extends InstrumentedActor with ActorLogging {
  import akka.util.Timeout
  import de.tkip.sbpm.model._
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future

  implicit val timeout = Timeout(30 seconds)
  def wrappedReceive = {
    case q @ Save.Entity(ps @ _*) => {
      log.debug("Start checking: " + q)
      // Check for alle process graphs, if they are startAble
      val newProcesses =
        ps map { p =>
          if (p.activeGraphId.isDefined) {
            for {
              g <- (ActorLocator.persistenceActor ?? Graphs.Read.ById(p.activeGraphId.get)).mapTo[Option[Graph]]
              newProcess = checkAndExchangeStartAble(p, g.get)
            } yield newProcess
          } else Future(p)
        }
      // Seq[Future] -> Future[Seq]
      val newQuery = Future.sequence(newProcesses)

      // forward the updated Processes to the ProcessPersistenceActor
      val from = sender
      newQuery onSuccess {
        case s =>
          forwardToPersistence(Save.Entity(s: _*), from)
      }
      newQuery onFailure {
        case s =>
          log.error("Graph checking failed, reason: " + s)
      }
    }
    case q @ Save.WithGraph(p, g) => {
      // check the graph and update the process
      // forward the updated Processes to the ProcessPersistenceActor
      val from = sender
      val newQuery =
        Future(Save.WithGraph(checkAndExchangeStartAble(p, g), g))
      newQuery onSuccess {
        case q =>
          forwardToPersistence(q, from)
      }
      newQuery onFailure {
        case s =>
          log.error("Graph checking failed, reason: " + s)
      }
    }
    case q: Query => forwardToPersistence(q, sender)
  }

  private def checkAndExchangeStartAble(p: Process, g: Graph): Process = {
    log.debug("Checking: " + p)
    exchangeStartAble(p, isStartAbleProcessGraph(g))
  }
  private def exchangeStartAble(p: Process, startAble: Boolean): Process =
    //    Process(p.id, p.name, p.isCase, Some(startAble), p.activeGraphId)
    p.copy(startAble = Some(startAble))

  private def isStartAbleProcessGraph(graph: Graph): Boolean = {
    // TODO correct check
    val correct =
      graph.subjects.exists(s => s._2.isStartSubject.getOrElse(false))
    log.debug(s"Result for graph ${graph.id} is $correct")
    correct
  }

  /**
   * Forwards a query to the specified Actor.
   * The actor is automatically stopped after processing the
   * query using PoisonPill message.
   */
  private def forwardToPersistence(query: BaseQuery, from: ActorRef) = {
    val actor = context.actorOf(Props[ProcessPersistenceActor], "ProcessPersistenceActor____" + UUID.randomUUID().toString())
    actor.tell(query, from)
    actor ! PoisonPill
  }
}

/**
 * Handles database connection for "process" entities using slick.
 */
private class ProcessPersistenceActor extends GraphPersistenceActor
  with DatabaseAccess with schema.ProcessesSchema with schema.ProcessActiveGraphsSchema with VerificationErrorsSchema
  with ProcessSubjectMappingSchema with ProcessMessageMappingSchema {
  // import current slick driver dynamically
  import driver.simple._

  private lazy val changeActor = ActorLocator.changeActor

  override def wrappedReceive = {
    // get all processes
    case Read.All => answerProcessed { implicit session: Session =>
      joinQuery().list.map{ case (p, gId) =>
        val errors = verificationErrors.filter(_.processId === p.id).list
        val subjectMappingsList = processSubjectMappings.filter(_.processId === p.id).list
        val subjectMappings = subjectMappingsList.groupBy(_.viewId).toMap.mapValues(_.map(sm => (sm.from, sm.to)).toMap)
        val messageMappingsList = processMessageMappings.filter(_.processId === p.id).list
        val messageMappings = messageMappingsList.groupBy(_.viewId).toMap.mapValues(_.map(mm => (mm.from, mm.to)).toMap)
        (errors, p, gId, subjectMappings, messageMappings)
      }
    }(_.map(convert))

    // Get message mappings for process id
    case Read.MessageMappings(processId) => answerProcessed { implicit session: Session =>
      val messageMappingsList = processMessageMappings.filter(_.processId === processId).list
      val messageMappings = messageMappingsList.groupBy(_.viewId).mapValues(_.map(mm => (mm.from, mm.to)).toMap)
      messageMappings
    }(identity)

    // get process with given id
    case Read.ById(id) => answerOptionProcessed { implicit session: Session =>
      val errors = verificationErrors.filter(_.processId === id).list
      val subjectMappingsList = processSubjectMappings.filter(_.processId === id).list
      val subjectMappings = subjectMappingsList.groupBy(_.viewId).toMap.mapValues(_.map(sm => (sm.from, sm.to)).toMap)
      val messageMappingsList = processMessageMappings.filter(_.processId === id).list
      val messageMappings = messageMappingsList.groupBy(_.viewId).toMap.mapValues(_.map(mm => (mm.from, mm.to)).toMap)
      val processOption = joinQuery(processes.filter(_.id === id)).firstOption
      val justProcess = processes.filter(_.id === id).firstOption
      log.debug("----------READING----------")
      log.debug(s"errors: $errors")
      log.debug(s"processOption: $processOption")
      log.debug(s"justProcess: $justProcess")
      log.debug("----------/READING----------")
      for {
        (p, pId) <- processOption
      } yield (errors, p, pId, subjectMappings, messageMappings)
    }(convert)

    // get process with given name
    case Read.ByName(name) => answerOptionProcessed { implicit session: Session =>
      joinQuery(processes.filter(_.name === name)).firstOption.map { case (p, gId) =>
        val errors = verificationErrors.filter(_.processId === p.id).list
        val subjectMappingsList = processSubjectMappings.filter(_.processId === p.id).list
        val subjectMappings = subjectMappingsList.groupBy(_.viewId).toMap.mapValues(_.map(sm => (sm.from, sm.to)).toMap)
        val messageMappingsList = processMessageMappings.filter(_.processId === p.id).list
        val messageMappings = messageMappingsList.groupBy(_.viewId).toMap.mapValues(_.map(mm => (mm.from, mm.to)).toMap)
        (errors, p, gId, subjectMappings, messageMappings)
      }
    }(convert)
    // create or update processes
    case Save.Entity(ps @ _*) => answer { implicit session =>
      // process all entities
      ps.map {
        // insert if id is None
        case p @ Process(None, _, _, _, _, _, _, _, _, _, _) => Some(insert(p))
        // update otherwise
        case p @ Process(Some(id), _, _, _, _, _, _, _, _, _, _)   => update(id, p)
      } match {
        // only one process was given, return it's id
//        case ids if (ids.size == 1) => ids.head
        // more processes were given return all ids
        case ids => ids
      }
    }
    // create new process with a corresponding graph
    case Save.WithGraph(p: Process, g) => answer { implicit session =>
      saveProcessWithGraph(p, g)
    }
    // delete process with given id
    case Delete.ById(id) =>
      answer { session =>
        verificationErrors.filter(_.processId === id).delete(session)
        processSubjectMappings.filter(_.processId === id).delete(session)
        processMessageMappings.filter(_.processId === id).delete(session)
        processes.filter(_.id === id).delete(session)
      }
      println("!!!!!!!!!!! process deleted: " + id)
      changeActor ! ProcessDelete(id, new java.util.Date())
  }

  /**
   * Return a query for joining process table with process active graph table.
   * A base query for the process table can be given (default all entities).
   */
  private def joinQuery(baseQuery: Query[Processes, mapping.Process, Seq] = processes) = for {
    // left join because active graph may not exist
    (p, pag) <- baseQuery.leftJoin(processActiveGraphs).on(_.id === _.processId)
  } yield (p, pag.graphId.?)

  /**
   * Insert entity and return it's id.
   */
  private def insert(p: Process)(implicit session: Session): Int = {
    // extract process and active graph id from domain model
    val (process, gId, mappingsGen) = convert(p)
    val pId = (processes returning processes.map(_.id)) += process
    val fn = saveMappings(pId, mappingsGen)

    log.debug("Save Process: " + p)
    // create active graph entry if it's id is defined
    updateActiveGraph(pId, gId)
    pId
  }

  private def saveMappings(pId: Int, mapsGen: ((Int) => (Seq[ProcessSubjectMapping], Seq[ProcessMessageMapping], Seq[VerificationError])))
                          (implicit session: Session) = {
    val (sMaps, mMaps, vErrors) = mapsGen(pId)
    processSubjectMappings.filter(_.processId === pId).delete(session)
    processMessageMappings.filter(_.processId === pId).delete(session)
    verificationErrors.filter(_.processId === pId).delete(session)
    processSubjectMappings.insertAll(sMaps: _*)(session)
    processMessageMappings.insertAll(mMaps: _*)(session)
    verificationErrors.insertAll(vErrors: _*)(session)
  }

  /**
   * Update entity or throw exception if it does not exist.
   */
  private def update(pId: Int, p: Process)(implicit session: Session): Unit = {
    // extract process and active graph id from domain model
    val (process, graphId, mappingsGen) = convert(p)
    val res = processes.filter(_.id === pId).update(process)
    saveMappings(pId, mappingsGen)

    if (res == 0) {
      throw new EntityNotFoundException("Process with id %d does not exist.", pId)
    }

    log.debug("Update Process: " + p)

    // update active graph entitiy for current process
    updateActiveGraph(pId, graphId)
  }

  /**
   * Update currently active graph for a process.
   * Deletes all existing mappings for the process and
   * inserts a new one if graphId is not None.
   */
  private def updateActiveGraph(processId: Int, graphId: Option[Int])(implicit session: Session) = {
    processActiveGraphs.filter(_.processId === processId).delete
    graphId.map(gId => processActiveGraphs += mapping.ProcessActiveGraph(processId, gId))
  }

  /**
   * Saves a process with the corresponding graph to the database.
   * Each save operation produces a new graph instance (for maintaining old versions).
   */
  private def saveProcessWithGraph(p: Process, g: Graph)(implicit session: Session) = {
    log.debug("Update Process with Graph: " + p)

    // set graph id to none -> insert new on every save to maintain old versions
    val graph = g.copy(id = None)
    // set current active graph to None (we don't know graph id yet)
    var process = p.copy(activeGraphId = None)

    // if id not defined -> save new process
    val newProcessId = process.id match {
      case None =>
        val pId = insert(process)
        // inject id into process
        process = process.copy(id = Some(pId))
        changeActor ! ProcessChange(process, "insert", new java.util.Date())
        pId
      case Some(pId) =>
        // update the process
        update(pId, process)
        changeActor ! ProcessChange(process, "update", new java.util.Date())
        pId
    }
    // set process id in graph and save graph to db
    val gId = save(graph.copy(processId = process.id))

    // update process' active graph to new id
    val graphId = updateActiveGraph(newProcessId, gId)
    (newProcessId, graphId)
  }
}
