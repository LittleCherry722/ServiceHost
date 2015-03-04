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
 * 
 * Parts of this Code are based on or ported from the jBPT framework v0.2.393 (http://code.google.com/p/jbpt/).
 * All rights of the jBPT framework belong to their respective owners.
 * The jBPT framework is originally licensed under the Lesser General Public License (LGPL).
 * A copy of this license can be obtained at http://www.gnu.org/licenses/lgpl-3.0.en.html.
 */

/*
 * RPST
 */

/**
 * The Refined Process Structure Tree.
 * 
 * @class RPST
 * @see org.jbpt.algo.tree.rpst.RPST
 * @param {LinearTimeLayout} parent - LinearTimeLayout instance
 */
LinearTimeLayout.prototype.RPST = function (parent)
{
	/**
	 * Adjacency list.
	 * @memberof! RPST
	 * @type {Array}
	 */
	this.adjacency	= {};
	
	/**
	 * ID of last added edge.
	 * @memberof! RPST
	 * @type {int}
	 */
	this.edgeID		= 0;
	
	/**
	 * Edges of the RPST.
	 * @memberof! RPST
	 * @type {Array}
	 */
	this.edges		= {};
	
	/**
	 * ID of node added last.
	 * @memberof! RPST
	 * @type {Object}
	 */
	this.nodeID		= 0;
	
	/**
	 * Nodes of the RPST.
	 * @memberof! RPST
	 * @type {Object}
	 */
	this.nodes		= {};
	
	/**
	 * LinearTimeLayout
	 * @memberof! RPST
	 * @type {Object}
	 */
	this.parent		= parent;
	
	/**
	 * Parents of nodes within the RPST.
	 * @memberof! RPST
	 * @type {Array}
	 */
	this.parents	= {};
	
	/**
	 * Root of the RPST.
	 * @memberof! RPST
	 * @type {String}
	 */
	this.root		= null;
};

/*
 * RPST Methods
 */

/**
 * Add edge to RPST.
 * 
 * @memberof! RPST
 * @param {String} v1 - ID of start node of the edge.
 * @param {String} v2 - ID of end node of the edge.
 */
LinearTimeLayout.prototype.RPST.prototype.addEdge = function (v1, v2)
{
	// v1, v2: node ID
	
	var id		= "e" + this.edgeID++;
	var edge	= new this.parent.BasicEdge(id, this.nodes[v1], this.nodes[v2]);
	this.edges[id]	= edge;
	
	// add edge to adjacency
	if (!gf_isset(this.adjacency[v1]))
		this.adjacency[v1]	= {};
		
	this.adjacency[v1][id]	= edge;
	
	// add parent
	this.parents[v2]	= v1;
};

/**
 * Add node to RPST.
 * 
 * @memberof! RPST
 * @param {Object} node - Node object.
 * @returns {void}
 */
LinearTimeLayout.prototype.RPST.prototype.addNode = function (node)
{
	var id	= "f:n" + this.nodeID++;
	node.id	= id;
	this.nodes[id]	= node;
};

/**
 * Returns all children for a certain node of the RPST.
 * 
 * @memberof! RPST
 * @param {String|Object} node - Either node or its ID.
 * @param {boolean} getNodes - When set to true the child nodes are returned instead of only their IDs.
 * @param {String} index - Index of the resulting array. Possible values: all, entry, exit, entryCount, exitCount; "all" includes the other four indexes; entry / exit maps children to their entry / exit nodes; entryCount / exitCount counts the number of children to which the node is entry / exit
 * @returns {Object|Array} The children of the given node.
 */
LinearTimeLayout.prototype.RPST.prototype.getChildren = function (node, getNodes, index)
{
	if (!gf_isset(getNodes) || getNodes !== true)
		getNodes = false;
	
	if (gf_isset(node.id))
		node	= node.id;
		
	if (!gf_isset(index) || getNodes != true)
		index	= "auto";
	
	// return direct child nodes
	var childNodes	= null;
	
	if (index == "auto")
	{
		childNodes	= new Array();
	}
	else if (index == "all")
	{
		childNodes	= {"entry": {}, "exit": {}, "entryCount": {}, "exitCount": {}};
	}
	else
	{
		childNodes	= {};
	}
	
	for (var a in this.adjacency[node])
	{
		var edge	= this.adjacency[node][a];
		var s		= edge.v1.id;
		var t		= edge.v2.id;
		
		if (t == node)
		{
			t	= s;
		}
		
		if (getNodes == true)
		{
			var adjNode	= this.nodes[t];
			
			// map child fragments to their entry node
			if (index == "entry")
			{
				childNodes[adjNode.getEntry()]	= adjNode;
			}
			
			// map child fragments to their exit node
			else if (index == "exit")
			{
				childNodes[adjNode.getExit()]	= adjNode;	
			}
			
			// count of how many fragments a certain node is the entry node of
			else if (index == "entrycount")
			{
				if (!gf_isset(childNodes[adjNode.getEntry()]))
				{
					childNodes[adjNode.getEntry()]	= 0;
				}
				childNodes[adjNode.getEntry()]++;
			}
			
			// count of how many fragments a certain node is the exit node of
			else if (index == "exitcount")
			{
				if (!gf_isset(childNodes[adjNode.getExit()]))
				{
					childNodes[adjNode.getExit()]	= 0;
				}
				childNodes[adjNode.getExit()]++;
			}
			
			// collect all of the above information
			else if (index == "all")
			{
				var entry	= adjNode.getEntry();
				var exit	= adjNode.getExit();
				
				if (!gf_isset(childNodes.entryCount[entry]))
				{
					childNodes.entryCount[entry]	= 0;
					childNodes.exitCount[entry]		= 0;
				}
				
				if (!gf_isset(childNodes.entryCount[exit]))
				{
					childNodes.entryCount[exit]		= 0;
					childNodes.exitCount[exit]		= 0;
				}
				
				childNodes.entry[entry]		= adjNode;
				childNodes.exit[exit]		= adjNode;
				childNodes.entryCount[entry]++;
				childNodes.exitCount[exit]++;
			}
			else
			{
				childNodes.push(this.nodes[t]);
			}
		}
		
		// only return IDs of children
		else
		{
			childNodes.push(t);
		}
	}
	
	return childNodes;
};

/**
 * Returns all children for a certain node of the RPST.
 * Attributes of the resulting Object:
 * <br>
 * - children: Array containing the actual children<br>
 * - entry: Mapping fragments to their entry nodes<br>
 * - exit: Mapping fragments to their exit nodes<br>
 * - entryCount: Count of fragments to which a certain node is the entry<br>
 * - exitCount: Count of fragments to which a certain node is the exit<br>
 * - ids: Maps fragments to their IDs<br>
 * - count: Count of child fragments
 * 
 * @memberof! RPST
 * @param {Object} node - Instance of RPSTnode.
 * @returns {Object} Information about the fragment's children.
 */
LinearTimeLayout.prototype.RPST.prototype.getChildren2 = function (node)
{	
	var childNodes	= {"children": new Array(), "entry": {}, "exit": {}, "entryCount": {}, "exitCount": {}, "ids": {}, "count": 0};
	
	if (node instanceof this.parent.RPSTNode)
	{
		node	= node.id;
		
		for (var a in this.adjacency[node])
		{
			var edge	= this.adjacency[node][a];
			var s		= edge.v1.id;
			var t		= edge.v2.id;
			
			if (s == node)
			{
				var adjNode	= this.nodes[t];
				var entry	= adjNode.getEntry();
				var exit	= adjNode.getExit();
				
				if (!gf_isset(childNodes.entryCount[entry]))
				{
					childNodes.entryCount[entry]	= 0;
					childNodes.exitCount[entry]		= 0;
				}
				
				if (!gf_isset(childNodes.entryCount[exit]))
				{
					childNodes.entryCount[exit]		= 0;
					childNodes.exitCount[exit]		= 0;
				}
				
				childNodes.children.push(adjNode);
				childNodes.entry[entry]		= adjNode;
				childNodes.exit[exit]		= adjNode;
				childNodes.entryCount[entry]++;
				childNodes.exitCount[exit]++;
				childNodes.ids[adjNode.id]	= adjNode;
				childNodes.count++;
			}
		}
	}
	
	return childNodes;
};

/**
 * Get edges of the RPST which are incident to a given node.
 * 
 * @memberof! RPST
 * @param {Object} node - Instance of RPSTNode, the node for which to get the edges
 * @param {String} direction - in | out | both - depicting the direction of the edges
 * @returns {Array} List of edges incident to the given node (either incoming, outgoing or both).
 */
LinearTimeLayout.prototype.RPST.prototype.getEdges = function (node, direction)
{
	var edges	= new Array();
	
	if (!gf_isset(direction) || (direction != "in" && direction != "out"))
		direction	= "both";
	
	if (node instanceof this.parent.RPSTNode)
	{
		node	= node.id;
		
		for (var a in this.adjacency[node])
		{
			var edge	= this.adjacency[node][a];
			var s		= edge.v1.id;
			var t		= edge.v2.id;
			
			if ((direction == "out" || direction == "both") && s == node)
			{
				edges.push(edge);
			}
			
			if ((direction == "in" || direction == "both") && t == node)
			{
				edges.push(edge);
			}
		}
	}
	
	return edges;
};

/**
 * Get root node of RPST.
 * 
 * @memberof! RPST
 * @returns {Object} The root node.
 */
LinearTimeLayout.prototype.RPST.prototype.getRoot = function ()
{
	if (this.root != null)
	{
		return this.nodes[this.root];
	}
	return null;
};

/**
 * Get ID of source node of edge.
 *  
 * @memberof! RPST
 * @param {String} edge - ID of the edge.
 * @returns {String} ID of the edge's start node.
 */
LinearTimeLayout.prototype.RPST.prototype.source = function (edge)
{
	if (gf_isset(this.edges[edge]))
		return this.edges[edge].v1.id;
		
	return null;
};

/**
 * Get ID of target node of edge.
 *  
 * @memberof! RPST
 * @param {String} edge - ID of the edge.
 * @returns {String} ID of the edge's end node.
 */
LinearTimeLayout.prototype.RPST.prototype.target = function (edge)
{
	if (gf_isset(this.edges[edge]))
		return this.edges[edge].v2.id;
		
	return null;
};