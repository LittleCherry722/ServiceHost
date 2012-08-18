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
	arrowColorDeact: "#666666",			// any hex-color-value
	arrowColorOpt: "#666666",			// any hex-color-value
	arrowColorOptDeact: "#666666",		// any hex-color-value
	arrowColorOptSel: "#666666",		// any hex-color-value
	arrowColorOptSelDeact: "#666666",	// any hex-color-value
	arrowColorSelDeact: "#0000FF",		// any hex-color-value
	arrowColorSel: "#0000FF",			// any hex-color-value
	arrowLinecap: "square",				// possible values: butt, square, round
	arrowLinejoin: "bevel",				// possible values: bevel, round, miter
	arrowMiterLimit: 0,					// any number
	arrowOpacity: 1.0,					// floating number
	arrowOpacityDeact: 0.4,				// floating number
	arrowOpacityOpt: 1.0,				// floating number
	arrowOpacityOptDeact: 0.4,			// floating number
	arrowOpacityOptSel: 1.0,			// floating number
	arrowOpacityOptSelDeact: 0.7,		// floating number
	arrowOpacitySelDeact: 0.7,			// floating number
	arrowOpacitySel: 1.0,				// floating number
	arrowStyle: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleDeact: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleOpt: "- ",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleOptDeact: "- ",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleOptSel: "- ",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleOptSelDeact: "- ",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleSelDeact: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleSel: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowWidth: 2,						// pixels
	arrowWidthDeact: 2,					// pixels
	arrowWidthOpt: 2,					// pixels
	arrowWidthOptDeact: 2,				// pixels
	arrowWidthOptSel: 2,				// pixels
	arrowWidthOptSelDeact: 2,			// pixels
	arrowWidthSelDeact: 2,				// pixels
	arrowWidthSel: 2,					// pixels
	
	/*
	 * Border
	 */
	borderColor: "#666666",				// any hex-color-value
	borderColorDeact: "#666666",		// any hex-color-value
	borderColorOpt: "#666666",			// any hex-color-value
	borderColorOptDeact: "#666666",		// any hex-color-value
	borderColorOptSel: "#666666",		// any hex-color-value
	borderColorOptSelDeact: "#666666",	// any hex-color-value
	borderColorSelDeact: "#0000FF",		// any hex-color-value
	borderColorSel: "#0000FF",			// any hex-color-value
	borderOpacity: 1.0,					// floating number
	borderOpacityDeact: 0.4,			// floating number
	borderOpacityOpt: 1.0,				// floating number
	borderOpacityOptDeact: 0.4,			// floating number
	borderOpacityOptSel: 1.0,			// floating number
	borderOpacityOptSelDeact: 0.7,		// floating number
	borderOpacitySelDeact: 0.7,			// floating number
	borderOpacitySel: 1.0,				// floating number
	borderStyle: ".",					// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleDeact: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleOpt: ".",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleOptDeact: ".",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleOptSel: ".",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleOptSelDeact: ".",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelDeact: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSel: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderWidth: 2,						// pixels
	borderWidthDeact: 2,				// pixels
	borderWidthOpt: 2,					// pixels
	borderWidthOptDeact: 2,				// pixels
	borderWidthOptSel: 2,				// pixels
	borderWidthOptSelDeact: 2,			// pixels
	borderWidthSelDeact: 2,				// pixels
	borderWidthSel: 2,					// pixels

	/*
	 * Background
	 */
	bgColor: "#F0FFFF",					// any hex-color-value
	bgColorDeact: "#F0FFFF",			// any hex-color-value
	bgColorOpt: "#F0FFFF",				// any hex-color-value
	bgColorOptDeact: "#F0FFFF",			// any hex-color-value
	bgColorOptSel: "#F0FFFF",			// any hex-color-value
	bgColorOptSelDeact: "#F0FFFF",		// any hex-color-value
	bgColorSelDeact: "#F0FFFF",			// any hex-color-value
	bgColorSel: "#F0FFFF",				// any hex-color-value
	bgOpacity: 1.0,						// floating number
	bgOpacityDeact: 0.4,				// floating number
	bgOpacityOpt: 1.0,					// floating number
	bgOpacityOptDeact: 0.4,				// floating number
	bgOpacityOptSel: 1.0,				// floating number
	bgOpacityOptSelDeact: 0.7,			// floating number
	bgOpacitySelDeact: 0.7,				// floating number
	bgOpacitySel: 1.0,					// floating number
	opacity: 1.0,						// floating number
	opacityDeact: 0.4,					// floating number
	opacityOpt: 1.0,					// floating number
	opacityOptDeact: 0.4,				// floating number
	opacityOptSel: 1.0,					// floating number
	opacityOptSelDeact: 0.7,			// floating number
	opacitySelDeact: 0.7,				// floating number
	opacitySel: 1.0,					// floating number
	
	/*
	 * Text
	 */
	fontColor: "#000000",				// any hex-color-value
	fontColorDeact: "#000000",			// any hex-color-value
	fontColorOpt: "#000000",			// any hex-color-value
	fontColorOptDeact: "#000000",		// any hex-color-value
	fontColorOptSel: "#000000",			// any hex-color-value
	fontColorOptSelDeact: "#000000",	// any hex-color-value
	fontColorSelDeact: "#0000FF",		// any hex-color-value
	fontColorSel: "#0000FF",			// any hex-color-value
	fontOpacity: 1.0,					// floating number
	fontOpacityDeact: 0.4,				// floating number
	fontOpacityOpt: 1.0,				// floating number
	fontOpacityOptDeact: 0.4,			// floating number
	fontOpacityOptSel: 1.0,				// floating number
	fontOpacityOptSelDeact: 0.7,		// floating number
	fontOpacitySelDeact: 0.7,			// floating number
	fontOpacitySel: 1.0,				// floating number
	fontFamily: "Verdana, sans-serif",	// any font
	fontSize: 12,						// pixels
	fontWeight: "normal",				// possible values: normal, bold
	fontWeightDeact: "normal",			// possible values: normal, bold
	fontWeightOpt: "normal",			// possible values: normal, bold
	fontWeightOptDeact: "normal",		// possible values: normal, bold
	fontWeightOptSel: "normal",			// possible values: normal, bold
	fontWeightOptSelDeact: "normal",	// possible values: normal, bold
	fontWeightSelDeact: "normal",		// possible values: normal, bold
	fontWeightSel: "normal",			// possible values: normal, bold
	// paddingBottom: 5,				// pixels
	// paddingLeft: 5,					// pixels
	// paddingRight: 5,					// pixels
	// paddingTop: 5,					// pixels
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
 * Additional style set for message-edges.
 * The given attributes overwrite the values of the default edge style set gv_bv_style_edge.
 * 
 * @type Object
 */
