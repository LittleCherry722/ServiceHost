/*
 * S-BPM Groupware v0.9
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
 * Styles (can be edited, added, deleted)
 */

/**
 * Default style set for edges between two nodes.
 * The given attributes overwrite the values of the default style set defined in tk_graph.config.js.
 * 
 * @type Object
 */
var gv_bv_style_edge = {
	/*
	 * Arrow
	 */
	arrowHeadType: "open",				// possible values: classic, block, open, oval, diamond, none
	arrowHeadLength: "long",			// possible values: long, short, medium
	arrowHeadWidth: "wide",				// possible values: wide, narrow, medium
	arrowColor: "#666666",				// any hex-color-value
	arrowColorDeactivated: "#666666",	// any hex-color-value
	arrowColorSelDeactive: "#0000FF",	// any hex-color-value
	arrowColorSelected: "#0000FF",		// any hex-color-value
	arrowLinecap: "square",				// possible values: butt, square, round
	arrowLinejoin: "bevel",				// possible values: bevel, round, miter
	arrowMiterLimit: 0,					// any number
	arrowOpacity: 1.0,					// floating number
	arrowOpacityDeactivated: 0.4,		// floating number
	arrowOpacitySelDeactive: 0.7,		// floating number
	arrowOpacitySelected: 1.0,			// floating number
	arrowStyle: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleDeactivated: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleSelDeactive: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleSelected: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowWidth: 2,						// pixels
	arrowWidthDeactivated: 2,			// pixels
	arrowWidthSelDeactive: 2,			// pixels
	arrowWidthSelected: 2,				// pixels
	
	/*
	 * Border
	 */
	borderColor: "#666666",				// any hex-color-value
	borderColorDeactivated: "#666666",	// any hex-color-value
	borderColorSelDeactive: "#0000FF",	// any hex-color-value
	borderColorSelected: "#0000FF",		// any hex-color-value
	borderOpacity: 1.0,					// floating number
	borderOpacityDeactivated: 0.4,		// floating number
	borderOpacitySelDeactive: 0.7,		// floating number
	borderOpacitySelected: 1.0,			// floating number
	borderStyle: ".",					// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleDeactivated: "solid",	// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelDeactive: "solid",	// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelected: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderWidth: 2,						// pixels
	borderWidthDeactivated: 2,			// pixels
	borderWidthSelDeactive: 2,			// pixels
	borderWidthSelected: 2,				// pixels

	/*
	 * Background
	 */		
	bgColor: "#F0FFFF",					// any hex-color-value
	bgColorDeactivated: "#F0FFFF",		// any hex-color-value
	bgColorSelDeactive: "#F0FFFF",		// any hex-color-value
	bgColorSelected: "#F0FFFF",			// any hex-color-value
	bgOpacity: 1.0,						// floating number
	bgOpacityDeactivated: 0.4,			// floating number
	bgOpacitySelDeactive: 0.7,			// floating number
	bgOpacitySelected: 1.0,				// floating number
	opacity: 1.0,						// floating number
	opacityDeactivated: 0.4,			// floating number
	opacitySelDeactive: 0.7,			// floating number
	opacitySelected: 1.0,				// floating number
	
	/*
	 * Text
	 */
	fontColor: "#000000",				// any hex-color-value
	fontColorDeactivated: "#000000",	// any hex-color-value
	fontColorSelDeactive: "#0000FF",	// any hex-color-value
	fontColorSelected: "#0000FF",		// any hex-color-value
	fontOpacity: 1.0,					// floating number
	fontOpacityDeactivated: 0.4,		// floating number
	fontOpacitySelDeactive: 0.7,		// floating number
	fontOpacitySelected: 1.0,			// floating number
	fontFamily: "Verdana, sans-serif",	// any font
	fontSize: 12,						// pixels
	fontWeight: "normal",				// possible values: normal, bold
	fontWeightDeactivated: "normal",	// possible values: normal, bold
	fontWeightSelDeactive: "normal",	// possible values: normal, bold
	fontWeightSelected: "normal",		// possible values: normal, bold
	// paddingBottom: 5,					// pixels
	// paddingLeft: 5,						// pixels
	// paddingRight: 5,					// pixels
	// paddingTop: 5,						// pixels
	textAlign: "middle",				// possible values: left, right, middle
	textAlignLi: "middle",				// possible values: left, right, middle
	textVAlign: "top",					// possible values: top, bottom, middle
	
	/*
	 * Misc
	 */
	liSymbol: "\u2022 ",				// any unicode
	rectangleRadius: 5,					// radius for rounded rectangles
	width: 0,							// int
	height: 0,							// int
	minWidth: 0,						// int
	minHeight: 0						// int
};

/**
 * Default style set for normal circle nodes.
 * The given attributes overwrite the values of the default style set defined in tk_graph.config.js.
 * 
 * @type Object
 */
