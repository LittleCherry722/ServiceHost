package de.tkip.sbpm.graph.graphml

private class Edge(edgeId: EdgeId, from: NodeId, target: NodeId, fields: EdgeFields) {
  def toXml =
    <edge id={ edgeId } source={ from } target={ target }>
      <data key="d9">
        <y:BezierEdge>
          <y:Path sx="0.0" sy="0.0" tx="0.0" ty="0.0"/>
          <y:LineStyle color="#000000" type="line" width="1.0"/>
          <y:Arrows source="none" target="standard"/>
          <y:EdgeLabel alignment="center" configuration="AutoFlippingLabel" distance="0.0" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.701171875" modelName="six_pos" modelPosition="tail" preferredPlacement="anywhere" ratio="0.5" textColor="#000000" visible="true" width="31.345703125" x="10.28812682136657" y="17.0">{
              fields.text
            }<y:PreferredPlacementDescriptor angle="0.0" angleOffsetOnRightSide="0" angleReference="absolute" angleRotationOnRightSide="co" distance="-1.0" frozen="true" placement="anywhere" side="anywhere" sideReference="relative_to_edge_flow"/>
          </y:EdgeLabel>
        </y:BezierEdge>
      </data>
    </edge>
}
