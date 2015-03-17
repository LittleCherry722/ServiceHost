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

import java.sql.Timestamp
import java.util.UUID
import spray.json.{
DefaultJsonProtocol,
JsObject,
RootJsonFormat,
JsValue,
DeserializationException,
JsNumber
}
import de.tkip.sbpm.application.subject.misc.AvailableAction
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.history._
import de.tkip.sbpm.application.ProcessInstanceActor.AgentAddress
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.UserID

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

// Model for Modeling/Execution
case class ProcessInstance(id: Option[Int], processId: Int, graphId: Int, data: Option[String] = None)

case class Process(id: Option[Int],
                   uuid: UUID,
                   interfaceId: Option[Int],
                   publishInterface: Boolean,
                   name: String,
                   isCase: Boolean = false,
                   startAble: Option[Boolean] = None,
                   activeGraphId: Option[Int] = None)

case class Message(id: Option[Int], fromUser: UserID, toUser: UserID, title: String, isRead: Boolean, content: String, date: java.sql.Timestamp)

//case class Action(id: Option[Int], data: String) // TODO extend this case class to fit the requirements

// Model for changeAPI
object ChangeDataMode {
  type ChangeMode = String
  val Inserted = "insert"
  val Updated = "updated"
  val Deleted = "delete"
}

trait ChangeData {
  def date: java.util.Date
}

trait ProcessChangeData extends ChangeData

case class ProcessChange(process: Process, info: String, date: java.util.Date) extends ProcessChangeData

case class ProcessDelete(id: Int, date: java.util.Date) extends ProcessChangeData

trait ActionChangeData extends ChangeData

case class ActionChange(action: AvailableAction,
                        info: String,
                        date: java.util.Date)
  extends ActionChangeData

case class ActionDelete(id: Int, date: java.util.Date) extends ActionChangeData

trait ProcessInstanceChangeData extends ChangeData

case class ProcessInstanceChange(id: Int,
                                 processId: Int,
                                 processName: String,
                                 name: String,
                                 info: String,
                                 date: java.util.Date)
  extends ProcessInstanceChangeData

case class ProcessInstanceDelete(id: Int, date: java.util.Date) extends ProcessInstanceChangeData

trait MessageChangeData extends ChangeData

case class MessageChange(message: Message, info: String, date: java.util.Date) extends MessageChangeData

case class ProcessRelatedChangeData(id: Int,
                                    interfaceId: Option[Int],
                                    name: String,
                                    isCase: Boolean,
                                    startAble: Boolean,
                                    activeGraphId: Option[Int])

case class ProcessRelatedDeleteData(id: Int)

case class ProcessRelatedChange(inserted: Option[Array[ProcessRelatedChangeData]],
                                updated: Option[Array[ProcessRelatedChangeData]],
                                deleted: Option[Array[ProcessRelatedDeleteData]])

case class ActionRelatedChangeData(id: Int,
                                   userID: Int,
                                   processInstanceID: Int,
                                   subjectID: String,
                                   macroID: String,
                                   stateID: Int,
                                   stateText: String,
                                   stateType: String,
                                   actionData: Array[ActionData])

case class ActionRelatedDeleteData(id: Int)

case class ActionRelatedChange(inserted: Option[Array[ActionRelatedChangeData]],
                               updated: Option[Array[ActionRelatedChangeData]],
                               deleted: Option[Array[ActionRelatedDeleteData]])

case class HistoryRelatedChangeData(userId: Option[Int],
                                    process: NewHistoryProcessData,
                                    subject: Option[String],
                                    transitionEvent: Option[NewHistoryTransitionData],
                                    lifecycleEvent: Option[String],
                                    timeStamp: java.sql.Timestamp)

case class HistoryRelatedChange(inserted: Option[Array[HistoryRelatedChangeData]])

case class ProcessInstanceRelatedChangeData(id: Int, processId: Int, processName: String, name: String)

case class ProcessInstanceRelatedDeleteData(id: Int)

case class ProcessInstanceRelatedChange(inserted: Option[Array[ProcessInstanceRelatedChangeData]],
                                        updated: Option[Array[ProcessInstanceRelatedChangeData]],
                                        deleted: Option[Array[ProcessInstanceRelatedDeleteData]])

case class MessageRelatedChangeData(id: Option[Int],
                                    fromUser: Int,
                                    toUser: Int,
                                    title: String,
                                    isRead: Boolean,
                                    content: String,
                                    date: java.sql.Timestamp)

case class MessageRelatedChange(inserted: Option[Array[MessageRelatedChangeData]])

case class ChangeRelatedData(process: Option[ProcessRelatedChange],
                             processInstance: Option[ProcessInstanceRelatedChange],
                             action: Option[ActionRelatedChange],
                             history: Option[HistoryRelatedChange],
                             message: Option[MessageRelatedChange])


case class Configuration(key: String,
                         label: Option[String],
                         value: Option[String],
                         dataType: String)

case class Interface(// TODO: interfaceType
                     address: AgentAddress,
                     id: Option[Int],
                     processId: Int,
                     name: String,
                     graph: Graph)

case class InterfaceImplementation(processId: Int,
                                   address: AgentAddress,
                                   subjectId: String)

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

case class MergedSubject(id: String, name: String)

case class GraphSubject(id: String,
                        name: String,
                        subjectType: String,
                        mergedSubjects: Option[List[MergedSubject]],
                        isDisabled: Boolean,
                        isStartSubject: Option[Boolean],
                        inputPool: Short,
                        blackboxname: Option[String],
                        relatedProcessId: Option[String],
                        relatedSubjectId: Option[String],
                        relatedInterfaceId: Option[Int],
                        isImplementation: Option[Boolean],
                        externalType: Option[String],
                        role: Option[Role],
                        url: Option[String],
                        implementations: Option[List[InterfaceImplementation]],
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
                     blackboxname: Option[String],
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
                           exchangeOriginId: Option[String],
                           exchangeTargetId: Option[String],
                           min: Short = -1,
                           max: Short = -1,
                           createNew: Boolean,
                           variableId: Option[String])
