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
 * Defines the database schema of GraphEdges.
 * If you want to query GraphEdges database table mix this trait
 * into the actor performing the queries.
 */
object GraphEdgesSchema extends Schema {
  // import current slick driver dynamically
  import driver.simple._
  import GraphNodesSchema.graphNodes
  import GraphSubjectsSchema.graphSubjects
  import GraphVariablesSchema.graphVariables

  // represents schema if the "graph_edges" table in the database
  // using slick's lifted embedding API
  class GraphEdges(tag: Tag) extends SchemaTable[GraphEdge](tag, "graph_edges") {
    def startNodeId = column[Short]("id", DbType.smallint)
    def endNodeId = column[Short]("end_node_id", DbType.smallint)
    def macroId = column[String]("macro_id", DbType.stringIdentifier)
    def subjectId = column[String]("subject_id", DbType.stringIdentifier)
    def graphId = column[Int]("graph_id")
    def text = column[String]("text", DbType.name)
    def edgeType = column[String]("type", DbType.stringIdentifier)
    def manualPositionOffsetLabelX = column[Option[Short]]("manual_position_offset_label_x", DbType.smallint)
    def manualPositionOffsetLabelY = column[Option[Short]]("manual_position_offset_label_y", DbType.smallint)
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


    def * = (startNodeId, endNodeId, macroId, subjectId, graphId, text
      , edgeType, manualPositionOffsetLabelX, manualPositionOffsetLabelY
      , targetSubjectId, targetMin, targetMax, targetCreateNew, targetVariableId
      , isDisabled, isOptional, priority, manualTimeout, variableId
      , correlationId, comment, transportMethod) <> (GraphEdge.tupled, GraphEdge.unapply)

    def pk = primaryKey(pkName, (startNodeId, endNodeId, macroId, subjectId, graphId))
    def idx = index(s"${tableName}_idx_graph_id", graphId)

    def startNode = foreignKey(fkName("graph_nodes_start"), (startNodeId, macroId, subjectId, graphId), graphNodes)(n => (n.id, n.macroId, n.subjectId, n.graphId), NoAction, Cascade)
    def endNode = foreignKey(fkName("graph_nodes_end"), (startNodeId, macroId, subjectId, graphId), graphNodes)(n => (n.id, n.macroId, n.subjectId, n.graphId), NoAction, Cascade)
    def targetSubject = foreignKey(fkName("graph_subjects_target"), (targetSubjectId, graphId), graphSubjects)(s => (s.id, s.graphId))
    def variable = foreignKey(fkName("graph_variables"), (variableId, subjectId, graphId), graphVariables)(v => (v.id, v.subjectId, v.graphId))
    def targetVariable = foreignKey(fkName("graph_variables_target"), (targetVariableId, subjectId, graphId), graphVariables)(v => (v.id, v.subjectId, v.graphId))
  }

  val graphEdges = TableQuery[GraphEdges]
}