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
// save process instance to db, if id is None a new process instance is created 
// and its id (Option[Int]) is returned (on update None)
case class SaveProcessInstance(processInstance: ProcessInstance) extends ProcessInstanceAction
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
      case GetProcessInstance(None) =>
        answer { ProcessInstances.sortBy(_.id).list }
      // get process instance with given id
      case GetProcessInstance(id) =>
        answer { ProcessInstances.where(_.id === id).firstOption }
      // create new process instance
      case SaveProcessInstance(pi @ ProcessInstance(None, _, _, _, _)) =>
        answer { Some(ProcessInstances.autoInc.insert(pi)) }
      // save existing process instance
      case SaveProcessInstance(pi @ ProcessInstance(id, _, _, _, _)) => update(id, pi)
      // delete process instance with given id
      case DeleteProcessInstance(id) =>
        answer { ProcessInstances.where(_.id === id).delete(session) }
      // execute DDL for creating "process_instance" table
      case InitDatabase => answer { ProcessInstances.ddl.create(session) }
      case DropDatabase => answer { dropIgnoreErrors(ProcessInstances.ddl) }
    }
  }

  // update entity or throw exception if it does not exist
  def update(id: Option[Int], pi: ProcessInstance)(implicit session: Session) = answer {
    val res = ProcessInstances.where(_.id === id).update(pi)
    if (res == 0)
      throw new EntityNotFoundException("Process instance with id %d does not exist.", id.get)
    None
  }
}