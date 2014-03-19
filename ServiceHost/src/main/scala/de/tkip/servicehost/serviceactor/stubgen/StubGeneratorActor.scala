package de.tkip.servicehost.serviceactor.stubgen
import scala.util.parsing.json.JSON
import de.tkip.sbpm.rest.GraphJsonProtocol._
import scala.collection.immutable.Map
import spray.json._
import java.io.File
import akka.actor.Actor
import scala.reflect.ClassTag
import de.tkip.servicehost.ActorLocator
import de.tkip.servicehost.Messages._
import akka.actor.Props
import de.tkip.servicehost.ReferenceXMLActor

class StubGeneratorActor extends Actor {
  abstract class State {
    def id: Int
    def exittype: String
    var targets: Map[String, String]
    var targetIds: Map[String, Int]
  }
  case class Target(id: String, min: Int, max: Int, createNew: Boolean, variable: String)
  case class ReceiveState(id: Int, exittype: String, var targets: Map[String, String], var targetIds: Map[String, Int]) extends State
  case class SendState(id: Int, exittype: String, var targets: Map[String, String], var targetIds: Map[String, Int]) extends State
  case class ExitState(id: Int, exittype: String, var targets: Map[String, String], var targetIds: Map[String, Int]) extends State
  case class ActionState(id: Int, exittype: String, var targets: Map[String, String], var targetIds: Map[String, Int]) extends State

  //   val simpleGraphSource = Source.fromURL(getClass.getResource("service_export_test_name2.json")).mkString
  //  val domainGraph = json_string.asJson.convertTo[Graph](graphJsonFormat)
  //  val domainGraph = json_string.asJson.convertTo[Graph](graphJsonFormat)

  def receive = {
    case path: String => {
      val (name, id, states, messages) = extractStates(path)
      fillInClass("./src/main/scala/de/tkip/servicehost/serviceactor/stubgen/$TemplateServiceActor.scala", name, id, states,messages)
    }
  }
  def extractStates(jsonPath: String): (String, String, List[State],Map[String,String]) = {
    val json_string = scala.io.Source.fromFile(jsonPath).getLines.mkString
    val json: Option[Any] = JSON.parseFull(json_string)
    val process: Map[String, Any] = Map() ++ json.get.asInstanceOf[Map[String, Any]]
    var states: Map[Int, Any] = Map()
    var statesList: List[State] = List()

    val graph = process("graph").asInstanceOf[Map[String, Any]]
    val messages = process.getOrElse("messages", Map[String,String]()).asInstanceOf[Map[String, String]]
    val macros = graph("macros").asInstanceOf[List[Map[String, Any]]]
    val macro = macros(0)
    val nodes = macro("nodes").asInstanceOf[List[Map[String, Any]]]
    for (node <- nodes) yield {
      states = states + (node.asInstanceOf[Map[String, Any]]("id").asInstanceOf[Double].toInt -> (node, scala.collection.mutable.Map(), scala.collection.mutable.Map()))
      if (node("type") == "end")
        statesList = statesList ::: List(ExitState(node("id").asInstanceOf[Double].toInt, null, null, null))
    }
    val edges = macro("edges").asInstanceOf[List[Map[String, Any]]]
    for (edge <- edges) yield {
      val start: Map[String, Any] = states(edge("start").asInstanceOf[Double].toInt).asInstanceOf[Tuple3[Map[String, Any], Map[String, String], Map[String, Int]]]._1
      var t: Map[String, Any] = Map()
      if (edge.contains("target") && !(edge("target") == ""))
        t = edge("target").asInstanceOf[Map[String, Any]]
      var state: State = null
      start("type") match {
        case "receive" =>
          state = ReceiveState(start("id").asInstanceOf[Double].toInt, "\"" + edge("type").asInstanceOf[String] + "\"", null, null)
        case "send" =>
          state = SendState(start("id").asInstanceOf[Double].toInt, "\"" + edge("type").asInstanceOf[String] + "\"", null, null)
        case "action" =>
          state = ActionState(start("id").asInstanceOf[Double].toInt, "\"" + edge("type").asInstanceOf[String] + "\"", null, null)
      }
      var target: String = null
      state match {
        case s: ActionState =>
        case s: ExitState =>
        case _ => target = "Target(\"" + t("id").asInstanceOf[String] + "\"," + t("min").asInstanceOf[Double].toInt + "," + t("max").asInstanceOf[Double].toInt + "," + t("createNew").asInstanceOf[Boolean] + "," + "\"" + t.getOrElse("variable", "").asInstanceOf[String] + "\")"
      }
      val text = edge("text")
      val endId = edge("end").asInstanceOf[Double].toInt
      states(edge("start").asInstanceOf[Double].toInt).asInstanceOf[Tuple3[Map[String, Any], scala.collection.mutable.Map[String, String], Map[String, Int]]]._2("\"" + text + "\"") = target
      states(edge("start").asInstanceOf[Double].toInt).asInstanceOf[Tuple3[Map[String, Any], Map[String, String], scala.collection.mutable.Map[String, Int]]]._3("\"" + text + "\"") = endId
      state.targets = Map() ++ states(edge("start").asInstanceOf[Double].toInt).asInstanceOf[Tuple3[Map[String, Any], Map[String, String], Map[String, Int]]]._2
      state.targetIds = Map() ++ states(edge("start").asInstanceOf[Double].toInt).asInstanceOf[Tuple3[Map[String, Any], Map[String, String], Map[String, Int]]]._3
      statesList = statesList :+ state
    }
    (graph("name").asInstanceOf[String], graph("id").asInstanceOf[String], statesList,messages)
  }

