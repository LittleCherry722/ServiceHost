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
 * create BasicEdge SubClass
 */

/**
 * A generic edge with basic attributes.
 * 
 * @class BasicEdge
 * @param {String} id - ID of the edge
 * @param {Object} v1 - Start node of edge.
 * @param {Object} v2 - End node of edge.
 */
LinearTimeLayout.prototype.BasicEdge	= function (id, v1, v2)
{
	/**
	 * ID of the edge
	 * @memberof! BasicEdge
	 * @type {String}
	 */
	this.id			= id;
	
	/**
	 * Original ID of the edge.
	 * @memberof! BasicEdge
	 * @type {String}
	 */
	this.orgId		= null;
	
	/**
	 * Type of the edge as of LinearTimeLayout.edgeTypes enumeration.
	 * @memberof! BasicEdge
	 * @type {String}
	 */
	this.type		= null;
	
	/**
	 * Start node of edge.
	 * @memberof! BasicEdge
	 * @type {Object}
	 */
	this.v1			= v1;
	
	/**
	 * End node of edge.
	 * @memberof! BasicEdge
	 * @type {Object}
	 */
	this.v2			= v2;
	
	/**
	 * Flag to indicate virtual edges.
	 * @memberof! BasicEdge
	 * @type {boolean}
	 */
	this.virtual	= false;
	
};

/*
 * Edge Methods
 */

/**
 * Change orientation of edge.
 * 
 * @memberof! BasicEdge
 * @param {Object} v1 - New start node (either current start node or current end node)
 * @param {Object} v2 - New end node (either current start node or current end node)
 * @returns {void}
 */
LinearTimeLayout.prototype.BasicEdge.prototype.changeOrientation = function (v1, v2)
{
	if (v2 == this.v1.id || v1 == this.v2.id)
	{
		var temp	= this.v1;
		this.v1		= this.v2;
		this.v2		= temp;
	}
};

/**
 * Checks if the edge connects two vertices.
 * 
 * @memberof! BasicEdge
 * @param {Object} v1 - First node.
 * @param {Object} v2 - Second node.
 * @returns {boolean} True if both nodes are connected by the edge, false otherwise.
 */
LinearTimeLayout.prototype.BasicEdge.prototype.connectsVertices = function (v1, v2)
{
	return this.v1.id == v1 && this.v2.id == v2 || this.v1.id == v2 && this.v2.id == v1;
};

/**
 * Returns the ID of the end or start node of the edge depending on which node is passed.
 * 
 * @memberof! BasicEdge
 * @param {Object} v - One node.
 * @returns {String} ID of other node of edge. Default: null
 */
LinearTimeLayout.prototype.BasicEdge.prototype.getOtherVertex = function (v)
{
	if (v == this.v1.id)
		return this.v2.id;
		
	if (v == this.v2.id)
		return this.v1.id;
		
	return null;
};

/**
 * Returns the IDs of the start and the end node of the edge.
 * 
 * @memberof! BasicEdge
 * @returns {Array} An array with two entries containing the IDs of start and end node of the edge. 
 */
LinearTimeLayout.prototype.BasicEdge.prototype.getVertices = function ()
{
	return [this.v1.id, this.v2.id];
};

/**
 * Checks if the edge is a self loop, i.e. its start and end node are the same.
 * 
 * @memberof! BasicEdge
 * @returns {boolean} True if start and end node of the edge are the same.
 */
LinearTimeLayout.prototype.BasicEdge.prototype.isSelfLoop = function ()
{
	return this.v1.id == this.v2.id && this.v1 != null && this.v2 != null;
};

/**
 * Check if edge is virtual.
 * 
 * @memberof! BasicEdge
 * @returns {boolean} True if virtual flag is set to true.
 */
LinearTimeLayout.prototype.BasicEdge.prototype.isVirtual = function ()
{
	return this.virtual === true;
};

/**
 * Set virtual flag.
 * 
 * @memberof! BasicEdge
 * @param {boolean} virtual - Virtual flag.
 * @returns {void}
 */
LinearTimeLayout.prototype.BasicEdge.prototype.setVirtual = function (virtual)
{
	if (gf_isset(virtual) && virtual === true)
	{
		this.virtual	= true;
	}
};

/**
 * Get string representation of edge: start node -> end node
 * 
 * @memberof! BasicEdge
 * @returns {String} String representation of edge.
 */
LinearTimeLayout.prototype.BasicEdge.prototype.toString = function ()
{
	return this.v1.name + "->" + this.v2.name;
};