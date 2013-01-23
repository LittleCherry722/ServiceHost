package de.tkip.sbpm.rest.test

import spray.json._
import Tab.graphFormat
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import scala.collection.mutable.ArrayBuffer
import akka.actor.ActorSystem
import akka.util.Timeout
import akka.actor.Props
import akka.pattern.ask
import de.tkip.sbpm.application.ProcessManagerActor
import de.tkip.sbpm.application.SubjectProviderManagerActor
import de.tkip.sbpm.application.miscellaneous.ProcessCreated
import de.tkip.sbpm.application.miscellaneous.ProcessInstanceCreated
import de.tkip.sbpm.application.miscellaneous.AddSubject
import scala.concurrent.Await
import de.tkip.sbpm.application.miscellaneous.CreateProcess
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.miscellaneous.Debug

case class Abc(h: String, in: Int)
case class Graph1(id: Option[Int], graph: String, abc: Abc, processId: Array[Int])

object Tab extends DefaultJsonProtocol {

  implicit val abcFormat = jsonFormat2(Abc)
  implicit val graphFormat = jsonFormat4(Graph1)

  implicit val tarFormat = jsonFormat1(Target)
  implicit val nodeFormat = jsonFormat4(Node)
  implicit val edgeFormat = jsonFormat4(Edge)
  implicit val macFormat = jsonFormat4(Macro)
  implicit val subFormat = jsonFormat3(JSONSubject)
  implicit val proFormat = jsonFormat1(JSONProcess)

}
// TODO im graph heisst es type nicht typ
object MyJSONTry {
  def main(a: Array[String]) {

    val graph = MyJSONTestGraph.processGraph;

    import Tab._

    val abcd = graph.replace("\"type\":", "\"typ\":")

    val abc = abcd.asJson
    //  println(abc)

    println(abc.convertTo[JSONProcess])
    //
    val process = abc.convertTo[JSONProcess]

    ProcessExe.executeProcess(Fkts.parseProcess(process))
  }
}

object ProcessExe {
  def executeProcess(processGraph: ProcessGraph) {

    val system = ActorSystem("JSONParsedProcess")
    val processManager = system.actorOf(Props(new ProcessManagerActor("BT_Application")), name = "BT_Application")
    val subjectProviderManager = system.actorOf(Props(new SubjectProviderManagerActor(processManager)))

    implicit val timeout = Timeout(500)
    //fuer CreateProcess wird die userId benoetigt
    val future1 = processManager ? CreateProcess(2, "my process", processGraph)

    val processID: Int =
      Await.result(future1, timeout.duration).asInstanceOf[ProcessCreated].processID

    val future2 = processManager ? CreateProcessInstance(processID)

    val processInstanceID: Int =
      Await.result(future2, timeout.duration).asInstanceOf[ProcessInstanceCreated].processInstanceID

    processManager ! ((processInstanceID, new AddSubject(0, "Subj1") with Debug))
    processManager ! ((processInstanceID, new AddSubject(0, "Subj2") with Debug))
    processManager ! ((processInstanceID, new AddSubject(0, "Subj3") with Debug))
    processManager ! ((processInstanceID, new AddSubject(0, "Subj4") with Debug))
  }
}

object Fkts {
  class StateCreator(val id: String, val stateType: StateType) {
    val transitions = new ArrayBuffer[Transition]

    def addTransition(transition: Transition) {
      transitions += transition
    }

    def createState: State = State(id, "", stateType, transitions.toArray)
  }

  def parseProcess(process: JSONProcess): ProcessGraph = {

    ProcessGraph(process.process.map(parseSubject(_)))
  }

  def parseSubject(subject: JSONSubject): Subject = {
    val states = scala.collection.mutable.Map[String, StateCreator]()
    // erstmal ein subject
    val internalBehavior = subject.macros(0)

    // creater the states  
    parseNodes(internalBehavior.nodes)
    parseEdges(internalBehavior.edges)

    def parseNodes(nodes: Array[Node]) {
      // hier werden die states erstellt
      for (node <- nodes) yield {
        if (node.start) {
          // create startnode and transition to this
          // TODO unique IDs
          val startID = "StartState"
          states(startID) = new StateCreator(startID, StartStateType)
          states(startID).addTransition(StartTransition(node.id.toString))
        }
        if (node.end) {
          // create transition from this to the endstate
        }

        if (states.contains(node.id.toString)) {
          // TODO darf nicht sein!
          println(node.id + " wird doppelt erstellt!")
          throw new Exception(node.id + " wird doppelt erstellt!")
        }

        node.typ match {
          case "send" =>
            // TODO kann man davorziehen
            // create sendstate
            states(node.id.toString) = new StateCreator(node.id.toString, SendStateType)
          case "action" =>
            states(node.id.toString) = new StateCreator(node.id.toString, ActStateType)
          case "receive" =>
            states(node.id.toString) = new StateCreator(node.id.toString, ReceiveStateType)
          case "end" =>
            states(node.id.toString) = new StateCreator(node.id.toString, EndStateType)
          case "start" =>
            states(node.id.toString) = new StateCreator(node.id.toString, StartStateType)

        }
      }
    }

    def parseEdges(edges: Array[Edge]) {
      // hier werden die transitions in die states eingefuegt
      for (edge <- edges) {
        // TODO messagetype
        var s: String = ""
        import Tab._
        if (!edge.target.isInstanceOf[JsString]) {
          s = edge.target.convertTo[Target].id
        }

        states(edge.start.toString).addTransition(Transition(edge.text, s, edge.end.toString))
      }
    }

    Subject(subject.id, states.map(_._2.createState).toArray)
  }
}

///////////////////////////////////////////////////////
case class JSONProcess(process: Array[JSONSubject])
case class JSONSubject(id: String, name: String, macros: Array[Macro])
case class Macro(id: String, name: String, nodes: Array[Node], edges: Array[Edge])
// type kann man nicht uebernehmen! (heisst hier typ)
case class Node(id: Int, start: Boolean, end: Boolean, typ: String)
case class Edge(start: Int, end: Int, text: String, target: JsValue)
case class Target(id: String)
///////////////////////////////////////////////////////
