package de.tkip.servicehost.serviceactor.stubgen

import java.awt.TrayIcon.MessageType
import java.nio.file.Files
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.ChannelID

import scala.reflect.ClassTag
import scala.collection.immutable.Map
import scala.collection.mutable.{Map => MutableMap, ArrayBuffer}
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import akka.actor.Actor
import akka.actor.Props
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import spray.json._
import de.tkip.sbpm.application.miscellaneous.RoleMapper
import de.tkip.sbpm.model._
import de.tkip.sbpm.rest.GraphJsonProtocol._
import de.tkip.sbpm.rest.JsonProtocol.{GraphHeader, createGraphHeaderFormat}
import de.tkip.servicehost.ActorLocator
import de.tkip.servicehost.Messages._
import de.tkip.servicehost.ReferenceXMLActor
import de.tkip.sbpm.instrumentation.InstrumentedActor


case class ServiceExport(version: Int, name: String, author: String, subjectId: String, process: GraphHeader)

object StubGeneratorActor {
  implicit def serviceExportFormat(implicit roles: RoleMapper) = jsonFormat5(ServiceExport)
}

class StubGeneratorActor extends InstrumentedActor {
  implicit val timeout = Timeout(15 seconds)

  import StubGeneratorActor.serviceExportFormat

  lazy val refAc = this.context.actorOf(Props[ReferenceXMLActor], "reference-xml-actor")

  case class Target(id: String, min: Int, max: Int, createNew: Boolean, variable: String)

  abstract class State {
    def id: Int

    var exittype: String

    def targets: MutableMap[String, String]

    def targetIds: MutableMap[String, Int]

    var text: String
    var variableId: String
    var correlationId: String
  }

  case class ReceiveState(id: Int, var exittype: String = null, targets: MutableMap[String, String] = MutableMap(), targetIds: MutableMap[String, Int] = MutableMap(), var text: String = null, var variableId: String = null, var correlationId: String = null) extends State

  case class SendState(id: Int, var exittype: String = null, targets: MutableMap[String, String] = MutableMap(), targetIds: MutableMap[String, Int] = MutableMap(), var text: String = null, var variableId: String = null, var correlationId: String = null) extends State

  case class ExitState(id: Int, var exittype: String = null, targets: MutableMap[String, String] = MutableMap(), targetIds: MutableMap[String, Int] = MutableMap(), var text: String = null, var variableId: String = null, var correlationId: String = null) extends State

  case class ActionState(id: Int, var exittype: String = null, targets: MutableMap[String, String] = MutableMap(), targetIds: MutableMap[String, Int] = MutableMap(), var text: String = null, var variableId: String = null, var correlationId: String = null) extends State

  case class DecisionState(id: Int, var exittype: String = null, targets: MutableMap[String, String] = MutableMap(), targetIds: MutableMap[String, Int] = MutableMap(), var text: String = null, var variableId: String = null, var correlationId: String = null) extends State

  case class CloseIPState(id: Int, var exittype: String = null, targets: MutableMap[String, String] = MutableMap(), targetIds: MutableMap[String, Int] = MutableMap(), var text: String = null, var variableId: String = null, var correlationId: String = null) extends State

  case class OpenIPState(id: Int, var exittype: String = null, targets: MutableMap[String, String] = MutableMap(), targetIds: MutableMap[String, Int] = MutableMap(), var text: String = null, var variableId: String = null, var correlationId: String = null) extends State

  case class ActivateState(id: Int, var exittype: String = null, targets: MutableMap[String, String] = MutableMap(), targetIds: MutableMap[String, Int] = MutableMap(), var text: String = null, var variableId: String = null, var correlationId: String = null) extends State

  case class DeactivateState(id: Int, var exittype: String = null, targets: MutableMap[String, String] = MutableMap(), targetIds: MutableMap[String, Int] = MutableMap(), var text: String = null, var variableId: String = null, var correlationId: String = null) extends State

  val edgeMap = scala.collection.mutable.Map[Int, List[Tuple3[String, String, Int]]]()
  var startNodeIndex: String = ""
  var inputPool: Int = 0
  val isCreatedNewVariable = scala.collection.mutable.Map[Int, String]()
  var closeChannelID: ChannelID = ("", "")
  var openChannelID: ChannelID = ("", "")
  var preferentialState = List[Tuple2[Int, Int]]()


