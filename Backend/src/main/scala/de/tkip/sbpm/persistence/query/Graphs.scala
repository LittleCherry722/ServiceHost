package de.tkip.sbpm.persistence.query

import de.tkip.sbpm.model.Graph

object Graphs {
  trait Query extends BaseQuery

  object Read {
    def apply() = All
    case object All extends Query
    case class ById(id: Int) extends Query
  }

  object Save {
    def apply(graph: Graph*) = Entity(graph: _*)
    case class Entity(graph: Graph*) extends Query
  }

  object Delete {
    def apply(graph: Graph) = ById(graph.id.get)
    case class ById(id: Int) extends Query
  }
}
