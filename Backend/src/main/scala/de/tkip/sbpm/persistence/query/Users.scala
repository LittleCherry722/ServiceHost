package de.tkip.sbpm.persistence.query

import de.tkip.sbpm.model.User

object Users {
  trait Query extends BaseQuery
  
  object Read {
    def apply() = All
    case object All extends Query
    case class ById(id: Int) extends Query
    case class ByName(name: String) extends Query
    case class Identity(provider: String, eMail: String) extends Query
  }

  object Save {
    def apply(user: User*) = Entity(user: _*)
    case class Entity(user: User*) extends Query
    case class Identity(userId: Int, provider: String, eMail: String, password: Option[String] = None) extends Query
  }

  object Delete {
    def apply(user: User) = ById(user.id.get)
    case class ById(id: Int) extends Query
  }
}
