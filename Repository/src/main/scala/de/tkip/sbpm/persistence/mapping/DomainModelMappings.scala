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

import de.tkip.sbpm.model.MergedSubject
import de.tkip.sbpm.persistence.schema.GraphMergedSubjectsSchema.GraphMergedSubjects
import de.tkip.sbpm.{ model => domainModel }
import shapeless._

/**
 * Methods to convert domain model objects (defined in sbmp.model package)
 * to database entities (defined in Models.scala).
 */
object DomainModelMappings {

  /**
   * Convert the graph domain model to db entities.
   * Disassembles object tree into relational records.
   * If id of graph is None only graph itself is converted
   * id must be known for converting sub entities.
   */
  def convert(g: domainModel.Graph): Either[Graph, (Graph, Seq[GraphMergedSubject], Seq[GraphConversation], Seq[GraphMessage], Seq[GraphSubject], Seq[GraphVariable], Seq[GraphMacro], Seq[GraphNode], Seq[GraphVarMan], Seq[GraphEdge])] = {
    val graph = Graph(g.id)
    if (!g.id.isDefined) {
      Left(graph)
    } else {
      val (subjects, mergedSubjects, variables, macros, nodes, varMans, edges) = extractSubjects(g.subjects.values, g.id.get)
      val conversations = extractConversations(g.conversations.values, g.id.get)
      val messages = extractMessages(g.messages.values, g.id.get)
        Right((graph, mergedSubjects, conversations, messages, subjects, variables, macros, nodes, varMans, edges))
    }
  }

  /**
   * Disassembles object trees of given subjects into relational entities.
   * Returns subjects, variables, macros, nodes and edges.
   */
  private def extractSubjects(ss: Iterable[domainModel.GraphSubject],
                              graphId: Int): (Seq[GraphSubject], Seq[GraphMergedSubject], Seq[GraphVariable], Seq[GraphMacro], Seq[GraphNode], Seq[GraphVarMan], Seq[GraphEdge]) = ss.map { s =>
    val subject = GraphSubject(s.id,
      graphId,
      s.name,
      s.subjectType,
      s.isDisabled,
      s.isStartSubject.getOrElse(false),
      s.inputPool,
      s.blackboxname,
      s.relatedSubjectId,
      s.relatedInterfaceId,
      s.isImplementation,
      s.externalType,
      s.role,
      s.comment)
    val mergedSubjects = s.mergedSubjects.map(_.toSeq).getOrElse(Seq()).map { ms : MergedSubject =>
      GraphMergedSubject(id = ms.id, subjectId = s.id, graphId = graphId, name = ms.name)
    }
    val variables = extractVariables(s.variables.values, s.id, graphId)
    val (macros, nodes, varMans, edges) = extractMacros(s.macros.values, s.id, graphId)
    (subject, mergedSubjects, variables, macros, nodes, varMans, edges)
  }.foldLeft((List[GraphSubject](), List[GraphMergedSubject](), List[GraphVariable](), List[GraphMacro](), List[GraphNode](), List[GraphVarMan](), List[GraphEdge]())) { (agg, t) =>
    // aggregate all entities for all subjects into flat lists
    (agg._1 :+ t._1, agg._2 ++ t._2, agg._3 ++ t._3, agg._4 ++ t._4, agg._5 ++ t._5, agg._6 ++ t._6, agg._7 ++ t._7)
  }

  /**
   * Convert variables into relational entities.
   */
  private def extractVariables(vs: Iterable[domainModel.GraphVariable], subjectId: String, graphId: Int) = vs.map { v =>
    GraphVariable(v.id, subjectId, graphId, v.name)
  }.toSeq

  /**
   * Disassembles object trees of given macros into relational entities.
   * Returns macros, nodes and edges.
   */
  private def extractMacros(ms: Iterable[domainModel.GraphMacro], subjectId: String, graphId: Int): (Seq[GraphMacro], Seq[GraphNode], Seq[GraphVarMan], Seq[GraphEdge]) = ms.map { m =>
    val graphMacro = GraphMacro(m.id, subjectId, graphId, m.name)
    val (nodes, varMans) = extractNodes(m.nodes.values, m.id, subjectId, graphId).unzip[GraphNode, GraphVarMan]
    val edges = extractEdges(m.edges, m.id, subjectId, graphId)
    (graphMacro, nodes, varMans, edges)
  }.foldLeft((List[GraphMacro](), List[GraphNode](), List[GraphVarMan](), List[GraphEdge]())) { (agg, t) =>
    (agg._1 :+ t._1, agg._2 ++ t._2, agg._3 ++ t._3, agg._4 ++ t._4)
  }

