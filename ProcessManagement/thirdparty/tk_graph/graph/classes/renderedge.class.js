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
 * The renderEdge class represents an edge as a render object
 *
 * @private
 * @class represents an edge as a render object
 * @param {String} id The id of the node.
 * @param {GCedge} edge The edge object.
 * @returns {void}
 */
function GCrenderEdge (id, edge)
{
	/*
	 * shape, startobject, endobject
	 */

	this.edge			= edge;

	this.id				= id;

	this.idStart		= "";

	this.idEnd			= "";

	this.loopPosition	= "right";

	this.loopSpace		= 0;

	this.posEndH		= "center";		// left, center, right

	this.posEndV		= "bottom";		// top, center, bottom

	this.posStartH		= "center";		// left, center, right

	this.posStartV		= "bottom";		// top, center, bottom

	this.selected		= false;		// is set by GCmacro

	this.shape			= "straight";

	this.style			= null;

	this.text			= gf_isset(edge.textToString) ? edge.textToString() : id;


	// initialize the object
	this.init();

}

GCrenderEdge.prototype.draw = function ()
{
	/*
	 * ltl version
	 */
	var shape	= "straight";				// this.shape

	var loopSpace	= 0;					// this.loopSpace

	if (gf_isset(gv_objects_nodes[this.idStart], gv_objects_nodes[this.idEnd]))
	{
		var srcBBox	= gv_objects_nodes[this.idStart].getBoundaries();
		var tgtBBox	= gv_objects_nodes[this.idEnd].getBoundaries();

		var x1	= srcBBox.x;
		var x2	= tgtBBox.x;
		var y1	= srcBBox.y;
		var y2	= tgtBBox.y;

		if (this.posStartH.toLowerCase() == "left")
		{
			x1	= srcBBox.left;
		}
		else if (this.posStartH.toLowerCase() == "right")
		{
			x1	= srcBBox.right;
		}

		if (this.posStartV.toLowerCase() == "top")
		{
			y1	= srcBBox.top;
		}
		else if (this.posStartV.toLowerCase() == "bottom")
		{
			y1	= srcBBox.bottom;
		}

		if (this.posEndH.toLowerCase() == "left")
		{
			x2	= tgtBBox.left;
		}
		else if (this.posEndH.toLowerCase() == "right")
		{
			x2	= tgtBBox.right;
		}

		if (this.posEndV.toLowerCase() == "top")
		{
			y2	= tgtBBox.top;
		}
		else if (this.posEndV.toLowerCase() == "bottom")
		{
			y2	= tgtBBox.bottom;
		}

		if (this.loopSpace != 0)
		{
			if (this.loopPosition.toLowerCase() == "bottom")
			{
				y2	+= this.loopSpace;
			}
			else if (this.loopPosition.toLowerCase() == "top")
			{
				y2	-= this.loopSpace;
			}
			else if (this.loopPosition.toLowerCase() == "left")
			{
				x2	-= this.loopSpace;
			}
			else if (this.loopPosition.toLowerCase() == "right")
			{
				x2	+= this.loopSpace;
			}
		}

		gf_timeCalc("drawing edges - drawArrow() - create GCpath");
            var path	= new GCpath(x1, y1, x2, y2, this.shape, this.text, this.id, true);
		gf_timeCalc("drawing edges - drawArrow() - create GCpath");


		gf_timeCalc("drawing edges - drawArrow() - apply settings");
			// apply the deactivation status to the path
			if (gf_isset(this.edge.isDeactivated) && this.edge.isDeactivated())
				path.deactivate(true);

			// apply the optional status to the path
			path.setOptional(gf_isset(this.edge.isOptional) && this.edge.isOptional(), true);

			// apply the selection status to the path
			if (this.selected)
				path.select(true);

			// add the click events to the path
			path.click();
		gf_timeCalc("drawing edges - drawArrow() - apply settings");


		gf_timeCalc("drawing edges - drawArrow() - apply style");
			path.setStyle(this.style);
		gf_timeCalc("drawing edges - drawArrow() - apply style");


		gf_timeCalc("drawing edges - drawArrow() - apply calculated path");
			/*
			gt_bv_edge.setFirstLine(gt_bv_firstLine);
			gt_bv_edge.setSpace1(gt_bv_space1);
			gt_bv_edge.setSpace2(gt_bv_space2);
			*/
			path.setShape(this.shape, 2);
		gf_timeCalc("drawing edges - drawArrow() - apply calculated path");
	}
};

GCrenderEdge.prototype.init = function ()
{
	this.style		= gv_bv_arrow.style;

	if (gf_isset(this.edge.getType))
	{
		if (this.edge.getType() == "timeout")
		{
			this.style	= gf_mergeStyles(gv_bv_arrow.style, gv_bv_arrow.styleTimeout);
		}

		if (this.edge.getType() == "cancelcondition")
		{
			this.style	= gf_mergeStyles(gv_bv_arrow.style, gv_bv_arrow.styleException);
		}
	}
};

GCrenderEdge.prototype.setPosEnd = function (horizontal, vertical)
{
	this.posEndH	= horizontal;
	this.posEndV	= vertical;
};

GCrenderEdge.prototype.setPosStart = function (horizontal, vertical)
{
	this.posStartH	= horizontal;
	this.posStartV	= vertical;
};

GCrenderEdge.prototype.setEndPoints = function (start, end)
{
	this.idStart	= start;
	this.idEnd		= end;
};

/**
 *
 * @param {Object} loopSpace
 * @param {Object} position "bottom", "right", "left", "top"
 */
GCrenderEdge.prototype.setLoopSpace = function (loopSpace, position)
{
	this.loopSpace		= loopSpace;
	this.loopPosition	= position;
};

GCrenderEdge.prototype.setShape = function (shape)
{
	this.shape	= shape;
};
