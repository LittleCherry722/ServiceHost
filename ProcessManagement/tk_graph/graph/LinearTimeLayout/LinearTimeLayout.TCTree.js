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
 * TCTree
 */

/**
 * The triconnected-components tree (TCTree)
 * 
 * @class TCTree
 * @see org.jbpt.algo.tree.tctree.TCTree
 * @param {Object} parent - Instance of LinearTimeLayout
 * @param {String} backedge - ID of the backedge between the graph's sink and its source.
 */
LinearTimeLayout.prototype.TCTree = function (parent, backedge)
{
	/**
	 * Adjacency list.
	 * @memberof! TCTree
	 * @type {Array}
	 */
	this.adjacency		= {};

	/**
	 * Backedge between graph's sink and its source node.
	 * @memberof! TCTree
	 * @type {Object}
	 */
	this.backedge	= parent.normGraph.edges[backedge];
	
	/**
	 * ID of backedge between graph's sink and its source node.
	 * @memberof! TCTree
	 * @type {String}
	 */
	this.backedgeID		= backedge;
	
	/**
	 * ID of last added edge.
	 * @memberof! TCTree
	 * @type {int}
	 */
	this.edgeID			= 0;
	
	/**
	 * Edges of the TCTree.
	 * @memberof! TCTree
	 * @type {Array}
	 */
	this.edges			= {};
	
	/**
	 * Normalized graph.
	 * @memberof! TCTree
	 * @type {Object} 
	 */
	this.graph			= parent.normGraph;
	
	/**
	 * Incidence lists.
	 * @memberof! TCTree
	 * @type {Array}
	 */
	this.incidentEdges	= {};
	
	/**
	 * ID of last added node.
	 * @memberof! TCTree
	 * @type {int}
	 */
	this.nodeID			= 0;
	
	/**
	 * Nodes of the TCTree.
	 * @memberof! TCTree
	 * @type {Array}
	 */
	this.nodes			= {};
	
	/**
	 * Instance of LinearTimeLayout.
	 * @memberof! TCTree
	 * @type {Object}
	 */
	this.parent			= parent;
	
	/**
	 * Parents of nodes within the TCTree.
	 * @memberof! TCTree
	 * @type {Array}
	 */
	this.parents		= {};
	
	/**
	 * ID of the tree's root.
	 * @memberof! TCTree
	 * @type {String}
	 */
	this.root			= null;
	
	// construct tree
	this.construct();
};

/*
 * TCTree Methods
 */

/**
 * Add edge to TCTree.
 * 
 * @memberof! TCTree
 * @param {String} v1 - ID of parent triconnected component.
 * @param {String} v2 - ID of child triconnected component.
 * @returns {void}
 */
LinearTimeLayout.prototype.TCTree.prototype.addEdge = function (v1, v2)
{
	var id		= "e" + this.edgeID++;
	var edge	= new this.parent.BasicEdge(id, this.nodes[v1], this.nodes[v2]);
	this.edges[id]	= edge;
	
	// add edge to adjacency
	if (!gf_isset(this.adjacency[v1]))
		this.adjacency[v1]	= {};
		
	this.adjacency[v1][id]	= id;
	
	// add parent
	this.parents[v2]	= v1;
	
	// add incident edges
	if (!gf_isset(this.incidentEdges[v1]))
		this.incidentEdges[v1]	= {};
		
	if (!gf_isset(this.incidentEdges[v2]))
		this.incidentEdges[v2]	= {};
		
	this.incidentEdges[v1][id]	= edge;
	this.incidentEdges[v2][id]	= edge;
};

/**
 * Add node to TCTree.
 * 
 * @memberof! TCTree
 * @param {Object} node - Triconnected component.
 * @returns {void}
 */
LinearTimeLayout.prototype.TCTree.prototype.addNode = function (node)
{
	var id	= "n" + this.nodeID++;
	node.id	= id;
	this.nodes[id]	= node;
};

/**
 * Checks if the given node is the root of the TCTree.
 * 
 * @memberof! TCTree
 * @param {Object} node - A TCTreeNode (triconnected component).
 * @returns {boolean} True if node is root of the TCTree, false otherwise.
 */
LinearTimeLayout.prototype.TCTree.prototype.checkRoot = function (node)
{
	return gf_isset(node.skeleton.o2e[this.backedgeID]);
};

/**
 * Construct the tree of triconnected-components (TCTree).
 * 
 * @memberof! TCTree
 * @returns {void}
 */
