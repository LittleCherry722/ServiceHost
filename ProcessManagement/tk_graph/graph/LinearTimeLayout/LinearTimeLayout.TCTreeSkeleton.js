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
 * TCTreeSkeleton
 */

/**
 * Skeleton of TCTree
 * 
 * @class TCTreeSkeleton
 * @see org.jbpt.algo.tree.tctree.TCSkeleton
 * @param {Object} parent - Instance of LinearTimeLayout
 */
LinearTimeLayout.prototype.TCTreeSkeleton = function (parent)
{
	/**
	 * Edges removed from the graph.
	 * @memberof! TCTreeSkeleton
	 * @type {Array}
	 */
	this.deletedEdges	= {};
	
	/**
	 * Maps original edges to edges of the TCTree.
	 * @memberof! TCTreeSkeleton
	 * @type {Array}
	 */
	this.e2o			= {};
	
	/**
	 * ID of last inserted edge.
	 * @memberof! TCTreeSkeleton
	 * @type {int}
	 */
	this.edgeID			= 0;
	
	/**
	 * Edges of the TCTree.
	 * @memberof! TCTreeSkeleton
	 * @type {Array}
	 */
	this.edges			= {};
	
	/**
	 * Incidence list for nodes.
	 * @memberof! TCTreeSkeleton
	 * @type {Array}
	 */
	this.incidentEdges	= {};
	
	/**
	 * Count of nodes of the TCTree.
	 * @memberof! TCTreeSkeleton
	 * @type {int}
	 */
	this.nodeCount		= 0;
	
	/**
	 * Nodes of the TCTree.
	 * @memberof! TCTreeSkeleton
	 * @type {Array}
	 */
	this.nodes			= {};
	
	/**
	 * Maps edges of the TCTree to edges of the original graph.
	 * @memberof! TCTreeSkeleton
	 * @type {Array}
	 */
	this.o2e			= {};
	
	/**
	 * Array of edges of the original graph.
	 * @memberof! TCTreeSkeleton
	 * @type {Array}
	 */
	this.originalEdges	= {};
	
	/**
	 * Array of nodes of the original graph.
	 * @memberof! TCTreeSkeleton
	 * @type {Array}
	 */
	this.originalNodes	= {};
	
	/**
	 * Instance of LinearTimeLayout.
	 * @memberof! TCTreeSkeleton
	 * @type {Object}
	 */
	this.parent			= parent;
	
	/**
	 * Virtual Edges.
	 * @memberof! TCTreeSkeleton
	 * @type {Array}
	 */
	this.virtualEdges	= {};
	
	/**
	 * ID of last virtual edge.
	 * @memberof! TCTreeSkeleton
	 * @type {int}
	 */
	this.virtualEdgeID	= 0;
};

/*
 * TCTreeSkeleton Methods
 */

/**
 * Add all edges of the original graph to the skeleton.
 * Maps edges of the skeleton to the edges of the original graph and vice versa.
 *  
 * @memberof! TCTreeSkeleton
 * @param {Object} graph - The original graph.
 * @returns {void}
 */
LinearTimeLayout.prototype.TCTreeSkeleton.prototype.addAll = function (graph)
{
	for (var o in graph.edges)
	{
		var e			= "es" + this.edgeID++;
		var edge		= new this.parent.BasicEdge(e, graph.edges[o].v1, graph.edges[o].v2);
			edge.orgId	= o;
		this.e2o[e]		= o;
		this.o2e[o]		= edge;
		
		this.addEdge(edge);
	}
};

/**
 * Add edge to TCTree.
 * 
 * @memberof! TCTreeSkeleton
 * @param {String|Object} edge - Either edge ID or actual instance.
 * @returns {void} 
 */
LinearTimeLayout.prototype.TCTreeSkeleton.prototype.addEdge = function (edge)
{
	if (edge instanceof LinearTimeLayout.prototype.BasicEdge)
		edgeID	= edge.id;
	else
		edgeID	= edge;

	if (edge instanceof LinearTimeLayout.prototype.BasicEdge)
	{
		this.o2e[edge.orgId]	= edge;
	}
	
	// edge: edgeID
	this.originalEdges[edgeID]	= edge;
	this.edges[edgeID]			= edge;
	
	this.addNodes(edge);
};

/**
 * Add a single node to the TCTree if not already added.
 * Adds the given edge to the incidence list for the node.
 * 
 * @memberof! TCTreeSkeleton
 * @param {String} node - ID of the node to add.
 * @param {String|Object} edge - Either edge ID or actual edge object.
 * @returns {void}
 */
LinearTimeLayout.prototype.TCTreeSkeleton.prototype.addNode = function (node, edge)
{
	if (!gf_isset(this.nodes[node]))
	{
		this.nodes[node]			= node;
		this.incidentEdges[node]	= new Array();
		this.nodeCount++;
	}
	
	if (edge instanceof LinearTimeLayout.prototype.BasicEdge)
		edge	= edge.id;
	
	this.incidentEdges[node].unshift(edge);
};

