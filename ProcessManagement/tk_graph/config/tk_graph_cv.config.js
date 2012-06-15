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
var gv_cv_style_arrow = {
	/*
	 * Arrow
	 */
	arrowHeadType: "classic",			// possible values: classic, block, open, oval, diamond, none
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
	arrowWidth: 2,						// pixels
	arrowWidthDeactivated: 2,			// pixels
	arrowWidthSelected: 2,				// pixels
	
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

var gv_cv_style_roundedRectangleExternal = {
	/*
	 * Border
	 */
	borderColor: "#AA0000",				// any hex-color-value
	borderColorDeactivated: "#AA0000",	// any hex-color-value
	borderColorSelDeactive: "#0000FF",	// any hex-color-value
	borderColorSelected: "#0000FF",		// any hex-color-value
	borderOpacity: 1.0,					// floating number
	borderOpacityDeactivated: 0.5,		// floating number
	borderOpacitySelDeactive: 1.0,		// floating number
	borderOpacitySelected: 1.0,			// floating number
	borderStyle: "- ",					// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleDeactivated: "- ",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelDeactive: "- ",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelected: "- ",			// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderWidth: 2,						// pixels
	borderWidthDeactivated: 2,			// pixels
	borderWidthSelDeactive: 2,			// pixels
	borderWidthSelected: 2,				// pixels

	/*
	 * Background
	 */		
	bgColor: "#FF9999",					// any hex-color-value
	bgColorDeactivated: "#FF9999",		// any hex-color-value
	bgColorSelDeactive: "#FF9999",		// any hex-color-value
	bgColorSelected: "#FF9999",			// any hex-color-value
	bgOpacity: 1.0,						// floating number
	bgOpacityDeactivated: 0.5,			// floating number
	bgOpacitySelDeactive: 0.75,			// floating number
	bgOpacitySelected: 1.0,				// floating number
	opacity: 1.0,						// floating number
	opacityDeactivated: 0.5,			// floating number
	opacitySelDeactive: 0.75,			// floating number
	opacitySelected: 1.0,				// floating number
	
	/*
	 * Text
	 */
	fontColor: "#CC0000",				// any hex-color-value
	fontColorDeactivated: "#CC0000",	// any hex-color-value
	fontColorSelDeactive: "#0000FF",	// any hex-color-value
	fontColorSelected: "#0000FF",		// any hex-color-value
	fontOpacity: 1.0,					// floating number
	fontOpacityDeactivated: 0.5,		// floating number
	fontOpacitySelDeactive: 0.75,		// floating number
	fontOpacitySelected: 1.0,			// floating number
	fontFamily: "Times New Roman",		// any font
	fontSize: 16,						// pixels
	fontWeight: "normal",				// possible values: normal, bold
	fontWeightDeactivated: "normal",	// possible values: normal, bold
	fontWeightSelDeactive: "bold",		// possible values: normal, bold
	fontWeightSelected: "bold",			// possible values: normal, bold
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

var gv_cv_style_roundedRectangleMulti = {
	/*
	 * Border
	 */
	borderColor: "#000000",				// any hex-color-value
	borderColorDeactivated: "#000000",	// any hex-color-value
	borderColorSelDeactive: "#0000FF",	// any hex-color-value
	borderColorSelected: "#0000FF",		// any hex-color-value
	borderOpacity: 1.0,					// floating number
	borderOpacityDeactivated: 0.5,		// floating number
	borderOpacitySelDeactive: 1.0,		// floating number
	borderOpacitySelected: 1.0,			// floating number
	borderStyle: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleDeactivated: "solid",	// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelDeactive: "solid",	// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelected: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderWidth: 2,						// pixels
	borderWidthDeactivated: 2,			// pixels
	borderWidthSelDeactive: 3,			// pixels
	borderWidthSelected: 3,				// pixels

	/*
	 * Background
	 */		
	bgColor: "#999999",					// any hex-color-value
	bgColorDeactivated: "#999999",		// any hex-color-value
	bgColorSelDeactive: "#999999",		// any hex-color-value
	bgColorSelected: "#999999",			// any hex-color-value
	bgOpacity: 1.0,						// floating number
	bgOpacityDeactivated: 0.5,			// floating number
	bgOpacitySelDeactive: 0.75,			// floating number
	bgOpacitySelected: 1.0,				// floating number
	opacity: 1.0,						// floating number
	opacityDeactivated: 0.5,			// floating number
	opacitySelDeactive: 0.75,			// floating number
	opacitySelected: 1.0,				// floating number
	
	/*
	 * Text
	 */
	fontColor: "#000000",				// any hex-color-value
	fontColorDeactivated: "#000000",	// any hex-color-value
	fontColorSelDeactive: "#0000FF",	// any hex-color-value
	fontColorSelected: "#0000FF",		// any hex-color-value
	fontOpacity: 1.0,					// floating number
	fontOpacityDeactivated: 0.5,		// floating number
	fontOpacitySelDeactive: 0.75,		// floating number
	fontOpacitySelected: 1.0,			// floating number
	fontFamily: "Arial",				// any font
	fontSize: 12,						// pixels
	fontWeight: "normal",				// possible values: normal, bold
	fontWeightDeactivated: "normal",	// possible values: normal, bold
	fontWeightSelDeactive: "bold",		// possible values: normal, bold
	fontWeightSelected: "bold",			// possible values: normal, bold
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

var gv_cv_style_roundedRectangleSingle = {
	/*
	 * Border
	 */
	borderColor: "#000000",				// any hex-color-value
	borderColorDeactivated: "#000000",	// any hex-color-value
	borderColorSelDeactive: "#0000FF",	// any hex-color-value
	borderColorSelected: "#0000FF",		// any hex-color-value
	borderOpacity: 1.0,					// floating number
	borderOpacityDeactivated: 0.5,		// floating number
	borderOpacityDeactivated: 1.0,		// floating number
	borderOpacitySelected: 1.0,			// floating number
	borderStyle: "solid",				// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleDeactivated: "solid",	// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelDeactive: "solid",	// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderStyleSelected: "solid",		// possible values: "dotted", "dashed", "double", "solid", "none", or Raphael's stroke-dasharray: " ", "-", ".", "-.", "-..", ". ", "- ", "--", "- .", "--.", "--.."
	borderWidth: 2,						// pixels
	borderWidthDeactivated: 2,			// pixels
	borderWidthSelDeactive: 3,			// pixels
	borderWidthSelected: 3,				// pixels

	/*
	 * Background
	 */		
	bgColor: "#999999",					// any hex-color-value
	bgColorDeactivated: "#999999",		// any hex-color-value
	bgColorSelDeactive: "#999999",		// any hex-color-value
	bgColorSelected: "#999999",			// any hex-color-value
	bgOpacity: 1.0,						// floating number
	bgOpacityDeactivated: 0.5,			// floating number
	bgOpacitySelDeactive: 0.75,			// floating number
	bgOpacitySelected: 1.0,				// floating number
	opacity: 1.0,						// floating number
	opacityDeactivated: 0.5,			// floating number
	opacitySelDeactive: 0.75,			// floating number
	opacitySelected: 1.0,				// floating number
	
	/*
	 * Text
	 */
	fontColor: "#000000",				// any hex-color-value
	fontColorDeactivated: "#000000",	// any hex-color-value
	fontColorSelDeactive: "#0000FF",	// any hex-color-value
	fontColorSelected: "#0000FF",		// any hex-color-value
	fontOpacity: 1.0,					// floating number
	fontOpacityDeactivated: 0.5,		// floating number
	fontOpacitySelDeactive: 0.75,		// floating number
	fontOpacitySelected: 1.0,			// floating number
	fontFamily: "Arial",				// any font
	fontSize: 12,						// pixels
	fontWeight: "normal",				// possible values: normal, bold
	fontWeightDeactivated: "normal",	// possible values: normal, bold
	fontWeightSelDeactive: "bold",		// possible values: normal, bold
	fontWeightSelected: "bold",			// possible values: normal, bold
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
 * do not delete any piece of information below this line; only edit the values
 */
var gv_cv_arrow = {
		correctionH: 25,
		correctionV: 50,
		arrowSpace: 20,
		tout: true,
		tin: true,
		lout: true,
		lin: true,
		rout: true,
		rin: true,
		bout: true,
		bin: true,
		style: gv_cv_style_arrow
};

var gv_cv_roundedRectangle = {
		arrowCorrectionV: 50,
		arrowCorrectionH: 25,
		height: 200,
		width: 120,
		radius: 10,
		textPosY: -60,
		linePosY: -20,
		lineWidth: 2,
		startX: 80,
		startY: 150,
		distance: 275,
		styleSingle: gv_cv_style_roundedRectangleSingle,
		styleMulti: gv_cv_style_roundedRectangleMulti,
		styleExternal: gv_cv_style_roundedRectangleExternal
};