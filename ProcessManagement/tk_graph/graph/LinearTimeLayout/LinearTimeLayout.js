/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Matthias Schrammek, Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * LinearTimeLayout
 */

/**
 * LinearTimeLayout Constructor
 */
function LinearTimeLayout (direction)
{
	if (!gf_isset(direction) || direction != "ltr")
		direction	= "ttb";
	
	// TODO: configuration (like renderer, spaces.aesthetics)
	// spaces (calc with max-node-height / 2 + arrow-space, max-node-width / 2 + arrow-space)
	// increase arrow-space when max-edge-label is too wide / too high
	this.backedge		= null;
	this.branchHeight	= {};
	this.branchWidth	= {};
	this.direction		= direction;
	this.drawnNodes		= {};
	this.edgeMap		= {};
	this.edges			= {};
	this.edgeTypes		= {"tree": "Treeedge", "back": "Backedge", "forward": "Forwardedge", "cross": "Crossedge"};
	this.edgex			= {};
	this.extraEdges		= {};
	this.inEdges		= {};
	this.loopEdges		= new Array();
	this.mostLeft		= null;
	this.ne2oe			= {};
	this.nn2on			= {};
	this.nodes			= {};
	this.nodex			= {};
	this.nodey			= {};
	this.normGraph		= null;
	this.on2nn			= {};
	this.outEdges		= {};
	this.renderObjects	= {"nodes": {}, "edges": {}};
	this.rpst			= null;
	this.spaces			= {x: 0, y: 0, aesthetics: 0};
}

/*
 * LinearTimeLayout Methods
 */
LinearTimeLayout.prototype.addEdge = function (id, source, target, edgeData)
{
	this.edges[id]	= new this.Edge(id, source, target, edgeData);
	
	// store out edge
	if (!gf_isset(this.outEdges[source]))
		this.outEdges[source]	= [];
		
	this.edges[id].outEdgesCurID	= this.outEdges[source].length;
	this.outEdges[source][this.outEdges[source].length]	= id;
		
	// store in edge
	if (!gf_isset(this.inEdges[target]))
		this.inEdges[target]	= [];
		
	this.edges[id].inEdgesCurID	= this.inEdges[target].length;
	this.inEdges[target][this.inEdges[target].length]	= id;
};

LinearTimeLayout.prototype.addNode = function (id, nodeData)
{
	this.inEdges[id]	= [];
	this.outEdges[id]	= [];
	this.nodes[id]		= new this.Node(id, nodeData);
	
	return id;
};

LinearTimeLayout.prototype.calculateSpaces = function ()
{
	this.spaces.x	= gv_bv_nodeSettings.distanceX - 100;
	this.spaces.y	= gv_bv_nodeSettings.distanceY - 100;
	
	// cycle through all nodes
	for (var n in this.nodes)
	{
		this.spaces.x	= Math.max(this.nodes[n].getWidth(), this.spaces.x);
		this.spaces.y	= Math.max(this.nodes[n].getHeight(), this.spaces.y);
	}
	
	// cycle through all edges
	for (var e in this.edges)
	{
		this.spaces.x	= Math.max(this.edges[e].getWidth(), this.spaces.x);
		this.spaces.y	= Math.max(this.edges[e].getHeight(), this.spaces.y);
	}
	
	this.spaces.x	+= 100;
	this.spaces.y	+= 100;
};

LinearTimeLayout.prototype.compactLayout = function (rpstnode)
{
	
	// TODO
	/*
In the fourth phase, we compute how much closer a given branch might
be moved to the branch drawn directly above it, ensuring constraint C6 (i.e.,
minimality should be applied). Second, the tree is compacted to ensure con-
straint C6 (i.e., minimality should be applied). In Figure 9, this stage would
shift nodes e and f closer to nodes a to c since the lower branch ends before the
upper branch requires more space to fit in the final nodes.

These arrows form a planar graph
and a planar graph is know to have a maximum of 3v − 6 edges, where v is the
number of vertices for graphs with three or more vertices [13] (v < |f |). During
the compaction phase we iterate over this graph twice, assessing how much each
element may be moved (once to compute the minimum for moving the entire
branch, and once for the actual compaction).

	 */
	
	// TODO
	
	var fragments	= this.fragments(rpstnode);
	var exitNodes	= new Array();
		exitNodes.push(this.nodes[rpstnode.getEntry()]);
		
	for (var f in fragments)
	{
		var fragment	= fragments[f];
		
		if (fragment instanceof this.RPSTNode)
		{
			var entry	= fragment.getEntry();
			if (gf_isset(this.nodes[entry]))
				exitNodes.push(this.nodes[entry]);
		}
	}
	
	// 1) sort by x position	// this.sortByY
	exitNodes.sort(this.sortByX);
	
	var lastX	= 0;
	var diffX	= 0;
	for (var en in exitNodes)
	{
		var node	= exitNodes[en];
		if (node.x > (lastX + this.spaces.x))
		{
			diffX	= Math.floor((node.x - lastX - this.spaces.x) / 2);
		}
		lastX	= node.x;
	}
	
	// TODO: temp hack
	/*
	this.nodes["n3"].x	= 225;
	this.nodes["n4"].x	= 75;
	this.nodes["n5"].x	= 225;
	this.nodes["n6"].x	= 150;
	this.branchWidth["n1"]	= 300;
	*/
};

LinearTimeLayout.prototype.computeBranchDimensions = function (nodeID, parent)
{
	/*
	 * TODO:
	 * - Anpassungen für Layout von oben nach unten + Label-Width (Knoten / Kanten-Label) -> max(kanten-label, knoten-label)
	 * - globaler space Parameter -> spaces.aesthetics
	 * - globales Array height, width -> getHeight / getWidth -> accessing height / width of node / edge
	 * - h, w umbenennen in width / height? -> done
	 * - add width / height of edge
	 */
	
	// TODO: add switch for direction (ttb vs. ltr)
	
	var height	= 0;
	var width	= 0;
	var k		= 0;
	var outEdges	= this.getOutEdges(nodeID);
	for (var edgeID in outEdges)
	{
		edgeID	= outEdges[edgeID].edge;
		
		var newHeight	= 0;
		var newWidth	= 0;
		
		var targetNode	= this.target(edgeID);
		
		if (gf_isset(parent[targetNode]) && parent[targetNode] == nodeID)
		{
			// fixed recursive call -> changed order of parameters
			this.computeBranchDimensions(targetNode, parent);
			
			newHeight	= this.branchHeight[targetNode];
			newWidth	= this.branchWidth[targetNode];
		}
		
		// TODO: ignore spaces.aesthetics?
		if (this.dirLtr())
		{
			height	= height + k * this.spaces.aesthetics + newHeight;
			width	= Math.max(width, newWidth);
		}
		else
		{
			height	= Math.max(height, newHeight);
			width	= width + k * this.spaces.aesthetics + newWidth;
		}
		k		= 1;
	}
	
	// TODO: ignore spaces.aesthetics?
	if (this.dirLtr())
	{
		height	= Math.max(this.getHeight(nodeID, "node"), height);
		width	= this.getWidth(nodeID, "node") + k * this.spaces.aesthetics + width;
	}
	else
	{
		height	= this.getHeight(nodeID, "node") + k * this.spaces.aesthetics + height;	// TODO: Math.max(this.getHeight(node), this.getHeight(edge))?
		width	= Math.max(this.getWidth(nodeID, "node"), width);						// TODO: add space? Width of edge label?	/ Math.max(this.getWidth(nodeID, "node"), this.getWidth(edgeID, "edge"))?
	}
	
	this.branchHeight[nodeID]	= height;
	this.branchWidth[nodeID]	= width;
};

