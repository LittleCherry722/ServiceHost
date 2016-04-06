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

import de.tkip.sbpm.model.Process
import de.tkip.sbpm.model.Graph

/**
 * PersistenceActor queries for "Processes".
 */
object Processes {
  // used to identify all Processes queries
  trait Query extends BaseQuery

  object Read {
    def apply() = All
    /**
     * returns all processes (Seq[Process])
     */
    case object All extends Query
    /**
     * returns process by id or None if not found (Option[Process])
     */
    case class ById(id: Int) extends Query
    /**
     * returns process by name or None if not found (Option[Process])
     */
    case class ByName(name: String) extends Query

    case class SubjectMappings(processId: Int) extends Query
  }

  object Save {
    def apply(process: Process*) = Entity(process: _*)
    /**
     * saves all given processes
     * if one entity given, returns generated id if
     * entry was created (because id in given process was None)
     * or None if entry was updated (Option[Int])
     * if multiple entities given, Seq[Option[Int]]
     * is returned respectively
     */
    case class Entity(process: Process*) extends Query
    /**
     * saves given process and graph
     * if process.id = None, a new process is created
     * a new version of the graph is created always
     * (regardless whether id is None or not)
     * the graph is associated to the given process automatically
     * and set as active graph
     * result contains generated ids (processId, graphId): (Option[Int], Option[Int])
     * ids can be None if entity was updated and not created
     */
    case class WithGraph(process: Process, graph: Graph) extends Query
  }

  object Delete {
    def apply(process: Process) = ById(process.id.get)
    /**
     * deletes entity by id with empty result
     */
    case class ById(id: Int) extends Query
  }
}
