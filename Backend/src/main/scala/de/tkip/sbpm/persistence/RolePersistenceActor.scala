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

import de.tkip.sbpm.instrumentation.InstrumentedActor
import akka.actor.Props
import scala.slick.lifted
import de.tkip.sbpm.model._
import mapping.PrimitiveMappings._
import query.Roles._

/**
 * Handles all database operations for table "roles".
 */
private[persistence] class RolePersistenceActor extends InstrumentedActor
  with DatabaseAccess with schema.RolesSchema {
  // import current slick driver dynamically
  import driver.simple._

  // methods to convert internal persistence models to
  // application wide domain models and vice versa
  def toDomainModel(u: mapping.Role) =
    convert(u, Persistence.role, Domain.role)

  def toDomainModel(u: Option[mapping.Role]) =
    convert(u, Persistence.role, Domain.role)

  def toPersistenceModel(u: Role) =
    convert(u, Domain.role, Persistence.role)

  def wrappedReceive = {
    // get all roles ordered by id
    case Read.All => answer { implicit session =>
      Query(Roles).list.map(toDomainModel)
    }
    // get role with given id
    case Read.ById(id) => answer { implicit session =>
      toDomainModel(Query(Roles).where(_.id === id).firstOption)
    }
    // get role with given name
    case Read.ByName(name) => answer { implicit session =>
      toDomainModel(Query(Roles).where(_.name === name).firstOption)
    }
    // save roles
    case Save.Entity(rs @ _*) => answer { implicit session =>
      // process all roles
      rs.map {
        // insert if id is None
        case r @ Role(None, _, _) => Some(Roles.autoInc.insert(toPersistenceModel(r)))
        // otherwise update existing
        case r @ Role(id, _, _)   => update(id, r)
      } match {
        // only one role was given, return it's id
        case ids if (ids.size == 1) => ids.head
        // more roles were given return all ids
        case ids                    => ids
      }
    }
    // delete role with given id
    case Delete.ById(id) => answer { implicit session =>
      Roles.where(_.id === id).delete(session)
    }
  }

  // update entity or throw exception if it does not exist
  private def update(id: Option[Int], r: Role) = answer { implicit session =>
    val res = Roles.where(_.id === id).update(toPersistenceModel(r))
    if (res == 0)
      throw new EntityNotFoundException("Role with id %d does not exist.", id.get)
    None
  }

}