LinearTimeLayout.prototype.createEdgeMap = function (rpstnode)
{
	this.edgeMap		= {};
	
	var children	= this.rpst.getChildren2(rpstnode);
	var outEdges	= {};
	var inEdges		= {};
	
	for (var e in this.edges)
	{
		var src		= this.source(e);
		var tgt		= this.target(e);
		
		if (!gf_isset(outEdges[src]))
		{
			outEdges[src]	= new Array();
		}
		
		if (!gf_isset(inEdges[tgt]))
		{
			inEdges[tgt]	= new Array();
		}
		
		outEdges[src].push(e);
		inEdges[tgt].push(e);
	}
	
	// add entry's edges
	var entry	= rpstnode.getEntry();
	if (gf_isset(outEdges[entry]))
	{
		this.edgeMap[entry]	= new Array();
		for (var o in outEdges[entry])
		{
			var edge	= outEdges[entry][o];
			var tgt		= this.target(edge);
			
			if (gf_isset(this.nodes[entry], children.entry[tgt]))
			{
				this.edgeMap[entry].push({"type": "out", "edge": edge, "v1": entry, "v2": tgt, "v1node": this.nodes[entry], "v2node": children.entry[tgt]});
				
				if (!gf_isset(this.edgeMap[tgt]))
					this.edgeMap[tgt]	= new Array();
					
				this.edgeMap[tgt].push({"type": "in", "edge": edge, "v1": entry, "v2": tgt, "v1node": this.nodes[entry], "v2node": children.entry[tgt]});
			}
		}
	}
	
	// add exit's edges
	var exit	= rpstnode.getExit();
	if (gf_isset(inEdges[exit]))
	{
		this.edgeMap[exit]	= new Array();
		for (var i in inEdges[exit])
		{
			var edge	= inEdges[exit][i];
			var src		= this.source(edge);
			
			if (gf_isset(this.nodes[exit], children.exit[src]))
			{
				this.edgeMap[exit].push({"type": "in", "edge": edge, "v1": src, "v2": exit, "v1node": children.exit[src], "v2node": this.nodes[exit]});
				
				if (!gf_isset(this.edgeMap[src]))
					this.edgeMap[src]	= new Array();
					
				this.edgeMap[src].push({"type": "out", "edge": edge, "v1": src, "v2": exit, "v1node": children.exit[src], "v2node": this.nodes[exit]});
			}
		}
	}
	// add the edges of each child's entry / exit
	for (var c in children.children)
	{
		var child	= children.children[c];
		var entry	= child.getEntry();
		var exit	= child.getExit();
		
		if (gf_isset(inEdges[entry]))
		{
			
			if (!gf_isset(this.edgeMap[entry]))
				this.edgeMap[entry]	= new Array();
				
			for (var i in inEdges[entry])
			{
				var edge	= inEdges[entry][i];
				var src		= this.source(edge);
				
				if (gf_isset(this.nodes[entry], children.exit[src]))
				{
					this.edgeMap[entry].push({"type": "in", "edge": edge, "v1": src, "v2": entry, "v1node": children.exit[src], "v2node": this.nodes[entry]});
				}
			}
		}
		
		if (gf_isset(outEdges[exit]))
		{
		
			if (!gf_isset(this.edgeMap[exit]))
				this.edgeMap[exit]	= new Array();
			
			for (var o in outEdges[exit])
			{
				var edge	= outEdges[exit][o];
				var tgt		= this.target(edge);
				
				if (gf_isset(this.nodes[exit], children.entry[tgt]))
				{
					this.edgeMap[exit].push({"type": "out", "edge": edge, "v1": exit, "v2": tgt, "v1node": this.nodes[exit], "v2node": children.entry[tgt]});
				}
			}
		}
	}
	
	return this.edgeMap;
};

// process structure tree (refined process structure tree -> RPST)
LinearTimeLayout.prototype.decomposePST = function (graph)
{
	// decompose the resulting process model into SESE fragments (Single-Entry Single-Exit)
	var pst	= new this.PST(this);	
	return pst.decompose();
};

// Purpose of this function: saving some typing
LinearTimeLayout.prototype.dirLtr = function ()
{
	return this.direction == "ltr";
};

LinearTimeLayout.prototype.draw = function (rpstnode, x, y)
{	
	var entry	= rpstnode.getEntry();
	var exit	= rpstnode.getExit();
	
	this.drawNode(entry, rpstnode, x, y);
	this.drawNode(exit, rpstnode, x, y);
	
	x	+= rpstnode.x;
	y	+= rpstnode.y;

	if (this.isAtomic(rpstnode))
	{
		// already done by drawing entry and exit of atomic fragment
	}
	else if (this.isSequence(rpstnode))
	{
		var children		= this.rpst.getChildren2(rpstnode).entry;
		
		while (gf_isset(children[entry]))
		{
			var child	= children[entry];
			this.draw(child, x, y);
			
			entry	= child.getExit();
		}
	}
	else if (this.isBranching(rpstnode))
	{
		// update entry and exit
		this.nodes[entry].branchingSplit	= true;
		this.nodes[exit].branchingJoin		= true;
		
		var children		= this.rpst.getChildren2(rpstnode).children;
		for (var c in children)
		{
			var child	= children[c];
			this.draw(child, x, y);
		}
	}
	else if (this.isLoop(rpstnode))
	{
		var children		= this.rpst.getChildren2(rpstnode).entry;
		var visited			= {};
		var loopSpace		= this.dirLtr() ? (rpstnode.dimensions.height - Math.round(this.spaces.y / 2)) : (rpstnode.dimensions.width - Math.round(this.spaces.x / 2));
		
		while (gf_isset(children[entry]))
		{
			var child	= children[entry];
			this.draw(child, x, y);
			
			visited[entry]	= true;
			
			entry	= child.getExit();
			
			if (gf_isset(visited[entry]))
			{
				console.log("it loops");
				this.loopEdges.push({start: child.getEntry(), end: child.getExit(), space: loopSpace});
				break;
			}
		}
		
		// this.loopEdges.push({start: rpstnode.getExit(), end: rpstnode.getEntry(), width: loopWidth});
	}
	// unstructured fragments
	else
	{		
		var fragments	= this.fragments(rpstnode);
		for (var f in fragments)
		{
			var fragment	= fragments[f];
			if (fragment instanceof this.RPSTNode)
			{
				this.draw(fragment, x, y);
			}
		}
	}
};

