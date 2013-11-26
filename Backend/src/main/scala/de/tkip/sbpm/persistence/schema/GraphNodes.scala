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
import scala.slick.lifted.ForeignKeyAction._

/**
 * Defines the database schema of GraphNodes.
 * If you want to query GraphNodes database table mix this trait
 * into the actor performing the queries.
 */
trait GraphNodesSchema extends GraphMacrosSchema
  with GraphVariablesSchema with GraphConversationsSchema with GraphMessagesSchema {
  // import current slick driver dynamically
  import driver.simple._

  // represents schema if the "graph_nodes" table in the database
  // using slick's lifted embedding API
  object GraphNodes extends SchemaTable[GraphNode]("graph_nodes") {
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
    def isDisabled = column[Boolean]("disabled")
    def isMajorStartNode = column[Boolean]("major_start_node")
    def conversationId = column[Option[String]]("conversation_id", DbType.stringIdentifier)
    def variableId = column[Option[String]]("variable_id", DbType.stringIdentifier)
    def optionMessageId = column[Option[String]]("option_message_id", DbType.stringIdentifier)
    def optionSubjectId = column[Option[String]]("option_subject_id", DbType.stringIdentifier)
    def optionCorrelationId = column[Option[String]]("option_correlation_id", DbType.stringIdentifier)
    def optionConversationId = column[Option[String]]("option_conversation_id", DbType.stringIdentifier)
    def optionNodeId = column[Option[Short]]("option_node_id", DbType.smallint)
    def executeMacroId = column[Option[String]]("execute_macro_id", DbType.stringIdentifier)
    def varManVar1Id = column[Option[String]]("var_man_var1_id", DbType.stringIdentifier)
    def varManVar2Id = column[Option[String]]("var_man_var2_id", DbType.stringIdentifier)
    def varManOperation = column[Option[String]]("var_man_operation", DbType.stringIdentifier)
    def varManStoreVarId = column[Option[String]]("var_man_store_var_id", DbType.stringIdentifier)

    def * = id ~ macroId ~ subjectId ~ graphId ~ text ~ isStart ~ isEnd ~ nodeType ~ isDisabled ~
      isMajorStartNode ~ conversationId ~ variableId ~ optionMessageId ~ optionSubjectId ~
      optionCorrelationId ~ optionConversationId ~ optionNodeId ~ executeMacroId ~ varManVar1Id ~
      varManVar2Id ~ varManOperation ~ varManStoreVarId <> (GraphNode, GraphNode.unapply _)

    def pk = primaryKey(pkName, (id, macroId, subjectId, graphId))

    def nodeMacro =
      foreignKey(fkName("graph_macros"), (macroId, subjectId, graphId), GraphMacros)(m => (m.id, m.subjectId, m.graphId), NoAction, Cascade)
    def conversation =
      foreignKey(fkName("graph_conversations"), (conversationId, graphId), GraphConversations)(c => (c.id, c.graphId))
    def variable =
      foreignKey(fkName("graph_variables"), (variableId, subjectId, graphId), GraphVariables)(v => (v.id, v.subjectId, v.graphId))
    def optionMessage =
      foreignKey(fkName("graph_messages_opt"), (optionMessageId, graphId), GraphMessages)(m => (m.id, m.graphId))
    def optionSubject =
      foreignKey(fkName("graph_subjects_opt"), (optionSubjectId, graphId), GraphSubjects)(s => (s.id, s.graphId))
    def optionConversation =
      foreignKey(fkName("graph_conversations_opt"), (optionConversationId, graphId), GraphConversations)(c => (c.id, c.graphId))
    def optionNode =
      foreignKey(fkName("opt_node"), (optionNodeId, macroId, subjectId, graphId), GraphNodes)(n => (n.id, n.macroId, n.subjectId, n.graphId))
    def executeMacro =
      foreignKey(fkName("graph_macros_exec"), (executeMacroId, subjectId, graphId), GraphMacros)(m => (m.id, m.subjectId, m.graphId))
    def varManVar1 =
      foreignKey(fkName("graph_variables_var_man1"), (varManVar1Id, subjectId, graphId), GraphVariables)(v => (v.id, v.subjectId, v.graphId))
    def varManVar2 =
      foreignKey(fkName("graph_variables_var_man2"), (varManVar2Id, subjectId, graphId), GraphVariables)(v => (v.id, v.subjectId, v.graphId))
    def varManStore =
      foreignKey(fkName("graph_variables_var_man"), (varManStoreVarId, subjectId, graphId), GraphVariables)(v => (v.id, v.subjectId, v.graphId))
  }

}