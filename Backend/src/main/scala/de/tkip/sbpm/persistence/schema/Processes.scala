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

  implicit val stringToStringList = MappedTypeMapper.base[List[String], String](
    list => list mkString ",",
    str => (str split ",").toList
  )

  // represents schema if the "processes" table in the database
  // using slick's lifted embedding API
  object Processes extends SchemaTable[Process]("processes") {
    def id = autoIncIdCol[Int]
    def name = nameCol
    def isCase = column[Boolean]("case")
    def isImplementation = column[Boolean]("isImplementation", O.Default(false))
    def offerId = column[Option[Int]]("offerId")
    def fixedSubjectId = column[Option[String]]("fixedSubjectId")
    def interfaceSubjects = column[List[String]]("interfaceSubjects")
    def startAble = column[Boolean]("startAble")
    def * = id.? ~ name ~ isCase ~ isImplementation ~ offerId ~ fixedSubjectId ~ interfaceSubjects ~ startAble <>(Process, Process.unapply _)
    def autoInc = * returning id
    def uniqueName = unique(name)
  }

}