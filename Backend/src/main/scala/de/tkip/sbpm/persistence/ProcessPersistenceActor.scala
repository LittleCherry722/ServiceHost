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

import akka.actor.Actor
import query.Processes._
import mapping.ProcessMappings._
import de.tkip.sbpm.model._
import akka.actor.Props
import scala.slick.lifted
import akka.pattern._
import scala.concurrent._

/**
 * Handles database connection for "process" entities using slick.
 */
private[persistence] class ProcessPersistenceActor extends GraphPersistenceActor
  with DatabaseAccess with schema.ProcessesSchema with schema.ProcessActiveGraphsSchema {
  // import current slick driver dynamically
  import driver.simple._

  override def receive = {
    // get all processes
    case Read.All => answerProcessed { implicit session: Session =>
      joinQuery().list
    }(_.map(convert))
    // get process with given id
    case Read.ById(id) => answerOptionProcessed { implicit session: Session =>
      joinQuery(Query(Processes).where(_.id === id)).firstOption
    }(convert)
    // get process with given name
    case Read.ByName(name) => answerOptionProcessed { implicit session: Session =>
      joinQuery(Query(Processes).where(_.name === name)).firstOption
    }(convert)
    // create or update processes
    case Save.Entity(ps @ _*) => answer { implicit session =>
      // process all entities
      ps.map {
        // insert if id is None
        case p @ Process(None, _, _, _) => Some(insert(p))
        // update otherwise
        case p @ Process(id, _, _, _)   => update(id, p)
      } match {
         // only one process was given, return it's id
        case ids if (ids.size == 1) => ids.head
        // more processes were given return all ids
        case ids                    => ids
      }
    }
    // create new process with a corresponding graph
    case Save.WithGraph(p: Process, g) =>  answer { implicit session =>
      saveProcessWithGraph(p, g)
    }
    // delete process with given id
    case Delete.ById(id) => answer { session =>
      Processes.where(_.id === id).delete(session)
    }
  }

  /**
   * Return a query for joining process table with process active graph table.
   * A base query for the process table can be given (default all entities).
   */
  private def joinQuery(baseQuery: driver.simple.Query[Processes.type, mapping.Process] = Query(Processes)) = for {
    // left join because active graph may not exist
    (p, pag) <- baseQuery.leftJoin(Query(ProcessActiveGraphs)).on(_.id === _.processId)
  } yield (p, pag.graphId.?)

  /**
   * Insert entity and return it's id.
   */
  private def insert(p: Process)(implicit session: Session) = {
    // extract process and active graph id from domain model
    val entities = convert(p)
    val id = Processes.autoInc.insert(entities._1)
    // create active graph entry if it's id is defined
    if (entities._2.isDefined)
      ProcessActiveGraphs.insert(mapping.ProcessActiveGraph(id, entities._2.get))
    id
  }

  /** 
   * Update entity or throw exception if it does not exist.
   */
  private def update(id: Option[Int], p: Process)(implicit session: Session) = {
    // extract process and active graph id from domain model
    val entities = convert(p)
    val res = Processes.where(_.id === id).update(entities._1)

    if (res == 0)
      throw new EntityNotFoundException("Process with id %d does not exist.", id.get)
    
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
    ProcessActiveGraphs.where(_.processId === processId).delete

    if (graphId.isDefined)
      ProcessActiveGraphs.insert(mapping.ProcessActiveGraph(processId.get, graphId.get))
  }

  /**
   * Saves a process with the corresponding graph to the database.
   * Each save operation produces a new graph instance (for maintaining old versions).
   */
  private def saveProcessWithGraph(p: Process, g: Graph)(implicit session: Session) ={
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
    } else {
      // check if process exists if we should update it
      if (!Processes.where(_.id === process.id).firstOption.isDefined)
        throw new EntityNotFoundException("Process with id %d does not exist.", process.id.get)
      // result on update is always None
      resultId = None
    }
    // set process id in graph
    graph = graph.copy(processId = process.id)
    // save graph to db
    val gId = save(graph)

    // update process' active graph to new id
    updateActiveGraph(resultId, gId)

    (resultId, gId)
  }
}

