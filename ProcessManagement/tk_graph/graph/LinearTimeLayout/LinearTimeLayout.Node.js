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
LinearTimeLayout.prototype.Node = function (id, nodeData)
{
	this.clickType		= "bv";
	this.deactivated	= false;
	this.height			= 0;
	this.id				= id;
	this.img			= null;
	this.node			= null;
	this.orgId			= id;
	this.selected		= false;
	this.shape			= "roundedrectangle";
	this.style			= null;
	this.text			= id;
	this.virtual		= false;
	this.width			= 0;
	// this.id			= 0;
	
	// position
	this.x				= 0;
	this.y				= 0;
	
	// dimensions for calculation
	this.dimensions	= {width: 0, height: 0};
	
	// branching
	this.branchingSplit	= false;
	this.branchingJoin	= false;
	
	this.init(nodeData)
};

/*
 * Node Methods
 */
LinearTimeLayout.prototype.Node.prototype.calculateHeight = function ()
{
	// TODO
	// calc height by number-of-lines * (font-size + some space) + padding + border
	// function calcHeight (text, style)
	if (this.style != null)
	{
		if (this.img != null)
		{
			this.height	= this.img.height;
		}
		else if (this.text != "")
		{
			/*
			var split	= this.text.split(/<br>|<br \/>|<br\/>|\\r\\n|\\r|\\n|\n/gi);
			
			// estimation: number of lines * (fontSize + someSpace)
			this.height	= Math.ceil(split.length * (this.style.fontSize + 3));
			*/
			
			this.height	= gf_estimateTextHeight(this.text, this.style);
		}
		
		// TODO: add borders, padding, minHeight, ... to height
	}
};

LinearTimeLayout.prototype.Node.prototype.calculateWidth = function ()
{
	// TODO
	// calc width by max-width of text + padding + border
	// function calcWidth (text, style)
	if (this.style != null)
	{
		if (this.img != null)
		{
			this.width	= this.img.width;
		}
		else if (this.text != "")
		{
			/*
			var split	= this.text.split(/<br>|<br \/>|<br\/>|\\r\\n|\\r|\\n|\n/gi);
			for (var s in split)
			{
				// estimation: max of length of each line
				this.width	= Math.ceil(Math.max(split[s].length * (this.style.fontSize / 2.434), this.width));
			}
			*/
			this.width	= gf_estimateTextWidth(this.text, this.style);
		}
		
		// TODO: add borders, padding, minWidth, ... to width
	}
};

LinearTimeLayout.prototype.Node.prototype.getHeight = function ()
{
	return this.height;
};

LinearTimeLayout.prototype.Node.prototype.getWidth = function ()
{
	return this.width;
};

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

LinearTimeLayout.prototype.Node.prototype.isEnd = function ()
{
	return this.node != null && gf_isset(this.node.isEnd) && this.node.isEnd();
};

LinearTimeLayout.prototype.Node.prototype.isStart = function ()
{
	return this.node != null && gf_isset(this.node.isStart) && this.node.isStart();
};
