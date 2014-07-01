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

import query.Graphs._
import mapping.GraphMappings._
import scala.slick.lifted.ConstColumn
import de.tkip.sbpm.model._
import de.tkip.sbpm.instrumentation.InstrumentedActor

/**
 * Handles all database operations for tables "graphs" and "graph_*".
 */
private[persistence] class GraphPersistenceActor extends InstrumentedActor
  with DatabaseAccess
  with schema.GraphConversationsSchema
  with schema.GraphMessagesSchema
  with schema.GraphEdgesSchema
  with schema.GraphNodesSchema
  with schema.GraphVarMansSchema
  with schema.GraphRoutingsSchema {

  // import current slick driver dynamically
  import driver.simple._

  def wrappedReceive = {
    // get all graphs
    case Read.All => answerProcessed { implicit session: Session =>
      // load graph, all it sub entities and the roles from db
      (graphs.list.map { g =>
        (g, retrieveSubEntities(g.id.get))
      },
        retrieveRoles())
    } { res =>
      // convert graph entities to domain model in post process function
      res._1.map { t =>
        convert(t._1, t._2, res._2)
      }
    }
    // get graph with given id as Option (None if not found)
    case Read.ById(id) => answerOptionProcessed { implicit session: Session =>
      log.debug("Read Graph: " + id)
      // load graph by id , all it sub entities and the roles from db
      graphs.filter(_.id === id).firstOption match {
        // return None if graph not found
      	case None    => None
        case Some(g) => Some(g, retrieveSubEntities(g.id.get), retrieveRoles(g.id))
      }
    } { m =>
      // convert graph entities to domain model in post process function
      convert(m._1, m._2, m._3)
    }
    // save given graphs to db
    case Save.Entity(gs @ _*) => answer { implicit session =>
      log.debug("Save Graph for: " + (gs map (_.processId) mkString (", ")))
      // save all graphs
      gs.map(save) match {
        // only one graph was given, return it's id
        case ids if (ids.size == 1) => ids.head
        // more graphs were given return all ids
        case ids                    => ids
      }
    }
    // delete graph with given id
    case Delete.ById(id) => answer { implicit session =>
      // first delete all dependent entities of the graph
      deleteSubEntities(id)
      graphs.filter(_.id === id).delete
    }
  }

  /**
   * Load all roles that are needed in the graph. Reads the roles out of the
   * graph subjects or all roles if no graphId is given.
   */
  private def retrieveRoles(graphId: Option[Int] = None)(implicit session: Session) = {
    val gs = graphId match {
      case None => graphSubjects.withFilter(_ => true)
      case Some(id) => graphSubjects.filter(_.graphId === id)
    }
    val roles = for {
      subj <- gs
      role <- subj.role
    } yield role
    roles.list.distinct
  }

  // update entity or throw exception if it does not exist
  protected def save(g: Graph)(implicit session: Session) = {
    // convert domain model graph to db entities
    val (graph, conversations, messages, routings, subjects, variables, macros, nodes, varMans, edges) =
      convert(g) match {
      // only graph was converted, because it's a new graph (no id exits)
        case Left(model: mapping.Graph) =>
          // insert graph to get it's id
          val id = graphs.autoInc.insert(model)
          // then convert model again with known graph id
          convert(g.copy(Some(id))).right.get
        case Right(models) =>
          // id of graph was given -> update existing
          // first check if graph really exists
          val q = graphs.filter(_.id === models._1.id.get)
          if (!q.firstOption.isDefined)
            throw new EntityNotFoundException("Graph with id %d does not exist.", models._1.id.get)
          // update graph
          q.update(models._1)
          models
      }

    // delete all dependent entities of graph and
    // insert them with new values again
    deleteSubEntities(graph.id.get)

    graphConversations.insertAll(conversations: _*)
    graphMessages.insertAll(messages: _*)
    graphRoutings.insertAll(routings: _*)
    graphSubjects.insertAll(subjects: _*)
    graphVariables.insertAll(variables: _*)
    graphMacros.insertAll(macros: _*)
    graphNodes.insertAll(nodes: _*)
    graphVarMans.insertAll(varMans: _*)
    graphEdges.insertAll(edges: _*)

    // only return id if graph was inserted
    // on update return None
    g.id match {
      case None => graph.id
      case _    => None
    }
  }

  /**
   * Delete all dependent entities of a graph with given id.
   */
  private def deleteSubEntities(id: Int)(implicit session: Session) = {
    graphEdges.filter(_.graphId === id).delete
    graphNodes.filter(_.graphId === id).delete
    graphVarMans.filter(_.graphId === id).delete
    graphMacros.filter(_.graphId === id).delete
    graphVariables.filter(_.graphId === id).delete
    graphSubjects.filter(_.graphId === id).delete
    graphRoutings.filter(_.graphId === id).delete
    graphMessages.filter(_.graphId === id).delete
    graphConversations.filter(_.graphId === id).delete
  }

  /**
   * Load all dependent entities of a graph with given id.
   */
  def retrieveSubEntities(id: Int)(implicit session: Session) = (
    graphConversations.filter(_.graphId === id).list,
    graphMessages.filter(_.graphId === id).list,
    graphRoutings.filter(_.graphId === id).list,
    graphSubjects.filter(_.graphId === id).list,
    graphVariables.filter(_.graphId === id).list,
    graphMacros.filter(_.graphId === id).list,
    graphNodes.filter(_.graphId === id).list,
    graphVarMans.filter(_.graphId === id).list,
    graphEdges.filter(_.graphId === id).list)
}
