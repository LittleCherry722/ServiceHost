package de.tkip.sbpm.persistence.query

import de.tkip.sbpm.model.GroupRole

object GroupsRoles {
  trait Query extends BaseQuery

  object Read {
    def apply() = All
    case object All extends Query
    case class ById(groupId: Int, roleId: Int) extends Query
    case class ByGroupId(id: Int) extends Query
    case class ByRoleId(id: Int) extends Query
  }

  object Save {
    def apply(groupRole: GroupRole*) = Entity(groupRole: _*)
    case class Entity(groupRole: GroupRole*) extends Query
  }

  object Delete {
    def apply(groupRole: GroupRole) = ById(groupRole.groupId, groupRole.roleId)
    case class ById(groupId: Int, roleId: Int) extends Query
    case class ByGroupId(id: Int) extends Query
    case class ByRoleId(id: Int) extends Query
  }
}
