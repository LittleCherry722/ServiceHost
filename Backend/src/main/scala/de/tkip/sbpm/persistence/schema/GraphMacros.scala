package de.tkip.sbpm.persistence.schema
import de.tkip.sbpm.persistence.mapping._
import scala.slick.lifted.ForeignKeyAction._

trait GraphMacrosSchema extends GraphSubjectsSchema {
  import driver.simple._
  
   object GraphMacros extends SchemaTable[GraphMacro]("graph_macros") {
    def id = stringIdCol
    def subjectId = column[String]("subject_id", DbType.stringIdentifier)
    def graphId = column[Int]("graph_id")
    def name = nameCol
    def * = id ~ subjectId ~ graphId ~ name <> (GraphMacro, GraphMacro.unapply _)

    def pk = primaryKey(pkName, (id, subjectId, graphId))

    def subject =
      foreignKey(fkName("subjects"), (subjectId, graphId), GraphSubjects)(s => (s.id, s.graphId), NoAction, Cascade)
  }

}