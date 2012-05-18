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
 * graph file for communication view
 */

var gv_cv_canvas	= null;
var gv_cv_ctx		= null;
var gv_cv_clickPos	= {x: 0, y: 0};
var gv_cv_objects	= new Array();

var gv_cv_nodes	= new Array();
var gv_cv_edges	= new Array();

var gv_cv_graph = {subjects: new Array(), messages: new Array()};

/*
 * called by API: adds a subject to the graph
 */
function gf_cv_addSubject (gt_cv_id, gt_cv_text, gt_cv_selected)
{
	if (!gf_isset(gt_cv_selected) || gt_cv_selected != true)
		gt_cv_selected = false;
	
	gv_cv_graph.subjects[gt_cv_id] = {text: gt_cv_text, selected: gt_cv_selected, id: gt_cv_id};
}

/*
 * called by API: adds a message between two subjects to the graph
 */
function gf_cv_addMessage (start, end, text)
{
	if (!gf_isset(gv_cv_graph.messages[start]))
		gv_cv_graph.messages[start] = new Array();
	
	gv_cv_graph.messages[start][end] = text;
}

/* 
 * DISPLAYING FUNCTIONS
 */

/*
 * main function for drawing the graph
 */
function gf_cv_drawGraph (gt_cv_canvasElem)
{
	// init the canvas element
	gf_cv_init(gt_cv_canvasElem);
	gv_cv_objects = new Array();

	// initialize the variables and clear the arrays
	var gt_cv_subjects = new Array();
	var gt_cv_messages = gv_cv_graph.messages;
	
	var gt_cv_msgCounter	= new Array();
	var gt_cv_interactions	= new Array();
	var gt_cv_nextNodes		= new Array();
	var gt_cv_subjectsSorted	= new Array();
	var gt_cv_subjectsVisited	= new Array();
		
	// determine the starting point
	var gt_cv_x = gv_cv_roundedRect.startX;
	var gt_cv_y = Math.round(gv_cv_canvas.height/2);
	
	// 0. place the subjects
	// 0.0 sort subjects alphabetically
	for (var gt_cv_subjectId in gv_cv_graph.subjects)
	{
		gt_cv_nextNodes[gt_cv_subjects.length] = gt_cv_subjects.length;
		gt_cv_subjects[gt_cv_subjects.length] = gv_cv_graph.subjects[gt_cv_subjectId];
	}
	gt_cv_subjects.sort(gf_cv_sortSubjectsByIdCI);
	
	// 0.1 calculate in- and outgoing messages
	for (var gt_cv_start in gt_cv_messages)
	{
		for (var gt_cv_end in gt_cv_messages[gt_cv_start])
		{
			if (!gf_isset(gt_cv_msgCounter[gt_cv_start]))
				gt_cv_msgCounter[gt_cv_start] = {in: 0, out: 0, inout: 0};
			
			if (!gf_isset(gt_cv_msgCounter[gt_cv_end]))
				gt_cv_msgCounter[gt_cv_end] = {in: 0, out: 0, inout: 0};
			
			gt_cv_msgCounter[gt_cv_start].out++;
			gt_cv_msgCounter[gt_cv_end].in++;
			
			if (gf_isset(gt_cv_interactions[gt_cv_end]) && gf_isset(gt_cv_interactions[gt_cv_end][gt_cv_start]))
			{
				gt_cv_msgCounter[gt_cv_start].inout++;
				gt_cv_msgCounter[gt_cv_end].inout++;
			}
			else
			{
				if (!gf_isset(gt_cv_interactions[gt_cv_start]))
					gt_cv_interactions[gt_cv_start] = new Array();
				
				gt_cv_interactions[gt_cv_start][gt_cv_end] = true;
			}
		}
	}
	
	// 0.2 sort subjects
	while (gf_count(gt_cv_subjectsSorted) < gf_count(gt_cv_subjects))
	{
		var gt_cv_mlSubject	= null;
		var gt_cv_mlCount	= 9999;
		
		for (var gt_cv_nnIndex in gt_cv_nextNodes)
		{
			var gt_cv_subjectId = gt_cv_nextNodes[gt_cv_nnIndex];
			
			var gt_cv_subject	= gt_cv_subjects[gt_cv_subjectId];
			var gt_cv_subjId	= gt_cv_subject.id;
			var gt_cv_outCount	= gf_isset(gt_cv_msgCounter[gt_cv_subjId]) ? gt_cv_msgCounter[gt_cv_subjId].out : 0;
			var gt_cv_inCount	= gf_isset(gt_cv_msgCounter[gt_cv_subjId]) ? gt_cv_msgCounter[gt_cv_subjId].in : 0;
			
			var gt_cv_mlUpdate	= false; // gt_cv_mlSubject == null;
			
			if (!gt_cv_mlUpdate && gt_cv_outCount < gt_cv_mlCount && gt_cv_outCount > 0)
				gt_cv_mlUpdate = true;
			
			if (!gt_cv_mlUpdate && gt_cv_inCount > 0 && gt_cv_mlSubject == null)
				gt_cv_mlUpdate = true;
			
			if (gt_cv_mlUpdate)
			{
				gt_cv_mlSubject	= gt_cv_subjectId;
				
				if (gt_cv_outCount > 0)
					gt_cv_mlCount	= gt_cv_outCount;
			}
		}
		
		if (gt_cv_mlSubject == null && gf_count(gt_cv_nextNodes) > 0 && gf_isset(gt_cv_nextNodes[0]))
		{
			gt_cv_mlSubject	= gt_cv_nextNodes[0];
			gt_cv_mlCount	= 0;
		}
		
		if (gt_cv_mlSubject == null)
		{
			break;
		}
		else
		{
			gt_cv_subjectsSorted[gt_cv_subjectsSorted.length]	= gt_cv_mlSubject;
			gt_cv_subjectsVisited[gt_cv_mlSubject]				= true;
			
			// get neighbors
			gt_cv_nextNodes.length = 0;
			gt_cv_nextNodes = new Array();
			
			if (gt_cv_mlCount > 0 && gt_cv_mlCount < 9999)
			{
				for (var gt_cv_subjectId in gt_cv_subjects)
				{
					var gt_cv_endId		= gt_cv_subjects[gt_cv_subjectId].id;
					var gt_cv_startId	= gt_cv_subjects[gt_cv_mlSubject].id;
					
					if (gt_cv_mlSubject != gt_cv_subjectId && gf_isset(gt_cv_messages[gt_cv_startId][gt_cv_endId]) && !gf_isset(gt_cv_subjectsVisited[gt_cv_subjectId]))
					{
						gt_cv_nextNodes[gt_cv_nextNodes.length] = gt_cv_subjectId;
					}
				}
			}
			
			if (gf_count(gt_cv_nextNodes) < 1)
			{
				for (var gt_cv_subjectId in gt_cv_subjects)
				{					
					if (!gf_isset(gt_cv_subjectsVisited[gt_cv_subjectId]))
					{
						gt_cv_nextNodes[gt_cv_nextNodes.length] = gt_cv_subjectId;
					}
				}
			}
		}
	}
	
	// 1. draw the subjects
	for (var gt_cv_ssId in gt_cv_subjectsSorted)
	{
		var gt_cv_subjectId = gt_cv_subjectsSorted[gt_cv_ssId];
		
		gf_cv_drawRoundedRectangle(gt_cv_x, gt_cv_y, gt_cv_subjects[gt_cv_subjectId].id, gt_cv_subjects[gt_cv_subjectId].text, gt_cv_subjects[gt_cv_subjectId].selected);
		gt_cv_x += gv_cv_roundedRect.distance;
	}
	
	// 2. draw the messages
	for (var gt_cv_start in gt_cv_messages)
	{
		for (var gt_cv_end in gt_cv_messages[gt_cv_start])
		{
			gf_cv_drawArrow(gt_cv_start, gt_cv_end, gt_cv_messages[gt_cv_start][gt_cv_end]);
		}
	}
}

