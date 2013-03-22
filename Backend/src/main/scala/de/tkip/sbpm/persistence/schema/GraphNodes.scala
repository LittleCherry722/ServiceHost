package de.tkip.sbpm.persistence.schema
import de.tkip.sbpm.persistence.mapping._
import scala.slick.lifted.ForeignKeyAction._

trait GraphNodesSchema extends GraphMacrosSchema
  with GraphVariablesSchema with GraphChannelsSchema with GraphMessagesSchema {
  import driver.simple._

  object GraphNodes extends SchemaTable[GraphNode]("graph_nodes") {
    def id = column[Short]("id", DbType.smallint)
    def macroId = column[String]("macro_id", DbType.stringIdentifier)
    def subjectId = column[String]("subject_id", DbType.stringIdentifier)
    def graphId = column[Int]("graph_id")
    def text = column[String]("text", DbType.name)
    def isStart = column[Boolean]("start")
    def isEnd = column[Boolean]("end")
    def nodeType = column[String]("type", DbType.stringIdentifier)
    def isDisabled = column[Boolean]("disabled")
    def isMajorStartNode = column[Boolean]("major_start_node")
    def channelId = column[Option[String]]("channel_id", DbType.stringIdentifier)
    def variableId = column[Option[String]]("variable_id", DbType.stringIdentifier)
    def optionMessageId = column[Option[String]]("option_message_id", DbType.stringIdentifier)
    def optionSubjectId = column[Option[String]]("option_subject_id", DbType.stringIdentifier)
    def optionCorrelationId = column[Option[String]]("option_correlation_id", DbType.stringIdentifier)
    def optionChannelId = column[Option[String]]("option_channel_id", DbType.stringIdentifier)
    def optionNodeId = column[Option[Short]]("option_node_id", DbType.smallint)
    def executeMacroId = column[Option[String]]("execute_macro_id", DbType.stringIdentifier)
    def varManVar1Id = column[Option[String]]("var_man_var1_id", DbType.stringIdentifier)
    def varManVar2Id = column[Option[String]]("var_man_var2_id", DbType.stringIdentifier)
    def varManOperation = column[Option[String]]("var_man_operation", DbType.stringIdentifier)
    def varManStoreVarId = column[Option[String]]("var_man_store_var_id", DbType.stringIdentifier)

    def * = id ~ macroId ~ subjectId ~ graphId ~ text ~ isStart ~ isEnd ~ nodeType ~ isDisabled ~
      isMajorStartNode ~ channelId ~ variableId ~ optionMessageId ~ optionSubjectId ~
      optionCorrelationId ~ optionChannelId ~ optionNodeId ~ executeMacroId ~ varManVar1Id ~
      varManVar2Id ~ varManOperation ~ varManStoreVarId <> (GraphNode, GraphNode.unapply _)

    def pk = primaryKey(pkName, (id, macroId, subjectId, graphId))

    def nodeMacro =
      foreignKey(fkName("graph_macros"), (macroId, subjectId, graphId), GraphMacros)(m => (m.id, m.subjectId, m.graphId), NoAction, Cascade)
    def channel =
      foreignKey(fkName("graph_channels"), (channelId, graphId), GraphChannels)(c => (c.id, c.graphId))
    def variable =
      foreignKey(fkName("graph_variables"), (variableId, subjectId, graphId), GraphVariables)(v => (v.id, v.subjectId, v.graphId))
    def optionMessage =
      foreignKey(fkName("graph_messages_opt"), (optionMessageId, graphId), GraphMessages)(m => (m.id, m.graphId))
    def optionSubject =
      foreignKey(fkName("graph_subjects_opt"), (optionSubjectId, graphId), GraphSubjects)(s => (s.id, s.graphId))
    def optionChannel =
      foreignKey(fkName("graph_channels_opt"), (optionChannelId, graphId), GraphChannels)(c => (c.id, c.graphId))
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