package de.tkip.sbpm.persistence.schema

import de.tkip.sbpm.persistence.mapping.GraphSubjectViewId

import scala.slick.model.ForeignKeyAction.{Cascade, NoAction}


trait GraphSubjectViewIdsSchema extends GraphSubjectsSchema {
  // import current slick driver dynamically
  import driver.simple._

  // represents schema if the "groups_roles" table in the database
  // using slick's lifted embedding API
  class GraphSubjectViewIds(tag: Tag) extends SchemaTable[GraphSubjectViewId](tag, "graph_subject_view_ids") {
    def graphId = column[Int]("graph_id")
    def subjectId = column[String]("subject_id")
    def viewId = column[Int]("view_id")

    def * = (graphId, subjectId, viewId) <> (GraphSubjectViewId.tupled, GraphSubjectViewId.unapply)

    def pk = primaryKey(pkName, (graphId, subjectId, viewId))

    def subject =
      foreignKey(fkName("subjects"), subjectId, graphSubjects)(s => s.id, NoAction, Cascade)
  }

  val graphSubjectViewIds = TableQuery[GraphSubjectViewIds]
}
