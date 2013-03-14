package de.tkip.sbpm.persistence.schema
import de.tkip.sbpm.persistence.mapping._
import scala.slick.lifted.ForeignKeyAction._

trait GraphChannelsSchema extends GraphsSchema {
  import driver.simple._
  
  object GraphChannels extends SchemaTable[GraphChannel]("graph_channels") {
    def id = stringIdCol
    def graphId = column[Int]("graph_id")
    def name = nameCol
    def * = id ~ graphId ~ name <> (GraphChannel, GraphChannel.unapply _)
    
    def pk = primaryKey(pkName, (id, graphId))
    
    def graph = 
      foreignKey(fkName("graphs"), graphId, Graphs)(_.id, NoAction, Cascade)
  }

}