package de.tkip.sbpm.persistence.schema
import de.tkip.sbpm.persistence.mapping._
import scala.slick.lifted.ForeignKeyAction._

trait GraphEdgesSchema extends GraphNodesSchema {
  import driver.simple._

  object GraphEdges extends SchemaTable[GraphEdge]("graph_edges") {
    def startNodeId = column[Short]("id", DbType.smallint)
    def endNodeId = column[Short]("end_node_id", DbType.smallint)
    def macroId = column[String]("macro_id", DbType.stringIdentifier)
    def subjectId = column[String]("subject_id", DbType.stringIdentifier)
    def graphId = column[Int]("graph_id")
    def text = column[Option[String]]("text", DbType.name)
    def edgeType = column[String]("type", DbType.stringIdentifier)
    def targetSubjectId = column[Option[String]]("target_subject_id", DbType.stringIdentifier)
    def targetMin = column[Option[Short]]("target_min", DbType.smallint)
    def targetMax = column[Option[Short]]("target_max", DbType.smallint)
    def targetCreateNew = column[Option[Boolean]]("target_create_new")
    def targetVariableId = column[Option[String]]("target_variable_id", DbType.stringIdentifier)
    def isDisabled = column[Boolean]("disabled", O.Default(false))
    def isOptional = column[Boolean]("optional", O.Default(false))
    def priority = column[Byte]("priority", DbType.tinyint)
    def manualTimeout = column[Boolean]("manual_timeout", O.Default(false))
    def variableId = column[Option[String]]("variable_id", DbType.stringIdentifier)
    def correlationId = column[Option[String]]("correlation_id", DbType.stringIdentifier)
    def comment = column[Option[String]]("comment", DbType.comment)
    def transportMethod = column[String]("transport_method", DbType.stringIdentifier, O.Default("internal"))

    def * = startNodeId ~ endNodeId ~ macroId ~ subjectId ~ graphId ~ text ~ edgeType ~ targetSubjectId ~ targetMin ~ targetMax ~ targetCreateNew ~ targetVariableId ~ isDisabled ~ isOptional ~ priority ~ manualTimeout ~ variableId ~ correlationId ~ comment ~ transportMethod <> (GraphEdge, GraphEdge.unapply _)

    def pk = primaryKey(pkName, (startNodeId, endNodeId, macroId, subjectId, graphId))

    def startNode = foreignKey(fkName("graph_nodes_start"), (startNodeId, macroId, subjectId, graphId), GraphNodes)(n => (n.id, n.macroId, n.subjectId, n.graphId), NoAction, Cascade)
    def endNode = foreignKey(fkName("graph_nodes_end"), (startNodeId, macroId, subjectId, graphId), GraphNodes)(n => (n.id, n.macroId, n.subjectId, n.graphId), NoAction, Cascade)
    def targetSubject = foreignKey(fkName("graph_subjects_target"), (targetSubjectId, graphId), GraphSubjects)(s => (s.id, s.graphId))
    def variable = foreignKey(fkName("graph_variables"), (variableId, subjectId, graphId), GraphVariables)(v => (v.id, v.subjectId, v.graphId))
    def targetVariable = foreignKey(fkName("graph_variables_target"), (targetVariableId, subjectId, graphId), GraphVariables)(v => (v.id, v.subjectId, v.graphId))
  }

}