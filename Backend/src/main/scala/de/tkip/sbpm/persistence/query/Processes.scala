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
    case object All extends Query
    case class ById(id: Int) extends Query
    case class ByName(name: String) extends Query
  }

  object Save {
    def apply(process: Process*) = Entity(process: _*)
    case class Entity(process: Process*) extends Query
    case class WithGraph(process: Process, graph: Graph) extends Query
  }

  object Delete {
    def apply(process: Process) = ById(process.id.get)
    case class ById(id: Int) extends Query
  }
}
