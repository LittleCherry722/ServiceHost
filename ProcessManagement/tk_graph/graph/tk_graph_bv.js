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
 * graph file for behavior view
 */

var gv_bv_objectPorts	= {};

var gv_bv_graphs = {};				// array of {nodes: Array(), edges: Array()};

var gv_bv_ports			= {t: "t", b: "b", r: "r", l: "l"};
var gv_bv_portSettings	= {t: "io", b: "io", r: "io", l: "io"};

/*
 * called by API: adds a subject to the graph
 */
function gf_bv_addSubject (gt_bv_subject)
{
	gv_bv_graphs[gt_bv_subject]	= {nodes: {}, edges: {}, startNodes: {}, nodeCount: 0, startNodeCount: 0};
}

/*
 * called by API: adds a node to a subject
 * 
 * gt_bv_text is
 * - "R" / "S" for receive and send
 * - "" for end
 * - any other text for internal actions
 */
function gf_bv_addNode (gt_bv_subject, gt_bv_id, gt_bv_node, gt_bv_selected)
{
	if (!gf_isset(gv_bv_graphs[gt_bv_subject]))
		gf_bv_addSubject(gt_bv_subject);
		
	if (!gf_isset(gt_bv_id) || !gf_isset(gt_bv_node))
		return false;
	
	var gt_bv_graph = gv_bv_graphs[gt_bv_subject];
	
	if (!gf_isset(gt_bv_selected) || gt_bv_selected != true)
		gt_bv_selected = false;
	
	// add node
	gt_bv_graph.nodes[gt_bv_id] = {	id:			gt_bv_id,
									node:		gt_bv_node,
									visited:	false,
									posx:		0,
									posy:		0,
									edgesIn:	0,
									edgesOut:	0,
									edgesOutCur:0,
									selected:	gt_bv_selected
									};
	gt_bv_graph.nodeCount++;
	
	// add node to startNodes
	if (gt_bv_node.isStart())
	{
		gt_bv_graph.startNodes[gt_bv_id] = true;
		gt_bv_graph.startNodeCount++;
	}
}

/*
 * called by the API: adds an edge to a subject
 */
function gf_bv_addEdge (gt_bv_subject, gt_bv_id, gt_bv_start, gt_bv_end, gt_bv_edge, gt_bv_selected)
{
	if (gf_isset(gv_bv_graphs[gt_bv_subject], gt_bv_edge) && gf_isset(gv_bv_graphs[gt_bv_subject].nodes[gt_bv_start], gv_bv_graphs[gt_bv_subject].nodes[gt_bv_end]))
	{	
		
		if (!gf_isset(gt_bv_selected) || gt_bv_selected != true)
			gt_bv_selected = false;
		
		var gt_bv_graph = gv_bv_graphs[gt_bv_subject];
		
		if (!gf_isset(gt_bv_graph.edges[gt_bv_start]))
			gt_bv_graph.edges[gt_bv_start] = [];
		
		// store the edge
		gt_bv_graph.edges[gt_bv_start][gt_bv_graph.edges[gt_bv_start].length] = {	start:		gt_bv_start,
																					end:		gt_bv_end,
																					edge:		gt_bv_edge,
																					id:			gt_bv_id,
																					visited:	false,
																					selected:	gt_bv_selected
																					};
		
		if (!gt_bv_graph.nodes[gt_bv_end].node.isStart() || !gt_bv_graph.nodes[gt_bv_start].node.isStart())
		{
			gt_bv_graph.nodes[gt_bv_start].edgesOut++;
			gt_bv_graph.nodes[gt_bv_end].edgesIn++;
		}
	}
}

/*
 * DISPLAYING THE GRAPH
 */

/*
 * main function for drawing the graph
 */
