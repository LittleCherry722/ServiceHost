package de.tkip.sbpm.persistence.schema
import de.tkip.sbpm.persistence.mapping._

trait UsersSchema extends Schema {
  import driver.simple._

  object Users extends SchemaTable[User]("users") {
    def id = autoIncIdCol[Int]
    def name = nameCol
    def isActive = activeCol
    def inputPoolSize = column[Int]("input_pool_size", DbType.smallint, O.Default(8))

    def * = id.? ~ name ~ isActive ~ inputPoolSize <> (User, User.unapply _)
    
    def autoInc = * returning id

    def uniqueName = unique(name)
  }

}