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

import spray.json.DefaultJsonProtocol
import spray.json.JsObject
import spray.json.RootJsonFormat
import spray.json.JsValue
import java.sql.Timestamp
import spray.json.DeserializationException
import spray.json.JsNumber

// Model for Administration
case class User(id: Option[Int], name: String, isActive: Boolean = true, inputPoolSize: Int = 8)
case class ProviderMail(provider: String, mail: String)
case class UserWithMail(var id: Option[Int], name: String, isActive: Boolean = true, inputPoolSize: Int = 8, providerMail: Seq[ProviderMail])
case class UserIdentity(user: User, provider: String, eMail: String, password: Option[String])
case class Role(id: Option[Int], name: String, isActive: Boolean = true)
case class Group(id: Option[Int], name: String, isActive: Boolean = true)
case class Credentials(provider: String, oldEmail: String, newEmail: Option[String], oldPassword: String, newPassword: Option[String])
case class UserUpdate(var name: Option[String], var isActive: Option[Boolean], var inputPoolSize: Option[Int], newEmail: Option[String], oldPassword: String, newPassword: Option[String])

// Model for DB Relations 
case class GroupRole(groupId: Int, roleId: Int)
case class GroupUser(groupId: Int, userId: Int)

// Model for Modeling/Execution
case class ProcessInstance(id: Option[Int], processId: Int, graphId: Int, data: Option[String] = None)
case class Process(id: Option[Int], name: String, isCase: Boolean = false, activeGraphId: Option[Int] = None)
case class Message(id: Option[Int], from: Int, to: Int, instanceId: Int, isRead: Boolean, data: String, date: java.sql.Timestamp)
case class Action(id: Option[Int], data: String) // TODO extend this case class to fit the requirements

case class Configuration(key: String,
  label: Option[String],
  value: Option[String],
  dataType: String)

case class Graph(id: Option[Int],
  processId: Option[Int],
  date: java.sql.Timestamp,
  channels: Map[String, GraphChannel],
  messages: Map[String, GraphMessage],
  subjects: Map[String, GraphSubject],
  routings: Seq[GraphRouting])

case class GraphChannel(id: String, name: String)

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
  relatedGraphId: Option[Int],
  externalType: Option[String],
  role: Option[Role],
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
  isDisabled: Boolean,
  isMajorStartNode: Boolean,
  channelId: Option[String],
  variableId: Option[String],
  options: GraphNodeOptions,
  macroId: Option[String],
  varMan: Option[GraphVarMan])

case class GraphNodeOptions(messageId: Option[String] = None,
  subjectId: Option[String] = None,
  correlationId: Option[String] = None,
  channelId: Option[String] = None,
  nodeId: Option[Short] = None)

case class GraphVarMan(var1Id: String,
  var2Id: String,
  operation: String,
  storeVarId: String)

case class GraphEdge(startNodeId: Short,
  endNodeId: Short,
  text: String,
  edgeType: String,
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
