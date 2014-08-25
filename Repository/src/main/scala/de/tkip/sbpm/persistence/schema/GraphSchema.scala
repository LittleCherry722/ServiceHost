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

import de.tkip.sbpm.persistence.mapping.Graph
import scala.slick.model.ForeignKeyAction.{Cascade, NoAction}

/**
 * Defines the database schema of GraphConversations.
 * If you want to query GraphConversations database table mix this trait
 * into the actor performing the queries.
 */
object GraphSchema extends Schema {
  // import current slick driver dynamically
  import driver.simple._
  import InterfaceSchema.interfaces

  // represents schema if the "graphs" table in the database
  // using slick's lifted embedding API
  class Graphs(tag: Tag) extends SchemaTable[Graph](tag, "graphs") {
    def id = autoIncIdCol[Int]
    def interfaceId = column[Int]("interface_id")
    def date = column[java.sql.Timestamp]("date")

    def * = (id.?, interfaceId) <> (Graph.tupled, Graph.unapply)
    // def autoInc = * returning id

    def interface =
      foreignKey(fkName("interfaces"), interfaceId, interfaces)(_.id, NoAction, Cascade)
  }

  val graphs = TableQuery[Graphs]
}