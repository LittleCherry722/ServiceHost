package de.tkip.sbpm.persistence

import scala.slick.lifted
import de.tkip.sbpm.model._
import akka.actor.Actor

/**
 * Handles all database operations for table "process_graphs".
 */
private[persistence] class GraphPersistenceActor extends Actor
  with DatabaseAccess
  with schema.GraphChannelsSchema
  with schema.GraphMessagesSchema
  with schema.GraphEdgesSchema
  with schema.GraphNodesSchema
  with schema.GraphRoutingsSchema {
  import driver.simple._
  import query.Graphs._
  import mapping.GraphMappings._

  def receive = {
    // get all graphs ordered by id
    case Read.All => answerProcessed { implicit session: Session =>
      (Query(Graphs).list.map { g =>
        (g, retrieveSubEntities(g.id.get))
      },
        retrieveRoles())
    } { res =>
      res._1.map { t =>
        convert(t._1, t._2, res._2)
      }
    }
    // get graph with given id as Option (None if not found)
    case Read.ById(id) => answerOptionProcessed { implicit session: Session =>
      Query(Graphs).where(_.id === id).firstOption match {
        case None => None
        case Some(g) => Some(g, retrieveSubEntities(g.id.get), retrieveRoles(g.id))
      }
    } { m =>
      convert(m._1, m._2, m._3)
    }
    // save existing graph
    case Save.Entity(gs @ _*) => answer { implicit session =>
      gs.map(save)
    }
    // delete graph with given id
    case Delete.ById(id) => answer { implicit session =>
      deleteSubEntities(id)
      Graphs.where(_.id === id).delete
    }
  }

  def retrieveRoles(graphId: Option[Int] = None)(implicit session: Session) = (for {
    s <- GraphSubjects if (ConstColumn(false) === graphId.isDefined || s.graphId === graphId.get)
    r <- s.role
  } yield r).list.distinct

  // update entity or throw exception if it does not exist
  def save(g: Graph)(implicit session: Session) = {
    val (graph, channels, messages, routings, subjects, variables, macros, nodes, edges) =
      convert(g) match {
        case Left(model: mapping.Graph) =>
          val id = Graphs.autoInc.insert(model)
          convert(g.copy(Some(id))).right.get
        case Right(models) =>
          val q = Graphs.where(_.id === models._1.id.get)
          if (!q.firstOption.isDefined)
            throw new EntityNotFoundException("Graph with id %d does not exist.", models._1.id.get)
          q.update(models._1)
          models
      }

    deleteSubEntities(graph.id.get)

    GraphChannels.insertAll(channels: _*)

    GraphMessages.insertAll(messages: _*)

    GraphRoutings.insertAll(routings: _*)

    GraphSubjects.insertAll(subjects: _*)

    GraphVariables.insertAll(variables: _*)

    GraphMacros.insertAll(macros: _*)

    GraphNodes.insertAll(nodes: _*)

    GraphEdges.insertAll(edges: _*)

    g.id match {
      case None => graph.id
      case _ => None
    }
  }

  def deleteSubEntities(id: Int)(implicit session: Session) = {
    GraphEdges.where(_.graphId === id).delete
    GraphNodes.where(_.graphId === id).delete
    GraphMacros.where(_.graphId === id).delete
    GraphVariables.where(_.graphId === id).delete
    GraphSubjects.where(_.graphId === id).delete
    GraphRoutings.where(_.graphId === id).delete
    GraphMessages.where(_.graphId === id).delete
    GraphChannels.where(_.graphId === id).delete
  }

  def retrieveSubEntities(id: Int)(implicit session: Session) = (
    Query(GraphChannels).where(_.graphId === id).list,
    Query(GraphMessages).where(_.graphId === id).list,
    Query(GraphRoutings).where(_.graphId === id).list,
    Query(GraphSubjects).where(_.graphId === id).list,
    Query(GraphVariables).where(_.graphId === id).list,
    Query(GraphMacros).where(_.graphId === id).list,
    Query(GraphNodes).where(_.graphId === id).list,
    Query(GraphEdges).where(_.graphId === id).list)
}