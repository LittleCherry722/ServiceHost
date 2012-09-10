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

/**
 * The class draws a graph for the communication view.
 * 
 * @private
 * @class used to draw the graph for the communication view
 * @returns {void}
 */
function GCgraphcv ()
{
	/**
	 * This contains all subjects and all messages that are used during the creation of the graph.
	 * 
	 * @private
	 * @type Object
	 */
	this.messages	= {};
	
	/**
	 * This contains all subjects and all messages that are used during the creation of the graph.
	 * 
	 * @private
	 * @type Object
	 */
	this.subjects	= {};
	
	/**
	 * Adds a message to the graph.
	 * This is called by the API.
	 * 
	 * @private
	 * @param {String} sender The sender subject.
	 * @param {String} receiver The receiver subject.
	 * @param {String} text The text of the message.
	 * @returns {void}
	 */
	this.addMessage = function (sender, receiver, text)
	{
		if (!gf_isset(this.messages[sender]))
			this.messages[sender] = {};
		
		this.messages[sender][receiver] = text;
	};
	
	/**
	 * Adds a subject to the graph.
	 * This is called by the API.
	 * 
	 * @private
	 * @param {GCsubject} subject A complete GCsubject.
	 * @param {boolean} selected When set to true the subject will be selected.
	 * @param {String} Callback function [optional].
	 * @returns {void}
	 */
	this.addSubject = function (subject, selected, callback)
	{
		if (!gf_isset(selected) || selected != true)
			selected = false;
		
		this.subjects[subject.getId()] = {subject: subject, selected: selected};
		
		// call the callback function (if one)
		if (gf_isset(callback) && gf_functionExists(callback))
		{
			callback();
		}
	};
	
	/**
	 * The main function for drawing the graph.
	 * 
	 * @private
	 * @param {String} Callback function [optional].
	 * @returns {void}
	 */
	this.drawGraph = function (callback)
	{
		// init the paper
		gf_paperChangeView("cv");
	
		// initialize the variables and clear the arrays
		var gt_cv_subjects = [];
		var gt_cv_messages = this.messages;
		
		var gt_cv_msgCounter	= {};
		var gt_cv_interactions	= {};
		var gt_cv_nextNodes		= [];
		var gt_cv_subjectsSorted	= [];
		var gt_cv_subjectsVisited	= {};
			
		// determine the starting point
		var gt_cv_x = gv_cv_roundedRectangle.startX;
		var gt_cv_y = Math.round(gv_paperSizes.cv_height/2);
		
		// 0. place the subjects
		// 0.0 sort subjects alphabetically
		for (var gt_cv_subjectId in this.subjects)
		{
			gt_cv_nextNodes[gt_cv_nextNodes.length] = gt_cv_subjects.length;
			gt_cv_subjects[gt_cv_subjects.length] = this.subjects[gt_cv_subjectId];
		}
		gt_cv_subjects.sort(this.sortSubjectsByNameCI);
		
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
						gt_cv_interactions[gt_cv_start] = {};
					
					gt_cv_interactions[gt_cv_start][gt_cv_end] = true;
				}
			}
		}
		
		// 0.2 sort subjects
		while (gt_cv_subjectsSorted.length < gt_cv_subjects.length)
		{
			var gt_cv_mlSubject	= null;
			var gt_cv_mlCount	= 9999;
			
			for (var gt_cv_nnIndex in gt_cv_nextNodes)
			{
				var gt_cv_subjectId = gt_cv_nextNodes[gt_cv_nnIndex];
				
				var gt_cv_subject	= gt_cv_subjects[gt_cv_subjectId];
				var gt_cv_subjId	= gt_cv_subject.subject.getId();
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
			
			if (gt_cv_mlSubject == null && gt_cv_nextNodes.length > 0 && gf_isset(gt_cv_nextNodes[0]))
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
				gt_cv_nextNodes = [];
				
				if (gt_cv_mlCount > 0 && gt_cv_mlCount < 9999)
				{
					for (var gt_cv_subjectId in gt_cv_subjects)
					{
						var gt_cv_endId		= gt_cv_subjects[gt_cv_subjectId].subject.getId();
						var gt_cv_startId	= gt_cv_subjects[gt_cv_mlSubject].subject.getId();
						
						if (gt_cv_mlSubject != gt_cv_subjectId && gf_isset(gt_cv_messages[gt_cv_startId][gt_cv_endId]) && !gf_isset(gt_cv_subjectsVisited[gt_cv_subjectId]))
						{
							gt_cv_nextNodes[gt_cv_nextNodes.length] = gt_cv_subjectId;
						}
					}
				}
				
				if (gt_cv_nextNodes.length < 1)
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
			
			gv_graph_cv.drawSubject(gt_cv_x, gt_cv_y, gt_cv_subjects[gt_cv_subjectId].subject, gt_cv_subjects[gt_cv_subjectId].selected);
			gt_cv_x += gv_cv_roundedRectangle.distance;
		}
		
		// 2. draw the messages
		for (var gt_cv_start in gt_cv_messages)
		{
			for (var gt_cv_end in gt_cv_messages[gt_cv_start])
			{
				gv_graph_cv.drawMessage(gt_cv_start, gt_cv_end, gt_cv_messages[gt_cv_start][gt_cv_end]);
			}
		}
		
		// call the callback function (if one)
        if (gf_isset(callback) && gf_functionExists(callback))
        {
        	callback();
        }
	};
	
	/**
	 * Displays a message sent from one subject to another.
	 * This creates a new GCpath.
	 * 
	 * @private
	 * @param {String} sender The id of the sender subject.
	 * @param {String} receiver The id of the receiver subject.
	 * @param {String} text The text of the message.
	 * @returns {void}
	 */
	this.drawMessage = function (sender, receiver, text)
	{
		if (!gf_isset(gv_objects_nodes[sender], gv_objects_nodes[receiver], text))
			return false;
			
		var gt_cv_objectStart	= gv_objects_nodes[sender].getBoundaries();
		var gt_cv_objectEnd		= gv_objects_nodes[receiver].getBoundaries();
		
		var gt_cv_startx			= 0;
		var gt_cv_starty			= 0;
		var gt_cv_endx				= 0;
		var gt_cv_endy				= 0;
		var gt_cv_arrowUspace		= 0;
			
		var gt_cv_orgDistance	= gv_cv_roundedRectangle.distance;
		
		var gt_cv_distance		= gt_cv_objectStart.x - gt_cv_objectEnd.x;
		var gt_cv_arrowType		= "I";
		
		// receiver is on the right of the sender
		if (gt_cv_distance < 0)
		{
			gt_cv_distance	= Math.abs(gt_cv_distance);
			
			// when the sender and receiver subjects are no direct neighbors: draw a U shaped arrow from sender to receiver
			if (gt_cv_distance > gt_cv_orgDistance)
			{			
				// move the arrow ends out of the middle
				gt_cv_startx	= gt_cv_objectStart.x + gv_cv_roundedRectangle.arrowCorrectionH;
				gt_cv_endx		= gt_cv_objectEnd.x - gv_cv_roundedRectangle.arrowCorrectionH;
				gt_cv_starty	= gt_cv_objectStart.top;
				gt_cv_endy		= gt_cv_objectEnd.top;
				gt_cv_arrowType	= "U";
				
				gt_cv_arrowUspace	= 0 - (Math.round(gt_cv_distance/gt_cv_orgDistance) - 1) * 25;
			}
			
			// when sender and receiver are direct neighbors: draw an I shaped arrow
			else if (gt_cv_distance == gt_cv_orgDistance)
			{			
				gt_cv_startx	= gt_cv_objectStart.right;
				gt_cv_endx		= gt_cv_objectEnd.left;
				
				// move the arrow ends out of the middle
				gt_cv_starty	= gt_cv_objectStart.y - gv_cv_roundedRectangle.arrowCorrectionV;
				gt_cv_endy		= gt_cv_objectEnd.y - gv_cv_roundedRectangle.arrowCorrectionV;
			}
		}
		
		// receiver is on the left of the sender
		else
		{
			// when the sender and receiver subjects are no direct neighbors: draw a U shaped arrow from sender to receiver
			if (gt_cv_distance > gt_cv_orgDistance)
			{			
				// move the arrow ends out of the middle
				gt_cv_startx	= gt_cv_objectStart.x - gv_cv_roundedRectangle.arrowCorrectionH;
				gt_cv_endx		= gt_cv_objectEnd.x + gv_cv_roundedRectangle.arrowCorrectionH;
				gt_cv_starty	= gt_cv_objectStart.bottom;
				gt_cv_endy		= gt_cv_objectEnd.bottom;
				gt_cv_arrowType	= "U";
				
				gt_cv_arrowUspace	= (Math.round(gt_cv_distance/gt_cv_orgDistance) - 1) * 25;
			}
			
			// when sender and receiver are direct neighbors: draw an I shaped arrow
			else if (gt_cv_distance == gt_cv_orgDistance)
			{			
				gt_cv_startx	= gt_cv_objectStart.left;
				gt_cv_endx		= gt_cv_objectEnd.right;
				
				// move the arrow ends out of the middle
				gt_cv_starty	= gt_cv_objectStart.y + gv_cv_roundedRectangle.arrowCorrectionV;
				gt_cv_endy		= gt_cv_objectEnd.y + gv_cv_roundedRectangle.arrowCorrectionV;
			}
		}
		
		// create a new GCpath
		var gt_cv_path = new GCpath(gt_cv_startx, gt_cv_starty, gt_cv_endx, gt_cv_endy, gt_cv_arrowType, text, "doesNotMatter" + Math.random());
			gt_cv_path.setStyle(gv_cv_arrow.style);
			gt_cv_path.setSpace1(gt_cv_arrowUspace);
			gt_cv_path.setFirstLine("v");
			gt_cv_path.updatePath();
			
		// check if the path intersects with any other message
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
	};
	
	/**
	 * Displays a subject.
	 * This creates a new GClabel.
	 * 
	 * @private
	 * @param {int} x The x ordinate of the subject's center.
	 * @param {int} y The x ordinate of the subject's center.
	 */
	this.drawSubject = function (x, y, subject, selected)
	{
		// determine the shape: multi subjects are displayed with a stack of four subjects
		var gt_cv_shape	= subject.isMulti() ? "roundedrectanglemulti" : "roundedrectangle";
		
		// create a new GClabel
		var gt_cv_rect	= new GClabel(x, y, subject.textToString(), gt_cv_shape, subject.getId());
			
		// apply the correct style set
		if (subject.isMulti() && subject.isExternal())
		{
			gt_cv_rect.setStyle(gf_mergeStyles(gv_cv_roundedRectangle.styleMulti, gv_cv_roundedRectangle.styleExternal));
		}
		else if (subject.isExternal())
		{
			gt_cv_rect.setStyle(gv_cv_roundedRectangle.styleExternal);
		}
		else if (subject.isMulti())
		{
			gt_cv_rect.setStyle(gv_cv_roundedRectangle.styleMulti);
		}
		else
		{
			gt_cv_rect.setStyle(gv_cv_roundedRectangle.styleSingle);	
		}
		
		// add the click events to the rectangle
		gt_cv_rect.click("cv");
		
		// apply the deactivation status to the rectangle
		if (subject.isDeactivated())
			gt_cv_rect.deactivate();
		
		// apply the selection status to the rectangle
		if (gf_isset(selected) && selected === true)
			gt_cv_rect.select();
	};
	
	/**
	 * Initialize the canvas.
	 * 
	 * @private
	 * @returns {void}
	 */
	this.init = function ()
	{
		this.subjects = {};
		this.messages = {};
	};
	
	/**
	 * Function to be passed as parameter for array.sort() to sort the objects in the given array by name.
	 * 
	 * @private
	 * @param {Object} obj1 The subject to compare.
	 * @param {Object} obj2 The subject to compare against.
	 * @returns {int} Returns 1 when obj1 is sorted after obj2, -1 when obj2 is sorted after obj1 and 0 when both elements are equal.
	 */
	this.sortSubjectsByNameCI = function (obj1, obj2)
	{
		if (obj1.subject.getText().toLowerCase() > obj2.subject.getText().toLowerCase())
			return 1;
		if (obj1.subject.getText().toLowerCase() < obj2.subject.getText().toLowerCase())
			return -1;
		return 0;
	};
}