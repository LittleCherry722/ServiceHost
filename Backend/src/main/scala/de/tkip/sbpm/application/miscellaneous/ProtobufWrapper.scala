package de.tkip.sbpm.application.miscellaneous

import de.tkip.sbpm.application.subject.misc.AvailableAction
import de.tkip.sbpm.proto.{ GAEexecution => proto }
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.misc.ExecuteAction
import de.tkip.sbpm.proto.GAEexecution
import de.tkip.sbpm.model.Graph
import de.tkip.sbpm.model.GraphConversation
import de.tkip.sbpm.model.GraphMessage
import de.tkip.sbpm.model.GraphSubject
import de.tkip.sbpm.model.GraphVariable
import de.tkip.sbpm.model.GraphMacro
import de.tkip.sbpm.model.GraphNode
import de.tkip.sbpm.model.GraphNodeOptions
import de.tkip.sbpm.model.GraphVarMan
import de.tkip.sbpm.model.GraphEdge
import de.tkip.sbpm.model.GraphEdgeTarget
import de.tkip.sbpm.model.GraphRouting
import de.tkip.sbpm.model.GraphRoutingExpression
import de.tkip.sbpm.model.GraphConversation
import de.tkip.sbpm.model.GraphSubject
import de.tkip.sbpm.model.GraphMacro
import de.tkip.sbpm.model.GraphNode
import de.tkip.sbpm.model.GraphVarMan
import de.tkip.sbpm.model.GraphEdge
import de.tkip.sbpm.model.GraphRoutingExpression
import de.tkip.sbpm.model.GraphRoutingExpression
import de.tkip.sbpm.model.GraphMessage
import de.tkip.sbpm.model.Action
import de.tkip.sbpm.model.Action
import de.tkip.sbpm.application.subject.misc.AvailableAction
import de.tkip.sbpm.application.subject.misc.AvailableAction
import java.util.Date
import java.text.SimpleDateFormat
import de.tkip.sbpm.application.History
import scala.collection.JavaConverters._
import de.tkip.sbpm.application.subject.misc.TargetUser
import de.tkip.sbpm.application.subject.misc.TargetUser
import de.tkip.sbpm.application.subject.misc.MessageData
import de.tkip.sbpm.application.subject.misc.MessageData
import de.tkip.sbpm.application.subject.misc.TargetUser

object ProtobufWrapper {

  def buildProto(processes: Array[ProcessInstanceInfo]): Array[Byte] = {
    val processInfoBuilder = GAEexecution.ListProcesses.newBuilder();

    for (process <- processes) {
      processInfoBuilder.addProcesses(GAEexecution.ListProcesses.ProcessInfo.newBuilder().setId(process.id).setProcessId(process.processId).build())
    }

    processInfoBuilder.build().toByteArray()
  }

  def buildProcessInstanceInfos(bytes: Array[Byte]): Array[ProcessInstanceInfo] = {
    val protoInfos = GAEexecution.ListProcesses.parseFrom(bytes)

    val infos = protoInfos.getProcessesList()

    val processes = for (info <- infos.asScala)
      yield ProcessInstanceInfo(info.getId(), info.getName(), info.getProcessId())

    processes.toArray
  }

  private def buildProcessInstanceInfo(bytes: Array[Byte]): ProcessInstanceInfo = {
    val protoInfos = GAEexecution.ListProcesses.ProcessInfo.parseFrom(bytes)

    ProcessInstanceInfo(protoInfos.getId(), protoInfos.getName(), protoInfos.getProcessId())
  }

