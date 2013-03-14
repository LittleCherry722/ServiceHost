package de.tkip.sbpm.persistence.mapping
import de.tkip.sbpm.{ model => domainModel }
import shapeless._
import Traversables._
import Tuples._

object GraphMappings {

  def convert(g: domainModel.Graph): Either[Graph, (Graph, Seq[GraphChannel], Seq[GraphMessage], Seq[GraphRouting], Seq[GraphSubject], Seq[GraphVariable], Seq[GraphMacro], Seq[GraphNode], Seq[GraphEdge])] = {
    val graph = Graph(g.id, g.processId.get, g.date)
    if (!g.id.isDefined) {
      Left(graph)
    } else {
      val (subjects, variables, macros, nodes, edges) = extractSubjects(g.subjects.values, g.id.get)
      val channels = extractChannels(g.channels.values, g.id.get)
      val messages = extractMessages(g.messages.values, g.id.get)
      val routings = extractRoutings(g.routings, g.id.get)
      Right((graph, channels, messages, routings, subjects, variables, macros, nodes, edges))
    }
  }

  def extractSubjects(ss: Iterable[domainModel.GraphSubject], graphId: Int): (Seq[GraphSubject], Seq[GraphVariable], Seq[GraphMacro], Seq[GraphNode], Seq[GraphEdge]) = ss.map { s =>
    val subject = GraphSubject(s.id,
      graphId,
      s.name,
      s.subjectType,
      s.isDisabled,
      s.isStartSubject.getOrElse(false),
      s.inputPool,
      s.relatedSubjectId,
      s.relatedGraphId,
      s.externalType,
      if (s.role.isDefined) s.role.get.id else None,
      s.comment)
    val variables = extractVariables(s.variables.values, s.id, graphId)
    val (macros, nodes, edges) = extractMacros(s.macros.values, s.id, graphId)
    (subject, variables, macros, nodes, edges)
  }.foldLeft((List[GraphSubject](), List[GraphVariable](), List[GraphMacro](), List[GraphNode](), List[GraphEdge]())) { (agg, t) =>
    (agg._1 :+ t._1, agg._2 ++ t._2, agg._3 ++ t._3, agg._4 ++ t._4, agg._5 ++ t._5)
  }

  def extractVariables(vs: Iterable[domainModel.GraphVariable], subjectId: String, graphId: Int) = vs.map { v =>
    GraphVariable(v.id, subjectId, graphId, v.name)
  }.toSeq

  def extractMacros(ms: Iterable[domainModel.GraphMacro], subjectId: String, graphId: Int): (Seq[GraphMacro], Seq[GraphNode], Seq[GraphEdge]) = ms.map { m =>
    val macro = GraphMacro(m.id, subjectId, graphId, m.name)
    val nodes = extractNodes(m.nodes.values, m.id, subjectId, graphId)
    val edges = extractEdges(m.edges, m.id, subjectId, graphId)
    (macro, nodes, edges)
  }.foldLeft((List[GraphMacro](), List[GraphNode](), List[GraphEdge]())) { (agg, t) =>
    (agg._1 :+ t._1, agg._2 ++ t._2, agg._3 ++ t._3)
  }

  def extractNodes(ns: Iterable[domainModel.GraphNode], macroId: String, subjectId: String, graphId: Int): Seq[GraphNode] = ns.map { n =>
    val (varManVar1Id, varManVar2Id, varManOperation, varManStoreVarId) =
      extractVarMan(n.varMan)
    GraphNode(n.id,
      macroId,
      subjectId,
      graphId,
      n.text,
      n.isStart,
      n.isEnd,
      n.nodeType,
      n.isDisabled,
      n.isMajorStartNode,
      n.channelId,
      n.variableId,
      n.options.messageId,
      n.options.subjectId,
      n.options.correlationId,
      n.options.channelId,
      n.options.nodeId,
      n.macroId,
      varManVar1Id,
      varManVar2Id,
      varManOperation,
      varManStoreVarId)
  }.toSeq

  def extractVarMan(vm: Option[domainModel.GraphVarMan]): (Option[String], Option[String], Option[String], Option[String]) =
    if (vm.isDefined)
      (Some(vm.get.var1Id), Some(vm.get.var2Id), Some(vm.get.operation), Some(vm.get.storeVarId))
    else
      (None, None, None, None)

