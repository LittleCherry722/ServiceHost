package de.tkip.sbpm.persistence
import akka.actor.Actor
import de.tkip.sbpm.model._
import akka.actor.Props
import scala.slick.lifted

/*
* Messages for querying database
* all message classes that inherit ConfigurationAction
* are redirected to ConfigurationPersistenceActor
*/
sealed abstract class ConfigurationAction extends PersistenceAction
/* get entry (Option[model.Configuration]) by key 
* or all entries (Seq[model.Configuration]) by sending None as key
* None or empty Seq is returned if no entities where found
*/
case class GetConfiguration(key: Option[String] = None) extends ConfigurationAction
// save config to db (nothing is returned)
case class SaveConfiguration(config: Configuration) extends ConfigurationAction
// delete config with given key from db (nothing is returned)
case class DeleteConfiguration(key: String) extends ConfigurationAction

/**
 * Handles all database operations for database table "configuration".
 */
private[persistence] class ConfigurationPersistenceActor extends Actor with DatabaseAccess {

  import driver.simple._
  import DBType._
  
  // represents the "configuration" table in the database
  object Configurations extends Table[Configuration]("configuration") {
    def key = column[String]("key", O.PrimaryKey, O.DBType(varchar(64)))
    def label = column[String]("label", O.DBType(varchar(64)))
    def value = column[String]("value", O.DBType(varchar(128)))
    def dataType = column[String]("type", O.DBType(varchar(16)), O.Default("String"))
    // map table to Configuration case class
    def * = key ~ label ~ value ~ dataType <> (Configuration, Configuration.unapply _)
  }

  def receive = database.withSession { implicit session => // execute all db operations in a session
    {
      // get all configs ordered by key
      case GetConfiguration(None) => answer { Configurations.sortBy(_.key).list }
      // get config with given key as option (None if not found)
      case GetConfiguration(key) => answer { Configurations.where(_.key === key).firstOption }
      // save config entry
      case SaveConfiguration(c: Configuration) => answer { save(c) }
      // delete config with given key
      case DeleteConfiguration(key) => answer { delete(key) }
      // execute DDL for "configuration" table
      case InitDatabase => answer { Configurations.ddl.create(session) }
    }
  }

  // replaces the config entry with given key with new values
  def save(config: Configuration)(implicit session: Session) = {
    val exists = delete(config.key)
    Configurations.insert(config)
    if (exists == 0)
      Some(config.key)
    else
      None
  }

  // delete config entry with given key
  def delete(key: String)(implicit session: Session) = {
    Configurations.where(_.key === key).delete(session)
  }
}