  /**
   * Disassembles object trees of given nodes into relational entities.
   */
  private def extractNodes(ns: Iterable[domainModel.GraphNode], macroId: String, subjectId: String, graphId: Int): Seq[(GraphNode, GraphVarMan)] = ns.map { n =>
    // disassemble varMan object
    val (varManVar1Id, varManVar2Id, varManOperation, varManStoreVarId) =
      extractVarMan(n.varMan)
    (
      GraphNode(n.id,
        macroId,
        subjectId,
        graphId,
        n.text,
        n.isStart,
        n.isEnd,
        n.nodeType,
        n.manualPositionOffsetX,
        n.manualPositionOffsetY,
        n.isAutoExecute,
        n.isDisabled,
        n.isMajorStartNode,
        n.conversationId,
        n.variableId,
        n.options.messageId,
        n.options.subjectId,
        n.options.correlationId,
        n.options.conversationId,
        n.options.nodeId,
        n.chooseAgentSubject,
        n.macroId,
        n.blackboxname)
      ,
      GraphVarMan(n.id,
        macroId,
        subjectId,
        graphId,
        varManVar1Id,
        varManVar2Id,
        varManOperation,
        varManStoreVarId
      )
      )
  }.toSeq

  /**
   * Convert VarMan object for node into single Option values,
   * because they are saved directly in nodes table.
   */
  private def extractVarMan(vm: Option[domainModel.GraphVarMan]): (Option[String], Option[String], Option[String], Option[String]) =
    if (vm.isDefined)
      (Some(vm.get.var1Id), Some(vm.get.var2Id), Some(vm.get.operation), Some(vm.get.storeVarId))
    else
      (None, None, None, None)

  /**
   * Disassembles object trees of given edges into relational entities.
   */
  private def extractEdges(es: Iterable[domainModel.GraphEdge], macroId: String, subjectId: String, graphId: Int): Seq[GraphEdge] = es.map { e =>
    val (targetSubjectId, exchangeOriginId, exchangeTargetId, targetMin, targetMax, targetCreateNew, targetVariableId) =
      extractTarget(e.target)
    GraphEdge(e.startNodeId,
      e.endNodeId,
      macroId,
      subjectId,
      exchangeOriginId,
      graphId,
      e.text,
      e.edgeType,
      e.manualPositionOffsetLabelX,
      e.manualPositionOffsetLabelY,
      targetSubjectId,
      exchangeTargetId,
      targetMin,
      targetMax,
      targetCreateNew,
      targetVariableId,
      e.isDisabled,
      e.isOptional,
      e.priority,
      e.manualTimeout,
      e.variableId,
      e.correlationId,
      e.comment,
      e.transportMethod.head)
  }.toSeq

  /**
   * Convert Target object for edge into single Option values,
   * because they are saved directly in edges table.
   */
  private def extractTarget(et: Option[domainModel.GraphEdgeTarget]): (Option[String], Option[String], Option[String], Option[Short], Option[Short], Option[Boolean], Option[String]) =
    if (et.isDefined) {
      val etg = et.get
      (Some(et.get.subjectId), etg.exchangeOriginId, etg.exchangeTargetId, Some(etg.min), Some(etg.max), Some(etg.createNew), etg.variableId)
    } else {
      (None, None, None, None, None, None, None)
    }

  /**
   * Convert conversation objects to relational entities.
   */
  private def extractConversations(cs: Iterable[domainModel.GraphConversation], graphId: Int): Seq[GraphConversation] = cs.map { c =>
    GraphConversation(c.id, graphId, c.name)
  }.toSeq

