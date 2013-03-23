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
import scala.slick.lifted.ForeignKeyAction._

/**
 * Defines the database schema of GroupsUsers.
 * If you want to query GroupsUsers database table mix this trait
 * into the actor performing the queries.
 */
trait GroupsUsersSchema extends GroupsSchema with UsersSchema {
  // import current slick driver dynamically
  import driver.simple._

  // represents schema if the "groups_users" table in the database
  // using slick's lifted embedding API
  object GroupsUsers extends SchemaTable[GroupUser]("groups_users") {
    def groupId = column[Int]("group_id")
    def userId = column[Int]("user_id")

    def * = groupId ~ userId <> (GroupUser, GroupUser.unapply _)

    def pk = primaryKey(pkName, (groupId, userId))

    def group =
      foreignKey(fkName("groups"), groupId, Groups)(_.id, NoAction, Cascade)
    def role =
      foreignKey(fkName("users"), userId, Users)(_.id, NoAction, Cascade)
  }

}