  def extractEdges(es: Iterable[domainModel.GraphEdge], macroId: String, subjectId: String, graphId: Int): Seq[GraphEdge] = es.map { e =>
    val (targetSubjectId, targetMin, targetMax, targetCreateNew, targetVariableId) =
      extractTarget(e.target)
    GraphEdge(e.startNodeId,
      e.endNodeId,
      macroId,
      subjectId,
      graphId,
      e.text,
      e.edgeType,
      targetSubjectId,
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

  def extractTarget(et: Option[domainModel.GraphEdgeTarget]): (Option[String], Option[Short], Option[Short], Option[Boolean], Option[String]) =
    if (et.isDefined)
      (Some(et.get.subjectId), Some(et.get.min), Some(et.get.max), Some(et.get.createNew), et.get.variableId)
    else
      (None, None, None, None, None)

  def extractChannels(cs: Iterable[domainModel.GraphChannel], graphId: Int): Seq[GraphChannel] = cs.map { c =>
    GraphChannel(c.id, graphId, c.name)
  }.toSeq

  def extractMessages(cs: Iterable[domainModel.GraphMessage], graphId: Int): Seq[GraphMessage] = cs.map { c =>
    GraphMessage(c.id, graphId, c.name)
  }.toSeq

  def extractRoutings(rs: Iterable[domainModel.GraphRouting], graphId: Int): Seq[GraphRouting] = rs.map { r =>
    GraphRouting(
      r.id,
      graphId,
      r.condition.subjectId,
      r.condition.operator,
      r.condition.groupId,
      r.condition.userId,
      r.implication.subjectId,
      r.implication.operator,
      r.implication.groupId,
      r.implication.userId)
  }.toSeq

  def convert(graph: Graph, subModels: (Seq[GraphChannel], Seq[GraphMessage], Seq[GraphRouting], Seq[GraphSubject], Seq[GraphVariable], Seq[GraphMacro], Seq[GraphNode], Seq[GraphEdge]), roles: Seq[Role]): domainModel.Graph = {
    val (channels, messages, routings, subjects, variables, macros, nodes, edges) = subModels
    domainModel.Graph(
      graph.id,
      Some(graph.processId),
      graph.date,
      channels.map(convert).toMap,
      messages.map(convert).toMap,
      convert(subjects, variables, macros, nodes, edges, roles.map(convert).toMap),
      routings.map(convert))
  }

  def convert(r: Role): (Int, domainModel.Role) = {
    import PrimitiveMappings._
    (r.id.get -> PrimitiveMappings.convert(r, Persistence.role, Domain.role))
  }

  def convert(c: GraphChannel): (String, domainModel.GraphChannel) =
    (c.id -> domainModel.GraphChannel(c.id, c.name))

  def convert(m: GraphMessage): (String, domainModel.GraphMessage) =
    (m.id -> domainModel.GraphMessage(m.id, m.name))

  def convert(r: GraphRouting): domainModel.GraphRouting = {
    val expr = domainModel.GraphRoutingExpression.apply _
    domainModel.GraphRouting(
      r.id,
      expr(r.conditionSubjectId, r.conditionOperator, r.conditionGroupId, r.conditionUserId),
      expr(r.implicationSubjectId, r.implicationOperator, r.implicationGroupId, r.implicationUserId))
  }

  def convert(ss: Seq[GraphSubject], vs: Seq[GraphVariable], ms: Seq[GraphMacro], ns: Seq[GraphNode], es: Seq[GraphEdge], roles: Map[Int, domainModel.Role]): Map[String, domainModel.GraphSubject] = ss.map { s =>
    (s.id -> domainModel.GraphSubject(
      s.id,
      s.name,
      s.subjectType,
      s.isDisabled,
      Some(s.isStartSubject),
      s.inputPool,
      s.relatedSubjectId,
      s.relatedGraphId,
      s.externalType,
      s.roleId match {
        case None => None
        case Some(id) => Some(roles(id))
      },
      s.comment,
      vs.map(convert).toMap,
      convert(ms, ns, es)))
  }.toMap

  def convert(v: GraphVariable): (String, domainModel.GraphVariable) =
    (v.id -> domainModel.GraphVariable(v.id, v.name))

  def convert(ms: Seq[GraphMacro], ns: Seq[GraphNode], es: Seq[GraphEdge]): Map[String, domainModel.GraphMacro] = {
    val nodes = ns.groupBy(_.macroId).mapValues(_.map(convert).toMap)
    val edges = es.groupBy(_.macroId).mapValues(_.map(convert))
    ms.map { m =>
      (m.id -> domainModel.GraphMacro(
        m.id,
        m.name,
        nodes(m.id),
        edges(m.id)))
    }.toMap
  }

  def convert(n: GraphNode): (Short, domainModel.GraphNode) = {
    val graphVarManList = List(n.varManVar1Id, n.varManVar2Id, n.varManOperation, n.varManStoreVarId)
    val graphVarMan =
      if (graphVarManList.forall(_.isDefined))
        Some(domainModel.GraphVarMan(
          n.varManVar1Id.get,
          n.varManVar2Id.get,
          n.varManOperation.get,
          n.varManStoreVarId.get))
      else
        None

    (n.id, domainModel.GraphNode(
      n.id,
      n.text,
      n.isStart,
      n.isEnd,
      n.nodeType,
      n.isDisabled,
      n.isMajorStartNode,
      n.channelId,
      n.variableId,
      domainModel.GraphNodeOptions(
        n.optionMessageId,
        n.optionSubjectId,
        n.optionCorrelationId,
        n.optionChannelId,
        n.optionNodeId),
      n.executeMacroId,
      graphVarMan))
  }

  def convert(e: GraphEdge): domainModel.GraphEdge = {
    val targetList = List[Option[Any]](e.targetSubjectId, e.targetMin, e.targetMax, e.targetCreateNew)
    val target =
      if (targetList.forall(_.isDefined))
        Some(domainModel.GraphEdgeTarget(
          e.targetSubjectId.get,
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
      target,
      e.isDisabled,
      e.isOptional,
      e.priority,
      e.manualTimeout,
      e.variableId,
      e.correlationId,
      e.comment,
      Array(e.transportMethod))
  }
}
