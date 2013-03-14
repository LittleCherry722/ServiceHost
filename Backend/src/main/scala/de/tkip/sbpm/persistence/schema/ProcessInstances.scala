package de.tkip.sbpm.persistence.schema
import de.tkip.sbpm.persistence.mapping._
import scala.slick.lifted.ForeignKeyAction._

trait ProcessInstancesSchema extends ProcessesSchema with GraphsSchema {
  import driver.simple._

  object ProcessInstances extends SchemaTable[ProcessInstance]("process_instances") {
    def id = autoIncIdCol[Int]
    def processId = column[Int]("process_id")
    def graphId = column[Int]("graph_id")
    def data = column[Option[String]]("data", DbType.blob)

    def * = id.? ~ processId ~ graphId ~ data <> (ProcessInstance, ProcessInstance.unapply _)
    def autoInc = * returning id

    def process =
      foreignKey(fkName("processes"), processId, Processes)(_.id, NoAction, Cascade)
    def graph =
      foreignKey(fkName("graphs"), graphId, Graphs)(_.id, NoAction, Cascade)
  }

}