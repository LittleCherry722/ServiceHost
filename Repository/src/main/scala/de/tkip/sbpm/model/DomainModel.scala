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

object InterfaceType extends Enumeration {
  type InterfaceType = Value

  val BlackboxcontentTypeString = "blackboxcontent"
  val InterfaceTypeString = "interface"

  val BlackboxcontentInterfaceType = Value(BlackboxcontentTypeString)
  val InterfaceInterfaceType = Value(InterfaceTypeString)
}


import InterfaceType.InterfaceType
import de.tkip.sbpm.newmodel.ProcessModelTypes.SubjectId

case class Configuration(key: String,
                         label: Option[String],
                         value: Option[String],
                         dataType: String)

case class Interface(interfaceType: InterfaceType,
                     address: Address,
                     id: Option[Int],
                     processId: Int,
                     name: String,
                     views: Map[SubjectId, View])

case class View(mainSubjectId: SubjectId,
                implementations: Seq[InterfaceImplementation],
                graph: Graph)

case class IntermediateInterface(interfaceType: InterfaceType,
                                 processId: Int,
                                 port: Int,
                                 interfaceId: Option[Int],
                                 name: String,
                                 views: Map[SubjectId, View])

case class InterfaceImplementation(processId: Int,
                                   address: Address,
                                   ownSubjectId: String,
                                   subjectIdMap: Map[SubjectId, SubjectId],
                                   messageIdMap: Map[String, String] )


case class Address(id: Option[Int], ip: String, port: Int)

case class Graph(id: Option[Int],
                 conversations: Map[String, GraphConversation],
                 messages: Map[String, GraphMessage],
                 subjects: Map[String, GraphSubject]) {

  lazy val startSubject = subjects.values.find(_.isStartSubject.getOrElse(false))

  // creates a set of message transitions (from, msg, to) that very roughly
  // describes the interaction between subjects in this graph.
  // Useful for quick sanity checks if two graphs could have an equivalent behavior.
  lazy val staticInterface = subjects.values.flatMap{s =>
    val transitions = s.transitions
    transitions.filter(t => t.isReceive || t.isSend).flatMap { t =>
      if (t.isReceive) {
        t.edge.target.map (target => (target.subjectId, t.edge.text, s.id) )
      } else if (t.isSend) {
        t.edge.target.map (target => (s.id, t.edge.text, target.subjectId) )
      } else {
        None
      }
    }
  }.toSet
}

case class GraphTransition(fromNode: GraphNode
                           ,toNode: GraphNode
                           ,edge: GraphEdge
                           ,mId: String = "##main##"
                           ,mName: String = "internal behavior") {
  lazy val isReceive = fromNode.isReceive
  lazy val isSend = fromNode.isSend
  lazy val isModal = fromNode.isModalJoin || fromNode.isModalSplit
  lazy val isVarMan = fromNode.isVarMan
  lazy val isChooseAgent = fromNode.isChooseAgent

  lazy val saveToVariable = edge.variableId.filterNot(_.isEmpty)

  lazy val targetSubjectId: Option[SubjectId] = edge.target.map{target: GraphEdgeTarget => target.subjectId}

  def usesVariable(variable: String): Boolean = {
    fromNode.variableId.contains(variable) ||
      fromNode.varMan.exists{v => Seq(v.var1Id, v.var2Id, v.storeVarId).contains(variable) } ||
      edge.target.exists(_.variableId.contains(variable))
  }
  val communicationPartner =
    if (isSend || isReceive) {
      edge.target.map(_.subjectId)
    } else {
      None
    }

  def interactsWith(subject: GraphSubject): Boolean = {
    (isSend || isReceive) && edge.target.map(_.subjectId).contains(subject.id)
  }
  def interactsWith(subject: SubjectId): Boolean = {
    (isSend || isReceive) && edge.target.map(_.subjectId).contains(subject)
  }
}

case class GraphConversation(id: String, name: String)

case class GraphMessage(id: String, name: String)

case class MergedSubject(id: String, name: String)

case class GraphSubject(id: String,
                        name: String,
                        subjectType: String,
                        mergedSubjects: Option[Seq[MergedSubject]],
                        isDisabled: Boolean,
                        isStartSubject: Option[Boolean],
                        inputPool: Short,
                        blackboxname: Option[String],
                        relatedSubjectId: Option[String],
                        relatedInterfaceId: Option[Int],
                        isImplementation: Option[Boolean],
                        externalType: Option[String],
                        role: Option[String],
                        comment: Option[String],
                        variables: Map[String, GraphVariable],
                        macros: Map[String, GraphMacro]) extends Ordered[GraphSubject] {

  def compare(that: GraphSubject): Int = this.id compare that.id

  // Incoming messages as (SubjectId, MsgId) tuple
  lazy val inCom: Set[(String, String)] = transitions.filter{t => t.isReceive}
    .flatMap(trans => trans.edge.target.map(t => (t.subjectId, trans.edge.text))).toSet

  // Outgoing messages as (SubjectId, MsgId) tuple
  lazy val outCom: Set[(String, String)] = transitions.filter{t => t.isSend}
    .flatMap(trans => trans.edge.target.map(t => (t.subjectId, trans.edge.text))).toSet

  lazy val outDegree: Int = outCom.size
  lazy val inDegree: Int = inCom.size

  // Set of SubjectIds this subject interacts with (sending or receiving messages)
  lazy val neighborSubjectIds: Set[SubjectId] = transitions.filter{t => t.isSend}
    .flatMap(_.edge.target.map(_.subjectId))
    .filter(_ != id).toSet

  // State Transitions flattened over all macros as a List of GraphTransitions.
  lazy val transitions: Seq[GraphTransition] = {
    val ts = for {
      makro <- macros.values
      edge <- makro.edges
    } yield GraphTransition(
        fromNode = makro.nodes(edge.startNodeId)
        ,toNode = makro.nodes(edge.endNodeId)
        ,edge = edge
        ,mId = makro.id
        ,mName = makro.name)
    ts.toSeq
  }
}

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
                     varMan: Option[GraphVarMan]) {

  // Convenience variables for easy node type comparisons
  val isVarMan = nodeType == StateType.VariableManipulationString
  val isBlackbox = nodeType == StateType.BlackboxStateString
  val isTau = nodeType == StateType.TauStateString
  val isMacro = nodeType == StateType.MacroStateString
  val isArchive = nodeType == StateType.ArchiveStateString
  val isSplitGuard = nodeType == StateType.SplitGuardStateString
  val isModalJoin = nodeType == StateType.ModalJoinStateString
  val isModalSplit = nodeType == StateType.ModalSplitStateString
  val isChooseAgent = nodeType == StateType.ChooseAgentStateString
  val isDecision = nodeType == StateType.DecisionStateString
  val isDeactivate = nodeType == StateType.DeactivateStateString
  val isActivate = nodeType == StateType.ActivateStateString
  val isIsIpEmpty = nodeType == StateType.IsIPEmptyStateString
  val isOpenIp = nodeType == StateType.OpenIPStateString
  val isCloseIp = nodeType == StateType.CloseIPStateString
  val isReceive = nodeType == StateType.ReceiveStateString
  val isSend = nodeType == StateType.SendStateString
  val isAct = nodeType == StateType.ActStateString
}

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
