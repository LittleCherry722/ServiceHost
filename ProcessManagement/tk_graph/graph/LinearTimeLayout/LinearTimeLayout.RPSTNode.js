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
 * RPSTNode
 */

/**
 * Node of the RPST.
 * 
 * @class RPSTNode
 * @see org.jbpt.algo.tree.rpst.RPSTNode
 * @param {Object} parent - Instance of LinearTimeLayout
 * @param {Object} rpst - The RPST the node belongs to.
 * @param {Object} tcnode - The node of the TCTree (triconnected component) that forms the RPSTNode (fragment).
 */
LinearTimeLayout.prototype.RPSTNode = function (parent, rpst, tcnode)
{
	/**
	 * ID of the entry node of the fragment.
	 * @memberof! RPSTNode
	 * @type {String}
	 */
	this.entry			= null;
	
	/**
	 * ID of the exit node of the fragment.
	 * @memberof! RPSTNode
	 * @type {String}
	 */
	this.exit			= null;
	
	/**
	 * The actual fragment belonging to this RPSTNode.
	 * @memberof! RPSTNode
	 * @type {Object}
	 */
	this.fragment		= null;
	
	/**
	 * The ID of the node.
	 * @memberof! RPSTNode
	 * @type {String}
	 */
	this.id				= null;
	
	/**
	 * Name of the fragment.
	 * @memberof! RPSTNode
	 * @type {String}
	 */
	this.name			= tcnode.name;
	
	/**
	 * Instance of LinearTimeLayout.
	 * @memberof! RPSTNode
	 * @type {Object}
	 */
	this.parent			= parent;
	
	/**
	 * Instance of RPST.
	 * @memberof! RPSTNode
	 * @type {Object}
	 */
	this.rpst			= rpst;
	
	/**
	 * Corresponding node of the TCTree (triconnected component).
	 * @memberof! RPSTNode
	 * @type {Object}
	 */
	this.tcnode			= tcnode;
	
	/**
	 * Type of the triconnected component.
	 * @memberof! RPSTNode
	 * @type {String}
	 */
	this.type			= tcnode.type;
};

/*
 * RPSTNode Methods
 */

/**
 * Determine boundaries of fragment.
 * Determine exit and entry node of the fragment.
 * 
 * @memberof! RPSTNode
 * @returns {void}
 */
LinearTimeLayout.prototype.RPSTNode.prototype.classifyBoundaries = function ()
{
	// as of jBPT RPSTNode :: classifyBoundaries()
	
	var nodes		= new Array();
	var fragment	= this.getFragment();
	
	for (var e in fragment.edges)
	{
		var edge	= fragment.edges[e];
		var s		= this.parent.source(edge);
		var t		= this.parent.target(edge);
		nodes.push(s);
		nodes.push(t);
	}
	
	var countSource	= 0;
	var countSink	= 0;
	var flag1		= false;
	var flag2		= true;
	var node2		= null;
	var visited		= {};
	for (var n in nodes)
	{
		var node			= nodes[n];
		
		if (gf_isset(visited[node]))
		{
			continue;
		}
			
		visited[node]	= node;
		
		var containsAllIn	= fragment.containsAll(this.parent.inEdges[node]);
		var containsAllOut	= fragment.containsAll(this.parent.outEdges[node]);
		
		if (this.parent.inEdges[node].length == 0)
		{
			this.entry	= node;
			countSource++;
			flag2		= false;
			continue;
		}
		
		if (this.parent.outEdges[node].length == 0)
		{
			this.exit	= node;
			countSink++;
			flag2		= false;
			continue;
		}
		
		if (containsAllIn && containsAllOut)
		{
			continue;
		}
		
		if (flag1 == true)
		{
			flag1	= false;
		}
		else if (flag1 == false)
		{
			flag1	= true;
			node2	= node;
		}
		
		if (containsAllOut || fragment.containsNone(this.parent.inEdges[node]))
		{
			this.entry	= node;
		}
		
		if (containsAllIn || fragment.containsNone(this.parent.outEdges[node]))
		{
			this.exit	= node;
		}
	}
	
	if (countSource > 1)
	{
		this.entry	= null;
	}
	if (countSink > 1)
	{
		this.exit	= null;
	}
	if (flag1 && flag2)
	{
		this.entry	= node2;
		this.exit	= node2;
	}
};

/**
 * Get edges of the fragment.
 * 
 * @memberof! RPSTNode
 * @returns {Array} List of edges of the fragment.
 */
LinearTimeLayout.prototype.RPSTNode.prototype.getEdges = function ()
{
	var edges			= new Array();
	var originalEdges	= this.tcnode.skeleton.o2e;
	for (var e in originalEdges)
	{
		var edge	= originalEdges[e];
		if (gf_isset(this.parent.extraEdges[edge.orgId]))
		{
			continue;
		}
		
		edges.push(this.parent.ne2oe[edge.orgId]);
	}
	
	return edges;
};

/**
 * Get entry of the fragment.
 * If entry is not yet set, classify boundaries to determine entry and exit of fragment.
 * 
 * @memberof! RPSTNode
 * @returns {String} ID of the entry node.
 */
LinearTimeLayout.prototype.RPSTNode.prototype.getEntry = function ()
{
	if (this.entry == null)
	{
		this.classifyBoundaries();
	}
	
	return this.entry;
};

/**
 * Get exit of the fragment.
 * If exit is not yet set, classify boundaries to determine entry and exit of fragment.
 * 
 * @memberof! RPSTNode
 * @returns {String} ID of the exit node.
 */
LinearTimeLayout.prototype.RPSTNode.prototype.getExit = function ()
{
	if (this.exit == null)
	{
		this.classifyBoundaries();
	}
	
	return this.exit;
};

/**
 * Get fragment belonging to this RPSTNode.
 * If fragment has not yet been initialized: initialize the fragment.
 * 
 * @memberof! RPSTNode
 * @returns {Object} The corresponding fragment.
 */
LinearTimeLayout.prototype.RPSTNode.prototype.getFragment = function ()
{
	if (this.fragment == null)
	{
		this.fragment	= new this.parent.Fragment(this.parent, this);
		
		// add nodes
		this.fragment.addNodes(this.tcnode.skeleton.nodes);
		
		var children	= this.rpst.getChildren2(this).children;
		for (var c in children)
		{
			var node			= children[c];
			var childFragment	= node.getFragment();
			this.fragment.addEdges(childFragment.edges);
		}
		
		var originalEdges	= this.tcnode.skeleton.o2e;
		for (var e in originalEdges)
		{
			var edge	= originalEdges[e];
			if (gf_isset(this.parent.extraEdges[edge.orgId]))
			{
				continue;
			}
			
			this.fragment.addEdge(this.parent.ne2oe[edge.orgId], true);
		}
	}
	
	return this.fragment;
};