function gf_bv_drawGraph (gt_bv_subject)
{
	if (gf_isset(gv_bv_graphs[gt_bv_subject]))
	{
		
		// init the paper
		gf_paperChangeView("bv");
		
		// clear arrays
		gv_bv_objectPorts	= {};
		
		gv_node_parents		= {};
		gv_node_children	= {};
		
		// read the object's port settings from the config file
		gf_bv_readPortSettings();
		
		// retrieve the graph and clear the nodeSet
		var gt_bv_graph		= gv_bv_graphs[gt_bv_subject];
		var gt_bv_nodeSet	= {count: 0, nodes: {}};
		
		var gt_bv_startNodes	= gt_bv_graph.startNodeCount;
			gt_bv_startNodes	= gt_bv_startNodes > 0 ? gt_bv_startNodes - 1 : 0;
		
		var gt_bv_distanceX = gv_bv_nodeSettings.distanceX;
		var gt_bv_distanceY = gv_bv_nodeSettings.distanceY;
		
		var gt_bv_x = Math.round(gv_paperSizes.bv_width/2) + gv_bv_nodeSettings.startX;
		var gt_bv_y = gv_bv_nodeSettings.startY;
		
		var gt_bv_mostLeft	= gt_bv_x;
		
		// 1. start with the start nodes
		for (var gt_bv_startNode in gt_bv_graph.startNodes)
		{
			var gt_bv_edgesOut	= gt_bv_graph.nodes[gt_bv_startNode].edgesOut;
				gt_bv_edgesOut	= gt_bv_edgesOut > 0 ? gt_bv_edgesOut - 1 : 0
			
			gt_bv_graph.nodes[gt_bv_startNode].posx		= gt_bv_x + gt_bv_edgesOut * gt_bv_distanceX/2;
			gt_bv_graph.nodes[gt_bv_startNode].posy		= gt_bv_y;
			gt_bv_graph.nodes[gt_bv_startNode].visited	= true;
			
			gt_bv_nodeSet.nodes[gt_bv_startNode]	= gt_bv_startNode;
			gt_bv_nodeSet.count++;
			
			gt_bv_x += gt_bv_distanceX + gt_bv_edgesOut * gt_bv_distanceX/2;
			
			gv_node_children["n" + gt_bv_startNode] = [];
		}

		// 2 set nodes connected by edges (starting with the start nodes)
		var gt_bv_rescueCount = 10000;
		// var gt_bv_rescueCount = 10;
		while (gt_bv_nodeSet.count > 0 && gt_bv_rescueCount > 0)
		{
			for (var gt_bv_node in gt_bv_nodeSet.nodes)
			{
				
				delete gt_bv_nodeSet.nodes[gt_bv_node];
				gt_bv_nodeSet.count--;
				
				if (gf_isset(gt_bv_graph.edges[gt_bv_node]))
				{
					for (var gt_bv_edgeId in gt_bv_graph.edges[gt_bv_node])
					{
						var gt_bv_edge = gt_bv_graph.edges[gt_bv_node][gt_bv_edgeId];
						
						gt_bv_edge.visited = true;
						
						if (!gf_isset(gt_bv_nodeSet.nodes[gt_bv_edge.end]))
						{
							// draw node
							var gt_bv_edgesOut	= gt_bv_graph.nodes[gt_bv_edge.start].edgesOut;
								gt_bv_edgesOut	= gt_bv_edgesOut > 0 ? gt_bv_edgesOut - 1 : 0
						
							if (gt_bv_graph.nodes[gt_bv_edge.end].visited == false)
							{
								gt_bv_graph.nodes[gt_bv_edge.end].posx		= gt_bv_graph.nodes[gt_bv_edge.start].posx + (gt_bv_graph.nodes[gt_bv_edge.start].edgesOutCur - gt_bv_edgesOut/2) * gt_bv_distanceX;
								gt_bv_graph.nodes[gt_bv_edge.end].posy		= gt_bv_graph.nodes[gt_bv_edge.start].posy + gt_bv_distanceY;
								gt_bv_graph.nodes[gt_bv_edge.end].visited	= true;
								gt_bv_graph.nodes[gt_bv_edge.start].edgesOutCur++;
								
								// mostLeft
								if (gt_bv_graph.nodes[gt_bv_edge.end].posx < gt_bv_mostLeft)
									gt_bv_mostLeft = gt_bv_graph.nodes[gt_bv_edge.end].posx;
									
								if (!gf_isset(gv_node_children["n" + gt_bv_edge.start]))
									gv_node_children["n" + gt_bv_edge.start] = [];
									
								gv_node_parents["n" + gt_bv_edge.end]	= gt_bv_edge.start;
								gv_node_children["n" + gt_bv_edge.start][gv_node_children["n" + gt_bv_edge.start].length] = gt_bv_edge.end;
							}
							
							gt_bv_nodeSet.nodes[gt_bv_edge.end] = gt_bv_edge.end;
							gt_bv_nodeSet.count++;
						}
					}
				}
			}
			
			gt_bv_rescueCount--;
		}
		
		// 3. all other nodes (not connected to the start nodes)
		gt_bv_x	= gt_bv_mostLeft - gv_bv_nodeSettings.startNewX;
		gt_bv_y = gv_bv_nodeSettings.startNewY;
		for (var gt_bv_node in gt_bv_graph.nodes)
		{
			if (gt_bv_graph.nodes[gt_bv_node].visited == false)
			{
				gt_bv_graph.nodes[gt_bv_node].posx		= gt_bv_x;
				gt_bv_graph.nodes[gt_bv_node].posy		= gt_bv_y;
				gt_bv_graph.nodes[gt_bv_node].visited	= true;
				gt_bv_y += gt_bv_distanceY/2;
			}
		}
		
		// draw the nodes
		for (var gt_bv_nodeId in gv_bv_graphs[gt_bv_subject].nodes)
		{
			var gt_bv_node = gv_bv_graphs[gt_bv_subject].nodes[gt_bv_nodeId];
			
			gf_bv_drawNode(gt_bv_node);
			
			gf_bv_addObjectPort(gt_bv_node.id);
		}
		
		// draw the edges
		for (var gt_bv_nodeId in gv_bv_graphs[gt_bv_subject].edges)
		{
			for (var gt_bv_edgeId in gv_bv_graphs[gt_bv_subject].edges[gt_bv_nodeId])
			{
				var gt_bv_edge = gv_bv_graphs[gt_bv_subject].edges[gt_bv_nodeId][gt_bv_edgeId];

				// perhaps add a space if edgesOut > 1
				gf_bv_drawArrow(gt_bv_edge);
			}
		}
		
		var ioTop	= false;		
		// check tin and tout of startnodes
		for (var gt_bv_startNode in gt_bv_graph.startNodes)
		{			
			ioTop	= ioTop || gf_bv_checkObjectPort (gt_bv_startNode, "t", "i") === false || gf_bv_checkObjectPort (gt_bv_startNode, "t", "o") === false
		}
		
		if (ioTop)
		{
			gv_originalViewBox.y -= 100;
			gf_paperZoomReset();
		}
	}
}

