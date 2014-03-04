package de.tkip.servicehost.serviceactor.stubgen
import scala.util.parsing.json.JSON
import de.tkip.sbpm.rest.GraphJsonProtocol._
import scala.collection.immutable.Map
import spray.json._
import java.io.File
import akka.actor.Actor

//object sss extends App {
//  val r = new StubGeneratorActor
//  val (name, states) = r.extractStates("D:/study/TKIP/ServiceHost/src/main/scala/de/tkip/servicehost/service_export_test_name2.json")
//  r.fillInClass("D:\\study\\TKIP\\ServiceHost\\src\\main\\scala\\de\\tkip\\servicehost\\serviceactor\\stubgen\\$TemplateServiceActor.scala", name,states)
//  println()
//}

class StubGeneratorActor extends Actor{
  sealed class State
  case class Target(id: String, min: Int, max: Int, createNew: Boolean, variable: String)
  case class ReceiveState(id: Int, exittype: String, target: String, targetId: Int) extends State
  case class SendState(id: Int, exittype: String, target: String, targetId: Int) extends State
  case class ExitState(id: Int, target: Int) extends State

  //   val simpleGraphSource = Source.fromURL(getClass.getResource("service_export_test_name2.json")).mkString
  //  val domainGraph = json_string.asJson.convertTo[Graph](graphJsonFormat)
  //  val domainGraph = json_string.asJson.convertTo[Graph](graphJsonFormat)

  def receive = {
    case path:String=>{
      val (name,states)=extractStates(path)
      fillInClass("D:\\study\\TKIP\\ServiceHost\\src\\main\\scala\\de\\tkip\\servicehost\\serviceactor\\stubgen\\$TemplateServiceActor.scala", name, states)
    }
  }
  def extractStates(jsonPath: String): (String, List[State]) = {
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
        statesList = statesList ::: List(ExitState(node("id").asInstanceOf[Double].toInt, -1))
    }
    val edges = macro("edges").asInstanceOf[List[Map[String, Any]]]
    for (edge <- edges) yield {
      val start: Map[String, Any] = states(edge("start").asInstanceOf[Double].toInt).asInstanceOf[Map[String, Any]]
      val t = edge("target").asInstanceOf[Map[String, Any]]
      val taget = "Target(\""+t("id").asInstanceOf[String]+"\","+ t("min").asInstanceOf[Double].toInt+","+ t("max").asInstanceOf[Double].toInt+"," +t("createNew").asInstanceOf[Boolean]+"," +"\""+t.getOrElse("variable", "").asInstanceOf[String]+"\")"
      start("type") match {
        case "receive" =>
          statesList = statesList ::: List(ReceiveState(start("id").asInstanceOf[Double].toInt, "\""+edge("type").asInstanceOf[String]+"\"", taget, edge("end").asInstanceOf[Double].toInt))
        case "send" =>
          statesList = statesList ::: List(SendState(start("id").asInstanceOf[Double].toInt, "\""+edge("type").asInstanceOf[String]+"\"", taget, edge("end").asInstanceOf[Double].toInt))
      }
    }
    (graph("name").asInstanceOf[String], statesList)
  }

  def fillInClass(classPath: String, name:String, states: List[State]) {
    var classText = scala.io.Source.fromFile(classPath).mkString
    var text = ""
    for (state <- states) {
      text = text + state + ","
    }
    classText = classText.replace("//$EMPTYSTATE$//", text.subSequence(0, text.length - 1))
    var f=new File(classPath.replace("$Template", name))
    if(f.exists()){
      var i=2
      while(f.exists()){
        f=new File(classPath.replace("$Template", name+i))
        i=i+1
      }
    }
    val pw = new java.io.PrintWriter(f.getAbsolutePath())
    pw.print(classText)
    pw.close()
  }
}



