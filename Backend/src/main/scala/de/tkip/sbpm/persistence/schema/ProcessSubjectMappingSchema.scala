package de.tkip.sbpm.persistence.schema

import de.tkip.sbpm.persistence.mapping.ProcessSubjectMapping

import scala.slick.model.ForeignKeyAction.{Cascade, NoAction}

trait ProcessSubjectMappingSchema extends ProcessesSchema {
  // import current slick driver dynamically
  import driver.simple._

  // represents schema if the "graph_subjects" table in the database
  // using slick's lifted embedding API
  class ProcessSubjectMappings(tag: Tag) extends SchemaTable[ProcessSubjectMapping](tag, "process_subject_mappings") {
    def processId = column[Int]("process_id")
    def viewId = column[Int]("view_id")
    def from = column[String]("from")
    def to = column[String]("to")

    def * = (processId, viewId, from, to) <> (ProcessSubjectMapping.tupled, ProcessSubjectMapping.unapply)

    def pk = primaryKey(pkName, (processId, viewId, from, to))

    def process = foreignKey(fkName("processes"), processId, processes)(_.id, NoAction, Cascade)
  }

  val processSubjectMappings = TableQuery[ProcessSubjectMappings]
}
