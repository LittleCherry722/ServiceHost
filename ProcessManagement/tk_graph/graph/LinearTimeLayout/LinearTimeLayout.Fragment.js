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

/**
 * A fragment of the graph.
 * 
 * @class Fragment
 * @see org.jbpt.graph.Fragment
 * @param {Object} parent - LinearTimeLayout instance
 * @param {Object} rpstnode - Node from RPST
 */
LinearTimeLayout.prototype.Fragment = function (parent, rpstnode)
{
	/**
	 * Edges of the fragment.
	 * @memberof! Fragment
	 * @type {Array}
	 */
	this.edges			= {};
	
	/**
	 * Edges outgoing of nodes, stored for each node.
	 * @memberof! Fragment
	 * @type {Object}
	 */
	this.incidentEdges	= {};
	
	/**
	 * Nodes of the fragment.
	 * @memberof! Fragment
	 * @type {Array}
	 */
	this.nodes			= {};
	
	/**
	 * Counter of outgoing edges per node.
	 * @memberof! Fragment
	 * @type {Array}
	 */
	this.outEdges		= {};
	
	/**
	 * Edges that belong to the fragment itself instead of one of its child fragments.
	 * @memberof! Fragment
	 * @type {Array}
	 */
	this.ownEdges		= {};
	
	/**
	 * Nodes that belong to the fragment itself instead of one of its child fragments.
	 * @memberof! Fragment
	 * @type {Array}
	 */
	this.ownNodes		= null;
	
	/**
	 * LinearTimeLayout
	 * @memberof! Fragment
	 * @type {Object}
	 */
	this.parent			= parent;
	
	/**
	 * Assigned node of RPST.
	 * @memberof! Fragment
	 * @type {Object}
	 */
	this.rpstnode		= rpstnode;
};

/**
 * Fragment Methods
 */

/**
 * Add edge to fragment.
 * 
 * @memberof! Fragment
 * @param {String} edge - ID of the graph's edge to add to the fragment.
 * @param {boolean} ownEdge - Indicates if edge belongs to fragment itself instead of one of its child fragments.
 * @returns {void}
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
		
		// potentially unused
		if (ownEdge)
		{
			this.ownEdges[edge]	= edge;
		}
	}
};

/**
 * Add multiple edges to the fragment.
 * 
 * @memberof! Fragment
 * @param {Array} edges - Array of edges to add via addEdge()
 * @returns {void}
 */
LinearTimeLayout.prototype.Fragment.prototype.addEdges = function (edges)
{
	for (var e in edges)
	{
		this.addEdge(edges[e]);
	}
};

/**
 * Add nodes to fragment.
 * 
 * @memberof! Fragment
 * @param {Array} nodes - Array of nodes to add.
 * @returns {void}
 */
LinearTimeLayout.prototype.Fragment.prototype.addNodes = function (nodes)
{
	for (var n in nodes)
	{
		// load ID of original node from nn2on map of LinearTimeLayout
		var node			= this.parent.nn2on[nodes[n]];
		if (gf_isset(node))
		{
			this.nodes[node]	= node;
		}
	}
};

/**
 * Checks if the fragment contains all edges passed.
 * 
 * @memberof! Fragment
 * @param {Array} edges - Edges to check for.
 * @returns {boolean} True if all edges are contained in the fragment, false otherwise.
 */
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

/**
 * Opposite of containsAll().
 * Checks if fragment contains none of the edges passed.
 * 
 * @memberof! Fragment
 * @param {Object} edges - Edges to check for.
 * @returns {boolean} True if none of the edges is contained in the fragment, false otherwise.
 */
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

/**
 * Get nodes of fragment.
 * 
 * @memberof! Fragment
 * @param {boolean} allNodes - Return only nodes of fragment or also include all nodes of its child fragments?
 * @returns {Array} Array of nodes contained in the fragment.
 */
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