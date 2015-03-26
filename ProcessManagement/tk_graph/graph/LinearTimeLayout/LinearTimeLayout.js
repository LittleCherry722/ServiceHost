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
 * Main class of the linear time layouting algorithm.
 * 
 * @class LinearTimeLayout
 * @param {String} orientation - Orientation of the graph which. Either "ltr" (left-to-right) or "ttb" (top-to-bottom).
 */
function LinearTimeLayout (orientation)
{
	if (!gf_isset(orientation) || orientation != "ltr")
		orientation	= "ttb";
	
	/**
	 * Store atomic fragments to avoid duplicate computation.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.atomic					= {};
	
	/**
	 * Backedge between sink and source of graph.
	 * @memberof! LinearTimeLayout
	 * @type {Object}
	 */
	this.backedge				= null;
	
	/**
	 * Array containing converging gateways.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.branchingJoin			= {};
	
	/**
	 * Contains IDs of all nodes that are an entry or an exit to one or more fragments (needed for correct computation within unstructured fragments).
	 * @memberof! LinearTimeLayout
	 * @type {Object}
	 */
	this.boundaryNodes			= {"entry": {}, "exit": {}};
	
	/**
	 * Array containing diverging gateways.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.branchingSplit			= {};
	
	/**
	 * Array containing heights of fragments.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.branchHeight			= {};
	
	/**
	 * Array containing widths of fragments.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.branchWidth			= {};
	
	/**
	 * Cache for results of fragments(), getEdges() and getNodes()
	 * @memberof! LinearTimeLayout
	 * @type {Object}
	 */
	this.cache					= {"fragments": {}, "edges": {}, "nodes": {}};
	
	/**
	 * Counter for statistically purposes only
	 * @memberof! LinearTimeLayout
	 * @type {Object}
	 */
	this.count					= {"nodes": 0, "edges": 0, "freeNodes": 0};
	
	/**
	 * Dimensions of drawing area.
	 * Can be set via the setAreaSize() method.
	 * @memberof! LinearTimeLayout
	 * @type {Object}
	 */
	this.drawingArea			= {"width": 0, "height": 0};
	
	/**
	 * Array containing already rendered nodes.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.drawnNodes				= {};
	
	/**
	 * Array containing the edges of the graph.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.edges					= {};
	
	/**
	 * Enumeration of available edge types.
	 * @memberof! LinearTimeLayout
	 * @type {Object}
	 * @const
	 */
	this.edgeTypes				= {"tree": "Treeedge", "back": "Backedge", "forward": "Forwardedge", "cross": "Crossedge"};
	
	/**
	 * X ordinates of edge bends.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.edgex					= {};
	
	/**
	 * Y ordinates of edge bends.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.edgey					= {};
	
	/**
	 * Array mapping entry nodes to fragments.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.entryMap				= {};
	
	/**
	 * Array mapping exit nodes to fragments.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.exitMap				= {};
	
	/**
	 * Additional edges introduced during the preprocessing step.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.extraEdges				= {};
	
	/**
	 * Incidence list containing edges incoming to nodes, stored per node.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.incidenceIn			= {};
	
	/**
	 * Incidence list containing edges outgoing of nodes, stored per node.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.incidenceOut			= {};
	
	/**
	 * Original incoming edges, stored per node.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.inEdges				= {};
	
	/**
	 * Position of left most node within the graph. Starting point for drawing free nodes.
	 * @memberof! LinearTimeLayout
	 * @type {int}
	 */
	this.leftMost				= null;
	
	/**
	 * manual position changes (can be set via the setManualChanges() method)
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.manualx				= {};
	
	/**
	 * manual position changes (can be set via the setManualChanges() method)
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.manualy				= {};
	
	/**
	 * Array mapping normalized edges to original edges.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.ne2oe					= {};
	
	/**
	 * Array mapping normalized nodes to original nodes.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.nn2on					= {};
	
	/**
	 * Nodes of the graph.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.nodes					= {};
	
	/**
	 * X ordinates of nodes.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.nodex					= {};
	
	/**
	 * Y ordinates of nodes.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.nodey					= {};
	
	/**
	 * Normalized graph.
	 * @memberof! LinearTimeLayout
	 * @type {Object}
	 */
	this.normGraph				= null;
	
	/**
	 * Array mapping original nodes to normalized nodes.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.on2nn					= {};
	
	/**
	 * Graph orientation: ltr (left-to-right) or ttb (top-to-bottom)
	 * @memberof! LinearTimeLayout
	 * @type {String}
	 */
	this.orientation			= orientation;
	
	/**
	 * Original outgoing edges, stored per node.
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.outEdges				= {};
	
	/**
	 * Planarity checker: block stack
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.planB					= null;
	
	/**
	 * Planarity checker: points to last vertex on path
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.planF					= null;
	
	/**
	 * Planarity checker: first unused location in planStack
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.planFree				= null;
	
	/**
	 * Planarity checker: pointer to stack
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.planNext				= null;
	
	/**
	 * Planarity checker: number of current path
	 * @memberof! LinearTimeLayout
	 * @type {int}
	 */
	this.planP					= null;
	
	/**
	 * Planarity checker: maps nodes to paths
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.planPath				= null;
	
	/**
	 * Planarity checker: switching variable
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.planS					= null;
	
	/**
	 * Planarity checker: stack
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.planStack				= null;
	
	/**
	 * Switch to turn the rendering (@drawNode / drawEdges) on or off.
	 * Set to true when computed layout should also be drawn.
	 *  
	 * @memberof! LinearTimeLayout
	 * @type {boolean}
	 */
	this.renderElements			= true;
	
	/**
	 * Arrays containing the rendered elements.
	 * @memberof! LinearTimeLayout
	 * @type {Object}
	 */
	this.renderObjects			= {"nodes": {}, "edges": {}};
	
	/**
	 * decomposed RPST
	 * @memberof! LinearTimeLayout
	 * @type {Object}
	 */
	this.rpst					= null;
	
	/**
	 * Grid distances (x and y) and aesthetical space.
	 * Can be set via the setSpaces() method.
	 * @memberof! LinearTimeLayout
	 * @type {Object}
	 */
	this.spaces					= {x: 0, y: 0, a: 0};
	
	/**
	 * Temporary list containing edges incoming to nodes (limited to current fragment).
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.temporaryIncidenceIn	= {};
	
	/**
	 * Temporary list containing edges outgoing of nodes (limited to current fragment).
	 * @memberof! LinearTimeLayout
	 * @type {Array}
	 */
	this.temporaryIncidenceOut	= {};
}

/*
 * LinearTimeLayout Methods
 */

/**
 * Add an edge to the graph.
 * 
 * @memberof! LinearTimeLayout
 * @param {String} id - ID of the edge.
 * @param {String} source - ID of the start node.
 * @param {String} target - ID of end node.
 * @param {Object} edgeData - Additional data (style information, text information, ...) which is set via the S-BPM Groupware.
 * @returns {void}
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

/**
 * Add a node to the graph.
 * 
 * @memberof! LinearTimeLayout
 * @param {String} id - ID of the node.
 * @param {Object} nodeData - Additional information (assigned text, style information, type of the node, ...).
 * @returns {String} ID of the node.
 */
LinearTimeLayout.prototype.addNode = function (id, nodeData)
{
	this.inEdges[id]	= [];
	this.outEdges[id]	= [];
	this.nodes[id]		= new this.Node(id, nodeData);
	
	return id;
};

/**
 * Calculate the x and y distances of the grid based on the heights and widths of node elements and labels assigned to edges.
 * An additional aesthetic space is added to both distances.
 * The calculated distances are stored in the spaces.x and spaces.y attributes.
 * 
 * @memberof! LinearTimeLayout
 * @returns {void}
 */
LinearTimeLayout.prototype.calculateSpaces = function ()
{
	// spaces.x and spaces.y can be preset by calling the setSpaces() method
	
	// Read width, height of all node labels and increase spaces.x and spaces.y accordingly
	for (var n in this.nodes)
	{
		var node		= this.nodes[n];
		this.spaces.x	= Math.max(node.getWidth(), this.spaces.x);
		this.spaces.y	= Math.max(node.getHeight(), this.spaces.y);
		
		node			= node.id;
		
		// Set branchingSplit if node has at least two outgoing edges
		var outEdges	= this.getOutEdges(node);
		if (outEdges.length > 1)
		{
			this.branchingSplit[node]	= true;
		}
		
		// Set branchingJoin if node has at least two incoming edges
		var inEdges		= this.getInEdges(node);
		if (inEdges.length > 1)
		{
			this.branchingJoin[node]	= true;
		}
	}
	
	// Read width and height of all edge labels and increase spacex and spacey accordingly
	for (var e in this.edges)
	{
		this.spaces.x	= Math.max(this.edges[e].getWidth(), this.spaces.x);
		this.spaces.y	= Math.max(this.edges[e].getHeight(), this.spaces.y);
	}
	
	// add some aesthetical space
	this.spaces.x	+= this.spaces.a;
	this.spaces.y	+= this.spaces.a;
};

/**
 * Compacts the resulting layout.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - A fragment.
 * @returns {void}
 */
LinearTimeLayout.prototype.compactLayout = function (f)
{
	// TODO: left open for further research
};

/**
 * Computes the dimensions of an unstructured fragment.
 * The dimensions are stored in the global arrays branchHeight and branchWidth.
 * Implementation based on ComputeBranchDimensions function of Gschwind et al.
 * 
 * @memberof! LinearTimeLayout
 * @param {String} nodeID - ID of a node or fragment.
 * @param {Array} parent - Array with IDs of parent node (calculated by lpstree method)
 * @returns {void}
 */