LinearTimeLayout.prototype.drawEdges = function (rpstnode)
{
	var edges	= rpstnode.getFragment().edges;
			
	if (!gf_isset(this.renderObjects.edges))
		this.renderObjects.edges	= {};
	
	for (var e in edges)
	{
		
		var edge	= edges[e];
		var e		= this.edges[edge];
		
		if (!e.virtual)
		{
			// if reversed: re-reverse before drawing
			if (gf_isset(this.edges[edge]) && this.edges[edge].reversed)
			{
				this.reverse(edge);
			}
			
			var srcID	= this.source(edge);
			var tgtID	= this.target(edge);
			var src		= this.nodes[srcID];
			var tgt		= this.nodes[tgtID];
			var shape	= "straight";
			
			var loopSpace	= 0;
			
			// check loop edges
			for (var l in this.loopEdges)
			{
				var lEdge	= this.loopEdges[l];
				if (lEdge.start == srcID && lEdge.end == tgtID)
				{
					loopSpace	= lEdge.space;
				}
			}
			
			if (srcID.substr(0, 1) == "n")
				srcID	= srcID.substr(1);
			
			if (tgtID.substr(0, 1) == "n")
				tgtID	= tgtID.substr(1);
			
			
			var x1	= src.x;
			var x2	= tgt.x;
			var y1	= src.y;
			var y2	= tgt.y;
			
			var startH			= "center";
			var startV			= "center";
			var endH			= "center";
			var endV			= "center";
			var loopPosition	= "right";
			
			// loop edges
			// take care of loops (ttb): x1: src.right, x2: tgt.right + loopSpace, y1: src.y, y2: tgt.y
			// take care of loops (ltr): x1: src.x, x2: tgt.x, y1: src.top, y2: tgt.top - loopSpace
			if (loopSpace != 0)
			{
				if (this.dirLtr())
				{
					shape			= "loopltr";
					startV			= "bottom";
					endV			= "bottom";
					loopPosition	= "bottom";
				}
				else
				{
					shape			= "loop";
					startH			= "right";
					endH			= "right";
					loopPosition	= "right";
				}
			}
				
			// straight
			else if (x1 == x2)
			{
				startV	= y2 > y1	? "bottom"			: "top";
				endV	= y2 > y1	? "top"				: "bottom";
			}
			
			// straight
			else if (y1 == y2)
			{
				startH	= x2 > x1	? "right"			: "left";
				endH	= x2 > x1	? "left"			: "right";
			}
				
			// diag: change y1 to src.y and y2 to tgt.y
			else if (tgt.branchingJoin && src.branchingSplit)
			{
				shape	= "diag";
				startH	= x2 > x1	? "right"			: "left";
				endH	= x2 > x1	? "left"			: "right";
			}
			
			// diagvert / diaghor
			else if (src.branchingSplit)
			{
				if (this.dirLtr())
				{
					shape	= "diaghor";
					
					endH	= x2 > x1	? "left"			: "right";
					startV	= y2 > y1	? "bottom"			: "top";
				}
				else
				{
					shape	= "diagvert";
					
					startH	= x2 > x1	? "right"			: "left";
					endV	= y2 > y1	? "top"				: "bottom";
				}
			}
				
			// vertdiag / hordiag
			else if (tgt.branchingJoin)
			{
				if (this.dirLtr())
				{
					shape	= "hordiag";
					
					startH	= x2 > x1	? "right"			: "left";
					endV	= y2 > y1	? "top"				: "bottom";
				}
				else
				{
					shape	= "vertdiag";
					
					endH	= x2 > x1	? "left"			: "right";
					startV	= y2 > y1	? "bottom"			: "top";
				}	
			}
						
			if (!gf_isset(this.renderObjects.edges[e.orgId]))
				this.renderObjects.edges[e.orgId]	= new GCrenderEdge(e.orgId, e.edgeData);

			this.renderObjects.edges[e.orgId].setEndPoints(srcID, tgtID);
			this.renderObjects.edges[e.orgId].setLoopSpace(loopSpace, loopPosition);
			this.renderObjects.edges[e.orgId].setShape(shape);
			this.renderObjects.edges[e.orgId].setPosStart(startH, startV);
			this.renderObjects.edges[e.orgId].setPosEnd(endH, endV);
		}
	}
}

LinearTimeLayout.prototype.drawNode = function (id, rpstnode, paperOffsetX, paperOffsetY)
{
	if (!gf_isset(this.drawnNodes[id]))
	{
		var node	= this.nodes[id];
		if (!gf_isset(node))
		{
			console.log("not set");
			console.log(rpstnode);
		}
		else if (node.virtual == true)
		{
			// correct position as virtual nodes are not drawn
			if (gf_isset(rpstnode) && rpstnode != null && rpstnode.getEntry() == id)
			{
				if (this.dirLtr())
				{
					rpstnode.x	-= this.spaces.x;
				}
				else
				{
					rpstnode.y	-= this.spaces.y;
				}
			}
		}
		else
		{
			var rpstX	= rpstnode == null ? 0 : rpstnode.x;
			var rpstY	= rpstnode == null ? 0 : rpstnode.y;
			var x		= paperOffsetX + node.x + rpstX;
			var y		= paperOffsetY + node.y + rpstY;
			this.drawnNodes[id]	= true;
			
			// update node's x and y to have a reference for edges
			node.x		= x;
			node.y		= y;
			
			// console.log("... drawing " + id + " @ " + x + " / " + y);
			
			
			// TODO: prepare for ltr layout
			if (this.mostLeft == null || x < this.mostLeft)
			{
				this.mostLeft	= x;
			}
			
			if (!gf_isset(this.renderObjects.nodes))
				this.renderObjects.nodes	= {};
				
			if (!gf_isset(this.renderObjects.nodes[node.node.id]))
				this.renderObjects.nodes[node.node.id]	= new GCrenderNode(node.node.id, node.node);

			this.renderObjects.nodes[node.node.id].setPosition(node.x, node.y);
		}
	}	
};

LinearTimeLayout.prototype.entryNode = function (rpstnode)
{
	// returns the entry node of a given SESE fragment. That is the node to which the single input of the SESE fragment is connected to.
	return rpstnode.getEntry();
};

// currently returns array of RPSTNodes
LinearTimeLayout.prototype.fragments = function (node)
{
	/*
		The fragments function returns the fragments of a given process or fragment
		as well as the nodes that are not part of a another fragment.
	 */
	
	var fragments	= [];
	if (node instanceof this.RPSTNode)
	{
		// fragments = children ....
		fragments	= this.rpst.getChildren2(node).children;
		
		// + free nodes
		var freeNodes	= node.getFragment().getNodes();
		for (var fn in freeNodes)
		{
			fragments.push(this.nodes[fn]);
		}
	}
	return fragments;
};

LinearTimeLayout.prototype.getEdges = function (node)
{
	// returns the edges within a given fragment.
	
	var edges	= new Array();
	
	if (node instanceof this.RPSTNode)
	{
		var entry	= node.getEntry();
		var exit	= node.getExit();
		
		if (gf_isset(this.edgeMap[entry]))
		{
			for (var e in this.edgeMap[entry])
			{
				edges.push(this.edgeMap[entry][e]);
			}
		}
		
		if (gf_isset(this.edgeMap[exit]))
		{
			for (var e in this.edgeMap[exit])
			{
				edges.push(this.edgeMap[exit][e]);
			}
		}
	}
	else
	{
		if (gf_isset(this.edgeMap[node]))
		{
			for (var e in this.edgeMap[node])
			{
				edges.push(this.edgeMap[node][e]);
			}
		}
	}
	
	return edges;
};

// by Matthias Schrammek
LinearTimeLayout.prototype.getFragmentEdges = function ()
{
	var edges	= {};
	
	for (var em in this.edgeMap)
	{
		var edgeMap	= this.edgeMap[em];
		for (var e in edgeMap)
		{
			var edge	= edgeMap[e].edge;
			edges[edge]	= edge;
		}
	}
	
	return edges;
};

