package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted
import de.tkip.sbpm.model._
/*
* Messages for querying database
* all message classes that inherit GroupAction
* are redirected to GroupPersistenceActor
*/
sealed abstract class GroupAction extends PersistenceAction
/* get entry (Option[model.Group]) by id 
* or all entries (Seq[model.Group]) by sending None as id
* None or empty Seq is returned if no entities where found
*/
case class GetGroup(id: Option[Int] = None) extends GroupAction
// save group to db, if id is None a new group is created and its id is returned
case class SaveGroup(id: Option[Int], name: String, isActive: Boolean = true) extends GroupAction
// delete group with id from db (nothing is returned)
case class DeleteGroup(id: Int) extends GroupAction

private[persistence] class GroupPersistenceActor extends Actor with DatabaseAccess {
  import de.tkip.sbpm.model._
  // import driver loaded according to akka config
  import driver.simple._
  import DBType._

  // represents the "groups" table in the database
  object Groups extends Table[Group]("groups") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.DBType(varchar(32)))
    def isActive = column[Boolean]("active", O.Default(true))
    def * = id.? ~ name ~ isActive <> (Group, Group.unapply _)
    // auto increment method returning generated id
    def autoInc = * returning id
  }

  def receive = database.withSession { implicit session => // execute all db operations in a session
    {
      // get all groups ordered by id
      case GetGroup(None) => sender ! Groups.sortBy(_.id).list
      // get group with given id
      case GetGroup(id) => sender ! Groups.where(_.id === id).firstOption
      // create new group
      case SaveGroup(None, name, isActive) =>
        sender ! Groups.autoInc.insert(Group(None, name, isActive))
      // save existing group
      case SaveGroup(id, name, isActive) =>
        Groups.where(_.id === id).update(Group(id, name, isActive))
      // delete group with given id
      case DeleteGroup(id) => Groups.where(_.id === id).delete(session)
      // execute DDL for "groups" table
      case InitDatabase => Groups.ddl.create(session)
    }
  }

}