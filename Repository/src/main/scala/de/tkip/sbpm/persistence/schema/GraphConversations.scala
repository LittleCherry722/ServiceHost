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
import scala.slick.model.ForeignKeyAction._

/**
 * Defines the database schema of GraphConversations.
 * If you want to query GraphConversations database table mix this trait
 * into the actor performing the queries.
 */
object GraphConversationsSchema extends Schema {
  // import current slick driver dynamically
  import driver.simple._
  import GraphSchema.graphs

  // represents schema if the "graph_conversations" table in the database
  // using slick's lifted embedding API
  class GraphConversations(tag: Tag) extends SchemaTable[GraphConversation](tag, "graph_conversations") {
    def id = stringIdCol
    def graphId = column[Int]("graph_id")
    def name = nameCol
    def * = (id, graphId, name) <> (GraphConversation.tupled, GraphConversation.unapply)

    def pk = primaryKey(pkName, (id, graphId))
    def idx = index(s"${tableName}_idx_graph_id", graphId)


    def graph = foreignKey(fkName("graphs"), graphId, graphs)(_.id, NoAction, Cascade)
  }

  val graphConversations = TableQuery[GraphConversations]
}