LinearTimeLayout.prototype.computeBranchDimensions = function (nodeID, parent)
{
	var height	= 0;
	var width	= 0;
	
	var outEdges	= this.getOutEdges(nodeID);
	for (var edgeID in outEdges)
	{
		edgeID	= outEdges[edgeID];
		
		var newHeight	= 0;
		var newWidth	= 0;
		
		// recursively calculate dimenions of child fragments / nodes
		var targetNode	= this.target(edgeID);
		if (gf_isset(parent[targetNode]) && parent[targetNode] == this.source(edgeID))
		{
			this.computeBranchDimensions(targetNode, parent);
			
			newHeight	= this.branchHeight[targetNode];
			newWidth	= this.branchWidth[targetNode];
		}
		
		if (this.dirLtr())
		{
			height	= height + newHeight;
			width	= Math.max(width, newWidth);
		}
		else
		{
			height	= Math.max(height, newHeight);
			width	= width + newWidth;
		}
	}
	
	// calculate dimension based on dimenions of children and on graph orientation
	if (this.dirLtr())
	{
		height	= Math.max(this.spaces.y, height);
		width	= this.spaces.x + width;
	}
	else
	{
		height	= this.spaces.y + height;
		width	= Math.max(this.spaces.x, width);
	}
	
	// for structured fragments (sequences, branches, loops) the dimensions have already be calculated in the corresponding function
	var isStructured	= this.getF(nodeID).hasOwnProperty("type") && this.getF(nodeID).type != "trivial" && this.getF(nodeID).type != "rigid";
	if (gf_isset(this.branchHeight[nodeID]) && isStructured)
	{
		height	= Math.max(height, this.branchHeight[nodeID]);
	}
	
	if (gf_isset(this.branchWidth[nodeID]) && isStructured)
	{
		width	= Math.max(width, this.branchWidth[nodeID]);
	}
	
	this.branchHeight[nodeID]	= height;
	this.branchWidth[nodeID]	= width;
};

/**
 * Recursively creates maps for fragmentEntry -> fragment and fragmentExit -> fragment.
 * fragmentEntry, fragmentExit and fragment are IDs
 * fragment IDs are prefixed with f:
 * Maps are stored in global entryMap and exitMap arrays
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - Fragment to process.
 * @returns {void}
 */
LinearTimeLayout.prototype.createBoundaryNodesMap = function (f)
{
	var children	= this.rpst.getChildren2(f).children;
	for (var cf in children)
	{
		var child	= children[cf];
		this.createBoundaryNodesMap(child);
	}
	
	if (f.type != "trivial" && f.type != "polygon")
	{
		var entry	= this.entryNode(f);
		var exit	= this.exitNode(f);
		
		this.entryMap[entry]	= f.id;
		this.exitMap[exit]		= f.id;
	}
};

/**
 * Computes the PST / RPST (Refined Process Structure Tree).
 * 
 * @memberof! LinearTimeLayout
 * @see LinearTimeLayout.PST
 * @returns {Object} PST
 */
LinearTimeLayout.prototype.decomposePST = function ()
{
	// decompose the resulting process model into SESE fragments (Single-Entry Single-Exit)
	var pst	= new this.PST(this);	
	return pst.decompose();
};

/**
 * Returns true if graph is oriented left-to-right; false otherwise.
 * 
 * @memberof! LinearTimeLayout
 * @returns {boolean} True if graph is oriented left-to-right.
 */
LinearTimeLayout.prototype.dirLtr = function ()
{
	return this.orientation == "ltr";
};

/**
 * Main drawing function which calls all other functions to render edges or nodes.
 * 
 * @memberof! LinearTimeLayout
 * @returns {void}
 */
LinearTimeLayout.prototype.draw = function ()
{
	// Set start position of graph
	var x	= this.dirLtr()	? 50	: Math.round(this.drawingArea.width / 2);
	var y	= this.dirLtr()	? Math.round(this.drawingArea.height / 2)	: 50;
	
	// main case when a RPST has been computed
	if (this.rpst != null)
	{
		// Set root fragment of calculated RPST as starting point
		var root	= this.rpst.getRoot();
		
		this.drawFragment(root, x, y);
		this.drawEdges();
		this.drawFreeNodes(y);
	}
	
	// when no RPST was computed only free nodes exist
	else
	{
		this.leftMost	= x;
		this.drawFreeNodes(y);
	}
};

/**
 * Renders all edges of the graph.
 * During the routing of the edges the actual positions and dimensions of the nodes is taken into account.
 * 
 * @memberof! LinearTimeLayout
 * @returns {void}
 */
LinearTimeLayout.prototype.drawEdges = function ()
{
	if (!gf_isset(this.renderObjects.edges))
		this.renderObjects.edges	= {};
		
	for (var e in this.edges)
	{
		var edge	= this.edges[e];
		var source	= this.source(edge);
		var target	= this.target(edge);
		
		if (source == "##uSRC##" || target == "##uSNK##")
		{
			// Ignore edges that are inserted during the PreProcess phase
		}
		else
		{
			var bend1	= null;
			var bend2	= null;
			var label	= null;
			
			var src		= this.source(e);
			var tgt		= this.target(e);
			
			// Re-reverse edges
			if (edge.reversed)
			{
				var bak	= src;
					src	= tgt;
					tgt	= bak;
			}
			
			var xs		= this.nodex[src];
			var ys		= this.nodey[src];
			var xt		= this.nodex[tgt];
			var yt		= this.nodey[tgt];
			
			var startH			= "center";
			var startV			= "center";
			var endH			= "center";
			var endV			= "center";
			
			if (ys == yt && gf_isset(this.edgey[edge.id]) && this.edgey[edge.id] != 0)
			{
				// Loop edge - LTR case
				var yb	= ys + this.edgey[edge.id];
				bend1	= {x: xs, y: yb};
				bend2	= {x: xt, y: yb};
				
				if (this.edgey[edge.id] > 0)
				{
					// Update start and end of edge (bottom of src and tgt)
					ys	+= Math.ceil(this.getHeight(src) / 2);
					yt	+= Math.ceil(this.getHeight(tgt) / 2);
					startV	= "bottom";
					endV	= "bottom";
				}
				else
				{
					// Update start and end of edge (top of src and tgt)
					ys	-= Math.ceil(this.getHeight(src) / 2);
					yt	-= Math.ceil(this.getHeight(tgt) / 2);
					startV	= "top";
					endV	= "top";	
				}
				
				// Position label in the center of the edge
				var xl	= xs + Math.ceil((xt - xs) / 2);
				label	= {x: xl, y: yb};
			}
			
			else if (xs == xt && gf_isset(this.edgex[edge.id]) && this.edgex[edge.id] != 0)
			{
				// Loop edge - TTB case
				var xb	= xs + this.edgex[edge.id];
				bend1	= {x: xb, y: ys};
				bend2	= {x: xb, y: yt};
				
				if (this.edgex[edge.id] > 0)
				{
					// Update start and end of edge (right of src and tgt)
					xs	+= Math.ceil(this.getWidth(src) / 2);
					xt	+= Math.ceil(this.getWidth(tgt) / 2);
					startH	= "right";
					endH	= "right";
				}
				else
				{
					// Update start and end of edge (left of src and tgt)
					xs	-= Math.ceil(this.getWidth(src) / 2);
					xt	-= Math.ceil(this.getWidth(tgt) / 2);
					startH	= "left";
					endH	= "left";
				}
				
				// Position label in the center of the edge
				var yl	= ys + Math.ceil((yt - ys) / 2);
				label	= {x: xb, y: yl};
			}
			
			else if (ys == yt)
			{
				// Straight edge - LTR case
				if (xs < xt)
				{
					xs	+= Math.ceil(this.getWidth(src) / 2);	// right of src
					xt	-= Math.ceil(this.getWidth(tgt) / 2);	// left of tgt
					startH	= "right";
					endH	= "left";
				}
				else
				{
					xs	-= Math.ceil(this.getWidth(src) / 2);	// left of src
					xt	+= Math.ceil(this.getWidth(tgt) / 2);	// right of tgt	
					startH	= "left";
					endH	= "right";
				}
				
				// Position label in the center of the edge
				var xl	= xs + Math.ceil((xt - xs) / 2);
				label	= {x: xl, y: ys};
			}
			
			else if (xs == xt)
			{
				// Straight edge - TTB case
				if (ys < yt)
				{
					ys	+= Math.ceil(this.getHeight(src) / 2);	// bottom of src
					yt	-= Math.ceil(this.getHeight(tgt) / 2);	// top of tgt
					startV	= "bottom";
					endV	= "top";
				}
				else
				{
					ys	-= Math.ceil(this.getHeight(src) / 2);	// top of src
					yt	+= Math.ceil(this.getHeight(tgt) / 2);	// bottom of tgt
					startV	= "top";
					endV	= "bottom";	
				}
				
				// Position label in the center of the edge
				var yl	= ys + Math.ceil((yt - ys) / 2);
				label	= {x: xs, y: yl};
			}
			
			else if (this.dirLtr() && gf_isset(this.branchingSplit[src]) && gf_isset(this.branchingJoin[tgt]))
			{
				// Special case crossing edge - LTR case
				if (xs < xt)
				{
					xs	+= Math.ceil(this.getWidth(src) / 2);	// right of src
					xt	-= Math.ceil(this.getWidth(tgt) / 2);	// left of tgt
					startH	= "right";
					endH	= "left";
				}
				else
				{
					xs	-= Math.ceil(this.getWidth(src) / 2);	// left of src
					xt	+= Math.ceil(this.getWidth(tgt) / 2);	// right of tgt	
					startH	= "left";
					endH	= "right";
				}
				
				// Add two bends to give more room for labels
				var xb	= xs + Math.ceil((xt - xs) * 0.4);
				var yb	= ys + Math.ceil((yt - ys) * 0.3);
				bend1	= {x: xb, y: yb};
				
					xb	= xt - Math.ceil((xt - xs) * 0.4);
					yb	= yt - Math.ceil((yt - ys) * 0.3);
				bend2	= {x: xb, y: yb};
				
				// Place label
				if (ys < yt)
				{
					label	= bend1;
				}
				else
				{
					label	= bend2;
				}
			}
			
			else if (!this.dirLtr() && gf_isset(this.branchingSplit[src]) && gf_isset(this.branchingJoin[tgt]))
			{
				// Special case crossing edge - TTB case
				if (ys < yt)
				{
					ys	+= Math.ceil(this.getHeight(src) / 2);	// bottom of src
					yt	-= Math.ceil(this.getHeight(tgt) / 2);	// top of tgt
					startV	= "bottom";
					endV	= "top";
				}
				else
				{
					ys	-= Math.ceil(this.getHeight(src) / 2);	// top of src
					yt	+= Math.ceil(this.getHeight(tgt) / 2);	// bottom of tgt	
					startV	= "top";
					endV	= "bottom";
				}
				
				// Add two bends to give more room for labels
				var xb	= xs + Math.ceil((xt - xs) * 0.3);
				var yb	= ys + Math.ceil((yt - ys) * 0.4);
				bend1	= {x: xb, y: yb};
				
					xb	= xt - Math.ceil((xt - xs) * 0.3);
					yb	= yt - Math.ceil((yt - ys) * 0.4);
				bend2	= {x: xb, y: yb};
				
				// Place label
				if (xs < xt)
				{
					label	= bend1;
				}
				else
				{
					label	= bend2;
				}
			}
			
			else if (this.dirLtr() && gf_isset(this.branchingSplit[src]))
			{
				// First segment diagonal, edge starts at diverging node - LTR case
				if (xs < xt)
				{
					xs	+= Math.ceil(this.getWidth(src) / 2);	// right of src
					xt	-= Math.ceil(this.getWidth(tgt) / 2);	// left of tgt
					startH	= "right";
					endH	= "left";
				}
				else
				{
					xs	-= Math.ceil(this.getWidth(src) / 2);	// left of src
					xt	+= Math.ceil(this.getWidth(tgt) / 2);	// right of tgt	
					startH	= "left";
					endH	= "right";
				}
				
				var xb	= xs + Math.ceil((xt - xs) / 2);
				bend1	= {x: xb, y: yt};
				
				// Position label at the bend
				label	= bend1;
			}
			
			else if (!this.dirLtr() && gf_isset(this.branchingSplit[src]))
			{
				// First segment diagonal, edge starts at diverging node - TTB case
				if (ys < yt)
				{
					ys	+= Math.ceil(this.getHeight(src) / 2);	// bottom of src
					yt	-= Math.ceil(this.getHeight(tgt) / 2);	// top of tgt
					startV	= "bottom";
					endV	= "top";
				}
				else
				{
					ys	-= Math.ceil(this.getHeight(src) / 2);	// top of src
					yt	+= Math.ceil(this.getHeight(tgt) / 2);	// bottom of tgt
					startV	= "top";
					endV	= "bottom";	
				}

				var yb	= ys + Math.ceil((yt - ys) / 2);
				bend1	= {x: xt, y: yb};
				
				// Position label at the bend
				label	= bend1;
			}
			
			else if (this.dirLtr() && gf_isset(this.branchingJoin[tgt]))
			{
				// Last segment diagonal, edge ends at converging node - LTR case
				if (xs < xt)
				{
					xs	+= Math.ceil(this.getWidth(src) / 2);	// right of src
					xt	-= Math.ceil(this.getWidth(tgt) / 2);	// left of tgt
					startH	= "right";
					endH	= "left";
				}
				else
				{
					xs	-= Math.ceil(this.getWidth(src) / 2);	// left of src
					xt	+= Math.ceil(this.getWidth(tgt) / 2);	// right of tgt	
					startH	= "left";
					endH	= "right";
				}
				
				var xb	= xs + Math.ceil((xt - xs) / 2);
				bend1	= {x: xb, y: ys};
				
				// Position label at the bend
				label	= bend1;
			}
			
			else if (!this.dirLtr() && gf_isset(this.branchingJoin[tgt]))
			{
				// Last segment diagonal, edge ends at converging node - TTB case
				if (ys < yt)
				{
					ys	+= Math.ceil(this.getHeight(src) / 2);	// bottom of src
					yt	-= Math.ceil(this.getHeight(tgt) / 2);	// top of tgt
					startV	= "bottom";
					endV	= "top";
				}
				else
				{
					ys	-= Math.ceil(this.getHeight(src) / 2);	// top of src
					yt	+= Math.ceil(this.getHeight(tgt) / 2);	// bottom of tgt
					startV	= "top";
					endV	= "bottom";	
				}

				var yb	= ys + Math.ceil((yt - ys) / 2);
				bend1	= {x: xs, y: yb};
				
				// Position label at the bend
				label	= bend1;
			}
			
			else
			{
				// default case when no other criteria is met
				if (ys < yt)
				{
					ys	+= Math.ceil(this.getHeight(src) / 2);	// bottom of src
					yt	-= Math.ceil(this.getHeight(tgt) / 2);	// top of tgt
					startV	= "bottom";
					endV	= "top";
				}
				else
				{
					ys	-= Math.ceil(this.getHeight(src) / 2);	// top of src
					yt	+= Math.ceil(this.getHeight(tgt) / 2);	// bottom of tgt
					startV	= "top";
					endV	= "bottom";	
				}

				var yb	= ys + Math.ceil((yt - ys) / 2);
				bend1	= {x: xs, y: yb};
				
				// Position label at the bend
				label	= bend1;
			}
			
			var edgeStart	= {x: xs, y: ys};
			var edgeEnd		= {x: xt, y: yt};
			
			// Draw the actual edge by applying edgeStart, edgeEnd, bend1, bend2, label (the GCrenderEdgee is part of the tk_graph library)
			if (this.renderElements == true)
			{
				if (!gf_isset(this.renderObjects.edges[edge.orgId]))
					this.renderObjects.edges[edge.orgId]	= new GCrenderEdge(edge.orgId, edge.edgeData);
					
				this.renderObjects.edges[edge.orgId].setEndPoints(this.idToGW(src), this.idToGW(tgt));
				this.renderObjects.edges[edge.orgId].setPosStart(startH, startV);
				this.renderObjects.edges[edge.orgId].setPosEnd(endH, endV);
				this.renderObjects.edges[edge.orgId].setLTL(label, bend1, bend2);
			}
			
			this.count.edges++;
		}
	}
};

