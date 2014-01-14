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
import scala.slick.lifted
import de.tkip.sbpm.model._
import akka.actor.Actor

/**
 * Handles all database operations for tables "graphs" and "graph_*".
 */
private[persistence] class GraphPersistenceActor extends Actor
  with DatabaseAccess
  with schema.GraphConversationsSchema
  with schema.GraphMessagesSchema
  with schema.GraphEdgesSchema
  with schema.GraphNodesSchema
  with schema.GraphVarMansSchema
  with schema.GraphRoutingsSchema {

  // import current slick driver dynamically
  import driver.simple._

  def receive = {
    // get all graphs
    case Read.All => answerProcessed { implicit session: Session =>
      // load graph, all it sub entities and the roles from db
      (Query(Graphs).list.map { g =>
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
      Query(Graphs).where(_.id === id).firstOption match {
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
      Graphs.where(_.id === id).delete
    }
  }

  /**
   * Load all roles that are needed in the graph. Reads the roles out of the
   * graph subjects or all roles if no graphId is given. 
   */
  private def retrieveRoles(graphId: Option[Int] = None)(implicit session: Session) = (for {
    s <- GraphSubjects if (ConstColumn(false) === graphId.isDefined || s.graphId === graphId.get)
    r <- s.role
  } yield r).list.distinct

  // update entity or throw exception if it does not exist
  protected def save(g: Graph)(implicit session: Session) = {
    // convert domain model graph to db entities
    val (graph, conversations, messages, routings, subjects, variables, macros, nodes, varMans, edges) =
      convert(g) match {
      // only graph was converted, because it's a new graph (no id exits)
        case Left(model: mapping.Graph) =>
          // insert graph to get it's id
          val id = Graphs.autoInc.insert(model)
          // then convert model again with known graph id
          convert(g.copy(Some(id))).right.get
        case Right(models) =>
          // id of graph was given -> update existing
          // first check if graph really exists
          val q = Graphs.where(_.id === models._1.id.get)
          if (!q.firstOption.isDefined)
            throw new EntityNotFoundException("Graph with id %d does not exist.", models._1.id.get)
          // update graph
          q.update(models._1)
          models
      }

    // delete all dependent entities of graph and
    // insert them with new values again
    deleteSubEntities(graph.id.get)

    GraphConversations.insertAll(conversations: _*)

    GraphMessages.insertAll(messages: _*)

    GraphRoutings.insertAll(routings: _*)

    GraphSubjects.insertAll(subjects: _*)

    GraphVariables.insertAll(variables: _*)

    GraphMacros.insertAll(macros: _*)

    GraphNodes.insertAll(nodes: _*)

    GraphVarMans.insertAll(varMans: _*)

    GraphEdges.insertAll(edges: _*)

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
    GraphEdges.where(_.graphId === id).delete
    GraphNodes.where(_.graphId === id).delete
    GraphVarMans.where(_.graphId === id).delete
    GraphMacros.where(_.graphId === id).delete
    GraphVariables.where(_.graphId === id).delete
    GraphSubjects.where(_.graphId === id).delete
    GraphRoutings.where(_.graphId === id).delete
    GraphMessages.where(_.graphId === id).delete
    GraphConversations.where(_.graphId === id).delete
  }

  /**
   * Load all dependent entities of a graph with given id.
   */
  def retrieveSubEntities(id: Int)(implicit session: Session) = (
    Query(GraphConversations).where(_.graphId === id).list,
    Query(GraphMessages).where(_.graphId === id).list,
    Query(GraphRoutings).where(_.graphId === id).list,
    Query(GraphSubjects).where(_.graphId === id).list,
    Query(GraphVariables).where(_.graphId === id).list,
    Query(GraphMacros).where(_.graphId === id).list,
    Query(GraphNodes).where(_.graphId === id).list,
    Query(GraphVarMans).where(_.graphId === id).list,
    Query(GraphEdges).where(_.graphId === id).list)
}