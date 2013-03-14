package de.tkip.sbpm.persistence.schema
import de.tkip.sbpm.persistence.mapping._

trait RolesSchema extends Schema {
  import driver.simple._
  
  object Roles extends SchemaTable[Role]("roles") {
    def id = autoIncIdCol[Int]
    def name = nameCol
    def isActive = activeCol
    
    def * = id.? ~ name ~ isActive <> (Role, Role.unapply _)
    def autoInc = * returning id
    
    def uniqueName = unique(name)
  }

}