/**
 * Add the start and end node of an edge to the TCTree.
 * The edge itself will be stored as incident edge for both nodes.
 * 
 * @memberof! TCTreeSkeleton
 * @param {String|Object} edge - The edge (ID or instance) for which both nodes will be added.
 * @returns {void}
 */
LinearTimeLayout.prototype.TCTreeSkeleton.prototype.addNodes = function (edge)
{
	// edge: edgeID
	var v1	= null;
	var v2	= null;
	
	if (edge instanceof LinearTimeLayout.prototype.BasicEdge)
	{
		v1	= edge.v1.id;
		v2	= edge.v2.id;
	}
	else
	{
		v1	= this.parent.normGraph.source(edge);
		v2	= this.parent.normGraph.target(edge); 
	}
	
	this.addNode(v1, edge);
	this.addNode(v2, edge);
};

/**
 * Add start and end node for an edge of the original graph.
 * 
 * @memberof! TCTreeSkeleton
 * @param {String} edge - ID of the edge for which both nodes should be added.
 * @returns {void}
 */
LinearTimeLayout.prototype.TCTreeSkeleton.prototype.addOriginalNodes = function (edge)
{
	if (!gf_isset(this.parent.extraEdges[edge]))
	{
		var src	= this.parent.source(edge);
		var tgt	= this.parent.target(edge);
		
		if (src != 0 && tgt != 0)
		{			
			this.originalNodes[src]	= src;
			this.originalNodes[tgt]	= tgt;
		}
	}
};

/**
 * Add a virtual edge to the TCTree.
 * 
 * @memberof! TCTreeSkeleton
 * @param {String|Object} edge - ID or actual object.
 * @returns {void}
 */
LinearTimeLayout.prototype.TCTreeSkeleton.prototype.addVirtualEdge = function (edge)
{
	if (edge instanceof LinearTimeLayout.prototype.BasicEdge)
		edgeID	= edge.id;
	else
		edgeID	= edge;
	
	// edge: edgeID
	this.virtualEdges[edgeID]	= edge;
	this.edges[edgeID]			= edge;
	
	this.addNodes(edge);
};

/**
 * Create a new virtual edge and add it to the TCTree.
 * 
 * @memberof! TCTreeSkeleton
 * @param {String} v1 - ID of the start node of the virtual edge.
 * @param {String} v2 - ID of the end node of the virtual edge.
 * @returns {Object} The virtual edge.
 */
LinearTimeLayout.prototype.TCTreeSkeleton.prototype.createVirtualEdge = function (v1, v2)
{
	if (gf_isset(this.parent.normGraph.nodes[v1]))
		v1	= this.parent.normGraph.nodes[v1];
		
	if (gf_isset(this.parent.normGraph.nodes[v2]))
		v2	= this.parent.normGraph.nodes[v2];
	
	var id				= "vEdge" + this.virtualEdgeID++;
	var edge			= new this.parent.BasicEdge(id, v1, v2);
		edge.virtual	= true;
	
	this.addVirtualEdge(edge);
	
	return edge;
};

/**
 * Checks if the given edge is a virtual edge.
 * 
 * @memberof! TCTreeSkeleton
 * @param {String|Object} edge - ID or actual object.
 * @returns {boolean} True if the given edge is virtual, false otherwise.
 */
LinearTimeLayout.prototype.TCTreeSkeleton.prototype.isVirtual = function (edge)
{
	if (edge instanceof LinearTimeLayout.prototype.BasicEdge)
		edge	= edge.id;
		
	return gf_isset(this.virtualEdges[edge]);
};

/**
 * Get count of edges of original graph.
 * 
 * @memberof! TCTreeSkeleton
 * @returns {int} Returns number of edges of the original graph (if there are 2 or more edges, 2 is returned).
 */
LinearTimeLayout.prototype.TCTreeSkeleton.prototype.originalEdgesCount = function ()
{
	var count	= 0;
	for (var e in this.o2e)
	{
		count++;
		
		if (count > 1)
			break;
	}
	
	return count;
};

/**
 * Remove an edge from skeleton and add to deletedEdges array.
 *  
 * @memberof! TCTreeSkeleton
 * @param {String|Object} edge - ID or actual object.
 * @returns {void}
 */
LinearTimeLayout.prototype.TCTreeSkeleton.prototype.removeEdge = function (edge)
{	
	if (edge instanceof LinearTimeLayout.prototype.BasicEdge)
		edge	= edge.id;
		
	// edge: edgeID
	this.deletedEdges[edge]	= this.edges[edge];
	delete this.o2e[this.e2o[edge]];
	delete this.e2o[edge];
	delete this.edges[edge];
	delete this.originalEdges[edge];
	delete this.virtualEdges[edge];
};

/**
 * Removes an edge of the original graph from the skeleton.
 * 
 * @memberof! TCTreeSkeleton
 * @param {String} edge - ID of edge of original graph.
 * @returns {void}
 */
LinearTimeLayout.prototype.TCTreeSkeleton.prototype.removeOriginalEdge = function (edge)
{
	delete this.removeEdge[this.o2e[edge]];
};