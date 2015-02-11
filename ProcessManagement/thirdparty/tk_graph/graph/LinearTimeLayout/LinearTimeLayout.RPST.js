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
LinearTimeLayout.prototype.RPST = function (parent)
{
	this.adjacency	= {};
	this.edgeID		= 0;
	this.edges		= {};
	this.nodeID		= 0;
	this.nodes		= {};
	this.parent		= parent;
	this.parents	= {};
	this.root		= null;
};

/*
 * RPST Methods
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

LinearTimeLayout.prototype.RPST.prototype.addNode = function (node)
{
	var id	= "n" + this.nodeID++;
	node.id	= id;
	this.nodes[id]	= node;
};

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
			if (index == "entry")
			{
				childNodes[adjNode.getEntry()]	= adjNode;
			}
			else if (index == "exit")
			{
				childNodes[adjNode.getExit()]	= adjNode;	
			}
			else if (index == "entrycount")
			{
				if (!gf_isset(childNodes[adjNode.getEntry()]))
				{
					childNodes[adjNode.getEntry()]	= 0;
				}
				childNodes[adjNode.getEntry()]++;
			}
			else if (index == "exitcount")
			{
				if (!gf_isset(childNodes[adjNode.getExit()]))
				{
					childNodes[adjNode.getExit()]	= 0;
				}
				childNodes[adjNode.getExit()]++;
			}
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
		else
		{
			childNodes.push(t);
		}
	}
	
	return childNodes;
};

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

LinearTimeLayout.prototype.RPST.prototype.getExternalEdgecount = function (node, adjacentNodes, direction)
{
	var count	= 0;
	
	if (!gf_isset(direction) || direction != "in")
		direction	= "out";
	
	if (node instanceof this.parent.RPSTNode)
	{
		node	= node.id;
		
		for (var a in this.adjacency[node])
		{
			var edge	= this.adjacency[node][a];
			var s		= edge.v1.id;
			var t		= edge.v2.id;
			
			if (direction == "out" && s == node && !gf_isset(adjacentNodes[t]))
			{	
				count++;
			}
			
			if (direction == "in" && t == node && !gf_isset(adjacentNodes[s]))
			{	
				count++;
			}
		}
	}
	
	return count;
};

LinearTimeLayout.prototype.RPST.prototype.getRoot = function ()
{
	if (this.root != null)
	{
		return this.nodes[this.root];
	}
	return null;
};

LinearTimeLayout.prototype.RPST.prototype.source = function (edge)
{
	if (gf_isset(this.edges[edge]))
		return this.edges[edge].v1.id;
		
	return null;
};

LinearTimeLayout.prototype.RPST.prototype.target = function (edge)
{
	if (gf_isset(this.edges[edge]))
		return this.edges[edge].v2.id;
		
	return null;
};