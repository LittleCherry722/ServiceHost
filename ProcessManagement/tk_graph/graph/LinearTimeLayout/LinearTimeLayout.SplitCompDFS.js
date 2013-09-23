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
 * SplitCompDFS
 */
LinearTimeLayout.prototype.SplitCompDFS = function (graph, meta, adjMap, components, treeArc)
{
	this.adj					= adjMap;
	this.complNum				= 0;
	this.components				= components;
	this.dfsNum					= 0;
	this.dfsRoot				= null;
	this.eStack					= new Array();
	this.graph					= graph;
	this.meta					= meta;
	this.numNotVisitedTreeEdges	= null;
	this.treeArc				= treeArc;
	this.tStack					= new Array();
	
	this.init();
};

/*
 * SplitCompDFS Attributes
 */

/*
 * SplitCompDFS Methods
 */
LinearTimeLayout.prototype.SplitCompDFS.prototype.addToComponent = function (el, component)
{
	var edges	= el;
	if (!(el instanceof Array))
	{
		edges	= new Array();
		edges.push(el);
	}
	
	this.removeEdges(edges);
	
	for (var e in edges)
	{
		var edge	= this.e(edges[e]);		
		component.push(edge);
	}
	
	return edge;
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.assignVirtualEdge = function (component, virtualEdge)
{
	for (var c in component)
	{
		var edge	= component[c];
		
		edge	= this.e(edge);
		
		this.meta.assignedVirtEdges[edge.id]	= virtualEdge.id;
		component[c]	= edge;
	}
};


LinearTimeLayout.prototype.SplitCompDFS.prototype.checkType1 = function (eBacktrack, v, w)
{
	if (this.getL2Num(w) >= this.getNum(v) && this.getL1Num(w) < this.getNum(v) && (this.meta.dfsParent[v] != this.dfsRoot || this.numNotVisitedTreeEdges[v] > 0))
	{
		var lowpt1W		= this.meta.dfsLowpt1Vertex[w];
		var c			= this.newComponent(new Array());
		var virtEdge	= null;
		var numW		= this.getNum(w);
		var h			= numW + this.getNumDesc(w) - 1;
		var e			= null;
		
		if (!this.isEmpty(this.eStack))
		{
			e	= this.peek(this.eStack);
		}
		
		while (	!this.isEmpty(this.eStack) &&
				(	
					(numW <= this.getNum(this.e(e).v1.id) && this.getNum(this.e(e).v1.id) <= h) ||
					(numW <= this.getNum(this.e(e).v2.id) && this.getNum(this.e(e).v2.id) <= h)
				)
			)
		{
			e	= this.eStack.shift();
			this.addToComponent(e, c);
			
			if (!this.isEmpty(this.eStack))
			{
				e	= this.peek(this.eStack);
			}
		}
		
		virtEdge	= this.newVirtualEdge(c, v, lowpt1W);
		
		if (!this.isEmpty(this.eStack))
		{
			e	= this.peek(this.eStack);
			if (this.isSameEdge(e, v, lowpt1W))
			{
				var el	= new Array();
				
				e	= this.eStack.shift();
				el.push(e);
				el.push(virtEdge);
				
				c			= this.newComponent(el);
				virtEdge	= this.newVirtualEdge(c, v, lowpt1W);
			}
		}
		
		if (lowpt1W != this.meta.dfsParent[v])
		{
			this.eStack.unshift(virtEdge);
		}
		else
		{
			var treeArcOfV	= this.treeArc[v];
			
			var el			= new Array();
				el.push(treeArcOfV);
				el.push(virtEdge);
				
			c			= this.newComponent(el);
			virtEdge	= this.newVirtualEdge(c, lowpt1W, v);
			this.treeArc[v]	= virtEdge;
		}
		
		this.meta.dfsOrderedAdjLists[v].push(virtEdge);
		this.makeTreeEdge(virtEdge, lowpt1W, v);
	}
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.checkType2 = function (eBacktrack, v, w)
{
	var topTriple	= null;
	if (!this.isEmpty(this.tStack))
	{
		topTriple	= this.top(this.tStack);
	}
	
	var adjOfW			= this.meta.dfsOrderedAdjLists[w];
	var firstChildOfW	= null;
	if (!this.isEmpty(adjOfW))
	{
		firstChildOfW	= this.e(this.peek(adjOfW)).getOtherVertex(w);
		// firstChildOfW	= this.e(adjOfW[0]).getOtherVertex(w);
	}
	var edgeCountOfW	= this.meta.dfsEdgeCount[w];
	
	while (	v != this.dfsRoot &&
			(
				(topTriple != null && topTriple.a == v) ||
				(edgeCountOfW == 2 && firstChildOfW != null && this.getNum(firstChildOfW) > this.getNum(w))
			)
		)
	{
		var eAB		= new Array();
		if (topTriple.a == v && this.meta.dfsParent[topTriple.b] == topTriple.a)
		{
			this.tStack.pop();
			
			if (!this.isEmpty(this.tStack))
			{
				topTriple	= this.top(this.tStack);
			}
			else
			{
				topTriple	= null;
			}
		}
		else
		{
			var c			= this.newComponent(new Array());
			var virtEdge	= null;
			
			if (edgeCountOfW == 2 && firstChildOfW != null && this.getNum(firstChildOfW) > this.getNum(w))
			{
				var e	= this.eStack.shift();
				var el	= new Array();
					el.push(e);
					e	= this.eStack.shift();
					el.push(e);
					
				this.addToComponent(el, c);
				
				virtEdge	= this.newVirtualEdge(c, v, firstChildOfW);
				
				if (!this.isEmpty(this.eStack))
				{
					e	= this.peek(this.eStack);
					
					if (this.isSameEdge(e, v, topTriple.b) || this.isSameEdge(e, v, firstChildOfW))
					{
						eAB.push(this.eStack.shift());
					}
				}
			}
			else
			{
				topTriple	= this.tStack.pop();
				var e		= null;
				
				if (!this.isEmpty(this.eStack))
				{
					e	= this.peek(this.eStack);
				}
				
				while (	e != null && topTriple.numA <= this.getNum(this.e(e).v1.id) && topTriple.numA <= this.getNum(this.e(e).v2.id) &&
						this.getNum(this.e(e).v1.id) <= topTriple.numH && this.getNum(this.e(e).v2.id) <= topTriple.numH
					)
				{
					e	= this.eStack.shift();
					if (this.isSameEdge(e, topTriple.a, topTriple.b))
					{
						eAB.push(e);
					}
					else
					{
						this.addToComponent(e, c);
					}
					
					if (!this.isEmpty(this.eStack))
					{
						e	= this.peek(this.eStack);
					}
					else
					{
						e	= null;
					}
				}
				
				virtEdge	= this.newVirtualEdge(c, topTriple.a, topTriple.b);
			}
			
			if (!this.isEmpty(eAB))
			{
				eAB.push(virtEdge);
				c		= this.newComponent(eAB);
				var b	= null;
				
				if (topTriple.b == "invalidNode" || (firstChildOfW != null && this.isSameEdge(this.peek(eAB), v, firstChildOfW)))
				{
					b	= firstChildOfW;
				}
				else
				{
					b	= topTriple.b;
				}
				
				virtEdge	= this.newVirtualEdge(c, v, b);
			}
			
			w	= virtEdge.getOtherVertex(v);
			
			this.eStack.unshift(virtEdge);
			this.makeTreeEdge(virtEdge, v, w);
			
			this.meta.dfsParent[w]	= v;
			
			if (!this.isEmpty(this.tStack))
			{
				topTriple	= this.top(this.tStack);
			}
			else
			{
				topTriple	= null;
			}
			
			adjOfW	= this.meta.dfsOrderedAdjLists[w];
			if (!this.isEmpty(adjOfW))
			{
				firstChildOfW	= this.e(this.peek(adjOfW)).getOtherVertex(w);
				// firstChildOfW	= this.e(adjOfW[0]).getOtherVertex();
			}
			edgeCountOfW	= this.meta.dfsEdgeCount[w];
		}
	}
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.createEdgeMap = function (graph)
{
	var map	= {};
	
	for (var edgeID in graph.edges)
	{
		map[edgeID]	= null;
	}
	
	return map;
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.createNodeMap = function (graph)
{
	var map	= {};
	for (var v in graph.nodes)
	{
		map[v]	= null;
	}
	return map;
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.createTStackEntry = function (h, a, b)
{
	var numA	= this.getNum(a);
	var numB	= this.getNum(b);
	
	return {"a": a, "b": b, "numH": h, "numA": numA, "numB": numB};
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.dfs = function (node)
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

LinearTimeLayout.prototype.SplitCompDFS.prototype.e = function (edge)
{
	var e	= edge;
	if (!this.isBasicEdge(e))
	{
		if (gf_isset(this.graph.edges[e]))
		{
			e	= this.graph.edges[e];
		}
		else if (gf_isset(this.graph.deletedEdges[e]))
		{
			e	= this.graph.deletedEdges[e];
		}
	}
	return e;
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.getHNum = function (node)
{
	var num	= 0;
	if (!this.isEmpty(this.meta.dfsHighptLists[node]))
	{
		num	= this.getNum(this.meta.dfsHighptLists[node][0]);
	}
	
	return num;
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.getL1Num = function (node)
{
	return this.meta.dfsLowpt1Num[node];
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.getL2Num = function (node)
{
	return this.meta.dfsLowpt2Num[node];
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.getNum = function (node)
{
	return this.meta.dfsNumV[node];
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.getNumDesc = function (node)
{
	return this.meta.dfsNumDesc[node];
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.init = function ()
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
	
	// SplitCompDFS
	this.numNotVisitedTreeEdges	= this.createNodeMap(this.graph);
	
	for (var n in this.graph.nodes)
	{
		this.numNotVisitedTreeEdges[n]	= this.meta.dfsNumTreeEdges[n];
		this.retainAll(this.meta.dfsAdjLists[n], this.adj[n]);
	}
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.isBasicEdge = function (edge)
{
	return edge instanceof LinearTimeLayout.prototype.BasicEdge;
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.isEmpty = function (array)
{
	var arrayCount	= 0;
	for (var a in array)
	{
		arrayCount++;
		break;
	}
	
	return arrayCount == 0;
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.isSameEdge = function (edge, v, w)
{
	var e	= this.e(edge);
	return (e.v1.id == v && e.v2.id == w || e.v1.id == w && e.v2.id == v);
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.makeTreeEdge = function (edge, v, w)
{
	this.e(edge).changeOrientation(v, w);
	this.meta.dfsEdgeType[edge]	= "treeedge";
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.newComponent = function (el)
{
	console.log(el.length);
	
	for (var e in el)
	{
		el[e]	= this.e(el[e]);
	}
	
	this.removeEdges(el);
	this.components.push(el);
	return el;
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.newVirtualEdge = function (component, v, w)
{
	var virtualEdge	= this.graph.createVirtualEdge(v, w);
	this.updateEdgeCount(v, 1);
	this.updateEdgeCount(w, 1);
	
	this.meta.virtEdges[virtualEdge.id]	= true;
	component.unshift(virtualEdge.id);
	
	this.assignVirtualEdge(component, virtualEdge);
	
	this.meta.dfsOrderedAdjLists[v].push(virtualEdge.id);
	
	// hack
	this.meta.edges[virtualEdge.id]	= virtualEdge;
	
	return virtualEdge;
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.peek = function (array)
{
	var temp	= null;
	if (!this.isEmpty(array))
	{
		temp	= array.shift();
		array.unshift(temp);
	}
	return temp;
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.postTraverse = function (edge, w)
{
	if (this.isBasicEdge(edge))
		edge	= edge.id;
	
	var v	= this.e(edge).getOtherVertex(w);
	
	if (this.meta.hiddenEdges[edge])
	{
		var eToPush	= this.meta.assignedVirtEdges[edge];
		
		while (this.meta.hiddenEdges[eToPush] == true)
		{
			eToPush	= this.meta.assignedVirtEdges[eToPush];
		}
		
		this.eStack.unshift(eToPush);
	}
	else
	{
		this.eStack.unshift(edge);
	}
	
	this.checkType2(edge, v, w);
	this.checkType1(edge, v, w);
	
	if (this.meta.dfsStartsNewPath[edge] == true)
	{
		while (!this.isEmpty(this.tStack) && this.top(this.tStack) != "eos")
		{
			this.tStack.pop();
		}
		
		if (!this.isEmpty(this.tStack))
		{
			// remove "eos"
			this.tStack.pop();
		}
	}
	
	if (!this.isEmpty(this.tStack))
	{
		var i		= this.top(this.tStack);
		var highV	= this.getHNum(v);
		while (i != "eos" && i.a != v && i.b != v && highV > i.numH)
		{
			this.tStack.pop();
			i	= this.top(this.tStack);
		}
	}
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.postVisit = function (node, dfsNumber, complNumber)
{
	
};

// treeEdge: boolean
LinearTimeLayout.prototype.SplitCompDFS.prototype.preTraverse = function (edge, w, treeEdge)
{
	var v	= this.e(edge).getOtherVertex(w);
	
	// SplitCompDFS
	this.numNotVisitedTreeEdges[v]	= this.numNotVisitedTreeEdges[v] - 1;
	
	if (this.meta.dfsStartsNewPath[edge] == true)
	{
		this.updateTStack(v, w, treeEdge);
	}
	
	if (treeEdge != true)
	{
		if (w == this.meta.dfsParent[v])
		{
			
			var el	= new Array();
				el.push(edge);
				el.push(this.treeArc[v]);
				
			var c			= this.newComponent(el);
			var virtEdge	= this.newVirtualEdge(c, w, v);
			this.makeTreeEdge(virtEdge, w, v);
		}
		else
		{
			this.eStack.unshift(edge);
		}
	}
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.preVisit = function (node, dfsNumber)
{
	
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.removeEdges = function (edges)
{
	for (var edge in edges)
	{
		var e	= this.e(edges[edge]);
		var adj	= this.meta.dfsOrderedAdjLists[e.v1.id];
		
		if (!this.isEmpty(adj))
		{
			// remove edge from adj
			var keks = adj.splice($.inArray(e.id, adj), 1);
		}
		
		this.graph.removeEdge(e.id);
		this.updateEdgeCount(e.v1.id, -1);
		this.updateEdgeCount(e.v2.id, -1);
		this.meta.hiddenEdges[e.id]	= true;
	}
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.retainAll = function (source, toCheck)
{
	var pos	= 0;
	
	while (pos < source.length)
	{
		// remove element
		if ($.inArray(source[pos], toCheck) == -1)
		{
			source.splice(pos, 1);
		}
		
		// when item is contained: continue
		else
		{
			pos++;
		}
	}
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.start = function (root)
{
	this.dfsRoot	= root;
	this.tStack.push("eos");	// tStack is stack
	this.dfs(root);
	
	if (!this.isEmpty(this.eStack))
	{
		this.newComponent(this.eStack);
	}
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.top = function (array)
{
	var temp	= null;
	if (!this.isEmpty(array))
	{
		temp	= array.pop();
		array.push(temp);
	}
	return temp;
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.updateEdgeCount = function (node, i)
{
	this.meta.dfsEdgeCount[node] += i;
};

LinearTimeLayout.prototype.SplitCompDFS.prototype.updateTStack = function (v, w, isTreeEdge)
{
	var lastRemoved	= null;
	var itemToPush	= null;
	var y			= -1;
	var topElement	= null;
	
	// tree edge
	if (isTreeEdge == true)
	{
		topElement	= this.top(this.tStack);
		while (!this.isEmpty(this.tStack) && topElement != "eos" && topElement.numA > this.getL1Num(w))
		{
			lastRemoved	= this.tStack.pop();
			topElement	= this.top(this.tStack);
			
			if (lastRemoved.numH > y)
			{
				y	= lastRemoved.numH;
			}
		}
		
		var h	= this.getNum(w) + this.getNumDesc(w) - 1;
		var l1v	= this.meta.dfsLowpt1Vertex[w];
		if (lastRemoved == null)
		{
			itemToPush	= this.createTStackEntry(h, l1v, v);
		}
		else
		{
			itemToPush	= this.createTStackEntry(Math.max(y, h), l1v, lastRemoved.b);
		}
		
		this.tStack.push(itemToPush);
		this.tStack.push("eos");
	}
	
	// back edge
	else
	{
		topElement	= this.top(this.tStack);
		while (!this.isEmpty(this.tStack) && topElement != "eos" && topElement.numA > this.getNum(w))
		{
			lastRemoved	= this.tStack.pop();
			topElement	= this.top(this.tStack);
			
			if (lastRemoved.numH > y)
			{
				y	= lastRemoved.numH;
			}
		}
		
		if (lastRemoved == null)
		{
			itemToPush	= this.createTStackEntry(this.getNum(v), w, v);
		}
		else
		{
			itemToPush	= this.createTStackEntry(y, w, lastRemoved.b);
		}
		
		this.tStack.push(itemToPush);
	}
	
};
