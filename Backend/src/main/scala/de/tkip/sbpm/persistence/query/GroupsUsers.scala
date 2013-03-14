package de.tkip.sbpm.persistence.query

import de.tkip.sbpm.model.GroupUser

object GroupsUsers {
  trait Query extends BaseQuery

  object Read {
    def apply() = All
    case object All extends Query
    case class ById(groupId: Int, userId: Int) extends Query
    case class ByGroupId(id: Int) extends Query
    case class ByUserId(id: Int) extends Query
  }

  object Save {
    def apply(groupUser: GroupUser*) = Entity(groupUser: _*)
    case class Entity(groupUser: GroupUser*) extends Query
  }

  object Delete {
    def apply(groupUser: GroupUser) = ById(groupUser.groupId, groupUser.userId)
    case class ById(groupId: Int, userId: Int) extends Query
    case class ByGroupId(id: Int) extends Query
    case class ByUserId(id: Int) extends Query
  }
}
