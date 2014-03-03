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

import spray.json.{
  DefaultJsonProtocol,
  JsObject,
  RootJsonFormat,
  JsValue,
  DeserializationException,
  JsNumber
}

// Model for Administration
case class User(id: Option[Int], name: String, isActive: Boolean = true, inputPoolSize: Int = 8, gdriveId: String = "")

case class ProviderMail(provider: String, mail: String)

case class UserWithMail(var id: Option[Int], name: String, isActive: Boolean = true, inputPoolSize: Int = 8, providerMail: Seq[ProviderMail])

case class UserIdentity(user: User, provider: String, eMail: String, password: Option[String])

case class Role(id: Option[Int], name: String, isActive: Boolean = true)

case class Group(id: Option[Int], name: String, isActive: Boolean = true)

case class SetPassword(oldPassword: String, newPassword: String)

case class UserUpdate(name: String, isActive: Boolean = true, inputPoolSize: Int = 8)

// Model for DB Relations 
case class GroupRole(groupId: Int, roleId: Int)

case class GroupUser(groupId: Int, userId: Int)

case class Process(id: Option[Int],
                   interfaceId: Option[Int],
                   publishInterface: Boolean,
                   name: String,
                   isCase: Boolean = false,
                   startAble: Option[Boolean] = None,
                   activeGraphId: Option[Int] = None)

case class Configuration(key: String,
  label: Option[String],
  value: Option[String],
  dataType: String)

case class Interface(address: Address,
                     id: Int,
                     processId: Int,
                     name: String,
                     graph: Graph)

case class InterfaceImplementation(processId: Int,
                                   interfaceId: Int,
                                   address: Address,
                                   subjectId: String)

case class Address(ip: String, port: Int)

case class Graph(id: Option[Int],
  processId: Option[Int],
  date: java.sql.Timestamp,
  conversations: Map[String, GraphConversation],
  messages: Map[String, GraphMessage],
  subjects: Map[String, GraphSubject],
  routings: Seq[GraphRouting])

case class GraphConversation(id: String, name: String)

case class GraphMessage(id: String, name: String)

case class GraphRouting(id: String,
  condition: GraphRoutingExpression,
  implication: GraphRoutingExpression)

case class GraphRoutingExpression(subjectId: String,
  operator: Boolean = true,
  groupId: Option[Int],
  userId: Option[Int])

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
  implementations: List[InterfaceImplementation],
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
  isDisabled: Boolean,
  isMajorStartNode: Boolean,
  conversationId: Option[String],
  variableId: Option[String],
  options: GraphNodeOptions,
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
