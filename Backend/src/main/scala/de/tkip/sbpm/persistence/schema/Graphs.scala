package de.tkip.sbpm.persistence.schema
import de.tkip.sbpm.persistence.mapping._
import scala.slick.lifted.ForeignKeyAction._

trait GraphsSchema extends ProcessesSchema {
  import driver.simple._
  
  object Graphs extends SchemaTable[Graph]("graphs") {
    def id = autoIncIdCol[Int]
    def processId = column[Int]("process_id")
    def date = column[java.sql.Timestamp]("date")
    
    def * = id.? ~ processId ~ date <> (Graph, Graph.unapply _)
    def autoInc = * returning id
    
    def process =
      foreignKey(fkName("processes"), processId, Processes)(_.id, NoAction, Cascade)
  }

}