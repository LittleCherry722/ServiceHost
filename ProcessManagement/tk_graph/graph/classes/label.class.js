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
 * Creates a Raphael ellipse or rect and contains some information about that element.
 * This represents a label in either the behavioral view or the communication view.
 * 
 * @private
 * @class creates a Raphael ellipse or rect
 * @param {int} x The x ordinate of the top left corner of this element.
 * @param {int} y The y ordinate of the top left corner of this element.
 * @param {String} text The text of the label.
 * @param {String} shape The shape of the label. Possible values are "rectangle", "roundedrectangle", "roundedrectanglemulti", "circle", "ellipse" (default: "roundedrectangle")
 * @param {String} id The id of the label.
 * @param {boolean} belongsToPath This indicates whether the label belongs to a path.
 * @param {boolean} performanceMode When set to true the style won't be updated on init.
 * @returns {void}
 */
function GClabel (x, y, text, shape, id, belongsToPath, performanceMode)
{
	
	/**
	 * A reference to either the rectangle element or the ellipse element.
	 * This is used to get the dimensions of the object depending on its current shape.
	 * 
	 * @type Element (from Raphael)
	 */
	this.bboxObj	= null;
	
	/**
	 * When set to true this label belongs to a path so it has to call a different set of methods on an event.
	 * 
	 * @type boolean
	 */
	this.belongsToPath = false;
	
	/**
	 * Deactivation flag.
	 * When it is set to true the label will be displayed as deactivated.
	 * 
	 * @type boolean
	 */
	this.deactive = false;
	
	/**
	 * A Raphael ellipse.
	 * 
	 * @see Paper.ellipse() at the <a href="http://raphaeljs.com/reference.html#Paper.ellipse">Raphael documentation</a>
	 * @type Element (from Raphael)
	 */
	this.ellipse	= null;
	
	/**
	 * The id of this label.
	 * 
	 * @type String
	 */
	this.id = "";
	
	/**
	 * The image of this label (if one).
	 * 
	 * @see Paper.img() at the <a href="http://raphaeljs.com/reference.html#Paper.img">Raphael documentation</a>
	 * @type Element (from Raphael)
	 */
	this.img	= null;
	
	/**
	 * This array holds 3 Raphael rects.
	 * It is used to display multi subjects.
	 * 
	 * @see Paper.rect() at the <a href="http://raphaeljs.com/reference.html#Paper.rect">Raphael documentation</a>
	 * @type Element[]
	 */
	this.multiRR	= [null, null, null];
	
	/**
	 * Optional flag.
	 * Whe it is set to true the path will be displayed as an optional label.
	 * 
	 * @type boolean
	 */
	this.optional	= false;
	
	/**
	 * Path segments of the label's boundaries.
	 * 
	 * @type Array
	 */
	this.pathSegments	= [];
	
	/**
	 * X-radius of label, backed up for performance issues.
	 * 
	 * @type int
	 */
	this.radiusx	= 0;
	
	/**
	 * Y-radius of label, backed up for performance issues.
	 * 
	 * @type int
	 */
	this.radiusy	= 0;
	
	/**
	 * A Raphael rect.
	 * 
	 * @see Paper.rect() at the <a href="http://raphaeljs.com/reference.html#Paper.rect">Raphael documentation</a>
	 * @type Element
	 */
	this.rectangle	= null;
	
	/**
	 * Selected flag.
	 * When set to true the label will be displayed in a different way.
	 * 
	 * @type boolean
	 */
	this.selected = false;
	
	/**
	 * The shape of the label.
	 * Depending on this value the multiRR, rectangle or ellipse objects are hidden / shown
	 * Possible values: "rectangle", "roundedrectangle", "roundedrectanglemulti", "circle", "ellipse"
	 * 
	 * @type String
	 */
	this.shape		= "";	// roundedrectangle
	
	/**
	 * The style information for this node.
	 * 
	 * @type Object
	 */
	this.style = gv_defaultStyle;
	
	/**
	 * A raphael text.
	 * 
	 * @see Paper.text() at the <a href="http://raphaeljs.com/reference.html#Paper.text">Raphael documentation</a>
	 * @type Element
	 */
	this.text		= null;
	
	/**
	 * The textAlign Attribute of the style.
	 * This depends on the text of the label.
	 * When the text contains a list element the value of this attribute is set to "textAlignLi".
	 * 
	 * @type String
	 */
	this.textAlignAttribute	= "textAlign";
	
	/**
	 * Holds the current position of the text.
	 * 
	 * @type Object
	 */
	this.textPosition	= {x: 0, y: 0};
	
	/**
	 * Backup the label's text.
	 * 
	 * @type String
	 */
	this.textString		= "";
	
	/**
	 * The x ordinate of the top left corner.
	 * 
	 * @type int
	 */
	this.x = 0;
	
	/**
	 * The y ordinate of the top left corner.
	 * 
	 * @type int
	 */	
	this.y = 0;
	
	/**
	 * Activate the label and update its look.
	 * 
	 * @returns {void}
	 */
	this.activate = function ()
	{
		this.deactive = false;
		this.refreshStyle();
	};
	
	/**
	 * Activate the event handlers for click and dblClick on this object depending on the current graph.
	 * 
	 * @param {String} graph Indicates which graph is currently shown. Depending on this the called methods on an event vary. Possible values: "cv" (communication view), "bv" (behavioral view)
	 * @returns {void}
	 */
	this.click = function (graph)
	{
		if (gf_isset(graph))
		{
			graph = graph.toLowerCase();
			id = this.id;
			
			// set the event handlers for the communication view (label is a subject)
			if (graph == "cv")
			{
				if (this.rectangle != null)
				{
					this.rectangle.click(function () {gf_paperClickNodeC(id); });
					this.rectangle.dblclick(function () {gf_paperDblClickNodeC(id); });
				}
				
				if (this.ellipse != null)
				{
					this.ellipse.click(function () {gf_paperClickNodeC(id); });
					this.ellipse.dblclick(function () {gf_paperDblClickNodeC(id); });
				}
				
				for (rrId in this.multiRR)
	 			{
	 				if (this.multiRR[rrId] != null)
	 				{
		 				this.multiRR[rrId].click(function () {gf_paperClickNodeC(id);});
		 				this.multiRR[rrId].dblclick(function () {gf_paperDblClickNodeC(id); });
	 				}
	 			}
			}
			
			// set the event handlers for the behavioral view (label is either a node or the label of an edge)
			else if (graph == "bv")
			{
				if (this.belongsToPath)
				{
					if (this.rectangle != null)
						this.rectangle.click(function () {gf_paperClickEdge(id); });
						
					if (this.ellipse != null)
						this.ellipse.click(function () {gf_paperClickEdge(id); });
				}
				else
				{
					if (this.rectangle != null)
						this.rectangle.click(function () {gf_paperClickNodeB(id); });
					
					if (this.ellipse != null)
						this.ellipse.click(function () {gf_paperClickNodeB(id); });
				}
			}
			
			// set the event handlers for the behavioral view (double click for macro nodes)
			else if (graph == "bv_dblclick")
			{
				if (this.rectangle != null)
				{
					this.rectangle.click(function () {gf_paperClickNodeB(id); });
					this.rectangle.dblclick(function () {gf_paperDblClickNodeB(id); });
				}
				
				if (this.ellipse != null)
				{
					this.ellipse.click(function () {gf_paperClickNodeB(id); });
					this.ellipse.dblclick(function () {gf_paperDblClickNodeB(id); });
				}
			}
			
			// set the event handlers for the behavioral view (no click)
			else if (graph == "bv_noclick")
			{
				// no click events
			}
			
			if (this.text != null)
				$(this.text.node).css("pointer-events", "none");
				
			if (this.img != null)
				$(this.img.node).css("pointer-events", "none");
		}
	};
	
	/**
	 * Deactivate the label and update its look.
	 * 
	 * @param {boolean} performanceMode When set to true, the style won't be refreshed.
	 * @returns {void}
	 */
	this.deactivate = function (performanceMode)
	{
		if (!gf_isset(performanceMode) || performanceMode != true)
			performanceMode	= false;
			
		this.deactive = true;
		
		if (!performanceMode)
			this.refreshStyle();
	};
	
	/**
	 * Deselect the label and update its look.
	 * 
	 * @returns {void}
	 */
	this.deselect = function ()
	{
		this.selected = false;
		this.refreshStyle();
	};
	
	/**
	 * Returns the boundaries of this label.
	 * The resulting object contains the following information:
	 * <br />
	 * - x: the x ordinate of the label's center<br />
	 * - y: the y ordinate of the label's center<br />
	 * - top: the top of the label<br />
	 * - bottom: the bottom of the label<br />
	 * - left: the left of the label<br />
	 * - right: the right of the label<br />
	 * - width: the width of the label<br />
	 * - height: the height of the label
	 * 
	 * @returns {Object} The boundaries of this label.
	 */
	this.getBoundaries = function ()
	{
		var bbox = {x: 0, y: 0, top: 0, bottom: 0, left: 0, right: 0, width: 0, height: 0};
		
		// when the shape is "roundedrectanglemulti" calculate the shape from the first and the last roundedrectangle on the stack
		if (this.shape == "roundedrectanglemulti")
		{
			var bbox1	= this.rectangle.getBBox();
			var bbox2	= this.multiRR[this.multiRR.length - 1].getBBox();
			
			bbox.top	= Math.round(bbox2.y);
			bbox.bottom	= Math.round(bbox1.y2);
			bbox.left	= Math.round(bbox1.x);
			bbox.right	= Math.round(bbox2.x2);
			bbox.width	= Math.round(bbox.right) - Math.round(bbox.left);
			bbox.height	= Math.round(bbox.bottom) - Math.round(bbox.top);
			bbox.x		= Math.round(bbox1.x) + Math.round(bbox1.width / 2);
			bbox.y		= Math.round(bbox1.y) + Math.round(bbox1.height / 2);
		}
		
		// for all other shapes simply calculate the boundaries of the element referenced by bboxObj
		else
		{
			var bbox1	= this.bboxObj.getBBox();
			
			bbox.top	= Math.round(bbox1.y);
			bbox.bottom	= Math.round(bbox1.y2);
			bbox.left	= Math.round(bbox1.x);
			bbox.right	= Math.round(bbox1.x2);
			bbox.width	= Math.round(bbox1.width);
			bbox.height	= Math.round(bbox1.height);
			bbox.x		= Math.round(bbox1.x) + Math.round(bbox1.width / 2);
			bbox.y		= Math.round(bbox1.y) + Math.round(bbox1.height / 2);
		}
		
		return bbox;
	};
	
	/**
	 * Returns the coordinates of this label.
	 * 
	 * @returns {Object} The coordinates of this label.
	 */
	this.getPosition = function ()
	{
		return {x: this.x, y: this.y};
	};
		
	/**
	 * Update the value of the textAlignAttribute depending on the new text of this label.
	 * 
	 * @param {String} text The text to check for list elements.
	 * @returns {void}
	 */
	this.getTextAlignAttribute = function (text)
	{
		this.textAlignAttribute	= "textAlign";
		
		if (text.search(/<li>|<li \/>|<li\/>/gi) > -1)
		{
			this.textAlignAttribute += "Li";
		}
	};
	
	/**
	 * Hide the element.
	 * Hide all Raphael Elements of this label.
	 * 
	 * @returns {void}
	 */
	this.hide = function ()
	{
		this.hideObjects();
		
		if (this.text != null)
			this.text.hide();
		
		if (this.img != null)
			this.img.hide();
	};
	
	/**
	 * Hide all Raphael Elements of this label.
	 * 
	 * @returns {void}
	 */
	this.hideObjects = function ()
	{
		for (rrId in this.multiRR)
 		{
 			if (this.multiRR[rrId] != null)
				this.multiRR[rrId].hide();
		}
		
		if (this.rectangle != null)
			this.rectangle.hide();
		
		if (this.ellipse != null)
			this.ellipse.hide();
	};
	
	/**
	 * Initialize the Raphael Elements of this label.
	 * 
	 * @returns {void}
	 */
	this.init = function ()
	{
		gf_timeCalc("label - init", this.belongsToPath);
		
		// create the Raphael elements
		this.multiRR[3]	= gv_paper.rect(0, 0, 0, 0, 0);
		this.multiRR[2]	= gv_paper.rect(0, 0, 0, 0, 0);
		this.multiRR[1]	= gv_paper.rect(0, 0, 0, 0, 0);
		this.rectangle	= gv_paper.rect(0, 0, 0, 0, 0);
		this.ellipse	= gv_paper.ellipse(0, 0, 0, 0);
		// this.img		= gv_paper.image(gv_emptyImgPath, 0, 0, 0, 0);
		
		// gf_timeCalc("label - init - text", this.belongsToPath);
		// this.text		= gv_paper.text(0, 0, "");
		// gf_timeCalc("label - init - text", this.belongsToPath);
		
		gf_timeCalc("label - init", this.belongsToPath);
		
		this.bboxObj	= this.rectangle;
	};
	
	/**
	 * Returns true when the label is set to be optional.
	 * 
	 * @returns {boolean} True when the label is set to be optional.
	 */
	this.isOptional = function ()
	{
		return this.optional === true;
	};
	
	/**
	 * Read a value from the style set.
	 * 
	 * @param {String} key The key to read from the style set.
	 * @param {String} type A type to determine the default value if the key was not found in the style.
	 * @return {String} The style value.
	 */
	this.readStyle = function (key, type)
	{
		return gf_getStyleValue(this.style, key, type);
	};
	
	/**
	 * Update the style information of all Raphael elements belonging to this label.
	 * 
	 * @returns {void}
	 */
	this.refreshStyle = function ()
	{
		
		if (this.belongsToPath === true)
			gf_taskCounterCount("label - refresh style");
		
		/*
		 * status dependent styles
		 */
		var statusDependent = "";
		if (this.optional === true)
		{
			statusDependent += "Opt";
		}
		if (this.selected === true)
		{
			statusDependent += "Sel";
		}
		if (this.deactive === true)
		{
			statusDependent += "Deact";
		}
		
		var strokeDasharray	= gf_getStrokeDasharray(this.readStyle("borderStyle" + statusDependent, ""));
		var strokeWidth		= strokeDasharray == "none" ? 0 : this.readStyle("borderWidth" + statusDependent, "int");
		var textAlign		= gf_getTextPosition(this.readStyle(this.textAlignAttribute, ""), "").align;
		
		var params						= {};
			params["opacity"]			= this.readStyle("opacity" + statusDependent, "float");
			params["stroke-opacity"]	= this.readStyle("borderOpacity" + statusDependent, "float");
			params["stroke-width"]		= strokeWidth;
			params["stroke-dasharray"]	= strokeDasharray;
			params["fill-opacity"]		= this.readStyle("bgOpacity" + statusDependent, "float");
			params["stroke"]			= this.readStyle("borderColor" + statusDependent, "");
			params["fill"]				= this.readStyle("bgColor" + statusDependent, "");
		
		// apply rectangle style information
		if (this.shape == "roundedrectangle" || this.shape == "rectangle" || this.shape == "roundedrectanglemulti")
		{
			this.rectangle.attr(params);
		}
		
		// apply rr1-3 style information
		if (this.shape == "roundedrectanglemulti")
		{
			for (rrId in this.multiRR)
		 	{
				this.multiRR[rrId].attr(params);
			}
		}
		
		// apply ellipse style information
		if (this.shape == "circle" || this.shape == "ellipse")
		{
			this.ellipse.attr(params);
		}
		
		// apply text style information
			params						= {};
			params["opacity"]			= this.readStyle("opacity" + statusDependent, "float");
			params["fill-opacity"]		= this.readStyle("fontOpacity" + statusDependent, "float");
			params["fill"]				= this.readStyle("fontColor" + statusDependent, "");
			params["font-weight"]		= this.readStyle("fontWeight" + statusDependent, "");
			params["font-size"]			= this.readStyle("fontSize", "int");
			params["font-family"]		= this.readStyle("fontFamily", "");
			params["text-anchor"]		= textAlign;
			
		if (this.textString != "")
			this.text.attr(params);
		
		// textVAlign: "top",	// TODO / remove?
		
		this.updateBoundaries();
	};
	
	/**
	 * Replace all new line characters and the li identifieres by \n and the liSymbol defined in the style set.
	 * 
	 * @param {String} text The text to process.
	 * @returns {String} The processed text.
	 */
	this.replaceNewline = function (text)
	{
		return gf_replaceNewline(text).replace(/<li>|<li \/>|<li\/>/gi, this.readStyle("liSymbol", ""));
	};
	
	/**
	 * Select the label and update its look.
	 * 
	 * @param {boolean} performanceMode When set to true, the style won't be refreshed.
	 * @returns {void}
	 */
	this.select = function (performanceMode)
	{
		if (!gf_isset(performanceMode) || performanceMode != true)
			performanceMode	= false;
			
		this.selected = true;
		
		if (!performanceMode)
			this.refreshStyle();
	};
	
	/**
	 * Updates the source and dimensions of the label's image.
	 * 
	 * @param {String} src The source of the image.
	 * @param {int} width The width of the image.
	 * @param {int} height The height of the image.
	 * @returns {void}
	 */
	this.setImg = function (src, width, height)
	{
		// update the source
		if (gf_isset(src) && src != "" && src != null)
		{
			var params	= {src: src};
			
			// update the dimensions and the position
			if (gf_isset(width, height))
			{
				params.width	= width;
				params.height	= height;
				
				params.x		= this.x - Math.round(width / 2);
				params.y		= this.y - Math.round(height / 2);
			}
			
			if (this.img == null)
				this.img = gv_paper.image(src, params.x, params.y, params.width, params.height);
			else
				this.img.attr(params);
		}
	};
	
	/**
	 * Set the optional flag to the label.
	 * 
	 * @param {boolean} optional When set to true the label will be set to be optional.
	 * @param {boolean} performanceMode When set to true, the style won't be refreshed.
	 * @returns {void}
	 */
	this.setOptional = function (optional, performanceMode)
	{
		if (!gf_isset(performanceMode) || performanceMode != true)
			performanceMode	= false;
			
		this.optional = gf_isset(optional) && optional === true;
		
		if (!performanceMode)
			this.refreshStyle();
	};
	
	/**
	 * Moves the label to the given position.
	 * 
	 * @param {int} x The x ordinate of the new position.
	 * @param {int} y The y ordinate of the new position.
	 * @param {int} performanceMode The performance mode will be passed to the updateBoundaries method to reduce the number of calculations done as they are obsolete in some cases.
	 * @returns {void}
	 */
	this.setPosition = function (x, y, performanceMode)
	{
		if (gf_isset(x, y))
		{
			if (!gf_isset(performanceMode))
				performanceMode = 0;
				
			this.x = x;
			this.y = y;
				
			// performance mode to ignore unnecessary calculations	
			if (performanceMode == 1 || performanceMode == 2)
			{
				if (this.shape == "circle" || this.shape == "ellipse")
				{
					this.ellipse.attr({cx: x, cy: y});
				}
				else if (this.shape == "roundedrectangle" || this.shape == "rectangle" || this.shape == "roundedrectanglemulti")
				{
					this.rectangle.attr({x: (x - this.radiusx), y: (y - this.radiusy)});
				}
				
				if (performanceMode == 2)
					this.updateBoundariesText();
			}
			else
			{
				this.updateBoundaries();
			}
		}
	};
	
	/**
	 * Update the shape of the label.
	 * 
	 * @param {String} shape The new shape. Possible values are "circle", "ellipse", "rectangle", "roundedrectangle", "roundedrectanglemulti"
	 * @param {boolean} performanceMode When set to true, the style won't be refreshed.
	 * @returns {void}
	 */
	this.setShape = function (shape, performanceMode)
	{
		if (!gf_isset(performanceMode) || performanceMode != true)
			performanceMode	= false;
			
		if (gf_isset(shape))
		{
			shape = shape.toLowerCase();
			
			this.hideObjects();
			
			if (this.textString != "" || this.belongsToPath !== true) // !(this.textString == "" && this.belongsToPath === true)
			{
				if (shape == "circle" || shape == "ellipse")
				{
					if (this.ellipse == null)
						this.ellipse	= gv_paper.ellipse(0, 0, 0, 0);
					
					this.shape		= shape;
					this.bboxObj	= this.ellipse;
					this.ellipse.show();
				}
				else if (shape == "rectangle" || shape == "roundedrectangle")
				{
			
					if (this.rectangle == null)
						this.rectangle	= gv_paper.rect(0, 0, 0, 0, 0);
						
					this.shape		= shape;
					this.bboxObj	= this.rectangle;
					this.rectangle.show();
				}
				else if (shape == "roundedrectanglemulti")
				{
					
					if (this.rectangle == null)
						this.rectangle	= gv_paper.rect(0, 0, 0, 0, 0);
					
					this.shape		= shape;
					
					for (rrId in this.multiRR)
		 			{
						if (this.rectangle == null)
							this.multiRR[rrId]	= gv_paper.rect(0, 0, 0, 0, 0);
							
						this.multiRR[rrId].show();
					}
					this.rectangle.show();
				}
				
				if (!performanceMode)
					this.refreshStyle();
			}
		}
	};
	
	/**
	 * Loads a new style set and calls the refreshStyle method.
	 * 
	 * @param {Object} style The style set to load.
	 * @param {boolean} performanceMode When set to true, the style won't be refreshed.
	 * @returns {void}
	 */
	this.setStyle = function (style, performanceMode)
	{
		if (!gf_isset(performanceMode) || performanceMode != true)
			performanceMode	= false;
			
		this.style = gf_mergeStyles(gv_defaultStyle, style);
		
		if (!performanceMode)
			this.refreshStyle();
	};
	
	/**
	 * Set a new text to the label.
	 * 
	 * @param {String} text The new text.
	 * @param {boolean} performanceMode When set to true, the style won't be refreshed.
	 * @returns {void}
	 */
	this.setText = function (text, performanceMode)
	{
		if (!gf_isset(performanceMode) || performanceMode != true)
			performanceMode	= false;
			
		var gt_text	= this.replaceNewline(text);
		
		if (gt_text != this.textString)
		{			
			this.getTextAlignAttribute(text);
			this.textString	= gt_text;
			
			if (this.text == null)
				this.text		= gv_paper.text(0, 0, this.textString);
			else
				this.text.attr("text", this.textString);
			
			if (!performanceMode)
				this.refreshStyle();
		}
	};
	
	/**
	 * Show the label.
	 * 
	 * @param {boolean} performanceMode When set to true, the style won't be refreshed.
	 * @returns {void}
	 */
	this.show = function (performanceMode)
	{
		if (!gf_isset(performanceMode) || performanceMode != true)
			performanceMode	= false;
			
		this.setShape(this.shape, performanceMode);
		
		if (this.text != null)
			this.text.show();
			
		if (this.img != null)
			this.img.show();
	};
	
	/**
	 * Returns a path representation of the label's diagonals.
	 * This is needed for intersection checks.
	 * 
	 * @returns {String} A path representation of the label's diagonals.
	 */
	this.toPath = function ()
	{
		var gt_bbox = this.getBoundaries();
		
		// simple path (two diagonal lines)
		return "M" + gt_bbox.left + "," + gt_bbox.top + "L" + gt_bbox.right + "," + gt_bbox.bottom + "M" + gt_bbox.left + "," + gt_bbox.bottom + "L" + gt_bbox.right + "," + gt_bbox.top;
	};
	
	/**
	 * Returns the label's boundaries as path segments.
	 * This is needed for intersection checks.
	 * 
	 * @returns {Array} Array of path segments {x,y}.
	 */
	this.toPathSegments = function ()
	{
		return this.pathSegments;
	}
	
	/**
	 * Update the boundaries of the Raphael Elements that are associated with this label depending on the information stored in this label.
	 * 
	 * @returns {void}
	 */
	this.updateBoundaries = function ()
	{
		// TODO: some more options like apply padding and move the text according to the new position
		
		var gt_textBBox	= this.textString == "" ? {top: 0, left: 0, right: 0, bottom: 0, width: 0, height: 0} : this.text.getBBox();
		
		this.updateBoundariesText(gt_textBBox);

		
		var paddingLeft		= this.readStyle("paddingLeft", "int");
		var paddingRight	= this.readStyle("paddingRight", "int");
		var paddingTop		= this.readStyle("paddingTop", "int");
		var paddingBottom	= this.readStyle("paddingBottom", "int");
		var styleWidth		= this.readStyle("width", "int");
		var styleHeight		= this.readStyle("height", "int");
		
		// apply the width and height information
		var width	= Math.round(gt_textBBox.width);
		var height	= Math.round(gt_textBBox.height);
		var width2	= styleWidth > 0 ? styleWidth : Math.max(width + paddingLeft + paddingRight, this.readStyle("minWidth", "int"));
		var height2	= styleHeight > 0 ? styleHeight : Math.max(height + paddingTop + paddingBottom, this.readStyle("minHeight", "int"));
		var radiusx	= Math.round(width2 / 2);
		var radiusy	= Math.round(height2 / 2);
		var radius	= Math.max(radiusx, radiusy);
		var rectR	= this.shape == "roundedrectangle" || this.shape == "roundedrectanglemulti" ? this.readStyle("rectangleRadius", "int") : 0;
		
		var bbox	= {top: 0, left: 0, right: 0, bottom: 0};
		
		// backup radiusx, radiusy for performance optimization
		this.radiusx	= radiusx;
		this.radiusy	= radiusy;
		
		if (this.shape == "circle")
		{
			this.ellipse.attr({cx: this.x, cy: this.y, rx: radius, ry: radius});
			
			bbox	= {top: this.y - radius, bottom: this.y + radius, left: this.x - radius, right: this.x + radius};
		}
		else if (this.shape == "ellipse")
		{
			this.ellipse.attr({cx: this.x, cy: this.y, rx: radiusx, ry: radiusy});
			
			bbox	= {top: this.y - radiusy, bottom: this.y + radiusy, left: this.x - radiusx, right: this.x + radiusx};
		}
		else if (this.shape == "roundedrectangle" || this.shape == "rectangle" || this.shape == "roundedrectanglemulti")
		{
	 		this.rectangle.attr({x: (this.x - radiusx), y: (this.y - radiusy), width: width2, height: height2, r: rectR});
			
			bbox	= {top: this.y - radiusy, bottom: this.y - radiusy + height2, left: this.x - radiusx, right: this.x - radiusx + width2};
		}
		
		if (this.shape == "roundedrectanglemulti")
		{
			var rrOverlap = 5;
			 		
	 		for (rrId in this.multiRR)
	 		{
		 		this.multiRR[rrId].attr({x: (this.x - radiusx + rrId * rrOverlap), y: (this.y - radiusy - rrId * rrOverlap), width: width2, height: height2, r: rectR});
	 		}			
		}
		
		this.updatePathSegments(bbox);
	};
	
	/**
	 * Updates the position of the text.
	 * 
	 * @param {Object} bbox The BBox of the label's text element.
	 * @returns {void}
	 */
	this.updateBoundariesText = function (bbox)
	{
		var textX	= this.x;
		
		if (this.text != null && this.textString != "")
		{		
			if (!gf_isset(bbox))
				bbox	= this.textString == "" ? {width: 0, height: 0} : this.text.getBBox();
	
			// correct the text position depending on the textAlign
			if (gf_getTextPosition(this.readStyle(this.textAlignAttribute, ""), "").align == "start")
			{
				textX	= this.x - bbox.width / 2;
			}
			else if (gf_getTextPosition(this.readStyle(this.textAlignAttribute, ""), "").align == "end")
			{
				textX	= this.x + bbox.width / 2;
			}
	
			if (textX != this.textPosition.x || this.y != this.textPosition.y)
			{				
				this.textPosition	= {x: textX, y: this.y};
				this.text.attr(this.textPosition);
				this.text.toFront();
			}
		}
	};
	
	/**
	 * Updates the path segments of this label's boundaries.
	 * 
	 * @param {Object} gt_bbox The BBox of this label.
	 * @returns {void}
	 */
	this.updatePathSegments = function (gt_bbox)
	{
		if (!gf_isset(gt_bbox))
			gt_bbox = this.getBoundaries();
		
		this.pathSegments	= [{x: gt_bbox.left, y: gt_bbox.top}, {x: gt_bbox.right, y: gt_bbox.top}, {x: gt_bbox.right, y: gt_bbox.bottom}, {x: gt_bbox.left, y: gt_bbox.bottom}, {x: gt_bbox.left, y: gt_bbox.top}];
	};
	
	// update the belongsToPath attribute
	if (gf_isset(belongsToPath) && belongsToPath === true)
		this.belongsToPath = true;
	
	if (!gf_isset(performanceMode) || performanceMode != true)
		performanceMode	= false;
	
	// initialize the label
	// this.init();
	
	// set the position of the label
	this.x	= x;
	this.y	= y;
	
	// set the id
	this.id = id;
	
	// set the text
	this.setText(text, true);
	
	// set the shape
	this.setShape(shape, true);
	
	// add the label to the objects array
	if (!this.belongsToPath)
		gv_objects_nodes[id] = this;
}