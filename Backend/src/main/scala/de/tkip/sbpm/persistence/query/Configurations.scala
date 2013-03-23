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

import de.tkip.sbpm.model.Configuration

/**
 * Queries for Configuration entities.
 */
object Configurations {
  // used to identify all Configuration related queries
  trait Query extends BaseQuery

  object Read {
    def apply() = All
    case object All extends Query
    case class ByKey(key: String) extends Query
  }

  object Save {
    def apply(config: Configuration*) = Entity(config: _*)
    case class Entity(config: Configuration*) extends Query
  }

  object Delete {
    def apply(config: Configuration) = ByKey(config.key)
    case class ByKey(key: String) extends Query
  }
}