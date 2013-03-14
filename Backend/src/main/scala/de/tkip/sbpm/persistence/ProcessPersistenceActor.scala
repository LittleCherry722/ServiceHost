package de.tkip.sbpm.persistence

import akka.actor.Actor
import de.tkip.sbpm.model._
import akka.actor.Props
import scala.slick.lifted
import akka.pattern._
import scala.concurrent._

/*
* Messages for querying database
* all message classes that inherit ProcessAction
* are redirected to ProcessPersistenceActor
*/
sealed abstract class ProcessAction extends PersistenceAction
// get process (Option[model.Process]) by id or all process (Seq[model.Process]) by sending None as id
// None or empty Seq is returned if no entities where found
case class GetProcess(id: Option[Int] = None, name: Option[String] = None) extends ProcessAction
// save process to db, if id is None a new process is created 
// and its id (Option[Int]) is returned otherwise None
// if a graph is given additionally, the graph is saved too and its id is injected into process entity
// the result of this message is a tuple (processId: Option[Int], graphId: Option[Int])
// values (if defined) are generated ids if entities were created
case class SaveProcess(process: Process, graph: Option[Graph] = None) extends ProcessAction
// delete process with id from db (no. of deleted entities is returned)
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
    def isCase = column[Boolean]("isCase", O.Default(false))
    def * = id.? ~ name ~ graphId ~ isCase ~ startSubjects <> (Process, Process.unapply _)
    // auto increment method returning generated id
    def autoInc = * returning id
  }

  def receive = database.withSession { implicit session => // execute all db operations in a session
    {
      // get all processes ordered by id
      case GetProcess(None, None) => answer { Processes.sortBy(_.id).list }
      // get process with given id
      case GetProcess(id, None) =>
        answer { Processes.where(_.id === id).firstOption }
      // get process with given name
      case GetProcess(None, name) =>
        answer { Processes.where(_.name === name).firstOption }
      // create new process
      case SaveProcess(p @ Process(None, _, _, _, _), None) =>
        answer { Some(Processes.autoInc.insert(p)) }
      // save existing process
      case SaveProcess(p @ Process(id, _, _, _, _), None) => update(id, p)
      // create new process with a corresponding graph
      case SaveProcess(p: Process, g: Option[Graph]) => saveProcessWithGraph(p, g)
      // delete process with given id
      case DeleteProcess(id) =>
        answer { Processes.where(_.id === id).delete(session) }
      // execute DDL for "process" table
      case InitDatabase => answer { Processes.ddl.create(session) }
      case DropDatabase => answer { dropIgnoreErrors(Processes.ddl) }
    }
  }
  
  private val graphActor = context.actorFor(context.parent.path / "graph")
  
  // update entity or throw exception if it does not exist
  private def update(id: Option[Int], p: Process)(implicit session: Session) = answer {
    val res = Processes.where(_.id === id).update(p)
    if (res == 0)
      throw new EntityNotFoundException("Process with id %d does not exist.", id.get)
    None
  }
  
  /**
   * Saves a process with the corresponding graph to the database.
   */
  private def saveProcessWithGraph(p: Process, g: Option[Graph])(implicit session: Session) = answer {
    var resultId = p.id
    // if id not defined -> save new process
    if (!p.id.isDefined) {
      var pId = Processes.autoInc.insert(p)
      p.id = Some(pId)
      g.get.id = None
      resultId = p.id
    } else {
      if (!Processes.where(_.id === p.id).firstOption.isDefined)
        throw new EntityNotFoundException("Process with id %d does not exist.", p.id.get)
      resultId = None
    }
    // set process id in graph
    g.get.processId = p.id.get
    // save graph via persistence actor
    val graphFuture = graphActor ? SaveGraph(g.get)
    val gId = Await.result(graphFuture.mapTo[Option[Int]], timeout.duration)
    // if graph was created retrieve id
    if (gId.isDefined)
      p.graphId = gId.get
    else
      p.graphId = g.get.id.get
    // update process with graph id
    Processes.where(_.id === p.id).update(p)
    (resultId, gId)
  }
}

