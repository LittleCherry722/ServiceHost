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

import scala.slick.model.ForeignKeyAction.{Cascade, NoAction}

/**
 * Defines the database schema of Processes.
 * If you want to query Processes database table mix this trait
 * into the actor performing the queries.
 */
object InterfaceImplementationSchema extends Schema {
  // import current slick driver dynamically

  import ProcessEngineAddressSchema.addresses
  import driver.simple._

  // represents schema if the "processes" table in the database
  // using slick's lifted embedding API
  class InterfaceImplementations(tag: Tag) extends SchemaTable[InterfaceImplementation](tag, "interface_implementation") {
    def id = autoIncIdCol[Int]
    def processId = column[Int]("process_id")
    def addressId = column[Int]("address_id")
    def ownSubjectId = column[Int]("own_subject_id")
    def viewId = column[Int]("view_id")

    def * = (id.?, processId, addressId, ownSubjectId, viewId) <> (InterfaceImplementation.tupled, InterfaceImplementation.unapply)

    def address = foreignKey(fkName("address"), processId, addresses)(_.id, NoAction, Cascade)
  }

  val interfaceImplementations = TableQuery[InterfaceImplementations]
}
