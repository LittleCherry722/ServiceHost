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
object EmptyViewMappingsSchema extends Schema {
  // import current slick driver dynamically
  import EmptyViewSchema.emptyViews
  import driver.simple._


  // represents schema if the "processes" table in the database
  // using slick's lifted embedding API
  class EmptyViewMappings(tag: Tag) extends SchemaTable[EmptyViewMapping](tag, "empty_view_mapping") {
    def mappingType = column[String]("mapping_type")
    def emptyViewId = column[Int]("empty_view_id")
    def from = column[String]("from")
    def to = column[String]("to")

    def * = (mappingType, emptyViewId, from, to) <> (EmptyViewMapping.tupled, EmptyViewMapping.unapply)

    def pk = primaryKey(pkName, (mappingType, emptyViewId, from, to))
    def idx = index(s"${tableName}_idx_view_mapping_id", (emptyViewId, mappingType))

    def emptyView = foreignKey(fkName("empty_views"), emptyViewId, emptyViews)(_.id, NoAction, Cascade)
  }

  val emptyViewMappings = TableQuery[EmptyViewMappings]
}