var gv_bv_style_circleNode = {	
	/*
	 * Border
	 */
	borderColor: "#666666",				// any hex-color-value
	borderColorDeactivated: "#666666",	// any hex-color-value
	borderColorSelDeactive: "#0000FF",	// any hex-color-value
	borderColorSelected: "#0000FF",		// any hex-color-value
	borderOpacity: 1.0,					// floating number
	borderOpacityDeactivated: 0.4,		// floating number
	borderOpacitySelDeactive: 0.7,		// floating number
	borderOpacitySelected: 1.0,			// floating number
	borderStyle: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleDeactivated: "solid",	// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelDeactive: "solid",	// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelected: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderWidth: 2,						// pixels
	borderWidthDeactivated: 2,			// pixels
	borderWidthSelDeactive: 2,			// pixels
	borderWidthSelected: 2,				// pixels

	/*
	 * Background
	 */		
	bgColor: "#90FFFF",					// any hex-color-value
	bgColorDeactivated: "#90FFFF",		// any hex-color-value
	bgColorSelDeactive: "#90FFFF",		// any hex-color-value
	bgColorSelected: "#90FFFF",			// any hex-color-value
	bgOpacity: 1.0,						// floating number
	bgOpacityDeactivated: 0.4,			// floating number
	bgOpacitySelDeactive: 0.7,			// floating number
	bgOpacitySelected: 1.0,				// floating number
	opacity: 1.0,						// floating number
	opacityDeactivated: 0.4,			// floating number
	opacitySelDeactive: 0.7,			// floating number
	opacitySelected: 1.0,				// floating number
	
	/*
	 * Text
	 */
	fontColor: "#000000",				// any hex-color-value
	fontColorDeactivated: "#000000",	// any hex-color-value
	fontColorSelDeactive: "#0000FF",	// any hex-color-value
	fontColorSelected: "#0000FF",		// any hex-color-value
	fontOpacity: 1.0,					// floating number
	fontOpacityDeactivated: 0.4,		// floating number
	fontOpacitySelDeactive: 0.7,		// floating number
	fontOpacitySelected: 1.0,			// floating number
	fontFamily: "Verdana, sans-serif",	// any font
	fontSize: 12,						// pixels
	fontWeight: "normal",				// possible values: normal, bold
	fontWeightDeactivated: "normal",	// possible values: normal, bold
	fontWeightSelDeactive: "bold",	// possible values: normal, bold
	fontWeightSelected: "bold",			// possible values: normal, bold
	// paddingBottom: 5,					// pixels
	// paddingLeft: 5,						// pixels
	// paddingRight: 5,					// pixels
	// paddingTop: 5,						// pixels
	textAlign: "middle",				// possible values: left, right, middle
	textAlignLi: "middle",				// possible values: left, right, middle
	textVAlign: "top",					// possible values: top, bottom, middle
	
	/*
	 * Misc
	 */
	liSymbol: "\u2022 ",				// any unicode
	width: 0,							// int
	height: 0,							// int
	minWidth: 24,						// int
	minHeight: 24						// int
};

/**
 * Default style set for circle shaped start nodes.
 * The given attributes overwrite the values of the gv_bv_style_circleNode style set.
 * 
 * @type Object
 */
var gv_bv_style_circleNodeStart = {
	/*
	 * Border
	 */
	borderWidth: 3,						// pixels
	borderWidthDeactivated: 3,			// pixels
	borderWidthSelDeactive: 4,			// pixels
	borderWidthSelected: 4				// pixels
};

/**
 * Default style set for end nodes.
 * The given attributes overwrite the values of the gv_bv_style_circleNode style set.
 * 
 * @type Object
 */
var gv_bv_style_circleNodeEnd = {	
	/*
	 * Border
	 */
	borderWidth: 3,						// pixels
	borderWidthDeactivated: 3,			// pixels
	borderWidthSelDeactive: 4,			// pixels
	borderWidthSelected: 4,				// pixels

	/*
	 * Background
	 */		
	bgColor: "#C0DDDD",					// any hex-color-value
	bgColorDeactivated: "#C0DDDD",		// any hex-color-value
	bgColorSelDeactive: "#C0DDDD",		// any hex-color-value
	bgColorSelected: "#C0DDDD"			// any hex-color-value
};

/**
 * Default style set for rectangular nodes.
 * The given attributes overwrite the values of the default style set defined in tk_graph.config.js.
 * 
 * @type Object
 */
