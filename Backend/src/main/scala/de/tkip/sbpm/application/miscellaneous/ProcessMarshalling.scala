/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.application.miscellaneous

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.{ Map => MutableMap }
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.rest.JsonProtocol._
import de.tkip.sbpm.application.subject.behavior._

object MarshallingAttributes {
  val exitCondLabel = "exitcondition"
  val timeoutLabel = "timeout"
}

/**
 * This objectfunction is responsible to create a ProcessGraph
 * out of the JSON representation
 */
object parseGraph {

  // This map matches the short versions and real versions of the message types
  private var messageMap: Map[String, String] = null

  def apply(graph: Graph): ProcessGraph = synchronized {

    // create the message map from the graph
    messageMap = graph.messages.mapValues(_.id)

    // parse the subjects and return the resulting processgraph
    ProcessGraph(parseSubjects(graph.subjects))
  }

  private object parseSubjects {
    // stores the information, which is extracted in the preparse
    private case class PreSubjectInfo(multi: Boolean, external: Boolean)
    // this map will be filled during the preparse
    private val subjectMap = MutableMap[SubjectID, PreSubjectInfo]()

    def apply(subjects: Map[String, GraphSubject]): Map[String, SubjectLike] = {
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

    def parseSubject(subject: GraphSubject): SubjectLike = {
      // reset the statesmap
      states = MutableMap[StateID, StateCreator]()

      // at the moment we only support internal behavior
      val behavior: GraphMacro = subject.macros("##main##")

      // extract the subject types
      val id = subject.id
      val multi = subjectMap(id).multi
      val external = subjectMap(id).external
      
      var tempMap:Map[String,String]=Map()
      subject.variables.foreach {case(key, GraphVariable(k,v)) => tempMap = tempMap + (key -> v)}
      val varMap=tempMap;
      val macros = subject.macros.map(m => parseMacro(m._1, m._2))

      // first parse the nodes then the edges
      //      parseNodes(behavior.nodes.values)
      //      parseEdges(behavior.edges)

      // all parsed states are in the states map, convert the creators,
      // create and return the subject
      if (!external)
        Subject(subject.id, subject.inputPool, macros, multi,varMap)
      else {
        // FIXME GraphId != processId
        // TODO check ob vorhanden!

        ExternalSubject(id, subject.inputPool, multi, subject.relatedGraphId, subject.relatedSubjectId, subject.relatedInterfaceId, subject.url,varMap)
      }
    }

    private def parseMacro(macroId: String, macro: GraphMacro): (String, ProcessMacro) = synchronized {
      states = MutableMap[StateID, StateCreator]()

      val mainMacro = macroId == "##main##"

      // first parse the nodes then the edges
      parseNodes(mainMacro, macro.nodes.values)
      parseEdges(macro.edges)

      macroId -> ProcessMacro(macro.name, states.map(_._2.createState).toArray)
    }

    private def parseNodes(mainMacro: Boolean, nodes: Iterable[GraphNode]) {
      for (node <- nodes) {
        val options = parseNodeOptions(node.options)

        // create and add a state creator for this state
        states(node.id) =
          new StateCreator(mainMacro, node.id, node.text, fromStringtoStateType(node.nodeType), node.isAutoExecute.getOrElse(false), node.isMajorStartNode, node.isStart, node.macroId, options)
      }
    }

    private def parseNodeOptions(nodeOptions: GraphNodeOptions): StateOptions = {
      val messageId = nodeOptions.messageId.map(id => if (id == GraphNodeOptions.AllMessages) AllMessages else id)
      val subjectId = nodeOptions.subjectId.map(id => if (id == GraphNodeOptions.AllSubjects) AllSubjects else id)
      val stateId = nodeOptions.nodeId.map(_.toInt)

      StateOptions(messageId, subjectId, nodeOptions.correlationId, nodeOptions.conversationId, stateId)
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
                val default = minValue < 1 && maxValue < 1
                val toExternal = subjectMap(t.subjectId).external

                if(toExternal) {
                  minValue = 0
                } else if (minValue < 1) {
                  minValue = 1
                }

                if (maxValue < 1) {
                  // maxValue should be infinity, if the other one is a multisubject
                  // if the other one is a single subject await only one message
                  maxValue =
                    if (subjectMap(t.subjectId).multi)
                      Short.MaxValue
                    else
                      1
                }

                Some(Target(t.subjectId, minValue, maxValue, t.createNew, t.variableId, toExternal, default))
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
    val mainMacro: Boolean,
    val id: StateID,
    val text: String,
    val stateType: StateType,
    val autoExecute: Boolean,
    val majorStartState: Boolean,
    val startState: Boolean,
    val macro: Option[String],
    val options: StateOptions) {

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
      State(id, text, stateType, autoExecute, majorStartState, !majorStartState && mainMacro && startState && stateType == ReceiveStateType, macro, options, transitions.toArray)
  }
}
