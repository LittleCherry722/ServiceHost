package de.tkip.sbpm.persistence.query

import de.tkip.sbpm.model.ProcessInstance

object ProcessInstances {
  trait Query extends BaseQuery
  
  object Read {
    def apply() = All
    case object All extends Query
    case class ById(id: Int) extends Query
  }

  object Save {
    def apply(instance: ProcessInstance*) = Entity(instance: _*)
    case class Entity(instance: ProcessInstance*) extends Query
  }

  object Delete {
    def apply(instance: ProcessInstance) = ById(instance.id.get)
    case class ById(id: Int) extends Query
  }
}
