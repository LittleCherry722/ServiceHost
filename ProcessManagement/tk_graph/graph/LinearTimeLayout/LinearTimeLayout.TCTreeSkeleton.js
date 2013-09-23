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
LinearTimeLayout.prototype.TCTreeSkeleton = function (parent)
{
	this.deletedEdges	= {};
	this.e2o			= {};
	this.edgeID			= 0;
	this.edges			= {};
	this.incidentEdges	= {};
	this.nodeCount		= 0;
	this.nodes			= {};
	this.o2e			= {};
	this.originalEdges	= {};
	this.originalNodes	= {};
	this.parent			= parent;
	this.virtualEdges	= {};
	this.virtualEdgeID	= 0;
};

/*
 * TCTreeSkeleton Methods
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
}

LinearTimeLayout.prototype.TCTreeSkeleton.prototype.addEdge = function (edge)
{
	if (edge instanceof LinearTimeLayout.prototype.BasicEdge)
		edgeID	= edge.id;
	else
		edgeID	= edge;

	if (edge instanceof LinearTimeLayout.prototype.BasicEdge)
	{
		this.o2e[edge.orgId]	= edge;
		// this.addOriginalNodes(edge.orgId);
	}
	
	// edge: edgeID
	this.originalEdges[edgeID]	= edge;
	this.edges[edgeID]			= edge;
	
	this.addNodes(edge);
};

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
}

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

LinearTimeLayout.prototype.TCTreeSkeleton.prototype.isVirtual = function (edge)
{
	if (edge instanceof LinearTimeLayout.prototype.BasicEdge)
		edge	= edge.id;
		
	return gf_isset(this.virtualEdges[edge]);
};

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

LinearTimeLayout.prototype.TCTreeSkeleton.prototype.removeEdge = function (edge)
{
	// TODO: also remove nodes? update incidentEdges?
	
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

LinearTimeLayout.prototype.TCTreeSkeleton.prototype.removeOriginalEdge = function (edge)
{
	delete this.removeEdge[this.o2e[edge]];
};