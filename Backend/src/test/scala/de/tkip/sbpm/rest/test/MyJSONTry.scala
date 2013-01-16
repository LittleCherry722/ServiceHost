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

case class Abc(h: String, in: Int)
case class Graph1(id: Option[Int], graph: String, abc: Abc, processId: Array[Int])

object Tab extends DefaultJsonProtocol {

  implicit val abcFormat = jsonFormat2(Abc)
  implicit val graphFormat = jsonFormat4(Graph1)

  implicit val tarFormat = jsonFormat1(Target)
  implicit val nodeFormat = jsonFormat4(Node)
  implicit val edgeFormat = jsonFormat3(Edge)
  implicit val macFormat = jsonFormat4(Macro)
  implicit val subFormat = jsonFormat3(JSONSubject)
  implicit val proFormat = jsonFormat1(JSONProcess)

}
// TODO im graph heißt es type nicht typ
object MyJSONTry extends App {
  val graph =
    """{
    "process": [
        {
            "id":               "S1",               
            "name":             "Subject1",         
            "type":             "single",           
                                                    
            "deactivated":      false,              
            "inputPool":        -1,                 
            "relatedProcess":   null,               
            "relatedSubject":   null,               
            "externalType":     "external",         
                                                    
            "role":             "S1",               
            "comment":          "Any comment",      
            "macros":   [
                {
                    "id":               "##main##",         
                    "name":             "internal behavior",
                    "nodeCounter":      3,                  
                                                            
                    "nodes":    [
                        {
                            "id":                  "n0",    
                            "text":                "S",     
                            "start":               true,    
                                                            
                            "end":                 false,   
                                                            
                            "typ":                "send",  
                                                            
                            "deactivated":         false,   
                            "options":                      
                                {
                                    "message":       "*",   
                                    "subject":       "*",   
                                                            
                                    "correlationId": "*",   
                                                            
                                    "channel":        "*",   
                                    "state":          ""     
                                },
                            "majorStartNode":      true,    
                            "channel":             "c0",    
                            "variable":            "",      
                            "macro":               "",      
                            "comment":      "Any comment",  
                            "varMan":                       
                                {
                                    "var1":        "",
                                    "var2":        "",   
                                    "operation":   "and",   
                                    "storevar":    ""       
                                }
                        }
                    ],
                    "edges":    [
                        {
                            "start":            "n0",                   
                            "end":              "n1",                   
                            "text":             "m0",                   
                                                                        
                                                                        
                            "type":             "exitcondition",        
                            "target":                         
                                                              
                                                              
                                {
                                    "id":           "S2",     
                                                              
                                    "min":          "-1",     
                                                              
                                                              
                                    "max":          "-1",     
                                                              
                                                              
                                    "createNew":    false,    
                                                              
                                                              
                                    "variable":     ""        
                                                              
                                },
                            "deactivated":      false,      
                                                                     
                            "optional":         false,               
                                                                     
                                                                     
                            "priority":         1,                   
                                                                     
                                                                     
                            "manualTimeout":    false,               
                                                                     
                            "variable":         "",                  
                                                                     
                            "correlationId":    "",                  
                                                                     
                            "comment":          "Any comment",       
                            "transportMethod":  "googleMail"         
                        }
                    ]
                }
            ]
        }
    ],
    "messages":                                 
        {
            "m0":        "some message"              
        },
    "messageCounter":    1,                          
    "nodeCounter":       2,                           
    "channels":                                       
        {
            "c0":        "Channel1"                
        },
    "channelCounter":    1                            
}"""

  val source = """ { "id":4, "hallo": "hallo", 
    "graph":"Das ist ein Graph", "abc":{"h":"tet","in":32},"processI":32, "processId":[32, 21]} """

  import Tab._

  val g = Graph1(Some(1), "{abcds}", Abc("abc start", 54), Array(2))

  val j = g.toJson
  println(j)

  val s = j.convertTo[Graph1]
  println(s)

  println("-" * 32)
  println(source)
  val c = source.asJson.convertTo[Graph1]
  println(c)
  println(c.processId.mkString(", "))
  //  
  //  

  val abc = graph.asJson
  println(abc)

  println(abc.convertTo[JSONProcess])

  val process = abc.convertTo[JSONProcess]

  println(process.process(0).macros(0).nodes(0).typ)

  // Ausführung wirft einen fehler weil der graph oben nicht vollständig ist
  ProcessExe.executeProcess(Fkts.parseProcess(process))
}

object ProcessExe {
  def executeProcess(processModel: ProcessModel) {

    val system = ActorSystem("TextualEpassIos")
    val processManager = system.actorOf(Props(new ProcessManagerActor("BT_Application")), name = "BT_Application")
    val subjectProviderManager = system.actorOf(Props(new SubjectProviderManagerActor(processManager)))

    implicit val timeout = Timeout(5)

    val future1 = processManager ? CreateProcess("my process", processModel)

    val processID: Int =
      Await.result(future1, timeout.duration).asInstanceOf[ProcessCreated].processID

    val future2 = processManager ? CreateProcessInstance(processID)

    val processInstanceID: Int =
      Await.result(future2, timeout.duration).asInstanceOf[ProcessInstanceCreated].processInstanceID

    processManager ! ((processInstanceID, AddSubject(1, 2, "S1")))
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

  def parseProcess(process: JSONProcess): ProcessModel = {

    // TODO id muss richtig gesetz werden oder aus model raus
    // TODO name?
    ProcessModel(
      -1,
      "name?",
      process.process.map(parseSubject(_)))
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
          states(startID).addTransition(StartTransition(node.id))
        }
        if (node.end) {
          // create transition from this to the endstate
        }

        if (states.contains(node.id)) {
          // TODO darf nicht sein!
          println(node.id + " wird doppelt erstellt!")
          throw new Exception(node.id + " wird doppelt erstellt!")
        }

        node.typ match {
          case "send" =>
            // create sendstate
            states(node.id) = new StateCreator(node.id, SendStateType)
        }
      }
    }

    def parseEdges(edges: Array[Edge]) {
      // hier werden die transitions in die states eingefügt
      for (edge <- edges) {
        // TODO messagetype
        states(edge.start).addTransition(Transition("", edge.target.id, edge.end))
      }
    }

    // TODO id oder name?
    Subject(subject.id, states.map(_._2.createState).toArray)
  }
}

///////////////////////////////////////////////////////
case class JSONProcess(process: Array[JSONSubject])

case class JSONSubject(id: String, name: String, macros: Array[Macro])

// nöetig? = internal behavior?
case class Macro(id: String, name: String, nodes: Array[Node], edges: Array[Edge])

// type kann man nicht übernehmen! (heißt hier typ)
case class Node(id: String, start: Boolean, end: Boolean, typ: String)
case class Edge(start: String, end: String, target: Target)

case class Target(id: String)
///////////////////////////////////////////////////////
// TOO messages und channels
case class Message()
