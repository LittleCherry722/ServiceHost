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
	action: {shape: "roundedrectangle", text: null, label: "action"},
	
	// a send node will be displayed as a circle containing an "S" to mark it as a send node
	send: {shape: "circle", text: "S", label: "send"},
	
	// a receive node will be displayed as a circle containing an "R" to mark it as a receive node
	receive: {shape: "circle", text: "R", label: "receive"},
	
	// an end node will be displayed as a circle without text
	end: {shape: "circle", text: "", label: "end"},
	
	// a modal split operator will be displayed as a circle with the letters "MS" on it
	modalsplit: {shape: "circle", text: "MS", label: "modal split"},
	
	// a modal join operator will be displayed as a circle with the letters "MJ" on it
	modaljoin: {shape: "circle", text: "MJ", label: "modal join"}
};

/**
 * Contains some predefined actions.
 * 
 * @type Object
 */
var gv_predefinedActions	= {
	// TODO
	closeip: {relatedSubject: true, message: true, label: "closeIP"},
	
	// TODO
	openip: {relatedSubject: true, message: true, label: "openIP"}
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
	arrowColorDeact: "#0000FF",			// any hex-color-value
	arrowColorOpt: "#0000FF",			// any hex-color-value
	arrowColorOptDeact: "#0000FF",		// any hex-color-value
	arrowColorOptSel: "#0000FF",		// any hex-color-value
	arrowColorOptSelDeact: "#0000FF",	// any hex-color-value
	arrowColorSel: "#0000FF",			// any hex-color-value
	arrowColorSelDeact: "#0000FF",		// any hex-color-value
	arrowLinecap: "square",				// possible values: butt, square, round
	arrowLinejoin: "bevel",				// possible values: bevel, round, miter
	arrowMiterLimit: 0,					// any number
	arrowOpacity: 1.0,					// floating number
	arrowOpacityDeact: 1.0,				// floating number
	arrowOpacityOpt: 1.0,				// floating number
	arrowOpacityOptDeact: 1.0,			// floating number
	arrowOpacityOptSel: 1.0,			// floating number
	arrowOpacityOptSelDeact: 1.0,		// floating number
	arrowOpacitySel: 1.0,				// floating number
	arrowOpacitySelDeact: 1.0,			// floating number
	arrowStyle: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleDeact: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleOpt: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleOptDeact: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleOptSel: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleOptSelDeact: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleSel: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleSelDeact: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowWidth: 1,						// pixels
	arrowWidthDeact: 1,					// pixels
	arrowWidthOpt: 1,					// pixels
	arrowWidthOptDeact: 1,				// pixels
	arrowWidthOptSel: 1,				// pixels
	arrowWidthOptSelDeact: 1,			// pixels
	arrowWidthSel: 1,					// pixels
	arrowWidthSelDeact: 1,				// pixels
	
	/*
	 * Border
	 */
	borderColor: "#000000",				// any hex-color-value
	borderColorDeact: "#0000FF",		// any hex-color-value
	borderColorOpt: "#0000FF",			// any hex-color-value
	borderColorOptDeact: "#0000FF",		// any hex-color-value
	borderColorOptSel: "#0000FF",		// any hex-color-value
	borderColorOptSelDeact: "#0000FF",	// any hex-color-value
	borderColorSel: "#0000FF",			// any hex-color-value
	borderColorSelDeact: "#0000FF",		// any hex-color-value
	borderOpacity: 1.0,					// floating number
	borderOpacityDeact: 1.0,			// floating number
	borderOpacityOpt: 1.0,				// floating number
	borderOpacityOptDeact: 1.0,			// floating number
	borderOpacityOptSel: 1.0,			// floating number
	borderOpacityOptSelDeact: 1.0,		// floating number
	borderOpacitySel: 1.0,				// floating number
	borderOpacitySelDeact: 1.0,			// floating number
	borderStyle: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleDeact: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleOpt: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleOptDeact: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleOptSel: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleOptSelDeact: "solid",	// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSel: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelDeact: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderWidth: 1,						// pixels
	borderWidthDeact: 1,				// pixels
	borderWidthOpt: 1,					// pixels
	borderWidthOptDeact: 1,				// pixels
	borderWidthOptSel: 1,				// pixels
	borderWidthOptSelDeact: 1,			// pixels
	borderWidthSel: 1,					// pixels
	borderWidthSelDeact: 1,				// pixels

	/*
	 * Background
	 */		
	bgColor: "#C0FFFF",					// any hex-color-value
	bgColorDeact: "#C0FFFF",			// any hex-color-value
	bgColorOpt: "#C0FFFF",				// any hex-color-value
	bgColorOptDeact: "#C0FFFF",			// any hex-color-value
	bgColorOptSel: "#C0FFFF",			// any hex-color-value
	bgColorOptSelDeact: "#C0FFFF",		// any hex-color-value
	bgColorSel: "#C0FFFF",				// any hex-color-value
	bgColorSelDeact: "#C0FFFF",			// any hex-color-value
	bgOpacity: 1.0,						// floating number
	bgOpacityDeact: 1.0,				// floating number
	bgOpacityOpt: 1.0,					// floating number
	bgOpacityOptDeact: 1.0,				// floating number
	bgOpacityOptSel: 1.0,				// floating number
	bgOpacityOptSelDeact: 1.0,			// floating number
	bgOpacitySel: 1.0,					// floating number
	bgOpacitySelDeact: 1.0,				// floating number
	opacity: 1.0,						// floating number
	opacityDeact: 1.0,					// floating number
	opacityOpt: 1.0,					// floating number
	opacityOptDeact: 1.0,				// floating number
	opacityOptSel: 1.0,					// floating number
	opacityOptSelDeact: 1.0,			// floating number
	opacitySel: 1.0,					// floating number
	opacitySelDeact: 1.0,				// floating number
	
	/*
	 * Text
	 */
	fontColor: "#000000",				// any hex-color-value
	fontColorDeact: "#000000",			// any hex-color-value
	fontColorOpt: "#000000",			// any hex-color-value
	fontColorOptDeact: "#000000",		// any hex-color-value
	fontColorOptSel: "#000000",			// any hex-color-value
	fontColorOptSelDeact: "#000000",	// any hex-color-value
	fontColorSel: "#000000",			// any hex-color-value
	fontColorSelDeact: "#000000",		// any hex-color-value
	fontOpacity: 1.0,					// floating number
	fontOpacityDeact: 1.0,				// floating number
	fontOpacityOpt: 1.0,				// floating number
	fontOpacityOptDeact: 1.0,			// floating number
	fontOpacityOptSel: 1.0,				// floating number
	fontOpacityOptSelDeact: 1.0,		// floating number
	fontOpacitySel: 1.0,				// floating number
	fontOpacitySelDeact: 1.0,			// floating number
	fontFamily: "Verdana, sans-serif",	// any font
	fontSize: 12,						// pixels
	fontWeight: "normal",				// possible values: normal, bold
	fontWeightDeact: "normal",			// possible values: normal, bold
	fontWeightOpt: "normal",			// possible values: normal, bold
	fontWeightOptDeact: "normal",		// possible values: normal, bold
	fontWeightOptSel: "normal",			// possible values: normal, bold
	fontWeightOptSelDeact: "normal",	// possible values: normal, bold
	fontWeightSel: "normal",			// possible values: normal, bold
	fontWeightSelDeact: "normal",		// possible values: normal, bold
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