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

import de.tkip.sbpm.model.ProcessInstance

/**
 * PersistenceActor queries for "ProcessInstances".
 */
object ProcessInstances {
  // used to identify all ProcessInstances queries
  trait Query extends BaseQuery

  object Read {
    def apply() = All
    /**
     * returns all process instances (Seq[ProcessInstance])
     */
    case object All extends Query
    /**
     * returns process instance by id or None if not found (Option[ProcessInstance])
     */
    case class ById(id: Int) extends Query
  }

  object Save {
    def apply(instance: ProcessInstance*) = Entity(instance: _*)
    /**
     * saves all given process instances
     * if one entity given, returns generated id if
     * entry was created (because id in given instance was None)
     * or None if entry was updated (Option[Int])
     * if multiple entities given, Seq[Option[Int]]
     * is returned respectively
     */
    case class Entity(instance: ProcessInstance*) extends Query
  }

  object Delete {
    def apply(instance: ProcessInstance) = ById(instance.id.get)
    /**
     * deletes entity by id with empty result
     */
    case class ById(id: Int) extends Query
  }
}
