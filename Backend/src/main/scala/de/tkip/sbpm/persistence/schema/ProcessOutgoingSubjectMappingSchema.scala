package de.tkip.sbpm.persistence.schema

import de.tkip.sbpm.persistence.mapping.ProcessOutgoingSubjectMapping

import scala.slick.model.ForeignKeyAction.{Cascade, NoAction}

trait ProcessOutgoingSubjectMappingSchema extends ProcessesSchema {
  // import current slick driver dynamically
  import driver.simple._

  // represents schema if the "graph_subjects" table in the database
  // using slick's lifted embedding API
  class ProcessOutgoingSubjectMappings(tag: Tag) extends SchemaTable[ProcessOutgoingSubjectMapping](tag, "process_subject_mappings") {
    def processId = column[Int]("process_id")
    def from = column[String]("from")
    def to = column[String]("to")

    def * = (processId, from, to) <> (ProcessOutgoingSubjectMapping.tupled, ProcessOutgoingSubjectMapping.unapply)

    def pk = primaryKey(pkName, (processId, from, to))

    def process = foreignKey(fkName("processes"), processId, processes)(_.id, NoAction, Cascade)
  }

  val processOutgoingSubjectMappings = TableQuery[ProcessOutgoingSubjectMappings]
}