var gv_bv_style_rectNode = {
	/*
	 * Border
	 */
	borderColor: "#666666",				// any hex-color-value
	borderColorDeactivated: "#666666",	// any hex-color-value
	borderColorSelDeactive: "#0000FF",	// any hex-color-value
	borderColorSelected: "#0000FF",		// any hex-color-value
	borderOpacity: 1.0,					// floating number
	borderOpacityDeactivated: 0.4,		// floating number
	borderOpacitySelDeactive: 0.7,		// floating number
	borderOpacitySelected: 1.0,			// floating number
	borderStyle: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleDeactivated: "solid",	// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelDeactive: "solid",	// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelected: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderWidth: 2,						// pixels
	borderWidthDeactivated: 2,			// pixels
	borderWidthSelDeactive: 2,			// pixels
	borderWidthSelected: 2,				// pixels

	/*
	 * Background
	 */		
	bgColor: "#90FFFF",					// any hex-color-value
	bgColorDeactivated: "#90FFFF",		// any hex-color-value
	bgColorSelDeactive: "#90FFFF",		// any hex-color-value
	bgColorSelected: "#90FFFF",			// any hex-color-value
	bgOpacity: 1.0,						// floating number
	bgOpacityDeactivated: 0.4,			// floating number
	bgOpacitySelDeactive: 0.7,			// floating number
	bgOpacitySelected: 1.0,				// floating number
	opacity: 1.0,						// floating number
	opacityDeactivated: 0.4,			// floating number
	opacitySelDeactive: 0.7,			// floating number
	opacitySelected: 1.0,				// floating number
	
	/*
	 * Text
	 */
	fontColor: "#000000",				// any hex-color-value
	fontColorDeactivated: "#000000",	// any hex-color-value
	fontColorSelDeactive: "#0000FF",	// any hex-color-value
	fontColorSelected: "#0000FF",		// any hex-color-value
	fontOpacity: 1.0,					// floating number
	fontOpacityDeactivated: 0.4,		// floating number
	fontOpacitySelDeactive: 0.7,		// floating number
	fontOpacitySelected: 1.0,			// floating number
	fontFamily: "Verdana, sans-serif",	// any font
	fontSize: 12,						// pixels
	fontWeight: "normal",				// possible values: normal, bold
	fontWeightDeactivated: "normal",	// possible values: normal, bold
	fontWeightSelDeactive: "normal",	// possible values: normal, bold
	fontWeightSelected: "normal",		// possible values: normal, bold
	// paddingBottom: 5,					// pixels
	// paddingLeft: 5,						// pixels
	// paddingRight: 5,					// pixels
	// paddingTop: 5,						// pixels
	textAlign: "middle",				// possible values: left, right, middle
	textAlignLi: "middle",				// possible values: left, right, middle
	textVAlign: "top",					// possible values: top, bottom, middle
	
	/*
	 * Misc
	 */
	liSymbol: "\u2022 ",				// any unicode
	rectangleRadius: 4,					// radius for rounded rectangles
	width: 0,							// int
	height: 0,							// int
	minWidth: 50,						// int
	minHeight: 24						// int
};

/**
 * Default style set for rectangular start nodes.
 * The given attributes overwrite the values of the gv_bv_style_rectNode style set.
 * 
 * @type Object
 */
var gv_bv_style_rectNodeStart = {
	/*
	 * Border
	 */
	borderWidth: 3,						// pixels
	borderWidthDeactivated: 3,			// pixels
	borderWidthSelDeactive: 4,			// pixels
	borderWidthSelected: 4				// pixels
};

/*
 * do not delete any piece of information below this line; only edit the values
 */

/**
 * Port settings
 * 
 * @type Object
 */
var gv_bv_arrow = {
		style: gv_bv_style_edge,	// Style set to use for edges
		tout: true,					// Allow outgoing edges on the top of a node (true | false)
		tin: true,					// Allow incoming edges on the top of a node (true | false)
		lout: true,					// Allow outgoing edges on the left of a node (true | false)
		lin: true,					// Allow incoming edges on the left of a node (true | false)
		rout: true,					// Allow outgoing edges on the right of a node (true | false)
		rin: true,					// Allow incoming edges on the right of a node (true | false)
		bout: true,					// Allow outgoing edges on the bottom of a node (true | false)
		bin: true					// Allow incoming edges on the bottom of a node (true | false)
};

/**
 * Style sets for circle nodes.
 * 
 * @type Object
 */
var gv_bv_circleNode = {
		style: gv_bv_style_circleNode,				// style set for circle nodes
		styleStart: gv_bv_style_circleNodeStart,	// style set for circle start nodes
		styleEnd: gv_bv_style_circleNodeEnd			// style set for end nodes
};

/**
 * Style sets for rectangular nodes.
 * 
 * @type Object
 */
var gv_bv_rectNode = {
		style: gv_bv_style_rectNode,				// style set for rectangular nodes
		styleStart: gv_bv_style_rectNodeStart		// style set for rectangular start nodes
};

/**
 * Settings for the automated layouting algorithm.
 * 
 * @type Object
 */
var gv_bv_nodeSettings = {
		distanceX: 220,		// The distance between two nodes (x-axis)
		distanceY: 150,		// The distance between two nodes (y-axis)
		startX: 0,			// The x-ordinate of the starting position of the graph.
		startY: 50,			// The y-ordinate of the starting position of the graph.
		startNewX: 150,		// The x-ordinate of the starting position of unconnected nodes.
		startNewY: 50,		// The y-ordinate of the starting position of unconnected nodes.
		arrowSpace: 75		// Space needed for calculation
};