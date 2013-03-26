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

import de.tkip.sbpm.model.GroupUser

/**
 * PersistenceActor queries for "GroupsUsers".
 */
object GroupsUsers {
  // used to identify all GroupsUsers queries
  trait Query extends BaseQuery

  object Read {
    def apply() = All
    /**
     * returns all group -> user associations (Seq[GroupUser])
     */
    case object All extends Query
    /**
     * returns group -> user association by group and user id
     * or None if not found (Option[GroupUser])
     */
    case class ById(groupId: Int, userId: Int) extends Query
    /**
     * returns all group -> user associations for a group id
     * (Seq[GroupUser])
     */
    case class ByGroupId(id: Int) extends Query
    /**
     * returns all group -> user associations for a user id
     * (Seq[GroupUser])
     */
    case class ByUserId(id: Int) extends Query
  }

  object Save {
    def apply(groupUser: GroupUser*) = Entity(groupUser: _*)
    /**
     * saves all given associations
     * if one entity given, returns (groupId, userId) if entry was created
     * or None if entry was updated (Option[(Int, Int)])
     * if multiple entities given, Seq[Option[(Int, Int)]]
     * is returned respectively
     */
    case class Entity(groupUser: GroupUser*) extends Query
  }

  object Delete {
    def apply(groupUser: GroupUser) = ById(groupUser.groupId, groupUser.userId)
    /**
     * deletes association by group and user id with empty result
     */
    case class ById(groupId: Int, userId: Int) extends Query
    /**
     * deletes associations of group with empty result
     */
    case class ByGroupId(id: Int) extends Query
    /**
     * deletes associations of user with empty result
     */
    case class ByUserId(id: Int) extends Query
  }
}
