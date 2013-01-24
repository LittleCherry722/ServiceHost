package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted
import de.tkip.sbpm.model._

/*
* Messages for querying database
* all message classes that inherit GroupRoleAction
* are redirected to GroupRolePersistenceActor
*/
sealed abstract class GroupRoleAction extends PersistenceAction
// get all group -> role mappings (Seq[model.GroupRole])
case class GetGroupRole() extends GroupRoleAction
// save group -> role mapping to db
// returns primary key Some((groupId, roleId)) if created otherwise None
case class SaveGroupRole(groupRole: GroupRole) extends GroupRoleAction
// delete group -> role mapping from db
case class DeleteGroupRole(groupId: Int, roleId: Int) extends GroupRoleAction

/**
 * Handles all DB operations for table "group_x_roles".
 */
private[persistence] class GroupRolePersistenceActor extends Actor with DatabaseAccess {

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
      case GetGroupRole() => answer { GroupRoles.sortBy(_.groupId).list }
      // save group -> role mapping
      case SaveGroupRole(gr: GroupRole) => answer { save(gr) }
      // delete group -> role mapping
      case DeleteGroupRole(groupId, roleId) =>
        answer { delete(groupId, roleId) }
      // execute DDL to create "group_x_roles" table
      case InitDatabase => answer { GroupRoles.ddl.create(session) }
    }
  }

  // delete existing entry with given group and role id 
  // and insert new record with given values
  private def save(gr: GroupRole)(implicit session: Session) = {
    val res = delete(gr.groupId, gr.roleId)
    GroupRoles.insert(gr)
    if (res == 0)
      Some((gr.groupId, gr.roleId))
    else
      None
  }

  // delete existing entry with given group and role id
  private def delete(groupId: Int, roleId: Int)(implicit session: Session) = {
    GroupRoles.where(e => e.groupId === groupId && (e.roleId === roleId)).delete(session)
  }

}