/**
 * Fragments are drawn recursively by rendering each fragment's entry and exit node.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - The fragment to draw.
 * @param {int} x - The x ordinate of the parent fragment.
 * @param {int} y - The y ordinate of the parent fragment.
 * @returns {void}
 */
LinearTimeLayout.prototype.drawFragment = function (f, x, y)
{
	var entry	= this.entryNode(f);
	var exit	= this.exitNode(f);
	
	// correct offset if artificial source node was added during the preprocessing phase
	if (f.id == this.rpst.getRoot().id && entry == "##uSRC##")
	{
		if (this.dirLtr())
		{
			x	-= this.spaces.x;
		}
		else
		{
			y	-= this.spaces.y;
		}
	}
	
	// JS optimization
	if (!gf_isset(this.drawnNodes[entry]) && f.hasOwnProperty("type") && f.type != "rigid")
	{
		this.nodex[entry]	= 0;
		this.nodey[entry]	= 0;
	}

	// Add position of this fragment relative to its parent
	x	+= this.nodex[f.id];
	y	+= this.nodey[f.id];
		
	// draw entry and exit node
	this.drawNode(entry, x, y);
	this.drawNode(exit, x, y);
	
	if (this.isAtomic(f))
	{
		// Ignore as entry and exit already drawn	
	}
	else
	{
		// Draw child fragments
		var children	= this.rpst.getChildren2(f).children;
		for (var cf in children)
		{
			var child	= children[cf];
			
			if (child.type == "trivial")
			{
				entry	= this.entryNode(child);
				exit	= this.exitNode(child);
				
				this.drawNode(entry, x, y);
				this.drawNode(exit, x, y);
			}
			else
			{
				this.drawFragment(child, x, y);
			}
		}
	}
};

/**
 * Draws free nodes which are not part of the actual graph.
 * Only called for top-to-bottom oriented graphs.
 * Nodes are placed at the leftMost position of the graph (computed in the drawNode() method).
 * 
 * @memberof! LinearTimeLayout
 * @param {int} y - y offset for the free nodes
 * @returns {void}
 */
LinearTimeLayout.prototype.drawFreeNodes = function (y)
{
	if (!this.dirLtr())
	{
		// if no leftMost position has been computed (for graphs with only free nodes): start at the center of the drawing area
		if (this.leftMost == null)
		{
			this.leftMost	= 0;
		}
		
		// when a leftMost position is given, move to the left by 3*spaces.x of the leftMost position
		else
		{
			this.leftMost	-= 3 * this.spaces.x;
		}
		
		// draw all free nodes
		for (var n in this.nodes)
		{
			var node	= this.nodes[n];
			
			if (!gf_isset(this.drawnNodes[n]) && n != "##uSRC##" && n != "##uSNK##")
			{
				this.nodex[n]	= 0;
				this.nodey[n]	= 0;
				
				this.drawNode(n, this.leftMost, y);
				
				// add some space below each node
				y	+= this.spaces.y;
			
				this.count.freeNodes++;
			}
		}
	}
};

/**
 * Draws a single node based on the position of the corresponding fragment, the position determined during the layout computation (nodex, nodey) and the manual repositioning (manualx, manualy).
 * 
 * @memberof! LinearTimeLayout
 * @param {String} n - ID of the node to draw.
 * @param {int} x - x ordinate of the corresponding fragment
 * @param {int} y - y ordinate of the corresponding fragment
 * @returns {void}
 */
LinearTimeLayout.prototype.drawNode = function (n, x, y)
{
	// To avoid duplicate calls
	if (!gf_isset(this.drawnNodes[n]))
	{	
		this.drawnNodes[n]	= true;
			
		// Ignore virtual nodes that are inserted during the PreProcess phase
		if (n == "##uSRC##" || n == "##uSNK##")
		{
			
		}
		else
		{
			if (!gf_isset(this.manualx[n]))
			{
				this.manualx[n]	= 0;
			}
			
			if (!gf_isset(this.manualy[n]))
			{
				this.manualy[n]	= 0;
			}
			
			// Calculate absolute position of node depending on given offset and on offset relative to framgent as well as manual change
			this.nodex[n]	= x + this.nodex[n] + this.manualx[n];
			this.nodey[n]	= y + this.nodey[n] + this.manualy[n];
			
			// Get position of left most node for drawing free nodes
			if (!this.dirLtr() && (this.nodex[n] < this.leftMost || this.leftMost == null))
			{
				this.leftMost	= this.nodex[n];
			}
			
			// At this point the node and its position is passed to the renderer (the GCrenderNode is part of the tk_graph library)
			if (this.renderElements == true)
			{
				var nid	= this.idToGW(n);
				
				if (!gf_isset(this.renderObjects.nodes))
					this.renderObjects.nodes	= {};
					
				if (!gf_isset(this.renderObjects.nodes[nid]))
					this.renderObjects.nodes[nid]	= new GCrenderNode(nid, this.nodes[n].node);
		
				this.renderObjects.nodes[nid].setPosition(Math.floor(this.nodex[n]), Math.floor(this.nodey[n]));
			}
			
			this.count.nodes++;
		}
		
	}
};

