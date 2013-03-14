package de.tkip.sbpm.persistence.query

import de.tkip.sbpm.model.Message

object Messages {
  trait Query extends BaseQuery
  
  object Read {
    def apply() = All
    case object All extends Query
    case class ById(id: Int) extends Query
  }

  object Save {
    def apply(message: Message*) = Entity(message: _*)
    case class Entity(message: Message*) extends Query
  }

  object Delete {
    def apply(message: Message) = ById(message.id.get)
    case class ById(id: Int) extends Query
  }
}
