package de.tkip.sbpm.persistence.query

import de.tkip.sbpm.model.Configuration

object Configurations {
	trait Query extends BaseQuery

  object Read {
    def apply() = All
    case object All extends Query
    case class ByKey(key: String) extends Query
  }

  object Save {
    def apply(config: Configuration*) = Entity(config: _*)
    case class Entity(config: Configuration*) extends Query
  }

  object Delete {
    def apply(config: Configuration) = ByKey(config.key)
    case class ByKey(key: String) extends Query
  }
}