// elementID: either edgeID or nodeID
// elementType: either "node" or "edge"
LinearTimeLayout.prototype.getHeight = function (elementID, elementType)
{
	if (!gf_isset(elementType) || elementType != "edge")
		elementType	= "node";
		
	if (elementType == "edge")
	{
		// TODO
		/*
		if (gf_isset(this.edges[elementID]))
			return this.edges[elementID].getHeight();
		*/
		return this.spaces.y;
	}
	else
	{
		// TODO
		/*
		if (gf_isset(this.nodes[elementID]))
			return this.nodes[elementID].getHeight();
		*/
		return this.spaces.y;
	}
		
	return 0;
};

LinearTimeLayout.prototype.getInEdges = function (node)
{	
	var edges	= new Array();
	
	if (node instanceof this.RPSTNode)
	{
		node	= node.getEntry();
	}
	
	if (gf_isset(this.edgeMap[node]))
	{
		for (var e in this.edgeMap[node])
		{
			var edge	= this.edgeMap[node][e];
			if (edge.type == "in")
				edges.push(edge);
		}
	}
	
	return edges;
};

LinearTimeLayout.prototype.getNodes = function (rpstnode)
{
	/*
	 * returns the nodes within a given fragment.
	 * NODES(B) = {g, C, D, h} == fragments(B)??
	 */
	// TODO: return as array; [.length] = nodeID
	// return rpstnode.getFragment().getNodes();
	
	/*
	var resultingNodes	= new Array();
	var fragmentNodes	= rpstnode.getFragment().getNodes();
	for (var n in fragmentNodes)
	{
		resultingNodes.push(n);
	}
	
	return resultingNodes;
	*/
	return this.fragments(rpstnode);
};

LinearTimeLayout.prototype.getOutEdges = function (node)
{
	var edges	= new Array();
	
	if (node instanceof this.RPSTNode)
	{
		node	= node.getExit();
	}
	
	if (gf_isset(this.edgeMap[node]))
	{
		for (var e in this.edgeMap[node])
		{
			var edge	= this.edgeMap[node][e];
			if (edge.type == "out")
				edges.push(edge);
		}
	}
	
	return edges;
};

// elementID: either edgeID or nodeID
// elementType: either "node" or "edge
LinearTimeLayout.prototype.getWidth = function (elementID, elementType)
{
	if (!gf_isset(elementType) || elementType != "edge")
		elementType	= "node";
		
	if (elementType == "edge")
	{
		// TODO
		/*
		if (gf_isset(this.edges[elementID]))
			return this.edges[elementID].width;
		*/
		return this.spaces.x;
	}
	else
	{
		// TODO
		/*
		if (gf_isset(this.nodes[elementID]))
			return this.nodes[elementID].width;
		*/
		return this.spaces.x;	
	}
		
	return 0;
};

LinearTimeLayout.prototype.identifyEdgeTypes = function (node, type, discovered, finished, t)
{
	// DFS implementation!
		
	t++;
	discovered[node]	= t;
	
	var outEdges	= this.getOutEdges(node);
	
	for (var edgeID in outEdges)
	{		
		var edge	= outEdges[edgeID];
		var newNode	= edge.v2;
		
		if (!gf_isset(discovered[newNode]))
		{
			type[edge.edge]	= this.edgeTypes.tree;
			t				= this.identifyEdgeTypes(newNode, type, discovered, finished, t);		// TODO: check, if type, discovered and finished are filled correctly
		}
		else if (!gf_isset(finished[newNode]))
		{
			type[edge.edge]	= this.edgeTypes.back;
		}
		else if (discovered[newNode] > discovered[node])
		{
			type[edge.edge]	= this.edgeTypes.forward;
		}
		else
		{
			type[edge.edge]	= this.edgeTypes.cross;
		}
	}
	
	t++;
	finished[node]	= t;
	return t;
};

LinearTimeLayout.prototype.isAtomic = function (node)
{
	// TODO
	// Atomic fragments are individual nodes that cannot be split up any further.
	
	return (node instanceof this.Node);
};

LinearTimeLayout.prototype.isBranching = function (rpstnode)
{
	// TODO
	/*
Branching fragments, in turn, are laid out by
first arranging the individual branches vertically. Then, the diverging node is
put to the left of the branches and the converging node is put to the right, as
shown in Figure 5. When laying out the branches, they can be optimized by
using the actual shape of the branches and pushing them vertically together as
shown in Figure 5 to minimize the fragment’s area.
	 */
	
	if (rpstnode.type == "rigid")
	{
		return false;
	}
	
	var children	= this.rpst.getChildren2(rpstnode);
	var entry		= rpstnode.getEntry();
	var exit		= rpstnode.getExit();
	
	if (children.entryCount[entry] < 2)
	{
		return false;
	}
	
	if (children.exitCount[exit] < 2)
	{
		return false;
	}
	
	return true;
};

LinearTimeLayout.prototype.isLoop = function (rpstnode)
{
	
	// TODO
	/*
Since Structured loops and sequences are almost identical from a structural
perspective, we use a similar strategy for computing the layout. In particular, we
lay out the converging gateway, the optional body and the diverging gateway like
a sequence. In addition, the loop-back branches are laid out like the branches of
a branching fragment.
	 */
	
	// walk through all edges, note visited nodes, if one edge ends at an already visited node and all other edges have outEdges <= 1 --> loop
	if (rpstnode.type == "rigid")
	{
		return false;
	}
	
	var children	= this.rpst.getChildren2(rpstnode);
	var entry		= rpstnode.getEntry();
	var exit		= rpstnode.getExit();
	var visited		= {};
	
	while (gf_isset(children.entry[entry]) && !gf_isset(visited[entry]))
	{
		var child	= children.entry[entry];
			exit	= child.getExit();
			
		visited[entry]	= true;
			
		if (children.entryCount[entry] > 1 || children.exitCount[entry] > 1)
		{
			return false;
		}
		
		if (children.entryCount[exit] > 1 || children.exitCount[exit] > 1)
		{	
			return false;
		}
		
		entry	= exit;
	}
	
	return true;
};

LinearTimeLayout.prototype.isSequence = function (rpstnode)
{
	// TODO
	/*
Sequences are laid out as
straight lines, ensuring that the exit of one fragment is on the same height as
the entry of the next fragment.
	 */
	
	// TODO: check for cases when no children are available (only free nodes)
	
	if (rpstnode.type == "rigid")
	{
		return false;
	}
	
	var children	= this.rpst.getChildren2(rpstnode);
	var entry		= rpstnode.getEntry();
	var exit		= rpstnode.getExit();
	var visited		= {};
	
	while (gf_isset(children.entry[entry]))
	{
		var child	= children.entry[entry];
			exit	= child.getExit();
			
		// avoid loops
		visited[entry]	= true;
		if (gf_isset(visited[exit]))
		{
			return false;
		}
			
		if (children.entryCount[entry] > 1 || children.exitCount[entry] > 1)
		{
			return false;
		}
		
		if (children.entryCount[exit] > 1 || children.exitCount[exit] > 1)
		{
			return false;
		}
		
		entry	= exit;
	}
	
	return true;
};