/*
 * function to be passed as parameter for array.sort() to sort the objects in the given array by name
 */
function gf_cv_sortSubjectsByIdCI (obj1, obj2)
{
	if (obj1.id.toLowerCase() > obj2.id.toLowerCase())
		return 1;
	if (obj1.id.toLowerCase() < obj2.id.toLowerCase())
		return -1;
	return 0;
}

/*
 * initializes the canvas
 */
function gf_cv_init (gt_cv_canvasElem)
{
	if (gf_elementExists(gt_cv_canvasElem))
	{
		gv_cv_canvas	= document.getElementById(gt_cv_canvasElem);
		gv_cv_ctx		= gv_cv_canvas.getContext("2d");
		
		gv_cv_ctx.clearRect(0, 0, gv_cv_canvas.width, gv_cv_canvas.height);
		
		if (gv_cv_canvas.addEventListener)
		{
			gv_cv_canvas.addEventListener("click", gf_cv_onClick, false);
			gv_cv_canvas.addEventListener("dblclick", gf_cv_onDblClick, false);
		}
		else if (document.attachEvent)
		{
			gv_cv_canvas.attachEvent("onclick", gf_cv_onClick);
			gv_cv_canvas.attachEvent("ondblclick", gf_cv_onDblClick);
		}
	}
}