/*
 * read the config and determine which ports (top, bottom, left, right) of an object are set to allow incoming / outgoing / both / none edges
 */
function gf_bv_readPortSettings ()
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
	
	gv_bv_portSettings.t = gt_bv_t;
	gv_bv_portSettings.b = gt_bv_b;
	gv_bv_portSettings.l = gt_bv_l;
	gv_bv_portSettings.r = gt_bv_r;
}

/*
 * store the available ports of the corresponding object
 */
function gf_bv_addObjectPort (gt_bv_id)
{
	gv_bv_objectPorts[gt_bv_id] = {	t: gv_bv_portSettings.t, tc: 0,
									b: gv_bv_portSettings.b, bc: 0,
									l: gv_bv_portSettings.l, lc: 0,
									r: gv_bv_portSettings.r, rc: 0};
}

/*
 * checks if an object's port (gt_bv_port) is available for an incoming / outgoing edge (gt_bv_flag)
 */
function gf_bv_checkObjectPort (gt_bv_id, gt_bv_port, gt_bv_flag)
{
	if (!gf_isset(gv_bv_objectPorts[gt_bv_id]))
		gf_bv_addObjectPort(gt_bv_id);
	
	if (gf_isset(gv_bv_ports[gt_bv_port]) && (gt_bv_flag == "i" || gt_bv_flag == "o"))
	{
		if (gv_bv_objectPorts[gt_bv_id][gt_bv_port] == "io" || gv_bv_objectPorts[gt_bv_id][gt_bv_port] == gt_bv_flag)
		{
			return true;
		}
	}
	return false;
}

