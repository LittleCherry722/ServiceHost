package de.tkip.sbpm.persistence

import scala.slick.lifted

import akka.actor.Actor

/*
* Messages for querying database
* all message classes that inherit GraphAction
* are redirected to GraphPersistenceActor
*/
sealed abstract class GraphAction extends PersistenceAction
/* get entry (Option[model.Graph]) by id 
* or all entries (Seq[model.Graph]) by sending None as id
* None or empty Seq is returned if no entities where found
*/
case class GetGraph(id: Option[Int] = None) extends GraphAction
// save graph to db, if id is None a new graph is created and its id is returned
case class SaveGraph(id: Option[Int] = None, graph: String, date: java.sql.Timestamp, processId: Int) extends GraphAction
// delete graph with id from db (nothing is returned)
case class DeleteGraph(id: Int) extends GraphAction


/**
 * Handles all database operations for table "process_graphs".
 */
private[persistence] class GraphPersistenceActor extends Actor with DatabaseAccess {

  import driver.simple._
  import DBType._
  import de.tkip.sbpm.model._

  // represents the "process_graphs" table in the database
  object Graphs extends Table[Graph]("process_graphs") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def graph = column[String]("graph", O.DBType(blob))
    def date = column[java.sql.Timestamp]("date")
    def processId = column[Int]("processID")
    def * = id.? ~ graph ~ date ~ processId <> (Graph, Graph.unapply _)
    // auto increment method returning generated id
    def autoInc = * returning id
  }

  def receive = database.withSession { implicit session => // execute all db operations in a session
    {
      // get all graphs ordered by id
      case GetGraph(None) => sender ! Graphs.sortBy(_.id).list
      // get graph with given id as Option (None if not found)
      case GetGraph(id) => sender ! Graphs.where(_.id === id).firstOption
      // create new graph
      case SaveGraph(None, graph, date, processId) =>
        sender ! Graphs.autoInc.insert(Graph(None, graph, date, processId))
      // save existing graph
      case SaveGraph(id, graph, date, processId) =>
        Graphs.where(_.id === id).update(Graph(id, graph, date, processId))
      // delete graph with given id
      case DeleteGraph(id) => Graphs.where(_.id === id).delete(session)
      // execute DDL to create "process_graphs" table
      case InitDatabase() => Graphs.ddl.create(session)
    }
  }

}