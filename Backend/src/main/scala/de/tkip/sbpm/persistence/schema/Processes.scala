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

/**
 * Defines the database schema of Processes.
 * If you want to query Processes database table mix this trait
 * into the actor performing the queries.
 */
trait ProcessesSchema extends Schema {
  // import current slick driver dynamically

  import driver.simple._

  implicit val stringToStringList = MappedColumnType.base[List[String], String](
    list => list mkString ",",
    str => (str split ",").toList
  )

  // represents schema if the "processes" table in the database
  // using slick's lifted embedding API
  class Processes(tag: Tag) extends SchemaTable[Process](tag, "processes") {
    def id = autoIncIdCol[Int]
    def interfaceId = column[Option[Int]]("interface_id")
    def publishInterface = column[Boolean]("publish_interface")
    def name = nameCol
    def isCase = column[Boolean]("case")
    def startAble = column[Boolean]("startAble")
    def * = (id.?, interfaceId, publishInterface, name, isCase, startAble) <> (Process.tupled, Process.unapply)
    // def autoInc = * returning id
    def uniqueName = unique(name)
  }

  val processes = TableQuery[Processes]
}