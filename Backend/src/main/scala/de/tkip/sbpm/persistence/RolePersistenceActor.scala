package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted

/*
* Messages for querying database
* all message classes that inherit RoleAction
* are redirected to RolePersistenceActor
*/
sealed abstract class RoleAction extends PersistenceAction
/* get entry (Option[model.Role]) by id 
* or all entries (Seq[model.Role]) by sending None as id
* None or empty Seq is returned if no entities where found
*/
case class GetRole(id: Option[Int] = None) extends RoleAction
// save role to db, if id is None a new process is created and its id is returned
case class SaveRole(id: Option[Int], name: String, isActive: Boolean = true) extends RoleAction
// delete role with id from db
case class DeleteRole(id: Int) extends RoleAction

/**
 * Handles all database operations for table "roles".
 */
private[persistence] class RolePersistenceActor extends Actor with DatabaseAccess {
  // import driver loaded according to akka config
  import driver.simple._
  import DBType._
  import de.tkip.sbpm.model._
  
  // represents the "roles" table in the database
  object Roles extends Table[Role]("roles") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.DBType(varchar(32)))
    def isActive = column[Boolean]("active", O.Default(true))
    def * = id.? ~ name ~ isActive <> (Role, Role.unapply _)
    // auto increment method returning generated id
    def autoInc = * returning id
  }

  def receive = database.withSession { implicit session => // execute all db operations in a session
    {
      // get all roles ordered by id
      case GetRole(None) => sender ! Roles.sortBy(_.id).list
      // get role with given id
      case GetRole(id) => sender ! Roles.where(_.id === id).firstOption
      // create new role
      case SaveRole(None, name, isActive) =>
        sender ! Roles.autoInc.insert(Role(None, name, isActive))
      // save existing role
      case SaveRole(id, name, isActive) =>
        Roles.where(_.id === id).update(Role(id, name, isActive))
      // delete role with given id
      case DeleteRole(id) => Roles.where(_.id === id).delete(session)
      // execute DDL for "roles" table
      case InitDatabase() => Roles.ddl.create(session)
    }
  }

}