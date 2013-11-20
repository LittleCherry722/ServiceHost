/*
 * S-BPM Groupware v1.2
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
 * The class draws a graph for a behavioral view.
 * 
 * @private
 * @class used to draw the graph for a behavioral view
 * @returns {void}
 */
function GCgraphbv ()
{
	/**
	 * Contains the information about the graph.
	 * The indexes are the ids of the subjects.
	 * Each entry consist of
	 * <br />
	 * - nodes: Array()<br />
	 * - edges: Array()
	 * 
	 * @private
	 * @type Object
	 */
	this.graphs = {};
	
	/**
	 * Contains the outgoing edge counts for the nodes.
	 * 
	 * @private
	 * @type Object
	 */
	this.nodesOutgoingEdgeCount	= {};
	
	/**
	 * Contains the current statusses of the object ports.
	 * An object port is a side of a node (top, bottom, left, right).
	 * 
	 * @private
	 * @type Object
	 */
	this.objectPorts	= {};
	
	/**
	 * Stores which nodes are already placed in the graph.
	 * 
	 * @private
	 * @type Object
	 */
	this.placedNodes	= {};
	
	/**
	 * Points that are already blocked by a node.
	 * 
	 * @private
	 * @type Object
	 */
	this.pointsBlocked	= {};
	
	/**
	 * A list of available ports (top, bottom, right, left).
	 * 
	 * @private
	 * @type Object
	 */
	this.ports			= {t: "t", b: "b", r: "r", l: "l"};
	
	/**
	 * A list of available ports (in, ordered by priority) (top, bottom, right, left).
	 * 
	 * @private
	 * @type Object
	 */
	this.portsI			= {b: "b", t: "t", r: "r", l: "l"};
	
	/**
	 * A list of available ports (out, ordered by priority) (top, bottom, right, left).
	 * 
	 * @private
	 * @type Object
	 */
	this.portsO			= {t: "t", b: "b", r: "r", l: "l"};
	
	/**
	 * Lists the port settings from the configuration file.
	 * A port can be set to "i" (only incoming edges), "o" (only outgoing edges) or "io".
	 * 
	 * @private
	 * @type Object
	 */
	this.portSettings	= {t: "io", b: "io", r: "io", l: "io"};
	
	/**
	 * Render objects (only used for relative position changes).
	 * 
	 * @private
	 * @type Object
	 */
	this.renderObjects	= {"nodes": {}, "edges": {}};
	
	/**
	 * Time measuring: time used for intersection checks
	 * 
	 * @private
	 * @type int
	 */
	this.timeIntersection	= 0;
	
	/**
	 * Time measuring: time used for arrowShape calculation
	 * 
	 * @private
	 * @type int
	 */
	this.timeShape	= 0;
	
	/**
	 * Time measuring: temporary start time
	 * 
	 * @private
	 * @type int
	 */
	this.timeTEnd	= 0;
	
	/**
	 * Time measuring: temporary end time
	 * 
	 * @private
	 * @type int
	 */
	this.timeTStart	= 0;
	
	/**
	 * Adds an edge to a subject.
	 * 
	 * @private
	 * @param {String} subject The id of the subject.
	 * @param {int} id The id of the edge.
	 * @param {int} start The id of the edge's start node.
	 * @param {int} end The id of the edge's target node.
	 * @param {GCedge} edge The GCedge instance.
	 * @param {boolean} selected When set to true the edge will be selected.
	 * @param {String} Callback function [optional].
	 * @returns{void}
	 */
	this.addEdge = function (subject, id, start, end, edge, selected, callback)
	{
		if (gf_isset(this.graphs[subject], edge) && gf_isset(this.graphs[subject].nodes[start], this.graphs[subject].nodes[end]))
		{	
			
			if (!gf_isset(selected) || selected != true)
				selected = false;
			
			var gt_bv_graph = this.graphs[subject];
			
			if (!gf_isset(gt_bv_graph.edges[start]))
				gt_bv_graph.edges[start] = [];
			
			// store the edge
			gt_bv_graph.edges[start][gt_bv_graph.edges[start].length] = {	start:		start,
																			end:		end,
																			edge:		edge,
																			id:			id,
																			visited:	false,
																			selected:	selected
																			};
			
			// increase the counters for incoming and outgoing edges on the start and the end node
			if (!gt_bv_graph.nodes[end].node.isStart() || !gt_bv_graph.nodes[start].node.isStart())
			{
				gt_bv_graph.nodes[start].edgesOut++;
				gt_bv_graph.nodes[end].edgesIn++;
			}
			
			// call the callback function (if one)
            if (gf_isset(callback) && gf_functionExists(callback))
            {
            	callback();
            }
		}
	};
	
	/**
	 * Adds a node to a subject.
	 * 
	 * @private
	 * @param {String} subject The id of the subject.
	 * @param {int} id The id of the node.
	 * @param {GCnode} node The GCnode instance.
	 * @param {boolean} selected When set to true the node will be selected.
	 * @param {String} Callback function [optional].
	 * @returns {void}
	 */
	this.addNode = function (subject, id, node, selected, callback)
	{
		// add a new subject when it is not already set
		if (!gf_isset(this.graphs[subject]))
			this.addSubject(subject);
			
		if (!gf_isset(id) || !gf_isset(node))
			return false;
		
		var gt_bv_graph = this.graphs[subject];
		
		if (!gf_isset(selected) || selected != true)
			selected = false;
		
		// add node
		gt_bv_graph.nodes[id] = {	id:			id,
										node:		node,
										visited:	false,
										posx:		0,
										posy:		0,
										edgesIn:	0,
										edgesOut:	0,
										edgesOutCur:0,
										selected:	selected
										};
		gt_bv_graph.nodeCount++;
		
		// add node to startNodes
		if (node.isStart())
		{
			gt_bv_graph.startNodes[id] = true;
		}
		
		// call the callback function (if one)
        if (gf_isset(callback) && gf_functionExists(callback))
        {
        	callback();
        }
	};
	
	/**
	 * Store the available ports of the corresponding object.
	 * 
	 * @private
	 * @param {int} id The id of the object.
	 * @returns {void}
	 */
	this.addObjectPort = function (id)
	{
		this.objectPorts[id] = {	t: this.portSettings.t, tc: 0,
									b: this.portSettings.b, bc: 0,
									l: this.portSettings.l, lc: 0,
									r: this.portSettings.r, rc: 0};
	};
	
	/**
	 * Adds a subject to the graph.
	 * 
	 * @private
	 * @param {String} subject The id of the subject to add.
	 * @param {String} Callback function [optional].
	 * @returns {void}
	 */
	this.addSubject = function (subject, callback)
	{
		this.graphs[subject]	= {nodes: {}, edges: {}, startNodes: {}, nodeCount: 0};
	
		// call the callback function (if one)
        if (gf_isset(callback) && gf_functionExists(callback))
        {
        	callback();
        }
	};
	
	/**
	 * Corrects the number of outgoing edges for a certain node as it is only used to define the proper space for the child nodes.
	 * Nodes that are already placed result in wrong positions.
	 * 
	 * @param {Object} edges An array containing all edges of the selected graph.
	 * @param {int} nodeId The id of the node to check.
	 * @returns {int} The corrected number of outgoing edges.
	 */
	this.checkOutgoingEdgeCount = function (edges, nodeId)
	{
		if (!gf_isset(this.nodesOutgoingEdgeCount["n" + nodeId]))
		{
			var gt_outgoingCount	= 0;
			
			for (var gt_edgeId in edges[nodeId])
			{
				
				var gt_edge	= edges[nodeId][gt_edgeId];
				
				if (gt_edge.start == nodeId)
				{
					if (!gf_isset(this.placedNodes["n" + gt_edge.end]))
						gt_outgoingCount++;
				}
			}
			
			this.nodesOutgoingEdgeCount["n" + nodeId]	= gt_outgoingCount;
		}
		
		return this.nodesOutgoingEdgeCount["n" + nodeId];
	};
	
	/**
	 * Checks if an object's port is available for an incoming / outgoing edge (flag).
	 * Returns true when the port is open.
	 * A port is blocked when it already has an incoming edge / outgoing edge and the flag is set to outgoing / incoming.
	 * A port is available when it is set to "io" or when its value is the same as the flag.
	 * 
	 * @private
	 * @param {int} id The id of the object.
	 * @param {String} port The port to check. Possible values: "t" (top), "b" (bottom), "l" (left), "r" (right)
	 * @param {String} flag The flag to check. Possible values: "i" (incoming), "o" (outgoing)
	 * @returns {boolean} True when the port is available. False when the port is blocked.
	 */
	this.checkObjectPort = function (id, port, flag)
	{
		// when the object ports settings are not available, initialize them
		if (!gf_isset(this.objectPorts[id]))
			this.addObjectPort(id);
		
		// check if the port is given and if the flag is either set to "i" (incoming) or "o" (outgoing)
		if (gf_isset(this.ports[port]) && (flag == "i" || flag == "o"))
		{
			// return true when the port is set to "io" or to the value of the flag
			if (this.objectPorts[id][port] == "io" || this.objectPorts[id][port] == flag)
			{
				return true;
			}
		}
		return false;
	};
	
	/**
	 * Delete a subject from the graph.
	 * 
	 * @private
	 * @param {String} subject The id of the subject to remove.
	 * @param {String} Callback function [optional].
	 * @returns {void}
	 */
	this.deleteSubject = function (subject, callback)
	{
		if (gf_isset(this.graphs[subject]))
		{
			delete this.graphs[subject];
			
			// call the callback function (if one)
            if (gf_isset(callback) && gf_functionExists(callback))
            {
            	callback();
            }
		}
	};
	
	/**
	 * Draws an edge between two nodes.
	 * 
	 * @private
	 * @param {Object} edgeData The information about the edge.
	 * @returns {void}
	 */
	this.drawArrow = function (edgeData)
	{
		if (!gf_isset(edgeData))
			return false;
			
		// get the id of the start and the end node
		var gt_bv_start	= edgeData.start;
		var gt_bv_end	= edgeData.end;
		
		// when either the start or the end node does not exist: break
		if (!gf_isset(gv_objects_nodes[gt_bv_start], gv_objects_nodes[gt_bv_end]))
			return false;		
			
		// get the boundaries of the start and the end object
		var gt_bv_objStart	= gv_objects_nodes[gt_bv_start].getBoundaries();
		var gt_bv_objEnd	= gv_objects_nodes[gt_bv_end].getBoundaries();
		
		// initialize the variables needed to draw the path
		var gt_bv_firstLine			= "v";
		var gt_bv_endLine			= "v";
		var gt_bv_startx			= 0;
		var gt_bv_starty			= 0;
		var gt_bv_endx				= 1;
		var gt_bv_endy				= 1;
			
		var gt_bv_posStart	= "b";
		var gt_bv_posEnd	= "t";
		
		var gt_bv_o	= "";
		var gt_bv_i	= "";
		
		var gt_bv_space1	= 0;
		var gt_bv_space2	= 0;
		var gt_bv_minLength	= 999999999;
		var gt_bv_shape		= "Z";
		
		// maps the ports to the attributes of the boundaries
		var mapPorts		= {t: "top", b: "bottom", r: "right", l: "left"};
		
		// create the path
		// var gt_bv_edge	= new GCpath(gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_shape, edgeData.edge.textToString(), edgeData.id, true);
		gf_timeCalc("drawing edges - drawArrow() - create GCpath");
		var gt_bv_edge	= new GCpath(0, 0, 100, 100, "Z", edgeData.edge.textToString(), edgeData.id, true);
		gf_timeCalc("drawing edges - drawArrow() - create GCpath");

		gf_timeCalc("drawing edges - drawArrow() - apply settings");		
		if (gv_estimateTextDimensions)
			gt_bv_edge.setEstimateTextDimensions(true);
				
		// apply the deactivation status to the path
		if (edgeData.edge.isDeactivated())
			gt_bv_edge.deactivate(true);
		
		// apply the optional status to the path
		gt_bv_edge.setOptional(edgeData.edge.isOptional(), true);
		
		// apply the selection status to the path
		if (gf_isset(edgeData.selected) && edgeData.selected === true)
			gt_bv_edge.select(true);
		
		// add the click events to the path
		gt_bv_edge.click();
		gf_timeCalc("drawing edges - drawArrow() - apply settings");
			
		gf_timeCalc("drawing edges - drawArrow() - apply style");
		// update the style
		var gt_bv_edgeStyle	= gv_bv_arrow.style;
		if (edgeData.edge.getType() == "timeout")
			gt_bv_edgeStyle	= gf_mergeStyles(gv_bv_arrow.style, gv_bv_arrow.styleTimeout);
		if (edgeData.edge.getType() == "errorcondition")
			gt_bv_edgeStyle	= gf_mergeStyles(gv_bv_arrow.style, gv_bv_arrow.styleException);
			
		gt_bv_edge.setStyle(gt_bv_edgeStyle);
		gf_timeCalc("drawing edges - drawArrow() - apply style");
		
		// hide the path as long as it has not the final shape
			gt_bv_edge.hide();
			
		// cycle through all port combinations at startObject and endObject to determine the shape of the arrow and the ports to use
		for (var gt_bv_out in this.portsO)
		{
			for (var gt_bv_in in this.portsI)
			{
				// only process when the port of the start and the end objects are availables; if edge can already be displayed as an I shaped edge, do not cycle through the other alternatives as it won't result in any shorter edge
				if (this.checkObjectPort(gt_bv_start, gt_bv_out, "o") && this.checkObjectPort(gt_bv_end, gt_bv_in, "i") && (gt_bv_shape != "I" || gt_bv_minLength == 999999999))
				{
					gt_bv_o	= gt_bv_out;
					gt_bv_i	= gt_bv_in;
					
					// define the start and end position of the path depending on the current port
					gt_bv_startx	= gt_bv_o == "l" || gt_bv_o == "r" ? gt_bv_objStart[mapPorts[gt_bv_o]]	: gt_bv_objStart.x;
					gt_bv_starty	= gt_bv_o == "t" || gt_bv_o == "b" ? gt_bv_objStart[mapPorts[gt_bv_o]]	: gt_bv_objStart.y;
					gt_bv_endx		= gt_bv_i == "l" || gt_bv_i == "r" ? gt_bv_objEnd[mapPorts[gt_bv_i]]	: gt_bv_objEnd.x;
					gt_bv_endy		= gt_bv_i == "t" || gt_bv_i == "b" ? gt_bv_objEnd[mapPorts[gt_bv_i]]	: gt_bv_objEnd.y;
					
					// calculate the arrow shape that fits best for this port combination
					gf_timeCalc("drawing edges - drawArrow() - shape calculation");
					var gt_bv_arrowShape	= this.getArrowShape(gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_o, gt_bv_i);
					gf_timeCalc("drawing edges - drawArrow() - shape calculation");
					
					var gt_bv_setAsMin		= false;
					var gt_bv_currentLength	= 0;
					var gt_bv_doLoop		= true;
					
					// correction to avoid intersection problems
					var gt_bv_x1	= gt_bv_startx;
					var gt_bv_y1	= gt_bv_starty;
					var gt_bv_x2	= gt_bv_endx;
					var gt_bv_y2	= gt_bv_endy;
					
					// adapt the start and end positions by a few pixels to avoid problems on the intersection checks
					if (gt_bv_o == "l")
						gt_bv_x1 -= 5;
						
					if (gt_bv_o == "r")
						gt_bv_x1 += 5;
						
					if (gt_bv_o == "t")
						gt_bv_y1 -= 5;
						
					if (gt_bv_o == "b")
						gt_bv_y1 += 5;
					
					if (gt_bv_i == "l")
						gt_bv_x2 -= 5;
						
					if (gt_bv_i == "r")
						gt_bv_x2 += 5;
						
					if (gt_bv_i == "t")
						gt_bv_y2 -= 5;
						
					if (gt_bv_i == "b")
						gt_bv_y2 += 5;
					
					// update edge
					var gt_bv_asmStep	= 1;	// 0.5
					var gt_bv_asmMax	= 3;
					
					var gt_bv_asm1 = gt_bv_asmStep;		// asm = arrowShape multiplier
					var gt_bv_asm2 = gt_bv_asmStep;
					
					var gt_bv_asSpace1	= 0;
					var gt_bv_asSpace2	= 0;
					
					while (gt_bv_asm1 <= gt_bv_asmMax && gt_bv_doLoop)
					{
						gt_bv_asm2 = gt_bv_asmStep;
						
						while (gt_bv_asm2 <= gt_bv_asmMax && gt_bv_doLoop)
						{
							
							gt_bv_asSpace1	= gt_bv_arrowShape.space1 * gt_bv_asm1;
							gt_bv_asSpace2	= gt_bv_arrowShape.space2 * gt_bv_asm2;
							
							gf_timeCalc("drawing edges - drawArrow() - update path (position, space)");
							gt_bv_edge.setPositionStart(gt_bv_x1, gt_bv_y1);
							gt_bv_edge.setPositionEnd(gt_bv_x2, gt_bv_y2);
							gt_bv_edge.setFirstLine(gt_bv_arrowShape.firstLine);
							gt_bv_edge.setSpace1(gt_bv_asSpace1);
							gt_bv_edge.setSpace2(gt_bv_asSpace2);
							gf_timeCalc("drawing edges - drawArrow() - update path (position, space)");
							
							gf_timeCalc("drawing edges - drawArrow() - update path (shape)");
							gt_bv_edge.setShape(gt_bv_arrowShape.shape, 1);
							gf_timeCalc("drawing edges - drawArrow() - update path (shape)");
														
							// check if the new arrow would fit better than the currently best
							gf_timeCalc("drawing edges - drawArrow() - update path (check inters.)");
							var gt_bv_intersectionCheck	= gt_bv_edge.checkIntersection(false, {x1: gt_bv_startx, y1: gt_bv_starty, x2: gt_bv_endx, y2: gt_bv_endy});
							gf_timeCalc("drawing edges - drawArrow() - update path (check inters.)");
							
							if (!gt_bv_intersectionCheck)
							{
								gt_bv_currentLength	= gt_bv_arrowShape.length;
								
								// length correction
								if (gt_bv_arrowShape.shape == "U")
									gt_bv_currentLength += 2 * Math.abs(gt_bv_arrowShape.space1 * (gt_bv_asm1 - 1));
									
								else if (gt_bv_arrowShape.shape == "ZU")
									gt_bv_currentLength += 2 * Math.abs(gt_bv_arrowShape.space1 * (gt_bv_asm1 - 1)) + 2 * Math.abs(gt_bv_arrowShape.space2 * (gt_bv_asm2 - 1));
									
								else if (gt_bv_arrowShape.shape == "S")
									gt_bv_currentLength += 4 * Math.abs(gt_bv_arrowShape.space1 * (gt_bv_asm1 - 1));
									
								else if (gt_bv_arrowShape.shape == "C")
									gt_bv_currentLength += 4 * Math.abs(gt_bv_arrowShape.space1 * (gt_bv_asm1 - 1)) + 2 * Math.abs(gt_bv_arrowShape.space2 * (gt_bv_asm2 - 1));
									
								else if (gt_bv_arrowShape.shape == "UI")
									gt_bv_currentLength += 2 * Math.abs(gt_bv_arrowShape.space1 * (gt_bv_asm1 - 1));
									
								else if (gt_bv_arrowShape.shape == "G")
									gt_bv_currentLength += 2 * Math.abs(gt_bv_arrowShape.space1 * (gt_bv_asm1 - 1)) + 2 * Math.abs(gt_bv_arrowShape.space2 * (gt_bv_asm2 - 1));
								
								// TODO
								if (gt_bv_arrowShape.shape == "L")
								{
									if (gt_bv_o == "b" && this.nodesOutgoingEdgeCount["n" + edgeData.start] > 1)
									{
										gt_bv_currentLength += gv_bv_nodeSettings.arrowSpace;
									}
								}
									
								
								// calculate the shortest path (shorter length)
								if (gt_bv_currentLength < gt_bv_minLength)
								{
									// prefer L shaped arrows TODO
									if (!(gt_bv_shape == "L" && gt_bv_arrowShape.shape == "Z" && 1.15*gt_bv_currentLength > gt_bv_minLength))	// TODO
										gt_bv_setAsMin = true;
								}
								
								// calculate the shortest path (same length)
								else if (gt_bv_currentLength == gt_bv_minLength)
								{
									if (gt_bv_shape == gt_bv_arrowShape.shape)
									{	
										
										if (gt_bv_posStart == "r" && gt_bv_o == "l" && gt_bv_startx > gt_bv_endx)
											gt_bv_setAsMin	= true;
										
										if (gt_bv_posStart == "t" && gt_bv_o == "b" && gt_bv_starty < gt_bv_endy)
											gt_bv_setAsMin	= true;
											
									}
									else if (gt_bv_arrowShape.shape == "L" && gt_bv_shape != "I")
									{
										gt_bv_setAsMin	= true;
									}
								}
								else
								{
									// prefer L shaped arrows
									if (gt_bv_shape == "Z" && gt_bv_arrowShape.shape == "L" && gt_bv_currentLength < 1.15*gt_bv_minLength)	// TODO
										gt_bv_setAsMin	= true;
								}
								
								gt_bv_doLoop	= false;	// leave loop
							}
							
							// leave loop when no space1 or space2 can be increased because both are 0
							if (gt_bv_asSpace1 == 0 && gt_bv_asSpace2 == 0)
							{
								gt_bv_doLoop	= false;
							}
							
							gt_bv_asm2 += gt_bv_asmStep;
						}
						gt_bv_asm1 += gt_bv_asmStep;
					}
						
					// store the information about the min graph
					if (gt_bv_setAsMin)
					{
						gt_bv_minLength	= gt_bv_currentLength;
						gt_bv_space1	= gt_bv_asSpace1;
						gt_bv_space2	= gt_bv_asSpace2;
						gt_bv_shape		= gt_bv_arrowShape.shape;
						gt_bv_posStart	= gt_bv_o;
						gt_bv_posEnd	= gt_bv_i;
					}
				}
				else
				{
					
				}
			}
		}
		
		// block ports
		this.editObjectPort(gt_bv_start, gt_bv_posStart, "o");
		this.editObjectPort(gt_bv_end, gt_bv_posEnd, "i");
	
		gt_bv_firstLine	= gt_bv_posStart == "l" || gt_bv_posStart == "r" 	? "h" : "v";
		gt_bv_endLine	= gt_bv_posEnd == "l" 	|| gt_bv_posEnd == "r" 		? "h" : "v";
		
		// update the start and end position depending on the calculated values
		gt_bv_startx	= gt_bv_firstLine == "h"	? gt_bv_objStart[mapPorts[gt_bv_posStart]]	: gt_bv_objStart.x;
		gt_bv_starty	= gt_bv_firstLine == "v"	? gt_bv_objStart[mapPorts[gt_bv_posStart]]	: gt_bv_objStart.y;
		gt_bv_endx		= gt_bv_endLine == "h"		? gt_bv_objEnd[mapPorts[gt_bv_posEnd]]		: gt_bv_objEnd.x;
		gt_bv_endy		= gt_bv_endLine == "v"		? gt_bv_objEnd[mapPorts[gt_bv_posEnd]]		: gt_bv_objEnd.y;
		
		// apply the start and end position to the path
		gf_timeCalc("drawing edges - drawArrow() - apply calculated path");
		gt_bv_edge.setPositionStart(gt_bv_startx, gt_bv_starty);
		gt_bv_edge.setPositionEnd(gt_bv_endx, gt_bv_endy);
		
		// apply the firstLine and space settings to the path
		gt_bv_edge.setFirstLine(gt_bv_firstLine);
		gt_bv_edge.setSpace1(gt_bv_space1);
		gt_bv_edge.setSpace2(gt_bv_space2);
		
		// update the shape of the path
		gt_bv_edge.setShape(gt_bv_shape, 2);
		
		// show the path as it now is complete
		gt_bv_edge.show(true);		
		gf_timeCalc("drawing edges - drawArrow() - apply calculated path");
	};
	
	/**
	 * Draws the graph for the given subject.
	 * 
	 * @private
	 * @param {String} subject The id of the subject.
	 * @param {String} Callback function [optional].
	 * @returns {void}
	 */
	this.drawGraph = function (subject, callback)
	{
		if (gf_isset(this.graphs[subject]))
		{
			
			// init the paper
			gf_paperChangeView("bv");
			
			// clear arrays
			this.objectPorts	= {};
			this.pointsBlocked	= {};
			this.placedNodes	= {};
			this.nodesOutgoingEdgeCount	= {};
			
			gv_node_parents		= {};
			gv_node_children	= {};
			
			// read the object's port settings from the config file
			this.readPortSettings();
			
			// retrieve the graph and clear the nodeSet
			var gt_bv_graph		= this.graphs[subject];
			var gt_bv_nodeSet	= {count: 0, nodes: {}};
			
			var gt_bv_distanceX = gv_bv_nodeSettings.distanceX;
			var gt_bv_distanceY = gv_bv_nodeSettings.distanceY;
			
			// calculate the start position of the first node
			var gt_bv_x = Math.round(gv_paperSizes.bv_width/2) + gv_bv_nodeSettings.startX;
			var gt_bv_y = gv_bv_nodeSettings.startY;
			
			var gt_bv_xOrg	= gt_bv_x;
			
			var gt_bv_mostLeft	= gt_bv_x;
			
			gf_timeCalc("placing nodes (start nodes)");
			// 1. start with the start nodes
			for (var gt_bv_startNode in gt_bv_graph.startNodes)
			{				
				// position the start node depending on the number of children
				// var gt_bv_edgesOut	= gt_bv_graph.nodes[gt_bv_startNode].edgesOut;
				var gt_bv_edgesOut	= this.checkOutgoingEdgeCount(gt_bv_graph.edges, gt_bv_startNode);
					gt_bv_edgesOut	= gt_bv_edgesOut > 0 ? gt_bv_edgesOut - 1 : 0;
					
				// do not move the first start node no matter how many child nodes it has
				var gt_tempPosX	= gt_bv_x == gt_bv_xOrg ? gt_bv_x : gt_bv_x + Math.floor(gt_bv_edgesOut * gt_bv_distanceX/2);
				var gt_tempPosY	= gt_bv_y;
				var gt_pointsBlockedIndex	= gt_tempPosX + "/" + gt_tempPosY;
				
				while (gf_isset(this.pointsBlocked[gt_pointsBlockedIndex]))
				{
					gt_tempPosX				+= gt_bv_distanceX;
					gt_pointsBlockedIndex	= gt_tempPosX + "/" + gt_tempPosY;
				}
				
				gt_bv_graph.nodes[gt_bv_startNode].posx		= gt_tempPosX;
				gt_bv_graph.nodes[gt_bv_startNode].posy		= gt_tempPosY;
				gt_bv_graph.nodes[gt_bv_startNode].visited	= true;
				
				// mark current position as blocked
				this.pointsBlocked[gt_pointsBlockedIndex] = gt_bv_startNode;
				
				// mark node as already visited (to decrease the number of outgoing edges)
				this.placedNodes["n" + gt_bv_startNode]	= true;
				
				gt_bv_nodeSet.nodes[gt_bv_startNode]	= gt_bv_startNode;
				gt_bv_nodeSet.count++;
				
				gt_bv_x += gt_bv_distanceX + gt_bv_edgesOut * gt_bv_distanceX/2;
				
				gv_node_children["n" + gt_bv_startNode] = [];
			}
			gf_timeCalc("placing nodes (start nodes)");
	
			// 2 set nodes connected by edges (starting with the start nodes)
			gf_timeCalc("placing nodes (connected nodes)");
			
			var gt_bv_rescueCount = 100;	// TODO: get rid of this
			while (gt_bv_nodeSet.count > 0 && gt_bv_rescueCount > 0)
			{
				for (var gt_bv_node in gt_bv_nodeSet.nodes)
				{
					
					delete gt_bv_nodeSet.nodes[gt_bv_node];
					gt_bv_nodeSet.count--;
					
					// check if the current node has any children (check the edges that start at the current node)
					if (gf_isset(gt_bv_graph.edges[gt_bv_node]))
					{
						for (var gt_bv_edgeId in gt_bv_graph.edges[gt_bv_node])
						{
							var gt_bv_edge = gt_bv_graph.edges[gt_bv_node][gt_bv_edgeId];
							
							gt_bv_edge.visited = true;
							
							if (!gf_isset(gt_bv_nodeSet.nodes[gt_bv_edge.end]))
							{
								// draw node
								// var gt_bv_edgesOut	= gt_bv_graph.nodes[gt_bv_edge.start].edgesOut;
								var gt_bv_edgesOut	= this.checkOutgoingEdgeCount(gt_bv_graph.edges, gt_bv_edge.start);
									gt_bv_edgesOut	= gt_bv_edgesOut > 0 ? gt_bv_edgesOut - 1 : 0;
							
								if (gt_bv_graph.nodes[gt_bv_edge.end].visited == false)
								{
									var gt_tempPosX	= gt_bv_graph.nodes[gt_bv_edge.start].posx + Math.floor((gt_bv_graph.nodes[gt_bv_edge.start].edgesOutCur - gt_bv_edgesOut/2) * gt_bv_distanceX);
									var gt_tempPosY	= gt_bv_graph.nodes[gt_bv_edge.start].posy + gt_bv_distanceY;
									var gt_pointsBlockedIndex	= gt_tempPosX + "/" + gt_tempPosY;
									
									while (gf_isset(this.pointsBlocked[gt_pointsBlockedIndex]))
									{
										gt_tempPosX				+= gt_bv_distanceX;
										gt_pointsBlockedIndex	= gt_tempPosX + "/" + gt_tempPosY;
									}
									
									// position the node depending on the number of children
									gt_bv_graph.nodes[gt_bv_edge.end].posx		= gt_tempPosX;
									gt_bv_graph.nodes[gt_bv_edge.end].posy		= gt_tempPosY;
									gt_bv_graph.nodes[gt_bv_edge.end].visited	= true;
									gt_bv_graph.nodes[gt_bv_edge.start].edgesOutCur++;
				
									// mark current position as blocked
									this.pointsBlocked[gt_pointsBlockedIndex] = gt_bv_edge.end;
				
									// mark node as already visited (to decrease the number of outgoing edges)
									this.placedNodes["n" + gt_bv_edge.end]	= true;
									
									// mostLeft
									if (gt_bv_graph.nodes[gt_bv_edge.end].posx < gt_bv_mostLeft)
										gt_bv_mostLeft = gt_bv_graph.nodes[gt_bv_edge.end].posx;
										
									if (!gf_isset(gv_node_children["n" + gt_bv_edge.start]))
										gv_node_children["n" + gt_bv_edge.start] = [];
										
									gv_node_parents["n" + gt_bv_edge.end]	= gt_bv_edge.start;
									gv_node_children["n" + gt_bv_edge.start][gv_node_children["n" + gt_bv_edge.start].length] = gt_bv_edge.end;
								}
								
								// TODO: do not add every node again
								
								gt_bv_nodeSet.nodes[gt_bv_edge.end] = gt_bv_edge.end;
								gt_bv_nodeSet.count++;
							}
						}
					}
				}
				
				gt_bv_rescueCount--;
			}
			gf_timeCalc("placing nodes (connected nodes)");
			
			// 3. all other nodes (not connected to the start nodes)
			gf_timeCalc("placing nodes (other nodes)");
			
			gt_bv_x	= gt_bv_mostLeft - gv_bv_nodeSettings.startNewX;
			gt_bv_y = gv_bv_nodeSettings.startNewY;
			for (var gt_bv_node in gt_bv_graph.nodes)
			{
				// position all other nodes to the most left
				if (gt_bv_graph.nodes[gt_bv_node].visited == false)
				{
					gt_bv_graph.nodes[gt_bv_node].posx		= gt_bv_x;
					gt_bv_graph.nodes[gt_bv_node].posy		= gt_bv_y;
					gt_bv_graph.nodes[gt_bv_node].visited	= true;
					gt_bv_y += gt_bv_distanceY/2;
				}
			}
			gf_timeCalc("placing nodes (other nodes)");
			
			// draw the nodes
			gf_timeCalc("drawing nodes");
			for (var gt_bv_nodeId in this.graphs[subject].nodes)
			{
				var gt_bv_node = this.graphs[subject].nodes[gt_bv_nodeId];
				
				this.drawNode(gt_bv_node);
				
				this.addObjectPort(gt_bv_node.id);
			}
			gf_timeCalc("drawing nodes");
			
			// draw the edges
			gf_timeCalc("drawing edges");
			for (var gt_bv_nodeId in this.graphs[subject].edges)
			{
				for (var gt_bv_edgeId in this.graphs[subject].edges[gt_bv_nodeId])
				{
					var gt_bv_edge = this.graphs[subject].edges[gt_bv_nodeId][gt_bv_edgeId];
	
					// perhaps add a space if edgesOut > 1
					this.drawArrow(gt_bv_edge);
				}
			}
			gf_timeCalc("drawing edges");
			
			var ioTop	= false;		
			// check tin and tout of startnodes
			for (var gt_bv_startNode in gt_bv_graph.startNodes)
			{			
				ioTop	= ioTop || this.checkObjectPort (gt_bv_startNode, "t", "i") === false || this.checkObjectPort (gt_bv_startNode, "t", "o") === false;
			}
			
			// move the canvas down when one of the start nodes has an incoming or and outgoing edge at the top
			if (ioTop)
			{
				gv_originalViewBox.y -= 100;
				gf_paperZoomReset();
			}
        		
        	// call the callback function (if one)
            if (gf_isset(callback) && gf_functionExists(callback))
            {
            	callback();
            }
		}
	};
	
	/**
	 * Draws a node.
	 * 
	 * @private
	 * @param {Object} node The node object created via addNode.
	 * @returns {void}
	 */
	this.drawNode = function (node)
	{
		gf_timeCalc("drawing nodes - drawNode() - calculate");
		var gt_bv_style		= null;
		var gt_bv_text		= node.node.textToString();
		var gt_bv_imgSrc	= "";
		var gt_bv_imgWidth	= 0;
		var gt_bv_imgHeight	= 0;
		
		var gt_bv_shape		= node.node.getShape();
		var gt_bv_start		= node.node.isStart(true);
		var gt_bv_end		= node.node.isEnd(true);
		
		if (gf_isset(this.renderObjects.nodes) && gf_isset(this.renderObjects.nodes[node.id]))
		{
			node.posx	+= this.renderObjects.nodes[node.id].posrx;
			node.posy	+= this.renderObjects.nodes[node.id].posry;
		}
		
		// when the shape of the node is a circle apply correct the style set for circles
		if (gt_bv_shape == "circle")
		{
			if (gt_bv_start)
			{
				gt_bv_style = gf_mergeStyles(gv_bv_circleNode.style, gv_bv_circleNode.styleStart);
			}
			else if (gt_bv_end)
			{
				gt_bv_style = gf_mergeStyles(gv_bv_circleNode.style, gv_bv_circleNode.styleEnd);
			}
			else
			{
				gt_bv_style = gv_bv_circleNode.style;
			}
			
			// get the image source
			if (gf_isset(gv_nodeTypeImg[node.node.textToString()]))
			{
				gt_bv_imgSrc	= gv_imgPath + gv_nodeTypeImg[node.node.textToString()];
				gt_bv_imgHeight	= gv_bv_circleNode.imgHeight;
				gt_bv_imgWidth	= gv_bv_circleNode.imgWidth;
				gt_bv_text		= "";
			}
		}
		
		// when the shape of the node is a roundedrectangle apply correct the style set for circles
		else
		{
			if (gt_bv_start)
			{
				gt_bv_style = gf_mergeStyles(gv_bv_rectNode.style, gv_bv_rectNode.styleStart);
			}
			else
			{
				gt_bv_style = gv_bv_rectNode.style;
			}
		}
		gf_timeCalc("drawing nodes - drawNode() - calculate");
			
		gf_timeCalc("drawing nodes - drawNode() - create");
		// create the GClabel at the x and y ordinates and pass the shape, the text and the id to the GClabel
		var gt_bv_rect	= new GClabel(node.posx, node.posy, gt_bv_text, node.node.getShape(), node.id, false, true);
		gf_timeCalc("drawing nodes - drawNode() - create");
		
		gf_timeCalc("drawing nodes - drawNode() - apply");
		
		if (gv_estimateTextDimensions)
			gt_bv_rect.setEstimateTextDimensions(true);
		
		// apply the deactivation status to the label
		if (node.node.isDeactivated(true))
			gt_bv_rect.deactivate(true);
				
		// apply the selection status to the label
		if (gf_isset(node.selected) && node.selected === true)
			gt_bv_rect.select(true);
			
		// apply the image
		if (gt_bv_imgSrc != "")
			gt_bv_rect.setImg(gt_bv_imgSrc, gt_bv_imgWidth, gt_bv_imgHeight);
			
		// determine click type
		var gt_clickType	= "bv";
		/*
		if (node.node.parentMacro.id != "##main##" && node.node.id == 0)
			gt_clickType +=	"_noclick";
		*/
		
		if (node.node.getType() == "macro")
			gt_clickType += "_dblclick";
			
		// apply the style
		gf_timeCalc("drawing nodes - drawNode() - apply III");
		gt_bv_rect.setStyle(gt_bv_style);
		gf_timeCalc("drawing nodes - drawNode() - apply III");
		gt_bv_rect.click(gt_clickType);	
		gf_timeCalc("drawing nodes - drawNode() - apply");
	};
	
	/**
	 * Updates the port setting of an object (blocks a port for incoming / outgoing edges).
	 * 
	 * @private
	 * @param {int} id The id of the node to update.
	 * @param {String} port The port to update. Possible values: "t" (top), "r" (right), "b" (bottom), "l" (left)
	 * @param {String} flag The flag to set. Possible values: "o" (outgoing), "i" (incoming)
	 * @returns {void}
	 */
	this.editObjectPort = function (id, port, flag)
	{
		// check if the ports statuses exist; if not: create them
		if (!gf_isset(this.objectPorts[id]))
			this.addObjectPort(id);
		
		// only update when the port is of type "t", "r", "l", "b" and the flag is either "o" or "i"
		if (gf_isset(this.ports[port]) && (flag == "i" || flag == "o"))
		{
			// check if the port is blocked and update the port
			if (this.checkObjectPort(id, port, flag))
			{
				this.objectPorts[id][port]	= flag;
				this.objectPorts[id][port + "c"]++;
			}
		}
	};
	
	/**
	 * Determine which arrow shape would fit best.
	 * The resulting object contains the following information:
	 * <br />
	 * - shape: The shape of the arrow that fits to the given information.<br />
	 * - length: The length of the resulting arrow<br />
	 * - space1: space1 of the arrow<br />
	 * - space2: space2 of the arrow<br />
	 * - firstLine: the firstLine of the arrow
	 * 
	 * @private
	 * @param {int} startx The x ordinate of the start point.
	 * @param {int} starty The y ordinate of the start point.
	 * @param {int} endx The x ordinate of the end point.
	 * @param {int} endy The y ordinate of the end point.
	 * @param {String} startPos The port of the start point. Needed to determine the correct shape.
	 * @param {String} endPos The port of the end point. Needed to determine the correct shape.
	 * @returns {Object} The shape, length, space1, space2, firstLine and intersection status of the arrow.
	 */
	this.getArrowShape = function (startx, starty, endx, endy, startPos, endPos)
	{
		
		var gt_bv_shape		= "Z";
		var gt_bv_length	= 0;
		var gt_bv_space1	= 0;
		var gt_bv_space2	= 0;
		
		var gt_bv_firstLine	= startPos == "l" || startPos == "r" 	? "h" : "v";
		
		// gv_bv_nodeSettings.distanceX | gv_bv_nodeSettings.distanceY; gv_bv_nodeSettings.arrowSpace
		
		// get the distance between start and end point
		var gt_bv_diffXreal	= Math.round(startx) - Math.round(endx);
		var gt_bv_diffYreal	= Math.round(starty) - Math.round(endy);
		var gt_bv_diffX		= Math.abs(gt_bv_diffXreal);
		var gt_bv_diffY		= Math.abs(gt_bv_diffYreal);
		
		// same port (like top -> top): shape U or ZU
		if (startPos == endPos)
		{
			gt_bv_space1	= startPos == "l" || startPos == "t" ? 0 - gv_bv_nodeSettings.arrowSpace : gv_bv_nodeSettings.arrowSpace;
			gt_bv_length	= gt_bv_diffX + gt_bv_diffY + 2 * gv_bv_nodeSettings.arrowSpace;
			
			// get the shape depending on the ports and the distance
			if (	gt_bv_diffX < gv_bv_nodeSettings.distanceX / 2 && (startPos == "t" || startPos == "b") ||
					gt_bv_diffY < gv_bv_nodeSettings.distanceY / 2 && (startPos == "l" || startPos == "r"))
			{
				gt_bv_shape 		= "ZU";
				gt_bv_space2		= startPos == "l" || startPos == "t" ? 0 - gv_bv_nodeSettings.arrowSpace : gv_bv_nodeSettings.arrowSpace;
				gt_bv_length		+= 2 * gv_bv_nodeSettings.arrowSpace;
			}
			
			// default shape for the same port is "U"
			else
			{
				gt_bv_shape			= "U";
			}
		}
		
		// opposing ports
		else if (	startPos == "l" && endPos == "r" ||
					startPos == "t" && endPos == "b" ||
					startPos == "b" && endPos == "t" ||
					startPos == "r" && endPos == "l")
		{
			gt_bv_length	= gt_bv_diffX + gt_bv_diffY;
			
			if (	startPos == "l" && gt_bv_diffXreal > 0 || 
					startPos == "r" && gt_bv_diffXreal < 0 ||
					startPos == "t" && gt_bv_diffYreal > 0 ||
					startPos == "b" && gt_bv_diffYreal < 0)
			{
				// shape "I" for opposing ports and either the x or the y ordinate of start and end point are the same
				if (gt_bv_diffX == 0 || gt_bv_diffY == 0)
				{
					gt_bv_shape			= "I";
				}
				
				// if neither the x nor the y ordinate of start and end point are the same the shape can either be "S" or "Z"
				else
				{
					// when either the distance between the x ordinates is smaller than the x ordinate of the node-distance or
					// the distance between the y ordinates is smaller than the y ordinate of the node-distance the shape has to be a S
					if (gt_bv_diffX < gv_bv_nodeSettings.distanceX / 2 || gt_bv_diffY < gv_bv_nodeSettings.distanceY / 2)
					{
						gt_bv_shape			= "S";
						gt_bv_length		+= 4 * gv_bv_nodeSettings.arrowSpace;
						gt_bv_space1		= startPos == "l" || startPos == "t" ? 0 - gv_bv_nodeSettings.arrowSpace : gv_bv_nodeSettings.arrowSpace;
					}
					
					// default shape for opposing ports: Z
					else
					{
						gt_bv_shape			= "Z";
					}
				}
			}
			
			// shape C for opposing ports and the arrow has to pass the start and end node
			else
			{
				gt_bv_shape			= "C";
				gt_bv_length		+= 6 * gv_bv_nodeSettings.arrowSpace;
				gt_bv_space2		= startPos == "l" || startPos == "t" ? 0 - gv_bv_nodeSettings.arrowSpace : gv_bv_nodeSettings.arrowSpace;
			}
		}
		
		// all other port combinations
		else
		{
			gt_bv_length	= gt_bv_diffX + gt_bv_diffY;
			
			if (	startPos == "l" && gt_bv_diffXreal > 0 || 
					startPos == "r" && gt_bv_diffXreal < 0 ||
					startPos == "t" && gt_bv_diffYreal > 0 ||
					startPos == "b" && gt_bv_diffYreal < 0)
			{
				
				// use a "U" shape combined with an "I" shaped arrow when appropriate
				if (gt_bv_diffX <= gv_bv_nodeSettings.distanceX / 4 || gt_bv_diffY <= gv_bv_nodeSettings.distanceY / 4)
				{
					gt_bv_shape			= "UI";
					gt_bv_length		+= 2 * gv_bv_nodeSettings.arrowSpace;
					gt_bv_space1		= startPos == "l" || startPos == "t" ? 0 - gv_bv_nodeSettings.arrowSpace : gv_bv_nodeSettings.arrowSpace;
					gt_bv_space2		= endPos == "r" || endPos == "b" ? 0 - gv_bv_nodeSettings.arrowSpace2 : gv_bv_nodeSettings.arrowSpace2;
				}
				
				// default shape is "L"
				else
				{
					gt_bv_shape			= "L";
				}
			}
			
			// shape "G"
			else
			{
				gt_bv_shape			= "G";
				gt_bv_length		+= 4 * gv_bv_nodeSettings.arrowSpace;
				gt_bv_space1		= startPos == "l" || startPos == "t" ? 0 - gv_bv_nodeSettings.arrowSpace : gv_bv_nodeSettings.arrowSpace;
				gt_bv_space2		= endPos == "l" || endPos == "t" ? 0 - gv_bv_nodeSettings.arrowSpace : gv_bv_nodeSettings.arrowSpace;
			}
		}
		
		return {shape: gt_bv_shape, length: gt_bv_length, space1: gt_bv_space1, space2: gt_bv_space2, firstLine: gt_bv_firstLine};
	};
	
	/**
	 * Read the configuration file and determine which ports (top, bottom, left, right) of an object are set to allow incoming / outgoing / both / none edges.
	 * 
	 * @private
	 * @returns {void}
	 */
	this.readPortSettings = function ()
	{
		var gt_bv_t	= "";
		var gt_bv_b	= "";
		var gt_bv_r	= "";
		var gt_bv_l	= "";
		
		// IN
		if (gf_isset(gv_bv_arrow.tin) && gv_bv_arrow.tin)
			gt_bv_t += "i";
	
		if (gf_isset(gv_bv_arrow.bin) && gv_bv_arrow.bin)
			gt_bv_b += "i";
	
		if (gf_isset(gv_bv_arrow.lin) && gv_bv_arrow.lin)
			gt_bv_l += "i";
	
		if (gf_isset(gv_bv_arrow.rin) && gv_bv_arrow.rin)
			gt_bv_r += "i";
		
		// OUT
		if (gf_isset(gv_bv_arrow.tout) && gv_bv_arrow.tout)
			gt_bv_t += "o";
	
		if (gf_isset(gv_bv_arrow.bout) && gv_bv_arrow.bout)
			gt_bv_b += "o";
	
		if (gf_isset(gv_bv_arrow.lout) && gv_bv_arrow.lout)
			gt_bv_l += "o";
	
		if (gf_isset(gv_bv_arrow.rout) && gv_bv_arrow.rout)
			gt_bv_r += "o";
		
		this.portSettings.t = gt_bv_t;
		this.portSettings.b = gt_bv_b;
		this.portSettings.l = gt_bv_l;
		this.portSettings.r = gt_bv_r;
	};

	/**
	 * TODO: comment
	 */
	this.setRenderObjects = function (nodes, edges)
	{
		this.renderObjects	= {"nodes": nodes, "edges": edges};
	};
}