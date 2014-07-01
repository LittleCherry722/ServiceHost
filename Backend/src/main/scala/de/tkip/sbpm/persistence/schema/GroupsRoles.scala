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

package de.tkip.sbpm.persistence.schema

import de.tkip.sbpm.persistence.mapping._
import scala.slick.model.ForeignKeyAction._

/**
 * Defines the database schema of GroupsRoles.
 * If you want to query GroupsRoles database table mix this trait
 * into the actor performing the queries.
 */
trait GroupsRolesSchema extends GroupsSchema with RolesSchema {
  // import current slick driver dynamically
  import driver.simple._

  // represents schema if the "groups_roles" table in the database
  // using slick's lifted embedding API
  class GroupsRoles(tag: Tag) extends SchemaTable[GroupRole](tag, "groups_roles") {
    def groupId = column[Int]("group_id")
    def roleId = column[Int]("role_id")
    
    def * = (groupId, roleId) <> (GroupRole.tupled, GroupRole.unapply)

    def pk = primaryKey(pkName, (groupId, roleId))

    def group =
      foreignKey(fkName("groups"), groupId, groups)(_.id, NoAction, Cascade)
    def role =
      foreignKey(fkName("roles"), roleId, roles)(_.id, NoAction, Cascade)
  }

  val groupsRoles = TableQuery[GroupsRoles]
}