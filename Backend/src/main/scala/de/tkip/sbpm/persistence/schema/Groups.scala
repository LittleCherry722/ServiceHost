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
 * Defines the database schema of Groups.
 * If you want to query Groups database table mix this trait
 * into the actor performing the queries.
 */
trait GroupsSchema extends Schema {
  // import current slick driver dynamically
  import driver.simple._
  
  // represents schema if the "groups" table in the database
  // using slick's lifted embedding API
  class Groups(tag: Tag) extends SchemaTable[Group](tag, "groups") {
    def id = autoIncIdCol[Int]
    def name = nameCol
    def isActive = activeCol
    
    def * = (id.?, name, isActive) <> (Group.tupled, Group.unapply)
    // def autoInc = * returning id
    
    def uniqueName = unique(name)
  }

  val groups = TableQuery[Groups]
}