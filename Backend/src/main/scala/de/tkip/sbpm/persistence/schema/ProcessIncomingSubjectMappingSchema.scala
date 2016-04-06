package de.tkip.sbpm.persistence.schema

import de.tkip.sbpm.persistence.mapping.ProcessIncomingSubjectMapping

import scala.slick.model.ForeignKeyAction.{Cascade, NoAction}

/**
  * Created by arne on 29.10.15.
  */
trait ProcessIncomingSubjectMappingSchema extends ProcessesSchema {
  // import current slick driver dynamically
  import driver.simple._

  // represents schema if the "graph_subjects" table in the database
  // using slick's lifted embedding API
  class ProcessIncomingSubjectMappings(tag: Tag) extends SchemaTable[ProcessIncomingSubjectMapping](tag, "process_message_mappings") {
    def processId = column[Int]("process_id")
    def from = column[String]("from")
    def to = column[String]("to")

    def * = (processId, from, to) <> (ProcessIncomingSubjectMapping.tupled, ProcessIncomingSubjectMapping.unapply)

    def pk = primaryKey(pkName, (processId, from, to))

    def process = foreignKey(fkName("processes"), processId, processes)(_.id, NoAction, Cascade)
  }

  val processIncomingSubjectMappings = TableQuery[ProcessIncomingSubjectMappings]
}
