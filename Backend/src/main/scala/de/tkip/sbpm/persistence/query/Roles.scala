package de.tkip.sbpm.persistence.query

import de.tkip.sbpm.model.Role

object Roles {
  trait Query extends BaseQuery
  
  object Read {
    def apply() = All
    case object All extends Query
    case class ById(id: Int) extends Query
    case class ByName(name: String) extends Query
  }

  object Save {
    def apply(role: Role*) = Entity(role: _*)
    case class Entity(role: Role*) extends Query
  }

  object Delete {
    def apply(role: Role) = ById(role.id.get)
    case class ById(id: Int) extends Query
  }
}
