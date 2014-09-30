package de.tkip.sbpm.persistence.schema

import de.tkip.sbpm.persistence.mapping.GraphMergedSubject
import de.tkip.sbpm.persistence.schema.GraphVariablesSchema._

import scala.slick.model.ForeignKeyAction.{Cascade, NoAction}

/**
 * Created by arne on 30.09.14.
 */
object GraphMergedSubjectsSchema extends Schema {
  // import current slick driver dynamically
  import driver.simple._
  import GraphSubjectsSchema.graphSubjects
  import GraphSchema.graphs

  // represents schema if the "graph_merged_subjects" table in the database
  // using slick's lifted embedding API
  class GraphMergedSubjects(tag: Tag) extends SchemaTable[GraphMergedSubject](tag, "graph_merged_subject") {
    def id = stringIdCol
    def subjectId = column[String]("subject_id", DbType.stringIdentifier)
    def graphId = column[Int]("graph_id")
    def name = nameCol

    def * = (id, subjectId, graphId, name) <> (GraphMergedSubject.tupled, GraphMergedSubject.unapply)

    def pk = primaryKey(pkName, (id, subjectId, graphId))
    def idx = index(s"${tableName}_idx_graph_id", graphId)

    def subject =
      foreignKey(fkName("subjects"), (subjectId, graphId), graphSubjects)(s => (s.id, s.graphId), NoAction, Cascade)
    def graph =
      foreignKey(fkName("graphs"), graphId, graphs)(_.id, NoAction, Cascade)
  }

  val graphMergedSubjects = TableQuery[GraphMergedSubjects]
}
