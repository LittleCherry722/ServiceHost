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

import de.tkip.sbpm.model.Message

/**
 * PersistenceActor queries for "Messages".
 */
object Messages {
  // used to identify all Messages queries
  trait Query extends BaseQuery
  
  object Read {
    def apply() = All
    case object All extends Query
    case class ById(id: Int) extends Query
  }

  object Save {
    def apply(message: Message*) = Entity(message: _*)
    case class Entity(message: Message*) extends Query
  }

  object Delete {
    def apply(message: Message) = ById(message.id.get)
    case class ById(id: Int) extends Query
  }
}
