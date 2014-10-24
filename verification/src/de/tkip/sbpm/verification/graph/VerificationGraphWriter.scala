package de.tkip.sbpm.verification.graph

import de.tkip.sbpm.graph.graphml.GraphWriter
import de.tkip.sbpm.graph.graphml.NodeFields
import de.tkip.sbpm.verification.lts.LtsState
import de.tkip.sbpm.graph.graphml.EdgeFields
import de.tkip.sbpm.verification.lts.LtsTransition
import de.tkip.sbpm.verification.lts.Tau
import de.tkip.sbpm.verification.lts.SendLabel
import de.tkip.sbpm.verification.lts.Lts
import de.tkip.sbpm.verification.lts.ReceiveLabel

object VerificationGraphWriter {
  def writeLts(lts: Lts,
               path: String = System.getProperty("user.home") + "/graphml/",
               filename: String = "graph") {
    val writer = new VerificationGraphWriter(lts) {
      override def getPath = path
      override def getFilename = filename + ".graphml"
    }
    writer.writeGraph(lts.states, lts.transitions)
  }
}

class VerificationGraphWriter(lts: Lts) extends GraphWriter[LtsState] {

  def getPath = System.getProperty("user.home") + "/graphml/"

  protected def getNodeInfo(node: LtsState) =
    node match {
      // if its the successfully end state
      case LtsState(m) if (m.isEmpty) => new NodeFields {
        override def myType = "ellipse"
        override def color = "#00FF00"
      }
      // if its the startstate
      case s if (s == lts.startState) => new NodeFields {
        override def text = node.mkString
        override def myType = "roundrectangle"
        override def color = "#00CCCC"
      }
      case _ =>
        if (lts.fromStatesMap(node).nonEmpty)
          // if its a failed end state in the run
          if (lts.invalidNodes contains node) new NodeFields() {
            override def text = node.mkString
            override def myType = "roundrectangle"
            override def color = "#FF9900"
          }
          else
            // successful state (has a way to the successful end state)
            new NodeFields() {
              override def text = node.mkString
              override def color = "#FFFF99"
            }
        // failed end state end of run
        // normal state 
        else new NodeFields() {
          override def text = node.mkString
          override def myType = "diamond"
          override def color = "#FF6600"
        }
    }

  protected def getEdgeInfo(edge: GraphEdge[LtsState]) =
    edge match {

      case LtsTransition(_, label, _) => label match {
        case Tau => new EdgeFields() {
          override def text = "\u03C4"
        }
        case label =>new EdgeFields() {
          override def text = label.toString()
        }
      }

      case _ => new EdgeFields()
    }
}