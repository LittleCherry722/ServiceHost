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
 * Defines the database schema of Messages.
 * If you want to query Messages database table mix this trait
 * into the actor performing the queries.
 */
trait MessagesSchema extends ProcessInstancesSchema with UsersSchema {
  // import current slick driver dynamically
  import driver.simple._

  // represents schema if the "messages" table in the database
  // using slick's lifted embedding API
  object Messages extends SchemaTable[Message]("messages") {
    def id = autoIncIdCol[Int]
    def fromUserId = column[Int]("from_user_id")
    def toUserId = column[Int]("to_user_id")
    def title = column[String]("title")
    def isRead = column[Boolean]("read")
    def data = column[String]("data", DbType.blob)
    def date = column[java.sql.Timestamp]("date")

    def * = id.? ~ fromUserId ~ toUserId ~ title ~
      isRead ~ data ~ date <> (Message, Message.unapply _)
    def autoInc = * returning id

    def fromUser =
      foreignKey(fkName("users_from"), fromUserId, Users)(_.id)
    def toUser =
      foreignKey(fkName("users_to"), toUserId, Users)(_.id)
  }

}