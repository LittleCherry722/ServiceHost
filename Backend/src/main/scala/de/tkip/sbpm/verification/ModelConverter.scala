package de.tkip.sbpm.verification

import de.tkip.sbpm.model.{Graph, GraphEdge, GraphMacro, GraphMessage, GraphNode, GraphSubject, StateType}
import de.tkip.sbpm.newmodel.ProcessModelTypes.SubjectId
import de.tkip.sbpm.newmodel.StateTypes.StateType
import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.verification.lts.Lts

/**
 * Created by Arne Link on 18.10.14.
 */
object ModelConverter {
  case class InvalidGraphException(message: String, loc: ErrorLocation) extends Exception(s"$message ${loc.toString}")
  case class ErrorLocation( subject: Option[GraphSubject] = None
                          , makro: Option[GraphMacro] = None
                          , edge: Option[GraphEdge] = None
                          , node: Option[GraphNode] = None) {
    override def toString: String = {
      val err = new StringBuilder()
      Seq(
        subject.map(s => s"Subject with id '${s.id}'")
      , makro.map(m => s"In macro '${m.id}'")
      , edge.map(e => s"At edge '${e.startNodeId} -> ${e.endNodeId}'")
      , node.map(n => s"Start node Type '${n.nodeType}'")).filter(_.isDefined).map(_.get).addString(err, ". ")
      err.toString() match {
        case "" => "At unknown location"
        case str => "At: " + str
      }
    }
  }

  def graphToLts(graph: Graph): Verificator = {
    val processModel = convertForVerification(graph)
    val veri = new Verificator(processModel)
    veri.optimize = true
    veri
  }

  def verifyGraph(graph: Graph): Option[String] = {
    val processModel = convertForVerification(graph)
    val lts = Verification.buildLts(ModelConverter.convertForVerification(graph), optimize = true)

    val subjectNameMap = graph.subjects.mapValues(_.name)
    val msgMap = graph.messages.mapValues(_.name)
    lazy val invalidNodesString = lts.invalidNodes.flatMap(_.subjectMap.values.flatMap{vs =>
      vs.activeStates.map{st =>
        val baseStateText = s"${st.id} (${st.stateType})"
        val comParams = st.communicationTransitions
          .map(_.exitParams)
          .filter(_.isInstanceOf[CommunicationParams])
          .map(_.asInstanceOf[CommunicationParams])
        val stateText = if (comParams.nonEmpty) {
          val arrow = if (st.stateType == StateTypes.Send) {
            "->"
          } else {
            "<-"
          }
          s"$baseStateText " +
            comParams
              .map(c => s"'${msgMap(c.messageType)}' $arrow ${subjectNameMap(c.subject)}")
              .mkString("msgs [(", "), (", ")]")
        } else {
          baseStateText
        }
        (subjectNameMap(vs.channel.subjectId), stateText)
      }
    }).mkString("\n")
    if (!lts.valid && lts.invalidNodes.isEmpty) {
      // something fishy, possibly dirty end states etc...
      Some("Graph invalid, no more information available")
    }
    if (lts.invalidNodes.isEmpty) {
      None
    } else {
      Some(invalidNodesString)
    }
  }

  def getErrorsForLts(graph: Graph, lts: Lts): Option[String] = {
    val subjectNameMap = graph.subjects.mapValues(_.name)
    val msgMap = graph.messages.mapValues(_.name)
    if (lts.invalidNodes.isEmpty) {
      None
    } else {
      val e = lts.invalidNodes.flatMap(_.subjectMap.values.flatMap{vs =>
        vs.activeStates.map{st =>
          val baseStateText = s"${st.id} (${st.stateType})"
          val comParams = st.communicationTransitions
            .map(_.exitParams)
            .filter(_.isInstanceOf[CommunicationParams])
            .map(_.asInstanceOf[CommunicationParams])
          val stateText = if (comParams.nonEmpty) {
            val arrow = if (st.stateType == StateTypes.Send) {
              "->"
            } else {
              "<-"
            }
            s"$baseStateText " +
              comParams
                .map(c => s"'${msgMap(c.messageType)}' $arrow ${subjectNameMap(c.subject)}")
                .mkString("msgs [(", "), (", ")]")
          } else {
            baseStateText
          }
          (subjectNameMap(vs.channel.subjectId), stateText)
        }
      }).mkString("\n")
      Some(e)
    }
  }

  def convertForVerification(model: Graph) : ProcessModel = {
      val subjects: Set[SubjectLike] = model.subjects.values.map(s => subjectToVerification(s)).toSet
      val messageTypes: Map[String, MessageContentType] = model.messages.map((messagesToVerification _).tupled)
      val processModel = ProcessModel(
        id =  model.id.getOrElse(0),
        name = "Process",
        subjects = subjects,
        messageTypes = messageTypes
      )
      processModel
  }

