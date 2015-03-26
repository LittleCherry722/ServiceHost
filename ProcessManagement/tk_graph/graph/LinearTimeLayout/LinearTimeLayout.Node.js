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
 * Node
 */

/**
 * A generic node.
 * 
 * @class Node
 * @param {String} id - ID of the node.
 * @param {Object} nodeData - Additional data like assigned text, type of node, etc.
 */
LinearTimeLayout.prototype.Node = function (id, nodeData)
{
	
	/**
	 * Information needed for the rendering library to determine what click event has to be assigned to the node.
	 * @memberof! Node
	 * @type {String}
	 */
	this.clickType		= "bv";
	
	/**
	 * Deactivation flag (for drawing reasons).
	 * @memberof! Node
	 * @type {boolean}
	 */
	this.deactivated	= false;
	
	/**
	 * Height of the node's label.
	 * @memberof! Node
	 * @type {int}
	 */
	this.height			= 0;
	
	/**
	 * ID of the node.
	 * @memberof! Node
	 * @type {String}
	 */
	this.id				= id;
	
	/**
	 * Information about the assigned image.
	 * @memberof! Node
	 * @type {Object}
	 */
	this.img			= null;
	
	/**
	 * Additional node information (e.g. style information, assigned text, ...)
	 * @memberof! Node
	 * @type {Object}
	 */
	this.node			= null;
	
	/**
	 * Original ID of the node (for normalized nodes).
	 * @memberof! Node
	 * @type {String}
	 */
	this.orgId			= id;
	
	/**
	 * Flag if node is selected (for drawing reasons).
	 * @memberof! Node
	 * @type {boolean}
	 */
	this.selected		= false;
	
	/**
	 * Shape of the node (for drawing reasons).
	 * @memberof! Node
	 * @type {String}
	 */
	this.shape			= "roundedrectangle";
	
	/**
	 * Style information.
	 * @memberof! Node
	 * @type {Object}
	 */
	this.style			= null;
	
	/**
	 * Additional text for node label. Default: id of node.
	 * @memberof! Node
	 * @type {String}
	 */
	this.text			= id;
	
	/**
	 * Flag if node is virtual.
	 * @memberof! Node
	 * @type {boolean}
	 */
	this.virtual		= false;
	
	/**
	 * Width of the node's label.
	 * @memberof! Node
	 * @type {int}
	 */
	this.width			= 0;
	
	
	/*
	 * initialize node
	 */
	this.init(nodeData);
};

/*
 * Node Methods
 */

/**
 * Calculate height of the node's label.
 * 
 * @memberof! Node
 * @returns {void}
 */
LinearTimeLayout.prototype.Node.prototype.calculateHeight = function ()
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
 * Calculate width of the node's label.
 * 
 * @memberof! Node
 * @returns {void}
 */
LinearTimeLayout.prototype.Node.prototype.calculateWidth = function ()
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
 * Get height of node's label.
 * 
 * @memberof! Node
 * @returns {int} Height of label.
 */
LinearTimeLayout.prototype.Node.prototype.getHeight = function ()
{
	return this.height;
};

/**
 * Get width of node's label.
 * 
 * @memberof! Node
 * @returns {int} Width of label.
 */
LinearTimeLayout.prototype.Node.prototype.getWidth = function ()
{
	return this.width;
};

/**
 * Initialize the node.
 * 
 * @memberof! Node
 * @param {Object} nodeData - Additional information (e.g. style information, assigned text, ...)
 * @returns {void}
 */
LinearTimeLayout.prototype.Node.prototype.init = function (nodeData)
{
	if (nodeData == "virtual")
	{
		this.virtual	= true;
	}
	else
	{
		// set GCNode object
		this.node	= nodeData;
		
		if (gf_isset(nodeData.getShape))
			this.shape		= nodeData.getShape();
			
		if (gf_isset(nodeData.textToString))
			this.text		= nodeData.textToString();
			
		var start		= this.isStart();
		var end			= this.isEnd();
		
		// load style: preload complete style set for node and store the compiled style here to pass it for width-calc, height-calc and actual drawing
		// when the shape of the node is a circle apply correct the style set for circles
		if (this.shape == "circle")
		{
			if (start)
			{
				this.style	= gf_mergeStyles(gv_bv_circleNode.style, gv_bv_circleNode.styleStart);
			}
			else if (end)
			{
				this.style	= gf_mergeStyles(gv_bv_circleNode.style, gv_bv_circleNode.styleEnd);
			}
			else
			{
				this.style = gv_bv_circleNode.style;
			}
			
			// get the image source
			if (gf_isset(nodeData.textToString) && gf_isset(gv_nodeTypeImg[nodeData.textToString()]))
			{
				this.img		= {};
				this.img.src	= gv_imgPath + gv_nodeTypeImg[nodeData.textToString()];
				this.img.height	= gv_bv_circleNode.imgHeight;
				this.img.width	= gv_bv_circleNode.imgWidth;
				this.text		= "";
			}
		}
		
		// when the shape of the node is a roundedrectangle apply correct the style set for circles
		else
		{
			if (start)
			{
				this.style	= gf_mergeStyles(gv_bv_rectNode.style, gv_bv_rectNode.styleStart);
			}
			else
			{
				this.style	= gv_bv_rectNode.style;
			}
		}
		
		if (gf_isset(nodeData.isDeactivated) && nodeData.isDeactivated(true))
			this.deactivated	= true;
			
		if (gf_isset(nodeData.selected) && nodeData.selected === true)
			this.selected		= true;
			
		if (gf_isset(nodeData.getType) && nodeData.getType() == "macro")
			this.clickType		= "bv_dblclick";
			
		if (gf_isset(nodeData.textToString))
			this.orgId			= this.id.substr(1);
	}
	
	// calculate height / width
	this.calculateHeight();
	this.calculateWidth();
};

/**
 * Check if end marker is set to node.
 * 
 * @memberof! Node
 * @returns {boolean} True if end marker is set, false otherwise.
 */
LinearTimeLayout.prototype.Node.prototype.isEnd = function ()
{
	return this.node != null && gf_isset(this.node.isEnd) && this.node.isEnd();
};

/**
 * Check if start marker is set to node.
 * 
 * @memberof! Node
 * @returns {boolean} True if start marker is set, false otherwise.
 */
LinearTimeLayout.prototype.Node.prototype.isStart = function ()
{
	return this.node != null && gf_isset(this.node.isStart) && this.node.isStart();
};
