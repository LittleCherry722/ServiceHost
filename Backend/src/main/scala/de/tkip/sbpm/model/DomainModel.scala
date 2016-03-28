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

import de.tkip.sbpm.application.history._
import de.tkip.sbpm.application.subject.misc.AgentAddress
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.{MessageID, UserID}
import de.tkip.sbpm.application.subject.misc.{ActionData, AvailableAction}
import de.tkip.sbpm.newmodel.ProcessModelTypes.SubjectId
import de.tkip.sbpm.anonymization.Anonymizer.createView
import de.tkip.sbpm.verification.ModelConverter.verifyGraph

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
                   interfaceId: Option[Int],
                   verificationErrors: Seq[String],
                   publishInterface: Boolean,
                   name: String,
                   subjectMap: Map[Int, Map[String, String]], // Map from viewId to a map of SubjectId from/to mapping
                   messageMap: Map[Int, Map[String, String]], // Map from viewId to a map of Message id from/to mapping
                   implementationIds: Seq[Int],
                   isCase: Boolean = false,
                   startAble: Option[Boolean] = None,
                   activeGraphId: Option[Int] = None
                  )

case class UserToUserMessage(id: Option[Int], fromUser: UserID, toUser: UserID, title: String, isRead: Boolean, content: String, date: java.sql.Timestamp)

object SubjectType {
  val ExternalSubjectType = "external"
  val SingleSubjectType = "single"
  val MultiSubjectType = "multi"
  val MultiExternalSubjectType = "multiexternal"
}

object SubjectExternalType {
  val ExternalSubjectdType = "external"
  val InterfaceSubjectType = "interface"
  val InstantInterfaceSubjectType = "instantinterface"
  val BlackBoxSubjectType = "blackbox"
}

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

case class MessageChange(message: UserToUserMessage, info: String, date: java.util.Date) extends MessageChangeData

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

case class InterfaceImplementation(viewId: Int,
                                   dependsOnInterface: Option[Int],
                                   ownAddress: AgentAddress,
                                   ownProcessId: Int,
                                   ownSubjectId: String)

case class View(id: Option[Int],
                mainSubjectId: SubjectId,
                implementations: Seq[InterfaceImplementation],
                graph: Graph)

case class Graph(id: Option[Int],
                 processId: Option[Int],
                 date: java.sql.Timestamp,
                 conversations: Map[String, GraphConversation],
                 messages: Map[String, GraphMessage],
                 subjects: Map[SubjectId, GraphSubject],
                 routings: Seq[GraphRouting]) {

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


  lazy val views: Either[Seq[String], Map[SubjectId, View]] = {
    verifyGraph(this).right.flatMap { vg =>
      val viewSubjects =  subjects.values.filterNot{s => s.isExternalView || s.subjectType == SubjectType.SingleSubjectType}
      val errorsOrViews: Seq[Either[String, (SubjectId, View)]] = viewSubjects.map { s =>
        createView(s.id, vg).right.map { v => (s.id, v) }
      }.toSeq
      val errors: Seq[String] = errorsOrViews.flatMap{eov => eov.left.toOption}
      val views: Map[SubjectId, View] = errorsOrViews.flatMap{eov => eov.right.toOption}.toMap
      if (errors.nonEmpty) {
        Left(errors)
      } else if(views.isEmpty) {
        Left(Seq("No views available for Graph"))
      } else {
        Right(views)
      }
    }
  }
}

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

case class GraphSubject(id: String,
                        name: String,
                        subjectType: String,
                        mergedSubjects: Option[List[MergedSubject]],
                        isDisabled: Boolean,
                        isStartSubject: Option[Boolean],
                        inputPool: Short,
                        blackboxname: Option[String],
                        implementsViews: Option[List[Int]],
                        viewId: Option[Int],
                        isExternalView: Boolean,
                        externalType: Option[String],
                        role: Option[Role],
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
                     text: String = "",
                     isStart: Boolean = false,
                     isEnd: Boolean = false,
                     nodeType: String,
                     manualPositionOffsetX: Option[Short] = None,
                     manualPositionOffsetY: Option[Short] = None,
                     isAutoExecute: Option[Boolean] = Some(false),
                     isDisabled: Boolean = false,
                     isMajorStartNode: Boolean = false,
                     conversationId: Option[String] = None,
                     variableId: Option[String] = None,
                     options: GraphNodeOptions = GraphNodeOptions(),
                     chooseAgentSubject: Option[String] = None,
                     macroId: Option[String] = None,
                     blackboxname: Option[String] = None,
                     varMan: Option[GraphVarMan] = None) {

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
                     manualPositionOffsetLabelX: Option[Short] = None,
                     manualPositionOffsetLabelY: Option[Short] = None,
                     target: Option[GraphEdgeTarget] = None,
                     isDisabled: Boolean = false,
                     isOptional: Boolean = false,
                     priority: Byte = 1,
                     manualTimeout: Boolean = false,
                     variableId: Option[String] = None,
                     correlationId: Option[String] = None,
                     comment: Option[String] = None,
                     transportMethod: Seq[String] = Seq("internal"))

case class GraphEdgeTarget(subjectId: String,
                           exchangeOriginId: Option[String] = None,
                           exchangeTargetId: Option[String] = None,
                           min: Short = -1,
                           max: Short = -1,
                           createNew: Boolean = false,
                           variableId: Option[String])
