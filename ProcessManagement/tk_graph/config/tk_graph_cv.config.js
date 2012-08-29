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
 * Default style set for edges between two subjects.
 * The given attributes overwrite the values of the default style set defined in tk_graph.config.js.
 * 
 * @type Object
 */
var gv_cv_style_arrow = {
	/*
	 * Arrow
	 */
	arrowHeadType: "classic",			// possible values: classic, block, open, oval, diamond, none
	arrowHeadLength: "long",			// possible values: long, short, medium
	arrowHeadWidth: "wide",				// possible values: wide, narrow, medium
	arrowColor: "#000000",				// any hex-color-value
	arrowColorDeact: "#0000FF",			// any hex-color-value
	arrowColorSel: "#0000FF",			// any hex-color-value
	arrowLinecap: "square",				// possible values: butt, square, round
	arrowLinejoin: "bevel",				// possible values: bevel, round, miter
	arrowMiterLimit: 0,					// any number
	arrowOpacity: 1.0,					// floating number
	arrowOpacityDeact: 1.0,				// floating number
	arrowOpacitySel: 1.0,				// floating number
	arrowStyle: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleDeact: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowStyleSel: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	arrowWidth: 2,						// pixels
	arrowWidthDeact: 2,					// pixels
	arrowWidthSel: 2,					// pixels
	
	/*
	 * Border
	 */
	borderColor: "#000000",				// any hex-color-value
	borderColorDeact: "#0000FF",		// any hex-color-value
	borderColorSel: "#0000FF",			// any hex-color-value
	borderOpacity: 1.0,					// floating number
	borderOpacityDeact: 1.0,			// floating number
	borderOpacitySel: 1.0,				// floating number
	borderStyle: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleDeact: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSel: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderWidth: 1,						// pixels
	borderWidthDeact: 1,				// pixels
	borderWidthSel: 1,					// pixels

	/*
	 * Background
	 */		
	bgColor: "#C0FFFF",					// any hex-color-value
	bgColorDeact: "#C0FFFF",			// any hex-color-value
	bgColorSel: "#C0FFFF",				// any hex-color-value
	bgOpacity: 1.0,						// floating number
	bgOpacityDeact: 1.0,				// floating number
	bgOpacitySel: 1.0,					// floating number
	opacity: 1.0,						// floating number
	opacityDeact: 1.0,					// floating number
	opacitySel: 1.0,					// floating number
	
	/*
	 * Text
	 */
	fontColor: "#000000",				// any hex-color-value
	fontColorDeact: "#000000",			// any hex-color-value
	fontColorSel: "#000000",			// any hex-color-value
	fontOpacity: 1.0,					// floating number
	fontOpacityDeact: 1.0,				// floating number
	fontOpacitySel: 1.0,				// floating number
	fontFamily: "Verdana, sans-serif",	// any font
	fontSize: 12,						// pixels
	fontWeight: "normal",				// possible values: normal, bold
	fontWeightDeact: "normal",			// possible values: normal, bold
	fontWeightSel: "normal",			// possible values: normal, bold
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

/**
 * Default style set for external subjects.
 * The given attributes overwrite the values of the default style set defined in tk_graph.config.js.
 * 
 * @type Object
 */
var gv_cv_style_roundedRectangleExternal = {
	/*
	 * Border
	 */
	borderColor: "#AA0000",				// any hex-color-value
	borderColorDeact: "#AA0000",		// any hex-color-value
	borderColorSelDeact: "#0000FF",		// any hex-color-value
	borderColorSel: "#0000FF",			// any hex-color-value
	borderOpacity: 1.0,					// floating number
	borderOpacityDeact: 0.5,			// floating number
	borderOpacitySelDeact: 1.0,			// floating number
	borderOpacitySel: 1.0,				// floating number
	borderStyle: "- ",					// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleDeact: "- ",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelDeact: "- ",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSel: "- ",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderWidth: 2,						// pixels
	borderWidthDeact: 2,				// pixels
	borderWidthSelDeact: 2,				// pixels
	borderWidthSel: 2,					// pixels

	/*
	 * Background
	 */		
	bgColor: "#FF9999",					// any hex-color-value
	bgColorDeact: "#FF9999",			// any hex-color-value
	bgColorSelDeact: "#FF9999",			// any hex-color-value
	bgColorSel: "#FF9999",				// any hex-color-value
	bgOpacity: 1.0,						// floating number
	bgOpacityDeact: 0.5,				// floating number
	bgOpacitySelDeact: 0.75,			// floating number
	bgOpacitySel: 1.0,					// floating number
	opacity: 1.0,						// floating number
	opacityDeact: 0.5,					// floating number
	opacitySelDeact: 0.75,				// floating number
	opacitySel: 1.0,					// floating number
	
	/*
	 * Text
	 */
	fontColor: "#CC0000",				// any hex-color-value
	fontColorDeact: "#CC0000",			// any hex-color-value
	fontColorSelDeact: "#0000FF",		// any hex-color-value
	fontColorSel: "#0000FF",			// any hex-color-value
	fontOpacity: 1.0,					// floating number
	fontOpacityDeact: 0.5,				// floating number
	fontOpacitySelDeact: 0.75,			// floating number
	fontOpacitySel: 1.0,				// floating number
	fontFamily: "Times New Roman",		// any font
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
	rectangleRadius: 15,				// radius for rounded rectangles
	width: 120,							// int
	height: 200							// int
};

/**
 * Default style set for multi subjects.
 * The given attributes overwrite the values of the default style set defined in tk_graph.config.js.
 * 
 * @type Object
 */
var gv_cv_style_roundedRectangleMulti = {
	/*
	 * Border
	 */
	borderColor: "#000000",				// any hex-color-value
	borderColorDeact: "#000000",		// any hex-color-value
	borderColorSelDeact: "#0000FF",		// any hex-color-value
	borderColorSel: "#0000FF",			// any hex-color-value
	borderOpacity: 1.0,					// floating number
	borderOpacityDeact: 0.5,			// floating number
	borderOpacitySelDeact: 1.0,			// floating number
	borderOpacitySel: 1.0,				// floating number
	borderStyle: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleDeact: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelDeact: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSel: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderWidth: 2,						// pixels
	borderWidthDeact: 2,				// pixels
	borderWidthSelDeact: 3,				// pixels
	borderWidthSel: 3,					// pixels
	
	/*
	 * Background
	 */		
	bgColor: "#999999",					// any hex-color-value
	bgColorDeact: "#999999",			// any hex-color-value
	bgColorSelDeact: "#999999",			// any hex-color-value
	bgColorSel: "#999999",				// any hex-color-value
	bgOpacity: 1.0,						// floating number
	bgOpacityDeact: 0.5,				// floating number
	bgOpacitySelDeact: 0.75,			// floating number
	bgOpacitySel: 1.0,					// floating number
	opacity: 1.0,						// floating number
	opacityDeact: 0.5,					// floating number
	opacitySelDeact: 0.75,				// floating number
	opacitySel: 1.0,					// floating number
	
	/*
	 * Text
	 */
	fontColor: "#000000",				// any hex-color-value
	fontColorDeact: "#000000",			// any hex-color-value
	fontColorSelDeact: "#0000FF",		// any hex-color-value
	fontColorSel: "#0000FF",			// any hex-color-value
	fontOpacity: 1.0,					// floating number
	fontOpacityDeact: 0.5,				// floating number
	fontOpacitySelDeact: 0.75,			// floating number
	fontOpacitySel: 1.0,				// floating number
	fontFamily: "Arial",				// any font
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
	rectangleRadius: 15,				// radius for rounded rectangles
	width: 120,							// int
	height: 200							// int
};

/**
 * Default style set for single subjects.
 * The given attributes overwrite the values of the default style set defined in tk_graph.config.js.
 * 
 * @type Object
 */
var gv_cv_style_roundedRectangleSingle = {
	/*
	 * Border
	 */
	borderColor: "#000000",				// any hex-color-value
	borderColorDeact: "#000000",		// any hex-color-value
	borderColorSelDeact: "#0000FF",		// any hex-color-value
	borderColorSel: "#0000FF",			// any hex-color-value
	borderOpacity: 1.0,					// floating number
	borderOpacityDeact: 0.5,			// floating number
	borderOpacitySelDeact: 1.0,			// floating number
	borderOpacitySel: 1.0,				// floating number
	borderStyle: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleDeact: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelDeact: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSel: "solid",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderWidth: 2,						// pixels
	borderWidthDeact: 2,				// pixels
	borderWidthSelDeact: 3,				// pixels
	borderWidthSel: 3,					// pixels

	/*
	 * Background
	 */		
	bgColor: "#999999",					// any hex-color-value
	bgColorDeact: "#999999",			// any hex-color-value
	bgColorSelDeact: "#999999",			// any hex-color-value
	bgColorSel: "#999999",				// any hex-color-value
	bgOpacity: 1.0,						// floating number
	bgOpacityDeact: 0.5,				// floating number
	bgOpacitySelDeact: 0.75,			// floating number
	bgOpacitySel: 1.0,					// floating number
	opacity: 1.0,						// floating number
	opacityDeact: 0.5,					// floating number
	opacitySelDeact: 0.75,				// floating number
	opacitySel: 1.0,					// floating number
	
	/*
	 * Text
	 */
	fontColor: "#000000",				// any hex-color-value
	fontColorDeact: "#000000",			// any hex-color-value
	fontColorSelDeact: "#0000FF",		// any hex-color-value
	fontColorSel: "#0000FF",			// any hex-color-value
	fontOpacity: 1.0,					// floating number
	fontOpacityDeact: 0.5,				// floating number
	fontOpacitySelDeact: 0.75,			// floating number
	fontOpacitySel: 1.0,				// floating number
	fontFamily: "Arial",				// any font
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
	rectangleRadius: 15,				// radius for rounded rectangles
	width: 120,							// int
	height: 200							// int
};

/*
 * do not delete any piece of information below this line; only edit the values
 */

/**
 * Configuration of edges between subjects.
 * 
 * @type Object
 */
var gv_cv_arrow = {
		style: gv_cv_style_arrow	// style set to use
};

/**
 * Configuration of subject nodes.
 * 
 * @type Object
 */
var gv_cv_roundedRectangle = {
		// an edge's vertical position in pixels relative to the center of the subject node
		arrowCorrectionV: 50,
		
		// an edge's horizontal position in pixels relative to the center of the subject node
		arrowCorrectionH: 25,
		
		// offset to left border of canvas
		startX: 80,
		
		// distance between two subject nodes in pixels (measured from center to center)
		distance: 300,
		
		// references style of the node of a single subject
		styleSingle: gv_cv_style_roundedRectangleSingle,
		
		// references style of the node of a multi subject
		styleMulti: gv_cv_style_roundedRectangleMulti,
		
		// references style of the node of an external subject
		styleExternal: gv_cv_style_roundedRectangleExternal
};