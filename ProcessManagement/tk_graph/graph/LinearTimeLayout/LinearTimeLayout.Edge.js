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
LinearTimeLayout.prototype.Edge	= function (id, source, target, edgeData)
{
	/**
	 * @type {GCedge}
	 */
	this.backedge		= false;	// back edge?
	this.components		= {};
	this.deactivated	= false;
	this.edgeData		= null;
	this.height			= 0;
	this.id				= id;
	this.inEdgesCurID	= null;
	this.inEdgesOldID	= null;
	this.optional		= false;
	this.orgId			= id;
	this.outEdgesCurID	= null;
	this.outEdgesOldID	= null;
	this.reversed		= false;
	this.selected		= false;

	/**
	 * @type {int}
	 */
	this.source			= 0;
	this.style			= null;

	/**
	 * @type {int}
	 */
	this.target			= 0;
	this.text			= id;
	this.virtual		= false;	// virtual edge?
	this.width			= 0;

	this.init(source, target, edgeData);
};


/*
 * Edge Methods
 */
LinearTimeLayout.prototype.Edge.prototype.calculateHeight = function ()
{
	// TODO: calculate width + height of label
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

LinearTimeLayout.prototype.Edge.prototype.calculateWidth = function ()
{
	// TODO: calculate width + height of label
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

LinearTimeLayout.prototype.Edge.prototype.getHeight = function ()
{
	return this.height;
};

LinearTimeLayout.prototype.Edge.prototype.getWidth = function ()
{
	return this.width;
};

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

LinearTimeLayout.prototype.Edge.prototype.reverse = function ()
{
	var temp	= this.source;
	this.source	= this.target;
	this.target	= temp;

	// set reversed flag
	this.reversed	= !this.reversed;
};
