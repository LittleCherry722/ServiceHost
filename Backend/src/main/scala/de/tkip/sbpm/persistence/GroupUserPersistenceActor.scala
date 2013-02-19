package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted
import de.tkip.sbpm.model._

/*
* Messages for querying database
* all message classes that inherit GroupUserAction
* are redirected to GroupUserPersistenceActor
*/
sealed abstract class GroupUserAction extends PersistenceAction
// get all group -> user mappings (Seq[model.GroupUser])
// optionally filtered by user id
// if both ids given single entity Option[GroupUser] is returned
case class GetGroupUser(groupId: Option[Int] = None, userId: Option[Int] = None) extends GroupUserAction
// save group -> user mapping to db
// returns primary key Some((groupId, userId)) if created otherwise None
case class SaveGroupUser(groupUser: GroupUser) extends GroupUserAction
// delete group -> user mapping from db
case class DeleteGroupUser(groupId: Int, userId: Int) extends GroupUserAction

private[persistence] class GroupUserPersistenceActor extends Actor with DatabaseAccess {
  // import driver loaded according to akka config
  import driver.simple._

  // represents the "group_x_users" table in the database
  object GroupUsers extends Table[GroupUser]("group_x_users") {
    def groupId = column[Int]("groupID")
    def userId = column[Int]("userID")
    def isActive = column[Boolean]("active", O.Default(true))
    // composite primary key
    def pk = primaryKey("pk", (groupId, userId))
    def * = groupId ~ userId ~ isActive <> (GroupUser, GroupUser.unapply _)
  }

  def receive = database.withSession { implicit session => // execute all db operations in a session
    {
      // get all group -> user mappings ordered by group id
      case GetGroupUser(None, None) =>
        answer { GroupUsers.sortBy(_.groupId).list }
        // get all group -> user mappings for a user
      case GetGroupUser(None, userId) =>
        answer { GroupUsers.where(_.userId === userId).sortBy(_.groupId).list }
        // get all group -> user mappings for a group
      case GetGroupUser(groupId, None) =>
        answer { GroupUsers.where(_.groupId === groupId).sortBy(_.userId).list }
        // get group -> user mapping
      case GetGroupUser(groupId, userId) =>
        answer { GroupUsers.where(e => e.groupId === groupId && (e.userId === userId)).firstOption }
      // save group -> user mapping
      case SaveGroupUser(gu: GroupUser) => answer { save(gu) }
      // delete group -> user mapping
      case DeleteGroupUser(groupId, userId) =>
        answer { delete(groupId, userId) }
      // execute DDL to create "group_x_users" table
      case InitDatabase => answer { GroupUsers.ddl.create(session) }
      case DropDatabase => answer { dropIgnoreErrors(GroupUsers.ddl) }
    }
  }

  // delete existing exntry with given group an user id 
  // and insert new record with given values
  private def save(gu: GroupUser)(implicit session: Session) = {
    val res = delete(gu.groupId, gu.userId)
    GroupUsers.insert(gu)
    if (res == 0)
      Some((gu.groupId, gu.userId))
    else
      None
  }

  // delete existing entry with given group an user id 
  private def delete(groupId: Int, userId: Int)(implicit session: Session) = {
    GroupUsers.where(e => e.groupId === groupId && (e.userId === userId)).delete(session)
  }

}