LinearTimeLayout.prototype.layout = function ()
{
	// 1) calculate spaces.y and spaces.x, width and height of objects
	this.calculateSpaces();
	
	// additional step: count edges
	var edgeCount	= 0;
	for (var e in this.edges)
	{
		edgeCount++;
		if (edgeCount > 0)
			break;
	}
	
	// additional step: draw elements
	var x	= this.dirLtr()	? 50			: Math.round(gv_paperSizes.bv_width/2);		// TODO
	var y	= this.dirLtr()	? Math.round(gv_paperSizes.bv_height/2)			: 50;		// TODO
	
	// addition: ignore single nodes
	if (edgeCount > 0)
	{
		this.normGraph		= this.preprocess();
		this.rpst			= this.decomposePST();
		
		for (var n in this.rpst.nodes)
		{
			var node	= this.rpst.nodes[n];
			// console.log(node.name + " " + node.type + " " + node.getEntry() + " " + node.getExit());
		}
		
		this.layoutFragment(this.rpst.getRoot());
	
		gf_timeCalc("draw nodes");
		this.draw(this.rpst.getRoot(), x, y);				// TODO: MTG, restart with every node
		gf_timeCalc("draw nodes");
		gf_timeCalc("draw edges");
		this.drawEdges(this.rpst.getRoot());
		gf_timeCalc("draw edges");
	}
	
	// additional step: draw all free nodes (only in ttb direction)
	if (!this.dirLtr())
	{
		/*
		 * TODO:
		 * 1) calc most left spot of tree; move next node by 2x this.spaces.x to the left; increase space between all nodes by this.spaces.y
		 */
		if (this.mostLeft == null)
		{
			this.mostLeft	= x;
		}
		else
		{
			this.mostLeft	-= 2 * this.spaces.x;
		}
		
		for (var n in this.nodes)
		{
			if (!gf_isset(this.drawnNodes[n]))
			{
				this.drawNode(n, null, this.mostLeft, y);
				y	+= this.spaces.y;
			}
		}
	}
};

LinearTimeLayout.prototype.layoutAtomic = function (node)
{
	// no special layout required
	node.dimensions.width	= this.spaces.x;
	node.dimensions.height	= this.spaces.y;
};

LinearTimeLayout.prototype.layoutBranching = function (rpstnode)
{	
	var sumHeight	= 0;
	var sumWidth	= 0;
	var maxHeight	= 0 - this.spaces.y;
	var maxWidth	= 0 - this.spaces.x;
	
	// precalc sumWidth, sumHeight
	var children	= this.rpst.getChildren2(rpstnode).children;
	for (var c in children)
	{
		var child	= children[c];
		sumHeight	= sumHeight + child.dimensions.height;
		sumWidth	= sumWidth + child.dimensions.width;
		maxHeight	= Math.max(maxHeight, child.dimensions.height);
		maxWidth	= Math.max(maxWidth, child.dimensions.width);
	}
	
	var offset		= this.dirLtr()	? (0 - Math.ceil(sumHeight / 2)) : (0 - Math.ceil(sumWidth / 2));
	var prevChild	= null;
	
	for (var c in children)
	{
		var child		= children[c];
		
		var prevSize	= 0;
		var prevPos		= offset;
		
		// update child's position (except if child == entry of this node)
		if (prevChild != null)
		{
			prevSize	= this.dirLtr() ? prevChild.dimensions.height : prevChild.dimensions.width;
			prevPos		= this.dirLtr() ? prevChild.y : prevChild.x;
		}
		
		if (this.dirLtr())
		{
			child.x		= 0;
			child.y		= prevPos + Math.ceil(child.dimensions.height / 2) + Math.ceil(prevSize / 2);
		}
		else
		{
			child.x		= prevPos + Math.ceil(child.dimensions.width / 2) + Math.ceil(prevSize / 2);
			child.y		= 0;
		}
		
		// update data for next loop run
		prevChild	= child;
	}
	
	// update exit
	var exit			= rpstnode.getExit();
	
	if (this.dirLtr())
		this.nodes[exit].x	= maxWidth	+ this.spaces.x;
	else
		this.nodes[exit].y	= maxHeight + this.spaces.y;
	
	// update size
	if (this.dirLtr())
	{
		rpstnode.dimensions.height	= sumHeight;
		rpstnode.dimensions.width	= maxWidth + this.spaces.x;
	}
	else
	{
		rpstnode.dimensions.height	= maxHeight + this.spaces.y;
		rpstnode.dimensions.width	= sumWidth;
	}
};

LinearTimeLayout.prototype.layoutFragment = function (rpstnode)
{
	var nodes	= this.fragments(rpstnode);
	for (var n in nodes)
	{
		var node	= nodes[n];
		this.layoutFragment(node);
	}
	
	if (this.isAtomic(rpstnode))
	{
		this.layoutAtomic(rpstnode);
	}
	else if (this.isSequence(rpstnode))
	{
		this.layoutSequence(rpstnode);
	}
	else if (this.isBranching(rpstnode))
	{
		this.layoutBranching(rpstnode);
	}
	else if (this.isLoop(rpstnode))
	{
		this.layoutLoop(rpstnode);
	}
	else
	{
		this.layoutUnstructured(rpstnode);
	}
};

LinearTimeLayout.prototype.layoutLoop = function (rpstnode)
{
	var entry		= rpstnode.getEntry();
	var maxHeight	= 0;
	var maxWidth	= 0;
	var sumHeight	= 0 - this.spaces.y;
	var sumWidth	= 0 - this.spaces.x;
	var visited		= {};
	
	var prevChild	= null;
	
	// TODO: 1) identify backedges?
	// TODO: how to calc width of edge? (where to lead it back) -> x offset (left | right; times x-space)
	
// get fragments??
// always layout entry + exit?
// start at entry point: follow edges to either child RPSTNodes or free nodes (from fragments)
	
	var children		= this.rpst.getChildren2(rpstnode).entry;
	while (gf_isset(children[entry]))
	{
		var child	= children[entry];
		
		visited[entry]	= true;
		
		// update size of this node
		maxHeight	= Math.max(maxHeight, child.dimensions.height);
		maxWidth	= Math.max(maxWidth, child.dimensions.width);
		sumHeight	= sumHeight + child.dimensions.height;
		sumWidth	= sumWidth + child.dimensions.width;
		
		// update child's position (except if child == entry of this node)
		if (prevChild != null)
		{
			if (this.dirLtr())
			{
				child.x	= prevChild.x + prevChild.dimensions.width;
			}
			else
			{
				child.y	= prevChild.y + prevChild.dimensions.height;
			}
		}
		
		// update data for next loop run
		entry		= child.getExit();
		prevChild	= child;
		
		if (gf_isset(visited[entry]))
		{
			break;
		}
	}
	
	// update size
	if (this.dirLtr())
	{
		rpstnode.dimensions.height	= maxHeight + this.spaces.y;
		rpstnode.dimensions.width	= sumWidth;
	}
	else
	{
		rpstnode.dimensions.height	= sumHeight;
		rpstnode.dimensions.width	= maxWidth + this.spaces.x;
	}
};