  /**
   * Convert message objects to relational entities.
   */
  private def extractMessages(cs: Iterable[domainModel.GraphMessage], graphId: Int): Seq[GraphMessage] = cs.map { c =>
    GraphMessage(c.id, graphId, c.name)
  }.toSeq

  def convertInterface(interface: Interface,
                       address: ProcessEngineAddress,
                       subModels: (Seq[GraphMergedSubject],
                                   Seq[GraphConversation], Seq[GraphMessage],
                                   Seq[GraphSubject], Seq[GraphVariable],
                                   Seq[GraphMacro], Seq[GraphNode],
                                   Seq[GraphVarMan], Seq[GraphEdge])
                       ) : domainModel.Interface = {
    domainModel.Interface(
      interfaceType = domainModel.InterfaceType.withName(interface.interfaceType),
      id = interface.id,
      processId = interface.processId,
      name = interface.name,
      address = domainModel.Address(
        id = address.id,
        ip = address.ip,
        port = address.port
      ),
      graph = convertGraph(Graph(id = Some(interface.graphId)), subModels)
    )
  }

  /**
   * Convert all database entities of a graph to a single
   * object tree of domain model objects. 
   */
  def convertGraph(graph: Graph,
              subModels: (Seq[GraphMergedSubject],
                          Seq[GraphConversation], Seq[GraphMessage],
                          Seq[GraphSubject], Seq[GraphVariable],
                          Seq[GraphMacro], Seq[GraphNode],
                          Seq[GraphVarMan], Seq[GraphEdge])
              ): domainModel.Graph = {
    val (mergedSubjects, conversations, messages, subjects, variables, macros, nodes, varMans, edges) = subModels
    domainModel.Graph(
      graph.id,
      conversations.map(convert).toMap,
      messages.map(convert).toMap,
      convertSubjects(subjects, mergedSubjects, variables, macros, nodes, varMans, edges))
  }

  /**
   * Convert  to id -> entity mapping.
   */
  def convert(c: GraphConversation): (String, domainModel.GraphConversation) =
    c.id -> domainModel.GraphConversation(c.id, c.name)

  /**
   * Convert  to id -> entity mapping.
   */
  def convert(a: ProcessEngineAddress): domainModel.Address =
    domainModel.Address(id = a.id, ip = a.ip, port = a.port)

  /**
   * Convert message to id -> entity mapping.
   */
  def convert(m: GraphMessage): (String, domainModel.GraphMessage) =
    m.id -> domainModel.GraphMessage(m.id, m.name)

  /**
   * Convert subjects with sub entities to domain model
   * object trees.
   */
  def convertSubjects(ss: Seq[GraphSubject],
                      mergedSubjects: Seq[GraphMergedSubject],
                      vs: Seq[GraphVariable],
                      ms: Seq[GraphMacro],
                      ns: Seq[GraphNode],
                      vm: Seq[GraphVarMan],
                      es: Seq[GraphEdge]) : Map[String, domainModel.GraphSubject] = ss.map { s =>
    // group variables, macros, nodes and edges by subject
    val variables = vs.groupBy(_.subjectId).mapValues(_.map(convert).toMap)
    val macros = ms.groupBy(_.subjectId)
    val nodes = ns.groupBy(_.subjectId)
    val varMans = vm.groupBy(_.subjectId)
    val edges = es.groupBy(_.subjectId)
    val convertedMergedSubjects = Some(mergedSubjects.filter(_.subjectId == s.id).map{sub : GraphMergedSubject => convertMS(sub)})

    s.id -> domainModel.GraphSubject(
      id = s.id,
      name = s.name,
      subjectType = s.subjectType,
      mergedSubjects = convertedMergedSubjects,
      isDisabled = s.isDisabled,
      isStartSubject = Some(s.isStartSubject),
      inputPool = s.inputPool,
      blackboxname = s.blackboxname,
      relatedSubjectId = s.relatedSubjectId,
      relatedInterfaceId = s.relatedInterfaceId,
      isImplementation = s.isImplementation,
      externalType = s.externalType,
      role = s.role,
      implementations = List.empty,
      comment = s.comment,
      variables = variables.getOrElse(s.id, Map()),
      macros = convert(macros.getOrElse(s.id, List()), nodes.getOrElse(s.id, List()), varMans.getOrElse(s.id, List()), edges.getOrElse(s.id, List())))
  }.toMap