  def wrappedReceive = {
    case msg@GenerateService(path) => {
      log.info("StubGeneratorActor received " + msg)

      val export: ServiceExport = loadServiceExport(path)
      val subjectId = export.subjectId
      val graph: Graph = export.process.graph.get
      val subject: GraphSubject = graph.subjects(subjectId)
      val variablesOfSubject: Map[String, String] = subject.variables.map({ case (x, v) => (v.id, v.name)})
      inputPool = subject.inputPool
      val subjectName = subject.name
      val states = extractStates(subject)
      val messages: Map[String, String] = graph.messages.map({ case (x, m) => (m.id, m.name)})
      val closeIPMap: Map[Int, ChannelID] = extractCloseIPMap(subject)
      val openIPMap: Map[Int, ChannelID] = extractOpenIPMap(subject)
      val activateMap: Map[Int, Int] = extractActivateState(subject)
      val deactivateMap: Map[Int, Int] = extractDeactivateState(subject)
      val observerStatesMap: Map[Int, Int] = observerStates(activateMap, preferentialState )
      val f: File = fillInClass("./src/main/scala/de/tkip/servicehost/serviceactor/stubgen/$TemplateServiceActor.scala", subjectName, subjectId, states, messages, variablesOfSubject, closeIPMap, openIPMap, activateMap, deactivateMap, observerStatesMap)
      val className = f.getName().replaceAll(".scala", "")
      val packagePath_tmp = f.getParent().replace("\\", "/")
      val packagePath = packagePath_tmp.substring(packagePath_tmp.indexOf("/de/") + 1, packagePath_tmp.length()).replaceAll("/", ".")
      val servicePath = saveServiceExport(export, className)
      val future = refAc ?? CreateXMLReferenceMessage(subjectId, packagePath + "." + className, servicePath)
      future pipeTo sender // pipe pattern: wait for completion and send the result
    }
  }

  def loadServiceExport(jsonPath: String): ServiceExport = {
    implicit val mapper: RoleMapper = RoleMapper.noneMapper
    val json_string = scala.io.Source.fromFile(jsonPath).getLines.mkString
    val export: ServiceExport = json_string.parseJson.convertTo[ServiceExport]
    val subjectId = export.subjectId
    val graph_raw: Graph = export.process.graph.get

    // reverse subjects
    val graph_new: Graph = graph_raw.copy(
      subjects = graph_raw.subjects.map({
        case (id, subj) if (id == subjectId) => (id, subj.copy(subjectType = "single", externalType = None, isImplementation = Some(true)))
        case (id, subj) if (subj.subjectType == "single") => (id, subj.copy(subjectType = "external", externalType = Some("interface")))
        case (id, subj) => (id, subj)
      })
    )
    export.copy(process = export.process.copy(graph = Some(graph_new)))
  }

