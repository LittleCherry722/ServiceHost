package de.tkip.sbpm.persistence.schema
import de.tkip.sbpm.persistence.mapping._
import scala.slick.lifted.ForeignKeyAction._

trait GroupsRolesSchema extends GroupsSchema with RolesSchema {
  import driver.simple._

  object GroupsRoles extends SchemaTable[GroupRole]("groups_roles") {
    def groupId = column[Int]("group_id")
    def roleId = column[Int]("role_id")
    
    def * = groupId ~ roleId <> (GroupRole, GroupRole.unapply _)

    def pk = primaryKey(pkName, (groupId, roleId))

    def group =
      foreignKey(fkName("groups"), groupId, Groups)(_.id, NoAction, Cascade)
    def role =
      foreignKey(fkName("roles"), roleId, Roles)(_.id, NoAction, Cascade)
  }

}