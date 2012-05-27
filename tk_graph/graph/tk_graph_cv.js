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
function gf_cv_drawGraph ()
{
	// init the paper
	gv_paper = gv_cv_paper;
	gf_initPaper();
	
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
	var gt_cv_x = gv_cv_roundedRectangle.startX;
	var gt_cv_y = Math.round(gv_paperSizes.cv_height/2);
	
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
				gt_cv_msgCounter[gt_cv_start] = {msgIn: 0, msgOut: 0, inout: 0};
			
			if (!gf_isset(gt_cv_msgCounter[gt_cv_end]))
				gt_cv_msgCounter[gt_cv_end] = {msgIn: 0, msgOut: 0, inout: 0};
			
			gt_cv_msgCounter[gt_cv_start].msgOut++;
			gt_cv_msgCounter[gt_cv_end].msgIn++;
			
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
			var gt_cv_outCount	= gf_isset(gt_cv_msgCounter[gt_cv_subjId]) ? gt_cv_msgCounter[gt_cv_subjId].msgOut : 0;
			var gt_cv_inCount	= gf_isset(gt_cv_msgCounter[gt_cv_subjId]) ? gt_cv_msgCounter[gt_cv_subjId].msgIn : 0;
			
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
		gt_cv_x += gv_cv_roundedRectangle.distance;
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
 * draws a rounded rectangle as a representation for a subject
 */
function gf_cv_drawRoundedRectangle (gt_cv_posx, gt_cv_posy, gt_cv_id, gt_cv_text, gt_cv_selected)
{
	
	var gt_cv_rect	= new GFlabel(gt_cv_posx, gt_cv_posy, gt_cv_text, "roundedrectangle", gt_cv_id);	// TODO: Multi
	
	if (gf_isset(gt_cv_selected) && gt_cv_selected === true)
		gt_cv_rect.select();
		
	gt_cv_rect.setStyle(gv_cv_roundedRectangle.styleSingle);		// TODO: Multi + External
	gt_cv_rect.click("cv");
	
	// TODO: remove random and use some real function to determine whether a node is deactivated or not
	// var num = Math.random();
	// if (num < 0.5)
	// gt_cv_rect.deactivate();
}

/*
 * draws an arrow (message) between two subjects
 */
function gf_cv_drawArrow (gt_cv_objStart, gt_cv_objEnd, gt_cv_text)
{
	if (!gf_isset(gv_objects_nodes[gt_cv_objStart], gv_objects_nodes[gt_cv_objEnd], gt_cv_text))
		return false;
		
	var gt_cv_objectStart	= gv_objects_nodes[gt_cv_objStart].getBoundaries();
	var gt_cv_objectEnd		= gv_objects_nodes[gt_cv_objEnd].getBoundaries();
	
	var gt_cv_startx			= 0;
	var gt_cv_starty			= 0;
	var gt_cv_endx				= 0;
	var gt_cv_endy				= 0;
	var gt_cv_headCorrection	= 0;
	var gt_cv_direction			= "r";
	var gt_cv_arrowUspace		= 0;
		
	var gt_cv_orgDistance	= gv_cv_roundedRectangle.distance;
	
	var gt_cv_distance		= gt_cv_objectStart.x - gt_cv_objectEnd.x;
	var gt_cv_arrowType		= "I";
	
	// end subject is on the right of the start subject
	if (gt_cv_distance < 0)
	{
		gt_cv_distance	= Math.abs(gt_cv_distance);
		
		if (gt_cv_distance > gt_cv_orgDistance)
		{			
			gt_cv_startx	= gt_cv_objectStart.x + gv_cv_roundedRectangle.arrowCorrectionH;
			gt_cv_endx		= gt_cv_objectEnd.x - gv_cv_roundedRectangle.arrowCorrectionH;
			gt_cv_starty	= gt_cv_objectStart.top;
			gt_cv_endy		= gt_cv_objectEnd.top;
			gt_cv_arrowType	= "U";
			
			gt_cv_arrowUspace	= 0 - (Math.round(gt_cv_distance/gt_cv_orgDistance) - 1) * 25;
			
			gt_cv_headCorrection	= 0 - gv_cv_roundedRectangle.arrowCorrectionH;
		}
		else if (gt_cv_distance == gt_cv_orgDistance)
		{			
			gt_cv_startx	= gt_cv_objectStart.right;
			gt_cv_endx		= gt_cv_objectEnd.left;
			gt_cv_starty	= gt_cv_objectStart.y - gv_cv_roundedRectangle.arrowCorrectionV;
			gt_cv_endy		= gt_cv_objectEnd.y - gv_cv_roundedRectangle.arrowCorrectionV;

			gt_cv_headCorrection	= 0 - gv_cv_roundedRectangle.arrowCorrectionV;
		}
	}
	else
	{
		if (gt_cv_distance > gt_cv_orgDistance)
		{			
			gt_cv_startx	= gt_cv_objectStart.x - gv_cv_roundedRectangle.arrowCorrectionH;
			gt_cv_endx		= gt_cv_objectEnd.x + gv_cv_roundedRectangle.arrowCorrectionH;
			gt_cv_starty	= gt_cv_objectStart.bottom;
			gt_cv_endy		= gt_cv_objectEnd.bottom;
			gt_cv_arrowType	= "U";
			
			gt_cv_arrowUspace	= (Math.round(gt_cv_distance/gt_cv_orgDistance) - 1) * 25;
			
			gt_cv_headCorrection	= gv_cv_roundedRectangle.arrowCorrectionH;
		}
		else if (gt_cv_distance == gt_cv_orgDistance)
		{			
			gt_cv_startx	= gt_cv_objectStart.left;
			gt_cv_endx		= gt_cv_objectEnd.right;
			gt_cv_starty	= gt_cv_objectStart.y + gv_cv_roundedRectangle.arrowCorrectionV;
			gt_cv_endy		= gt_cv_objectEnd.y + gv_cv_roundedRectangle.arrowCorrectionV;
			
			gt_cv_headCorrection	= gv_cv_roundedRectangle.arrowCorrectionV;
		}
	}
	
	var gt_cv_path = new GFpath(gt_cv_startx, gt_cv_starty, gt_cv_endx, gt_cv_endy, gt_cv_arrowType, gt_cv_text, "doesNotMatter" + Math.random());
		gt_cv_path.setStyle(gv_cv_arrow.style);
		gt_cv_path.setSpace1(gt_cv_arrowUspace);
		gt_cv_path.setFirstLine("v");
		gt_cv_path.updatePath();
		
	if (gt_cv_arrowType == "U")
	{
		var gt_cv_p_c = 2;
		while (gt_cv_path.checkIntersection(true) && gt_cv_p_c < 10)
		{
			gt_cv_path.setSpace1(gt_cv_arrowUspace * gt_cv_p_c);
			gt_cv_path.updatePath();
			gt_cv_p_c++;
		}
	}
}