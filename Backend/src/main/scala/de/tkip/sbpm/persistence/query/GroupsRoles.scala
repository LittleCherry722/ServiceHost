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

package de.tkip.sbpm.persistence.query

import de.tkip.sbpm.model.GroupRole

/**
 * PersistenceActor queries for "GroupsRoles".
 */
object GroupsRoles {
  // used to identify all GroupsRoles queries
  trait Query extends BaseQuery

  object Read {
    def apply() = All
    case object All extends Query
    case class ById(groupId: Int, roleId: Int) extends Query
    case class ByGroupId(id: Int) extends Query
    case class ByRoleId(id: Int) extends Query
  }

  object Save {
    def apply(groupRole: GroupRole*) = Entity(groupRole: _*)
    case class Entity(groupRole: GroupRole*) extends Query
  }

  object Delete {
    def apply(groupRole: GroupRole) = ById(groupRole.groupId, groupRole.roleId)
    case class ById(groupId: Int, roleId: Int) extends Query
    case class ByGroupId(id: Int) extends Query
    case class ByRoleId(id: Int) extends Query
  }
}
