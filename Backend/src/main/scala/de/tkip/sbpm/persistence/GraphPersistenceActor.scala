package de.tkip.sbpm.persistence

import scala.slick.lifted
import de.tkip.sbpm.model._
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
case class SaveGraph(graph: Graph) extends GraphAction
// delete graph with id from db (nothing is returned)
case class DeleteGraph(id: Int) extends GraphAction

/**
 * Handles all database operations for table "process_graphs".
 */
private[persistence] class GraphPersistenceActor extends Actor with DatabaseAccess {

  import driver.simple._
  import DBType._

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
      case GetGraph(None) => answer { Graphs.sortBy(_.id).list }
      // get graph with given id as Option (None if not found)
      case GetGraph(id) =>
        answer { Graphs.where(_.id === id).firstOption }
      // create new graph
      case SaveGraph(g @ Graph(None, _, _, _)) =>
        answer { Some(Graphs.autoInc.insert(g)) }
      // save existing graph
      case SaveGraph(g @ Graph(id, _, _, _)) => update(id, g)
      // delete graph with given id
      case DeleteGraph(id) =>
        answer { Graphs.where(_.id === id).delete(session) }
      // execute DDL to create "process_graphs" table
      case InitDatabase => answer { Graphs.ddl.create(session) }
    }
  }

  // update entity or throw exception if it does not exist
  def update(id: Option[Int], g: Graph)(implicit session: Session) = answer {
    val res = Graphs.where(_.id === id).update(g)
    if (res == 0)
      throw new EntityNotFoundException("Graph with id %d does not exist.", id.get)
    None
  }

}