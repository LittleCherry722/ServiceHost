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

  // This map matches the short versions and real versions of the message types
  private var messageMap: Map[String, String] = null

  def apply(graph: Graph): ProcessGraph = synchronized {

    // parse the message map from the json graph
    messageMap = graph.messages.mapValues(_.name)

    // parse the subjects and return the resulting processgraph
    ProcessGraph(parseSubjects(graph.subjects))
  }

  private object parseSubjects {
    // stores the information, which is extracted in the preparse
    private case class PreSubjectInfo(multi: Boolean, external: Boolean)
    // this map will be filled during the preparse
    private val subjectMap = MutableMap[SubjectID, PreSubjectInfo]()

    def apply(subjects: Map[String, GraphSubject]): Map[String, Subject] = {
      // first preparse the subjects, to extract information
      // e.g. which subject is a multisubject
      subjects.values.foreach(preParseSubject(_))
      // parse the subjects to the internal model
      subjects.mapValues(parseSubject(_))
    }

    def preParseSubject(subject: GraphSubject) = {
      val id = subject.id
      // extract the subject types
      val multi = subject.subjectType.matches("\\Amulti")
      val external = subject.subjectType.matches("(multi)?external")
      subjectMap(id) = PreSubjectInfo(multi, external)
    }

    // the Statesmap
    private var states: MutableMap[StateID, StateCreator] = null

    def parseSubject(subject: GraphSubject): Subject = {
      // reset the statesmap
      states = MutableMap[StateID, StateCreator]()

      // at the moment we only support one behavior
      val behavior: GraphMacro = subject.macros("##main##")

      // extract the subject types
      val id = subject.id
      val multi = subjectMap(id).multi
      val external = subjectMap(id).external

      // first parse the nodes then the edges
      parseNodes(behavior.nodes.values)
      parseEdges(behavior.edges)

      // all parsed states are in the states map, convert the creators,
      // create and return the subject
      Subject(subject.id, subject.inputPool, states.map(_._2.createState).toArray, multi, external)
    }

    private def parseNodes(nodes: Iterable[GraphNode]) {
      for (node <- nodes) {
        // create and add a state creator for this state
        states(node.id) =
          new StateCreator(node.id, node.text, fromStringtoStateType(node.nodeType), node.isStart)
      }
    }

    private def parseEdges(edges: Iterable[GraphEdge]) {
      import Integer.parseInt
      for (edge <- edges) {
        // match the edgetype and create the corresponding transition
        edge.edgeType match {

          case "exitcondition" => {
            // parse the target
            val target = edge.target match {
              case Some(t) => {
                var minValue = t.min
                var maxValue = t.max
                var default = minValue < 1 && maxValue < 1

                if (minValue < 1) minValue = 1
                if (maxValue < 1) {
                  // maxValue should be infinity, if the other one is a multisubject
                  // if the other one is a single subject await only one message
                  maxValue =
                    if (subjectMap(t.subjectId).multi)
                      Short.MaxValue
                    else
                      1
                }

                Some(Target(t.subjectId, minValue, maxValue, t.createNew, t.variableId, default))
              }
              case None => None
            }

            val state = states(edge.startNodeId)
            // the messageType is the edge text
            // for receive and send states the edgetext is the short form
            // so replace it with the real form, if possible
            val messageType = state.stateType match {
              case ReceiveStateType => messageMap.getOrElse(edge.text, edge.text)
              case SendStateType    => messageMap.getOrElse(edge.text, edge.text)
              case _                => edge.text
            }

            // at the transition to the state
            state.addTransition(
              Transition(ExitCond(messageType, target), edge.endNodeId, edge.priority, edge.variableId))
          }

          case "timeout" => {
            // get the duration only if its not manual
            val duration = if (edge.manualTimeout) -1 else parseInt(edge.text)
            // at the transition to the state
            states(edge.startNodeId).addTransition(
              TimeoutTransition(edge.manualTimeout, duration, edge.endNodeId))
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