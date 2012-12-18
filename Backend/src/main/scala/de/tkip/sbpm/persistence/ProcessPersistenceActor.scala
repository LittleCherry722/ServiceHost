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
    case SaveProcess(None, name, graph, subjects) => sender ! createProcess(name, graph, subjects)
    // save existing process
    case SaveProcess(id, name, graph, subjects) => sender ! updateProcess(id.get, name, graph, subjects)
    // delete process with given id
    case DeleteProcess(id) => sender ! execute("DELETE FROM process where id = %d".format(id))
    case _ => println("not yet implemented")
  }

  // returns current timestamp
  private def now = new java.sql.Timestamp(java.lang.System.currentTimeMillis())

  private def createProcess(name: String, graph:String, subjects:String) = {
    val id = autoInc("INSERT INTO process (name, startSubjects) VALUES ('%s', '%s')".format(name, graph))
    val graphId = createGraph(graph, id)
    execute("UPDATE process SET graphID = %d WHERE id = %d".format(graphId, id))
    id
  }
  
  private def createGraph(graph: String, processId: Int) =
    autoInc("INSERT INTO process_graphs (graph, processID, date) VALUES ('%s', %d, '%s')".format(graph, processId, now.toString()))
  
  private def updateProcess(id: Int, name:String, graph:String, subjects:String) ={
    val graphId = createGraph(graph, id)
    execute("UPDATE process SET graphID = %d, name = '%s', startSubjects = '%s' WHERE id = %d".format(graphId, name, subjects, id))
    id
  }
}
