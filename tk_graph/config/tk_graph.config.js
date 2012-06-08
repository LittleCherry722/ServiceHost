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
 * do NOT delete any line in this file; only edit the values
 */

var gv_paperSizes	= {
	bv_width: 5000,
	bv_height: 6000,
	cv_width: 2000,
	cv_height: 540
};

var gv_nodeTypes	= {
	action: {shape: "roundedrectangle", text: null},
	send: {shape: "circle", text: "S"},
	receive: {shape: "circle", text: "R"},
	end: {shape: "circle", text: ""}
};

var gv_defaultStyle	= {
	
	/*
	 * Arrow
	 */
	arrowHeadType: "none",				// possible values: classic, block, open, oval, diamond, none
	arrowHeadLength: "long",			// possible values: long, short, medium
	arrowHeadWidth: "wide",				// possible values: wide, narrow, medium
	arrowColor: "#000000",				// any hex-color-value
	arrowColorDeactivated: "#0000FF",	// any hex-color-value
	arrowColorSelected: "#0000FF",		// any hex-color-value
	arrowLinecap: "square",				// possible values: butt, square, round
	arrowLinejoin: "bevel",				// possible values: bevel, round, miter
	arrowMiterLimit: 0,					// any number
	arrowOpacity: 1.0,					// floating number
	arrowOpacityDeactivated: 1.0,		// floating number
	arrowOpacitySelected: 1.0,			// floating number
	arrowStyle: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleDeactivated: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleSelected: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowWidth: 1,						// pixels
	arrowWidthDeactivated: 1,			// pixels
	arrowWidthSelected: 1,				// pixels
	
	/*
	 * Border
	 */
	borderColor: "#000000",				// any hex-color-value
	borderColorDeactivated: "#0000FF",	// any hex-color-value
	borderColorSelected: "#0000FF",		// any hex-color-value
	borderOpacity: 1.0,					// floating number
	borderOpacityDeactivated: 1.0,		// floating number
	borderOpacitySelected: 1.0,			// floating number
	borderStyle: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleDeactivated: "solid",	// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelected: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderWidth: 1,						// pixels
	borderWidthDeactivated: 1,			// pixels
	borderWidthSelected: 1,				// pixels

	/*
	 * Background
	 */		
	bgColor: "#C0FFFF",					// any hex-color-value
	bgColorDeactivated: "#C0FFFF",		// any hex-color-value
	bgColorSelected: "#C0FFFF",			// any hex-color-value
	bgOpacity: 1.0,						// floating number
	bgOpacityDeactivated: 1.0,			// floating number
	bgOpacitySelected: 1.0,				// floating number
	opacity: 1.0,						// floating number
	opacityDeactivated: 1.0,			// floating number
	opacitySelected: 1.0,				// floating number
	
	/*
	 * Text
	 */
	fontColor: "#000000",				// any hex-color-value
	fontColorDeactivated: "#000000",	// any hex-color-value
	fontColorSelected: "#000000",		// any hex-color-value
	fontOpacity: 1.0,					// floating number
	fontOpacityDeactivated: 1.0,		// floating number
	fontOpacitySelected: 1.0,			// floating number
	fontFamily: "Verdana, sans-serif",	// any font
	fontSize: 12,						// pixels
	fontWeight: "normal",				// possible values: normal, bold
	fontWeightDeactivated: "normal",	// possible values: normal, bold
	fontWeightSelected: "normal",		// possible values: normal, bold
	paddingBottom: 5,					// pixels
	paddingLeft: 5,						// pixels
	paddingRight: 5,					// pixels
	paddingTop: 5,						// pixels
	textAlign: "left",					// possible values: left, right, middle
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