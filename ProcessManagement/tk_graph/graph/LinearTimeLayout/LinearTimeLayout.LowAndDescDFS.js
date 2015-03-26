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
 * LowAndDescDFS
 */

/**
 * A DFS computing different numbers for each node.
 * 
 * @class LowAndDescDFS
 * @see org.jbpt.algo.tree.tctree.LowAndDescDFS
 * @param {Object} graph - The graph to run the DFS on.
 * @param {Object} meta - Meta data containing DFS numbers, etc.
 */
LinearTimeLayout.prototype.LowAndDescDFS = function (graph, meta)
{
	
	/**
	 * Number of completed paths.
	 * @memberof! LowAndDescDFS
	 * @type {int}
	 */
	this.complNum	= 0;
	
	/**
	 * DFS number.
	 * @memberof! LowAndDescDFS
	 * @type {int}
	 */
	this.dfsNum		= 0;
	
	/**
	 * The graph to run the DFS on.
	 * @memberof! LowAndDescDFS
	 * @type {Object}
	 */
	this.graph		= graph;
	
	/**
	 * New path switch.
	 * @memberof! LowAndDescDFS
	 * @type {boolean}
	 */
	this.isNewPath	= false;
	
	/**
	 * Meta data.
	 * @memberof! LowAndDescDFS
	 * @type{Object}
	 */
	this.meta		= meta;
	
	/**
	 * Number of current path.
	 * @memberof! LowAndDescDFS
	 * @type {int}
	 */
	this.pathNumber	= 1;
	
	// initialize the DFS
	this.init();
};

/*
 * LowAndDescDFS Methods
 */
/**
 * Create empty edge map to store visited state of edges, path numbers, etc.
 * 
 * @memberof! LowAndDescDFS
 * @param {Object} graph - The graph for which to create the map.
 * @returns {Array} Array of edge IDs, each entry set to null.
 */
LinearTimeLayout.prototype.LowAndDescDFS.prototype.createEdgeMap = function (graph)
{
	var map	= {};
	
	for (var edgeID in graph.edges)
	{
		map[edgeID]	= null;
	}
	
	return map;
};

/**
 * Create empty node map to store visited state of nodes, dfs numbers, etc.
 * 
 * @memberof! LowAndDescDFS
 * @param {Object} graph - The graph for which to create the map.
 * @returns {Array} Array of node IDs, each entry set to null.
 */
LinearTimeLayout.prototype.LowAndDescDFS.prototype.createNodeMap = function (graph)
{
	var map	= {};
	for (var v in graph.nodes)
	{
		map[v]	= null;
	}
	return map;
};

/**
 * The actual DFS.
 * 
 * @memberof! LowAndDescDFS
 * @param {String} node - ID of current node.
 * @returns {void}
 */
LinearTimeLayout.prototype.LowAndDescDFS.prototype.dfs = function (node)
{
	this.dfsNum++;
	this.meta.dfsNum[node]			= this.dfsNum;
	this.meta.dfsNodeState[node]	= "gray";
	
	// load adjacency list of this node
	var adjV	= this.meta.dfsAdjLists[node];
	
	this.preVisit(node, this.meta.dfsNum[node]);
	
	// cycle through all incident edges
	for (var e in adjV)
	{
		var edge	= adjV[e];
		
		if (this.meta.dfsEdgeType[edge] == "edgeNotVisited")
		{
			var w	= this.e(edge).getOtherVertex(node);
			
			this.e(edge).changeOrientation(node, w);
			
			if (this.meta.dfsNodeState[w] == "white")
			{
				this.meta.dfsEdgeType[edge]	= "treeedge";
				
				this.preTraverse(edge, w, true);
				
				this.dfs(w);
				
				this.postTraverse(edge, w);
			}
			else
			{
				this.meta.dfsEdgeType[edge]	= "backedge";
				
				this.preTraverse(edge, w, false);
			}
		}
	}
	
	this.complNum++;
	this.meta.dfsNodeState[node]	= "black";
	this.meta.dfsComplNum[node]		= this.complNum;
	
	this.postVisit(node, this.meta.dfsNum[node], this.meta.dfsComplNum[node]);
};