LinearTimeLayout.prototype.TCTree.prototype.construct = function ()
{
	var components	= new Array();
	
	var virtualEdgeMap					= this.createEdgeMap(this.graph, false);
		virtualEdgeMap[this.backedgeID]	= true;

	var assignedVirtEdgeMap				= this.createEdgeMap(this.graph, null);
	var isHiddenMap						= this.createEdgeMap(this.graph, false);
	
	var meta							= {};
		meta.virtEdges					= virtualEdgeMap;
		meta.assignedVirtEdges			= assignedVirtEdgeMap;
		meta.hiddenEdges				= isHiddenMap;
		meta.edges						= {};
		
	var mainSkeleton					= new this.parent.TCTreeSkeleton(this.parent);
		mainSkeleton.addAll(this.graph);
		
	for (var e in mainSkeleton.edges)
	{
		meta.edges[e]	= mainSkeleton.edges[e];
	}
		
	this.splitOffInitialMultipleEdges(mainSkeleton, components, meta);
	this.findSplitComponents(mainSkeleton, components, meta, this.backedge.v1);
	
	// 1) create TCTreeNodes and TCTreeSkeletons
	for (var c in components)
	{
		var component	= components[c];
		
		if (component.length <= 1)
		{
			continue;
		}
		
		var node		= new this.parent.TCTreeNode(this.parent);
		for (var e in component)
		{
			var edge	= component[e];
			if (meta.virtEdges[edge.id] == true)
			{
				node.skeleton.addVirtualEdge(edge);
			}
			else
			{
				node.skeleton.addEdge(edge);
			}
		}
		this.addNode(node);
	}
	
	// 2) classify components (bond, polygon, rigid)
	for (var n in this.nodes)
	{
		var node	= this.nodes[n];
		if (node.skeleton.nodeCount == 2)
		{
			node.type	= "bond";
		}
		else
		{
			var isPolygon	= true;
			for (var n in node.skeleton.nodes)
			{
				var vertex			= node.skeleton.nodes[n];
				if (node.skeleton.incidentEdges[vertex].length != 2)
				{
					isPolygon	= false;
					break;
				}
			}
			
			if (isPolygon)
				node.type	= "polygon";
			else
				node.type	= "rigid";
		}
	}
	
	// 3) index components
	var ve2nodes	= {};
	for (var n in this.nodes)
	{
		var node	= this.nodes[n];
		for (var edge in node.skeleton.virtualEdges)
		{
			if (!gf_isset(ve2nodes[edge]))
			{
				ve2nodes[edge]	= {};
			}
			ve2nodes[edge][node.id] = node;
		}
	}
	
	// 4) merge polygons and bonds
	var toRemove	= new Array();
	for (var entryA in ve2nodes)
	{
		// get the first two TCTreeNodes
		var v1	= null;
		var v2	= null;
		var i	= 1;
		
		// v1 = iterator.next(), v2 = iterator.next()
		for (var v in ve2nodes[entryA])
		{
			if (i == 1)
			{
				v1	= ve2nodes[entryA][v];
			}
			
			if (i == 2)
			{
				v2	= ve2nodes[entryA][v];
				break;
			}
			
			i++;
		}
		
		if (v1.type != v2.type)
			continue;
			
		if (v1.type == "rigid")
			continue;
			
		for (var e in v2.skeleton.edges)
		{
			var edge	= v2.skeleton.edges[e];
			if (v2.skeleton.isVirtual(edge))
			{
				if (edge.id != entryA)
				{
					v1.skeleton.addVirtualEdge(edge);
				}
			}
			else
			{
				v1.skeleton.addEdge(edge);
			}
		}
		
		var ves	= v1.skeleton.virtualEdges;
		for (var ve in ves)
		{
			var vedge	= ves[ve];
			if (vedge.id == entryA)
			{
				v1.skeleton.removeEdge(vedge);
			}
		}
		
		for (var entryB in ve2nodes)
		{
			// if entryB.contains(v2)
			if (gf_isset(ve2nodes[entryB][v2.id]))
			{
				delete ve2nodes[entryB][v2.id];
				ve2nodes[entryB][v1.id]	= v1;
				
				// count elements of entryB (count to 2 is sufficient)
				var entryBcount	= 0;
				for (var eBc in ve2nodes[entryB])
				{
					entryBcount++;
					if (entryBcount > 1)
						break;
				}
				
				// if entryB.size() == 1
				if (entryBcount == 1)
				{
					toRemove.push(entryB);
				}
			}
		}
		
		this.removeNode(v2.id);
	}
	
	for (var ve in toRemove)
	{
		var vedge	= toRemove[ve];
		delete ve2nodes[vedge];
	}
	
	// 5) name components
	var Bc	= 0;
	var Pc	= 0;
	var Rc	= 0;
	for (var n in this.nodes)
	{
		var node	= this.nodes[n];
		if (node.type == "bond")
			node.name	= "B" + Bc++;
		if (node.type == "polygon")
			node.name	= "P" + Pc++;
		if (node.type == "rigid")
			node.name	= "R" + Rc++;
	}	
	
	// 6) construct tree
	var nodeSize	= 0;
	var node1		= null;
	for (var n in this.nodes)
	{
		if (nodeSize == 0)
		{
			node1	= this.nodes[n];
		}
		nodeSize++;
		
		if (nodeSize > 1)
		{
			break;
		}
	}
	
	var tobeRoot	= nodeSize == 1 ? node1 : null;
	var visited		= {};
	for (var entry in ve2nodes)
	{
		// get the first two TCTreeNodes
		var v1	= null;
		var v2	= null;
		var i	= 1;
		
		// v1 = iterator.next(), v2 = iterator.next()
		for (var v in ve2nodes[entry])
		{
			if (i == 1)
			{
				v1	= ve2nodes[entry][v];
			}
			
			if (i == 2)
			{
				v2	= ve2nodes[entry][v];
				break;
			}
			
			i++;
		}
		this.addEdge(v1.id, v2.id);
		
		if (tobeRoot == null && !gf_isset(visited[v1.id]))
		{
			if (this.checkRoot(v1))
			{
				tobeRoot	= v1;
			}
		}
		
		visited[v1.id]	= true;
		if (tobeRoot == null && !gf_isset(visited[v2.id]))
		{
			if (this.checkRoot(v2))
			{
				tobeRoot	= v2;
			}
		}
		
		visited[v2.id]	= true;
	}
	
	// trivial fragments
	for (var n in this.nodes)
	{
		var node	= this.nodes[n];
		for (var o in node.skeleton.o2e)
		{
			var edge	= node.skeleton.o2e[o];
			var oEdge	= this.parent.normGraph.edges[o];
				oEdge.orgId	= o;		// backup org id
			var trivial	= new this.parent.TCTreeNode(this.parent);
			trivial.type	= "trivial";
			trivial.name	= oEdge.toString();
			trivial.skeleton.addEdge(oEdge);
			
			this.addNode(trivial);
			this.addEdge(node.id, trivial.id);
		}
	}
	
	// only reroot when node is set
	if (tobeRoot != null)
	{
		this.reRoot(tobeRoot.id);
	}
};

