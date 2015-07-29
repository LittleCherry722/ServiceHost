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
trait GraphEdgesSchema extends GraphNodesSchema {
  // import current slick driver dynamically
  import driver.simple._
  import scala.slick.collection.heterogenous._
  import syntax._

  // represents schema if the "graph_edges" table in the database
  // using slick's lifted embedding API
  class GraphEdges(tag: Tag) extends SchemaTable[GraphEdge](tag, "graph_edges") {
    def startNodeId = column[Short]("id", DbType.smallint)
    def endNodeId = column[Short]("end_node_id", DbType.smallint)
    def macroId = column[String]("macro_id", DbType.stringIdentifier)
    def subjectId = column[String]("subject_id", DbType.stringIdentifier)
    def originalSubjectId = column[Option[String]]("original_subject_id", DbType.stringIdentifier)
    def graphId = column[Int]("graph_id")
    def text = column[String]("text", DbType.name)
    def edgeType = column[String]("type", DbType.stringIdentifier)
    def manualPositionOffsetLabelX = column[Option[Short]]("manual_position_offset_label_x", DbType.smallint)
    def manualPositionOffsetLabelY = column[Option[Short]]("manual_position_offset_label_y", DbType.smallint)
    def targetSubjectId = column[Option[String]]("target_subject_id", DbType.stringIdentifier)
    def targetExchangeSubjectId = column[Option[String]]("target_exchange_subject_id", DbType.stringIdentifier)
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

    def * = (startNodeId :: endNodeId :: macroId :: subjectId
      :: originalSubjectId :: graphId :: text :: edgeType
      :: manualPositionOffsetLabelX :: manualPositionOffsetLabelY
      :: targetSubjectId :: targetExchangeSubjectId :: targetMin :: targetMax
      :: targetCreateNew :: targetVariableId :: isDisabled :: isOptional
      :: priority :: manualTimeout :: variableId :: correlationId :: comment
      :: transportMethod :: HNil).shaped <> ({
      case x => GraphEdge(
        startNodeId                = x(0),
        endNodeId                  = x(1),
        macroId                    = x(2),
        subjectId                  = x(3),
        originalSubjectId          = x(4),
        graphId                    = x(5),
        text                       = x(6),
        edgeType                   = x(7),
        manualPositionOffsetLabelX = x(8),
        manualPositionOffsetLabelY = x(9),
        targetSubjectId            = x(10),
        exchangeTargetId           = x(11),
        targetMin                  = x(12),
        targetMax                  = x(13),
        targetCreateNew            = x(14),
        targetVariableId           = x(15),
        isDisabled                 = x(16),
        isOptional                 = x(17),
        priority                   = x(18),
        manualTimeout              = x(19),
        variableId                 = x(20),
        correlationId              = x(21),
        comment                    = x(22),
        transportMethod            = x(23))
    },(
      {x:GraphEdge =>
        Option((
          x.startNodeId ::
            x.endNodeId ::
            x.macroId ::
            x.subjectId ::
            x.originalSubjectId ::
            x.graphId ::
            x.text ::
            x.edgeType ::
            x.manualPositionOffsetLabelX ::
            x.manualPositionOffsetLabelY ::
            x.targetSubjectId ::
            x.exchangeTargetId ::
            x.targetMin ::
            x.targetMax ::
            x.targetCreateNew ::
            x.targetVariableId ::
            x.isDisabled ::
            x.isOptional ::
            x.priority ::
            x.manualTimeout ::
            x.variableId ::
            x.correlationId ::
            x.comment ::
            x.transportMethod ::
            HNil))
      }))

    def pk = primaryKey(pkName, (startNodeId, endNodeId, macroId, subjectId, graphId))

    def startNode = foreignKey(fkName("graph_nodes_start"), (startNodeId, macroId, subjectId, graphId), graphNodes)(n => (n.id, n.macroId, n.subjectId, n.graphId), NoAction, Cascade)
    def endNode = foreignKey(fkName("graph_nodes_end"), (startNodeId, macroId, subjectId, graphId), graphNodes)(n => (n.id, n.macroId, n.subjectId, n.graphId), NoAction, Cascade)
    def targetSubject = foreignKey(fkName("graph_subjects_target"), (targetSubjectId, graphId), graphSubjects)(s => (s.id, s.graphId))
    def variable = foreignKey(fkName("graph_variables"), (variableId, subjectId, graphId), graphVariables)(v => (v.id, v.subjectId, v.graphId))
    def targetVariable = foreignKey(fkName("graph_variables_target"), (targetVariableId, subjectId, graphId), graphVariables)(v => (v.id, v.subjectId, v.graphId))
  }

  val graphEdges = TableQuery[GraphEdges]
}