/*
 * reads a click / dblClick event
 */
function gf_cv_onClick (gt_cv_event, gt_cv_double)
{
	if (!gf_isset(gt_cv_double) || gt_cv_double != true)
		gt_cv_double = false;
	
	// read click position and add the canvas' offset
	gf_getClickPosition(gt_cv_event, gv_cv_canvas, gv_cv_clickPos);
	gv_cv_clickPos.x += document.getElementById(gv_elements.graphCVouter).scrollLeft;

	// cycle to all objects to get the object that has been clicked
	for (var gt_cv_objectID in gv_cv_objects)
	{
		var gt_cv_object = gv_cv_objects[gt_cv_objectID];
		
		if (	gv_cv_clickPos.x >= gt_cv_object.l &&
				gv_cv_clickPos.x <= gt_cv_object.r &&
				gv_cv_clickPos.y >= gt_cv_object.t &&
				gv_cv_clickPos.y <= gt_cv_object.b)
		{
			if (gt_cv_object.id != "")
			{
				
				if (gt_cv_double == true)
				{
					gf_clickedCVbehavior(gt_cv_object.id);	
				}
				else
				{
					gf_clickedCVnode(gt_cv_object.id);					
				}
			}
			break;
		}
	}
}

/*
 * double click event
 */
function gf_cv_onDblClick (gt_cv_event)
{
	gf_cv_onClick(gt_cv_event, true);
}

/*
 * stores the clickable area of an object
 */
function gf_cv_storeClick (gt_cv_id, gt_cv_type, gt_cv_l, gt_cv_t, gt_cv_r, gt_cv_b)
{
	gv_cv_clicks[gv_cv_clicks.length] = {
			id:		gt_cv_id,
			type:	gt_cv_type,
			l:		gt_cv_l,
			t:		gt_cv_t,
			r:		gt_cv_r,
			b:		gt_cv_b
	};
}

/*
 * stores an object
 */
function gf_cv_storeObject (gt_cv_id, gt_cv_x, gt_cv_y, gt_cv_width, gt_cv_height, gt_cv_stroke)
{		
	// x: x, y: y, w: width, h: height, l: left, r: right, t: top, b: bottom, s: stroke
	gt_cv_width		+= gt_cv_stroke;
	gt_cv_height	+= gt_cv_stroke;
	gt_cv_width		 = Math.round(gt_cv_width/2);
	gt_cv_height	 = Math.round(gt_cv_height/2);
	
	gv_cv_objects[gt_cv_id] = {
			id: gt_cv_id,
			 x: gt_cv_x,
			 y: gt_cv_y,
			 w: gt_cv_width,
			 h: gt_cv_height,
			 l: gt_cv_x - gt_cv_width,
			 r: gt_cv_x + gt_cv_width,
			 t: gt_cv_y - gt_cv_height,
			 b: gt_cv_y + gt_cv_height,
			 s: gt_cv_stroke
	};
}

/*
 * DRAWING FUNCTIONS
 */

/*
 * draws a rounded rectangle as a representation for a subject
 */
