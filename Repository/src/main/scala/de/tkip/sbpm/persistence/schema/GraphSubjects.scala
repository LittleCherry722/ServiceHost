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
 * Defines the database schema of GraphSubjects.
 * If you want to query GraphSubjects database table mix this trait
 * into the actor performing the queries.
 */
object GraphSubjectsSchema extends Schema {
  // import current slick driver dynamically
  import driver.simple._

  // represents schema if the "graph_subjects" table in the database
  // using slick's lifted embedding API
  class GraphSubjects(tag: Tag) extends SchemaTable[GraphSubject](tag, "graph_subjects") {
    def id = stringIdCol
    def graphId = column[Int]("graph_id")
    def name = nameCol
    def subjectType = column[String]("type", DbType.stringIdentifier)
    def isDisabled = column[Boolean]("disabled")
    def isStartSubject = column[Boolean]("start_subject")
    def inputPool = column[Short]("input_pool", DbType.smallint)
    def blackboxname = column[Option[String]]("blackboxname")
    def relatedSubjectId = column[Option[String]]("related_subject_id")
    def relatedInterfaceId = column[Option[Int]]("related_interface_id")
    def isImplementation = column[Option[Boolean]]("is_implementation")
    def externalType = column[Option[String]]("external_type", DbType.stringIdentifier)
    def role = column[Option[String]]("role")
    def url = column[Option[String]]("url")
    def comment = column[Option[String]]("comment", DbType.comment)

    def * = (id, graphId, name, subjectType, isDisabled, isStartSubject, inputPool
      , blackboxname, relatedSubjectId , relatedInterfaceId, isImplementation, externalType, role
      , comment) <> (GraphSubject.tupled, GraphSubject unapply)

    def pk = primaryKey(pkName, (id, graphId))
    def idx = index(s"${tableName}_idx_graph_id", graphId)
  }

  val graphSubjects = TableQuery[GraphSubjects]
}
