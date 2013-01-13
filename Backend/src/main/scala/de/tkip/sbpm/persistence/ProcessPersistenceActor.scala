package de.tkip.sbpm.persistence

import akka.actor.Actor
import de.tkip.sbpm.model._
import akka.actor.Props
import scala.slick.lifted

/*
* Messages for querying database
* all message classes that inherit ProcessAction
* are redirected to ProcessPersistenceActor
*/
sealed abstract class ProcessAction extends PersistenceAction
// get process (Option[model.Process]) by id or all process (Seq[model.Process]) by sending None as id
// None or empty Seq is returned if no entities where found
case class GetProcess(id: Option[Int] = None, name: Option[String] = None) extends ProcessAction
// save process to db, if id is None a new process is created and its id is returned
case class SaveProcess(id: Option[Int] = None, name: String, graphId: Int, isProcess: Boolean = true, startSubjects: String = null) extends ProcessAction
// delete process with id from db (nothing is returned)
case class DeleteProcess(id: Int) extends ProcessAction

/**
 * Handles database connection for "Process" entities using slick.
 */
private[persistence] class ProcessPersistenceActor extends Actor with DatabaseAccess {

  import driver.simple._
  import DBType._
  import de.tkip.sbpm.model._
  
  // represents the "process" table in the database
  object Processes extends Table[Process]("process") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.DBType(varchar(64)))
    def startSubjects = column[String]("startSubjects", O.DBType(varchar(128)))
    def graphId = column[Int]("graphID")
    def isProcess = column[Boolean]("isProcess", O.Default(true))
    def * = id.? ~ name ~ graphId ~ isProcess ~ startSubjects <> (Process, Process.unapply _)
    // auto increment method returning generated id
    def autoInc = * returning id
  }

  def receive = database.withSession { implicit session => // execute all db operations in a session
    {
      // get all processes ordered by id
      case GetProcess(None, None) => sender ! Processes.sortBy(_.id).list
      // get process with given id
      case GetProcess(id, None) => sender ! Processes.where(_.id === id).firstOption
      // get process with given name
      case GetProcess(None, name) => sender ! Processes.where(_.name === name).firstOption
      // create new process
      case SaveProcess(None, name, graphId, isProcess, startSubjects) => 
        sender ! Processes.autoInc.insert(Process(None, name, graphId, isProcess, startSubjects))
      // save existing process
      case SaveProcess(id, name, graphId, isProcess, startSubjects) =>
        Processes.where(_.id === id).update(Process(id, name, graphId, isProcess))
      // delete process with given id
      case DeleteProcess(id) => Processes.where(_.id === id).delete(session)
      // execute DDL for "process" table
      case InitDatabase() => Processes.ddl.create(session)
    }
  }

}

