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

package de.tkip.sbpm.persistence.query

import de.tkip.sbpm.model.Graph

/**
 * PersistenceActor queries for "Graphs".
 */
object Graphs {
  // used to identify all Graphs queries
  trait Query extends BaseQuery

  object Read {
    def apply() = All
    /**
     * returns all graphs (Seq[Graph])
     */
    case object All extends Query
    /**
     * returns graph by id or None if not found (Option[Graph])
     */
    case class ById(id: Int) extends Query
  }

  object Save {
    def apply(graph: Graph*) = Entity(graph: _*)
    /**
     * saves all given graphs
     * if one entity given, returns generated id if 
     * entry was created (because id in given graph was None)
     * or None if entry was updated (Option[Int])
     * if multiple entities given, Seq[Option[Int]]
     * is returned respectively
     */
    case class Entity(graph: Graph*) extends Query
  }

  object Delete {
    def apply(graph: Graph) = ById(graph.id.get)
    /**
     * deletes entity by id with empty result
     */
    case class ById(id: Int) extends Query
  }
}