  def convertForInterface(model: Graph, viewSubjectId: SubjectId) : ProcessModel = {
    val subjects: Set[SubjectLike] = model.subjects.values.map(s => subjectToVerification(s, Some(viewSubjectId))).toSet
    val messageTypes: Map[String, MessageContentType] = model.messages.map((messagesToVerification _).tupled)
    val processModel = ProcessModel(
      id =  model.id.getOrElse(0),
      name = "Process",
      subjects = subjects,
      messageTypes = messageTypes
    )
    processModel
  }


  /* ************************************* *
   * *************** DONE **************** *
   * ************************************* */

  private def subjectToVerification(sub: GraphSubject, viewSubjectId: Option[SubjectId] = None) : SubjectLike = {
    val id = sub.id
    val name = sub.name
    val isMulti = sub.externalType.contains("multisubject")
    lazy val ipSize = sub.inputPool
    lazy val isStartSubject = sub.isStartSubject.contains(true)
    lazy val startState = sub.macros.values.flatMap(_.nodes.find(_._2.isStart).map(_._1)).head.toInt
    lazy val macros = sub.macros.values.flatMap(m => macrosToVerification(m, sub)).toSet
    lazy val states = statesToVerification(sub.macros("##main##"), sub)
    // External subjects etc are not really supported in the verification engine the same way they
    // are supported in the process engine and have different meanings.
    // For now, we only create local, single subjects for the verification engine.
    if (false && sub.subjectType == "external" && sub.externalType.contains("external")) {
      ExternalSubject(
        id = id,
        name = name,
        relatedProcess = 0,
        multi = isMulti
      )
    } else if (viewSubjectId.contains(sub.id) && sub.subjectType == "external" && sub.externalType.contains("interface")) {
      InstantInterface(
        id = id,
        name = name
      )
    } else {
      Subject(
        id = id,
        name = name,
        startSubject = isStartSubject,
        multi = isMulti,
        ipSize = ipSize,
        states = states,
        startState = startState,
        macros = macros
      )
    }
  }

  private def macrosToVerification(m: GraphMacro, subject: GraphSubject): Option[Macro] = {
    if (m.id == "##main##") {
      None
    } else {
      val states = nodesAndEdgesToVerification(m.nodes, m.edges, subject, m)
      m.nodes.values.find(_.isStart) match {
        case None =>
          val errorLoc = ErrorLocation(subject = Some(subject), makro = Some(m))
          throw InvalidGraphException("No start state detected.", errorLoc)
        case Some(startState) => {
          val startStateId = startState.id.toInt
          val vMacro = Macro(name = m.name, startState = startStateId, states = states)
          Some(vMacro)
        }
      }

    }
  }

  private def statesToVerification(m: GraphMacro, subject: GraphSubject): Set[State] = {
    nodesAndEdgesToVerification(m.nodes, m.edges, subject, m)
  }

  private def nodesAndEdgesToVerification(nodes: Map[Short, GraphNode],
                                          edges: Iterable[GraphEdge],
                                          subject: GraphSubject,
                                          graphMacro: GraphMacro): Set[State] = {
    nodes.values.map { node =>
      val transitions = edges.filter(_.startNodeId == node.id).map{ edge =>
        val exitParams = exitParamsForEdge(edge, nodes, subject, graphMacro)
        val priority = edge.priority.toInt
        val successor = edge.endNodeId
        Transition(
          exitParams = exitParams,
          priority = priority,
          successor = successor
        )
      }.toSet
      val stateType = stateTypeForNode(node)
      val serviceParams = serviceParamsForNode(node, edges)
      State(
        id = node.id.toInt,
        text = node.text,
        stateType = stateType,
        transitions = transitions,
        serviceParams = serviceParams
      )
    }.toSet
  }