  def convertMS(ms: GraphMergedSubject) : MergedSubject = {
    MergedSubject(id = ms.id, name = ms.name)
  }

  /**
   * Convert variable to id -> entity mapping.
   */
  def convert(v: GraphVariable): (String, domainModel.GraphVariable) =
    v.id -> domainModel.GraphVariable(v.id, v.name)

  /**
   * Convert macros, nodes and edges to domain model
   * object trees.
   * Returns id -> entity map.
   */
  def convert(ms: Seq[GraphMacro], ns: Seq[GraphNode], vm: Seq[GraphVarMan], es: Seq[GraphEdge]): Map[String, domainModel.GraphMacro] = {
    // group nodes and edges by it's macros
    val nodes = ns.groupBy(_.macroId).mapValues(_.map(x => {
      val filteredVarMans = vm.filter(t => t.id == x.id && t.macroId == x.macroId && t.subjectId == x.subjectId)
      val varMan:Option[GraphVarMan] =  if (filteredVarMans.length == 0) None else Some(filteredVarMans(0))
      convert(x, varMan)
    }).toMap)
    val edges = es.groupBy(_.macroId).mapValues(_.map(convert))

    ms.map { m =>
      m.id -> domainModel.GraphMacro(
        m.id,
        m.name,
        nodes.getOrElse(m.id, Map()),
        edges.getOrElse(m.id, List()))
    }.toMap
  }

  /**
   * Convert node to domain model.
   * Returns id -> entity mapping
   */
  def convert(n: GraphNode, vm: Option[GraphVarMan]): (Short, domainModel.GraphNode) = {
    // create list of varMan properties to check easily if all are defined
    val graphVarMan = vm match {
      case Some(GraphVarMan(_, _, _, _, varManVar1Id, varManVar2Id, varManOperation, varManStoreVarId)) => {
        val graphVarManList = List(varManVar1Id, varManVar2Id, varManOperation, varManStoreVarId)
        // if not all props are defined then varMan is None
        if (graphVarManList.forall(_.isDefined))
          Some(domainModel.GraphVarMan(
            vm.get.varManVar1Id.get,
            vm.get.varManVar2Id.get,
            vm.get.varManOperation.get,
            vm.get.varManStoreVarId.get))
        else
          None
      }
      case null => None
    }

    (n.id, domainModel.GraphNode(
      n.id,
      n.text,
      n.isStart,
      n.isEnd,
      n.nodeType,
      n.manualPositionOffsetX,
      n.manualPositionOffsetY,
      n.isAutoExecute,
      n.isDisabled,
      n.isMajorStartNode,
      n.conversationId,
      n.variableId,
      domainModel.GraphNodeOptions(
        n.optionMessageId,
        n.optionSubjectId,
        n.optionCorrelationId,
        n.optionConversationId,
        n.optionNodeId),
      n.chooseAgentSubject,
      n.executeMacroId,
      n.blackboxname,
      graphVarMan))
  }

  /**
   * Convert edge to domain model.
   */
  def convert(e: GraphEdge): domainModel.GraphEdge = {
    // create list of target properties to check easily if all are defined
    val targetList = List[Option[Any]](e.targetSubjectId, e.targetMin, e.targetMax, e.targetCreateNew)
    // if not all props are defined then target is None 
    val target =
      if (targetList.forall(_.isDefined))
        Some(domainModel.GraphEdgeTarget(
          e.targetSubjectId.get,
          e.originalSubjectId,
          e.exchangeTargetId,
          e.targetMin.get,
          e.targetMax.get,
          e.targetCreateNew.get,
          e.targetVariableId))
      else
        None

    domainModel.GraphEdge(e.startNodeId,
      e.endNodeId,
      e.text,
      e.edgeType,
      e.manualPositionOffsetLabelX,
      e.manualPositionOffsetLabelY,
      target,
      e.isDisabled,
      e.isOptional,
      e.priority,
      e.manualTimeout,
      e.variableId,
      e.correlationId,
      e.comment,
      // only one transport method supported currently
      Array(e.transportMethod))
  }
}
