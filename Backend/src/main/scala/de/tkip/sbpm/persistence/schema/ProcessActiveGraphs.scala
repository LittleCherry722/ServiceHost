package de.tkip.sbpm.persistence.schema
import de.tkip.sbpm.persistence.mapping._
import scala.slick.lifted.ForeignKeyAction._

trait ProcessActiveGraphsSchema extends ProcessesSchema with GraphsSchema {
  import driver.simple._

  object ProcessActiveGraphs extends SchemaTable[ProcessActiveGraph]("process_active_graphs") {
    def processId = column[Int]("process_id", O.PrimaryKey)
    def graphId = column[Int]("graph_id")
    
    def * = processId ~ graphId <> (ProcessActiveGraph, ProcessActiveGraph.unapply _)

    def process =
      foreignKey(fkName("processes"), processId, Processes)(_.id, NoAction, Cascade)
    def graph =
      foreignKey(fkName("graphs"), (graphId, processId), Graphs)(g => (g.id, g.processId), NoAction, Cascade)
  }

}