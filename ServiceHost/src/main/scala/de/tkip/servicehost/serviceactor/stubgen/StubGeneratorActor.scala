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

//object sss extends App {
//  val r = new StubGeneratorActor
//  val (name, states) = r.extractStates("D:/study/TKIP/ServiceHost/src/main/scala/de/tkip/servicehost/service_export_test_name2.json")
//  r.fillInClass("D:\\study\\TKIP\\ServiceHost\\src\\main\\scala\\de\\tkip\\servicehost\\serviceactor\\stubgen\\$TemplateServiceActor.scala", name,states)
//  println()
//}

class StubGeneratorActor extends Actor {
  sealed class State
  case class Target(id: String, min: Int, max: Int, createNew: Boolean, variable: String)
  case class ReceiveState(id: Int, exittype: String, target: String, targetId: Int) extends State
  case class SendState(id: Int, exittype: String, target: String, targetId: Int) extends State
  case class ExitState(id: Int, exittype: Any, t: Any, target: Int) extends State
  case class ActionState(id: Int, exittype: String, target: String, targetId: Int) extends State

  //   val simpleGraphSource = Source.fromURL(getClass.getResource("service_export_test_name2.json")).mkString
  //  val domainGraph = json_string.asJson.convertTo[Graph](graphJsonFormat)
  //  val domainGraph = json_string.asJson.convertTo[Graph](graphJsonFormat)

  def receive = {
    case path: String => {
      val (name, id, states) = extractStates(path)
      fillInClass("./src/main/scala/de/tkip/servicehost/serviceactor/stubgen/$TemplateServiceActor.scala", name, id, states)
    }
  }
  def extractStates(jsonPath: String): (String, String, List[State]) = {
    val json_string = scala.io.Source.fromFile(jsonPath).getLines.mkString
    val json: Option[Any] = JSON.parseFull(json_string)
    val process: Map[String, Any] = json.get.asInstanceOf[Map[String, Any]]
    var states: Map[Int, Any] = Map()
    var statesList: List[State] = List()

    val graph = process("graph").asInstanceOf[Map[String, Any]]
    val macros = graph("macros").asInstanceOf[List[Map[String, Any]]]
    val macro = macros(0)
    val nodes = macro("nodes").asInstanceOf[List[Map[String, Any]]]
    for (node <- nodes) yield {
      states = states + (node.asInstanceOf[Map[String, Any]]("id").asInstanceOf[Double].toInt -> node)
      if (node("type") == "end")
        statesList = statesList ::: List(ExitState(node("id").asInstanceOf[Double].toInt, null, null, -1))
    }
    val edges = macro("edges").asInstanceOf[List[Map[String, Any]]]
    for (edge <- edges) yield {
      val start: Map[String, Any] = states(edge("start").asInstanceOf[Double].toInt).asInstanceOf[Map[String, Any]]
      var t: Map[String, Any] = null
      if (edge.contains("target"))
        t = edge("target").asInstanceOf[Map[String, Any]]
      start("type") match {
        case "receive" => {
          val taget = "Target(\"" + t("id").asInstanceOf[String] + "\"," + t("min").asInstanceOf[Double].toInt + "," + t("max").asInstanceOf[Double].toInt + "," + t("createNew").asInstanceOf[Boolean] + "," + "\"" + t.getOrElse("variable", "").asInstanceOf[String] + "\")"
          statesList = statesList ::: List(ReceiveState(start("id").asInstanceOf[Double].toInt, "\"" + edge("type").asInstanceOf[String] + "\"", taget, edge("end").asInstanceOf[Double].toInt))
        }
        case "send" => {
          val taget = "Target(\"" + t("id").asInstanceOf[String] + "\"," + t("min").asInstanceOf[Double].toInt + "," + t("max").asInstanceOf[Double].toInt + "," + t("createNew").asInstanceOf[Boolean] + "," + "\"" + t.getOrElse("variable", "").asInstanceOf[String] + "\")"
          statesList = statesList ::: List(SendState(start("id").asInstanceOf[Double].toInt, "\"" + edge("type").asInstanceOf[String] + "\"", taget, edge("end").asInstanceOf[Double].toInt))
        }
        case "action" =>
          statesList = statesList ::: List(ActionState(start("id").asInstanceOf[Double].toInt, "\"" + edge("type").asInstanceOf[String] + "\"", null, edge("end").asInstanceOf[Double].toInt))
      }
    }
    (graph("name").asInstanceOf[String], graph("id").asInstanceOf[String], statesList)
  }

  def fillInClass(classPath: String, name: String, id: String, states: List[State]) {
    var classText = scala.io.Source.fromFile(classPath).mkString
    classText = classText.replace("$SERVICEID", id)
    var text = ""
    var actionsCount = 0
    for (state <- states) {
      text = text + state + ","
      state match {
        case ActionState(_, _, _, _) => actionsCount += 1
        case _ =>
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
    if (actionsCount > 0) {
      classText = classText + "\n  case class ActionState(override val id: Int, override val exitType: String, override val target: Target, override val targetId: Int) extends State(\"action\", id, exitType, target, targetId) {"
      classText = classText + "\n    def process()(implicit actor: ServiceActor) {"
      classText = classText + "\n      // TODO implement internal behavior"
      classText = classText + "\n    }"
      classText = classText + "\n  }"
    }
    val pw = new java.io.PrintWriter(f.getAbsolutePath())
    pw.print(classText)
    pw.close()
    val packagePath = f.getParent().replace("\\", "/")
    registerService(id, f.getName().replaceAll(".scala", ""), packagePath.substring(packagePath.indexOf("/de/") + 1, packagePath.length()).replaceAll("/", "."))
  }

  def registerService(id: String, className: String, packagePath: String) {
    //    val refAc = ActorLocator.referenceXMLActor
    val refAc = this.context.actorOf(Props[ReferenceXMLActor], "reference-xml-actor")

    refAc ! CreateXMLReferenceMessage(id, packagePath + "." + className)
  }
}



