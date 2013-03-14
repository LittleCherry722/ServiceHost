package de.tkip.sbpm.persistence
import akka.actor.Actor
import de.tkip.sbpm.model._
import akka.actor.Props
import scala.slick.lifted
import de.tkip.sbpm.persistence.schema.ConfigurationsSchema

/**
 * Handles all database operations for database table "configuration".
 */
private[persistence] class ConfigurationPersistenceActor extends Actor
  with DatabaseAccess
  with ConfigurationsSchema {
  import query.Configurations._
  import mapping.PrimitiveMappings._
  import driver.simple._

  def toDomainModel(c: mapping.Configuration) =
    convert(c, Persistence.configuration, Domain.configuration)

  def toDomainModel(c: Option[mapping.Configuration]) =
    convert(c, Persistence.configuration, Domain.configuration)

  def toPersistenceModel(c: Configuration) =
    convert(c, Domain.configuration, Persistence.configuration)

  def receive = {
    // get all configs ordered by key
    case Read.All => answer { implicit session: Session =>
      Query(Configurations).list.map(toDomainModel)
    }
    // get config with given key as option (None if not found)
    case Read.ByKey(key) => answer { implicit session: Session =>
      toDomainModel(Query(Configurations).where(_.key === key).firstOption)
    }
    // save config entry
    case Save.Entity(c: Configuration) => answer { implicit session: Session =>
      save(c)
    }
    // delete config with given key
    case Delete.ByKey(key) => answer { implicit session: Session =>
      delete(key)
    }
  }

  // replaces the config entry with given key with new values
  def save(config: Configuration)(implicit session: Session) = {
    val exists = delete(config.key)
    Configurations.insert(toPersistenceModel(config))
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