/**
 * Get the entry node of the given fragment.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - A fragment or the ID of a fragment.
 * @returns {String} ID of the entry node.
 */
LinearTimeLayout.prototype.entryNode = function (f)
{
	// if only ID is given
	if (typeof f == "string")
	{
		// f is node and no fragment -> return the ID of the node itself
		if (f.substr(0,3) != "f:n")
		{
			return f;
		}
		
		// get fragment corresponding to ID
		f	= this.getF(f);
	}
	
	// if given f is a node and no fragment -> return the ID of the node
	else if (f instanceof this.Node)
	{
		return f.id;
	}
	
	// get the entry node of the fragment from the PST
	return f.getEntry();
};

/**
 * Get the exit node of the given fragment.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - A fragment or the ID of a fragment.
 * @returns {String} ID of the exit node.
 */
LinearTimeLayout.prototype.exitNode = function (f)
{
	// if only ID is given
	if (typeof f == "string")
	{
		// f is node and no fragment -> return the ID of the node itself
		if (f.substr(0,3) != "f:n")
		{
			return f;
		}
		
		// get fragment corresponding to ID
		f	= this.getF(f);
	}
	
	// if given f is a node and no fragment -> return the ID of the node
	else if (f instanceof this.Node)
	{
		return f.id;
	}
	
	// get the exit node of the fragment from the PST
	return f.getExit();
};

/**
 * Get the fragments and nodes contained in a fragment.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - A fragment.
 * @returns {Array} All fragments and nodes of the fragment.
 */
LinearTimeLayout.prototype.fragments = function (f)
{
	var fragments	= [];
	if (f instanceof this.RPSTNode)
	{
		// if fragments list not cached for this fragment
		if (!gf_isset(this.cache.fragments[f.id]))
		{
			// fragments = children from RPST ....
			fragments	= this.rpst.getChildren2(f).children;
			
			// + free nodes
			var freeNodes	= f.getFragment().getNodes();
			for (var fn in freeNodes)
			{
				fragments.push(this.nodes[fn]);
			}
			
			// cache the fragments list for this fragment
			this.cache.fragments[f.id]	= fragments;
		}
		
		// load cached version for this fragment
		else
		{
			fragments	= this.cache.fragments[f.id];
		}
	}
	return fragments;
};

/**
 * Get edges within a fragment.
 * Also computes temporary incidence lists limiting the edges in the incidence lists to the edges of the fragment.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - A fragment.
 * @returns {Array} Array of edges within the fragment.
 */
LinearTimeLayout.prototype.getEdges = function (f)
{
	// get nodes contained within the fragment in order to correctly load the edges of the fragment
	var nodes			= this.getNodes(f);
	var fragmentNodes	= {};
	
	// create temporary incidence lists
	this.temporaryIncidenceIn		= {};
	this.temporaryIncidenceOut		= {};
	for (var n in nodes)
	{
		var node	= nodes[n];
		if (node instanceof this.RPSTNode)
		{
			node	= node.id;
		}
		fragmentNodes[node]	= true;
		this.temporaryIncidenceIn[node]		= new Array();
		this.temporaryIncidenceOut[node]	= new Array();
	}
	
	
	var edges	= new Array();
	var visited	= {};
	var queue	= new Array();
	
	var entry	= this.entryNode(f);
	var exit	= this.exitNode(f);
	
	visited[entry]	= true;
	queue.push(entry);
	
	// cycle through the queue and collect all edges
	while (queue.length > 0)
	{
		var node		= queue.pop();
		visited[node]	= true;
		var outEdges	= this.getOutEdges(node, true);
		
		for (var e in outEdges)
		{
			var edge	= outEdges[e];
			var tgt		= this.target(edge);
			
			// JS workaround
			if (exit == this.entryNode(tgt) && tgt != this.entryNode(tgt))
			{
				tgt		= this.entryNode(tgt);
				edge	= {"src": node, "tgt": tgt, "temporary": true, "orgId": edge.orgId};
			}
			// end JS workaround
			
			if (gf_isset(fragmentNodes[tgt]))
			{
				edges.push(edge);
					
				this.temporaryIncidenceIn[tgt].push(edge);
				this.temporaryIncidenceOut[node].push(edge);
				
				if (!gf_isset(visited[tgt]))
				{
					queue.push(tgt);
					visited[tgt]	= true;
				}
			}
		}
	}

	return edges;
};

/**
 * Auxiliary function to retrieve fragment from RPST.
 * 
 * @memberof! LinearTimeLayout
 * @param {String} f - ID of a fragment.
 * @returns {Object} The fragment with the given ID.
 */
LinearTimeLayout.prototype.getF = function (f)
{
	if (gf_isset(this.rpst.nodes[f]))
		return this.rpst.nodes[f];
	
	return f;
};

/**
 * Get height of a node.
 * 
 * @memberof! LinearTimeLayout
 * @param {String} nodeID - ID of a node.
 * @returns {int} Height of the node.
 */
LinearTimeLayout.prototype.getHeight = function (nodeID)
{
	if (gf_isset(this.nodes[nodeID]))
		return this.nodes[nodeID].getHeight();
		
	// fallback: return spaces.y
	return this.spaces.y;
};

/**
 * Get edges that are incoming to a node.
 * 
 * @memberof! LinearTimeLayout
 * @param {String} nodeID - ID of a node.
 * @returns {Array} List of incoming edges.
 */
LinearTimeLayout.prototype.getInEdges = function (nodeID)
{
	// use temporary incidence list if available (limites edges to the current fragment)
	if (gf_isset(this.temporaryIncidenceIn[nodeID]))
	{
		return this.temporaryIncidenceIn[nodeID];
	}
	
	if (!gf_isset(this.incidenceIn[nodeID]))
		this.incidenceIn[nodeID]	= new Array();
	
	// use precomputed incidency list	
	return this.incidenceIn[nodeID];
};

/**
 * Get nodes within fragment.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - A fragment.
 * @returns {Array} List of nodes.
 */
LinearTimeLayout.prototype.getNodes = function (f)
{
	// if list not cached	
	if (!gf_isset(this.cache.nodes[f.id]))
	{
		var children	= this.rpst.getChildren2(f);
		var entry		= f.getEntry();
		var exit		= f.getExit();
		var freeNodes	= {};
		var nodes		= new Array();
		
		// cycle through all child fragments and push entry and exit nodes of fragment to list as well as all non trivial fragments
		for (var c in children.children)
		{
			var child	= children.children[c];
			var cEntry	= child.getEntry();
			var cExit	= child.getExit();		
			
			if (!gf_isset(freeNodes[cEntry]))
				freeNodes[cEntry]	= true;
				
			if (!gf_isset(freeNodes[cExit]))
				freeNodes[cExit]	= true;
			
			if (child.type != "trivial")
			{
				nodes.push(child);
			}
		}
		
		freeNodes[entry]	= true;
		freeNodes[exit]		= true;
		
		for (var f in freeNodes)
		{
			if (freeNodes[f] == true)
				nodes.push(f);
		}
		
		// add to cache
		this.cache.nodes[f.id]	= nodes;
	}
	
	return this.cache.nodes[f.id];
};

/**
 * Get edges outgoing of a node.
 * 
 * @memberof! LinearTimeLayout
 * @param {String} nodeID - ID of a node.
 * @param {boolean} [org] - If set the original incidence list is used even if a temporary incidence list exists. Only used in getEdges().  
 * @returns {Array} List of incoming edges.
 */
LinearTimeLayout.prototype.getOutEdges = function (nodeID, org)
{
	// use temporary incidence list if available (limites edges to the current fragment)
	if (!gf_isset(org) && gf_isset(this.temporaryIncidenceOut[nodeID]))
	{
		return this.temporaryIncidenceOut[nodeID];
	}
	
	if (!gf_isset(this.incidenceOut[nodeID]))
		this.incidenceOut[nodeID]	= new Array();
		
	// use precomputed incidency list	
	return this.incidenceOut[nodeID];
};

/**
 * Get width of a node.
 * 
 * @memberof! LinearTimeLayout
 * @param {String} nodeID - ID of a node.
 * @returns {int} Width of the node.
 */
LinearTimeLayout.prototype.getWidth = function (nodeID)
{
	if (gf_isset(this.nodes[nodeID]))
		return this.nodes[nodeID].getWidth();
		
	// fallback: return spaces.x
	return this.spaces.x;
};

/**
 * Identifies edge types (Backward, Forward, Cross, Tree edges) in unstructured fragments.
 * A DFS implementation.
 * 
 * @memberof! LinearTimeLayout
 * @param {String} nodeID - ID of the node to start the DFS.
 * @param {Array} type - Array storing the edge types.
 * @param {Array} discovered - Array containing already visited nodes.
 * @param {Array} finished - Array containing finished nodes.
 * @param {int} t - Counter
 * @returns {int} t
 */
LinearTimeLayout.prototype.identifyEdgeTypes = function (nodeID, type, discovered, finished, t)
{		
	t++;
	discovered[nodeID]	= t;
	
	var outEdges	= this.getOutEdges(nodeID);
	
	// cycle through all outgoing edges of current node
	for (var edgeID in outEdges)
	{		
		var edge	= outEdges[edgeID];
		var newNode	= this.target(edge);
		
		// DFS: follow edge to next node; edge is tree edge
		if (!gf_isset(discovered[newNode]))
		{
			type[edge.orgId]	= this.edgeTypes.tree;
			t					= this.identifyEdgeTypes(newNode, type, discovered, finished, t);
		}
		
		// backedges
		else if (!gf_isset(finished[newNode]))
		{
			type[edge.orgId]	= this.edgeTypes.back;
		}
		
		// forward edges
		else if (discovered[newNode] > discovered[nodeID])
		{
			type[edge.orgId]	= this.edgeTypes.forward;
		}
		
		// crossing edges
		else
		{
			type[edge.orgId]	= this.edgeTypes.cross;
		}
	}
	
	t++;
	finished[nodeID]	= t;
	return t;
};

/**
 * Convert node ID to groupware conform ID.
 * @param {String} n - ID of a node.
 * @returns {String} Modified ID.
 */