  def buildProto(action: ExecuteAction): Array[Byte] = {
    val executeActionBuilder = proto.ExecuteAction.newBuilder()

    val actionBuilder = proto.Action.newBuilder()
    actionBuilder.setUserID(1)
      .setProcessInstanceID(action.processInstanceID)
      .setSubjectID(action.subjectID) //TODO String
      .setStateID(action.stateID)
      .setStateType(action.stateType)
      // TODO stateTexts
      .setStateText("")

    val data = action.actionData
    val actionDataBuilder = proto.ActionData.newBuilder()
    actionDataBuilder.setText(data.text)
      .setExecutable(data.executeAble)
      .setTransitionType(data.transitionType)

    // Add the target users
    if (data.targetUsersData.isDefined) {
      val target = data.targetUsersData.get
      val targetUserBuilder = proto.TargetUserData.newBuilder()
      targetUserBuilder.setMin(target.min)
        .setMax(target.max)
      for (user <- target.targetUsers) {
        targetUserBuilder.addTargetUsers(user)
      }
      actionDataBuilder.setTargetUserData(targetUserBuilder)
    }
    // add the related subject
    if (data.relatedSubject.isDefined) {
      actionDataBuilder.setRelatedSubject(data.relatedSubject.get)
    }
    // add the messageContent (TODO)

    actionBuilder.addActionData(actionDataBuilder.build())
    //    actionBuilder.setActionData(d, actionDataBuilder.build())

    executeActionBuilder.setAction(actionBuilder.build())

    executeActionBuilder.build().toByteArray()
  }

  def buildAvailableAction(bytes: Array[Byte]): AvailableAction = {
    import scala.collection.JavaConversions._

    val action = proto.Action.parseFrom(bytes)

    AvailableAction(
      action.getUserID().toInt,
      action.getProcessInstanceID(),
      action.getSubjectID().toString,
      "##main##",
      action.getStateID(),
      action.getStateText(),
      action.getStateType(),
      buildActionData(action.getActionDataList().asScala.toList))
  }

  private def buildActionsFromList(protoActions: List[proto.Action]): Array[AvailableAction] = {
    (for (action <- protoActions)
      yield AvailableAction(
      action.getUserID(),
      action.getProcessInstanceID(),
      action.getSubjectID(),
      "##main##",
      action.getStateID(),
      action.getStateText(),
      action.getStateType(),
      buildActionData(action.getActionDataList().asScala.toList))).toArray
  }

  def buildActions(bytes: Array[Byte]): Array[AvailableAction] = {
    val actions = GAEexecution.ListActions.parseFrom(bytes)

    buildActionsFromList(actions.getActionsList().asScala.toList)
  }

  def buildActionData(actionData: List[proto.ActionData]): Array[ActionData] = {
    (for (data <- actionData)
      yield ActionData(
      data.getText(),
      data.getExecutable(),
      data.getTransitionType(), //            data.getTa/
      targetUsersData = if (data.hasTargetUserData()) Some(buildTargetUser(data.getTargetUserData())) else None,
      relatedSubject = if (data.hasRelatedSubject()) Some(data.getRelatedSubject()) else None, // TODO...
      messages = buildMessageData(data.getMessagesList().asScala.toList))).toArray
  }

  def buildTargetUser(data: proto.TargetUserData): TargetUser =
    TargetUser(
      data.getMin(),
      data.getMax(), {
        val x =
          data.getTargetUsersList().asScala.toList
        x.map(_.toInt).toArray
      })

  def buildMessageData(messageData: List[proto.MessageData]): Option[Array[MessageData]] =
    if (messageData.isEmpty) None
    else Some((for (data <- messageData)
      yield MessageData(
      data.getUserID(),
      data.getMessageContent())).toArray)

