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
    /**
     * returns all group -> role associations (Seq[GroupRole])
     */
    case object All extends Query
    /**
     * returns group -> role association by group and role id
     * or None if not found (Option[GroupRole])
     */
    case class ById(groupId: Int, roleId: Int) extends Query
    /**
     * returns all group -> role associations for a group id
     * (Seq[GroupRole])
     */
    case class ByGroupId(id: Int) extends Query
    /**
     * returns all group -> role associations for a role id
     * (Seq[GroupRole])
     */
    case class ByRoleId(id: Int) extends Query
  }

  object Save {
    def apply(groupRole: GroupRole*) = Entity(groupRole: _*)
    /**
     * saves all given associations
     * if one entity given, returns (groupId, roleId) if entry was created
     * or None if entry was updated (Option[(Int, Int)])
     * if multiple entities given, Seq[Option[(Int, Int)]]
     * is returned respectively
     */
    case class Entity(groupRole: GroupRole*) extends Query
  }

  object Delete {
    def apply(groupRole: GroupRole) = ById(groupRole.groupId, groupRole.roleId)
    /**
     * deletes association by group and role id with empty result
     */
    case class ById(groupId: Int, roleId: Int) extends Query
    /**
     * deletes associations of group with empty result
     */
    case class ByGroupId(id: Int) extends Query
    /**
     * deletes associations of role with empty result
     */
    case class ByRoleId(id: Int) extends Query
  }
}