/**
 * Create empty or preset edge map to store virtual edges, etc.
 * 
 * @memberof! TCTree
 * @param {Object} graph - The graph for which to create the map.
 * @param {mixed} initialValue - Initial value that is assigned to each edge. (default: null)
 * @returns {Array} Array of edge IDs, each entry set to null or to the initialValue.
 */
LinearTimeLayout.prototype.TCTree.prototype.createEdgeMap = function (graph, initialValue)
{
	var map	= {};
	
	for (var edgeID in graph.edges)
	{
		map[edgeID]	= gf_isset(initialValue) ? initialValue : null;
	}
	
	return map;
};

/**
 * Create empty node map to store adjacency lists.
 * 
 * @memberof! TCTree
 * @param {Object} graph - The graph for which to create the map.
 * @returns {Array} Array of node IDs, each entry set to null.
 */
LinearTimeLayout.prototype.TCTree.prototype.createNodeMap = function (graph)
{
	var map	= {};
	for (var v in graph.nodes)
	{
		map[v]	= null;
	}
	return map;
};

/**
 * Checks if the tree does not contain any edges.
 * 
 * @memberof! TCTree
 * @returns {boolean} True if edge list of tree is empty, false otherwise.
 */
LinearTimeLayout.prototype.TCTree.prototype.edgesEmpty = function ()
{
	var isEmpty	= true;
	for (var e in this.edges)
	{
		isEmpty	= false;
		break;
	}
	return isEmpty;
};

