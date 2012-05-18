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

var gv_bv_style_arrowLabel = {
		fgColor: "#0000FF",
		bgColor: "#C0FFFF",
		borderColor: false,
		borderWidth: 0,
		textAlign: "center"
};

var gv_bv_style_circleNode = {
		fgColor: "#0000FF",
		bgColor: "#C0FFFF",
		borderColor: "#0000FF",
		borderColorSelected: "#FF9900",
		borderWidth: 2,
		fontSize: 12,
		paddingTop: 5,
		paddingBottom: 5
};

var gv_bv_style_circleNodeStart = {
		borderWidth: 4
};

var gv_bv_style_circleNodeEnd = {
		borderWidth: 6
};

var gv_bv_style_rectNode = {
	fgColor: "#0000FF",
	bgColor: "#C0FFFF",
	borderColor: "#0000FF",
	borderColorSelected: "#FF9900",
	borderWidth: 2,
	textAlign: "center"
};

var gv_bv_style_rectNodeStart = {
		borderWidth: 4
};

var gv_bv_style_arrow = {
		borderColor: "#FF0000",
		borderColorSelected: "#FF9900",
		borderWidth: 2
};

/**
 * do not delete any piece of information below this line; only edit the values
 */
var gv_bv_arrow = {
		styleText: gv_bv_style_arrowLabel,
		styleArrow: gv_bv_style_arrow,
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