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

package de.tkip.sbpm.persistence.mapping

/*
 * Define all database entities here.
 * These entities are converted to domain model entities
 * and vice versa when communicating via PersistenceActor. 
 */

case class Configuration(key: String,
    label: Option[String],
    value: Option[String],
    dataType: String)

case class Graph(id: Option[Int],
  processId: Int,
  date: java.sql.Timestamp)

case class Group(id: Option[Int],
  name: String,
  isActive: Boolean = true)

case class GroupRole(groupId: Int, roleId: Int)

case class GroupUser(groupId: Int, userId: Int)

case class GraphConversation(id: String,
  graphId: Int,
  name: String)

case class ProcessInstance(id: Option[Int],
  processId: Int,
  graphId: Int,
  data: Option[String] = None)

case class Process(id: Option[Int],
  name: String,
  isCase: Boolean = false)

case class Message(id: Option[Int],
  fromUserId: Int,
  toUserId: Int,
  processInstanceId: Int,
  isRead: Boolean,
  data: String,
  date: java.sql.Timestamp)

case class ProcessActiveGraph(processId: Int, graphId: Int)

case class User(id: Option[Int],
  name: String,
  isActive: Boolean = true,
  inputPoolSize: Int = 8,
  gdriveId: String)

case class UserIdentity(userId: Int,
  provider: String,
  eMail: String,
  password: Option[String])

case class Role(id: Option[Int],
  name: String,
  isActive: Boolean = true)

case class GraphMessage(id: String,
  graphId: Int,
  name: String)

case class GraphRouting(id: String,
  graphId: Int,
  conditionSubjectId: String,
  conditionOperator: Boolean = true,
  conditionGroupId: Option[Int],
  conditionUserId: Option[Int],
  implicationSubjectId: String,
  implicationOperator: Boolean = true,
  implicationGroupId: Option[Int],
  implicationUserId: Option[Int])

case class GraphSubject(id: String,
  graphId: Int,
  name: String,
  subjectType: String,
  isDisabled: Boolean,
  isStartSubject: Boolean,
  inputPool: Short,
  relatedSubjectId: Option[String],
  relatedGraphId: Option[Int],
  externalType: Option[String],
  roleId: Option[Int],
  comment: Option[String])

case class GraphVariable(id: String,
  subjectId: String,
  graphId: Int,
  name: String)

case class GraphMacro(id: String,
  subjectId: String,
  graphId: Int,
  name: String)

case class GraphNode(id: Short,
  macroId: String,
  subjectId: String,
  graphId: Int,
  text: String,
  isStart: Boolean,
  isEnd: Boolean,
  nodeType: String,
  isDisabled: Boolean,
  isMajorStartNode: Boolean,
  conversationId: Option[String],
  variableId: Option[String],
  optionMessageId: Option[String],
  optionSubjectId: Option[String],
  optionCorrelationId: Option[String],
  optionConversationId: Option[String],
  optionNodeId: Option[Short],
  executeMacroId: Option[String],
  varManVar1Id: Option[String],
  varManVar2Id: Option[String],
  varManOperation: Option[String],
  varManStoreVarId: Option[String])

case class GraphEdge(startNodeId: Short,
  endNodeId: Short,
  macroId: String,
  subjectId: String,
  graphId: Int,
  text: String,
  edgeType: String,
  targetSubjectId: Option[String],
  targetMin: Option[Short],
  targetMax: Option[Short],
  targetCreateNew: Option[Boolean],
  targetVariableId: Option[String],
  isDisabled: Boolean,
  isOptional: Boolean,
  priority: Byte,
  manualTimeout: Boolean,
  variableId: Option[String],
  correlationId: Option[String],
  comment: Option[String],
  transportMethod: String)