/*
 * updates the port setting of an object (blocks a port for incoming / outgoing edges)
 */
function gf_bv_editObjectPort (gt_bv_id, gt_bv_port, gt_bv_flag)
{
	if (!gf_isset(gv_bv_objectPorts[gt_bv_id]))
		gf_bv_addObjectPort(gt_bv_id);
	
	if (gf_isset(gv_bv_ports[gt_bv_port]) && (gt_bv_flag == "i" || gt_bv_flag == "o"))
	{
		if (gf_bv_checkObjectPort(gt_bv_id, gt_bv_port, gt_bv_flag))
		{
			gv_bv_objectPorts[gt_bv_id][gt_bv_port]	= gt_bv_flag;
			gv_bv_objectPorts[gt_bv_id][gt_bv_port + "c"]++;
		}
	}
}

/*
 * DRAWING FUNCTIONS
 */

/*
 * draws a node
 */
function gf_bv_drawNode (gt_bv_node)
{
	var gt_bv_style	= null;
	
	if (gt_bv_node.node.getShape() == "circle")
	{
		if (gt_bv_node.node.isStart())
		{
			gt_bv_style = gf_mergeStyles(gv_bv_circleNode.style, gv_bv_circleNode.styleStart);
		}
		else if (gt_bv_node.node.isEnd())
		{
			gt_bv_style = gf_mergeStyles(gv_bv_circleNode.style, gv_bv_circleNode.styleEnd);
		}
		else
		{
			gt_bv_style = gv_bv_circleNode.style;
		}
	}
	else
	{
		if (gt_bv_node.node.isStart())
		{
			gt_bv_style = gf_mergeStyles(gv_bv_rectNode.style, gv_bv_rectNode.styleStart);
		}
		else
		{
			gt_bv_style = gv_bv_rectNode.style;
		}
	}
		
	var gt_bv_rect	= new GFlabel(gt_bv_node.posx, gt_bv_node.posy, gt_bv_node.node.getTextGraph(), gt_bv_node.node.getShape(), gt_bv_node.id);
	
	if (gt_bv_node.node.isDeactivated())
		gt_bv_rect.deactivate();
			
	if (gf_isset(gt_bv_node.selected) && gt_bv_node.selected === true)
		gt_bv_rect.select();
		
	gt_bv_rect.setStyle(gt_bv_style);
	gt_bv_rect.click("bv");	
}

/*
 * draws an arrow between two objects
 */