  def extractStates(subject: GraphSubject): Map[Int, State] = {
    val nodes: Map[Short, GraphNode] = subject.macros.values.head.nodes // just take the first macro..
    val edges: Seq[GraphEdge] = subject.macros.values.head.edges // just take the first macro..
    val textMap: Map[Int, String] = for ((id, node) <- nodes) yield {
        if (node.text != null) {
          id.toInt -> ("\"" + node.text + "\"")
        } else {
          id.toInt -> ("\"" + "ActionState" + node.id + "\"")
        }
      }

    // will be returned
    val statesList: Map[Int, State] = for ((id, node) <- nodes) yield {
      node.nodeType match {
        case StateType.ReceiveStateString => (id.toInt -> ReceiveState(id.toInt))
        case StateType.SendStateString => (id.toInt -> SendState(id.toInt))
        case StateType.ActStateString => (id.toInt -> ActionState(id.toInt))
        case StateType.EndStateString => (id.toInt -> ExitState(id.toInt))
        case StateType.DecisionStateString => (id.toInt -> DecisionState(id.toInt))
        case StateType.CloseIPStateString => (id.toInt -> CloseIPState(id.toInt))
        case StateType.OpenIPStateString => (id.toInt) -> OpenIPState(id.toInt)
        case StateType.ActivateStateString => (id.toInt) -> ActivateState(id.toInt)
        case StateType.DeactivateStateString => (id.toInt) -> DeactivateState(id.toInt)
        case _ => (id.toInt -> null)
      }
    }
    for ((id, node) <- nodes) {
      if (node.isStart && node.isMajorStartNode) {
        startNodeIndex = id.toString
      }
    }

    for (edge <- edges) {
      if (!edgeMap.contains(edge.startNodeId.toInt)) {
        //  every startNode and its edges
        val edgeList = List((edge.text, edge.edgeType, edge.endNodeId.toInt))
        edgeMap += edge.startNodeId.toInt -> edgeList

      } else {
        val edgeList = edgeMap(edge.startNodeId.toInt)
        edgeMap += edge.startNodeId.toInt -> ((edge.text, edge.edgeType, edge.endNodeId.toInt) :: edgeList)
      }
      val startNodeId: Int = edge.startNodeId.toInt
      val state: State = statesList(startNodeId)

      // set exit type of its starting node
      // quotes needs to be escaped, as the case class is printed into source code
      state.exittype = "\"" + edge.edgeType + "\""
      state.text = textMap(startNodeId)
      if (edge.target.isDefined) {
        val t = edge.target.get
        val target: String = state match {
          case (ReceiveState(_, _, _, _, _, _, _)) => {
            if(edge.priority.toInt > 1) {
              preferentialState = (state.id, edge.priority.toInt):: preferentialState
            }
            "Target(\"" + t.subjectId + "\"," + t.min + "," + t.max + "," + t.createNew + "," + "\"" + t.variableId.getOrElse("") + "\")"
          }

          case (SendState(_, _, _, _, _, _, _)) => {
            "Target(\"" + t.subjectId + "\"," + t.min + "," + t.max + "," + t.createNew + "," + "\"" + t.variableId.getOrElse("") + "\")"
          }
          case _ => null
        }
        // quotes needs to be escaped, as the case class is printed into source code
        val text = "\"" + edge.text + "\""
        val endId = edge.endNodeId.toInt

        // add this edge to its starting node
        state.targets += (text -> target)
        state.targetIds += (text -> endId)

      } else {
        if (edgeMap(startNodeId.toInt).size == 1) {
          val text = "\"" + edge.startNodeId.toString + "\""
          state.targetIds += (text -> edge.endNodeId)
        } else {
          // multi edges
          state.targetIds.clear()
          for (i <- 0 until edgeMap(startNodeId.toInt).size) {
            val text = "\"" + edgeMap(startNodeId.toInt)(i)._1 + "\""
            if (edgeMap(startNodeId.toInt)(i)._2.equals("timeout")) {
              val newText = "\"" + "timeout " + edgeMap(startNodeId.toInt)(i)._1 + "s" + "\""
              state.targetIds += (newText -> edgeMap(startNodeId.toInt)(i)._3)
            } else {
              state.targetIds += (text -> edgeMap(startNodeId.toInt)(i)._3)
            }
          }
        }
      }
      // whether the current state uses variable or not.
      state match {
        case (ReceiveState(_, _, _, _, _, _, _) | SendState(_, _, _, _, _, _, _)) => {
          state.variableId = "\"" + edge.variableId.getOrElse() + "\""

          if (edge.correlationId.get.equals("")) {
            state.correlationId = "\"" + 0 + "\""
          } else {
            state.correlationId = "\"" + edge.correlationId.get + "\""
          }
        }
        case _ => {
          var vID = ""
          for ((id, node) <- nodes) {
            if (id == startNodeId) {
              vID = node.variableId.getOrElse("")
            }
          }
          state.variableId = "\"" + vID + "\""
          state.correlationId = "\"" + "" + "\""

          var newVariable = edge.variableId.getOrElse("") // some action need to create a new Variable
          if (vID != "") {
            if (vID == newVariable) {
              // do Nothing
            }
            else if ((vID != newVariable) && (newVariable != "")) {
              isCreatedNewVariable += state.id -> newVariable
            }
          } else {
            if (newVariable != "") {
              isCreatedNewVariable += state.id -> newVariable
            }
          }

        }
      }
    }
    statesList
  }

