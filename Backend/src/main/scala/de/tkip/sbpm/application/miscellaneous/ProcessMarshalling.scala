package de.tkip.sbpm.application.miscellaneous

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.{ Map => MutableMap }
import spray.json._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.rest.JsonProtocol._

/**
 * This objectfunction is responsible to divide a string listing of subjects
 * into the independent subjectIDs
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
 * This objectfunction is responsible to create a ProcessGraph
 * out of the JSON representation
 */
object parseGraph {
  // The marshalling case classes
  private case class JGraph(process: Array[JSubject], messages: JsValue)
  private case class JSubject(id: SubjectID, name: SubjectName, inputPool: Int, macros: Array[JBehavior])
  private case class JBehavior(nodes: Array[JNode], edges: Array[JEdge])
  private case class JNode(id: StateID, text: String, start: Boolean, end: Boolean, myType: String, options: JNodeOption)
  private case class JNodeOption(message: MessageType)
  private case class JEdge(start: StateID, end: StateID, text: MessageType, myType: String, manualTimeout: Boolean, target: JsValue)
  private case class JEdgeTarget(id: SubjectID)

  // The marshalling formats for the case classes
  private object JsonFormats extends DefaultJsonProtocol {
    implicit val edgeTargetFormat = jsonFormat1(JEdgeTarget)
    implicit val edgeFormat = jsonFormat6(JEdge)
    implicit val nodeOptionFormat = jsonFormat1(JNodeOption)
    implicit val nodeFormat = jsonFormat6(JNode)
    implicit val behaviorFormat = jsonFormat2(JBehavior)
    implicit val subjectFormat = jsonFormat4(JSubject)
    implicit val graphFormat = jsonFormat2(JGraph)
  }
  import JsonFormats._

  // This map matches the short versions and real versions of the message types
  private var messageMap: Map[String, String] = null

  def apply(graph: String): ProcessGraph = {
    // TODO fehlerbehandlung bei falschem String

    // TODO type replacement is not efficient
    // parse the graph message count and the message object from the graph
    val jgraph = graph.replace("\"type\":", "\"myType\":").asJson.convertTo[JGraph]
    val jmessages = jgraph.messages.asJsObject()

    // parse the message map from the json graph
    messageMap = for ((k, v) <- jmessages.fields) yield (k, v.convertTo[String])

    // create the processGraph by parsing all subjects
    ProcessGraph(jgraph.process.map(parseSubject(_)).toArray)
  }

  private object parseSubject {
    private var states: MutableMap[StateID, StateCreator] = null

    def apply(subject: JSubject): Subject = {
      // reset the statesmap
      states = MutableMap[StateID, StateCreator]()

      // at the moment we only support one behavior
      val behavior: JBehavior = subject.macros(0)

      // first parse the nodes then the edges
      parseNodes(behavior.nodes)
      parseEdges(behavior.edges)

      // all parsed states are in the states map, convert the creators,
      // create and return the subject
      Subject(subject.id, subject.inputPool, states.map(_._2.createState).toArray)
    }

    private def parseNodes(nodes: Array[JNode]) {
      for (node <- nodes) {
        // create and add a state creator for this state
        states(node.id) =
          new StateCreator(node.id, node.text, fromStringtoStateType(node.myType), node.start)
      }
    }

    private def parseEdges(edges: Array[JEdge]) {
      for (edge <- edges) {
        // match the edgetype and create the corresponding transition
        edge.myType match {

          case "exitcondition" => {
            //  get the id of the target subject
            val s =
              if (!edge.target.isInstanceOf[JsString])
                edge.target.convertTo[JEdgeTarget].id
              else
                "None"
            // at the transition to the state
            states(edge.start).addTransition(Transition(ExitCond(edge.text, s), edge.end))
          }

          case "timeout" => {
            // get the duration only if i
            val duration = if (edge.manualTimeout) -1 else Integer.parseInt(edge.text)
            // at the transition to the state
            states(edge.start).addTransition(TimeoutTransition(edge.manualTimeout, duration, edge.end))
          }

          case s => {
            // TODO error loggen
            System.err.println("Cant parse edgetype: " + s)
          }
        }
      }
    }
  }

  /**
   * This class holds a state while it is in creation, so it is possible
   * to add transitions
   */
  private class StateCreator(
    id: StateID,
    name: SubjectName,
    stateType: StateType,
    startState: Boolean) {

    // store all transitions in this Buffer
    private val transitions = new ArrayBuffer[Transition]

    /**
     * Add a transition to this state creator
     */
    def addTransition(transition: Transition) {
      transitions += updateMessageType(transition)
    }

    /**
     * Creates and returns the state for this state creator
     */
    def createState: State =
      State(id, name, stateType, startState, transitions.toArray)

    /**
     * Updates the MessageType of a transition, but only if this
     * StateCreator is for a Send- or ReceiveState
     */
    private def updateMessageType(transition: Transition): Transition = {
        def transitionWithNewMessageType: Transition = {
          // only update exitconds, only update existing message types
          if (transition.isExitCond && messageMap.contains(transition.messageType)) {
            Transition(
              ExitCond(messageMap(transition.messageType), transition.subjectID),
              transition.successorID)
          } else {
            transition
          }
        }

      // only update message type for receive- and send states
      stateType match {
        case ReceiveStateType => transitionWithNewMessageType
        case SendStateType    => transitionWithNewMessageType
        case _                => transition
      }
    }
  }
}
