package de.tkip.sbpm.application.miscellaneous

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.{ Map => MutableMap }
import spray.json._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.rest.JsonProtocol._

object MarshallingAttributes {
  val exitCondLabel = "exitcondition"
  val timeoutLabel = "timeout"
}

/**
 * This objectfunction is responsible to divide a string listing of subjects
 * into the independent subjectIDs
 */
object parseSubjects {
  def apply(subjects: String): Array[SubjectID] = synchronized {
    try {
      subjects.asJson.convertTo[Array[String]]
    } catch {
      case _: Throwable => {
        System.err.println("cant parse start subjects")
        Array()
      }
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
  private case class JSubject(id: SubjectID, name: SubjectName, myType: String, inputPool: Int, macros: Array[JBehavior])
  private case class JBehavior(nodes: Array[JNode], edges: Array[JEdge])
  private case class JNode(id: StateID, text: String, start: Boolean, end: Boolean, myType: String, options: JNodeOption)
  private case class JNodeOption(message: MessageType)
  private case class JEdge(start: StateID, end: StateID, text: MessageType, myType: String, target: JsValue, priority: Int, manualTimeout: Boolean, variable: JsValue)
  private case class JEdgeTarget(id: SubjectID, min: JsValue, max: JsValue, createNew: Boolean, variable: JsValue)
  // The marshalling formats for the case classes
  private object JsonFormats extends DefaultJsonProtocol {
    implicit val edgeTargetFormat = jsonFormat5(JEdgeTarget)
    implicit val edgeFormat = jsonFormat8(JEdge)
    implicit val nodeOptionFormat = jsonFormat1(JNodeOption)
    implicit val nodeFormat = jsonFormat6(JNode)
    implicit val behaviorFormat = jsonFormat2(JBehavior)
    implicit val subjectFormat = jsonFormat5(JSubject)
    implicit val graphFormat = jsonFormat2(JGraph)
  }
  import JsonFormats._

  // This map matches the short versions and real versions of the message types
  private var messageMap: Map[String, String] = null

  def apply(graph: String): ProcessGraph = synchronized {
    // TODO fehlerbehandlung bei falschem String

    // TODO type replacement is not efficient
    // parse the graph message count and the message object from the graph
    val jgraph = graph.replace("\"type\":", "\"myType\":").asJson.convertTo[JGraph]
    val jmessages = jgraph.messages.asJsObject()

    // parse the message map from the json graph
    messageMap = for ((k, v) <- jmessages.fields) yield (k, v.convertTo[String])

    // parse the subjects and return the resulting processgraph
    ProcessGraph(parseSubjects(jgraph.process))
  }

  private object parseSubjects {
    // stores the information, which is extracted in the preparse
    private case class PreSubjectInfo(multi: Boolean, external: Boolean)
    // this map will be filled during the preparse
    private val subjectMap = MutableMap[SubjectID, PreSubjectInfo]()

    def apply(subjects: Array[JSubject]): Array[Subject] = {
      // first preparse the subjects, to extract information
      // e.g. which subject is a multisubject
      subjects.map(preParseSubject(_))
      // parse the subjects to the internal model
      subjects.map(parseSubject(_))
    }

    def preParseSubject(subject: JSubject) = {
      val id = subject.id
      // extract the subject types
      val multi = subject.myType.matches("\\Amulti")
      val external = subject.myType.matches("(multi)?external")
      subjectMap(id) = PreSubjectInfo(multi, external)
    }

    // the Statesmap
    private var states: MutableMap[StateID, StateCreator] = null

    def parseSubject(subject: JSubject): Subject = {
      // reset the statesmap
      states = MutableMap[StateID, StateCreator]()

      // at the moment we only support one behavior
      val behavior: JBehavior = subject.macros(0)

      // extract the subject types
      val id = subject.id
      val multi = subjectMap(id).multi
      val external = subjectMap(id).external

      // first parse the nodes then the edges
      parseNodes(behavior.nodes)
      parseEdges(behavior.edges)

      // all parsed states are in the states map, convert the creators,
      // create and return the subject
      Subject(subject.id, subject.inputPool, states.map(_._2.createState).toArray, multi, external)
    }

    private def parseNodes(nodes: Array[JNode]) {
      for (node <- nodes) {
        // create and add a state creator for this state
        states(node.id) =
          new StateCreator(node.id, node.text, fromStringtoStateType(node.myType), node.start)
      }
    }

    private def parseEdges(edges: Array[JEdge]) {
      import Integer.parseInt
      for (edge <- edges) {
        // match the edgetype and create the corresponding transition
        edge.myType match {

          case "exitcondition" => {
            // parse the target
            val target =
              if (!edge.target.isInstanceOf[JsString]) {
                val jtarget = edge.target.convertTo[JEdgeTarget]
                  // TODO Currently the graph contains e.g. -1 and "-1" for
                  // min and max values, this function parses both to an Int
                  // delete if its not needed anymore
                  def parseNumber(value: JsValue): Int = value match {
                    case s: JsString => parseInt(s.convertTo[String])
                    case i: JsNumber => i.convertTo[Int]
                    case _           => -1
                  }

                val targetVariable: Option[String] = jtarget.variable match {
                  case s: JsString => if (s != "") Some(s.convertTo[String]) else None
                  case _           => None
                }

                var minValue = parseNumber(jtarget.min)
                var maxValue = parseNumber(jtarget.max)
                var default = minValue < 1 && maxValue < 1

                if (minValue < 1) minValue = 1
                if (maxValue < 1) {
                  // maxValue should be infinity, if the other one is a multisubject
                  // if the other one is a single subject await only one message
                  maxValue =
                    if (subjectMap(jtarget.id).multi)
                      Int.MaxValue
                    else
                      1
                }

                Some(Target(jtarget.id, minValue, maxValue, jtarget.createNew, targetVariable, default))
              } else {
                None
              }

            val storeVariable: String = edge.variable match {
              case s: JsString => s.convertTo[String]
              case _           => ""
            }

            val state = states(edge.start)
            val text = edge.text
            // the messageType is the edge text
            // for receive and send states the edgetext is the short form
            // so replace it with the real form, if possible
            val messageType = state.stateType match {
              case ReceiveStateType => messageMap.getOrElse(text, text)
              case SendStateType    => messageMap.getOrElse(text, text)
              case _                => text
            }

            // at the transition to the state
            state.addTransition(
              Transition(ExitCond(messageType, target), edge.end, edge.priority, storeVariable))
          }

          case "timeout" => {
            // get the duration only if its not manual
            val duration = if (edge.manualTimeout) -1 else parseInt(edge.text)
            // at the transition to the state
            states(edge.start).addTransition(
              TimeoutTransition(edge.manualTimeout, duration, edge.end))
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
    val id: StateID,
    val text: String,
    val stateType: StateType,
    val startState: Boolean) {

    // store all transitions in this Buffer
    private val transitions = new ArrayBuffer[Transition]

    /**
     * Add a transition to this state creator
     */
    def addTransition(transition: Transition) {
      transitions += transition
    }

    /**
     * Creates and returns the state for this state creator
     */
    def createState: State =
      State(id, text, stateType, startState, transitions.toArray)
  }
}