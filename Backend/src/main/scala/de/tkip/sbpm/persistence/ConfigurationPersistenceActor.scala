/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.persistence

import akka.actor.Actor
import de.tkip.sbpm.model._
import akka.actor.Props
import scala.slick.lifted
import de.tkip.sbpm.persistence.schema.ConfigurationsSchema
import query.Configurations._
import mapping.PrimitiveMappings._

/**
 * Handles all database operations for database table "configuration".
 */
private[persistence] class ConfigurationPersistenceActor extends Actor
  with DatabaseAccess
  with ConfigurationsSchema {
  // import current slick driver dynamically
  import driver.simple._

  // methods to convert internal persistence models to
  // application wide domain models and vice versa
  private def toDomainModel(c: mapping.Configuration) =
    convert(c, Persistence.configuration, Domain.configuration)

  private def toDomainModel(c: Option[mapping.Configuration]) =
    convert(c, Persistence.configuration, Domain.configuration)

  private def toPersistenceModel(c: Configuration) =
    convert(c, Domain.configuration, Persistence.configuration)

  def receive = {
    // get all configs
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

  // creates or replaces the given config entry
  private def save(config: Configuration)(implicit session: Session) = {
    // delete old entry
    val exists = delete(config.key)
    Configurations.insert(toPersistenceModel(config))
    // return None of entry already existed otherwise its key
    if (exists == 0)
      Some(config.key)
    else
      None
  }

  // delete config entry with given key
  private def delete(key: String)(implicit session: Session) = {
    Configurations.where(_.key === key).delete
  }
}