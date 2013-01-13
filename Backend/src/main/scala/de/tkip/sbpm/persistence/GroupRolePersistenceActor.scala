package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted

/*
* Messages for querying database
* all message classes that inherit GroupRoleAction
* are redirected to GroupRolePersistenceActor
*/
sealed abstract class GroupRoleAction extends PersistenceAction
// get all group -> role mappings (Seq[model.GroupRole])
case class GetGroupRole() extends GroupRoleAction
// save group -> role mapping to db (nothing is returned)
case class SaveGroupRole(groupId: Int, roleId: Int, isActive: Boolean = true) extends GroupRoleAction
// delete group -> role mapping from db
case class DeleteGroupRole(groupId: Int, roleId: Int) extends GroupRoleAction

package model {
  // represents a group -> role mapping in the db
  case class GroupRole(groupId: Int, roleId: Int, isActive: Boolean = true)
}

/**
 * Handles all DB operations for table "group_x_roles".
 */
private[persistence] class GroupRolePersistenceActor extends Actor with DatabaseAccess {
  import model._
  // import driver loaded according to akka config
  import driver.simple._

  // represents the "group_x_roles" table in the database
  object GroupRoles extends Table[GroupRole]("group_x_roles") {
    def groupId = column[Int]("groupID")
    def roleId = column[Int]("roleID")
    def isActive = column[Boolean]("active", O.Default(true))
    // composite primary key
    def pk = primaryKey("pk", (groupId, roleId))
    def * = groupId ~ roleId ~ isActive <> (GroupRole, GroupRole.unapply _)
  }

  def receive = database.withSession { implicit session => // execute all db operations in a session
    {
      // get all group -> role mappings ordered by group id
      case GetGroupRole() => sender ! GroupRoles.sortBy(_.groupId).list
      // save group -> role mapping
      case SaveGroupRole(groupId, roleId, isActive) => save(groupId, roleId, isActive)
      // delete group -> role mapping
      case DeleteGroupRole(groupId, roleId) => delete(groupId, roleId)
      // execute DDL to create "group_x_roles" table
      case InitDatabase => GroupRoles.ddl.create(session)
    }
  }

  // delete existing exntry with given group an role id 
  // and insert new record with given values
  private def save(groupId: Int, roleId: Int, isActive: Boolean)(implicit session: Session) = {
    delete(groupId, roleId)
    GroupRoles.insert(GroupRole(groupId, roleId, isActive))
  }

  // delete existing exntry with given group an role id
  private def delete(groupId: Int, roleId: Int)(implicit session: Session) = {
    GroupRoles.where(e => e.groupId === groupId && (e.roleId === roleId)).delete(session)
  }

}