/**
 * Auxiliary function to resolve edgeID to edge of original graph.
 * 
 * @memberof! LowAndDescDFS
 * @param {String} edge - ID of edge to resolve.
 * @returns {Object} The actual edge.
 */
LinearTimeLayout.prototype.LowAndDescDFS.prototype.e = function (edge)
{
	return this.graph.edges[edge];
};

/**
 * Initialize the DFS and the meta object.
 * 
 * @memberof! LowAndDescDFS
 * @returns {void}
 */
LinearTimeLayout.prototype.LowAndDescDFS.prototype.init = function ()
{
	// AbstractDFS
	this.meta.dfsNodeState	= this.createNodeMap(this.graph);
	this.meta.dfsNum		= this.createNodeMap(this.graph);
	this.meta.dfsComplNum	= this.createNodeMap(this.graph);
	this.meta.dfsEdgeType	= this.createEdgeMap(this.graph);
	
	for (var n in this.graph.nodes)
	{
		this.meta.dfsNodeState[n]	= "white";
		this.meta.dfsNum[n]			= -1;
		this.meta.dfsComplNum[n]	= -1;
	}
	
	for (var e in this.graph.edges)
	{
		this.meta.dfsEdgeType[e]	= "edgeNotVisited";
	}
	
	
	// ParentAndPathDFS
	this.meta.dfsTreeArc		= this.createNodeMap(this.graph);
	this.meta.dfsParent			= this.createNodeMap(this.graph);
	this.meta.dfsPathNumber		= this.createEdgeMap(this.graph);
	this.meta.dfsStartsNewPath	= this.createEdgeMap(this.graph);
	
	for (var n in this.graph.nodes)
	{
		this.meta.dfsParent[n]	= "invalidNode";
		this.meta.dfsTreeArc[n]	= "invalidEdge";
	}
	
	for (var e in this.graph.edges)
	{
		this.meta.dfsPathNumber[e]		= -1;
		this.meta.dfsStartsNewPath[e]	= false;
	}
	
	
	// LowAndDescDFS
	this.meta.dfsLowpt1Num		= this.createNodeMap(this.graph);
	this.meta.dfsLowpt2Num		= this.createNodeMap(this.graph);
	this.meta.dfsLowpt1Vertex	= this.createNodeMap(this.graph);
	this.meta.dfsLowpt2Vertex	= this.createNodeMap(this.graph);
	this.meta.dfsNumDesc		= this.createNodeMap(this.graph);
	
	for (var n in this.graph.nodes)
	{
		this.meta.dfsLowpt1Num[n]		= -1;
		this.meta.dfsLowpt2Num[n]		= -1;
		this.meta.dfsLowpt1Vertex[n]	= "invalidNode";
		this.meta.dfsLowpt2Vertex[n]	= "invalidNode";
		this.meta.dfsNumDesc[n]			= -1;
	}
};

/**
 * Post traverse step.
 * 
 * @memberof! LowAndDescDFS
 * @param {Object} edge - ID of followed edge.
 * @param {Object} w - Current node of DFS.
 * @returns {void}
 */
LinearTimeLayout.prototype.LowAndDescDFS.prototype.postTraverse = function (edge, w)
{
	var v	= this.e(edge).getOtherVertex(w);
	
	
	if (this.meta.dfsLowpt1Num[w] < this.meta.dfsLowpt1Num[v])
	{
		var min	= Math.min(this.meta.dfsLowpt1Num[v], this.meta.dfsLowpt2Num[w]);
		this.meta.dfsLowpt2Num[v]	= min;

		if (min == this.meta.dfsLowpt1Num[v])
		{
			this.meta.dfsLowpt2Vertex[v]	= this.meta.dfsLowpt1Vertex[v];
		}
		else
		{	
			this.meta.dfsLowpt2Vertex[v]	= this.meta.dfsLowpt2Vertex[w];
		}
		
		this.meta.dfsLowpt1Num[v]		= this.meta.dfsLowpt1Num[w];
		this.meta.dfsLowpt1Vertex[v]	= this.meta.dfsLowpt1Vertex[w];
	}
	else if (this.meta.dfsLowpt1Num[w] == this.meta.dfsLowpt1Num[v])
	{
		if (this.meta.dfsLowpt2Num[w] < this.meta.dfsLowpt2Num[v])
		{
			this.meta.dfsLowpt2Num[v]		= this.meta.dfsLowpt2Num[w];
			this.meta.dfsLowpt2Vertex[v]	= this.meta.dfsLowpt2Vertex[w];
		}
	}
	else
	{
		if (this.meta.dfsLowpt1Num[w] < this.meta.dfsLowpt2Num[v])
		{
			this.meta.dfsLowpt2Num[v]		= this.meta.dfsLowpt1Num[w];
			this.meta.dfsLowpt2Vertex[v]	= this.meta.dfsLowpt1Vertex[w];
		}
	}
	
	this.meta.dfsNumDesc[v]		= this.meta.dfsNumDesc[v] + this.meta.dfsNumDesc[w];
};

