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

/**
 * Defines the database schema of Users.
 * If you want to query Users database table mix this trait
 * into the actor performing the queries.
 */
trait UsersSchema extends Schema {
  // import current slick driver dynamically
  import driver.simple._

  // represents schema if the "users" table in the database
  // using slick's lifted embedding API
  object Users extends SchemaTable[User]("users") {
    def id = autoIncIdCol[Int]
    def name = nameCol
    def isActive = activeCol
    def inputPoolSize = column[Int]("input_pool_size", DbType.smallint, O.Default(8))

    def * = id.? ~ name ~ isActive ~ inputPoolSize <> (User, User.unapply _)
    
    def autoInc = * returning id

    def uniqueName = unique(name)
  }

}