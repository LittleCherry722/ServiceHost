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

import de.tkip.sbpm.instrumentation.InstrumentedActor
import query.Processes._
import mapping.ProcessMappings._
import de.tkip.sbpm.model._
import akka.actor.Props
import scala.slick.lifted
import akka.pattern._
import scala.concurrent._
import akka.actor.ActorLogging
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.persistence.query.Graphs
import akka.actor.PoisonPill
import de.tkip.sbpm.persistence.query.BaseQuery
import akka.actor.ActorRef
import scala.concurrent.duration._
import de.tkip.sbpm._
import java.util.UUID
import akka.event.Logging

private[persistence] class ProcessInspectActor extends InstrumentedActor with ActorLogging {
  import de.tkip.sbpm.model._
  import akka.pattern.ask
  import akka.util.Timeout
  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global
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
  with DatabaseAccess with schema.ProcessesSchema with schema.ProcessActiveGraphsSchema {
  // import current slick driver dynamically
  import driver.simple._

  private lazy val changeActor = ActorLocator.changeActor

  override def wrappedReceive = {
    // get all processes
    case Read.All => answerProcessed { implicit session: Session =>
      joinQuery().list
    }(_.map(convert))
    // get process with given id
    case Read.ById(id) => answerOptionProcessed { implicit session: Session =>
      joinQuery(processes.filter(_.id === id)).firstOption
    }(convert)
    // get process with given name
    case Read.ByName(name) => answerOptionProcessed { implicit session: Session =>
      joinQuery(processes.filter(_.name === name)).firstOption
    }(convert)
    // create or update processes
    case Save.Entity(ps @ _*) => answer { implicit session =>
      // process all entities
      ps.map {
        // insert if id is None
        case p @ Process(None, _, _, _, _, _, _) => Some(insert(p))
        // update otherwise
        case p @ Process(id, _, _, _, _, _, _)   => update(id, p)
      } match {
        // only one process was given, return it's id
        case ids if (ids.size == 1) => ids.head
        // more processes were given return all ids
        case ids                    => ids
      }
    }
    // create new process with a corresponding graph
    case Save.WithGraph(p: Process, g) => answer { implicit session =>
      saveProcessWithGraph(p, g)
    }
    // delete process with given id
    case Delete.ById(id) => {
      answer { session =>
        processes.filter(_.id === id).delete(session)
      }
      println("!!!!!!!!!!! process deleted: " + id)
      changeActor ! ProcessDelete(id, new java.util.Date())
    }
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
  private def insert(p: Process)(implicit session: Session) = {
    // extract process and active graph id from domain model
    val entities = convert(p)
    val id = (processes returning processes.map(_.id)) += entities._1
    log.debug("Save Process: " + p)
    // create active graph entry if it's id is defined
    if (entities._2.isDefined)
      processActiveGraphs.insert(mapping.ProcessActiveGraph(id, entities._2.get))
    id
  }

  /**
   *  Insert new row for graphs to mark delete time
   */
  private def insertForDelete(id: Int)(implicit session: Session) = {
    val date = new java.util.Date()
    val time = new java.sql.Timestamp(date.getTime())
    val gid = (graphs returning graphs.map(_.id)) += mapping.Graph(Some(16), id, time)
    println("gid is: " + gid + " and id is: " + id)
    println("time is: " + time.getTime())
    val res = graphs += mapping.Graph(Some(gid), id, time)
  }

  /**
   * Update entity or throw exception if it does not exist.
   */
  private def update(id: Option[Int], p: Process)(implicit session: Session) = {
    // extract process and active graph id from domain model
    val entities = convert(p)
    val res = processes.filter(_.id === id).update(entities._1)

    if (res == 0)
      throw new EntityNotFoundException("Process with id %d does not exist.", id.get)

    log.debug("Update Process: " + p)

    // update active graph entitiy for current process
    updateActiveGraph(id, entities._2)

    // update always returns None
    None
  }

  /**
   * Update currently active graph for a process.
   * Deletes all existing mappings for the process and
   * inserts a new one if graphId is not None.
   */
  private def updateActiveGraph(processId: Option[Int], graphId: Option[Int])(implicit session: Session) = {
    processActiveGraphs.filter(_.processId === processId).delete

    if (graphId.isDefined)
      processActiveGraphs += mapping.ProcessActiveGraph(processId.get, graphId.get)
  }

  /**
   * Saves a process with the corresponding graph to the database.
   * Each save operation produces a new graph instance (for maintaining old versions).
   */
  private def saveProcessWithGraph(p: Process, g: Graph)(implicit session: Session) = {

    log.debug("Update Process with Graph: " + p)

    // set graph id to none -> insert new on every save to maintain old versions
    var graph = g.copy(id = None)
    // set current active graph to None (we don't know graph id yet)
    var process = p.copy(activeGraphId = None)
    var resultId = process.id

    // if id not defined -> save new process
    if (!resultId.isDefined) {
      resultId = Some(insert(process))
      // inject id into process
      process = process.copy(id = resultId)

      changeActor ! ProcessChange(process, "insert", new java.util.Date())
    } else {
      // update the process
      val res = processes.filter(_.id === process.id).update(convert(process)._1)
      if (res == 0)
        throw new EntityNotFoundException("Process with id %d does not exist.", process.id.get)
      // check if process exists if we should update it
      //      if (!Processes.where(_.id === process.id).firstOption.isDefined)
      //        throw new EntityNotFoundException("Process with id %d does not exist.", process.id.get)
      // result on update is always None
      resultId = None
      changeActor ! ProcessChange(process, "update", new java.util.Date())
    }
    // set process id in graph
    graph = graph.copy(processId = process.id)
    // save graph to db
    val gId = save(graph)

    // update process' active graph to new id
    updateActiveGraph(process.id, gId)

    (resultId, gId)
  }
}
