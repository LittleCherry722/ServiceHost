/**
 * S-BPM Groupware v1.0
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
 * @returns {void}
 */
function GClabel (x, y, text, shape, id, belongsToPath)
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
	this.multiRR	= [];
	
	/**
	 * Optional flag.
	 * Whe it is set to true the path will be displayed as an optional label.
	 * 
	 * @type boolean
	 */
	this.optional	= false;
	
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
	this.shape		= "roundedrectangle";
	
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
				this.rectangle.click(function () {gf_paperClickNodeC(id); });
				this.ellipse.click(function () {gf_paperClickNodeC(id); });
				
				this.rectangle.dblclick(function () {gf_paperDblClickNodeC(id); });
				this.ellipse.dblclick(function () {gf_paperDblClickNodeC(id); });
				
				for (rrId in this.multiRR)
	 			{
	 				this.multiRR[rrId].click(function () {gf_paperClickNodeC(id);});
	 				this.multiRR[rrId].dblclick(function () {gf_paperDblClickNodeC(id); });
	 			}
			}
			
			// set the event handlers for the behavioral view (label is either a node or the label of an edge)
			else if (graph == "bv")
			{
				if (this.belongsToPath)
				{
					this.rectangle.click(function () {gf_paperClickEdge(id); });
					this.ellipse.click(function () {gf_paperClickEdge(id); });
				}
				else
				{
					this.rectangle.click(function () {gf_paperClickNodeB(id); });
					this.ellipse.click(function () {gf_paperClickNodeB(id); });
				}
			}
			$(this.text.node).css("pointer-events", "none");
			$(this.img.node).css("pointer-events", "none");
		}
	};
	
	/**
	 * Deactivate the label and update its look.
	 * 
	 * @returns {void}
	 */
	this.deactivate = function ()
	{
		this.deactive = true;
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
			var bbox2	= this.multiRR[3].getBBox();
			
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
		this.text.hide();
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
			this.multiRR[rrId].hide();
		}
		this.rectangle.hide();
		this.ellipse.hide();
	};
	
	/**
	 * Initialize the Raphael Elements of this label.
	 * 
	 * @returns {void}
	 */
	this.init = function ()
	{
		// create the Raphael elements
		this.multiRR[3]	= gv_paper.rect(0, 0, 0, 0, 0);
		this.multiRR[2]	= gv_paper.rect(0, 0, 0, 0, 0);
		this.multiRR[1]	= gv_paper.rect(0, 0, 0, 0, 0);
		this.rectangle	= gv_paper.rect(0, 0, 0, 0, 0);
		this.ellipse	= gv_paper.ellipse(0, 0, 0, 0);
		this.text		= gv_paper.text(0, 0, "");
		this.img		= gv_paper.image("tk_graph/img/" + gv_nodeTypeImg.emptyNodeImg, 0, 0, gv_bv_circleNode.imgWidth, gv_bv_circleNode.imgHeight);		
		
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
		
		// apply rectangle style information
		this.rectangle.attr("opacity", this.readStyle("opacity" + statusDependent, "float"));
		this.rectangle.attr("stroke-opacity", this.readStyle("borderOpacity" + statusDependent, "float"));
		this.rectangle.attr("stroke-width", strokeWidth);
		this.rectangle.attr("stroke-dasharray", strokeDasharray);
		this.rectangle.attr("fill-opacity", this.readStyle("bgOpacity" + statusDependent, "float"));
		this.rectangle.attr("stroke", this.readStyle("borderColor" + statusDependent, ""));
		this.rectangle.attr("fill", this.readStyle("bgColor" + statusDependent, ""));
		
		// apply rr1-3 style information
		for (rrId in this.multiRR)
	 	{
			this.multiRR[rrId].attr("opacity", this.readStyle("opacity" + statusDependent, "float"));
			this.multiRR[rrId].attr("stroke-opacity", this.readStyle("borderOpacity" + statusDependent, "float"));
			this.multiRR[rrId].attr("stroke-width", strokeWidth);
			this.multiRR[rrId].attr("stroke-dasharray", strokeDasharray);
			this.multiRR[rrId].attr("fill-opacity", this.readStyle("bgOpacity" + statusDependent, "float"));
			this.multiRR[rrId].attr("stroke", this.readStyle("borderColor" + statusDependent, ""));
			this.multiRR[rrId].attr("fill", this.readStyle("bgColor" + statusDependent, ""));
		}
		
		// apply ellipse style information
		this.ellipse.attr("opacity", this.readStyle("opacity" + statusDependent, "float"));
		this.ellipse.attr("stroke-opacity", this.readStyle("borderOpacity" + statusDependent, "float"));
		this.ellipse.attr("stroke-width", strokeWidth);
		this.ellipse.attr("stroke-dasharray", strokeDasharray);
		this.ellipse.attr("fill-opacity", this.readStyle("bgOpacity" + statusDependent, "float"));
		this.ellipse.attr("stroke", this.readStyle("borderColor" + statusDependent, ""));
		this.ellipse.attr("fill", this.readStyle("bgColor" + statusDependent, ""));
		
		// apply text style information
		this.text.attr("opacity", this.readStyle("opacity" + statusDependent, "float"));
		this.text.attr("fill-opacity", this.readStyle("fontOpacity" + statusDependent, "float"));
		this.text.attr("fill", this.readStyle("fontColor" + statusDependent, ""));
		this.text.attr("font-weight", this.readStyle("fontWeight" + statusDependent, ""));
		this.text.attr("font-size", this.readStyle("fontSize", "int"));
		this.text.attr("font-family", this.readStyle("fontFamily", ""));
		this.text.attr("text-anchor", textAlign);
		
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
	 * @returns {void}
	 */
	this.select = function ()
	{
		this.selected = true;
		this.refreshStyle();
	};
	
	/**
	 * Set the optional flag to the label.
	 * 
	 * @param {boolean} optional When set to true the label will be set to be optional.
	 */
	this.setOptional = function (optional)
	{
		this.optional = gf_isset(optional) && optional === true;
		this.refreshStyle();
	};
	
	/**
	 * Moves the label to the given position.
	 * 
	 * @param {int} x The x ordinate of the new position.
	 * @param {int} y The y ordinate of the new position.
	 */
	this.setPosition = function (x, y)
	{
		if (gf_isset(x, y))
		{
			this.x = x;
			this.y = y;
			this.updateBoundaries();
		}
	};
	
	/**
	 * Update the shape of the label.
	 * 
	 * @param {String} shape The new shape. Possible values are "circle", "ellipse", "rectangle", "roundedrectangle", "roundedrectanglemulti"
	 * @returns {void}
	 */
	this.setShape = function (shape)
	{
		
		if (gf_isset(shape))
		{
			shape = shape.toLowerCase();
			
			this.hideObjects();
			
			if (!(this.text.attr("text") == "" && this.belongsToPath === true))
			{
				if (shape == "circle" || shape == "ellipse")
				{
					this.shape		= shape;
					this.bboxObj	= this.ellipse;
					this.ellipse.show();
				}
				else if (shape == "rectangle" || shape == "roundedrectangle")
				{
					this.shape		= shape;
					this.bboxObj	= this.rectangle;
					this.rectangle.show();
				}
				else if (shape == "roundedrectanglemulti")
				{
					this.shape		= shape;
					
					for (rrId in this.multiRR)
		 			{
						this.multiRR[rrId].show();
					}
					this.rectangle.show();
				}
				
				this.refreshStyle();
			}
		}
	};
	
	/**
	 * Loads a new style set and calls the refreshStyle method.
	 * 
	 * @param {Object} style The style set to load.
	 * @returns {void}
	 */
	this.setStyle = function (style)
	{
		this.style = gf_mergeStyles(gv_defaultStyle, style);
		this.refreshStyle();
	};
	
	/**
	 * Set a new text to the label.
	 * 
	 * @param {String} text The new text.
	 * @returns {void}
	 */
	this.setText = function (text)
	{
		this.getTextAlignAttribute(text);
		this.text.attr("text", this.replaceNewline(text));
		
		if (gf_isset(text) && text != null && gf_isset(gv_nodeTypeImg[text]))
		{
			this.img.attr("src", "tk_graph/img/" + gv_nodeTypeImg[text]);
			this.text.attr("text", "");
		}
		else
		{
			this.text.attr("text", this.replaceNewline(text));	
		}
		
		this.refreshStyle();
	};
	
	/**
	 * Show the label.
	 * 
	 * @returns {void}
	 */
	this.show = function ()
	{
		this.setShape(this.shape);
		this.text.show();
	};
	
	/**
	 * Returns a path representation of the label's boundareis.
	 * This is needed for intersection checks.
	 * 
	 * @returns {String} A path representation of the label's boundaries.
	 */
	this.toPath = function ()
	{
		var gt_bbox = this.getBoundaries();
		
		return "M" + gt_bbox.left + "," + gt_bbox.top + "H" + gt_bbox.right + "V" + gt_bbox.bottom + "H" + gt_bbox.left + "Z";
	};
	
	/**
	 * Update the boundaries of the Raphael Elements that are associated with this label depending on the information stored in this label.
	 * 
	 * @returns {void}
	 */
	this.updateBoundaries = function ()
	{
		
		// TODO: some more options like apply padding and move the text according to the new position
		
		var textX	= this.x;
		
		// correct the text position depending on the textAlign
		if (gf_getTextPosition(this.readStyle(this.textAlignAttribute, ""), "").align == "start")
		{
			textX	= this.x - this.text.getBBox().width / 2;
		}
		else if (gf_getTextPosition(this.readStyle(this.textAlignAttribute, ""), "").align == "end")
		{
			textX	= this.x + this.text.getBBox().width / 2;
		}
		
		this.text.attr("x", textX);
		this.text.attr("y", this.y);
		
		var paddingLeft		= this.readStyle("paddingLeft", "int");
		var paddingRight	= this.readStyle("paddingRight", "int");
		var paddingTop		= this.readStyle("paddingTop", "int");
		var paddingBottom	= this.readStyle("paddingBottom", "int");
		var styleWidth		= this.readStyle("width", "int");
		var styleHeight		= this.readStyle("height", "int");		
		
		
		// apply the width and height information
		var width	= Math.round(this.text.getBBox().width);
		var height	= Math.round(this.text.getBBox().height);
		var width2	= styleWidth > 0 ? styleWidth : Math.max(width + paddingLeft + paddingRight, this.readStyle("minWidth", "int"));
		var height2	= styleHeight > 0 ? styleHeight : Math.max(height + paddingTop + paddingBottom, this.readStyle("minHeight", "int"));
		var radiusx	= Math.round(width2 / 2);
		var radiusy	= Math.round(height2 / 2);
		var radius	= Math.max(radiusx, radiusy);
		var rectR	= this.shape == "roundedrectangle" || this.shape == "roundedrectanglemulti" ? this.readStyle("rectangleRadius", "int") : 0;
		
		if (this.shape == "circle")
		{
			this.ellipse.attr("cx", this.x);
			this.ellipse.attr("cy", this.y);
			this.ellipse.attr("rx", radius);
			this.ellipse.attr("ry", radius);
			
			this.img.attr("x", this.x - Math.round(gv_bv_circleNode.imgWidth/2));
			this.img.attr("y", this.y - Math.round(gv_bv_circleNode.imgHeight/2));
		}
		else if (this.shape == "ellipse")
		{
			this.ellipse.attr("cx", this.x);
			this.ellipse.attr("cy", this.y);
			this.ellipse.attr("rx", radiusx);
			this.ellipse.attr("ry", radiusy);
		}
		else if (this.shape == "roundedrectangle" || this.shape == "rectangle" || this.shape == "roundedrectanglemulti")
		{
			this.rectangle.attr("x", this.x - radiusx);
	 		this.rectangle.attr("y", this.y - radiusy);
	 		this.rectangle.attr("width", width2);
	 		this.rectangle.attr("height", height2);
	 		this.rectangle.attr("r", rectR);
		}
		
		if (this.shape == "roundedrectanglemulti")
		{
			var rrOverlap = 5;
			 		
	 		for (rrId in this.multiRR)
	 		{
				this.multiRR[rrId].attr("x", this.x - radiusx + rrId * rrOverlap);
		 		this.multiRR[rrId].attr("y", this.y - radiusy - rrId * rrOverlap);
		 		this.multiRR[rrId].attr("width", width2);
		 		this.multiRR[rrId].attr("height", height2);
		 		this.multiRR[rrId].attr("r", rectR);
	 		}			
		}
	};
	
	// initialize the label
	this.init();
	
	// set the position of the label
	this.x	= x;
	this.y	= y;
	
	// set the id
	this.id = id;
	
	// update the belongsToPath attribute
	if (gf_isset(belongsToPath) && belongsToPath === true)
	{
		this.belongsToPath = true;
	};
	
	// set the text
	this.setText(text);
	
	// set the shape
	this.setShape(shape);
	
	// add the label to the objects array
	if (!this.belongsToPath)
		gv_objects_nodes[id] = this;
}