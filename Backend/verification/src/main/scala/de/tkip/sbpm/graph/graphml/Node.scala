package de.tkip.sbpm.graph.graphml

private class Node(val id: NodeId, fields: NodeFields) {
  def text: String = fields.text
  def myType = fields.myType
  def color = fields.color

  def toXml =
    <node id={ id }>
      <data key="d6">
        <y:ShapeNode>
          <y:Geometry height="30.0" width="30.0" x="45.0" y="0.0"/>
          <y:Fill color={ color } transparent="false"/>
          <y:BorderStyle color="#000000" type="line" width="1.0"/>
          <y:NodeLabel alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.701171875" modelName="custom" textColor="#000000" visible="true" width="23.341796875" x="3.3291015625" y="5.6494140625">{
              text
            }<y:LabelModel>
               <y:SmartNodeLabelModel distance="4.0"/>
             </y:LabelModel>
            <y:ModelParameter>
              <y:SmartNodeLabelModelParameter labelRatioX="0.0" labelRatioY="0.0" nodeRatioX="0.0" nodeRatioY="0.0" offsetX="0.0" offsetY="0.0" upX="0.0" upY="-1.0"/>
            </y:ModelParameter>
          </y:NodeLabel>
          <y:Shape type={ myType }/>
        </y:ShapeNode>
      </data>
    </node>
}
