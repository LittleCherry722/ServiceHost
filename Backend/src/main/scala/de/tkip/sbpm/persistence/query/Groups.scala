package de.tkip.sbpm.persistence.query

import de.tkip.sbpm.model.Group

object Groups {
  trait Query extends BaseQuery

  object Read {
    def apply() = All
    case object All extends Query
    case class ById(id: Int) extends Query
    case class ByName(name: String) extends Query
  }

  object Save {
    def apply(group: Group*) = Entity(group: _*)
    case class Entity(group: Group*) extends Query
  }

  object Delete {
    def apply(group: Group) = ById(group.id.get)
    case class ById(id: Int) extends Query
  }
}