function gf_bv_drawArrow (gt_bv_edgeData)
{
	if (!gf_isset(gt_bv_edgeData))
		return false;
		
	var gt_bv_start	= gt_bv_edgeData.start;
	var gt_bv_end	= gt_bv_edgeData.end;
	
	if (!gf_isset(gv_objects_nodes[gt_bv_start], gv_objects_nodes[gt_bv_end]))
		return false;		
		
	var gt_bv_objStart	= gv_objects_nodes[gt_bv_start].getBoundaries();
	var gt_bv_objEnd	= gv_objects_nodes[gt_bv_end].getBoundaries();
	
	var gt_bv_firstLine			= "v";
	var gt_bv_endLine			= "v";
	var gt_bv_startx			= 0;
	var gt_bv_starty			= 0;
	var gt_bv_endx				= 0;
	var gt_bv_endy				= 0;
	var gt_bv_headCorrection	= 0;
		
	var gt_bv_minDist	= 999999999;
	var gt_bv_posStart	= "b";
	var gt_bv_posEnd	= "t";
	var gt_bv_tmpDist	= 0;
	var gt_bv_tmpDistX	= 0;
	var gt_bv_tmpDistY	= 0;
	
	var gt_bv_o	= "";
	var gt_bv_i	= "";
	
	var gt_bv_space1	= 0;
	var gt_bv_space2	= 0;
	var gt_bv_minLength	= 999999999;
	var gt_bv_shape		= "I";
	
	var mapPorts		= {t: "top", b: "bottom", r: "right", l: "left"};
	
	var gt_bv_edge	= new GFpath(gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_shape, gt_bv_edgeData.edge.textToString(), gt_bv_edgeData.id);
		gt_bv_edge.hide();
		
	// cycle through all port combinations at startObject and endObject to determine the shape of the arrow and the ports to use
	for (var gt_bv_out in gv_bv_ports)
	{
		for (var gt_bv_in in gv_bv_ports)
		{
			
			if (gf_bv_checkObjectPort(gt_bv_start, gt_bv_out, "o") && gf_bv_checkObjectPort(gt_bv_end, gt_bv_in, "i"))
			{
				gt_bv_o	= gt_bv_out;
				gt_bv_i	= gt_bv_in;
				
				gt_bv_startx	= gt_bv_o == "l" || gt_bv_o == "r" ? gt_bv_objStart[mapPorts[gt_bv_o]]	: gt_bv_objStart.x;
				gt_bv_starty	= gt_bv_o == "t" || gt_bv_o == "b" ? gt_bv_objStart[mapPorts[gt_bv_o]]	: gt_bv_objStart.y;
				gt_bv_endx		= gt_bv_i == "l" || gt_bv_i == "r" ? gt_bv_objEnd[mapPorts[gt_bv_i]]	: gt_bv_objEnd.x;
				gt_bv_endy		= gt_bv_i == "t" || gt_bv_i == "b" ? gt_bv_objEnd[mapPorts[gt_bv_i]]	: gt_bv_objEnd.y;
				
				// calculate the arrow shape that fits best for this port combination
				var gt_bv_arrowShape	= gf_bv_getArrowShape(gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_o, gt_bv_i);
				
				var gt_bv_setAsMin	= false;
				
				// correction to avoid intersection problems
				var gt_bv_x1	= gt_bv_startx;
				var gt_bv_y1	= gt_bv_starty;
				var gt_bv_x2	= gt_bv_endx;
				var gt_bv_y2	= gt_bv_endy;
				
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
				gt_bv_edge.setPositionStart(gt_bv_x1, gt_bv_y1);
				gt_bv_edge.setPositionEnd(gt_bv_x2, gt_bv_y2);
				gt_bv_edge.setFirstLine(gt_bv_arrowShape.firstLine);
				gt_bv_edge.setSpace1(gt_bv_arrowShape.space1);
				gt_bv_edge.setSpace2(gt_bv_arrowShape.space2);
				gt_bv_edge.setShape(gt_bv_arrowShape.shape);
				
				// check if the new arrow would fit better than the currently best
				if (!gt_bv_edge.checkIntersection())
				{
					
					if (gt_bv_arrowShape.shape == "L" && gt_bv_o == "b")
						gt_bv_arrowShape.length += gv_bv_nodeSettings.arrowSpace;
					
					if (gt_bv_arrowShape.length < gt_bv_minLength)
					{
						gt_bv_setAsMin = true;
					}
					else if (gt_bv_arrowShape.length == gt_bv_minLength)
					{
						if (gt_bv_shape == gt_bv_arrowShape.shape)
						{	
							
							if (gt_bv_posStart == "r" && gt_bv_o == "l" && gt_bv_startx > gt_bv_endx)
								gt_bv_setAsMin = true;
							
							if (gt_bv_posStart == "t" && gt_bv_o == "b" && gt_bv_starty < gt_bv_endy)
								gt_bv_setAsMin = true;
						}
					}
					else
					{
						if (gt_bv_shape == gt_bv_arrowShape.shape)
						{	
							
							if (gt_bv_posStart == "r" && gt_bv_o == "l" && gt_bv_startx > gt_bv_endx)
								gt_bv_setAsMin = true;
							
							if (gt_bv_posStart == "t" && gt_bv_o == "b" && gt_bv_starty < gt_bv_endy)
								gt_bv_setAsMin = true;
						}
					}
				}
				
				if (gt_bv_setAsMin)
				{
					gt_bv_minLength	= gt_bv_arrowShape.length;
					gt_bv_space1	= gt_bv_arrowShape.space1;
					gt_bv_space2	= gt_bv_arrowShape.space2;
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
	gf_bv_editObjectPort(gt_bv_start, gt_bv_posStart, "o");
	gf_bv_editObjectPort(gt_bv_end, gt_bv_posEnd, "i");

	gt_bv_firstLine	= gt_bv_posStart == "l" || gt_bv_posStart == "r" 	? "h" : "v";
	gt_bv_endLine	= gt_bv_posEnd == "l" 	|| gt_bv_posEnd == "r" 		? "h" : "v";
	
	gt_bv_startx	= gt_bv_firstLine == "h"	? gt_bv_objStart[mapPorts[gt_bv_posStart]]	: gt_bv_objStart.x;
	gt_bv_starty	= gt_bv_firstLine == "v"	? gt_bv_objStart[mapPorts[gt_bv_posStart]]	: gt_bv_objStart.y;
	gt_bv_endx		= gt_bv_endLine == "h"		? gt_bv_objEnd[mapPorts[gt_bv_posEnd]]		: gt_bv_objEnd.x;
	gt_bv_endy		= gt_bv_endLine == "v"		? gt_bv_objEnd[mapPorts[gt_bv_posEnd]]		: gt_bv_objEnd.y;
	
	gt_bv_edge.setPositionStart(gt_bv_startx, gt_bv_starty);
	gt_bv_edge.setPositionEnd(gt_bv_endx, gt_bv_endy);
	
	gt_bv_edge.setFirstLine(gt_bv_firstLine);
	gt_bv_edge.setSpace1(gt_bv_space1);
	gt_bv_edge.setSpace2(gt_bv_space2);
	gt_bv_edge.setShape(gt_bv_shape);
	// gt_bv_edge.updatePath();
	gt_bv_edge.setStyle(gv_bv_arrow.style);
	gt_bv_edge.show();
	gt_bv_edge.click();
	
	if (gt_bv_edgeData.edge.isDeactivated())
		gt_bv_edge.deactivate();
	
	if (gf_isset(gt_bv_edgeData.selected) && gt_bv_edgeData.selected === true)
		gt_bv_edge.select();
}

/*
 * determine which arrow shape would fit best
 */
function gf_bv_getArrowShape (gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_startPos, gt_bv_endPos)
{
	
	var gt_bv_shape		= "Z";
	var gt_bv_length	= 0;
	var gt_bv_space1	= 0;
	var gt_bv_space2	= 0;
	
	var gt_bv_intersects	= false;
	
	var gt_bv_firstLine	= gt_bv_startPos == "l" || gt_bv_startPos == "r" 	? "h" : "v";
	var gt_bv_endLine	= gt_bv_endPos == "l" 	|| gt_bv_endPos == "r" 		? "h" : "v";
	
	// gv_bv_nodeSettings.distanceX | gv_bv_nodeSettings.distanceY; gv_bv_nodeSettings.arrowSpace
	
	var gt_bv_diffXreal	= Math.round(gt_bv_startx) - Math.round(gt_bv_endx);
	var gt_bv_diffYreal	= Math.round(gt_bv_starty) - Math.round(gt_bv_endy);
	var gt_bv_diffX		= Math.abs(gt_bv_diffXreal);
	var gt_bv_diffY		= Math.abs(gt_bv_diffYreal);
	
	// same port (like top -> top): shape U or ZU
	if (gt_bv_startPos == gt_bv_endPos)
	{
		gt_bv_space1	= gt_bv_startPos == "l" || gt_bv_startPos == "t" ? 0 - gv_bv_nodeSettings.arrowSpace : gv_bv_nodeSettings.arrowSpace;
		gt_bv_length	= gt_bv_diffX + gt_bv_diffY + 2 * gv_bv_nodeSettings.arrowSpace;
		
		if (	gt_bv_diffX <= gv_bv_nodeSettings.distanceX / 2 && (gt_bv_startPos == "t" || gt_bv_startPos == "b") ||
				gt_bv_diffY <= gv_bv_nodeSettings.distanceY / 2 && (gt_bv_startPos == "l" || gt_bv_startPos == "r"))
		{
			gt_bv_shape 		= "ZU";
			gt_bv_space2		= gt_bv_startPos == "l" || gt_bv_startPos == "t" ? 0 - gv_bv_nodeSettings.arrowSpace : gv_bv_nodeSettings.arrowSpace;
			gt_bv_length		+= 2 * gv_bv_nodeSettings.arrowSpace;
		}
		else
		{
			gt_bv_shape			= "U";
		}
	}
	
	// opposing ports
	else if (	gt_bv_startPos == "l" && gt_bv_endPos == "r" ||
				gt_bv_startPos == "t" && gt_bv_endPos == "b" ||
				gt_bv_startPos == "b" && gt_bv_endPos == "t" ||
				gt_bv_startPos == "r" && gt_bv_endPos == "l")
	{
		gt_bv_length	= gt_bv_diffX + gt_bv_diffY;
		
		if (	gt_bv_startPos == "l" && gt_bv_diffXreal > 0 || 
				gt_bv_startPos == "r" && gt_bv_diffXreal < 0 ||
				gt_bv_startPos == "t" && gt_bv_diffYreal > 0 ||
				gt_bv_startPos == "b" && gt_bv_diffYreal < 0)
		{
			if (gt_bv_diffX == 0 || gt_bv_diffY == 0)
			{
				gt_bv_shape			= "I";
			}
			else
			{
				if (gt_bv_diffX <= gv_bv_nodeSettings.distanceX / 2 || gt_bv_diffY <= gv_bv_nodeSettings.distanceY / 2)
				{
					gt_bv_shape			= "S";
					gt_bv_length		+= 4 * gv_bv_nodeSettings.arrowSpace;
					gt_bv_space1		= gt_bv_startPos == "l" || gt_bv_startPos == "t" ? 0 - gv_bv_nodeSettings.arrowSpace : gv_bv_nodeSettings.arrowSpace;
				}
				else
				{
					gt_bv_shape			= "Z";
				}
			}
		}
		else
		{
			gt_bv_shape			= "C";
			gt_bv_length		+= 6 * gv_bv_nodeSettings.arrowSpace;
			gt_bv_space2		= gt_bv_startPos == "l" || gt_bv_startPos == "t" ? 0 - gv_bv_nodeSettings.arrowSpace : gv_bv_nodeSettings.arrowSpace;
		}
	}
	else
	{
		gt_bv_length	= gt_bv_diffX + gt_bv_diffY;
		
		if (	gt_bv_startPos == "l" && gt_bv_diffXreal > 0 || 
				gt_bv_startPos == "r" && gt_bv_diffXreal < 0 ||
				gt_bv_startPos == "t" && gt_bv_diffYreal > 0 ||
				gt_bv_startPos == "b" && gt_bv_diffYreal < 0)
		{
			if (gt_bv_diffX <= gv_bv_nodeSettings.distanceX / 4 || gt_bv_diffY <= gv_bv_nodeSettings.distanceY / 4)
			{
				gt_bv_shape			= "UI";
				gt_bv_length		+= 2 * gv_bv_nodeSettings.arrowSpace;
				gt_bv_space1		= gt_bv_startPos == "l" || gt_bv_startPos == "t" ? 0 - gv_bv_nodeSettings.arrowSpace : gv_bv_nodeSettings.arrowSpace;
			}
			else
			{
				gt_bv_shape			= "L";
			}
		}
		else
		{
			gt_bv_shape			= "G";
			gt_bv_length		+= 4 * gv_bv_nodeSettings.arrowSpace;
			gt_bv_space1		= gt_bv_startPos == "l" || gt_bv_startPos == "t" ? 0 - gv_bv_nodeSettings.arrowSpace : gv_bv_nodeSettings.arrowSpace;
			gt_bv_space2		= gt_bv_startPos == "l" || gt_bv_startPos == "t" ? 0 - gv_bv_nodeSettings.arrowSpace : gv_bv_nodeSettings.arrowSpace;
		}
	}
	
	return {shape: gt_bv_shape, length: gt_bv_length, space1: gt_bv_space1, space2: gt_bv_space2, intersects: gt_bv_intersects, firstLine: gt_bv_firstLine};
}