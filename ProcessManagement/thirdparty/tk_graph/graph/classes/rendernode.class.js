/**
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2012 Matthias Schrammek, Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/**
 * The renderNode class represents a node as a render object
 * 
 * @private
 * @class represents a node as a render object
 * @param {String} id The id of the node.
 * @param {GCnode} node The node object.
 * @returns {void}
 */
function GCrenderNode (id, node)
{
	
	this.id			= id;
	
	this.imgHeight	= 0;
	
	this.imgWidth	= 0;
	
	this.imgSrc		= "";
	
	this.isStart	= gf_isset(node.isStart) && node.isStart(true);
	
	this.isEnd		= gf_isset(node.isEnd) && node.isEnd(true);
	
	this.node		= node;
	
	/*
    * relative change
    */
    this.posrx		= 0;

    /*
     * relative change
     */
    this.posry		= 0;

	this.posx		= 0;
	
	this.posy		= 0;
	
	this.selected	= false;		// is set by GCmacro
	
	this.shape		= gf_isset(node.getShape) ? node.getShape() : "roundedrectangle";
	
	this.style		= null;
	
	this.text		= gf_isset(node.textToString) ? node.textToString() : id;
	
	// initialize the object
	this.init();
	
}

GCrenderNode.prototype.draw = function ()
{
	
	var posx	= this.posx + this.posrx;
	var posy	= this.posy + this.posry;
			
	gf_timeCalc("drawing nodes - drawNode() - create");
	// create the GClabel at the x and y ordinates and pass the shape, the text and the id to the GClabel
	var rect	= new GClabel(posx, posy, this.text, this.shape, this.id, false, true);
	gf_timeCalc("drawing nodes - drawNode() - create");

		
	gf_timeCalc("drawing nodes - drawNode() - apply");
		
		if (gv_estimateTextDimensions)
			rect.setEstimateTextDimensions(true);
		
		// apply the deactivation status to the label
		if (gf_isset(this.node.isDeactivated) && this.node.isDeactivated(true))
			rect.deactivate(true);
				
		// apply the selection status to the label
		if (gf_isset(this.selected) && this.selected === true)
			rect.select(true);
			
		// apply the image
		if (this.imgSrc != "")
			rect.setImg(this.imgSrc, this.imgWidth, this.imgHeight);
			
		// determine click type
		var clickType	= "bv";
		
		if (gf_isset(this.node.getType) && this.node.getType() == "macro")
			clickType += "_dblclick";
			
		// apply the style
		gf_timeCalc("drawing nodes - drawNode() - apply III");
			rect.setStyle(this.style);
		gf_timeCalc("drawing nodes - drawNode() - apply III");
		
		rect.click(clickType);
			
	gf_timeCalc("drawing nodes - drawNode() - apply");
};

GCrenderNode.prototype.getPositionRelative = function ()
{
	return {"x": this.posrx, "y": this.posry};
};

GCrenderNode.prototype.init = function ()
{
		
	// when the shape of the node is a circle apply correct the style set for circles
	if (this.shape == "circle")
	{
		if (this.isStart)
		{
			this.style = gf_mergeStyles(gv_bv_circleNode.style, gv_bv_circleNode.styleStart);
		}
		else if (this.isEnd)
		{
			this.style = gf_mergeStyles(gv_bv_circleNode.style, gv_bv_circleNode.styleEnd);
		}
		else
		{
			this.style = gv_bv_circleNode.style;
		}
			
		// get the image source
		if (gf_isset(gv_nodeTypeImg[this.text]))
		{
			this.imgSrc		= gv_imgPath + gv_nodeTypeImg[this.text];
			this.imgHeight	= gv_bv_circleNode.imgHeight;
			this.imgWidth	= gv_bv_circleNode.imgWidth;
			this.text		= "";
		}
	}
		
	// when the shape of the node is a roundedrectangle apply correct the style set for circles
	else
	{
		if (this.isStart)
		{
			this.style = gf_mergeStyles(gv_bv_rectNode.style, gv_bv_rectNode.styleStart);
		}
		else
		{
			this.style = gv_bv_rectNode.style;
		}
	}
};

GCrenderNode.prototype.setPosition = function (x, y)
{
	this.posx	= x;
	this.posy	= y;
};

GCrenderNode.prototype.setPositionRelative = function (x, y)
{
	this.posrx	= x;
	this.posry	= y;
};