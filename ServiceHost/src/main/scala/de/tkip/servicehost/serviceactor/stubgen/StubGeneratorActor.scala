package de.tkip.servicehost.serviceactor.stubgen

import java.nio.file.Files
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import scala.reflect.ClassTag
import scala.collection.immutable.Map
import scala.collection.mutable.{ Map => MutableMap, ArrayBuffer }
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
import de.tkip.sbpm.rest.JsonProtocol.{ GraphHeader, createGraphHeaderFormat }
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
  }
  case class ReceiveState(id: Int, var exittype: String = null, targets: MutableMap[String, String] = MutableMap(), targetIds: MutableMap[String, Int] = MutableMap(), var text: String = null) extends State
  case class SendState(id: Int, var exittype: String = null, targets: MutableMap[String, String] = MutableMap(), targetIds: MutableMap[String, Int] = MutableMap(), var text: String = null) extends State
  case class ExitState(id: Int, var exittype: String = null, targets: MutableMap[String, String] = MutableMap(), targetIds: MutableMap[String, Int] = MutableMap(), var text: String = null) extends State
  case class ActionState(id: Int, var exittype: String = null, targets: MutableMap[String, String] = MutableMap(), targetIds: MutableMap[String, Int] = MutableMap(), var text: String = null) extends State

  val edgeMap = scala.collection.mutable.Map[Int, List[String]]() // each node and its edges
  val actionStateNumber = scala.collection.mutable.Map[String, List[Int]]()

  def wrappedReceive = {
    case msg @ GenerateService(path) => {
      log.info("StubGeneratorActor received " + msg)

      val export: ServiceExport = loadServiceExport(path)
      val subjectId = export.subjectId
      val graph: Graph = export.process.graph.get

      val subject: GraphSubject = graph.subjects(subjectId)
      val subjectName = subject.name
      val states = extractStates(subject)
      val messages: Map[String, String] = graph.messages.map({ case (x, m) => (m.id, m.name) })

      val f: File = fillInClass("./src/main/scala/de/tkip/servicehost/serviceactor/stubgen/$TemplateServiceActor.scala", subjectName, subjectId, states, messages)
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
        case StateType.SendStateString    => (id.toInt -> SendState(id.toInt))
        case StateType.ActStateString     => (id.toInt -> ActionState(id.toInt))
        case StateType.EndStateString     => (id.toInt -> ExitState(id.toInt))
        case StateType.VasecStateString   => (id.toInt -> ActionState(id.toInt))
        case x                            => { log.error("unknown StateType: {}", x); (id.toInt -> null) }
      }
    }

    for ((id, node) <- nodes) {

      if (node.nodeType == "action") {
        if (!actionStateNumber.contains("action")) { //how many actionstate in this service
          val actionStateId = List(id.toInt)
          actionStateNumber += "action" -> actionStateId
        } else {
          val actionStateId = actionStateNumber("action")
          actionStateNumber += "action" -> (id.toInt :: actionStateId)
        }
      }
    }

    for (edge <- edges) {

      if (!edgeMap.contains(edge.startNodeId.toInt)) { //  every startNode and its edges
        val edgeList = List(edge.text)
        edgeMap += edge.startNodeId.toInt -> edgeList
      } else {
        val edgeList = edgeMap(edge.startNodeId.toInt)
        edgeMap += edge.startNodeId.toInt -> (edge.text :: edgeList)
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
          case (ReceiveState(_, _, _, _, _) | SendState(_, _, _, _, _)) => "Target(\"" + t.subjectId + "\"," + t.min + "," + t.max + "," + t.createNew + "," + "\"" + t.variableId.getOrElse("") + "\")"
          case _ => null
        }

        // quotes needs to be escaped, as the case class is printed into source code
        val text = "\"" + edge.text + "\""

        val endId = edge.endNodeId.toInt

        // add this edge to its starting node
        state.targets += (text -> target)
        state.targetIds += (text -> endId)
      } else {
        val text = "\"" + edge.startNodeId.toString + "\""
        state.targetIds += (text -> edge.endNodeId)
      }
    }

    statesList
  }

  def fillInClass(classPath: String, name: String, id: String, states: Map[Int, State], messages: Map[String, String]): File = {
    var classText = scala.io.Source.fromFile(classPath).mkString
    classText = classText.replace("$SERVICEID", id)
    var texts: ArrayBuffer[String] = ArrayBuffer()
    for (state <- states.values) {
      state match {
        case s: ActionState => {
          texts += s.toString.replaceFirst("ActionState", s.text.replaceAll("\"", "").replaceAll(" ", "").replaceAll(":", "_"))
        }
        case _ => texts += state.toString
      }
    }
    classText = classText.replace("//$EMPTYSTATE$//", texts.mkString(",\n      "))
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
    classText = fillInMessages(classText, messages)
    var impementation: String = ""
    for (state <- states.values) {
      state match {
        case s: ActionState => {
          impementation = impementation + "\n\n  case class " + state.text.replaceAll("\"", "").replaceAll(" ", "").replaceAll(":", "_") + "(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State(\"action\", id, exitType, targets, targetIds, text) {\n"
          impementation = impementation + "\n    val stateName = \"\" //TODO state name\n"
          impementation = impementation + "\n    def process()(implicit actor: ServiceActor) {"

          if (edgeMap(s.id).length > 1) {
            impementation = impementation + "\n      messageContent match {"
            for (i <- 0 until edgeMap(s.id).length) {
              impementation = impementation + "\n        case _ if (true) => actor.setMessage(" + edgeMap(s.id)(i) + "(messageContent))"
            }
            impementation = impementation + "\n      }"
          } else {
            impementation = impementation + "\n      actor.setMessage(\"\") //TODO set message"
          }

          impementation = impementation + "\n      actor.changeState()\n"
          for ((startNode, edge) <- edgeMap) {
            if (edgeMap(startNode).length > 1) {
              for (i <- 0 until edgeMap(startNode).length) { //create function.
                impementation = impementation + "\n      def " + edgeMap(startNode)(i) + "(msg: String): String = {\n"
                impementation = impementation + "        msg"
                impementation = impementation + "\n      }"
              }
            }
          }

          impementation = impementation + "\n    }"
          impementation = impementation + "\n  }"
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
    val texts: ArrayBuffer[String] = ArrayBuffer()
    for ((name, msgType) <- messages) {
      texts += "\"" + msgType + "\" -> \"" + name + "\""
    }
    classText.replace("//$EMPTYMESSAGE$//", texts.mkString(",\n      "))
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
