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

import mapping.PrimitiveMappings._
import query.Groups._
import akka.actor.Props
import scala.slick.lifted
import de.tkip.sbpm.model._
import de.tkip.sbpm.instrumentation.InstrumentedActor

/**
 * Handle all db operation for table "groups".
 */
private[persistence] class GroupPersistenceActor extends InstrumentedActor
  with DatabaseAccess with schema.GroupsSchema {
  // import current slick driver dynamically
  import driver.simple._

  // methods to convert internal persistence models to
  // application wide domain models and vice versa
  private def toDomainModel(u: mapping.Group) =
    convert(u, Persistence.group, Domain.group)

  private def toDomainModel(u: Option[mapping.Group]) =
    convert(u, Persistence.group, Domain.group)

  private def toPersistenceModel(u: Group) =
    convert(u, Domain.group, Persistence.group)

  def wrappedReceive = {
    // get all groups
    case Read.All => answer { implicit session =>
      Query(Groups).list.map(toDomainModel)
    }
    // get group with given id
    case Read.ById(id) => answer { implicit session =>
      toDomainModel(Query(Groups).where(_.id === id).firstOption)
    }
    // get group with given name
    case Read.ByName(name) => answer { implicit session =>
      toDomainModel(Query(Groups).where(_.name === name).firstOption)
    }
    // create or update given groups
    case Save.Entity(gs @ _*) => answer { implicit session =>
      // process all groups
      gs.map {
        // insert if id is None
        case g @ Group(None, _, _) => Some(Groups.autoInc.insert(toPersistenceModel(g)))
        // otherwise update existing
        case g @ Group(id, _, _)   => update(id, g)
      } match {
        // only one group was given, return it's id
        case ids if (ids.size == 1) => ids.head
        // more groups were given return all ids
        case ids                    => ids
      }
    }
    // delete group with given id
    case Delete.ById(id) => answer { implicit session =>
      Groups.where(_.id === id).delete(session)
    }
  }

  // update entity or throw exception if it does not exist
  private def update(id: Option[Int], g: Group)(implicit session: Session) = {
    val res = Groups.where(_.id === id).update(toPersistenceModel(g))
    if (res == 0)
      throw new EntityNotFoundException("Group with id %d does not exist.", id.get)
    None
  }

}
