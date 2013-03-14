package de.tkip.sbpm.persistence.query

import de.tkip.sbpm.model.Process
import de.tkip.sbpm.model.Graph

object Processes {
  trait Query extends BaseQuery
  
  object Read {
    def apply() = All
    case object All extends Query
    case class ById(id: Int) extends Query
    case class ByName(name: String) extends Query
  }

  object Save {
    def apply(process: Process*) = Entity(process: _*)
    case class Entity(process: Process*) extends Query
    case class WithGraph(process: Process, graph: Graph) extends Query
  }

  object Delete {
    def apply(process: Process) = ById(process.id.get)
    case class ById(id: Int) extends Query
  }
}
