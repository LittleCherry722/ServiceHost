package de.tkip.sbpm.persistence.schema
import de.tkip.sbpm.persistence.mapping._

trait GroupsSchema extends Schema {
  import driver.simple._
  
  object Groups extends SchemaTable[Group]("groups") {
    def id = autoIncIdCol[Int]
    def name = nameCol
    def isActive = activeCol
    
    def * = id.? ~ name ~ isActive <> (Group, Group.unapply _)
    def autoInc = * returning id
    
    def uniqueName = unique(name)
  }

}