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
 * NumberDFS
 */
LinearTimeLayout.prototype.NumberDFS = function (graph, meta, adjMap)
{
	this.adj			= adjMap;
	this.complNum		= 0;
	this.dfsNum			= 0;
	this.graph			= graph;
	this.isNewPath		= false;
	this.m				= 0;
	this.meta			= meta;
	this.pathNumber		= 1;
	this.treeArc		= null;
	
	this.init();
};

/*
 * NumberDFS Methods
 */
LinearTimeLayout.prototype.NumberDFS.prototype.createEdgeMap = function (graph)
{
	var map	= {};
	
	for (var edgeID in graph.edges)
	{
		map[edgeID]	= null;
	}
	
	return map;
};

LinearTimeLayout.prototype.NumberDFS.prototype.createNodeMap = function (graph)
{
	var map	= {};
	for (var v in graph.nodes)
	{
		map[v]	= null;
	}
	return map;
};

LinearTimeLayout.prototype.NumberDFS.prototype.dfs = function (node)
{
	this.dfsNum++;
	this.meta.dfsNum[node]			= this.dfsNum;
	this.meta.dfsNodeState[node]	= "gray";
	
	var adjV	= this.meta.dfsAdjLists[node];
	
	this.preVisit(node, this.meta.dfsNum[node]);
	
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

LinearTimeLayout.prototype.NumberDFS.prototype.e = function (edge)
{
	return this.graph.edges[edge];
};

LinearTimeLayout.prototype.NumberDFS.prototype.init = function ()
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
	this.treeArc				= this.createNodeMap(this.graph);
	this.meta.dfsParent			= this.createNodeMap(this.graph);
	this.meta.dfsPathNumber		= this.createEdgeMap(this.graph);
	this.meta.dfsStartsNewPath	= this.createEdgeMap(this.graph);
	
	for (var n in this.graph.nodes)
	{
		this.meta.dfsParent[n]	= "invalidNode";
		this.treeArc[n]			= "invalidEdge";
	}
	
	for (var e in this.graph.edges)
	{
		this.meta.dfsPathNumber[e]		= -1;
		this.meta.dfsStartsNewPath[e]	= false;
	}
	
	
	// NumberDFS
	this.meta.dfsHighptLists	= this.createNodeMap(this.graph);
	this.meta.dfsNumV			= this.createNodeMap(this.graph);
	this.meta.dfsLowpt1Num		= this.createNodeMap(this.graph);
	this.meta.dfsLowpt2Num		= this.createNodeMap(this.graph);
	this.meta.dfsNumTreeEdges	= this.createNodeMap(this.graph);
	
	for (var n in this.graph.nodes)
	{
		this.meta.dfsHighptLists[n]		= new Array();
		this.meta.dfsNumV[n]			= -1;
		this.meta.dfsNumTreeEdges[n]	= -1;
	}
	
	this.m	= this.graph.nodeCount;	// TODO: check if nodeCount is correct
};

LinearTimeLayout.prototype.NumberDFS.prototype.postTraverse = function (edge, w)
{
	var v	= this.e(edge).getOtherVertex(w);
	
	this.m--;
	this.meta.dfsNumTreeEdges[v]	= this.meta.dfsNumTreeEdges[v] + 1;
};

LinearTimeLayout.prototype.NumberDFS.prototype.postVisit = function (node, dfsNumber, complNumber)
{
	this.meta.dfsLowpt1Num[node]	= this.meta.dfsNumV[this.meta.dfsLowpt1Vertex[node]];
	this.meta.dfsLowpt2Num[node]	= this.meta.dfsNumV[this.meta.dfsLowpt2Vertex[node]];
};

// treeEdge: boolean
LinearTimeLayout.prototype.NumberDFS.prototype.preTraverse = function (edge, w, treeEdge)
{
	// ParentAndPathDFS
	var v	= this.e(edge).getOtherVertex(w);
	if (treeEdge == true)
	{
		this.treeArc[w]					= edge;
		this.meta.dfsParent[w]			= v;
		this.meta.dfsPathNumber[edge]	= this.pathNumber;
		
		if (this.isNewPath === true)
		{
			this.meta.dfsStartsNewPath[edge]	= true;
			this.isNewPath						= false;
		}
	}
	else
	{
		this.meta.dfsPathNumber[edge]	= this.pathNumber;
		
		if (this.isNewPath === true)
		{
			this.meta.dfsStartsNewPath[edge]	= true;
		}
		
		this.pathNumber++;
		this.isNewPath	= true;
	}
	
	
	// NumberDFS
	if (treeEdge !== true)
	{
		this.meta.dfsHighptLists[w].push(v);
	}
};

LinearTimeLayout.prototype.NumberDFS.prototype.preVisit = function (node, dfsNumber)
{
	// NumberDFS
	this.meta.dfsNumV[node]				= this.m - this.meta.dfsNumDesc[node] + 1; 
	this.meta.dfsNumTreeEdges[node]		= 0;
};

LinearTimeLayout.prototype.NumberDFS.prototype.start = function (root)
{
	this.dfs(root);
};
