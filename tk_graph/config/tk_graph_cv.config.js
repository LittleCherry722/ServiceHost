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

var gv_cv_style_rrLabel = {
		fgColor: "#000000",
		bgColor: false,
		borderColor: false,
		borderWidth: 0,
		textAlign: "center",
		textVAlign: "middle",
		minHeight: 70,
		width: 120
};

var gv_cv_style_msgLabel = {
		fgColor: "#0000FF",
		bgColor: "#C0FFFF",
		borderColor: "#0000FF",
		borderWidth: 1,
		textAlign: "left",
		textVAlign: "middle",
		liSymbol: "\u2055 ",
		lineSpacing: 3
};

var gv_cv_style_arrow = {
		borderColor: "#0000FF",
		borderWidth: 2
};

/**
 * do not delete any piece of information below this line; only edit the values
 */
var gv_cv_arrow = {
		styleText: gv_cv_style_msgLabel,
		styleArrow: gv_cv_style_arrow,
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
		bin: true
};

var gv_cv_roundedRect = {
		arrowCorrectionV: 50,
		arrowCorrectionH: 25,
		height: 200,
		width: 120,
		radius: 10,
		bgColor: "#CCCCCC",
		borderColor: "#000000",
		borderColorSelected: "#FF9900",
		borderWidth: 4,
		style: gv_cv_style_rrLabel,
		textPosY: -60,
		linePosY: -20,
		lineWidth: 2,
		startX: 80,
		startY: 150,
		distance: 275
};