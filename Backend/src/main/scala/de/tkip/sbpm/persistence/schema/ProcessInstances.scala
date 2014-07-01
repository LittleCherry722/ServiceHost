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
 * Defines the database schema of ProcessInstances.
 * If you want to query ProcessInstances database table mix this trait
 * into the actor performing the queries.
 */
trait ProcessInstancesSchema extends ProcessesSchema with GraphsSchema {
  // import current slick driver dynamically
  import driver.simple._

  // represents schema if the "process_instances" table in the database
  // using slick's lifted embedding API
  class ProcessInstances(tag: Tag) extends SchemaTable[ProcessInstance](tag, "process_instances") {
    def id = autoIncIdCol[Int]
    def processId = column[Int]("process_id")
    def graphId = column[Int]("graph_id")
    def data = column[Option[String]]("data", DbType.blob)

    def * = (id.?, processId, graphId, data) <> (ProcessInstance.tupled, ProcessInstance.unapply)
    // def autoInc = * returning id

    def process =
      foreignKey(fkName("processes"), processId, processes)(_.id, NoAction, Cascade)
    def graph =
      foreignKey(fkName("graphs"), graphId, graphs)(_.id, NoAction, Cascade)
  }

  val processInstances = TableQuery[ProcessInstances]
}