  def buildProcessInstanceData(bytes: Array[Byte]): ProcessInstanceData = {
    val protoInstanceData = GAEexecution.ProcessInstanceData.parseFrom(bytes)

    ProcessInstanceData( //TODO
      protoInstanceData.getId(),
      protoInstanceData.getName,
      protoInstanceData.getProcessId(),
      protoInstanceData.getProcessName(),
      buildGraph(protoInstanceData.getGraph()),
      protoInstanceData.getIsTerminated(),
      (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(protoInstanceData.getDate()),
      protoInstanceData.getOwner(),
      History(protoInstanceData.getName(), protoInstanceData.getProcessId()), // TODO HISTORY
      buildActionsFromList(protoInstanceData.getActionsList().asScala.toList))
  }

  def buildProto(createProcess: CreateProcessInstance, graph: Graph): Array[Byte] = {
    val requestBuilder = GAEexecution.CreateProcessInstance.newBuilder()

    requestBuilder.setProcessId(createProcess.processID)
    requestBuilder.setGraph(buildProto(graph))
    requestBuilder.setName(createProcess.name)

    requestBuilder.build().toByteArray()
  }

  private def buildProto(graph: Graph): proto.Graph = {
    val graphBuilder = GAEexecution.Graph.newBuilder()

    if (graph.id.isDefined)
      graphBuilder.setId(graph.id.get)
    if (graph.processId.isDefined)
      graphBuilder.setProcessId(graph.processId.get)

    graphBuilder.setDate(graph.date.toString())

    for (conversation <- graph.conversations)
      graphBuilder.addConversations(buildProto(conversation._2))

    for (message <- graph.messages)
      graphBuilder.addMessages(buildProto(message._2))

    for (subject <- graph.subjects)
      graphBuilder.addSubjects(buildProto(subject._2))

    for (routing <- graph.routings)
      graphBuilder.addRoutings(buildProto(routing))

    graphBuilder.build()
  }

  private def buildProto(conversation: GraphConversation): proto.GraphConversation = {
    val conversationBuilder = GAEexecution.GraphConversation.newBuilder()

    conversationBuilder.setId(conversation.id)
    conversationBuilder.setName(conversation.name)

    conversationBuilder.build()
  }

  private def buildProto(message: GraphMessage): proto.GraphMessage = {
    val messageBuilder = GAEexecution.GraphMessage.newBuilder()

    messageBuilder.setId(message.id)
    messageBuilder.setName(message.name)

    messageBuilder.build()
  }

  private def buildProto(subject: GraphSubject): proto.GraphSubject = {
    val subjectBuilder = GAEexecution.GraphSubject.newBuilder()

    subjectBuilder.setId(subject.id)
    subjectBuilder.setName(subject.name)
    subjectBuilder.setSubjectType(subject.subjectType)
    subjectBuilder.setIsDisabled(subject.isDisabled)

    if (subject.isStartSubject.isDefined)
      subjectBuilder.setIsStartSubject(subject.isStartSubject.get)

    subjectBuilder.setInputPool(subject.inputPool)

    if (subject.relatedSubjectId.isDefined)
      subjectBuilder.setRelatedSubjectId(subject.relatedSubjectId.get)

    if (subject.relatedGraphId.isDefined)
      subjectBuilder.setRelatedGraphId(subject.relatedGraphId.get)

    if (subject.externalType.isDefined)
      subjectBuilder.setExternalType(subject.externalType.get)

    if (subject.role.isDefined)
      subjectBuilder.setRole(subject.role.get.name) // TODO

    if (subject.url.isDefined)
      subjectBuilder.setUrl(subject.url.get)

    if (subject.comment.isDefined)
      subjectBuilder.setComment(subject.comment.get)

    for (variable <- subject.variables)
      subjectBuilder.addVariables(buildProto(variable._2))

    for (macro <- subject.macros)
      subjectBuilder.addMacros(buildProto(macro._2))

    subjectBuilder.build()
  }

  private def buildProto(variable: GraphVariable): proto.GraphVariable = {
    val variableBuilder = GAEexecution.GraphVariable.newBuilder()

    variableBuilder.setId(variable.id)
    variableBuilder.setName(variable.name)

    variableBuilder.build()
  }

  private def buildProto(macro: GraphMacro): proto.GraphMacro = {
    val macroBuilder = GAEexecution.GraphMacro.newBuilder()

    macroBuilder.setId(macro.id)
    macroBuilder.setName(macro.name)

    for (node <- macro.nodes)
      macroBuilder.addNodes(buildProto(node._2))

    for (edge <- macro.edges)
      macroBuilder.addEdges(buildProto(edge))

    macroBuilder.build()
  }

  private def buildProto(node: GraphNode): proto.GraphNode = {
    val nodeBuilder = GAEexecution.GraphNode.newBuilder()

    nodeBuilder.setId(node.id)
    nodeBuilder.setText(node.text)
    nodeBuilder.setIsStart(node.isStart)
    nodeBuilder.setIsEnd(node.isEnd)
    nodeBuilder.setNodeType(node.nodeType)
    nodeBuilder.setIsDisabled(node.isDisabled)
    nodeBuilder.setIsMajorStartNode(node.isMajorStartNode)

    if (node.conversationId.isDefined)
      nodeBuilder.setConversationId(node.conversationId.get)

    nodeBuilder.setOptions(buildProto(node.options))

    if (node.macroId.isDefined)
      nodeBuilder.setMacroId(node.macroId.get)

    if (node.varMan.isDefined)
      nodeBuilder.setVarMan(buildProto(node.varMan.get))

    nodeBuilder.build()
  }

  private def buildProto(options: GraphNodeOptions): proto.GraphNodeOptions = {
    val optionsBuilder = GAEexecution.GraphNodeOptions.newBuilder()

    if (options.messageId.isDefined)
      optionsBuilder.setMessageId(options.messageId.get)

    if (options.subjectId.isDefined)
      optionsBuilder.setSubjectId(options.subjectId.get)

    if (options.correlationId.isDefined)
      optionsBuilder.setCorrelationId(options.correlationId.get)

    if (options.conversationId.isDefined)
      optionsBuilder.setConversationId(options.conversationId.get)

    if (options.nodeId.isDefined)
      optionsBuilder.setNodeId(options.nodeId.get)

    optionsBuilder.build()
  }

  private def buildProto(varMan: GraphVarMan): proto.GraphVarMan = {
    val varManBuilder = GAEexecution.GraphVarMan.newBuilder()

    varManBuilder.setVar1Id(varMan.var1Id)
    varManBuilder.setVar2Id(varMan.var2Id)
    varManBuilder.setOperation(varMan.operation)
    varManBuilder.setStoreVarId(varMan.storeVarId)

    varManBuilder.build()
  }

  private def buildProto(edge: GraphEdge): proto.GraphEdge = {
    val edgeBuilder = GAEexecution.GraphEdge.newBuilder()

    edgeBuilder.setStartNodeId(edge.startNodeId)
    edgeBuilder.setEndNodeId(edge.endNodeId)
    edgeBuilder.setText(edge.text)
    edgeBuilder.setEdgeType(edge.edgeType)

    if (edge.target.isDefined)
      edgeBuilder.setTarget(buildProto(edge.target.get))

    edgeBuilder.setIsDisabled(edge.isDisabled)
    edgeBuilder.setIsOptional(edge.isOptional)
    edgeBuilder.setPriority(edge.priority.toInt)
    edgeBuilder.setManualTimeout(edge.manualTimeout)

    if (edge.variableId.isDefined)
      edgeBuilder.setVariableId(edge.variableId.get)

    if (edge.correlationId.isDefined)
      edgeBuilder.setCorrelationId(edge.correlationId.get)

    if (edge.comment.isDefined)
      edgeBuilder.setComment(edge.comment.get)

    for (transportMethod <- edge.transportMethod)
      edgeBuilder.addTransportMethod(transportMethod)

    edgeBuilder.build()
  }

  private def buildProto(edgeTarget: GraphEdgeTarget): proto.GraphEdgeTarget = {
    val edgeTargetBuilder = GAEexecution.GraphEdgeTarget.newBuilder()

    edgeTargetBuilder.setSubjectId(edgeTarget.subjectId)
    edgeTargetBuilder.setMin(edgeTarget.min)
    edgeTargetBuilder.setMax(edgeTarget.max)
    edgeTargetBuilder.setCreateNew(edgeTarget.createNew)

    if (edgeTarget.variableId.isDefined)
      edgeTargetBuilder.setVariableId(edgeTarget.variableId.get)

    edgeTargetBuilder.build()
  }

  private def buildProto(routing: GraphRouting): proto.GraphRouting = {
    val routingBuilder = GAEexecution.GraphRouting.newBuilder()

    routingBuilder.setId(routing.id)
    routingBuilder.setCondition(buildProto(routing.condition))
    routingBuilder.setImplication(buildProto(routing.implication))

    routingBuilder.build()
  }

  private def buildProto(routingExp: GraphRoutingExpression): proto.GraphRoutingExpression = {
    val routingExpBuilder = GAEexecution.GraphRoutingExpression.newBuilder()

    routingExpBuilder.setSubjectId(routingExp.subjectId)
    routingExpBuilder.setOperator(routingExp.operator)

    if (routingExp.groupId.isDefined)
      routingExpBuilder.setGroupId(routingExp.groupId.get)

    if (routingExp.userId.isDefined)
      routingExpBuilder.setUserId(routingExp.userId.get)

    routingExpBuilder.build()
  }

  def buildGraph(protoGraph: proto.Graph): Graph = {
    Graph(
      if (protoGraph.hasId()) Some(protoGraph.getId()) else None,
      if (protoGraph.hasProcessId()) Some(protoGraph.getProcessId()) else None,
      java.sql.Timestamp.valueOf(protoGraph.getDate()),
      buildGraphConversations(protoGraph.getConversationsList().asScala.toList),
      buildGraphMessages(protoGraph.getMessagesList().asScala.toList),
      buildGraphSubjects(protoGraph.getSubjectsList().asScala.toList),
      buildGraphRouting(protoGraph.getRoutingsList().asScala.toList))
  }

  private def buildGraphConversations(conversationArray: List[proto.GraphConversation]): Map[String, GraphConversation] = {
    val conversations =
      for (conversation <- conversationArray) yield (conversation.getName(), GraphConversation(conversation.getId(), conversation.getName()))

    conversations.toMap
  }

  private def buildGraphMessages(messagesArray: List[proto.GraphMessage]): Map[String, GraphMessage] = {
    val messages =
      for (message <- messagesArray) yield (message.getId(), GraphMessage(message.getId(), message.getName()))

    messages.toMap
  }

  private def buildGraphSubjects(subjectArray: List[proto.GraphSubject]): Map[String, GraphSubject] = {
    val subjects =
      for (subject <- subjectArray)
        yield (subject.getId(),
        GraphSubject(
          subject.getId(),
          subject.getName(),
          subject.getSubjectType(),
          subject.getIsDisabled(),
          if (subject.hasIsStartSubject()) Some(subject.getIsStartSubject()) else None,
          subject.getInputPool().toShort,
          if (subject.hasRelatedSubjectId()) Some(subject.getRelatedSubjectId()) else None,
          if (subject.hasRelatedGraphId()) Some(subject.getRelatedGraphId()) else None,
          if (subject.hasExternalType()) Some(subject.getExternalType()) else None,
          if (subject.hasRole()) None else None, // TODO
          if (subject.hasUrl()) Some(subject.getUrl()) else None,
          if (subject.hasComment()) Some(subject.getComment()) else None,
          buildGraphVariables(subject.getVariablesList().asScala.toList),
          buildGraphMacros(subject.getMacrosList().asScala.toList)))

    subjects.toMap
  }

  private def buildGraphVariables(variableArray: List[proto.GraphVariable]): Map[String, GraphVariable] = {
    val variables =
      for (variable <- variableArray)
        yield (variable.getId(), GraphVariable(variable.getId(), variable.getName()))

    variables.toMap
  }

  private def buildGraphMacros(macroArray: List[proto.GraphMacro]): Map[String, GraphMacro] = {
    val macros =
      for (macro <- macroArray)
        yield (
        macro.getId(),
        GraphMacro(
          macro.getId(),
          macro.getName(),
          buildGraphNodes(macro.getNodesList().asScala.toList),
          buildGraphEdges(macro.getEdgesList().asScala.toList)))

    macros.toMap
  }

  private def buildGraphNodes(nodeArray: List[proto.GraphNode]): Map[Short, GraphNode] = {
    val nodes =
      for (node <- nodeArray)
        yield (
        node.getId().toShort,
        GraphNode(
          node.getId().toShort,
          node.getText(),
          node.getIsStart(),
          node.getIsEnd(),
          node.getNodeType(),
          node.getIsDisabled(),
          node.getIsMajorStartNode(),
          if (node.hasConversationId()) Some(node.getConversationId()) else None,
          if (node.hasVariableId()) Some(node.getVariableId()) else None,
          buildGraphOptions(node.getOptions()),
          if (node.hasMacroId()) Some(node.getMacroId()) else None,
          if (node.hasVarMan()) Some(buildGraphVarMan(node.getVarMan())) else None))

    nodes.toMap
  }

  private def buildGraphOptions(options: proto.GraphNodeOptions): GraphNodeOptions = {
    GraphNodeOptions(
      if (options.hasMessageId()) Some(options.getMessageId()) else None,
      if (options.hasSubjectId()) Some(options.getSubjectId()) else None,
      if (options.hasCorrelationId()) Some(options.getCorrelationId()) else None,
      if (options.hasConversationId()) Some(options.getConversationId()) else None,
      if (options.hasNodeId()) Some(options.getNodeId().toShort) else None)
  }

  private def buildGraphVarMan(varMan: proto.GraphVarMan): GraphVarMan = {
    GraphVarMan(
      varMan.getVar1Id(),
      varMan.getVar2Id(),
      varMan.getOperation(),
      varMan.getStoreVarId())
  }

  private def buildGraphEdges(edgesArray: List[proto.GraphEdge]): Seq[GraphEdge] = {
    val edges =
      for (edge <- edgesArray)
        yield GraphEdge(
        edge.getStartNodeId().toShort,
        edge.getEndNodeId().toShort,
        edge.getText(),
        edge.getEdgeType(),
        if (edge.hasTarget()) Some(buildGraphEdgeTarget(edge.getTarget())) else None,
        edge.getIsDisabled(),
        edge.getIsOptional(),
        edge.getPriority().toByte,
        edge.getManualTimeout(),
        if (edge.hasVariableId()) Some(edge.getVariableId()) else None,
        if (edge.hasCorrelationId()) Some(edge.getCorrelationId()) else None,
        if (edge.hasComment()) Some(edge.getComment()) else None,
        edge.getTransportMethodList().asScala.toList)

    edges.asInstanceOf[Seq[GraphEdge]]
  }

  private def buildGraphEdgeTarget(edgeTarget: proto.GraphEdgeTarget): GraphEdgeTarget = {
    GraphEdgeTarget(
      edgeTarget.getSubjectId(),
      edgeTarget.getMin().toShort,
      edgeTarget.getMax().toShort,
      edgeTarget.getCreateNew(),
      if (edgeTarget.hasVariableId()) Some(edgeTarget.getVariableId()) else None)
  }

  private def buildGraphRouting(routingArray: List[proto.GraphRouting]): Seq[GraphRouting] = {
    val routings =
      for (routing <- routingArray)
        yield GraphRouting(
        routing.getId(),
        buildGraphRoutingExpression(routing.getCondition()),
        buildGraphRoutingExpression(routing.getImplication()))

    routings.asInstanceOf[Seq[GraphRouting]]
  }

  private def buildGraphRoutingExpression(routingExpression: proto.GraphRoutingExpression): GraphRoutingExpression = {
    GraphRoutingExpression(
      routingExpression.getSubjectId(),
      routingExpression.getOperator(),
      if (routingExpression.hasGroupId()) Some(routingExpression.getGroupId()) else None,
      if (routingExpression.hasUserId()) Some(routingExpression.getUserId()) else None)
  }
}