var gv_bv_style_edgeMessage = {
	
};

/**
 * Additional style set for timeout-edges.
 * The given attributes overwrite the values of the default edge style set gv_bv_style_edge.
 * 
 * @type Object
 */
var gv_bv_style_edgeTimeout = {
	/*
	 * Arrow
	 */
	arrowHeadType: "oval",				// possible values: classic, block, open, oval, diamond, none
	arrowOpacity: 0.7,					// floating number
	arrowOpacityDeact: 0.3,				// floating number
	arrowOpacityOpt: 0.7,				// floating number
	arrowOpacityOptDeact: 0.3,			// floating number
	arrowOpacityOptSel: 0.7,			// floating number
	arrowOpacityOptSelDeact: 0.5,		// floating number
	arrowOpacitySelDeact: 0.5,			// floating number
	arrowOpacitySel: 0.7,				// floating number
	arrowStyle: "--",					// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleDeact: "--",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleOpt: "--",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleOptDeact: "--",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleOptSel: "--",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleOptSelDeact: "--",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleSelDeact: "--",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleSel: "--",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowWidth: 2,						// pixels
	arrowWidthDeact: 2,					// pixels
	arrowWidthOpt: 2,					// pixels
	arrowWidthOptDeact: 2,				// pixels
	arrowWidthOptSel: 2,				// pixels
	arrowWidthOptSelDeact: 2,			// pixels
	arrowWidthSelDeact: 2,				// pixels
	arrowWidthSel: 2,					// pixels
	
	/*
	 * Border
	 */
	borderOpacity: 0.7,					// floating number
	borderOpacityDeact: 0.3,			// floating number
	borderOpacityOpt: 0.7,				// floating number
	borderOpacityOptDeact: 0.3,			// floating number
	borderOpacityOptSel: 0.7,			// floating number
	borderOpacityOptSelDeact: 0.5,		// floating number
	borderOpacitySelDeact: 0.5,			// floating number
	borderOpacitySel: 0.7,				// floating number
	borderStyle: ".",					// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleDeact: ".",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleOpt: ".",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleOptDeact: ".",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleOptSel: ".",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleOptSelDeact: ".",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelDeact: ".",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSel: ".",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
		
	/*
	 * Background
	 */
	bgColor: "#EEEEEE",					// any hex-color-value
	bgColorDeact: "#EEEEEE",			// any hex-color-value
	bgColorOpt: "#EEEEEE",				// any hex-color-value
	bgColorOptDeact: "#EEEEEE",			// any hex-color-value
	bgColorOptSel: "#EEEEEE",			// any hex-color-value
	bgColorOptSelDeact: "#EEEEEE",		// any hex-color-value
	bgColorSelDeact: "#EEEEEE",			// any hex-color-value
	bgColorSel: "#EEEEEE",				// any hex-color-value
	
	/*
	 * Misc
	 */
	rectangleRadius: 2					// radius for rounded rectangles
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
	borderColorDeact: "#666666",		// any hex-color-value
	borderColorSelDeact: "#0000FF",		// any hex-color-value
	borderColorSel: "#0000FF",			// any hex-color-value
	borderOpacity: 1.0,					// floating number
	borderOpacityDeact: 0.4,			// floating number
	borderOpacitySelDeact: 0.7,			// floating number
	borderOpacitySel: 1.0,				// floating number
	borderStyle: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleDeact: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelDeact: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSel: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderWidth: 2,						// pixels
	borderWidthDeact: 2,				// pixels
	borderWidthSelDeact: 2,				// pixels
	borderWidthSel: 2,					// pixels

	/*
	 * Background
	 */		
	bgColor: "#90FFFF",					// any hex-color-value
	bgColorDeact: "#90FFFF",			// any hex-color-value
	bgColorSelDeact: "#90FFFF",			// any hex-color-value
	bgColorSel: "#90FFFF",				// any hex-color-value
	bgOpacity: 1.0,						// floating number
	bgOpacityDeact: 0.4,				// floating number
	bgOpacitySelDeact: 0.7,				// floating number
	bgOpacitySel: 1.0,					// floating number
	opacity: 1.0,						// floating number
	opacityDeact: 0.4,					// floating number
	opacitySelDeact: 0.7,				// floating number
	opacitySel: 1.0,					// floating number
	
	/*
	 * Text
	 */
	fontColor: "#000000",				// any hex-color-value
	fontColorDeact: "#000000",			// any hex-color-value
	fontColorSelDeact: "#0000FF",		// any hex-color-value
	fontColorSel: "#0000FF",			// any hex-color-value
	fontOpacity: 1.0,					// floating number
	fontOpacityDeact: 0.4,				// floating number
	fontOpacitySelDeact: 0.7,			// floating number
	fontOpacitySel: 1.0,				// floating number
	fontFamily: "Verdana, sans-serif",	// any font
	fontSize: 12,						// pixels
	fontWeight: "normal",				// possible values: normal, bold
	fontWeightDeact: "normal",			// possible values: normal, bold
	fontWeightSelDeact: "bold",			// possible values: normal, bold
	fontWeightSel: "bold",				// possible values: normal, bold
	// paddingBottom: 5,				// pixels
	// paddingLeft: 5,					// pixels
	// paddingRight: 5,					// pixels
	// paddingTop: 5,					// pixels
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
	borderWidthDeact: 3,				// pixels
	borderWidthSelDeact: 4,				// pixels
	borderWidthSel: 4					// pixels
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
	borderWidthDeact: 3,				// pixels
	borderWidthSelDeact: 4,				// pixels
	borderWidthSel: 4,					// pixels
	
	/*
	 * Background
	 */		
	bgColor: "#C0DDDD",					// any hex-color-value
	bgColorDeact: "#C0DDDD",			// any hex-color-value
	bgColorSelDeact: "#C0DDDD",			// any hex-color-value
	bgColorSel: "#C0DDDD"				// any hex-color-value
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
	borderColorDeact: "#666666",		// any hex-color-value
	borderColorSelDeact: "#0000FF",		// any hex-color-value
	borderColorSel: "#0000FF",			// any hex-color-value
	borderOpacity: 1.0,					// floating number
	borderOpacityDeact: 0.4,			// floating number
	borderOpacitySelDeact: 0.7,			// floating number
	borderOpacitySel: 1.0,				// floating number
	borderStyle: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleDeact: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelDeact: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSel: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderWidth: 2,						// pixels
	borderWidthDeact: 2,				// pixels
	borderWidthSelDeact: 2,				// pixels
	borderWidthSel: 2,					// pixels

	/*
	 * Background
	 */		
	bgColor: "#90FFFF",					// any hex-color-value
	bgColorDeact: "#90FFFF",			// any hex-color-value
	bgColorSelDeact: "#90FFFF",			// any hex-color-value
	bgColorSel: "#90FFFF",				// any hex-color-value
	bgOpacity: 1.0,						// floating number
	bgOpacityDeact: 0.4,				// floating number
	bgOpacitySelDeact: 0.7,				// floating number
	bgOpacitySel: 1.0,					// floating number
	opacity: 1.0,						// floating number
	opacityDeact: 0.4,					// floating number
	opacitySelDeact: 0.7,				// floating number
	opacitySel: 1.0,					// floating number
	
	/*
	 * Text
	 */
	fontColor: "#000000",				// any hex-color-value
	fontColorDeact: "#000000",			// any hex-color-value
	fontColorSelDeact: "#0000FF",		// any hex-color-value
	fontColorSel: "#0000FF",			// any hex-color-value
	fontOpacity: 1.0,					// floating number
	fontOpacityDeact: 0.4,				// floating number
	fontOpacitySelDeact: 0.7,			// floating number
	fontOpacitySel: 1.0,				// floating number
	fontFamily: "Verdana, sans-serif",	// any font
	fontSize: 12,						// pixels
	fontWeight: "normal",				// possible values: normal, bold
	fontWeightDeact: "normal",			// possible values: normal, bold
	fontWeightSelDeact: "normal",		// possible values: normal, bold
	fontWeightSel: "normal",			// possible values: normal, bold
	// paddingBottom: 5,				// pixels
	// paddingLeft: 5,					// pixels
	// paddingRight: 5,					// pixels
	// paddingTop: 5,					// pixels
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
	borderWidthDeact: 3,				// pixels
	borderWidthSelDeact: 4,				// pixels
	borderWidthSel: 4					// pixels
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
		style: gv_bv_style_edge,					// Style set to use for edges
		styleMessage: gv_bv_style_edgeMessage,		// Additional style set to use for message edges
		styleTimeout: gv_bv_style_edgeTimeout,		// Additional style set to use for timeout edges
		tout: true,									// Allow outgoing edges on the top of a node (true | false)
		tin: true,									// Allow incoming edges on the top of a node (true | false)
		lout: true,									// Allow outgoing edges on the left of a node (true | false)
		lin: true,									// Allow incoming edges on the left of a node (true | false)
		rout: true,									// Allow outgoing edges on the right of a node (true | false)
		rin: true,									// Allow incoming edges on the right of a node (true | false)
		bout: true,									// Allow outgoing edges on the bottom of a node (true | false)
		bin: true									// Allow incoming edges on the bottom of a node (true | false)
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