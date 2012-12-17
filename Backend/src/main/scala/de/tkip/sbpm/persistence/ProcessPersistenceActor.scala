package de.tkip.sbpm.persistence

import akka.actor.Actor
import DatabaseConnection._
import collection._

/*
* Messages for querying database
* all message classes that inherit ProcessAction
* are redirected to ProcessPersistenceActor
*/
sealed abstract class ProcessAction
// get process by id or all process by sending None as id
case class GetProcess(id: Option[Int] = None) extends ProcessAction
// save process to db, if id is None a new process is created and its id is returned
case class SaveProcess(id: Option[Int] = None, name: String, graph: String, subjects: String) extends ProcessAction
// delete process with id from db
case class DeleteProcess(id: Int) extends ProcessAction

// result message for GetProcess
case class Process(id: Option[Int], name: String, startSubjects: String, graphId: Option[Int] = None)
// represents a graph in the db
case class Graph(id: Option[Int], definition: String, date: java.sql.Timestamp, processId: Int)

/**
 * Handles database connection for "Process" entities using slick.
 */
class ProcessPersistenceActor extends Actor {

  implicit val timeout = akka.util.Timeout(10)

  def receive = {
    // get all processes ordered by name
    case GetProcess(None) => sender ! queryEach("SELECT name FROM process")(_.getString("name"))
    // get process with given id
    case GetProcess(id) => sender ! queryEach("SELECT name FROM process where id = %d".format(id.get))(_.getString("name")).head
    // create new process
    case SaveProcess(None, name, graph, subjects) => println("not yet implemented")
    // save existing process
    case SaveProcess(id, name, graph, subjects) => println("not yet implemented")
    // delete process with given id
    case DeleteProcess(id) => println("not yet implemented")
    case _ => println("not yet implemented")
  }

  // returns current timestamp
  private def now = new java.sql.Timestamp(java.lang.System.currentTimeMillis())

}