/**
 * Find split components of tree.
 * 
 * @memberof! TCTree
 * @param {Object} graph - The skeleton of the tree.
 * @param {Array} components - Pointer to resulting array of triconnected components.
 * @param {Object} meta - Additional meta data, e.g. adjacency lists, etc.
 * @param {Object} root - The root node to start the DFSs with.
 * @returns {void}
 */
LinearTimeLayout.prototype.TCTree.prototype.findSplitComponents = function (graph, components, meta, root)
{
	var adjMap	= this.createNodeMap(graph);
	for (var v in graph.nodes)
	{
		var adj	= new Array();
		for (var e in graph.incidentEdges[v])
		{
			var edge	= graph.incidentEdges[v][e];
			adj.push(edge);
		}
		adjMap[v]	= adj;
	}
	meta.dfsAdjLists	= adjMap;
	
	var dfs1	= new this.parent.LowAndDescDFS(graph, meta);
		dfs1.start(root.id);
	
	var orderedAdjMap		= this.orderAdjLists(graph, meta);
	var copiedOrderedAdjMap	= new Array();
	for (var o in orderedAdjMap)
	{
		var oam	= orderedAdjMap[o];
		copiedOrderedAdjMap[o]	= new Array();
		for (var oa in oam)
		{
			copiedOrderedAdjMap[o][oa]	= oam[oa];
		}
	}

	var dfs2	= new this.parent.NumberDFS(graph, meta, copiedOrderedAdjMap);
		dfs2.start(root.id);
	
	var edgeCount	= {};
	for (var v in graph.nodes)
	{
		edgeCount[v]	= graph.incidentEdges[v].length;
	}
	meta.dfsEdgeCount	= edgeCount;
	
	var dfs3	= new this.parent.SplitCompDFS(graph, meta, copiedOrderedAdjMap, components, dfs2.treeArc);
		dfs3.start(root.id);
};

/**
 * Get adjacent nodes for a given node.
 * 
 * @memberof! TCTree
 * @param {String} nodeID - ID of the node to get the adjacent nodes for.
 * @returns {Array} Array of adjacent nodes.
 */
LinearTimeLayout.prototype.TCTree.prototype.getAdjacent = function (nodeID)
{	
	if (!gf_isset(this.incidentEdges[nodeID]))
		return {};
		
	var adjacentNodes	= new Array();
	
	for (var i in this.incidentEdges[nodeID])
	{
		var	iEdge	= this.incidentEdges[nodeID][i];
		var edge	= iEdge;
		
		if (nodeID != edge.v1.id)
		{
			adjacentNodes.push(edge.v1.id);
		}
		
		if (nodeID != edge.v2.id)
		{
			adjacentNodes.push(edge.v2.id);
		}
	}
		
	return adjacentNodes;
};

/**
 * Get children of a node within the TCTree.
 * 
 * @memberof! TCTree
 * @param {String} node - ID of node.
 * @returns {Array} Child nodes within the TCTree.
 */
LinearTimeLayout.prototype.TCTree.prototype.getChildren = function (node)
{
	// return direct child nodes
	var childNodes	= new Array();

	for (var e in this.edges)
	{
		var edge	= this.edges[e];
		var s		= edge.v1.id;
		var t		= edge.v2.id;
		
		if (s == node)
		{
			childNodes.push(t);
		}
	}
	
	return childNodes;
};

/**
 * Get all edges connecting two given nodes.
 * 
 * @memberof! TCTree
 * @param {String} v1 - ID of first node.
 * @param {String} v2 - ID of second node.
 * @returns {Array} Array of edges connecting both nodes.
 */
LinearTimeLayout.prototype.TCTree.prototype.getEdges = function (v1, v2)
{
	var result	= new Array();
	for (var e in this.edges)
	{
		if (this.edges[e].connectsVertices(v1, v2))
		{
			result.push(e);
		}
	}
	
	return result;
};

/**
 * Get parent for the given node.
 * 
 * @memberof! TCTree
 * @param {String} node - ID of a node.
 * @returns {String} ID of the node's parent within the TCTree.
 */
LinearTimeLayout.prototype.TCTree.prototype.getParent = function (node)
{
	return this.parents[node];
};

/**
 * Checks if a given node is the root of the TCTree.
 * 
 * @memberof! TCTree
 * @param {String} node - ID of a node.
 * @returns {boolean} True if the given node is the root of the TCTree, false otherwise.
 */
LinearTimeLayout.prototype.TCTree.prototype.isRoot = function (node)
{
	return this.root == node;
};