LinearTimeLayout.prototype.idToGW = function (n)
{
	if (n.substr(0,1) == "n")
	{
		n	= n.substr(1);
	}
	
	return n;
};

/**
 * Checks if a fragment is atomic, i.e. a single node.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - A fragment.
 * @returns {boolean} True if the fragment is a single node, false otherwise.
 */
LinearTimeLayout.prototype.isAtomic = function (f)
{
	return (f instanceof this.Node);
};

/**
 * Checks if a fragment is a branching.
 * Branching fragments are bonds within the RPST without loop edges.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - A fragment.
 * @returns {boolean} True if the fragment is a branching, false otherwise.
 */
LinearTimeLayout.prototype.isBranching = function (f)
{
	// branchings are bond fragments within the RPST
	if (f.type == "bond")
	{
		// check outgoing edges for loop edges (-> structured loop)
		var outEdges	= this.getOutEdges(this.exitNode(f));
		for (var e in outEdges)
		{
			var edge	= outEdges[e];
			if (this.target(edge) == f.id)
			{
				return false;
			}
		}
		
		return true;
	}
	
	return false;
};

/**
 * Checks if a fragment is a structured loop.
 * Structured loop fragments are bonds within the RPST with loop edges.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - A fragment.
 * @returns {boolean} True if the fragment is a structured loop, false otherwise.
 */
LinearTimeLayout.prototype.isLoop = function (f)
{
	// structured loops are bond fragments within the RPST
	if (f.type == "bond")
	{		
		return true;
	}
	
	return false;
};

/**
 * Auxiliary function checking a given fragment ID results in a sequence fragment.
 * 
 * @memberof! LinearTimeLayout
 * @param {String} f - ID of a fragment or node.
 * @returns {boolean} True if the given ID belongs to a fragment and the corresponding fragment is a sequence fragment.
 */
LinearTimeLayout.prototype.isS = function (f)
{
	f	= this.getF(f);
	
	if (typeof f != "string" && f.hasOwnProperty("type") && f.type == "polygon")
		return true;
	
	return false;
};

/**
 * Checks if a fragment is a structured sequence.
 * Sequence fragments are polygons within the RPST.
 * A second type are trivial fragments within the RPST which are single edges, i.e. a sequence of two nodes.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - A fragment.
 * @returns {boolean} True if the fragment is a sequence, false otherwise.
 */
LinearTimeLayout.prototype.isSequence = function (f)
{
	// sequences are polygon fragments or trivial fragments within the RPST
	if (f.type == "polygon" || f.type == "trivial")
	{
		return true;
	}
	return false;
};

/**
 * Computes the layout of the given graph in linear time.
 * 
 * @memberof! LinearTimeLayout
 * @returns {void}
 */
LinearTimeLayout.prototype.layout = function ()
{
	
	// additional step: count edges
	var edgeCount	= 0;
	for (var e in this.edges)
	{
		edgeCount++;
		if (edgeCount > 0)
			break;
	}
	
	// addition: ignore single nodes
	if (edgeCount > 0)
	{
		// preprocess and normalize graph
		this.normGraph		= this.preprocess();
		
		// compute RPST
		this.rpst			= this.decomposePST();
		
		// update incidence lists in order to cope fragments
		this.updateIncidenceLists(this.rpst.getRoot());
		
		// compute layout starting with root of RPST
		this.layoutFragment(this.rpst.getRoot());
	}
	
	// render computed layout
	this.draw();
};

/**
 * Lays out an atomic fragment.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - The fragment to lay out.
 * @returns {void}
 */
LinearTimeLayout.prototype.layoutAtomic = function (f)
{
	// no special layout required: only set branchHeight and branchWidth
	if (!gf_isset(this.atomic[f.id]))
	{
		this.branchHeight[f.id]	= this.spaces.y;
		this.branchWidth[f.id]	= this.spaces.x;
		this.nodex[f.id]		= 0;
		this.nodey[f.id]		= 0;
		
		this.atomic[f.id]		= true;
	}
};

/**
 * Lays out a branching fragment.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - The fragment to lay out.
 * @returns {void}
 */
LinearTimeLayout.prototype.layoutBranching = function (f)
{
	var sumHeight	= 0;
	var sumWidth	= 0;
	var maxHeight	= 0;
	var maxWidth	= 0;
	var x			= 0;
	var y			= 0;
	
	var entry		= this.entryNode(f);
	var exit		= this.exitNode(f);
	
	// Calculate width and height of the fragment
	var children	= this.fragments(f);
	for (var c in children)
	{
		var child	= children[c];
		var cf		= child.id;
		if (this.isAtomic(child))
		{
			continue;
		}
		
		var height	= this.spaces.y;
		var width	= this.spaces.x;
		if (!child.hasOwnProperty("type") || child.type != "trivial")
		{
			height	= this.branchHeight[cf];
			width	= this.branchWidth[cf];
		}
		sumHeight	+= height;
		sumWidth	+= width;
		maxHeight	= Math.max(maxHeight, height);
		maxWidth	= Math.max(maxWidth, width);
	}
	
	// Calculate the left most / top most position of the branching fragments
	if (this.dirLtr())
	{
		x	= 0;
		y	= 0 - Math.ceil(sumHeight / 2);
	}
	else
	{
		x	= 0 - Math.ceil(sumWidth / 2);
		y	= 0;
	}
	
	// Update positions of child fragments
	for (var c in children)
	{
		var child	= children[c];
		var cf		= child.id;
		if (this.isAtomic(child))
		{
			continue;
		}
		
		var height	= this.spaces.y;
		var width	= this.spaces.x;
		if (!child.hasOwnProperty("type") || child.type != "trivial")
		{
			height	= this.branchHeight[cf];
			width	= this.branchWidth[cf];
		}
		
		height	= Math.ceil(height / 2);
		width	= Math.ceil(width / 2);
		
		if (this.dirLtr())
		{
			y	+= height;
		}
		else
		{
			x	+= width;
		}
		
		this.nodex[cf]	= x;
		this.nodey[cf]	= y;
		
		var cEntry	= this.entryNode(cf);
		var cExit	= this.exitNode(cf);
		
		// Set position of bends of edges and update x / y for the next fragment
		if (this.dirLtr())
		{
			y	+= height;
		}
		else
		{
			x	+= width;	
		}
	}
	
	// Update position of entry and exit node and update fragment's size
	this.nodex[entry]	= 0;
	this.nodey[entry]	= 0;
	
	if (this.dirLtr())
	{
		this.nodex[exit]		= x + maxWidth;
		this.nodey[exit]		= 0;
		
		// JS workaround
		if (!gf_isset(this.entryMap[exit]))
		{
			maxWidth	+= this.spaces.x;
		}
		this.branchHeight[f.id]	= sumHeight;
		this.branchWidth[f.id]	= maxWidth;
	}
	else
	{
		this.nodex[exit]		= 0;
		this.nodey[exit]		= y + maxHeight;
		
		// JS workaround
		if (!gf_isset(this.entryMap[exit]))
		{
			maxHeight	+= this.spaces.y;
		}
		this.branchHeight[f.id]	= maxHeight;
		this.branchWidth[f.id]	= sumWidth;
	}
};

/**
 * Compute layout of each fragment in a bottp-up manner by starting at the inner most fragments.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - The fragment to process.
 * @returns {void}
 */
LinearTimeLayout.prototype.layoutFragment = function (f)
{
	if (!gf_isset(this.nodex[f.id]))
	{
		this.nodex[f.id]	= 0;
	}
	
	if (!gf_isset(this.nodey[f.id]))
	{
		this.nodey[f.id]	= 0;
	}
	
	// recursive call (bottom-up)
	var nodes	= this.fragments(f);
	for (var n in nodes)
	{
		var node	= nodes[n];
		this.layoutFragment(node);
	}
	
	// workaround for temporary incidence list: reset incidence list
	this.temporaryIncidenceIn	= {};
	this.temporaryIncidenceOut	= {};
	// end workaround
	
	if (this.isAtomic(f))
	{
		this.layoutAtomic(f);
	}
	else if (this.isSequence(f))
	{
		this.layoutSequence(f);
	}
	else if (this.isBranching(f))
	{
		this.layoutBranching(f);
	}
	else if (this.isLoop(f))
	{
		this.layoutLoop(f);
	}
	else
	{
		this.layoutUnstructured(f);
	}
	
	// store entry node and exit node of all structured fragments to avoid update during preliminaryLayout computation
	if (f.hasOwnProperty("type") && (f.type == "bond" || f.type == "polygon"))
	{
		this.boundaryNodes.entry[this.entryNode(f)]	= true;
		this.boundaryNodes.exit[this.exitNode(f)]	= true;
	}
};

/**
 * Lays out a structured loop fragment.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - The fragment to lay out.
 * @returns {void}
 */
LinearTimeLayout.prototype.layoutLoop = function (f)
{
	var node		= this.entryNode(f);
	var maxHeight	= 0;
	var maxWidth	= 0;
	var sumHeight	= 0;
	var sumWidth	= 0;
	
	var x			= 0;
	var y			= 0;
	
	// cycle through all sequence elements
	while (node != null)
	{
		var cf	= this.getF(node);
		
		// compute dimensions
		maxHeight	= Math.max(maxHeight, this.branchHeight[node]);
		maxWidth	= Math.max(maxWidth, this.branchWidth[node]);
		sumHeight	+= this.branchHeight[node];
		sumWidth	+= this.branchWidth[node];
		
		if (!cf.hasOwnProperty("type") || cf.type != "polygon")		// exclude sequence children
		{
			// Update position of child fragment relative to given fragment
			if (this.dirLtr())
			{
				this.nodex[node]	= x;
			}
			else
			{
				this.nodey[node]	= y;
			}
		
		}
		
		// correction for sequence fragments
		else
		{
			sumHeight	-= this.spaces.y;
			sumWidth	-= this.spaces.x;
			x			-= this.spaces.x;
			y			-= this.spaces.y;
		}
		
		x	+= this.branchWidth[node];
		y	+= this.branchHeight[node];
		
		// Get the next node in line in a DFS manner
		var outEdges	= this.getOutEdges(node);
		if (outEdges.length == 1)
		{
			node	= this.target(this.top(outEdges));
		}
		else
		{
			node	= null;
			
			// Set loop edge
			for (var e in outEdges)
			{
				var edge	= outEdges[e];
				if (this.target(edge) == this.entryNode(f) || this.target(edge) == f.id)
				{
					if (this.dirLtr())
					{
						this.edgey[edge.orgId]	= this.spaces.y;
					}
					else
					{
						this.edgex[edge.orgId]	= this.spaces.x;
					}
				}
			}
		}
	}
	
	// update branchingSplit / branchingJoin
	delete this.branchingJoin[this.entryNode(f)];
	delete this.branchingSplit[this.exitNode(f)];
	
	// Update size of fragment
	if (this.dirLtr())
	{
		this.branchHeight[f.id]	= maxHeight + this.spaces.y;
		this.branchWidth[f.id]	= sumWidth;
	}
	else
	{
		this.branchHeight[f.id]	= sumHeight;
		this.branchWidth[f.id]	= maxWidth + this.spaces.x;
	}
};

