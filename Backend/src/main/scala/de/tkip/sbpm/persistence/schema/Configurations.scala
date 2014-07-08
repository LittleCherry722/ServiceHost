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

import de.tkip.sbpm.persistence.mapping.Configuration


/**
 * Defines the database schema of Configurations.
 * If you want to query Configurations database table mix this trait
 * into the actor performing the queries.
 */
trait ConfigurationsSchema extends Schema {
  import driver.simple._

  // represents schema if the "configurations" table in the database
  // using slick's lifted embedding API
  class Configurations(tag: Tag) extends SchemaTable[Configuration](tag, "configurations") {
    def key = column[String]("key", O.PrimaryKey, DbType.name)
    def label = column[Option[String]]("label", DbType.name)
    def value = column[Option[String]]("value", DbType.comment)
    def dataType = column[String]("type", DbType.stringIdentifier, O.Default("String"))
    // map table to Configuration case class
    def * = (key, label, value, dataType) <> (Configuration.tupled, Configuration.unapply)
  }

  val configurations = TableQuery[Configurations]
}