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
 * Fragment class
 */
LinearTimeLayout.prototype.Fragment = function (parent, rpstnode)
{
	this.edges			= {};
	this.incidentEdges	= {};
	this.nodes			= {};
	this.outEdges		= {};
	this.ownEdges		= {};
	this.ownNodes		= null;
	this.parent			= parent;
	this.rpstnode		= rpstnode;
};

/**
 * Fragment Methods
 */
LinearTimeLayout.prototype.Fragment.prototype.addEdge = function (edge, ownEdge)
{
	if (!gf_isset(ownEdge))
		ownEdge = false;
	
	if (gf_isset(this.parent.edges[edge]) && !gf_isset(this.edges[edge]))
	{
		var source	= this.parent.source(edge);
		if (!gf_isset(this.outEdges[source]))
		{
			this.incidentEdges[source]	= new Array();
			this.outEdges[source]		= 0;
		}
		
		this.edges[edge]	= edge;
		this.outEdges[source]++;
		this.incidentEdges[source].push(edge);
		
		if (ownEdge)
		{
			this.ownEdges[edge]	= edge;
		}
	}
};

LinearTimeLayout.prototype.Fragment.prototype.addEdges = function (edges)
{
	for (var e in edges)
	{
		this.addEdge(edges[e]);
	}
};

LinearTimeLayout.prototype.Fragment.prototype.addNodes = function (nodes)
{
	for (var n in nodes)
	{
		var node			= this.parent.nn2on[nodes[n]];
		// TODO: find cause for undefined nodes (SNK, SRC are still in the nodeset)
		if (gf_isset(node))
		{
			this.nodes[node]	= node;
		}
	}
};

LinearTimeLayout.prototype.Fragment.prototype.containsAll = function (edges)
{
	for (var e in edges)
	{
		var edge	= edges[e];
		if (!gf_isset(this.edges[edge]))
			return false;
	}
	
	return true;
};

LinearTimeLayout.prototype.Fragment.prototype.containsNone = function (edges)
{
	for (var e in edges)
	{
		var edge	= edges[e];
		if (gf_isset(this.edges[edge]))
			return false;
	}
	
	return true;
};

LinearTimeLayout.prototype.Fragment.prototype.getNodes = function (allNodes)
{
	if (!gf_isset(allNodes))
		allNodes	= false;
	
	if (this.ownNodes == null)
	{
		this.ownNodes	= {};
		
		// get all nodes contained in children
		var childrenNodes	= {};
		
		if (!allNodes)
		{
			var children		= this.parent.rpst.getChildren2(this.rpstnode).children;
			for (var c in children)
			{
				var child		= children[c];
				
				if (child.type == "trivial")
				{
					var childNodes	= child.getFragment().getNodes();
					
					for (var cn in childNodes)
					{
						childrenNodes[cn]	= cn;
					}
				}
			}
		}
		
		// get own nodes
		for (var n in this.nodes)
		{
			if (!gf_isset(childrenNodes[n]))
			{
				this.ownNodes[n]	= n;
			}
		}
	}
	
	return this.ownNodes;
};