/**
 * Post visit step. No actual function, just implemented to follow Interface structure of jBPT.
 * 
 * @memberof! LowAndDescDFS
 * @param {Object} node
 * @param {Object} dfsNumber
 * @param {Object} complNumber
 * @returns {void}
 */
LinearTimeLayout.prototype.LowAndDescDFS.prototype.postVisit = function (node, dfsNumber, complNumber)
{
	
};

/**
 * Pre traverse step.
 * 
 * @memberof! LowAndDescDFS
 * @param {String} edge - ID of edge to traverse.
 * @param {String} w - ID of target node.
 * @param {boolean} treeEdge - Is edge of type "treeedge"?
 * @returns {void}
 */
LinearTimeLayout.prototype.LowAndDescDFS.prototype.preTraverse = function (edge, w, treeEdge)
{
	// ParentAndPathDFS
	var v	= this.e(edge).getOtherVertex(w);
	if (treeEdge == true)
	{
		this.meta.dfsParent[w]			= v;
		this.meta.dfsTreeArc[w]			= edge;
		this.meta.dfsPathNumber[edge]	= this.pathNumber;
		
		if (this.isNewPath == true)
		{
			this.meta.dfsStartsNewPath[edge]	= true;
			this.isNewPath						= false;
		}
	}
	else
	{
		this.meta.dfsPathNumber[edge]	= this.pathNumber;
		
		if (this.isNewPath == true)
		{
			this.meta.dfsStartsNewPath[edge]	= true;
		}
		
		this.pathNumber++;
		this.isNewPath	= true;
	}
	
	
	// LowAndDescDFS
	if (treeEdge != true)
	{
		if (this.meta.dfsNum[w] < this.meta.dfsLowpt1Num[v])
		{
			this.meta.dfsLowpt2Num[v]		= this.meta.dfsLowpt1Num[v];
			this.meta.dfsLowpt2Vertex[v]	= this.meta.dfsLowpt1Vertex[v];
			
			this.meta.dfsLowpt1Num[v]		= this.meta.dfsNum[w];
			this.meta.dfsLowpt1Vertex[v]	= w;
		}
		else if (this.meta.dfsNum[w] > this.meta.dfsLowpt1Num[v])
		{
			if (this.meta.dfsNum[w] < this.meta.dfsLowpt2Num[v])
			{
				this.meta.dfsLowpt2Num[v]		= this.meta.dfsNum[w];
				this.meta.dfsLowpt2Vertex[v]	= w;
			}
		}
	}
};

/**
 * Pre Visit step.
 * 
 * @memberof! LowAndDescDFS
 * @param {String} node - ID of current node.
 * @param {int} dfsNumber - Current dfs number.
 * @returns {void}
 */
LinearTimeLayout.prototype.LowAndDescDFS.prototype.preVisit = function (node, dfsNumber)
{
	// LowAndDescDFS
	this.meta.dfsLowpt1Num[node]		= dfsNumber;
	this.meta.dfsLowpt2Num[node]		= dfsNumber;
	this.meta.dfsLowpt1Vertex[node]		= node;
	this.meta.dfsLowpt2Vertex[node]		= node;
	this.meta.dfsNumDesc[node]			= 1;
};

/**
 * Start DFS by calling dfs() with the root node.
 * 
 * @memberof! LowAndDescDFS
 * @param {String} root - ID of the root of the DFS.
 * @returns {void}
 */
LinearTimeLayout.prototype.LowAndDescDFS.prototype.start = function (root)
{
	this.dfs(root);
};
