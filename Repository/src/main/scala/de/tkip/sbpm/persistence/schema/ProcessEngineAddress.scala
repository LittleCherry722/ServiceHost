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
 * Defines the database schema of Processes.
 * If you want to query Processes database table mix this trait
 * into the actor performing the queries.
 */
object ProcessEngineAddressSchema extends Schema {
  // import current slick driver dynamically

  import driver.simple._

  // represents schema if the "processes" table in the database
  // using slick's lifted embedding API
  class ProcessEngineAddresses(tag: Tag) extends SchemaTable[ProcessEngineAddress](tag, "addresses") {
    def id = autoIncIdCol[Int]
    def ip = column[String]("ip_address")
    def port = column[Int]("port")
    def * = (id.?, ip, port) <> (ProcessEngineAddress.tupled, ProcessEngineAddress.unapply)
  }

  val addresses = TableQuery[ProcessEngineAddresses]
}