function gf_cv_drawRoundedRectangle (gt_cv_posx, gt_cv_posy, gt_cv_id, gt_cv_text, gt_cv_selected)
{
	if (!gf_isset(gt_cv_selected) || gt_cv_selected != true)
		gt_cv_selected = false;
	
	// read settings
	var gt_cv_width			= gv_cv_roundedRect.width;
	var gt_cv_height		= gv_cv_roundedRect.height;
	var gt_cv_radius		= gv_cv_roundedRect.radius;
	var gt_cv_bgColor		= gv_cv_roundedRect.bgColor;
	var gt_cv_borderColor	= gt_cv_selected ? gv_cv_roundedRect.borderColorSelected : gv_cv_roundedRect.borderColor;
	var gt_cv_borderWidth	= gv_cv_roundedRect.borderWidth;
	var gt_cv_style			= gv_cv_roundedRect.style;
	var gt_cv_linePosY		= gv_cv_roundedRect.linePosY;
	var gt_cv_lineWidth		= gv_cv_roundedRect.lineWidth;
	var gt_cv_textPosY		= gv_cv_roundedRect.textPosY;
	
	var gt_cv_lengthH	= gt_cv_width - 2 * gt_cv_radius;
	var gt_cv_lengthV	= gt_cv_height - 2 * gt_cv_radius;
	
	var gt_cv_left	= gt_cv_posx - gt_cv_width / 2;
	var gt_cv_top	= gt_cv_posy - gt_cv_height / 2;
	
	// draw the rectangle
	gv_cv_ctx.beginPath();
	gv_cv_ctx.moveTo(gt_cv_left + gt_cv_radius, gt_cv_top);
	gv_cv_ctx.lineTo(gt_cv_left + gt_cv_lengthH + gt_cv_radius, gt_cv_top);
	gv_cv_ctx.arc(gt_cv_left + gt_cv_lengthH + gt_cv_radius, gt_cv_top + gt_cv_radius, gt_cv_radius, 1.5 * Math.PI, 0.0 * Math.PI, false);
	gv_cv_ctx.lineTo(gt_cv_left + gt_cv_width, gt_cv_top + gt_cv_lengthV + gt_cv_radius);
	gv_cv_ctx.arc(gt_cv_left + gt_cv_lengthH + gt_cv_radius, gt_cv_top + gt_cv_lengthV + gt_cv_radius, gt_cv_radius, 0.0 * Math.PI, 0.5 * Math.PI, false);
	gv_cv_ctx.lineTo(gt_cv_left + gt_cv_radius, gt_cv_top + gt_cv_height);
	gv_cv_ctx.arc(gt_cv_left + gt_cv_radius, gt_cv_top + gt_cv_lengthV + gt_cv_radius, gt_cv_radius, 0.5 * Math.PI, 1.0 * Math.PI, false);
	gv_cv_ctx.lineTo(gt_cv_left, gt_cv_top + gt_cv_radius);
	gv_cv_ctx.arc(gt_cv_left + gt_cv_radius, gt_cv_top + gt_cv_radius, gt_cv_radius, 1.0 * Math.PI, 1.5 * Math.PI, false);
	gv_cv_ctx.closePath();
	
	gv_cv_ctx.fillStyle		= gt_cv_bgColor;
	gv_cv_ctx.strokeStyle	= gt_cv_borderColor;
	gv_cv_ctx.lineWidth		= gt_cv_borderWidth;
	gv_cv_ctx.stroke();
	gv_cv_ctx.fill();
		
	// text
	gf_drawLabel (gv_cv_ctx, gt_cv_posx, gt_cv_posy + gt_cv_textPosY, gt_cv_text, gt_cv_style);
	
	gv_cv_ctx.beginPath();
	gv_cv_ctx.moveTo(gt_cv_left, gt_cv_posy + gt_cv_linePosY);
	gv_cv_ctx.lineTo(gt_cv_left + gt_cv_width, gt_cv_posy + gt_cv_linePosY);
	gv_cv_ctx.closePath();
	
	gv_cv_ctx.lineWidth = gt_cv_lineWidth;
	gv_cv_ctx.stroke();
	
	gf_cv_storeObject(gt_cv_id, gt_cv_posx, gt_cv_posy, gt_cv_width, gt_cv_height, gt_cv_borderWidth);
}

/*
 * draws an arrow (message) between two subjects
 */
