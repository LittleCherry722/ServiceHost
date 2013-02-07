package de.tkip.sbpm.application.miscellaneous

import scala.collection.mutable.ArrayBuffer
import spray.json._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.rest.JsonProtocol._

/**
 * This object is responsible to divide a string listing of subjects
 * into the independet subjectIDs
 */
object parseSubjects {
  def apply(subjects: String): Array[SubjectID] = {
    try {
      subjects.asJson.convertTo[Array[String]]
    } catch {
      case _: Throwable => {
        System.err.println("cant parse start subjects")
        Array()
      }
      //    Array("Employee")
    }
  }
}

/**
 * This object is responsible to create a ProcessGraph
 * out of the JSON representation
 */
object parseGraph {
  // The marshalling case classes
  // TODO wo genau steht die messagetype
  private case class JGraph(process: Array[JSubject])
  private case class JSubject(id: SubjectID, name: SubjectName, macros: Array[JBehavior])
  private case class JBehavior(nodes: Array[JNode], edges: Array[JEdge])
  private case class JNode(id: StateID, text: String, start: Boolean, end: Boolean, myType: String, options: JNodeOption)
  private case class JNodeOption(message: MessageType)
  private case class JEdge(start: StateID, end: StateID, target: JsValue, text: MessageType)
  private case class JEdgeTarget(id: SubjectID)

  // The marshalling formats for the case classes
  private object JsonFormats extends DefaultJsonProtocol {
    implicit val edgeTargetFormat = jsonFormat1(JEdgeTarget)
    implicit val edgeFormat = jsonFormat4(JEdge)
    implicit val nodeOptionFormat = jsonFormat1(JNodeOption)
    implicit val nodeFormat = jsonFormat6(JNode)
    implicit val behaviorFormat = jsonFormat2(JBehavior)
    implicit val subjectFormat = jsonFormat3(JSubject)
    implicit val graphFormat = jsonFormat1(JGraph)
  }
  import JsonFormats._

  /**
   * This class holds a state while it is in creation, so it is possible
   * to add transitions
   */
  private class StateCreator(val id: StateID,
                             val name: SubjectName,
                             val stateType: StateType) {
    val transitions = new ArrayBuffer[Transition]

    def addTransition(transition: Transition) {
      transitions += transition
    }

    def createState: State = State(id, name, stateType, transitions.toArray)
  }

  def apply(graph: String): ProcessGraph = {
    // TODO fehlerbehandlung bei falschem String
    // TODO type ersetzung ist so nicht effizient
    ProcessGraph(
      graph.replace("\"type\":", "\"myType\":").replace("Human Resource", "HumanResource").asJson.convertTo[JGraph]
        .process.map(parseSubject(_)).toArray)
  }

  private object parseSubject {
    import scala.collection.mutable.{ Map => MutableMap }
    // TODO irgentwie elegant loesen
    private var states = MutableMap[StateID, StateCreator]()
    // TODO unique id's
    private var startID: StateID = -1

    def apply(subject: JSubject): Subject = {
      states = MutableMap[StateID, StateCreator]()
      // First create an unique start and end state
      states(startID) = new StateCreator(startID, "StartState", StartStateType)

      // at the moment we only support one behavior
      val behavior: JBehavior = subject.macros(0)

      // first parse the nodes then the edges
      parseNodes(behavior.nodes)
      parseEdges(behavior.edges)

      // all parsed states are in the states map, convert the creators and return
      // the subject
      Subject(subject.id, states.map(_._2.createState).toArray)
    }

    private def parseNodes(nodes: Array[JNode]) {
      for (node <- nodes) {
        // if its the startstate add a transition from the startstate to this state
        if (node.start) {
          states(startID).addTransition(StartTransition(node.id))
        }
        if (states.contains(node.id)) {
          throw new Exception("Parse failed state id: " + node.id + " is given 2 times")
        }
        // add the state creator for this state
        states(node.id) =
          new StateCreator(node.id, node.text, fromStringtoStateType(node.myType))
        // TODO check if end state always is automatic parsed
      }

    }

    private def parseEdges(edges: Array[JEdge]) {
      for (edge <- edges) {
        val s =
          if (!edge.target.isInstanceOf[JsString])
            edge.target.convertTo[JEdgeTarget].id
          else
            "Me"

        // TODO werden die transitions richtig gebuildet?
        states(edge.start).addTransition(Transition(edge.text, s, edge.end))
      }
    }
  }
}
