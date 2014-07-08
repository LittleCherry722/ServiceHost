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
import java.nio.file.Files
import java.io.FileReader
import java.io.FileWriter

import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
//import scala.concurrent.ExecutionContext;

class StubGeneratorActor extends Actor {
  implicit val timeout = Timeout(15 seconds)

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

  def receive = {
    case path: String => {
      println("StubGeneratorActor received path " + path)
      val (name, id, states, messages) = extractStates(path)
      val future = fillInClass("./src/main/scala/de/tkip/servicehost/serviceactor/stubgen/$TemplateServiceActor.scala", name, id, states, messages, path)
      future pipeTo sender // pipe pattern: wait for completion and send the result
    }
  }
  
  def extractStates(jsonPath: String): (String, String, Map[Int,State],Map[String,String]) = {
    val json_string = scala.io.Source.fromFile(jsonPath).getLines.mkString
    val json: Option[Any] = JSON.parseFull(json_string)
    val process: Map[String, Any] = Map() ++ json.get.asInstanceOf[Map[String, Any]]
    var states: Map[Int, Any] = Map()
    var statesList: Map[Int,State] = Map()

    val graph = process("graph").asInstanceOf[Map[String, Any]]
    val messages = process.getOrElse("messages", Map[String,String]()).asInstanceOf[Map[String, String]]
    val macros = graph("macros").asInstanceOf[List[Map[String, Any]]]
    val macro = macros(0)
    val nodes = macro("nodes").asInstanceOf[List[Map[String, Any]]]
    for (node <- nodes) yield {
      states = states + (node.asInstanceOf[Map[String, Any]]("id").asInstanceOf[Double].toInt -> (node, scala.collection.mutable.Map(), scala.collection.mutable.Map()))
      if (node("type") == "end")
        statesList = statesList ++ Map(node("id").asInstanceOf[Double].toInt -> ExitState(node("id").asInstanceOf[Double].toInt, null, Map(), Map()))
    }
    val edges = macro("edges").asInstanceOf[List[Map[String, Any]]]
    for (edge <- edges) yield {
      val start: Map[String, Any] = states(edge("start").asInstanceOf[Double].toInt).asInstanceOf[Tuple3[Map[String, Any], Map[String, String], Map[String, Int]]]._1
      var t: Map[String, Any] = Map()
      if (edge.contains("target") && !(edge("target") == ""))
        t = edge("target").asInstanceOf[Map[String, Any]]
      var state: State = null
      start("type") match {
        case "receive" =>{
          if(statesList.contains(start("id").asInstanceOf[Double].toInt))
            state = statesList(start("id").asInstanceOf[Double].toInt)
          else 
            state = ReceiveState(start("id").asInstanceOf[Double].toInt, "\"" + edge("type").asInstanceOf[String] + "\"", null, null)
        }
        case "send" =>{
          if(statesList.contains(start("id").asInstanceOf[Double].toInt))
            state = statesList(start("id").asInstanceOf[Double].toInt)
          else 
            state = SendState(start("id").asInstanceOf[Double].toInt, "\"" + edge("type").asInstanceOf[String] + "\"", null, null)
        }
        case "action" =>{
           if(statesList.contains(start("id").asInstanceOf[Double].toInt))
            state = statesList(start("id").asInstanceOf[Double].toInt)
           else
            state = ActionState(start("id").asInstanceOf[Double].toInt, "\"" + edge("type").asInstanceOf[String] + "\"", null, null)
        }
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
      statesList = statesList ++ Map(state.id -> state)
    }
    (graph("name").asInstanceOf[String], graph("id").asInstanceOf[String], statesList,messages)
  }

  def fillInClass(classPath: String, name: String, id: String, states: Map[Int,State], messages: Map[String,String], json: String): Future[Any] = {
    var classText = scala.io.Source.fromFile(classPath).mkString
    classText = classText.replace("$SERVICEID", id)
    var text = ""
    for (state <- states.values) {
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
    for (state <- states.values) {
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
    registerService(id, f.getName().replaceAll(".scala", ""), packagePath.substring(packagePath.indexOf("/de/") + 1, packagePath.length()).replaceAll("/", "."), json)
  }
  
  def fillInMessages(classText: String, messages:Map[String,String]):String ={
    var text=""
    for((name,msgType) <- messages){
      text=text+ "\""+msgType+"\" -> \""+name+"\","
    }
    classText.replace("//$EMPTYMESSAGE$//", text.subSequence(0, text.length - 1))
  }

  def registerService(id: String, className: String, packagePath: String, json: String): Future[Any] = {
    val servicePath=copyFile(json)
    val refAc = this.context.actorOf(Props[ReferenceXMLActor], "reference-xml-actor")

    refAc ? CreateXMLReferenceMessage(id, packagePath + "." + className, servicePath)
  }
  
  def copyFile(path:String):String ={
    val inputFile:File = new File(path); 
    val dir:File=new File("./src/main/resources/service_JSONs")
    if(!dir.exists())
      dir.mkdirs()
    val outputFile = new File(dir+File.separator+inputFile.getName()); 

    val out = new FileWriter(outputFile); 
    val json_string = scala.io.Source.fromFile(path).getLines.mkString
    out.write(json_string); 
    out.close();
    outputFile.getPath()
  }
}



