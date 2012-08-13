/*
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

/*
 * do NOT delete any line in this file; only edit the values
 */

/**
 * When set to true the tk_graph library will alter attributes of DOM Elements; when set to false the proper GUI has to make all changes
 * 
 * @type boolean
 */
gv_standAlone	= false;


/*
 * predefined macros
 */
// creates a macro with the ID "newSendNode" that will insert a new send node into the graph and connect it with the currently selected node
gf_createMacro("newSendNode", "", "normal", "send", true);

// creates a macro with the ID "newReceiveNode" that will insert a new receive node into the graph and connect it with the currently selected node
gf_createMacro("newReceiveNode", "", "normal", "receive", true);

// creates a macro with the ID "newActionNode" that will insert a new action node into the graph and connect it with the currently selected node
gf_createMacro("newActionNode", "internal action", "normal", "action", true);

/**
 * Contains the dimensions of both canvas elements.
 * Edit these lines in order to fit the canvas elements to your page.
 * 
 * @type Object
 */
var gv_paperSizes	= {
	bv_width: 5000,		// width of the canvas for the behavioral view
	bv_height: 6000,	// height of the canvas for the behavioral view
	cv_width: 2000,		// width of the canvas for the communication view
	cv_height: 500		// height of the canvas for the communication view
};

/**
 * Contains some information about the node types in the behavioral view.
 * This is the node's shape and its default text.
 * 
 * @type Object
 */
var gv_nodeTypes	= {
	// action node will be displayed as a rounded rectangle containing the text defined in the node
	action: {shape: "roundedrectangle", text: null},
	
	// a send node will be displayed as a circle containing an "S" to mark it as a send node
	send: {shape: "circle", text: "S"},
	
	// a receive node will be displayed as a circle containing an "R" to mark it as a receive node
	receive: {shape: "circle", text: "R"},
	
	// an end node will be displayed as a circle without text
	end: {shape: "circle", text: ""}
};

/**
 * Settings for zoom.
 * Contains zoom-limits (min and max) and default zoomSpeed.
 * 
 * @type Object
 */
var gv_zoomSettings	= {
	// min zoom level (can't zoom out further)
	min: 0.25,
	
	// max zoom level (can't zoom in further)
	max: 4,
	
	// default zoomFactor (zoom in)
	zoomIn: 1.25,
	
	// default zoomFactor (zoom out)
	zoomOut: 1.25,
	
	// default zoomFactor (mousewheel)
	wheel: 1.25
}

/*
 * status dependent styles
 * 
 * arrowColor
 * arrowOpacity
 * arrowStyle
 * arrowWidth
 * bgColor
 * bgOpacity
 * borderColor
 * borderOpacity
 * borderStyle
 * borderWidth
 * fontColor
 * fontOpacity
 * fontWeight
 * opacity
 */

/**
 * Default style set.
 * Please see a reference at <a href="https://sbpm-groupware.atlassian.net/wiki/display/SBPM/tk_graph+%28v0.9%29+documentation#tkgraphv09documentation-Mainconfiguration">Atlassian</a>.
 * 
 * @type Object
 */
var gv_defaultStyle	= {
	
	/*
	 * Arrow
	 */
	arrowHeadType: "none",				// possible values: classic, block, open, oval, diamond, none
	arrowHeadLength: "long",			// possible values: long, short, medium
	arrowHeadWidth: "wide",				// possible values: wide, narrow, medium
	arrowColor: "#000000",				// any hex-color-value
	arrowColorDeactivated: "#0000FF",	// any hex-color-value
	arrowColorSelDeactive: "#0000FF",	// any hex-color-value
	arrowColorSelected: "#0000FF",		// any hex-color-value
	arrowLinecap: "square",				// possible values: butt, square, round
	arrowLinejoin: "bevel",				// possible values: bevel, round, miter
	arrowMiterLimit: 0,					// any number
	arrowOpacity: 1.0,					// floating number
	arrowOpacityDeactivated: 1.0,		// floating number
	arrowOpacitySelDeactive: 1.0,		// floating number
	arrowOpacitySelected: 1.0,			// floating number
	arrowStyle: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleDeactivated: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleSelDeactive: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleSelected: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowWidth: 1,						// pixels
	arrowWidthDeactivated: 1,			// pixels
	arrowWidthSelDeactive: 1,			// pixels
	arrowWidthSelected: 1,				// pixels
	
	/*
	 * Border
	 */
	borderColor: "#000000",				// any hex-color-value
	borderColorDeactivated: "#0000FF",	// any hex-color-value
	borderColorSelDeactive: "#0000FF",	// any hex-color-value
	borderColorSelected: "#0000FF",		// any hex-color-value
	borderOpacity: 1.0,					// floating number
	borderOpacityDeactivated: 1.0,		// floating number
	borderOpacitySelDeactive: 1.0,		// floating number
	borderOpacitySelected: 1.0,			// floating number
	borderStyle: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleDeactivated: "solid",	// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelDeactive: "solid",	// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelected: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderWidth: 1,						// pixels
	borderWidthDeactivated: 1,			// pixels
	borderWidthSelDeactive: 1,			// pixels
	borderWidthSelected: 1,				// pixels

	/*
	 * Background
	 */		
	bgColor: "#C0FFFF",					// any hex-color-value
	bgColorDeactivated: "#C0FFFF",		// any hex-color-value
	bgColorSelDeactive: "#C0FFFF",		// any hex-color-value
	bgColorSelected: "#C0FFFF",			// any hex-color-value
	bgOpacity: 1.0,						// floating number
	bgOpacityDeactivated: 1.0,			// floating number
	bgOpacitySelDeactive: 1.0,			// floating number
	bgOpacitySelected: 1.0,				// floating number
	opacity: 1.0,						// floating number
	opacityDeactivated: 1.0,			// floating number
	opacitySelDeactive: 1.0,			// floating number
	opacitySelected: 1.0,				// floating number
	
	/*
	 * Text
	 */
	fontColor: "#000000",				// any hex-color-value
	fontColorDeactivated: "#000000",	// any hex-color-value
	fontColorSelDeactive: "#000000",	// any hex-color-value
	fontColorSelected: "#000000",		// any hex-color-value
	fontOpacity: 1.0,					// floating number
	fontOpacityDeactivated: 1.0,		// floating number
	fontOpacitySelDeactive: 1.0,		// floating number
	fontOpacitySelected: 1.0,			// floating number
	fontFamily: "Verdana, sans-serif",	// any font
	fontSize: 12,						// pixels
	fontWeight: "normal",				// possible values: normal, bold
	fontWeightDeactivated: "normal",	// possible values: normal, bold
	fontWeightSelDeactive: "normal",	// possible values: normal, bold
	fontWeightSelected: "normal",		// possible values: normal, bold
	paddingBottom: 5,					// pixels
	paddingLeft: 5,						// pixels
	paddingRight: 5,					// pixels
	paddingTop: 5,						// pixels
	textAlign: "middle",				// possible values: left, right, middle
	textAlignLi: "left",				// possible values: left, right, middle
	textVAlign: "top",					// possible values: top, bottom, middle
	
	/*
	 * Misc
	 */
	liSymbol: "\u2022 ",				// any unicode
	rectangleRadius: 0,					// radius for rounded rectangles
	width: 0,							// int
	height: 0,							// int
	minWidth: 0,						// int
	minHeight: 0						// int
};