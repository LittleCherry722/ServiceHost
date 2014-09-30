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

case class Graph(id: Option[Int],
                 name: Option[String] = None)

case class GraphConversation(id: String,
                             graphId: Int,
                             name: String)

case class ProcessEngineAddress(id: Option[Int], ip: String, port: Int)

case class Interface(interfaceType: String,
                     id: Option[Int],
                     addressId: Int,
                     processId: Int,
                     graphId: Int,
                     name: String)

case class GraphMessage(id: String,
                        graphId: Int,
                        name: String)

case class GraphSubject(id: String,
                        graphId: Int,
                        name: String,
                        subjectType: String,
                        isDisabled: Boolean,
                        isStartSubject: Boolean,
                        inputPool: Short,
                        blackboxname: Option[String],
                        relatedSubjectId: Option[String],
                        relatedInterfaceId: Option[Int],
                        isImplementation: Option[Boolean],
                        externalType: Option[String],
                        role: Option[String],
                        comment: Option[String])

case class GraphMergedSubject(id: String,
                         subjectId: String,
                         graphId: Int,
                         name: String)

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
                     manualPositionOffsetX: Option[Short],
                     manualPositionOffsetY: Option[Short],
                     isAutoExecute: Option[Boolean],
                     isDisabled: Boolean,
                     isMajorStartNode: Boolean,
                     conversationId: Option[String],
                     variableId: Option[String],
                     optionMessageId: Option[String],
                     optionSubjectId: Option[String],
                     optionCorrelationId: Option[String],
                     optionConversationId: Option[String],
                     optionNodeId: Option[Short],
                     chooseAgentSubject: Option[String],
                     executeMacroId: Option[String],
                     blackboxname: Option[String])

case class GraphVarMan(id: Short,
                       macroId: String,
                       subjectId: String,
                       graphId: Int,
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
                     manualPositionOffsetLabelX: Option[Short],
                     manualPositionOffsetLabelY: Option[Short],
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