  private def stateTypeForNode(node: GraphNode) : StateType = {
    StateType.fromStringtoStateTypeOption(node.nodeType) match {
      case Some(StateType.ActStateType) => StateTypes.Act
      case Some(StateType.SendStateType) => StateTypes.Send
      case Some(StateType.ReceiveStateType) => StateTypes.Receive
      case Some(StateType.EndStateType) => StateTypes.End
      case Some(StateType.ModalSplitStateType) => StateTypes.Split
      case Some(StateType.ModalJoinStateType) => StateTypes.Join
      case Some(StateType.SplitGuardStateType) => StateTypes.SplitGuard
      case Some(StateType.TauStateType) => StateTypes.Act
      case Some(StateType.OpenIPStateType) => StateTypes.Function
      case Some(StateType.CloseIPStateType) => StateTypes.Function
      case Some(StateType.IsIPEmptyStateType) => StateTypes.Function
      case Some(StateType.ActivateStateType) => StateTypes.Function
      case Some(StateType.DeactivateStateType) => StateTypes.Function
      case Some(StateType.ChooseAgentStateType) => StateTypes.Function
      case Some(StateType.VariableManipulationType) => StateTypes.Function
      case Some(StateType.MacroStateType) => StateTypes.Function
/*      case Some(StateType.DecisionStateType) => null  // TODO Fill out missing states?
      case Some(StateType.ArchiveStateType) => null
      case Some(StateType.BlackboxStateType) => null
      case _ => null*/
    }
  }


  private def serviceParamsForNode(node: GraphNode,
                                   edges: Iterable[GraphEdge]) : InternalServiceParams = {
    StateType.fromStringtoStateTypeOption(node.nodeType) match {
      case None => NoServiceParams
      case Some(nodeType) =>
        lazy val messageType = node.options.messageId
        lazy val subjectId = node.options.subjectId
        lazy val chooseAgentParams = NewSubjectInstances(
          subject = node.chooseAgentSubject.get,
          min = Number(1),
          max = Number(1),
          storeVar = edges.find(_.startNodeId == node.id).get.variableId.get
        )
        nodeType match {
          case StateType.MacroStateType => ExecuteMacro(node.macroId.get)
          case StateType.OpenIPStateType => OpenIP(messageType, subjectId)
          case StateType.CloseIPStateType => CloseIP(messageType, subjectId)
          case StateType.IsIPEmptyStateType => IsIPEmpty(messageType, subjectId)
          case StateType.ActivateStateType => ActivateState(node.options.nodeId.get)
          case StateType.DeactivateStateType => DeactivateState(node.options.nodeId.get)
          case StateType.ChooseAgentStateType => chooseAgentParams
          case StateType.VariableManipulationType => VariableManipulation(
            v1 = node.varMan.get.var1Id,
            v2 = node.varMan.map(_.var2Id).find(_.nonEmpty),
            op = node.varMan.get.operation,
            target = node.varMan.get.storeVarId
          )

          case _ => NoServiceParams
        }
    }
  }

  private def exitParamsForEdge(edge: GraphEdge,
                                nodes: Map[Short, GraphNode],
                                subject: GraphSubject,
                                graphMacro: GraphMacro ): ExitParams = {
    edge.edgeType match {
      case "timeout" => TimeoutParam(duration = 0) // Timeout is not implemented at the moment
      case _ => {
        lazy val target = edge.target match {
          case None =>
            val errorLoc = ErrorLocation(subject = Some(subject)
              , makro = Some(graphMacro)
              , edge = Some(edge)
              , node = nodes.get(edge.startNodeId))
            throw InvalidGraphException("No target for message edge.", errorLoc)
          case Some(target) => target
        }
        val communicationParams = { (contentVar: Option[String],
                                     toVar: Option[String],
                                     storeVar: Option[String]) =>
          CommunicationParams(
            messageType = edge.text,
            contentVarName = contentVar,
            subject = target.subjectId,
            min = Number(1),
            max = Number(1),
            channelVar = toVar,
            storeVar = storeVar
          )
        }
        lazy val cVar = edge.variableId.find(_.nonEmpty)
        lazy val tVar = target.variableId.find(_.nonEmpty)
        stateTypeForNode(nodes(edge.startNodeId)) match {
          case StateTypes.Send => communicationParams(cVar, None, tVar)
          case StateTypes.Receive => communicationParams(None, None, cVar)
          case StateTypes.Observer => communicationParams(None, None, None) // TODO!!
          case StateTypes.Act => ActParam(edge.text)
          case StateTypes.Function => NoExitParams
          case StateTypes.Split => NoExitParams
          case StateTypes.Join => NoExitParams
          case StateTypes.SplitGuard => {
            // either NoExitParams or ImplicitTransitionParam, depending on
            // the end state (?). Implicit for Join, everything else: NoExitParams
            stateTypeForNode(nodes(edge.endNodeId)) match {
              case StateTypes.Join => ImplicitTransitionParam
              case _ => NoExitParams
            }
          }
          case StateTypes.End => NoExitParams // End states should not have transitions though
        }
      }
    }
  }

  private def messagesToVerification(name: String, message: GraphMessage): ((String, MessageContentType)) = {
    (message.id, TextContentType)
  }
}
