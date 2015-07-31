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
 * Defines the database schema of Graphs.
 * If you want to query Graphs database table mix this trait
 * into the actor performing the queries.
 */
trait VerificationErrorsSchema extends ProcessesSchema {
  // import current slick driver dynamically
  import driver.simple._

  // represents schema if the "graphs" table in the database
  // using slick's lifted embedding API
  class VerificationErrors(tag: Tag) extends SchemaTable[VerificationError](tag, "verification_errors") {
    def id = autoIncIdCol[Int]
    def processId = column[Int]("process_id")
    def message = column[String]("message")

    def * = (id.?, processId, message) <> (VerificationError.tupled, VerificationError.unapply)
    // def autoInc = * returning id

    def process = foreignKey(fkName("processes"), processId, processes)(_.id, NoAction, Cascade)
  }

  val verificationErrors = TableQuery[VerificationErrors]
}