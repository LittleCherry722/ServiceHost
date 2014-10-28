package de.tkip.sbpm.verification

import de.tkip.sbpm.application.miscellaneous.RoleMapper
import de.tkip.sbpm.model.{Graph, GraphEdge, GraphMacro, GraphMessage, GraphNode, GraphSubject, StateType}
import de.tkip.sbpm.newmodel.StateTypes.StateType
import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.rest.GraphJsonProtocol._
import spray.json._

import scala.io.Source

/**
 * Created by Arne Link on 18.10.14.
 */
object ModelConverter {

  def testConversion = {
    val pm = convertForVerification(loadGraph("ratiodrink"))
    val veri = new Verificator(pm)
    veri.optimize = true
    veri.verificate()
    veri
  }

  def loadGraph(file: String) : Graph = {
    val simpleGraphSource = Source.fromURL(getClass.getResource(s"/de/tkip/sbpm/persistence/testdata/${file}.json")).mkString
    val domainGraph = simpleGraphSource.parseJson.convertTo[Graph](graphJsonFormat(RoleMapper.noneMapper))
    domainGraph
  }

  def convertForVerification(model: Graph) : ProcessModel = {
    val subjects: Set[SubjectLike] = model.subjects.values.map(subjectToVerification).toSet
    val messageTypes: Map[String, MessageContentType] = model.messages.map((messagesToVerification _).tupled)

    ProcessModel(
      id =  model.id.getOrElse(0),
      name = "Process",
      subjects = subjects,
      messageTypes = messageTypes // fix this!
    )
  }


  /* ************************************* *
   * *************** DONE **************** *
   * ************************************* */

  private def subjectToVerification(sub: GraphSubject) : SubjectLike = {
    val id = sub.id
    val name = sub.name
    val isMulti = sub.externalType == Some("multisubject")
    lazy val ipSize = sub.inputPool
    lazy val isStartSubject = sub.isStartSubject == Some(true)
    lazy val startState = sub.macros.values.flatMap(_.nodes.find(_._2.isStart).map(_._1)).head.toInt
    lazy val macros = sub.macros.values.flatMap(macrosToVerification).toSet
    lazy val states = statesToVerification(sub.macros("##main##"))
    if (false && sub.subjectType == "external" && sub.externalType == Some("external")) {
      ExternalSubject(
        id = id,
        name = name,
        relatedProcess = 0, // TODO: why is there no externalProcessId defined for graphSubjects?
        multi = isMulti
      )
    } else if (false && sub.subjectType == "external" && sub.externalType == Some("interface")) {
      InterfaceSubject(
        id = id,
        name = name,
        multi = isMulti,
        ipSize = ipSize,
        states = states,
        startState = startState
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

  private def macrosToVerification(m: GraphMacro): Option[Macro] = {
    if (m.id == "##main##") {
      None
    } else {
      val states = nodesAndEdgesToVerification(m.nodes, m.edges)
      val startState = m.nodes.values.find(_.isStart).get.id.toInt
      val vMacro = Macro(name = m.name, startState = startState, states = states)
      Some(vMacro)
    }
  }

  private def statesToVerification(m: GraphMacro): Set[State] = {
    nodesAndEdgesToVerification(m.nodes, m.edges)
  }

  private def nodesAndEdgesToVerification(nodes: Map[Short, GraphNode],
                                          edges: Iterable[GraphEdge]): Set[State] = {
    nodes.values.map { node =>
      val transitions = edges.filter(_.startNodeId == node.id).map{ edge =>
        val exitParams = exitParamsForEdge(edge, nodes)
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

  // TODO implement
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
                                nodes: Map[Short, GraphNode]): ExitParams = {
    edge.edgeType match {
      case "timeout" => TimeoutParam(duration = 0) // Timeout is not implemented at the moment
      case _ => {
        val communicationParams = {(contentVar: Option[String],
                                         toVar: Option[String],
                                      storeVar: Option[String]) =>
          CommunicationParams(
            messageType = edge.text,
            contentVarName = contentVar,
            subject = edge.target.get.subjectId,
            min = Number(1),
            max = Number(1),
            channelVar = toVar,
            storeVar = storeVar
          )
        }
        lazy val cVar = edge.variableId.find(_.nonEmpty)
        lazy val tVar = edge.target.flatMap(_.variableId).find(_.nonEmpty)
        stateTypeForNode(nodes(edge.startNodeId)) match {
          case StateTypes.Send       => communicationParams(cVar, None, tVar)  // TODO!!
          case StateTypes.Receive    => communicationParams(None, None, cVar)  // TODO!!
          case StateTypes.Observer   => communicationParams(None, None, None)  // TODO!!
          case StateTypes.Act        => ActParam(edge.text)
          case StateTypes.Function   => NoExitParams
          case StateTypes.Split      => NoExitParams
          case StateTypes.Join       => NoExitParams
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
