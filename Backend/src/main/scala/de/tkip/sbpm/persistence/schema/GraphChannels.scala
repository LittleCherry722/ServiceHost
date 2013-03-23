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
import scala.slick.lifted.ForeignKeyAction._

/**
 * Defines the database schema of GraphChannels.
 * If you want to query GraphChannels database table mix this trait
 * into the actor performing the queries.
 */
trait GraphChannelsSchema extends GraphsSchema {
  // import current slick driver dynamically
  import driver.simple._

  // represents schema if the "graph_channels" table in the database
  // using slick's lifted embedding API
  object GraphChannels extends SchemaTable[GraphChannel]("graph_channels") {
    def id = stringIdCol
    def graphId = column[Int]("graph_id")
    def name = nameCol
    def * = id ~ graphId ~ name <> (GraphChannel, GraphChannel.unapply _)

    def pk = primaryKey(pkName, (id, graphId))

    def graph =
      foreignKey(fkName("graphs"), graphId, Graphs)(_.id, NoAction, Cascade)
  }

}