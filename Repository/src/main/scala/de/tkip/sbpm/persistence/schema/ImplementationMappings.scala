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
object ImplementationMappingsSchema extends Schema {
  // import current slick driver dynamically
  import EmptyViewSchema.emptyViews
  import driver.simple._


  // represents schema if the "processes" table in the database
  // using slick's lifted embedding API
  class ImplementationMappings(tag: Tag) extends SchemaTable[ImplementationMapping](tag, "impl_mappings") {
    def mappingType = column[String]("mapping_type")
    def implementationId = column[Int]("implementation_id")
    def from = column[String]("from")
    def to = column[String]("to")

    def * = (mappingType, implementationId, from, to) <> (ImplementationMapping.tupled, ImplementationMapping.unapply)

    def pk = primaryKey(pkName, (mappingType, implementationId, from, to))
    def idx = index(s"${tableName}_idx_implementation_id", (implementationId, mappingType))

    def implementation = foreignKey(fkName("implementation"), implementationId, emptyViews)(_.id, NoAction, Cascade)
  }

  val implementationMappings = TableQuery[ImplementationMappings]
}
