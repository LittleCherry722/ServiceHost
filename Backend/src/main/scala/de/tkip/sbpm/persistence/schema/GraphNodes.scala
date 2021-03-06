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
 * Defines the database schema of GraphNodes.
 * If you want to query GraphNodes database table mix this trait
 * into the actor performing the queries.
 */
trait GraphNodesSchema extends GraphMacrosSchema
  with GraphVariablesSchema with GraphConversationsSchema with GraphMessagesSchema {
  // import current slick driver dynamically
  import driver.simple._

  import scala.slick.collection.heterogenous._
  import syntax._

  // represents schema if the "graph_nodes" table in the database
  // using slick's lifted embedding API
  class GraphNodes(tag: Tag) extends SchemaTable[GraphNode](tag, "graph_nodes") {
    def id = column[Short]("id", DbType.smallint)
    def macroId = column[String]("macro_id", DbType.stringIdentifier)
    def subjectId = column[String]("subject_id", DbType.stringIdentifier)
    def graphId = column[Int]("graph_id")
    def text = column[String]("text", DbType.name)
    def isStart = column[Boolean]("start")
    def isEnd = column[Boolean]("end")
    def nodeType = column[String]("type", DbType.stringIdentifier)
    def manualPositionOffsetX = column[Option[Short]]("manual_position_offset_x", DbType.smallint)
    def manualPositionOffsetY = column[Option[Short]]("manual_position_offset_y", DbType.smallint)
    def isAutoExecute = column[Option[Boolean]]("autoExecute")
    def isDisabled = column[Boolean]("disabled")
    def isMajorStartNode = column[Boolean]("major_start_node")
    def conversationId = column[Option[String]]("conversation_id", DbType.stringIdentifier)
    def variableId = column[Option[String]]("variable_id", DbType.stringIdentifier)
    def optionMessageId = column[Option[String]]("option_message_id", DbType.stringIdentifier)
    def optionSubjectId = column[Option[String]]("option_subject_id", DbType.stringIdentifier)
    def optionCorrelationId = column[Option[String]]("option_correlation_id", DbType.stringIdentifier)
    def optionConversationId = column[Option[String]]("option_conversation_id", DbType.stringIdentifier)
    def optionNodeId = column[Option[Short]]("option_node_id", DbType.smallint)
    def chooseAgentSubject = column[Option[String]]("choose_agent_subject", DbType.stringIdentifier)
    def executeMacroId = column[Option[String]]("execute_macro_id", DbType.stringIdentifier)
    def blackboxname = column[Option[String]]("blackboxname", DbType.stringIdentifier)


    def * = (id.? :: macroId :: subjectId :: graphId :: text :: isStart :: isEnd :: nodeType ::
      manualPositionOffsetX :: manualPositionOffsetY :: isAutoExecute ::
      isDisabled :: isMajorStartNode :: conversationId  :: variableId :: optionMessageId  ::
      optionSubjectId :: optionCorrelationId  :: optionConversationId  :: optionNodeId  ::
      chooseAgentSubject  :: executeMacroId  :: blackboxname :: HNil).shaped <>
      ({
        case x => GraphNode(
          id = x(0).get,
          macroId = x(1),
          subjectId = x(2),
          graphId = x(3),
          text = x(4),
          isStart = x(5),
          isEnd = x(6),
          nodeType = x(7),
          manualPositionOffsetX = x(8),
          manualPositionOffsetY = x(9),
          isAutoExecute = x(10),
          isDisabled = x(11),
          isMajorStartNode = x(12),
          conversationId = x(13),
          variableId = x(14),
          optionMessageId = x(15),
          optionSubjectId = x(16),
          optionCorrelationId = x(17),
          optionConversationId= x(18),
          optionNodeId = x(19),
          chooseAgentSubject = x(20),
          executeMacroId = x(21),
          blackboxname = x(22))
      },(
        {x:GraphNode =>
          Option((
            Some(x.id) ::
            x.macroId ::
            x.subjectId ::
            x.graphId ::
            x.text ::
            x.isStart ::
            x.isEnd ::
            x.nodeType ::
            x.manualPositionOffsetX ::
            x.manualPositionOffsetY ::
            x.isAutoExecute ::
            x.isDisabled ::
            x.isMajorStartNode ::
            x.conversationId ::
            x.variableId ::
            x.optionMessageId ::
            x.optionSubjectId ::
            x.optionCorrelationId ::
            x.optionConversationId ::
            x.optionNodeId ::
            x.chooseAgentSubject ::
            x.executeMacroId ::
            x.blackboxname ::
            HNil))
      }))

    def pk = primaryKey(pkName, (id, macroId, subjectId, graphId))

    def nodeMacro =
      foreignKey(fkName("graph_macros"), (macroId, subjectId, graphId), graphMacros)(m => (m.id, m.subjectId, m.graphId), NoAction, Cascade)
    def conversation =
      foreignKey(fkName("graph_conversations"), (conversationId, graphId), graphConversations)(c => (c.id, c.graphId))
    def variable =
      foreignKey(fkName("graph_variables"), (variableId, subjectId, graphId), graphVariables)(v => (v.id, v.subjectId, v.graphId))
    def optionMessage =
      foreignKey(fkName("graph_messages_opt"), (optionMessageId, graphId), graphMessages)(m => (m.id, m.graphId))
    def optionSubject =
      foreignKey(fkName("graph_subjects_opt"), (optionSubjectId, graphId), graphSubjects)(s => (s.id, s.graphId))
    def optionConversation =
      foreignKey(fkName("graph_conversations_opt"), (optionConversationId, graphId), graphConversations)(c => (c.id, c.graphId))
    def optionNode =
      foreignKey(fkName("opt_node"), (optionNodeId, macroId, subjectId, graphId), graphNodes)(n => (n.id, n.macroId, n.subjectId, n.graphId))
    def executeMacro =
      foreignKey(fkName("graph_macros_exec"), (executeMacroId, subjectId, graphId), graphMacros)(m => (m.id, m.subjectId, m.graphId))
//    def varMan =
//      foreignKey(fkName("graph_var_man"), (id, subjectId, macroId, graphId), GraphVarMan)(vm => (vm.id, vm.subjectId, vm.macroId, vm.graphId))
  }

  val graphNodes = TableQuery[GraphNodes]
}
