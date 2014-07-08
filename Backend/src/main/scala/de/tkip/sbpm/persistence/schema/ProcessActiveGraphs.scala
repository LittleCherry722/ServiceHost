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
 * Defines the database schema of ProcessActiveGraphs.
 * If you want to query ProcessActiveGraphs database table mix this trait
 * into the actor performing the queries.
 */
trait ProcessActiveGraphsSchema extends ProcessesSchema with GraphsSchema {
  // import current slick driver dynamically
  import driver.simple._

  // represents schema if the "process_active_graphs" table in the database
  // using slick's lifted embedding API
  class ProcessActiveGraphs(tag: Tag) extends SchemaTable[ProcessActiveGraph](tag, "process_active_graphs") {
    def processId = column[Int]("process_id", O.PrimaryKey)
    def graphId = column[Int]("graph_id")
    
    def * = (processId, graphId) <> (ProcessActiveGraph.tupled, ProcessActiveGraph.unapply)

    def process =
      foreignKey(fkName("processes"), processId, processes)(_.id, NoAction, Cascade)
    def graph =
      foreignKey(fkName("graphs"), (graphId, processId), graphs)(g => (g.id, g.processId), NoAction, Cascade)
  }

  val processActiveGraphs = TableQuery[ProcessActiveGraphs]
}