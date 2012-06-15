/*
 * S-BPM Groupware v0.8
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
 * Styles (can be edited, added, deleted)
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

// only set difference to normal circleNode
var gv_bv_style_circleNodeStart = {
	/*
	 * Border
	 */
	borderWidth: 3,						// pixels
	borderWidthDeactivated: 3,			// pixels
	borderWidthSelDeactive: 4,			// pixels
	borderWidthSelected: 4				// pixels
};

// only set difference to normal circleNode
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

// only set difference to normal rectNode
var gv_bv_style_rectNodeStart = {
	/*
	 * Border
	 */
	borderWidth: 3,						// pixels
	borderWidthDeactivated: 3,			// pixels
	borderWidthSelDeactive: 4,			// pixels
	borderWidthSelected: 4				// pixels
};

/**
 * do not delete any piece of information below this line; only edit the values
 */
var gv_bv_arrow = {
		style: gv_bv_style_edge,
		correctionH: 0,
		correctionV: 5,
		arrowSpace: 20,
		tout: true,
		tin: true,
		lout: true,
		lin: true,
		rout: true,
		rin: true,
		bout: true,
		bin: true
};

var gv_bv_circleNode = {
		style: gv_bv_style_circleNode,
		styleStart: gv_bv_style_circleNodeStart,
		styleEnd: gv_bv_style_circleNodeEnd
};

var gv_bv_rectNode = {
		style: gv_bv_style_rectNode,
		styleStart: gv_bv_style_rectNodeStart
};

var gv_bv_nodeSettings = {
		distanceX: 220,
		distanceY: 150,
		startX: 0,
		startY: 50,
		startNewX: 150,
		startNewY: 50,
		arrowSpace: 75
};