  def fillInClass(classPath: String, name: String, id: String, states: List[State], messages:Map[String,String]) {
    var classText = scala.io.Source.fromFile(classPath).mkString
    classText = classText.replace("$SERVICEID", id)
    var text = ""
    for (state <- states) {
      state match {
        case s: ActionState => {
          text = text + (s.toString().replaceFirst("\\(", s.id + "(") + ",")
        }
        case _ => text = text + state + ","
      }
    }
    classText = classText.replace("//$EMPTYSTATE$//", text.subSequence(0, text.length - 1))
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
    var impementation:String=""
    for (state <- states) {
      state match {
        case s: ActionState => {
          impementation = impementation + "\n  case class ActionState" + state.id + "(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int]) extends State(\"action\", id, exitType, targets, targetIds) {\n"
          impementation = impementation + "\n    val stateName = \"\" //TODO state name\n"
          impementation = impementation + "\n    def process()(implicit actor: ServiceActor) {"
          impementation = impementation + "\n      actor.setMessage(\"\") //TODO set message"
          impementation = impementation + "\n      actor.changeState()"
          impementation = impementation + "\n    }"
          impementation = impementation + "\n  }"
        }
        case _=>
      }
    }
    classText = classText.replace("//$ACTIONSTATESIMPLEMENTATION$//", impementation)
    val pw = new java.io.PrintWriter(f.getAbsolutePath())
    pw.print(classText)
    pw.close()
    val packagePath = f.getParent().replace("\\", "/")
    registerService(id, f.getName().replaceAll(".scala", ""), packagePath.substring(packagePath.indexOf("/de/") + 1, packagePath.length()).replaceAll("/", "."))
  }
  
  def fillInMessages(classText: String, messages:Map[String,String]):String ={
    var text=""
    for((name,msgType) <- messages){
      text=text+ "\""+msgType+"\" -> \""+name+"\","
    }
    classText.replace("//$EMPTYMESSAGE$//", text.subSequence(0, text.length - 1))
  }

  def registerService(id: String, className: String, packagePath: String) {
    val refAc = this.context.actorOf(Props[ReferenceXMLActor], "reference-xml-actor")

    refAc ! CreateXMLReferenceMessage(id, packagePath + "." + className)
  }
}



