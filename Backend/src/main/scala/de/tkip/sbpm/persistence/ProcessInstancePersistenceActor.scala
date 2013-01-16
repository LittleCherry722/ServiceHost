package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted
import de.tkip.sbpm.model._

/*
* Messages for querying database
* all message classes that inherit ProcessInstanceAction
* are redirected to ProcessInstancePersistenceActor
*/
sealed abstract class ProcessInstanceAction extends PersistenceAction
/* get entry (Option[model.ProcessInstance]) by id 
* or all entries (Seq[model.ProcessInstance]) by sending None as id
* None or empty Seq is returned if no entities where found
*/
case class GetProcessInstance(id: Option[Int] = None) extends ProcessInstanceAction
// save process instance to db, if id is None a new process instance is created and its id is returned
case class SaveProcessInstance(id: Option[Int] = None, processId: Int, graphId: Int, involvedUsers: String, data: String) extends ProcessInstanceAction
// delete process instance with id from db
case class DeleteProcessInstance(id: Int) extends ProcessInstanceAction


/**
 * Handles all database operations for table "process_instance".
 */
private[persistence] class ProcessInstancePersistenceActor extends Actor with DatabaseAccess {

  import driver.simple._
  import DBType._
  import de.tkip.sbpm.model._
  
  // represents the "process_instance" table in the database
  object ProcessInstances extends Table[ProcessInstance]("process_instance") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def processId = column[Int]("processID")
    def graphId = column[Int]("graphID")
    def involvedUsers = column[String]("involvedUsers", O.DBType(varchar(128)))
    def data = column[String]("data", O.DBType(blob))
    def * = id.? ~ processId ~ graphId ~ involvedUsers ~ data <> (ProcessInstance, ProcessInstance.unapply _)
    // auto increment method returning generated id
    def autoInc = * returning id
  }

  def receive = database.withSession { implicit session => // execute all db operations in a session
    {
      // get all process instance ordered by id
      case GetProcessInstance(None) => sender ! ProcessInstances.sortBy(_.id).list
      // get process instance with given id
      case GetProcessInstance(id) => sender ! ProcessInstances.where(_.id === id).firstOption
      // create new process instance
      case SaveProcessInstance(None, processId, graphId, involvedUsers, data) =>
        sender ! ProcessInstances.autoInc.insert(ProcessInstance(None, processId, graphId, involvedUsers, data))
      // save existing process instance
      case SaveProcessInstance(id, processId, graphId, involvedUsers, data) =>
        ProcessInstances.where(_.id === id).update(ProcessInstance(id, processId, graphId, involvedUsers, data))
      // delete process instance with given id
      case DeleteProcessInstance(id) => ProcessInstances.where(_.id === id).delete(session)
      // execute DDL for creating "process_instance" table
      case InitDatabase => ProcessInstances.ddl.create(session)
    }
  }

}