LinearTimeLayout.prototype.layoutSequence = function (rpstnode)
{
	if (rpstnode.type == "trivial")
	{
		// update size
		rpstnode.dimensions.height	= this.spaces.y;
		rpstnode.dimensions.width	= this.spaces.x;
		
		// update position of exit
		var exit	= rpstnode.getExit();
		var node	= this.nodes[exit];
		
		if (this.dirLtr())
			node.x	= this.spaces.x;
		else
			node.y	= this.spaces.y;
	}
	else
	{
	
		var entry		= rpstnode.getEntry();
		var maxHeight	= 0;
		var maxWidth	= 0;
		var sumHeight	= 0 - this.spaces.y;
		var sumWidth	= 0 - this.spaces.x;
		
		var prevChild	= null;
		
	// get fragments??
	// always layout entry + exit?
	// start at entry point: follow edges to either child RPSTNodes or free nodes (from fragments)
		
		var children		= this.rpst.getChildren2(rpstnode).entry;
		while (gf_isset(children[entry]))
		{
			var child	= children[entry];
			
			// update size of this node
			maxHeight	= Math.max(maxHeight, child.dimensions.height);
			maxWidth	= Math.max(maxWidth, child.dimensions.width);
			sumHeight	= sumHeight + child.dimensions.height;
			sumWidth	= sumWidth + child.dimensions.width;
			
			// update child's position (except if child == entry of this node)
			if (prevChild != null)
			{
				if (this.dirLtr())
				{
					child.x	= prevChild.x + prevChild.dimensions.width;
				}
				else
				{
					child.y	= prevChild.y + prevChild.dimensions.height;
				}
			}
			
			// update data for next loop run
			entry		= child.getExit();
			prevChild	= child;
		}
		
		// update size
		if (this.dirLtr())
		{
			rpstnode.dimensions.height	= maxHeight;
			rpstnode.dimensions.width	= sumWidth;
		}
		else
		{
			rpstnode.dimensions.height	= sumHeight;
			rpstnode.dimensions.width	= maxWidth;
		}
		
		
		console.log(rpstnode.type + " " + rpstnode.name + " " + rpstnode.getEntry() + " " + rpstnode.getExit());
		
		// update exit
		var exit		= rpstnode.getExit();
		var exitNode	= this.nodes[exit];
		
		if (this.dirLtr())
		{
			exitNode.x	= sumWidth + this.spaces.x;
		}
		else
		{			
			exitNode.y	= sumHeight + this.spaces.y;
		}
	}
};

LinearTimeLayout.prototype.layoutUnstructured = function (rpstnode)
{
	console.log("laying out unstructured: " + rpstnode.name + " " + rpstnode.type);
	
	// by Matthias Schrammek
	this.createEdgeMap(rpstnode);
	
	// by Matthias Schrammek
	// cycle through all nodes and mark nodes with >1 incoming as "branchingJoin" and nodes with >1 outgoing as "branchingSplit" (for edge drawing)
	for (var n in this.edgeMap)
	{		
		var edgeMap		= this.edgeMap[n];
		var inCount		= 0;
		var outCount	= 0;
		for (var e in edgeMap)
		{
			var edge	= edgeMap[e];
			
			if (edge.type == "out")
			{
				outCount++;
			}
			
			if (edge.type == "in")
			{
				inCount++;
			}
		}
		
		this.nodes[n].branchingSplit	= outCount > 1;
		this.nodes[n].branchingJoin		= inCount > 1;
	}
	
	// 1) identify back edges and compute the node order
	var type	= {};
	// TODO: var nodeID	= this.entryNode(fragment);
	var nodeID	= rpstnode.getEntry();
	
	this.identifyEdgeTypes(nodeID, type, {}, {}, 0);
	
	var topology	= this.topologySort(rpstnode, nodeID, type);
	var parent		= {};
	var length		= {};
	
	this.lpstree(topology, type, parent, length);
	
	// 2) order edges to reduce number of crossings
	var newFragment	= this.orderEdges(rpstnode, nodeID);
	
	// TODO: use newFragment and implement orderEdges
	
	// 3) internally reverse back edges and compute layout
	// var fragmentEdges	= this.getEdges(rpstnode);	// TODO: newFragment?
	var fragmentEdges	= this.getFragmentEdges();
	
	for (var eID in fragmentEdges)
	{
		var edgeID	= fragmentEdges[eID];
		if (type[edgeID] == this.edgeTypes.back)
		{
			this.reverse(edgeID);
		}
	}
	this.computeBranchDimensions(nodeID, parent);
	this.preliminaryLayout(nodeID, parent, 0, 0);
	this.compactLayout(rpstnode);
	
	// by Matthias Schrammek
	if (this.dirLtr())
	{
		rpstnode.dimensions.width	= this.branchWidth[nodeID] - this.spaces.x;
		rpstnode.dimensions.height	= this.branchHeight[nodeID];
		rpstnode.y					= 0;
	}
	else
	{
		rpstnode.dimensions.width	= this.branchWidth[nodeID];
		rpstnode.dimensions.height	= this.branchHeight[nodeID] - this.spaces.y;
		rpstnode.x					= 0;
	}
	
	for (var n in this.edgeMap)
	{
		if (this.dirLtr())
		{
			this.nodes[n].y	-= Math.floor(this.branchHeight[nodeID] / 2);
		}
		else
		{
			this.nodes[n].x	-= Math.floor(this.branchWidth[nodeID] / 2);
		}
	}
	
	// TODO: hack? keep it?
	if (this.dirLtr())
	{
		this.nodes[nodeID].y		= 0;
	}
	else
	{
		this.nodes[nodeID].x		= 0;
	}
};

LinearTimeLayout.prototype.lpstree = function (topology, type, parent, length)
{
	for (var nID in topology)
	{
		var nodeID	= topology[nID];
		
		// TODO: change width to height (from top to bottom instead of left to right)
		// var width		= this.getWidth(nodeID, "node") + this.spaces.aesthetics;
		var space	= this.dirLtr() ? this.getWidth(nodeID, "node") : this.getHeight(nodeID, "node");	// TODO: spaces.aesthetics?, add Math.max(width of node, width of edge)?
			space += this.spaces.aesthetics;
		
		var outEdges	= this.getOutEdges(nodeID);
		for (var edgeID in outEdges)
		{
			edgeID	= outEdges[edgeID].edge;
			if (type[edgeID] != this.edgeTypes.back)
			{
				var targetID	= this.target(edgeID);
				
				// TODO: replace width by height
				if (!gf_isset(length[targetID]) || length[targetID] < length[nodeID] + space)
				{
					length[targetID]	= length[nodeID] + space;
					parent[targetID]	= nodeID;
				}
			}
		}			
	}
};

LinearTimeLayout.prototype.orderEdges = function (fragment, nodeID)
{
	// TODO:
	
	/*
Order Edges. In the second step, our layout algorithm computes the order
in which the edges have to be drawn to fulfill constraint C3 (i.e., edge crossings
should be minimized). A planar order for the edges of the graph is computed
using a left-right planarity checker [13]. This checker computes a spanning
13
tree of the business process’s underlying undirected graph and partitions the
remaining edges into left and right partitions such that they do not generate
conflicts (i.e., edge-crossings).
Figure 8: Artificial Edge Added to Each Fragment
Before passing a fragment to the planarity checker, it is pre-processed such
that edges are no longer permitted to be drawn around the entire process (cf.
Figure 2(a)). We accomplish this by adding an artificial edge from a fragment’s
entry node to the fragment’s exit node (cf. Figure 8) which prohibits an edge on
either the upper or the lower side of a fragment to be drawn around the entry
or exit nodes since it would need to cross the artificial edge.
The planarity checker returns a planar order for edges indicating how to draw
them in order to avoid edge crossing. In this stage all edges (i.e., including back
edges that were omitted previously) are considered. The planar order will be
used in subsequent stages. If a graph is non-planar we perform some extra
processing to find an optimal edge order. This modification will be discussed in
Section 4.4.
For the details of this algorithm, we refer the interested reader to [13] where
Hopcroft et al. show that the algorithm runs with a complexity of O(|f |).

	 */
	
	var newFragment	= null;
	return fragment;	// TODO
};

