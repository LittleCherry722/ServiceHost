package de.tkip.sbpm.persistence.schema
import de.tkip.sbpm.persistence.mapping._
import scala.slick.lifted.ForeignKeyAction._

trait GroupsUsersSchema extends GroupsSchema with UsersSchema {
  import driver.simple._

  object GroupsUsers extends SchemaTable[GroupUser]("groups_users") {
    def groupId = column[Int]("group_id")
    def userId = column[Int]("user_id")

    def * = groupId ~ userId <> (GroupUser, GroupUser.unapply _)

    def pk = primaryKey(pkName, (groupId, userId))

    def group =
      foreignKey(fkName("groups"), groupId, Groups)(_.id, NoAction, Cascade)
    def role =
      foreignKey(fkName("users"), userId, Users)(_.id, NoAction, Cascade)
  }

}