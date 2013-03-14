package de.tkip.sbpm.persistence.schema

import com.typesafe.config.Config
import de.tkip.sbpm.persistence.mapping.Configuration

trait ConfigurationsSchema extends Schema {
  import driver.simple._

  // represents the "configuration" table in the database
  object Configurations extends SchemaTable[Configuration]("configurations") {
    def key = column[String]("key", O.PrimaryKey, DbType.name)
    def label = column[Option[String]]("label", DbType.name)
    def value = column[Option[String]]("value", DbType.comment)
    def dataType = column[String]("type", DbType.stringIdentifier, O.Default("String"))
    // map table to Configuration case class
    def * = key ~ label ~ value ~ dataType <> (Configuration, Configuration.unapply _)
  }
}