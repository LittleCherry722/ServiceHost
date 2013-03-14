package de.tkip.sbpm.persistence.schema
import de.tkip.sbpm.persistence.mapping._

trait ProcessesSchema extends Schema {
  import driver.simple._
  
   object Processes extends SchemaTable[Process]("processes") {
    def id = autoIncIdCol[Int]
    def name = nameCol
    def isCase = column[Boolean]("case")
    
    def * = id.? ~ name ~ isCase <> (Process, Process.unapply _)
    def autoInc = * returning id
        
    def uniqueName = unique(name)
  }

}