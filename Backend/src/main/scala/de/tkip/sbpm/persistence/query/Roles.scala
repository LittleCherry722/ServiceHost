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

import de.tkip.sbpm.model.Role

/**
 * PersistenceActor queries for "Roles".
 */
object Roles {
  // used to identify all Roles queries
  trait Query extends BaseQuery

  object Read {
    def apply() = All
    /**
     * returns all roles (Seq[Role])
     */
    case object All extends Query
    /**
     * returns role by id or None if not found (Option[Role])
     */
    case class ById(id: Int) extends Query
    /**
     * returns role by name or None if not found (Option[Role])
     */
    case class ByName(name: String) extends Query
  }

  object Save {
    def apply(role: Role*) = Entity(role: _*)
    /**
     * saves all given roles
     * if one entity given, returns generated id if
     * entry was created (because id in given role was None)
     * or None if entry was updated (Option[Int])
     * if multiple entities given, Seq[Option[Int]]
     * is returned respectively
     */
    case class Entity(role: Role*) extends Query
  }

  object Delete {
    def apply(role: Role) = ById(role.id.get)
    /**
     * deletes entity by id with empty result
     */
    case class ById(id: Int) extends Query
  }
}
