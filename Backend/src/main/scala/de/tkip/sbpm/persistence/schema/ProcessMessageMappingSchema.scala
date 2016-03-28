package de.tkip.sbpm.persistence.schema

import de.tkip.sbpm.persistence.mapping.{ProcessMessageMapping, ProcessSubjectMapping}

import scala.slick.model.ForeignKeyAction.{Cascade, NoAction}

/**
  * Created by arne on 29.10.15.
  */
trait ProcessMessageMappingSchema extends ProcessesSchema {
  // import current slick driver dynamically
  import driver.simple._

  // represents schema if the "graph_subjects" table in the database
  // using slick's lifted embedding API
  class ProcessMessageMappings(tag: Tag) extends SchemaTable[ProcessMessageMapping](tag, "process_message_mappings") {
    def processId = column[Int]("process_id")
    def viewId = column[Int]("view_id")
    def from = column[String]("from")
    def to = column[String]("to")

    def * = (processId, viewId, from, to) <> (ProcessMessageMapping.tupled, ProcessMessageMapping.unapply)

    def pk = primaryKey(pkName, (processId, viewId, from, to))

    def process = foreignKey(fkName("processes"), processId, processes)(_.id, NoAction, Cascade)
  }

  val processMessageMappings = TableQuery[ProcessMessageMappings]
}