/**
 * Create a new component.
 * 
 * @memberof! TCTree
 * @param {Object} skeleton - Link to the tree's skeleton.
 * @param {Array} components - List of components to which the new component is added.
 * @param {Array} tempComp - Edges of the component.
 * @param {Object} meta - Additional meta data.
 * @param {Object} lastEdge - Last edge of component.
 * @returns {void}
 */
LinearTimeLayout.prototype.TCTree.prototype.newComponent = function (skeleton, components, tempComp, meta, lastEdge)
{
	for (var e in tempComp)
	{
		var edge	= tempComp[e];
		skeleton.removeEdge(edge.id);
		meta.hiddenEdges[edge.id]	= true;
	}
	
	var virtualEdge	= skeleton.createVirtualEdge(lastEdge.v1, lastEdge.v2);
	meta.virtEdges[virtualEdge.id]	= true;
	tempComp.unshift(virtualEdge);
	
	for (var e in tempComp)
	{
		var edge	= tempComp[e];
		meta.assignedVirtEdges[edge, virtualEdge.id];
	}
	
	components.push(tempComp);
};

/**
 * Order the adjacency lists depending on the edge's type and additional meta data.
 * 
 * @memberof! TCTree
 * @param {Object} graph - The graph for which to order the adjacency lists.
 * @param {Object} meta - Additional meta data used for the ordering.
 * @returns {Array} Ordered adjacency lists.
 */
LinearTimeLayout.prototype.TCTree.prototype.orderAdjLists = function (graph, meta)
{
	var edges		= graph.edges;
	var bucket		= new Array();
	var bucketSize	= 3 * graph.nodeCount + 2;
	for (var i = 0; i < bucketSize; i++)
	{
		bucket.push(new Array());
	}
	
	var phi	= 0;
	for (var e in edges)
	{
		phi	= -1;
		var edge	= edges[e];
		
		if (meta.dfsEdgeType[e] == "treeedge")
		{
			if (meta.dfsLowpt2Num[edge.v2.id] < meta.dfsNum[edge.v1.id])
			{
				phi	= 3 * meta.dfsLowpt1Num[edge.v2.id];
			}
			else
			{
				phi	= 3 * meta.dfsLowpt1Num[edge.v2.id] + 2;
			}
		}
		else
		{
			phi	= 3 * meta.dfsNum[edge.v2.id] + 1;
		}
		
		bucket[phi - 1].push(edge);
	}
	
	var orderedAdjMap	= this.createNodeMap(graph);
	for (var n in graph.nodes)
	{
		orderedAdjMap[n]	= new Array();
	}
	meta.dfsOrderedAdjLists	= orderedAdjMap;
	
	for (var b in bucket)
	{
		var el	= bucket[b];
		for (var e in el)
		{
			var edge	= el[e];
			orderedAdjMap[edge.v1.id].push(edge.id);
		}
	}
	
	return orderedAdjMap;
	
};

/**
 * Remove edges from TCTree.
 * 
 * @memberof! TCTree
 * @param {Array} edges - Array of edges to remove.
 * @returns {void}
 */
LinearTimeLayout.prototype.TCTree.prototype.removeEdges = function (edges)
{
	for (var e in edges)
	{
		var edge	= edges[e];
		
		if (!(edge instanceof LinearTimeLayout.prototype.BasicEdge))
		{
			edge	= this.edges[edge];
		}
		
		// edge: edgeID
		delete this.edges[edge.id];
		delete this.incidentEdges[edge.v1.id][edge.id];
		delete this.incidentEdges[edge.v2.id][edge.id];
	}
};

/**
 * Remove node from TCTree.
 * 
 * @memberof! TCTree
 * @param {String|Object} node - Either ID or actual node.
 * @returns {void}
 */
LinearTimeLayout.prototype.TCTree.prototype.removeNode = function (node)
{
	if (gf_isset(node.id))
		node	= node.id;
		
	if (gf_isset(this.nodes[node]))
	{
		delete this.nodes[node];
		
		// delete incident edges
		for (var ie in this.incidentEdges[node])
		{
			var edge	= this.incidentEdges[node][ie];
			if (gf_isset(this.edges[edge.id]))
			{
				var v1		= edge.v1.id;
				var v2		= edge.v2.id;
				
				delete this.incidentEdges[v1][edge.id];
				delete this.incidentEdges[v2][edge.id];
				delete this.edges[edge.id];
			}
		}
	}
};

