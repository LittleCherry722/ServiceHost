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
 * Defines the database schema of GraphMacros.
 * If you want to query GraphMacros database table mix this trait
 * into the actor performing the queries.
 */
object GraphMacrosSchema extends Schema {
  // import current slick driver dynamically
  import driver.simple._
  import GraphSubjectsSchema.graphSubjects
  
   // represents schema if the "graph_macros" table in the database
  // using slick's lifted embedding API
  class GraphMacros(tag: Tag) extends SchemaTable[GraphMacro](tag, "graph_macros") {
    def id = stringIdCol
    def subjectId = column[String]("subject_id", DbType.stringIdentifier)
    def graphId = column[Int]("graph_id")
    def name = nameCol
    def * = (id, subjectId, graphId, name) <> (GraphMacro.tupled, GraphMacro.unapply)

    def pk = primaryKey(pkName, (id, subjectId, graphId))
    def idx = index(s"${tableName}_idx_graph_id", graphId)

     def subject =
      foreignKey(fkName("subjects"), (subjectId, graphId), graphSubjects)(s => (s.id, s.graphId), NoAction, Cascade)
  }

  val graphMacros = TableQuery[GraphMacros]
}