  def extractCloseIPMap(subject: GraphSubject): Map[Int, ChannelID] = {
    val nodes: Map[Short, GraphNode] = subject.macros.values.head.nodes // just take the first macro..
    var closeIPList = Map[Int, ChannelID]()
    for ((id, node) <- nodes) {
      if (node.nodeType.equals("$closeip")) {
        (node.options.subjectId, node.options.messageId) match {
          case (subj, msg) if ((subj != None) && (msg != None)) => closeChannelID = (subj.get, msg.get)
          case (subj, msg) if ((subj == None) && (msg != None)) => closeChannelID = ("", msg.get)
          case (subj, msg) if ((subj != None) && (msg == None)) => closeChannelID = (subj.get, "")
          case (subj, msg) if ((subj == None) && (msg == None)) => closeChannelID = ("", "")
        }
        closeIPList += node.id.toInt -> closeChannelID
      }
    }
    closeIPList
  }

  def extractOpenIPMap(subject: GraphSubject): Map[Int, ChannelID] = {
    val nodes: Map[Short, GraphNode] = subject.macros.values.head.nodes // just take the first macro..
    var openIPList = Map[Int, ChannelID]()
    for ((id, node) <- nodes) {
      if (node.nodeType.equals("$openip")) {
        (node.options.subjectId, node.options.messageId) match {
          case (subj, msg) if ((subj != None) && (msg != None)) => openChannelID = (subj.get, msg.get)
          case (subj, msg) if ((subj == None) && (msg != None)) => openChannelID = ("", msg.get)
          case (subj, msg) if ((subj != None) && (msg == None)) => openChannelID = (subj.get, "")
          case (subj, msg) if ((subj == None) && (msg == None)) => openChannelID = ("", "")
        }
        openIPList += node.id.toInt -> openChannelID
      }
    }
    openIPList
  }

  def extractActivateState(subject: GraphSubject): Map[Int, Int] = {
    val nodes: Map[Short, GraphNode] = subject.macros.values.head.nodes // just take the first macro..
    var activateStateMap = Map[Int, Int]()
    for ((id, node) <- nodes) {
      if (node.nodeType.equals("$activatestate")) {
        activateStateMap += node.id.toInt -> node.options.nodeId.get.toInt
      }
    }
    activateStateMap
  }

  def observerStates (activateState: Map[Int, Int], priorityStates: List[Tuple2[Int, Int]]): Map[Int, Int] = {
    var observerState = Map[Int, Int]()
    if(!activateState.isEmpty){
      if(!priorityStates.isEmpty){
        activateState.foreach( actS => {
          priorityStates.foreach( ps => {
            if(actS._2 == ps._1){
              observerState += actS._2 -> ps._2
            }else{
              observerState += actS._2 -> 1
            }
          })
        })
      }else{
        activateState.foreach( actS => {
          observerState += actS._2 -> 1
        })
      }
    }else{
      log.debug("The service does not exist ObserverState!")
    }
    observerState
  }
  def extractDeactivateState(subject: GraphSubject): Map[Int, Int] = {
    val nodes: Map[Short, GraphNode] = subject.macros.values.head.nodes // just take the first macro..
    var deactivateStateMap = Map[Int, Int]()
    for ((id, node) <- nodes) {
      if (node.nodeType.equals("$deactivatestate")) {
        deactivateStateMap += node.id.toInt -> node.options.nodeId.get.toInt
      }
    }
    deactivateStateMap
  }

