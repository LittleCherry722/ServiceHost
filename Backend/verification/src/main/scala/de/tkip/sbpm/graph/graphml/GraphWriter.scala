package de.tkip.sbpm.graph.graphml

import scala.collection.mutable
import scala.xml.XML

abstract class GraphWriter[A] {
  type GraphEdge[A] = {
    val from: A
    val to: A
  }

  /**
   * Extracts the graphml information for a node
   *
   * node -> (label, [optional] type)
   */
  //  protected def getNodeInfo(node: A): (String, Option[String])
  protected def getNodeInfo(node: A): NodeFields
  protected def getEdgeInfo(edge: GraphEdge[A]): EdgeFields
  protected def getPath: String
  protected def getFilename = "graph.graphml"

  def writeGraph(nodes: Set[A], edges: Set[_ <: GraphEdge[A]]) {
    val path = getPath
    val filename = getFilename

    val graph = new Graph

    val getNodeId = mutable.Map[A, NodeId]()
    for (n <- nodes) {
      val fields = getNodeInfo(n)
      val nodeId = graph.addNode(fields)
      getNodeId += n -> nodeId
    }

    for (edge <- edges) {
      val (from, to) = (edge.from, edge.to)
      val data = getEdgeInfo(edge)
      graph.addEdge(getNodeId(from), getNodeId(to), data)
    }

    XML.save(path + filename, graph.toXml, "UTF-8", true, null)
  }
}