// nodeID: id, parent: array, x: int, y: int
LinearTimeLayout.prototype.preliminaryLayout = function (nodeID, parent, x, y)
{
	/*
	 * TODO:
	 * - branchheight, height, width, space als globale Variablen
	 */
	
	if (this.dirLtr())
	{
		this.nodex[nodeID]	= x;
		this.nodey[nodeID]	= y + Math.floor((this.branchHeight[nodeID] - this.getHeight(nodeID, "node")) / 2);	// TODO: add Math.max(getHeight(node), getHeight(edge))?
	}
	else
	{
		this.nodex[nodeID]	= x + Math.floor((this.branchWidth[nodeID] - this.getWidth(nodeID, "node")) / 2);	// TODO: add Math.max(getWidth(node), getWidth(edge))?
		this.nodey[nodeID]	= y;
	}
	
	// TODO: calculate a better position
	this.nodes[nodeID].x	= this.nodex[nodeID];
	this.nodes[nodeID].y	= this.nodey[nodeID];
	
	var height	= 0;
	var width	= 0;
	var k		= 0;
	var outEdges	= this.getOutEdges(nodeID);
	for (var edgeID in outEdges)
	{
		edgeID	= outEdges[edgeID].edge;
		if (parent[this.target(edgeID)] == this.source(edgeID))
		{
			height	= height + k * this.spaces.aesthetics + this.branchHeight[this.target(edgeID)];
			width	= width + k * this.spaces.aesthetics + this.branchWidth[this.target(edgeID)];
		}
		else
		{
			height	= height + k * this.spaces.aesthetics;
			width	= width + k * this.spaces.aesthetics;
		}
		k	= 1;
	}
	
	if (this.dirLtr())
	{
		x	= x + this.getWidth(nodeID, "node") + this.spaces.aesthetics;
		y	= y + Math.max(0, (this.branchHeight[nodeID] - height) / 2);
	}
	else
	{
		x	= x + Math.max(0, (this.branchWidth[nodeID] - width) / 2);
		// y	= y + this.height[nodeID] + this.spaces.aesthetics;
		y	= y + this.getHeight(nodeID, "node") + this.spaces.aesthetics;
	}
	
	for (var edgeID in outEdges)
	{
		edgeID	= outEdges[edgeID].edge;
		if (parent[this.target(edgeID)] == this.source(edgeID))
		{
			this.preliminaryLayout(this.target(edgeID), parent, x, y);
			
			if (this.dirLtr())
			{
				y	= y + this.branchHeight[this.target(edgeID)] + this.spaces.aesthetics;
			}
			else
			{				
				x	= x + this.branchWidth[this.target(edgeID)] + this.spaces.aesthetics;
			}
		}
		else
		{
			this.edgex[edgeID]	= x;
			
			if (this.dirLtr())
			{
				y	= y + this.spaces.aesthetics;
			}
			else
			{
				x	= x + this.spaces.aesthetics;
			}
		}
	}
};

LinearTimeLayout.prototype.preprocess = function ()
{
	// by Matthias Schrammek
	
	var sources			= new Array();
	var sinks			= new Array();
	var mixed			= new Array();
	
	for (var n in this.nodes)
	{
		// by Matthias Schrammek: added check for start nodes
		var node	= this.nodes[n];
		if (!node.isStart() && !node.isEnd())
		{
			continue;
		}
		
		if (node.isStart())
		{
			sources.push(n);
		}
		
		if (!node.isStart() && node.isEnd())
		{
			sinks.push(n);
		}
	}
	
	// create new source
	var uniqueSource	= this.addNode("##SRC##", "virtual");
	for (var s in sources)
	{
		var sourceID	= sources[s];
		this.addEdge("virtSRC" + sourceID, uniqueSource, sourceID, "virtual");
	}
	
	// create new sink
	var uniqueSink	= this.addNode("##SNK##", "virtual");
	for (var s in sinks)
	{
		var sinkID	= sinks[s];
		this.addEdge("virtSNK" + sinkID, sinkID, uniqueSink, "virtual");
	}
	
	
	// jBPT preprocess (MTG)
	var normalizedGraph	= new this.NormGraph(this);
	
	var sources			= new Array();
	var sinks			= new Array();
	var mixed			= new Array();
	
	// copy nodes
	for (var n in this.nodes)
	{
		// by Matthias Schrammek: added check for start nodes
		var node	= this.nodes[n];
		if (this.inEdges[n].length == 0 && this.outEdges[n].length == 0)
		{
			continue;
		}
		
		if (this.inEdges[n].length == 0)
		{
			sources.push(n);
		}
		
		if (this.outEdges[n].length == 0)
		{
			sinks.push(n);
		}
		
		if (this.inEdges[n].length > 1 && this.outEdges[n].length > 1)
		{
			mixed.push(n);
		}
		
		var newId			= normalizedGraph.addNode(n)
		
		this.on2nn[n]		= newId;
		this.nn2on[newId]	= n;
	}
	
	// copy edges
	for (var e in this.edges)
	{
		var edge	= this.edges[e];
		var source	= this.on2nn[this.source(e)];
		var target	= this.on2nn[this.target(e)];
		this.ne2oe[normalizedGraph.addEdge(source, target)]	= e;
	}
	
	// create new source
	var uniqueSource	= normalizedGraph.addNode("##SRC##", true);
	for (var s in sources)
	{
		var sourceID	= sources[s];
		var newEdge		= normalizedGraph.addEdge(uniqueSource, this.on2nn[sourceID]);
		this.extraEdges[newEdge]	= newEdge;
	}
	
	// create new sink
	var uniqueSink	= normalizedGraph.addNode("##SNK##", true);
	for (var s in sinks)
	{
		var sinkID	= sinks[s];
		var newEdge	= normalizedGraph.addEdge(this.on2nn[sinkID], uniqueSink);
		this.extraEdges[newEdge]	= newEdge;
	}
	
	// split nodes with > 1 incoming and > 1 outgoing edges
	for (var m in mixed)
	{
		var mixedID	= mixed[m];
		var newNode	= normalizedGraph.addNode(mixedID + "*");
		
			mixedID	= this.on2nn[mixedID];
		
		for (var e in normalizedGraph.inEdges[mixedID])
		{
			var edge	= normalizedGraph.inEdges[mixedID][e];
			normalizedGraph.removeEdge(edge);
			var oEdge	= this.ne2oe[edge];
			delete this.ne2oe[edge];
			var newEdge	= normalizedGraph.addEdge(this.on2nn[this.source(oEdge)], newNode);
			this.ne2oe[newEdge]	= oEdge;
		}
		
		delete normalizedGraph.inEdges[mixedID];
		var newEdge2	= normalizedGraph.addEdge(newNode, mixedID);
		this.extraEdges[newEdge2]	= newEdge;
	}
	
	var backedge				= normalizedGraph.addEdge(uniqueSink, uniqueSource);
	this.extraEdges[backedge]	= backedge;
	this.backedge				= backedge;
	
	return normalizedGraph;
	
	/*
	 * Overall, the proposed algorithm follows three phases, as shown in Algo-
rithm 1. First, the process model to be laid out is pre-processed.
	 * The Preprocess function of the algorithm pre-processes gateways with
multiple incoming and outgoing edges to facilitate the model’s decomposition
into SESE fragments. Respective gateways are split into two gateways: one
having all the originally incoming edges and the other covering all the originally
outgoing edges. Similarly, multiple incoming and multiple outgoing edges of
non-gateways (e.g., activities) are separated into an additional gateway. To
illustrate the pre-processing step, Figure 4 shows two exemplary pre-processing
transformations. In Figure 4 (a) an additional gateway is introduced to have
either multiple incoming or multiple outgoing edges. Similarly, in Figure 4 (b),
two gateways are introduced to make the join gateways and split gateways
explicit.

By applying these transformations, the business process model remains se-
mantically equivalent to the original model but ensures constraint C2 (i.e., in-
coming and outgoing edges must be separated). The gateways introduced during
this pre-processing step are used for the computation of the edge ordering only;
they can be removed from the final layout of the business process model, i.e.,
the algorithm does not change the structure of the model.

Due to the pre-processing, each node has either multiple incoming edges or
multiple outgoing edges. The only correct edge orderings for the left gateway
are all permutations of {1, 2, x} and for the right {x, 3, 4}. This constrains the
permutations allowed for edges 1 to 4. to the permutations of {1, 2, {3, 4}} which
makes sure that edges 1 and 2 and edges 3 and 4 stay together. Since edges are
drawn in a clock-wise order, orderings {1, 2, 3, 4} and {2, 3, 4, 1} are the same.


		 */
};

