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
import query.GroupsUsers._
import mapping.PrimitiveMappings._

private[persistence] class GroupUserPersistenceActor extends InstrumentedActor
  with DatabaseAccess with schema.GroupsUsersSchema {
  // import current slick driver dynamically
  import driver.simple._

  // methods to convert internal persistence models to
  // application wide domain models and vice versa
  private def toDomainModel(u: mapping.GroupUser) =
    convert(u, Persistence.groupUser, Domain.groupUser)

  private def toDomainModel(u: Option[mapping.GroupUser]) =
    convert(u, Persistence.groupUser, Domain.groupUser)

  private def toPersistenceModel(u: GroupUser) =
    convert(u, Domain.groupUser, Persistence.groupUser)

  def wrappedReceive = {
    // get all group -> user mappings
    case Read.All => answer { implicit session =>
      Query(GroupsUsers).list.map(toDomainModel)
    }
    // get all group -> user mappings for a user
    case Read.ByUserId(userId) => answer { implicit session =>
      Query(GroupsUsers).where(_.userId === userId).list.map(toDomainModel)
    }
    // get all group -> user mappings for a group
    case Read.ByGroupId(groupId) => answer { implicit session =>
      Query(GroupsUsers).where(_.groupId === groupId).list.map(toDomainModel)
    }
    // get group -> user mapping
    case Read.ById(groupId, userId) => answer { implicit session =>
      toDomainModel(Query(GroupsUsers).where(e => e.groupId === groupId && (e.userId === userId)).firstOption)
    }
    // save group -> user mappings
    case Save.Entity(gus @ _*) => answer { implicit session =>
      // save all given entities
      gus.map(save) match {
        // only one entity was given, return it's id
        case ids if (ids.size == 1) => ids.head
        // more entities were given return all ids
        case ids                    => ids
      }
    }
    // delete group -> user mapping
    case Delete.ById(groupId, userId) => answer { implicit session =>
      delete(groupId, userId)
    }
    // delete all mappings for a user
    case Delete.ByUserId(userId) => answer { implicit session =>
      GroupsUsers.where(_.userId === userId).delete
    }
    // delete all mappings for a group
    case Delete.ByGroupId(groupId) => answer { implicit session =>
      GroupsUsers.where(_.groupId === groupId).delete
    }
  }

  // delete existing entry with given group an user id
  // and insert new record with given values
  private def save(gu: GroupUser)(implicit session: Session) = {
    val res = delete(gu.groupId, gu.userId)
    GroupsUsers.insert(toPersistenceModel(gu))
    // return id if it was an insert
    if (res == 0)
      Some((gu.groupId, gu.userId))
    else
      None
  }

  // delete existing entry with given group an user id
  private def delete(groupId: Int, userId: Int)(implicit session: Session) = {
    GroupsUsers.where(e => e.groupId === groupId && (e.userId === userId)).delete
  }

}