/**
 * Remove multiple nodes from the TCTree.
 * 
 * @memberof! TCTree
 * @param {Array} nodes - List of nodes to remove.
 * @returns {void}
 */
LinearTimeLayout.prototype.TCTree.prototype.removeNodes = function (nodes)
{
	for (var n in nodes)
	{
		this.removeNode(nodes[n]);
	}
};

/**
 * Reorganize the TCTree to set a new root node.
 * 
 * @memberof! TCTree
 * @param {String} node - ID of the node to set new root.
 * @returns {String} ID of new root node.
 */
LinearTimeLayout.prototype.TCTree.prototype.reRoot = function (node)
{
	if (node == null || !gf_isset(this.nodes[node]))
		return this.root;
		
	if (this.root == node)
		return this.root;
		
	this.root	= node;
	
	var queue	= new Array();
	var visited	= {};
	
	queue.push(this.root);
	visited[this.root]	= this.root;
	
	// while queue not empty
	while (queue.length > 0)
	{
		
		var c	= queue.shift();	// FIFO
		
		var adjacentNodes	= this.getAdjacent(c);
		
		// cycle through adjacent nodes; ignore already visited nodes
		for (var a in adjacentNodes)
		{
			var node	= adjacentNodes[a];
			if (!gf_isset(visited[node]))
			{
				this.removeEdges(this.getEdges(c,node));
				
				this.addEdge(c, node);
				visited[node]	= node;
				queue.push(node);
			}
		}
	}
	
	return this.root;
};

/**
 * Sort edges so that multiple edges are ordered consecutively.
 * 
 * @memberof! TCTree
 * @param {Object} graph - The skeleton of the TCTree.
 * @returns {Array} Ordered list of edges.
 */
LinearTimeLayout.prototype.TCTree.prototype.sortConsecutiveMultipleEdges = function (graph)
{
	var indices	= {};
	var count	= 0;
	
	for (var vertex in graph.nodes)
	{
		indices[vertex]	= count++;
	}
	
	var edges	= graph.edges;	
	var bucket	= new Array();
	for (var i = 0; i < count; i++)
	{
		bucket.push(new Array());
	}
	
	var sum	= 0;
	for (var edge in edges)
	{
		var e	= edges[edge];
		var i	= Math.min(indices[e.v1.id], indices[e.v2.id]);
		bucket[i].push(e);
	}
	
	var sortedEdges	= new Array();
	for (var l in bucket)
	{
		var list	= bucket[l];
		var map		= [];
		for (var edge in list)
		{
			var e	= list[edge];
			var i	= indices[e.v1.id] + indices[e.v2.id];
			
			if (!gf_isset(map[i]))
			{
				map[i]	= new Array();
			}
			
			map[i].push(e);
		}
		
		for (var el in map)
		{
			for (var edge in map[el])
			{
				var e	= map[el][edge];
				sortedEdges.push(e);
			}
		}
	}
	
	return sortedEdges;
};

/**
 * Remove multiple edges by creating components.
 * 
 * @memberof! TCTree
 * @param {Object} graph - Skeleton of TCTree.
 * @param {Array} components - Pointer to the components array which holds the newly created components.
 * @param {Object} meta - Additional meta data.
 * @returns {void}
 */
LinearTimeLayout.prototype.TCTree.prototype.splitOffInitialMultipleEdges = function (graph, components, meta)
{
	var edges			= this.sortConsecutiveMultipleEdges(graph);
	var tempComp		= new Array();
	var lastEdge		= null;
	var currentEdge		= null;
	var tempCompSize	= 0;
	
	for (var edge in edges)
	{
		var e	= edges[edge];
		
		currentEdge	= e;
		
		if (lastEdge != null)
		{
			if (	currentEdge.v1.id == lastEdge.v1.id && currentEdge.v2.id == lastEdge.v2.id ||
					currentEdge.v1.id == lastEdge.v2.id && currentEdge.v2.id == lastEdge.v1.id)
			{
				tempComp.push(lastEdge);
				tempCompSize++;
			}
			else
			{
				if (tempCompSize > 0)
				{
					tempComp.push(lastEdge);
					this.newComponent(graph, components, tempComp, meta, lastEdge);
					tempComp		= new Array();
					tempCompSize	= 0;
				}
			}
		}
		lastEdge	= currentEdge;
	}
	
	if (tempCompSize > 0)
	{
		tempComp.push(lastEdge);
		this.newComponent(graph, components, tempComp, meta, lastEdge);
	}
};