LinearTimeLayout.prototype.reverse = function (edgeID)
{
	
	if (gf_isset(this.edges[edgeID]))
	{
		// Update outEdges
		var source	= this.source(edgeID);
		var target	= this.target(edgeID);
		
		/*
		 * out edge
		 */
		var curID	= null;
		
		// move edge to target's outEdges array
		if (this.edges[edgeID].outEdgesOldID == null)
		{
			curID	= this.outEdges[target].length;
		}
		// restore edge in old outEdges array
		else
		{
			curID	= this.edges[edgeID].outEdgesOldID;
		}
		this.outEdges[target][curID]	= edgeID;
		
		// remove node from source's outEdges array
		delete this.outEdges[source][this.edges[edgeID].outEdgesCurID];
		
		this.edges[edgeID].outEdgesOldID	= this.edges[edgeID].outEdgesCurID;
		this.edges[edgeID].outEdgesCurID	= curID;
		
		/*
		 * in edge
		 */
		var curID	= null;
		
		// move edge to target's inEdges array
		if (this.edges[edgeID].inEdgesOldID == null)
		{
			curID	= this.inEdges[target].length;
		}
		// restore edge in old inEdges array
		else
		{
			curID	= this.edges[edgeID].inEdgesOldID;
		}
		this.inEdges[target][curID]	= edgeID;
		
		// remove node from source's inEdges array
		delete this.inEdges[source][this.edges[edgeID].inEdgesCurID];
		
		this.edges[edgeID].inEdgesOldID	= this.edges[edgeID].inEdgesCurID;
		this.edges[edgeID].inEdgesCurID	= curID;
	
		this.edges[edgeID].reverse();
		
		// update edgeMap (if set)
		// TODO: check for necessaty
		if (gf_isset(this.edgeMap[source]))
		{
			for (var e in this.edgeMap[source])
			{
				if (this.edgeMap[source][e].edge == edgeID)
				{
					this.edgeMap[source][e].type	= this.edgeMap[source][e].type == "out" ? "in" : "out";
				}
			}
		}
		
		if (gf_isset(this.edgeMap[target]))
		{
			for (var e in this.edgeMap[target])
			{
				if (this.edgeMap[target][e].edge == edgeID)
				{
					this.edgeMap[target][e].type	= this.edgeMap[target][e].type == "out" ? "in" : "out";
				}
			}
		}
	}
};

LinearTimeLayout.prototype.setPos = function (id, x, y)
{
	var node	= this.nodes[id];
	if (node.x == 0 && node.y == 0)
	{
		this.nodes[id].x	= x;
		this.nodes[id].y	= y;
	}
};

LinearTimeLayout.prototype.setRenderObjects = function (nodes, edges)
{
	this.renderObjects	= {"nodes": nodes, "edges": edges};
};

LinearTimeLayout.prototype.sortByX = function (elem1, elem2)
{
	if (gf_isset(elem1.x, elem2.x))
	{
		if (elem1.x < elem2.x)
		{
			return -1;
		}
		else if (elem1.x > elem2.x)
		{
			return 1;
		}
	}
	
	return 0;
};

LinearTimeLayout.prototype.sortByY = function (elem1, elem2)
{
	if (gf_isset(elem1.y, elem2.y))
	{
		if (elem1.y < elem2.y)
		{
			return -1;
		}
		else if (elem1.y > elem2.y)
		{
			return 1;
		}
	}
	
	return 0;
};

LinearTimeLayout.prototype.source = function (edgeID, asNode)
{
	if (gf_isset(asNode) && asNode == true)
	{
		if (gf_isset(this.edges[edgeID]))
			return this.nodes[this.edges[edgeID].source];
			
		return null;		
	}
	
	if (gf_isset(this.edges[edgeID]))
		return this.edges[edgeID].source;
		
	return 0;
};

LinearTimeLayout.prototype.target = function (edgeID, asNode)
{
	if (gf_isset(asNode) && asNode == true)
	{
		if (gf_isset(this.edges[edgeID]))
			return this.nodes[this.edges[edgeID].target];
			
		return null;		
	}
	
	if (gf_isset(this.edges[edgeID]))
		return this.edges[edgeID].target;
		
	return 0;
};

// added param type
LinearTimeLayout.prototype.topologySort = function (fragment, nodeID, type)
{
	// 1) Setup
	var inedges	= {};
	var fragmentNodes	= this.getNodes(fragment);
	
	console.log("topo1");
	console.log(fragmentNodes);
	
	var visited	= {};
	for (var nID in fragmentNodes)
	{
		var newNode	= fragmentNodes[nID];
		
		if (newNode instanceof this.RPSTNode)
			newNode	= newNode.getExit();
		
		if (gf_isset(visited[newNode]))
			continue;
			
		visited[newNode]	= true;
			
		if (!gf_isset(inedges[newNode]))
			inedges[newNode]	= 0;

		var outEdges	= this.getInEdges(newNode);
		
		console.log("topo2");
		console.log(outEdges);
		
		for (var edgeID in outEdges)
		{
			edgeID	= outEdges[edgeID].edge;
			if (type[edgeID] != this.edgeTypes.back)
			{	
				inedges[newNode]++;
			}
		}
	}
	
					
	// 2) Topology Sort
	var topology	= [];
	var i			= 0;
	var length		= fragmentNodes.length;
	topology[topology.length]		= nodeID;
	
	// TODO: check for termination
	while (i < length)
	{		
		var newNode	= topology[i];
		i++;
		
		if (!gf_isset(newNode))
			continue;
		
		var outEdges	= this.getOutEdges(newNode);
		for (var edgeID in outEdges)
		{
			edgeID	= outEdges[edgeID].edge;
			if (type[edgeID] != this.edgeTypes.back)
			{
				var target		= this.target(edgeID);
				
				if(!gf_isset(inedges[target]))
					inedges[target]	= 0;
				
				inedges[target]--;
				if (inedges[target] == 0)
				{
					topology[topology.length]	= target;
				}
			}
		}
	}
	
	console.log("topo");
	console.log(topology);
	
	return topology;
};