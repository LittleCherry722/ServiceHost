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

package de.tkip.sbpm.model

case class Configuration(key: String,
  label: Option[String],
  value: Option[String],
  dataType: String)

case class Interface(address: Address,
                     id: Option[Int],
                     processId: Int,
                     name: String,
                     graph: Graph)

case class IntermediateInterface(id: Int,
                                 port: Int,
                                 interfaceId: Option[Int],
                                 name: String,
                                 graph: Graph)

case class InterfaceImplementation(processId: Int,
                                   address: Address,
                                   subjectId: String)

case class Address(id: Option[Int], ip: String, port: Int)

case class Graph(id: Option[Int],
  conversations: Map[String, GraphConversation],
  messages: Map[String, GraphMessage],
  subjects: Map[String, GraphSubject])

case class GraphConversation(id: String, name: String)

case class GraphMessage(id: String, name: String)

case class GraphSubject(id: String,
  name: String,
  subjectType: String,
  isDisabled: Boolean,
  isStartSubject: Option[Boolean],
  inputPool: Short,
  relatedSubjectId: Option[String],
  relatedInterfaceId: Option[Int],
  isImplementation: Option[Boolean],
  externalType: Option[String],
  role: Option[String],
  implementations: Seq[InterfaceImplementation],
  comment: Option[String],
  variables: Map[String, GraphVariable],
  macros: Map[String, GraphMacro])

case class GraphVariable(id: String, name: String)

case class GraphMacro(id: String,
  name: String,
  nodes: Map[Short, GraphNode],
  edges: Seq[GraphEdge])

case class GraphNode(id: Short,
  text: String,
  isStart: Boolean,
  isEnd: Boolean,
  nodeType: String,
  manualPositionOffsetX: Option[Short],
  manualPositionOffsetY: Option[Short],
  isAutoExecute: Option[Boolean],
  isDisabled: Boolean,
  isMajorStartNode: Boolean,
  conversationId: Option[String],
  variableId: Option[String],
  options: GraphNodeOptions,
  chooseAgentSubject: Option[String],
  macroId: Option[String],
  varMan: Option[GraphVarMan])

case class GraphNodeOptions(messageId: Option[String] = None,
  subjectId: Option[String] = None,
  correlationId: Option[String] = None,
  conversationId: Option[String] = None,
  nodeId: Option[Short] = None)

object GraphNodeOptions {
  val AllMessages = "##all##"
  val AllSubjects = "##all##"
}

case class GraphVarMan(var1Id: String,
  var2Id: String,
  operation: String,
  storeVarId: String)

case class GraphEdge(startNodeId: Short,
  endNodeId: Short,
  text: String,
  edgeType: String,
  manualPositionOffsetLabelX: Option[Short],
  manualPositionOffsetLabelY: Option[Short],
  target: Option[GraphEdgeTarget],
  isDisabled: Boolean,
  isOptional: Boolean,
  priority: Byte,
  manualTimeout: Boolean,
  variableId: Option[String],
  correlationId: Option[String],
  comment: Option[String],
  transportMethod: Seq[String])

case class GraphEdgeTarget(subjectId: String,
  min: Short = -1,
  max: Short = -1,
  createNew: Boolean,
  variableId: Option[String])
