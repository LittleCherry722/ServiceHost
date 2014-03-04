package de.tkip.servicehost.serviceactor.stubgen
import scala.util.parsing.json.JSON
import de.tkip.sbpm.rest.GraphJsonProtocol._
import scala.collection.immutable.Map
import spray.json._

class StubGenerator() {
  sealed class State
  case class ReceiveState(id: Double, target: Double) extends State
  case class SendState(id: Double, target: Double) extends State
  case class ExitState(id: Double, target: Double) extends State

  //   val simpleGraphSource = Source.fromURL(getClass.getResource("service_export_test_name2.json")).mkString
  //  val domainGraph = json_string.asJson.convertTo[Graph](graphJsonFormat)
  //  val domainGraph = json_string.asJson.convertTo[Graph](graphJsonFormat)
  def extractStates(jsonPath: String): List[State] = {
    val json_string = scala.io.Source.fromFile(jsonPath).getLines.mkString
    val json: Option[Any] = JSON.parseFull(json_string)
    val process: Map[String, Any] = json.get.asInstanceOf[Map[String, Any]]
    var states: Map[Double, Any] = Map()
    var statesList: List[State] = List()

    val graph = process("graph").asInstanceOf[Map[String, Any]]
    val macros = graph("macros").asInstanceOf[List[Map[String, Any]]]
    val macro = macros(0)
    val nodes = macro("nodes").asInstanceOf[List[Map[String, Any]]]
    for (node <- nodes) yield {
      states = states + (node.asInstanceOf[Map[String, Any]]("id").asInstanceOf[Double] -> node)
      if (node("type") == "end")
        statesList = statesList :::statesList ::: List(ExitState(node("id").asInstanceOf[Double], -1))
    }
    val edges = macro("edges").asInstanceOf[List[Map[String, Any]]]
    for (edge <- edges) yield {
      val start: Map[String, Any] = states(edge("start").asInstanceOf[Double]).asInstanceOf[Map[String, Any]]
      start("type") match {
        case "receive" =>
          statesList = statesList :::statesList ::: List(ReceiveState(start("id").asInstanceOf[Double], edge("end").asInstanceOf[Double]))
        case "send" =>
          statesList = statesList :::statesList ::: List(SendState(start("id").asInstanceOf[Double], edge("end").asInstanceOf[Double]))
      }
    }
    statesList
  }

  def fillInClass(classPath: String, states: List[State]) {
    var classText = scala.io.Source.fromFile(classPath).getLines.mkString
    var text = ""
    for (state <- states) {
      text = text + state + ","
    }
    classText=classText.replace("//$EMPTYSTATE$//", text.subSequence(0, text.length - 1))
    val pw = new java.io.PrintWriter(classPath)
    pw.print(classText)
    pw.close()
  }
}

object StubGenerator extends App {
  val r = new StubGenerator
  val states = r.extractStates("D:/study/TKIP/ServiceHost/src/main/scala/de/tkip/servicehost/service_export_test_name2.json")
  r.fillInClass("$TemplateServiceActor", states)
  println()
}