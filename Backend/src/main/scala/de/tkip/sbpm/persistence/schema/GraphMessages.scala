package de.tkip.sbpm.persistence.schema
import de.tkip.sbpm.persistence.mapping._
import scala.slick.lifted.ForeignKeyAction._

trait GraphMessagesSchema extends GraphsSchema {
  import driver.simple._
  
  object GraphMessages extends SchemaTable[GraphMessage]("graph_messages") {
    def id = stringIdCol
    def graphId = column[Int]("graph_id")
    def name = nameCol
    def * = id ~ graphId ~ name <> (GraphMessage, GraphMessage.unapply _)
    
    def pk = primaryKey(pkName, (id, graphId))
    
    def graph = 
      foreignKey(fkName("graphs"), graphId, Graphs)(_.id, NoAction, Cascade)
  }

}