  def fillInClass(classPath: String, name: String, id: String, states: Map[Int, State], messages: Map[String, String], variablesOfSubject: Map[String, String], closeIPMap: Map[Int, ChannelID], openIPMap: Map[Int, ChannelID], activateState: Map[Int, Int], deactivateState: Map[Int, Int], observerStatesMap: Map[Int, Int]): File = {
    var classText = scala.io.Source.fromFile(classPath).mkString
    classText = classText.replace("$SERVICENAME", name)
    classText = classText.replace("$SERVICEID", id)
    classText = classText.replace("$INPUTPOOL", inputPool.toString)
    classText = classText.replace("$STARTNODEINDEX", startNodeIndex)
    if(observerStatesMap.isEmpty){
      classText = classText.replace("//$OBSERVERSTATES$//", "")
    }else{
      var text = ""
      for((stateId, priority) <- observerStatesMap){
        text = text + "   " + stateId + " -> " + priority + ",\n "
      }
      classText = classText.replace("//$OBSERVERSTATES$//", text.subSequence(0, text.length - 3))
    }

    var text = ""
    for (state <- states.values) {
      state match {
        case s: ActionState => {
          text = text + "\n     "+ (s.toString.replaceFirst("ActionState", s.text.replaceAll("\"", "").replaceAll(" ", "").replaceAll("\\p{Punct}", ""))) + ",\n"
        }
        case s: DecisionState => {
          text = text + "\n     "+ (s.toString.replaceFirst("DecisionState", s.text.replaceAll("\"", "").replaceAll(" ", "").replaceAll("\\p{Punct}", ""))) + ",\n"
        }
        case _ => text = text + "\n     "+ state + ",\n"
      }
    }
    classText = classText.replace("//$EMPTYSTATE$//", text.subSequence(0, text.length - 2))

    var f = new File(classPath.replace("$Template", name))
    if (f.exists()) {
      var i = 2
      while (f.exists()) {
        f = new File(classPath.replace("$Template", name + i))
        i = i + 1
      }
      classText = classText.replace("$TemplateServiceActor", name + (i - 1) + "ServiceActor")
    } else {
      classText = classText.replace("$TemplateServiceActor", name + "ServiceActor")
    }
    classText = fillInMessages(classText, messages) // add Messages

    if (!variablesOfSubject.isEmpty) {
      classText = fillInVariables(variablesOfSubject) // add Variables
    } else {
      classText = classText.replace("//$EMPTYVARIABLES$//", "")
    }
    def fillInVariables(variables: Map[String, String]): String = {
      var text = ""
      text = text + "\n variablesOfSubject = Map("
      for ((vId, vType) <- variables) {
        text = text + "\"" + vType + "\" -> \"" + vId + "\","
      }
      classText.replace("//$EMPTYVARIABLES$//", text.subSequence(0, text.length - 1) + "\n)")
    }

    if (!closeIPMap.isEmpty) {
      classText = fillInCloseIPText(closeIPMap) //add closeIPMAP
    } else {
      classText = classText.replace("//$EMPTYCLOSEIP$//", "")
    }
    def fillInCloseIPText(closeIPMap: Map[Int, ChannelID]): String = {
      var text = ""
      text = text + "\n closeIPMap = Map("
      for ((nodeId, closeIPChannelID) <- closeIPMap) {
        text = text + "\n     " + nodeId + " -> " + "( " + "\"" + closeIPChannelID._1 + "\", " + "\"" + closeIPChannelID._2 + "\"" + "),\n"
      }
      classText.replace("//$EMPTYCLOSEIP$//", text.subSequence(0, text.length - 2) + "\n)")
    }

    if (!openIPMap.isEmpty) {
      classText = fillInOpenIPText(openIPMap)
    } else {
      classText = classText.replace("//$EMPTYOPENIP$//", "")
    }

    def fillInOpenIPText(openIPMap: Map[Int, ChannelID]): String = {
      var text = ""
      text = text + "\n openIPMap = Map("
      for ((nodeId, openIPChannelID) <- openIPMap) {
        text = text + "\n     " + nodeId + " -> " + "( " + "\"" + openIPChannelID._1 + "\", " + "\"" + openIPChannelID._2 + "\"" + "),\n"
      }
      classText.replace("//$EMPTYOPENIP$//", text.subSequence(0, text.length - 2) + "\n)")
    }

    if (!activateState.isEmpty) {
      classText = fillInActivateState(activateState)
    } else {
      classText = classText.replace("//$EMPTYACTIVATESTATE//", "")
    }
    def fillInActivateState(activateState: Map[Int, Int]): String = {
      var text: String = ""
      text = text + "\n activateStateMap = Map( "
      for ((activateStateId, stateId) <- activateState) {
        text = text + "\n   " + activateStateId + " -> " + stateId + ",\n"
      }
      classText.replace("//$EMPTYACTIVATESTATE//", text.subSequence(0, text.length - 2) + "\n)")
    }

    if(!deactivateState.isEmpty){
      classText = fillInDeactivateState(deactivateState)
    }else{
      classText = classText.replace("//$EMPTYDEACTIVATESTATE//", "")
    }

    def fillInDeactivateState(activateState: Map[Int, Int]): String = {
      var text: String = ""
      text = text + "\n deactivateStateMap = Map( "
      for ((deactivateStateId, stateId) <- deactivateState) {
        text = text + "\n   " + deactivateStateId + " -> " + stateId + ",\n"
      }
      classText.replace("//$EMPTYDEACTIVATESTATE//", text.subSequence(0, text.length - 2) + "\n)")
    }


    var impementation: String = ""
    for (state <- states.values) {
      state match {
        case s: ActionState => {

          impementation = impementation + "\n  case class " + state.text.replaceAll("\"", "").replaceAll(" ", "").replaceAll("\\p{Punct}", "") + "(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String, override val correlationId: String) extends State(\"action\", id, exitType, targets, targetIds, text, variableId, correlationId) {\n"
          impementation = impementation + "\n    def process( )(implicit actor: ServiceActor) {"
          if (isCreatedNewVariable.contains(state.id)) {
            impementation = impementation + "\n       //  create a new Variable and store it into sendingvariable"
            impementation = impementation + "\n      val newVariableType = " + "\"" + isCreatedNewVariable(state.id) + "\""
          }
          impementation = impementation + "\n        if(getState(id).variableId != null) {"
          impementation = impementation + "\n"
          impementation = impementation + "\n         }"

          if (edgeMap(s.id).length > 1) {
            for (i <- 0 until edgeMap(s.id).length) {

              impementation = impementation + "\n			  if(true) {" + "// custom condition"
              impementation = impementation + "\n           branchCondition = " + "\"" + edgeMap(s.id)(i)._1 + "\""
              impementation = impementation + "\n      }"
            }
          }
          impementation = impementation + "\n    	}"
          impementation = impementation + "\n  }"
        }

        case s: DecisionState => {
          impementation = impementation + "\n  case class " + state.text.replaceAll("\"", "").replaceAll(" ", "").replaceAll("\\p{Punct}", "") + "(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State(\"decision\", id, exitType, targets, targetIds, text, variableId) {\n"
          impementation = impementation + "\n    def process( )(implicit actor: ServiceActor) {"
          for (i <- 0 until edgeMap(s.id).size) {
            impementation = impementation + "\n		    if(true) {" + "// custom condition"
            impementation = impementation + "\n           branchCondition = " + "\"" + edgeMap(s.id)(i)._1 + "\""
            impementation = impementation + "\n       }"
          }
          impementation = impementation + "\n         changeState(id)"
          impementation = impementation + "\n    }"
          impementation = impementation + "\n }"
        }

        case _ =>
      }
    }
    classText = classText.replace("//$ACTIONSTATESIMPLEMENTATION$//", impementation)
    val pw = new java.io.PrintWriter(f.getAbsolutePath())
    pw.print(classText)
    pw.close()
    f
  }

  def fillInMessages(classText: String, messages: Map[String, String]): String = {
    var text = ""
    for ((name, msgType) <- messages) {
      text = text + "\"" + msgType + "\" -> \"" + name + "\","
    }
    classText.replace("//$EMPTYMESSAGE$//", text.subSequence(0, text.length - 1))
  }


  def saveServiceExport(export: ServiceExport, className: String): String = {
    implicit val mapper: RoleMapper = RoleMapper.noneMapper
    val outputDir: File = new File("./src/main/resources/service_JSONs")
    if (!outputDir.exists())
      outputDir.mkdirs()
    val json_string = export.toJson.prettyPrint
    val outputFile = new File(outputDir + File.separator + className + ".json");
    val out = new FileWriter(outputFile);
    out.write(json_string);
    out.close();
    outputFile.getPath()
  }
}
