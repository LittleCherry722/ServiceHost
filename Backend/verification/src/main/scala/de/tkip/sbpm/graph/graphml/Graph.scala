package de.tkip.sbpm.graph.graphml

import scala.collection.mutable

private class Graph {
  private val nodes: mutable.Set[Node] = mutable.Set()
  private val edges: mutable.Set[Edge] = mutable.Set()
  private var nodeId = 0
  private var edgeId = 0

  private def nextNodeId: NodeId = {
    val res = "n" + nodeId
    nodeId += 1
    res
  }

  private def nextEdgeId: EdgeId = {
    val res = "e" + edgeId
    edgeId += 1
    res
  }
  def addNode(fields: NodeFields): NodeId = {
    val id = nextNodeId
    nodes += new Node(id, fields)
    id
  }
  def addEdge(from: NodeId, target: NodeId, fields: EdgeFields) {
    edges += new Edge(nextEdgeId, from, target, fields)
  }

  def toXml =
    <graphml xmlns="http://graphml.graphdrawing.org/xmlns" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:y="http://www.yworks.com/xml/graphml" xmlns:yed="http://www.yworks.com/xml/yed/3" xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd">
      <key for="graphml" id="d0" yfiles.type="resources"/>
      <key for="port" id="d1" yfiles.type="portgraphics"/>
      <key for="port" id="d2" yfiles.type="portgeometry"/>
      <key for="port" id="d3" yfiles.type="portuserdata"/>
      <key attr.name="url" attr.type="string" for="node" id="d4"/>
      <key attr.name="description" attr.type="string" for="node" id="d5"/>
      <key for="node" id="d6" yfiles.type="nodegraphics"/>
      <key attr.name="url" attr.type="string" for="edge" id="d7"/>
      <key attr.name="description" attr.type="string" for="edge" id="d8"/>
      <key for="edge" id="d9" yfiles.type="edgegraphics"/>
      <graph id="G" edgedefault="directed">
        { nodes.map(_.toXml) }
        { edges.map(_.toXml) }
      </graph>
    </graphml>
}