/**
 * Lays out a sequence fragment.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - The fragment to lay out.
 * @returns {void}
 */
LinearTimeLayout.prototype.layoutSequence = function (f)
{
	// trivial fragments: single edges, simplest form of sequences
	if (f.type == "trivial")
	{
		// only when whole graph consists of only one trivial fragment the fragment has to be laid out
		if (this.rpst.getRoot().id == f.id)
		{			
			// Update position of fragment's exit node
			var exit	= this.exitNode(f);
			if (this.dirLtr())
			{
				this.nodex[exit]	= this.spaces.x;
			}
			else
			{
				this.nodey[exit]	= this.spaces.y;
			}
		}
	}
	
	// all other sequences
	else
	{
		var exit		= this.exitNode(f);
		var entry		= this.entryNode(f);
		
		var node		= entry;
		var maxHeight	= 0;
		var maxWidth	= 0;
		var sumHeight	= 0;
		var sumWidth	= 0;
		
		/*
		 * workaround for JS
		 */
		var children	= this.fragments(f);
		var cfEntryMap	= new Array();
		var cfExitMap	= new Array();
		for (var c in children)
		{
			var cf	= children[c];
			if (cf.id != this.entryNode(cf) || !gf_isset(cfEntryMap[this.entryNode(cf)]))
			{
				cfEntryMap[this.entryNode(cf)]	= cf;
				cfEntryMap[cf.id]				= cf;
			}
			
			if (cf.id != this.exitNode(cf) || !gf_isset(cfExitMap[this.exitNode(cf)]))
			{
				cfExitMap[this.exitNode(cf)]	= cf;
				cfExitMap[cf.id]				= cf;
			}
		}
		
		/**
		 * update incidency lists since entry of sequence fragment connects to the fragment itself
		 */
		var outEdges	= this.getOutEdges(entry);
		var inEdges		= this.getInEdges(exit);
		var nextNode	= null;
		var penuNode	= null;
		for (var e in outEdges)
		{
			if (gf_isset(cfEntryMap[this.target(outEdges[e])]))
			{
				nextNode	= this.target(outEdges[e]);
				this.incidenceOut[entry][e].tgt	= f.id;
				
				if (!gf_isset(this.incidenceIn[f.id]))
					this.incidenceIn[f.id]	= new Array();
					
				this.incidenceIn[f.id].push({"src": entry, "tgt": f.id, "temporary": true, "orgId": outEdges[e].orgId});
				
				break;
			}
		}
		for (var e in inEdges)
		{
			if (gf_isset(cfExitMap[this.source(inEdges[e])]))
			{
				penuNode	= this.source(inEdges[e]);
				this.incidenceIn[exit][e].src	= f.id;
				
				if (!gf_isset(this.incidenceOut[f.id]))
					this.incidenceOut[f.id]	= new Array();
					
				this.incidenceOut[f.id].push({"src": f.id, "tgt": exit, "temporary": true, "orgId": inEdges[e].orgId});
				
				break;
			}
		}
		// end workaround
		
		var x			= 0;
		var y			= 0;
		
		this.nodex[entry]	= x;
		this.nodey[entry]	= y;
		
		/*
		 * JS workaround:
		 * - no entry for fragment's entry in entryMap -> if entry is in entryMap the entry node must be removed from the calculation as it is also the entry of another fragment
		 * - current fragment must be parent of fragment stored in entryMap -> simply check for fragment's id
		 */
		if (this.rpst.getRoot().id == f.id && gf_isset(this.entryMap[entry]))
		{
			
			this.branchHeight[entry]	= this.spaces.y;
			this.branchWidth[entry]		= this.spaces.x;
			
			if (this.dirLtr())
			{
				x	-= this.spaces.x;
			}
			else
			{
				y	-= this.spaces.y;
			}
		}
		// end workaround
		
		// cycle through the sequence elements
		while (node != null)
		{
			// compute fragment's dimensions
			maxHeight	= Math.max(maxHeight, this.branchHeight[node]);
			maxWidth	= Math.max(maxWidth, this.branchWidth[node]);
			sumHeight	+= this.branchHeight[node];
			sumWidth	+= this.branchWidth[node];
			
			// Update position of child fragment relative to given fragment
			if (this.dirLtr())
			{
				this.nodex[node]	= x;
				this.nodey[node]	= 0;
			}
			else
			{
				this.nodex[node]	= 0;
				this.nodey[node]	= y;
			}
			
			x	+= this.branchWidth[node];
			y	+= this.branchHeight[node];
			
			// Get the next node in line in a Depth-First Search (DFS) manner
			if (node == exit)
			{
				// leave loop if exit is hit
				node	= null;
			}
			else if (nextNode != null)
			{
				// workaround for JS
				node		= nextNode;
				nextNode	= null;
			}
			else if (gf_isset(cfEntryMap[this.exitNode(node)]) && cfEntryMap[this.exitNode(node)].type != "trivial")
			{
				node		= cfEntryMap[this.exitNode(node)].id;
			}
			else
			{
				outEdges	= this.getOutEdges(node);
				if (outEdges.length > 0 && node != exit)
				{
					if (node == penuNode)
					{
						// workaround for JS
						node	= exit;
					}
					else
					{
						node	= this.target(this.top(outEdges));
					}
				}
				else
				{
					node	= null;
				}
			}
		}
		
		/*
		 * JS workaround:
		 * - no entry for fragment's exit in exitMap -> if exit is in exitMap the exit node must be repositioned and the fragment's size must be reduced
		 * - current fragment must be parent of fragment stored in exitMap -> simply check for fragment's id
		 */
		if (this.rpst.getRoot().id == f.id && gf_isset(this.exitMap[exit]))
		{
			if (this.dirLtr())
			{
				this.nodex[exit]	-= this.spaces.x;
				sumWidth			-= this.spaces.x;
			}
			else
			{
				this.nodey[exit]	-= this.spaces.y;
				sumHeight			-= this.spaces.y;
			}
		}
		// end workaround
		
		// Update size of fragment
		if (this.dirLtr())
		{
			this.branchHeight[f.id]	= maxHeight;
			this.branchWidth[f.id]	= sumWidth - this.spaces.x;		// correction is needed as sequence's entry node is also contained in another fragment
		}
		else
		{
			this.branchHeight[f.id]	= sumHeight - this.spaces.y;	// correction is needed as sequence's entry node is also contained in another fragment
			this.branchWidth[f.id]	= maxWidth;
		}
	}
};

/**
 * Lays out an unstructured fragment.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - The fragment to lay out.
 * @returns {void}
 */
LinearTimeLayout.prototype.layoutUnstructured = function (f)
{	
	// JS workaround: moved this line from step 3 up (because of temporaryIncidenceLists)
	var fragmentEdges	= this.getEdges(f);
	
	// 1) identify back edges and compute the node order
	var type	= {};
	var nodeID	= this.entryNode(f);
	
	this.identifyEdgeTypes(nodeID, type, {}, {}, 0);
	
	var topology	= this.topologySort(f, nodeID, type);
	var parent		= {};
	var length		= {};
	this.lpstree(topology, type, parent, length);
	
	// 2) order edges to reduce number of crossings
	var newFragment	= this.orderEdges(f, nodeID);
	// TODO: use newFragment and implement orderEdges
	
	// 3) internally reverse back edges and compute layout	
	for (var eID in fragmentEdges)
	{
		var edge	= fragmentEdges[eID];
		var edgeID	= edge.orgId;
		if (type[edgeID] == this.edgeTypes.back)
		{
			this.reverse(edge);
		}
	}
	this.computeBranchDimensions(nodeID, parent);
	
	// by Matthias Schrammek
	this.branchHeight[f.id]	= this.branchHeight[nodeID];
	this.branchWidth[f.id]	= this.branchWidth[nodeID];
	
	var x	= 0;
	var y	= 0;
	if (this.dirLtr())
	{
		y			= 0 - Math.ceil((this.branchHeight[f.id] - this.spaces.y) / 2);
	}
	else
	{
		x			= 0 - Math.ceil((this.branchWidth[f.id] - this.spaces.x) / 2);
	}
	// end by Matthias Schrammek
	
	// JS workaround: if fragment's exit node is also entry of another node it belongs to the other fragment and thus the size of this fragment has to be reduced
	if (gf_isset(this.entryMap[this.exitNode(f)]))
	{
		if (this.dirLtr())
		{
			this.branchWidth[f.id]	-= this.spaces.x;
		}
		else
		{
			this.branchHeight[f.id]	-= this.spaces.y;
		}
	}
	// end workaround
	
	this.preliminaryLayout(nodeID, parent, x, y);
	this.compactLayout(f);
};

/**
 * Computes a longest path spanning tree.
 * 
 * @memberof! LinearTimeLayout
 * @param {Array} topology - The computed topology.
 * @param {Array} type - Edge types.
 * @param {Array} parent - Parents of each node.
 * @param {Array} length - Length of path to each node.
 * @returns {void}
 */