function gf_cv_drawArrow (gt_cv_objStart, gt_cv_objEnd, gt_cv_text)
{
	if (!gf_isset(gv_cv_objects[gt_cv_objStart], gv_cv_objects[gt_cv_objEnd], gt_cv_text))
		return false;

	var gt_cv_styleArrow	= gf_isset(gv_cv_arrow.styleArrow)	? gv_cv_arrow.styleArrow	: gv_defaultStyle;
	var gt_cv_styleText		= gf_isset(gv_cv_arrow.styleText)	? gv_cv_arrow.styleText		: gv_defaultStyle;
	
	var gt_cv_arrowWidth	= gf_getStyleValue(gt_cv_styleArrow, "borderWidth");
	var gt_cv_arrowColor	= gf_getStyleValue(gt_cv_styleArrow, "borderColor");
	
	var gt_cv_arrowSpace	= gv_arrowHead.length + 10;
	
	var gt_cv_startx			= 0;
	var gt_cv_starty			= 0;
	var gt_cv_endx				= 0;
	var gt_cv_endy				= 0;
	var gt_cv_headCorrection	= 0;
	var gt_cv_direction			= "r";
	var gt_cv_arrowUspace		= 0;
	
	var gt_cv_labelCenter	= {x: 0, y: 0};
	
	var gt_cv_orgDistance	= gv_cv_roundedRect.distance;
	var gt_cv_distance		= gv_cv_objects[gt_cv_objStart].x - gv_cv_objects[gt_cv_objEnd].x;
	var gt_cv_arrowType		= "I";
	
	// end subject is on the right of the start subject
	if (gt_cv_distance < 0)
	{
		gt_cv_distance	= Math.abs(gt_cv_distance);
		
		if (gt_cv_distance > gt_cv_orgDistance)
		{
			gt_cv_direction	= "t";
			
			gt_cv_startx	= gv_cv_objects[gt_cv_objStart].x + gv_cv_roundedRect.arrowCorrectionH;
			gt_cv_endx		= gv_cv_objects[gt_cv_objEnd].x - gv_cv_roundedRect.arrowCorrectionH;
			gt_cv_starty	= gv_cv_objects[gt_cv_objStart].t;
			gt_cv_endy		= gv_cv_objects[gt_cv_objEnd].t;
			gt_cv_arrowType	= "U";
			
			gt_cv_arrowUspace	= 0 - (Math.round(gt_cv_distance/gt_cv_orgDistance) - 1) * 25;
			
			gt_cv_headCorrection	= 0 - gv_cv_roundedRect.arrowCorrectionH;
		}
		else if (gt_cv_distance == gt_cv_orgDistance)
		{
			gt_cv_direction	= "l";
			
			gt_cv_startx	= gv_cv_objects[gt_cv_objStart].r;
			gt_cv_endx		= gv_cv_objects[gt_cv_objEnd].l;
			gt_cv_starty	= gv_cv_objects[gt_cv_objStart].y - gv_cv_roundedRect.arrowCorrectionV;
			gt_cv_endy		= gv_cv_objects[gt_cv_objEnd].y - gv_cv_roundedRect.arrowCorrectionV;

			gt_cv_headCorrection	= 0 - gv_cv_roundedRect.arrowCorrectionV;
		}
	}
	else
	{
		if (gt_cv_distance > gt_cv_orgDistance)
		{
			gt_cv_direction	= "b";
			
			gt_cv_startx	= gv_cv_objects[gt_cv_objStart].x - gv_cv_roundedRect.arrowCorrectionH;
			gt_cv_endx		= gv_cv_objects[gt_cv_objEnd].x + gv_cv_roundedRect.arrowCorrectionH;
			gt_cv_starty	= gv_cv_objects[gt_cv_objStart].b;
			gt_cv_endy		= gv_cv_objects[gt_cv_objEnd].b;
			gt_cv_arrowType	= "U";
			
			gt_cv_arrowUspace	= (Math.round(gt_cv_distance/gt_cv_orgDistance) - 1) * 25;
			
			gt_cv_headCorrection	= gv_cv_roundedRect.arrowCorrectionH;
		}
		else if (gt_cv_distance == gt_cv_orgDistance)
		{
			gt_cv_direction	= "r";
			
			gt_cv_startx	= gv_cv_objects[gt_cv_objStart].l;
			gt_cv_endx		= gv_cv_objects[gt_cv_objEnd].r;
			gt_cv_starty	= gv_cv_objects[gt_cv_objStart].y + gv_cv_roundedRect.arrowCorrectionV;
			gt_cv_endy		= gv_cv_objects[gt_cv_objEnd].y + gv_cv_roundedRect.arrowCorrectionV;
			
			gt_cv_headCorrection	= gv_cv_roundedRect.arrowCorrectionV;
		}
	}
	
	if (gt_cv_arrowType == "I")
	{
		gt_cv_labelCenter	= gf_drawArrowI(gv_cv_ctx, "cv", 0, gt_cv_startx, gt_cv_starty, gt_cv_endx, gt_cv_endy, gt_cv_arrowColor, gt_cv_arrowWidth);	
	}
	else if (gt_cv_arrowType == "U")
	{
		gt_cv_labelCenter	= gf_drawArrowU(gv_cv_ctx, "cv", 0, gt_cv_startx, gt_cv_starty, gt_cv_endx, gt_cv_endy, gt_cv_arrowColor, gt_cv_arrowWidth, "v", gt_cv_arrowUspace);
	}
	
	gf_drawArrowHead(gv_cv_ctx, gv_cv_objects[gt_cv_objEnd], gt_cv_direction, gt_cv_arrowColor, gt_cv_headCorrection);
	gf_drawLabel(gv_cv_ctx, gt_cv_labelCenter.x, gt_cv_labelCenter.y, gt_cv_text, gt_cv_styleText);
}