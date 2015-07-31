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
import de.tkip.sbpm.persistence.schema.GraphSchema._

import scala.slick.model.ForeignKeyAction.{Cascade, NoAction}

/**
 * Defines the database schema of GraphMacros.
 * If you want to query GraphMacros database table mix this trait
 * into the actor performing the queries.
 */
object ViewSchema extends Schema {
  // import current slick driver dynamically
  import driver.simple._
  import InterfaceSchema.interfaces

  // represents schema if the "graph_macros" table in the database
  // using slick's lifted embedding API
  class Views(tag: Tag) extends SchemaTable[View](tag, "views") {
    def id = autoIncIdCol[Int]
    def interfaceId = column[Int]("interface_id")
    def mainSubjectId = column[String]("main_subject_id")
    def graphId = column[Int]("graph_id")

    def * = (id.?, interfaceId, mainSubjectId, graphId) <> (View.tupled, View.unapply)

    def interfaceIdIdx = index(s"${tableName}_idx_interface_id", interfaceId)
    def graph = foreignKey(fkName("graphs"), graphId, graphs)(_.id, NoAction, Cascade)
    def interface = foreignKey(fkName("interface"), interfaceId, interfaces)(_.id, NoAction, Cascade)
  }

  val views = TableQuery[Views]
}