LinearTimeLayout.prototype.lpstree = function (topology, type, parent, length)
{
	// follow the topology
	for (var nID in topology)
	{
		var nodeID	= topology[nID];
		
		var space	= 0;
		if (this.dirLtr())
		{
			space	= this.spaces.x;
		}
		else
		{
			space	= this.spaces.y;
		}
		
		if (!gf_isset(length[nodeID]))
		{
			length[nodeID]	= 0;
		}
		
		// compute longest path to each node
		var outEdges	= this.getOutEdges(nodeID);
		for (var edgeID in outEdges)
		{
			var edge	= outEdges[edgeID];
				edgeID	= edge.orgId;
			if (type[edgeID] != this.edgeTypes.back)
			{
				var targetID	= this.target(edge);
				
				if (!gf_isset(length[targetID]) || length[targetID] < length[nodeID] + space)
				{
					length[targetID]	= length[nodeID] + space;
					parent[targetID]	= nodeID;
				}
			}
		}			
	}
};

/**
 * Order edges to minimize crossings by using a modified planarity checker.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - The fragment for which the edges are ordered.
 * @param {String} nodeID - Start node for planarity checker.
 * @returns {Array} Ordered edges.
 */
LinearTimeLayout.prototype.orderEdges = function (f, nodeID)
{
	// TODO:
	
	/* From "A linear time layout algorithm for business process models" by Gschwind et al.:
Before passing a fragment to the planarity checker, it is pre-processed such
that edges are no longer permitted to be drawn around the entire process. We accomplish this by adding an artificial edge from a fragment's
entry node to the fragment's exit node which prohibits an edge on
either the upper or the lower side of a fragment to be drawn around the entry
or exit nodes since it would need to cross the artificial edge.

The planarity checker returns a planar order for edges indicating how to draw
them in order to avoid edge crossing. In this stage all edges (i.e., including back
edges that were omitted previously) are considered. The planar order will be
used in subsequent stages. If a graph is non-planar we perform some extra
processing to find an optimal edge order. This modification will be discussed in
Section 4.4.
	 */
	
	// TODO: add artificial edge from exit to entry
	// TODO: what does f contain? getEdges(f)?
	// TODO: this.planarityEmbed(f, nodeID);
	
	var newFragment	= null;
	return f;	// TODO
};

/**
 * Implementation of planarity checker.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} G - The Fragment.
 * @param {Object} n - Start node.
 * @returns {void}
 */
LinearTimeLayout.prototype.planarityEmbed = function (G, n)
{
	// G: fragment
	// n: start node (entry node of fragment);
	// A(v): kind of incidenceOut[v]
	
	// TODO: underlying undirected graph 
	
	/*
	 * - G represented by set of properly ordered adjacency lists A(v)
	 * - L and R stacks, stored as linked lists; using arrays STACK, NEXT
	 * - STACK(i) gives a stack entry
	 * - NEXT(i) points to next entry on same stack
	 *    - NEXT(0) points to first entry on L
	 *    - NEXT(-1) points to first entry on R
	 * - FREE: first unused location in STACK
	 * - p: number of current path
	 * - v is vertex -> PATH(v) is number of first path containing v
	 * - i is number of path -> f(i) is last vertex on path i
	 * - blocks: ordered pairs on stack B
	 *    - pairs: (x,y) -> x is last entry on L, y is last entry on R of block
	 *    - x=0: block has no entries on L
	 *    - y=0: block has no entries on R
	 * - SAVE is temporary variable used for switching
	 */
	
	this.planStack	= [];		// STACK(0::E)
	this.planNext	= [];		// NEXT(-1::E)
	this.planF		= [];		// f(1::E-V+1)
	this.planPath	= {};		// PATH(1::V)
	this.planB		= [];		// B(1::E)
	this.planFree	= 0;
	
	// init
	this.planNext[-1]	= 0;
	this.planNext[0]	= 0;
	this.planFree		= 1;
	this.planStack[0]	= 0;
	this.planP			= 0;
	this.planS			= 0;
	this.planPath[n]	= 1;
	
	this.planarityPathfinder(n);
};

/**
 * DFS implementation. Find paths.
 * 
 * @memberof! LinearTimeLayout
 * @param {String} n - Start node for current DFS run.
 * @returns {void}
 */
LinearTimeLayout.prototype.planarityPathfinder = function (v)
{
	// v: node
	
	// TODO: does v->w indicate a directed edge? - remember: underlying undirected graph
			// edge of directed, rooted tree
	// TODO: where to get the integer values from?
			// some kind of ordering by path (0->v_i->v_j->|V| in cycle where v_i = 1, v_j = 2 ...)
	
	var outEdges	= this.getOutEdges(v);
	for (var e in outEdges)		// for w in A(v) do
	{
		var edge	= outEdges[e];
		var w		= this.target(edge);		// if v -> w then begin
		
		if (this.planS == 0)
		{
			this.planS	= v;
			this.planP++;
		}
		
		this.planPath[w]	= p;
		this.planarityPathfinder(w);
		
		// paper: delete stack entries and blocks corresponding to vertices no smaller than v
		// TODO: follow up: first while loop
	}
};

/**
 * Computes preliminary layout of an unstructured fragment.
 * 
 * @memberof! LinearTimeLayout
 * @param {String} nodeID - Fragment or node to compute the layout for.
 * @param {Array} parent - Parents array.
 * @param {int} x - X offset.
 * @param {int} y - Y offset.
 * @returns {void}
 */
// nodeID: id, parent: array, x: int, y: int
LinearTimeLayout.prototype.preliminaryLayout = function (nodeID, parent, x, y)
{
	// JS workaround
	if (this.isS(nodeID))
	{
		if (this.dirLtr())
		{
			x	-= this.spaces.x;
		}
		else
		{
			y	-= this.spaces.y;
		}
	}
	// end workaround
	
	// avoid updating exit nodes of existing fragments (except if it is also the exit of the unstructured fragment)
	if (gf_isset(this.boundaryNodes.exit[nodeID]) && !gf_isset(this.exitMap[nodeID]))
	{
	
	}
		
	// position node based on the x and y offset
	else
	{
		if (this.dirLtr())
		{
			this.nodex[nodeID]	= x;
			this.nodey[nodeID]	= y + Math.floor((this.branchHeight[nodeID] - this.spaces.y) / 2);
		}
		else
		{
			this.nodex[nodeID]	= x + Math.floor((this.branchWidth[nodeID] - this.spaces.x) / 2);
			this.nodey[nodeID]	= y;
		}
	}
	
	// compute max space to next node from dimensions of all adjacent nodes / fragments
	var height	= 0;
	var width	= 0;
	var outEdges	= this.getOutEdges(nodeID);
	for (var e in outEdges)
	{
		var edge	= outEdges[e];
		var src		= this.source(edge);
		var tgt		= this.target(edge);
		if (parent[tgt] == src)
		{
			height	= height + this.branchHeight[tgt];
			width	= width + this.branchWidth[tgt];
		}
		else
		{
			height	= height;
			width	= width;
		}
	}
	
	if (this.dirLtr())
	{
		x	= x + this.spaces.x;
		y	= y + Math.max(0, (this.branchHeight[nodeID] - height) / 2);
	}
	else
	{
		x	= x + Math.max(0, (this.branchWidth[nodeID] - width) / 2);
		y	= y + this.spaces.y;
	}
	
	// follow up with adjacent nodes / fragments
	for (var e in outEdges)
	{
		var edge	= outEdges[e];
		var src		= this.source(edge);
		var tgt		= this.target(edge);
		if (parent[tgt] == src)
		{
			// JS workaround for sequence fragments
			if (this.isS(src))
			{
				if (this.dirLtr())
				{
					x	+= this.spaces.x;
				}
				else
				{
					y	+= this.spaces.y;
				}
			}
			// end workaround
			
			this.preliminaryLayout(tgt, parent, x, y);
			
			// JS workaround for sequence fragments
			if (this.isS(src))
			{
				if (this.dirLtr())
				{
					this.nodey[tgt]	= 0;
				}
				else
				{
					this.nodex[tgt]	= 0;
				}
			}
			// end workaround
			
			if (this.dirLtr())
			{
				y	= y + this.branchHeight[tgt];
			}
			else
			{				
				x	= x + this.branchWidth[tgt];
			}
		}
		else
		{
			if (this.dirLtr())
			{
				this.edgey[edge.orgId]	= y;
			}
			else
			{
				this.edgex[edge.orgId]	= x;
			}
		}
	}
};

/**
 * Preprocessing. Splits gateways with multiple incoming and outgoing edges.
 * Normalizes graph. Handles MTGs. Adds unique sink and source nodes.
 * Creates incidence lists.
 * 
 * @memberof! LinearTimeLayout
 * @returns {Object} normalized graph
 */
