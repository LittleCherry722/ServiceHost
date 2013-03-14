package de.tkip.sbpm.persistence.schema
import de.tkip.sbpm.persistence.mapping._
import scala.slick.lifted.ForeignKeyAction._

trait ProcessResponsibilitiesSchema extends ProcessesSchema
  with RolesSchema with UsersSchema {
  import driver.simple._

  object ProcessResponsibilities extends SchemaTable[ProcessResponsibility]("process_responsibilities") {
    def processId = column[Int]("process_id")
    def roleId = column[Int]("role_id")
    def userId = column[Int]("user_id")
    
    def * = processId ~ roleId ~ userId <> (ProcessResponsibility, ProcessResponsibility.unapply _)
    
    def pk = primaryKey(pkName, (processId, roleId, userId))
    
    def process = 
      foreignKey(fkName("processes"), processId, Processes)(_.id, NoAction, Cascade)
    def role = 
      foreignKey(fkName("roles"), roleId, Roles)(_.id, NoAction, Cascade)
    def user = 
      foreignKey(fkName("users"), userId, Users)(_.id, NoAction, Cascade)
  }

}