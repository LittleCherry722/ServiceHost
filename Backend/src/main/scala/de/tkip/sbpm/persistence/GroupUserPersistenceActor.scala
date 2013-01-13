package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted

/*
* Messages for querying database
* all message classes that inherit GroupUserAction
* are redirected to GroupUserPersistenceActor
*/
sealed abstract class GroupUserAction extends PersistenceAction
// get all group -> user mappings (Seq[model.GroupUser])
case class GetGroupUser() extends GroupUserAction
// save group -> user mapping to db (nothing is returned)
case class SaveGroupUser(groupId: Int, userId: Int, isActive: Boolean = true) extends GroupUserAction
// delete group -> user mapping from db
case class DeleteGroupUser(groupId: Int, userId: Int) extends GroupUserAction

package model {
  // represents a user in the db
  case class GroupUser(groupId: Int, userId: Int, isActive: Boolean = true)
}

private[persistence] class GroupUserPersistenceActor extends Actor with DatabaseAccess {
  import model._
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
      case GetGroupUser() => sender ! GroupUsers.sortBy(_.groupId).list
      // save group -> user mapping
      case SaveGroupUser(groupId, userId, isActive) => save(groupId, userId, isActive)
      // delete group -> user mapping
      case DeleteGroupUser(groupId, userId) => delete(groupId, userId)
      // execute DDL to create "group_x_users" table
      case InitDatabase => GroupUsers.ddl.create(session)
    }
  }

  // delete existing exntry with given group an user id 
  // and insert new record with given values
  private def save(groupId: Int, userId: Int, isActive: Boolean)(implicit session: Session) = {
    delete(groupId, userId)
    GroupUsers.insert(GroupUser(groupId, userId, isActive))
  }

  // delete existing entry with given group an user id 
  private def delete(groupId: Int, userId: Int)(implicit session: Session) = {
    GroupUsers.where(e => e.groupId === groupId && (e.userId === userId)).delete(session)
  }

}