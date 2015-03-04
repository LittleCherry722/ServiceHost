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
 */

/*
 * create Edge SubClass
 */

/**
 * Generic edge object.
 * 
 * @class Edge
 * @see org.jbpt.graph.Edge
 * @param {String} id - ID of the edge
 * @param {String} source - ID of source node
 * @param {String} target - ID of target node
 * @param {Object} edgeData - Additional information (style information, assigned text, ...)
 */
LinearTimeLayout.prototype.Edge	= function (id, source, target, edgeData)
{
	/**
	 * Flag to determine if edge is backedge.
	 * @memberof! Edge
	 * @type {boolean}
	 */
	this.backedge		= false;
	
	/**
	 * Deactivation flag (for drawing reasons).
	 * @memberof! Edge
	 * @type {boolean}
	 */
	this.deactivated	= false;
	
	/**
	 * Additional information, like style information, assigned text, ...
	 * @memberof! Edge
	 * @type {Object}
	 */
	this.edgeData		= null;
	
	/**
	 * Height of the edge's label.
	 * @memberof! Edge
	 * @type {int}
	 */
	this.height			= 0;
	
	/**
	 * ID of the edge.
	 * @memberof! Edge
	 * @type {String}
	 */
	this.id				= id;
	
	/**
	 * ID in inEdges array of LinearTimeLayout class.
	 * @memberof! Edge
	 * @type {int}
	 */
	this.inEdgesCurID	= null;
	
	/**
	 * ID in inEdges array of LinearTimeLayout class.
	 * @memberof! Edge
	 * @type {int}
	 */
	this.inEdgesOldID	= null;
	
	/**
	 * Flag if edge is optional (for drawing reasons).
	 * @memberof! Edge
	 * @type {boolean}
	 */
	this.optional		= false;
	
	/**
	 * Original ID of the edge (for normalized edges).
	 * @memberof! Edge
	 * @type {String}
	 */
	this.orgId			= id;
	
	/**
	 * ID in outEdges array of LinearTimeLayout class.
	 * @memberof! Edge
	 * @type {int}
	 */
	this.outEdgesCurID	= null;
	
	/**
	 * ID in outEdges array of LinearTimeLayout class.
	 * @memberof! Edge
	 * @type {int}
	 */
	this.outEdgesOldID	= null;
	
	/**
	 * Flag if edge is reversed, i.e. source and target node exchanged.
	 * @memberof! Edge
	 * @type {boolean}
	 */
	this.reversed		= false;
	
	/**
	 * Flag if edge is selected (for drawing reasons).
	 * @memberof! Edge
	 * @type {boolean}
	 */
	this.selected		= false;
		
	/**
	 * ID of source node.
	 * @memberof! Edge
	 * @type {String}
	 */
	this.source			= 0;
	
	/**
	 * Style information.
	 * @memberof! Edge
	 * @type {Object}
	 */
	this.style			= null;
		
	/**
	 * ID of target node.
	 * @memberof! Edge
	 * @type {String}
	 */
	this.target			= 0;
	
	/**
	 * Additional text for edge label. Default: id of edge.
	 * @memberof! Edge
	 * @type {String}
	 */
	this.text			= id;
	
	/**
	 * Flag if edge is virtual.
	 * @memberof! Edge
	 * @type {boolean}
	 */
	this.virtual		= false;
	
	/**
	 * Width of the edge's label.
	 * @memberof! Edge
	 * @type {int}
	 */
	this.width			= 0;
	
	/**
	 * Initialize the edge
	 */
	this.init(source, target, edgeData);
};
	
	
/*
 * Edge Methods
 */

/**
 * Calculate height of the edge's label.
 * 
 * @memberof! Edge
 * @returns {void}
 */
LinearTimeLayout.prototype.Edge.prototype.calculateHeight = function ()
{
	if (this.style != null)
	{
		// if an image is assigned to the element the element's height is the height of the image 
		if (this.img != null)
		{
			this.height	= this.img.height;
		}
		
		// for text elements the element's height is estimated based on the text
		else if (this.text != "")
		{
			// accesses the gf_estimateTextHeight function provided in the tk_graph library
			this.height	= gf_estimateTextHeight(this.text, this.style);
		}
	}
};

/**
 * Calculate width of the edge's label.
 * 
 * @memberof! Edge
 * @returns {void}
 */
LinearTimeLayout.prototype.Edge.prototype.calculateWidth = function ()
{
	if (this.style != null)
	{
		// if an image is assigned to the element the element's width is the width of the image 
		if (this.img != null)
		{
			this.width	= this.img.width;
		}
		
		// for text elements the element's width is estimated based on the text
		else if (this.text != "")
		{
			// accesses the gf_estimateTextWidth function provided in the tk_graph library
			this.width	= gf_estimateTextWidth(this.text, this.style);
		}
	}
};

/**
 * Get height of edge's label.
 * 
 * @memberof! Edge
 * @returns {int} Height of label.
 */
LinearTimeLayout.prototype.Edge.prototype.getHeight = function ()
{
	return this.height;
};

/**
 * Get width of edge's label.
 * 
 * @memberof! Edge
 * @returns {int} Width of label.
 */
LinearTimeLayout.prototype.Edge.prototype.getWidth = function ()
{
	return this.width;
};

/**
 * Initialize the edge.
 * 
 * @memberof! Edge
 * @param {Object} source - ID of source node.
 * @param {Object} target - ID of target node.
 * @param {Object} edgeData - Additional information (e.g. style information, assigned text, ...)
 * @returns {void}
 */
LinearTimeLayout.prototype.Edge.prototype.init	= function (source, target, edgeData)
{

	this.source		= source;
	this.target		= target;
	
	
	// set GCEdge
	if (edgeData == "virtual")
	{
		this.virtual	= true;
	}
	else if (edgeData == "backedge")
	{
		this.backedge	= true;
	}
	else
	{
		this.edgeData	= edgeData;
		
		// load style: preload complete style set for node and store the compiled style here to pass it for width-calc, height-calc and actual drawing
		this.style		= gv_bv_arrow.style;
		if (gf_isset(edgeData.getType))
		{
			if (edgeData.getType() == "timeout")
			{
				this.style	= gf_mergeStyles(gv_bv_arrow.style, gv_bv_arrow.styleTimeout);
			}
			if (edgeData.getType() == "cancelcondition")
			{
				this.style	= gf_mergeStyles(gv_bv_arrow.style, gv_bv_arrow.styleException);
			}
		}
		
		if (gf_isset(edgeData.isDeactivated) && edgeData.isDeactivated())
			this.deactivated	= true;
			
		if (gf_isset(edgeData.isOptional) && edgeData.isOptional())
			this.optional		= true;
		
		if (gf_isset(edgeData.selected) && edgeData.selected === true)
			this.selected		= true;
			
		if (gf_isset(edgeData.textToString))
		{
			this.text			= edgeData.textToString();
			this.orgId			= this.id.substr(1);
		}
	}
	
	// calculate height / width
	this.calculateHeight();
	this.calculateWidth();
};

/**
 * Reverse edge.
 * 
 * @memberof! Edge
 * @returns {void}
 */
LinearTimeLayout.prototype.Edge.prototype.reverse = function ()
{
	var temp	= this.source;
	this.source	= this.target;
	this.target	= temp;
	
	// set reversed flag
	this.reversed	= !this.reversed;
};