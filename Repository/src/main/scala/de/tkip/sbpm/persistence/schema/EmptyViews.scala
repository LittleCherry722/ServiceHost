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
object EmptyViewSchema extends Schema {
  // import current slick driver dynamically
  import driver.simple._
  import InterfaceSchema.interfaces
  import ViewSchema.views

  // represents schema if the "processes" table in the database
  // using slick's lifted embedding API
  class EmptyViews(tag: Tag) extends SchemaTable[EmptyView](tag, "empty_view") {
    def id = autoIncIdCol[Int]
    def interfaceId = column[Int]("interface_id")
    def viewId = column[Int]("view_id")

    def * = (id.?, interfaceId, viewId) <> (EmptyView.tupled, EmptyView.unapply)

    def interfaceIdIdx = index(s"${tableName}_idx_interface_id", interfaceId)
    def viewIdIdx = index(s"${tableName}_idx_view_id", viewId)

    def interface = foreignKey(fkName("interface"), interfaceId, interfaces)(_.id, NoAction, Cascade)
    def view = foreignKey(fkName("view"), viewId, views)(_.id, NoAction, Cascade)
  }

  val emptyViews = TableQuery[EmptyViews]
}