LinearTimeLayout.prototype.preprocess = function ()
{
	
	var sources			= new Array();
	var sinks			= new Array();
	
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
	var uniqueSource	= this.addNode("##uSRC##", "virtual");
	for (var s in sources)
	{
		var sourceID	= sources[s];
		this.addEdge("virtSRC" + sourceID, uniqueSource, sourceID, "virtual");
	}
	
	// create new sink
	var uniqueSink	= this.addNode("##uSNK##", "virtual");
	for (var s in sinks)
	{
		var sinkID	= sinks[s];
		this.addEdge("virtSNK" + sinkID, sinkID, uniqueSink, "virtual");
	}
	
	// Create the incidence lists
	this.incidenceIn	= {};
	this.incidenceOut	= {};
	
	// Create incidence lists
	for (var e in this.edges)
	{
		var edge	= this.edges[e];
		var src		= this.source(edge);
		var tgt		= this.target(edge);
		
		if (!gf_isset(this.incidenceIn[tgt]))
			this.incidenceIn[tgt]	= new Array();
			
		if (!gf_isset(this.incidenceOut[src]))
			this.incidenceOut[src]	= new Array();
		
		edge	= {"src": src, "tgt": tgt, "temporary": true, "orgId": edge.id};
		this.incidenceOut[src].push(edge);
		this.incidenceIn[tgt].push(edge);
	}
	
	// Calculate sizes of labels and thus the spaces between nodes
	this.calculateSpaces();
	
	// jBPT preprocess (MTG)
	var normalizedGraph	= new this.NormGraph(this);
	
	var sources			= new Array();
	var sinks			= new Array();
	var mixed			= new Array();
	
	// copy nodes
	for (var n in this.nodes)
	{
		// by Matthias Schrammek: added check for start nodes
		if (this.getInEdges(n).length == 0 && this.getOutEdges(n).length == 0)
		{
			continue;
		}
		
		if (this.getInEdges(n).length == 0)
		{
			sources.push(n);
		}
		
		if (this.getOutEdges(n).length == 0)
		{
			sinks.push(n);
		}
		
		if (this.getInEdges(n).length > 1 && this.getOutEdges(n).length > 1)
		{
			mixed.push(n);
		}
		
		var newId			= normalizedGraph.addNode(n);
		
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
};

/**
 * Reverse edge.
 * 
 * @memberof! LinearTimeLayout
 * @param {mixed} edgeID - ID of edge to reverse or edge object.
 * @returns {void}
 */
LinearTimeLayout.prototype.reverse = function (edgeID)
{
	if (typeof edgeID == "object" && edgeID.hasOwnProperty("temporary"))
	{
		var src		= edgeID.src;
		var tgt		= edgeID.tgt;
		var edge	= {"src": tgt, "tgt": src, "temporary": true, "orgId": edgeID.orgId, "reversed": true};
		
		// remove old edge from inedges of tgt
		var newInEdges		= new Array();
		var inEdges			= this.getInEdges(tgt);
		for (var e in inEdges)
		{
			e	= inEdges[e];
			if (this.source(e) != src || this.target(e) != tgt)
			{
				newInEdges.push(e);
			}
		}
		this.incidenceIn[tgt]	= newInEdges;
		
		// remove old edge from outedges of src
		var newOutEdges		= new Array();
		var outEdges		= this.getOutEdges(src);
		for (var e in outEdges)
		{
			e	= outEdges[e];
			if (this.source(e) != src || this.target(e) != tgt)
			{
				newOutEdges.push(e);
			}
		}
		this.incidenceOut[src]	= newOutEdges;
		
		// add reversed edge to inedges of src
		if (!gf_isset(this.incidenceIn[src]))
			this.incidenceIn[src]	= new Array();
		this.incidenceIn[src].push(edge);
		
		// add reversed edge to outedges of tgt
		if (!gf_isset(this.incidenceOut[tgt]))
			this.incidenceOut[tgt]	= new Array();
		this.incidenceOut[tgt].push(edge);
	}
	
	else if (gf_isset(this.edges[edgeID]))
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
	}
};

/**
 * Set size of drawing area in order to start the drawing at the right position.
 * 
 * @memberof! LinearTimeLayout
 * @param {int} width - Width of the drawing area.
 * @param {int} height - Height of the drawing area.
 */
LinearTimeLayout.prototype.setAreaSize = function (width, height)
{
	this.drawingArea.width	= width;
	this.drawingArea.height	= height;
};

/**
 * Sets the manual position changes applied via the hybrid layouting method.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} changes - Relative position changes of nodes.
 * @returns {void}
 */
LinearTimeLayout.prototype.setManualChanges = function (changes)
{
	// TODO: implementation needed that suits the actual hybrid implementation in te S-BPM Groupware
};

/**
 * Set objects that contain the actual rendered elements.
 * 
 * @memberof! LinearTimeLayout
 * @param {Array} nodes - Pointer to empty nodes list.
 * @param {Array} edges - Pointer to empty edges list.
 * @returns {void}
 */
LinearTimeLayout.prototype.setRenderObjects = function (nodes, edges)
{
	this.renderObjects	= {"nodes": nodes, "edges": edges};
};

/**
 * Preset x and y grid distances. Set aesthetical space.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} spaces - x and y distances (preset grid distances) and aesthetical space (a); attributes: x,y,a
 * @returns {void}
 */
LinearTimeLayout.prototype.setSpaces = function (spaces)
{
	if (gf_isset(spaces) && typeof spaces === "object")
	{
		if (spaces.hasOwnProperty("x"))
		{
			this.spaces.x	= spaces.x;
		}
		
		if (spaces.hasOwnProperty("y"))
		{
			this.spaces.y	= spaces.y;
		}
		
		if (spaces.hasOwnProperty("a"))
		{
			this.spaces.a	= spaces.a;
		}
	}
};

/**
 * Returns source node of edge.
 * 
 * @memberof! LinearTimeLayout
 * @param {mixed} edgeID - ID of edge or edge object.
 * @returns {String} ID of source node.
 */
LinearTimeLayout.prototype.source = function (edgeID)
{
	// temporary edges from incidence lists
	if (typeof edgeID == "object" && edgeID.hasOwnProperty("temporary"))
	{
		return edgeID.src;
	}
	
	// original graph edges
	if (typeof edgeID == "object")
	{
		edgeID	= edgeID.id;
	}
	
	if (gf_isset(this.edges[edgeID]))
	{
		return this.edges[edgeID].source;
	}
		
	return 0;
};

/**
 * Returns target node of edge.
 * 
 * @memberof! LinearTimeLayout
 * @param {mixed} edgeID - ID of edge or edge object.
 * @returns {String} ID of target node.
 */
LinearTimeLayout.prototype.target = function (edgeID)
{
	// temporary edges from incidence lists
	if (typeof edgeID == "object" && edgeID.hasOwnProperty("temporary"))
	{
		return edgeID.tgt;
	}
	
	// original graph edges
	if (typeof edgeID == "object")
	{
		edgeID	= edgeID.id;
	}
	
	if (gf_isset(this.edges[edgeID]))
	{
		return this.edges[edgeID].target;
	}
		
	return 0;
};

/**
 * Returns the top element of the given array without removing it from the array.
 * 
 * @memberof! LinearTimeLayout
 * @param {Array} arr - An array.
 * @returns {mixed} Top element of array.
 */
LinearTimeLayout.prototype.top = function (arr)
{
	var elem	= arr.pop();
	arr.push(elem);
	return elem;
};

/**
 * Computes a topological order of the given fragment.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - Fragment for which the topological node order should be computed.
 * @param {Object} nodeID - ID of start node.
 * @param {Array} type - Contains edge types.
 * @return {Array} Topological order.
 */
LinearTimeLayout.prototype.topologySort = function (f, nodeID, type)
{
	// 1) Setup
	var inedges	= {};
	var fragmentNodes	= this.getNodes(f);
	
	for (var nID in fragmentNodes)
	{
		var newNode	= fragmentNodes[nID];
		
		if (newNode instanceof this.RPSTNode)
		{
			newNode	= newNode.id;
		}
			
		var outEdges	= this.getOutEdges(newNode);
		for (var edgeID in outEdges)
		{
			
			var edge	= outEdges[edgeID];
				edgeID	= edge.orgId;
				
			var tgt	= this.target(edge);
			
			if (!gf_isset(inedges[tgt]))
				inedges[tgt]	= 0;
				
			if (type[edgeID] != this.edgeTypes.back)
			{	
				inedges[tgt]++;
			}
		}
	}
	
	// 2) Topology Sort
	var topology	= [];
	var i			= 0;
	var length		= fragmentNodes.length;
	topology[topology.length]		= nodeID;
	
	while (i < length)
	{		
		var newNode	= topology[i];
		i++;
		var outEdges	= this.getOutEdges(newNode);
		for (var edgeID in outEdges)
		{
			var edge	= outEdges[edgeID];
			edgeID		= edge.orgId;
			if (type[edgeID] != this.edgeTypes.back)
			{
				var target		= this.target(edge);
				inedges[target]--;
				if (inedges[target] == 0)
				{
					topology[topology.length]	= target;
				}
			}
		}
	}
	
	return topology;
};

/**
 * Recursively pdate incidence lists to link fragments instead of single nodes based on computed RPST.
 * 
 * @memberof! LinearTimeLayout
 * @param {Object} f - Current fragment
 * @returns {void}
 */
LinearTimeLayout.prototype.updateIncidenceLists = function (f)
{
	this.createBoundaryNodesMap(f);
	
	// Reset incidenceIn and incidenceOut
	this.incidenceIn	= {};
	this.incidenceOut	= {};
	
	// Update incidence lists
	for (var e in this.edges)
	{
		var edge	= this.edges[e];
		var edgeId	= edge.id;
		var src		= this.source(edge);
		var tgt		= this.target(edge);
			edge	= {"src": src, "tgt": tgt, "temporary": true, "orgId": edgeId};
		
		if (!gf_isset(this.incidenceIn[tgt]))
			this.incidenceIn[tgt]	= new Array();
			
		if (!gf_isset(this.incidenceOut[src]))
			this.incidenceOut[src]	= new Array();		
		
		if (gf_isset(this.entryMap[tgt]) && gf_isset(this.exitMap[src]))
		{
			// Edge ends at entry node of fragment and starts at exit of fragment
			var src2	= this.exitMap[src];	// Get corresponding fragment
			var tgt2	= this.entryMap[tgt];	// Get corresponding fragment
			var edge2	= {"src": src2, "tgt": tgt2, "temporary": true, "orgId": edgeId};
				
			if (!gf_isset(this.incidenceIn[tgt2]))
				this.incidenceIn[tgt2]	= new Array();
				
			if (!gf_isset(this.incidenceOut[src2]))
				this.incidenceOut[src2]	= new Array();
			
			this.incidenceIn[tgt].push(edge2);
			this.incidenceIn[tgt2].push(edge2);
			this.incidenceOut[src].push(edge2);
			this.incidenceOut[src2].push(edge2);
		}
		
		if (gf_isset(this.entryMap[tgt]))
		{
			// Edge ends at entry node of fragment
			var tgt2	= this.entryMap[tgt];	// Get corresponding fragment
			var edge2	= {"src": src, "tgt": tgt2, "temporary": true, "orgId": edgeId};
				
			if (!gf_isset(this.incidenceIn[tgt2]))
				this.incidenceIn[tgt2]	= new Array();
			
			this.incidenceIn[tgt].push(edge);
			this.incidenceIn[tgt2].push(edge2);
			this.incidenceOut[src].push(edge2);
		}
		
		else if (gf_isset(this.exitMap[src]))
		{
			// Edge starts at exit node of fragment
			var src2	= this.exitMap[src];	// Get corresponding fragment
			var edge2	= {"src": src2, "tgt": tgt, "temporary": true, "orgId": edgeId};
				
			if (!gf_isset(this.incidenceOut[src2]))
				this.incidenceOut[src2]	= new Array();
			
			this.incidenceIn[tgt].push(edge2);
			this.incidenceOut[src].push(edge);
			this.incidenceOut[src2].push(edge2);
		}
		
		else
		{
			// All other edges				
			this.incidenceIn[tgt].push(edge);
			this.incidenceOut[src].push(edge);
		}
	}
};
