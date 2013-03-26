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
import akka.actor.Props
import scala.slick.lifted
import de.tkip.sbpm.model._
import query.GroupsRoles._
  import mapping.PrimitiveMappings._

/**
 * Handles all DB operations for table "groups_roles".
 */
private[persistence] class GroupRolePersistenceActor extends Actor
  with DatabaseAccess with schema.GroupsRolesSchema {
  // import current slick driver dynamically
  import driver.simple._

  // methods to convert internal persistence models to
  // application wide domain models and vice versa
  def toDomainModel(u: mapping.GroupRole) =
    convert(u, Persistence.groupRole, Domain.groupRole)

  def toDomainModel(u: Option[mapping.GroupRole]) =
    convert(u, Persistence.groupRole, Domain.groupRole)

  def toPersistenceModel(u: GroupRole) =
    convert(u, Domain.groupRole, Persistence.groupRole)

  def receive = {
    // get all group -> role mappings
    case Read.All => answer { implicit session =>
      Query(GroupsRoles).list.map(toDomainModel)
    }
    // get all group -> role mappings for a role
    case Read.ByRoleId(roleId) => answer { implicit session =>
      Query(GroupsRoles).where(_.roleId === roleId).list.map(toDomainModel)
    }
    // get all group -> role mappings for a group
    case Read.ByGroupId(groupId) => answer { implicit session =>
      Query(GroupsRoles).where(_.groupId === groupId).sortBy(_.roleId).list.map(toDomainModel)
    }
    // get group -> role mapping
    case Read.ById(groupId, roleId) => answer { implicit session =>
      toDomainModel(Query(GroupsRoles).where(e => e.groupId === groupId && (e.roleId === roleId)).firstOption)
    }
    // save group -> role mappings
    case Save.Entity(grs @ _*) => answer { implicit session =>
      // save all given entities
      grs.map(save) match {
         // only one entity was given, return it's id
        case ids if (ids.size == 1) => ids.head
        // more entities were given return all ids
        case ids                    => ids
      }
    }
    // delete group -> role mapping
    case Delete.ById(groupId, roleId) => answer { implicit session =>
      delete(groupId, roleId)
    }
    // delete all mappings for a role
    case Delete.ByRoleId(roleId) => answer { implicit session =>
      GroupsRoles.where(_.roleId === roleId).delete
    }
    // delete all mappings for a group
    case Delete.ByGroupId(groupId) => answer { implicit session =>
      GroupsRoles.where(_.groupId === groupId).delete
    }
  }

  // delete existing entry with given group and role id 
  // and insert new record with given values
  private def save(gr: GroupRole)(implicit session: Session) = {
    val res = delete(gr.groupId, gr.roleId)
    GroupsRoles.insert(toPersistenceModel(gr))
    // return id if entity was newly created
    if (res == 0)
      Some((gr.groupId, gr.roleId))
    else
      None
  }

  // delete existing entry with given group and role id
  private def delete(groupId: Int, roleId: Int)(implicit session: Session) = {
    GroupsRoles.where(e => e.groupId === groupId && (e.roleId === roleId)).delete
  }

}