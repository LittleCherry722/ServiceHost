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

import de.tkip.sbpm.model.UserToUserMessage
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.UserID

/**
 * PersistenceActor queries for "Messages".
 */
object Messages {
  // used to identify all Messages queries
  trait Query extends BaseQuery
  
  object Read {
    def apply() = All
    /**
     * returns all messages (Seq[Messages])
     */
    case object All extends Query
    
    
    case class WithSource(id: UserID) extends Query
    
    case class WithTarget(id: UserID) extends Query
    /**
     * returns message by id or None if not found (Option[Message])
     */
    case class ById(id: Int) extends Query
  }

  object Save {
    def apply(message: UserToUserMessage*) = Entity(message: _*)
    /**
     * saves all given messages
     * if one entity given, returns generated id if 
     * entry was created (because id in given message was None)
     * or None if entry was updated (Option[Int])
     * if multiple entities given, Seq[Option[Int]]
     * is returned respectively
     */
    case class Entity(message: UserToUserMessage*) extends Query
  }

  object Delete {
    def apply(message: UserToUserMessage) = ById(message.id.get)
    /**
     * deletes entity by id with empty result
     */
    case class ById(id: Int) extends Query
  }
}
