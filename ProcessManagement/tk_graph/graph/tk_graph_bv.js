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
var gv_bv_canvas	= null;
var gv_bv_ctx		= null;
var gv_bv_clickPos	= {x: 0, y: 0};
var gv_bv_objects	= new Array();
var gv_bv_lines		= new Array();

var gv_bv_objectPorts	= new Array();

var gv_bv_nodes	= new Array();		// contains id, text, position, width, height (all information stored in objcts)
var gv_bv_edges	= new Array();		// contains id, text, startObject, endObject, positionAtStartNode (t,b,l,r), positionAtEndNode (t,b,l,r), shape (U, Z, I, ... incl. firstLine, space|space1,2; -shape: for drawing: switch start- and end-point)

var gv_bv_graphs = new Array();		// array of {nodes: new Array(), edges: new Array()};

var gv_bv_clicks = new Array();		// array of (type: e | n, id: id, l: left, t: top, r: right, b: bottom}

var gv_bv_ports			= {t: "t", b: "b", r: "r", l: "l"};
var gv_bv_portSettings	= {t: "io", b: "io", r: "io", l: "io"};

/*
 * called by API: adds a subject to the graph
 */
function gf_bv_addSubject (gt_bv_subject)
{
	gv_bv_graphs[gt_bv_subject]	= {nodes: new Array(), edges: new Array(), startNodes: new Array(), nodeCount: 0, startNodeCount: 0};
}

/*
 * called by API: adds a node to a subject
 * 
 * gt_bv_text is
 * - "R" / "S" for receive and send
 * - "" for end
 * - any other text for internal actions
 */
function gf_bv_addNode (gt_bv_subject, gt_bv_id, gt_bv_text, gt_bv_type, gt_bv_selected)
{
	if (!gf_isset(gv_bv_graphs[gt_bv_subject]))
		gf_bv_addSubject(gt_bv_subject);
	
	var gt_bv_graph = gv_bv_graphs[gt_bv_subject];
	
	if (!gf_isset(gt_bv_text) || gt_bv_text.toLowerCase() == "end")
		gt_bv_text = "";
	
	if (!gf_isset(gt_bv_type))
		gt_bv_type = "normal";
	
	if (!gf_isset(gt_bv_selected) || gt_bv_selected != true)
		gt_bv_selected = false;
	
	// add node
	gt_bv_graph.nodes[gt_bv_id] = {	id:			gt_bv_id,
									text:		gt_bv_text,
									type:		gt_bv_type,
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
	if (gt_bv_type == "start")
	{
		gt_bv_graph.startNodes[gt_bv_id] = true;
		gt_bv_graph.startNodeCount++;
	}
}

/*
 * called by the API: adds an edge to a subject
 */
function gf_bv_addEdge (gt_bv_subject, gt_bv_id, gt_bv_start, gt_bv_end, gt_bv_text, gt_bv_selected)
{
	if (gf_isset(gv_bv_graphs[gt_bv_subject]) && gf_isset(gv_bv_graphs[gt_bv_subject].nodes[gt_bv_start], gv_bv_graphs[gt_bv_subject].nodes[gt_bv_end]))
	{	
		
		if (!gf_isset(gt_bv_selected) || gt_bv_selected != true)
			gt_bv_selected = false;
		
		var gt_bv_graph = gv_bv_graphs[gt_bv_subject];
		
		if (!gf_isset(gt_bv_graph.edges[gt_bv_start]))
			gt_bv_graph.edges[gt_bv_start] = new Array();
		
		// store the edge
		gt_bv_graph.edges[gt_bv_start][gt_bv_graph.edges[gt_bv_start].length] = {	start:		gt_bv_start,
																					end:		gt_bv_end,
																					text:		gt_bv_text,
																					id:			gt_bv_id,
																					visited:	false,
																					selected:	gt_bv_selected
																					};
		
		if (gt_bv_graph.nodes[gt_bv_end].type != "start" || gt_bv_graph.nodes[gt_bv_start].type != "start")
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
function gf_bv_drawGraph (gt_bv_canvasElem, gt_bv_subject)
{
	if (gf_isset(gv_bv_graphs[gt_bv_subject]))
	{
		// init the canvas element
		gf_bv_init(gt_bv_canvasElem);
		
		// clear arrays
		gv_bv_objects 		= new Array();
		gv_bv_clicks 		= new Array();
		gv_bv_objectPorts	= new Array();
		gv_bv_lines			= new Array();
		
		// read the object's port settings from the config file
		gf_bv_readPortSettings();
		
		// retrieve the graph and clear the nodeSet
		var gt_bv_graph		= gv_bv_graphs[gt_bv_subject];
		var gt_bv_nodeSet	= {count: 0, nodes: new Array()};
		
		var gt_bv_startNodes	= gt_bv_graph.startNodeCount;
			gt_bv_startNodes	= gt_bv_startNodes > 0 ? gt_bv_startNodes - 1 : 0;
		
		var gt_bv_distanceX = gv_bv_nodeSettings.distanceX;
		var gt_bv_distanceY = gv_bv_nodeSettings.distanceY;
		
		var gt_bv_x = (gv_bv_canvas.width - gt_bv_startNodes*gt_bv_distanceX)/2 + gv_bv_nodeSettings.startX;
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
		}

		// 2 set nodes connected by edges (starting with the start nodes)
		var gt_bv_rescueCount = 10000;
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
			
			gf_bv_drawNode(gt_bv_node.posx, gt_bv_node.posy, gt_bv_node.id, gt_bv_node.text, gt_bv_node.type, gt_bv_node.selected);
			
			gf_bv_addObjectPort(gt_bv_node.id);
		}
		
		// draw the edges
		for (var gt_bv_nodeId in gv_bv_graphs[gt_bv_subject].edges)
		{
			for (var gt_bv_edgeId in gv_bv_graphs[gt_bv_subject].edges[gt_bv_nodeId])
			{
				var gt_bv_edge = gv_bv_graphs[gt_bv_subject].edges[gt_bv_nodeId][gt_bv_edgeId];

				// perhaps add a space if edgesOut > 1
				gf_bv_drawArrow(gt_bv_edge.id, gt_bv_edge.start, gt_bv_edge.end, gt_bv_edge.text, gt_bv_edge.selected);
			}
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
 * initializes a new graph
 */
function gf_bv_init (gt_bv_canvasElem)
{		
	if (gf_elementExists(gt_bv_canvasElem))
	{
		// initialize canvas
		gv_bv_canvas	= document.getElementById(gt_bv_canvasElem);
		gv_bv_ctx		= gv_bv_canvas.getContext("2d");
		
		gv_bv_ctx.clearRect(0, 0, gv_bv_canvas.width, gv_bv_canvas.height);
		
		if (gv_bv_canvas.addEventListener)
		{
			gv_bv_canvas.addEventListener("click", gf_bv_onClick, false);
		}
		else if (document.attachEvent)
		{
			gv_bv_canvas.attachEvent("onclick", gf_bv_onClick);
		}
	}
}

/*
 * reads a click event
 */
function gf_bv_onClick (gt_bv_event)
{
	// retrieve the mouse's click position
	gf_getClickPosition(gt_bv_event, gv_bv_canvas, gv_bv_clickPos);

	// add canvas's offset to the click positon
	gv_bv_clickPos.x += document.getElementById(gv_elements.graphBVouter).scrollLeft;
	gv_bv_clickPos.y += document.getElementById(gv_elements.graphBVouter).scrollTop;
	
	// cycle through all nodes and edges to check which node / edge has been clicked
	for (var gt_bv_clickId in gv_bv_clicks)
	{
		var gt_bv_click = gv_bv_clicks[gt_bv_clickId];
		
		if (	gv_bv_clickPos.x >= gt_bv_click.l &&
				gv_bv_clickPos.x <= gt_bv_click.r &&
				gv_bv_clickPos.y >= gt_bv_click.t &&
				gv_bv_clickPos.y <= gt_bv_click.b)
		{
			// check if a node or edge has been clicked and call the corresponding function at the API
			if (gt_bv_click.type == "n")
			{
				gf_clickedBVnode(gt_bv_click.id);
			}
			else if (gt_bv_click.type == "e")
			{
				gf_clickedBVedge(gt_bv_click.id);
			}
			break;
		}
	}
}

/*
 * store the click-area of any node or edge
 */
function gf_bv_storeClick (gt_bv_id, gt_bv_type, gt_bv_l, gt_bv_t, gt_bv_r, gt_bv_b)
{
	gv_bv_clicks[gv_bv_clicks.length] = {
			id:		gt_bv_id,
			type:	gt_bv_type,
			l:		gt_bv_l,
			t:		gt_bv_t,
			r:		gt_bv_r,
			b:		gt_bv_b
	};
}

/*
 * stores an object
 */
function gf_bv_storeObject (gt_bv_id, gt_bv_x, gt_bv_y, gt_bv_width, gt_bv_height, gt_bv_stroke)
{		
		
	// x: x, y: y, w: width, h: height, l: left, r: right, t: top, b: bottom, s: stroke
	gt_bv_width		+= gt_bv_stroke;
	gt_bv_height	+= gt_bv_stroke;
	gt_bv_width		 = Math.round(gt_bv_width/2);
	gt_bv_height	 = Math.round(gt_bv_height/2);
	
	gt_bv_l		= gt_bv_x - gt_bv_width;
	gt_bv_t		= gt_bv_y - gt_bv_height;
	gt_bv_r		= gt_bv_x + gt_bv_width;
	gt_bv_b		= gt_bv_y + gt_bv_height;
	
	gv_bv_objects[gt_bv_id] = {
			id: gt_bv_id,
			 x: gt_bv_x,
			 y: gt_bv_y,
			 w: gt_bv_width,
			 h: gt_bv_height,
			 l: gt_bv_l,
			 r: gt_bv_r,
			 t: gt_bv_t,
			 b: gt_bv_b,
			 s: gt_bv_stroke
	};
	
	gf_bv_storeClick(gt_bv_id, "n", gt_bv_l, gt_bv_t, gt_bv_r, gt_bv_b);
}

/*
 * DRAWING FUNCTIONS
 */

/*
 * draws a node
 */
function gf_bv_drawNode (gt_bv_posx, gt_bv_posy, gt_bv_id, gt_bv_text, gt_bv_type, gt_bv_selected)
{
	
	if (!gf_isset(gt_bv_selected) || gt_bv_selected != true)
		gt_bv_selected = false;
	
	var gt_bv_object	= {width: 0, height: 0, stroke: 0};
	var gt_bv_style		= {};
	
	if (!gf_isset(gt_bv_type))
		gt_bv_type = "normal";

	if (!gf_isset(gt_bv_text))
		gt_bv_text = "";
	
	if (gt_bv_text == "")
		gt_bv_type = "end";
	
	if (gf_isset(gt_bv_posx, gt_bv_posy, gt_bv_id))
	{
		// if the node is an end, receive or send node -> draw a circle
		if (gt_bv_text == "S" || gt_bv_text == "R" || gt_bv_text == "")	// send, receive, end
		{
			if (gt_bv_type == "start")
			{
				gt_bv_style = gf_mergeStyles(gv_bv_circleNode.style, gv_bv_circleNode.styleStart);
			}
			else if (gt_bv_type == "end")
			{
				gt_bv_style = gf_mergeStyles(gv_bv_circleNode.style, gv_bv_circleNode.styleEnd);
				gt_bv_text = "";
			}
			else
			{
				gt_bv_style = gv_bv_circleNode.style;
			}
			
			gt_bv_object = gf_bv_drawCircle(gt_bv_posx, gt_bv_posy, gt_bv_text, gt_bv_style, gt_bv_selected);
		}
		
		// if the node is an internal action -> draw a rectangle
		else
		{
			if (gt_bv_type == "start")
			{
				gt_bv_style = gf_mergeStyles(gv_bv_rectNode.style, gv_bv_rectNode.styleStart);
			}
			else
			{
				gt_bv_style = gv_bv_rectNode.style;
			}
			
			gt_bv_object = gf_drawLabel(gv_bv_ctx, gt_bv_posx, gt_bv_posy, gt_bv_text, gt_bv_style, gt_bv_selected);
		}
		
		gf_bv_storeObject(gt_bv_id, gt_bv_posx, gt_bv_posy, gt_bv_object.width, gt_bv_object.height, gt_bv_object.stroke);
	}
}

/*
 * draws a circle (end / receive / send)
 */
function gf_bv_drawCircle (gt_bv_posx, gt_bv_posy, gt_bv_text, gt_bv_style, gt_bv_selected)
{
	if (!gf_isset(gt_bv_style))
		gt_bv_style = gv_defaultStyle;
	
	if (!gf_isset(gt_bv_selected) || gt_bv_selected != true)
		gt_bv_selected = false;
	
	if (gf_isset(gt_bv_posx, gt_bv_posy, gt_bv_text))
	{	
		// read relevant style information
		var gt_bv_font			= gf_getStyleValue(gt_bv_style, "font");
		var gt_bv_fontSize		= gf_getStyleValue(gt_bv_style, "fontSize");
		var gt_bv_paddingTop	= gf_getStyleValue(gt_bv_style, "paddingTop");
		var gt_bv_paddingBottom	= gf_getStyleValue(gt_bv_style, "paddingBottom");
				
		var gt_bv_bgColor		= gf_getStyleValue(gt_bv_style, "bgColor");
		var gt_bv_fgColor		= gf_getStyleValue(gt_bv_style, "fgColor");
		var gt_bv_borderColor	= gt_bv_selected ? gf_getStyleValue(gt_bv_style, "borderColorSelected") : gf_getStyleValue(gt_bv_style, "borderColor");
		var gt_bv_borderWidth	= gf_getStyleValue(gt_bv_style, "borderWidth");
		
		var gt_bv_height	= gt_bv_fontSize + gt_bv_paddingTop + gt_bv_paddingBottom;
		var gt_bv_width		= gt_bv_height;

		gv_bv_ctx.font			= gt_bv_fontSize + "px " + gt_bv_font;
		gv_bv_ctx.textBaseline	= "middle";
		gv_bv_ctx.textAlign		= "center";
		
		gv_bv_ctx.beginPath();
		gv_bv_ctx.arc(gt_bv_posx, gt_bv_posy, gt_bv_height / 2, 0, 2 * Math.PI, true);
		gv_bv_ctx.closePath();
		
		if (gt_bv_borderColor !== false && gt_bv_borderWidth > 0)
		{
			gv_bv_ctx.lineWidth		= gt_bv_borderWidth;
			gv_bv_ctx.strokeStyle	= gt_bv_borderColor;
			gv_bv_ctx.stroke();
		}
		
		if (gt_bv_bgColor !== false)
		{
			gv_bv_ctx.fillStyle	= gt_bv_bgColor;
			gv_bv_ctx.fill();
		}
		
		if (gt_bv_fgColor !== false && gt_bv_text != "")
		{
			gv_bv_ctx.fillStyle	= gt_bv_fgColor;
			gv_bv_ctx.fillText(gt_bv_text, gt_bv_posx, gt_bv_posy + 1);
		}

		return {width: gt_bv_width, height: gt_bv_height, stroke: gt_bv_borderWidth};
	}
	
	return {width: 0, height: 0, stroke: 0};
}

/*
 * draws an arrow between two objects
 */
function gf_bv_drawArrow (gt_bv_id, gt_bv_start, gt_bv_end, gt_bv_text, gt_bv_selected)
{
	if (!gf_isset(gv_bv_objects[gt_bv_start], gv_bv_objects[gt_bv_end], gt_bv_text))
		return false;
	
	if (!gf_isset(gt_bv_selected) || gt_bv_selected != true)
		gt_bv_selected = false;
	
	
	var gt_bv_objStart	= gv_bv_objects[gt_bv_start];
	var gt_bv_objEnd	= gv_bv_objects[gt_bv_end];
	
	var gt_bv_styleArrow	= gf_isset(gv_bv_arrow.styleArrow)	? gv_bv_arrow.styleArrow	: gv_defaultStyle;
	var gt_bv_styleText		= gf_isset(gv_bv_arrow.styleText)	? gv_bv_arrow.styleText		: gv_defaultStyle;
		
	var gt_bv_arrowWidth	= gf_getStyleValue(gt_bv_styleArrow, "borderWidth");
	var gt_bv_arrowColor	= gt_bv_selected ? gf_getStyleValue(gt_bv_styleArrow, "borderColorSelected") : gf_getStyleValue(gt_bv_styleArrow, "borderColor");
		
	if (gt_bv_text == "timeout")
		gt_bv_arrowWidth *= 2;
	
	var gt_bv_arrowSpace	= gv_arrowHead.length + 10;
		
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
		
	var gt_bv_labelCenter	= {x: 0, y: 0};

	var gt_bv_space1	= 0;
	var gt_bv_space2	= 0;
	var gt_bv_minLength	= 999999999;
	var gt_bv_shape		= "I";
	
	gt_bv_headCorrection = 0;
	
	// cycle through all port combinations at startObject and endObject to determine the shape of the arrow and the ports to use
	for (var gt_bv_out in gv_bv_ports)
	{
		for (var gt_bv_in in gv_bv_ports)
		{
			
			if (gf_bv_checkObjectPort(gt_bv_start, gt_bv_out, "o") && gf_bv_checkObjectPort(gt_bv_end, gt_bv_in, "i"))
			{
				gt_bv_o	= gt_bv_out;
				gt_bv_i	= gt_bv_in;
				
				gt_bv_startx	= gt_bv_o == "l" || gt_bv_o == "r" ? gt_bv_objStart[gt_bv_o]	: gt_bv_objStart.x;
				gt_bv_starty	= gt_bv_o == "t" || gt_bv_o == "b" ? gt_bv_objStart[gt_bv_o]	: gt_bv_objStart.y;
				gt_bv_endx		= gt_bv_i == "l" || gt_bv_i == "r" ? gt_bv_objEnd[gt_bv_i]		: gt_bv_objEnd.x;
				gt_bv_endy		= gt_bv_i == "t" || gt_bv_i == "b" ? gt_bv_objEnd[gt_bv_i]		: gt_bv_objEnd.y;
				
				// calculate the arrow shape that fits best for this port combination
				var gt_bv_arrowShape	= gf_bv_getArrowShape(gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_o, gt_bv_i);
				
				var gt_bv_setAsMin	= false;
				
				// check if the new arrow would fit better than the currently best
				if (!gt_bv_arrowShape.intersects)
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
	
	gt_bv_startx	= gt_bv_firstLine == "h"	? gt_bv_objStart[gt_bv_posStart]	: gt_bv_objStart.x;
	gt_bv_starty	= gt_bv_firstLine == "v"	? gt_bv_objStart[gt_bv_posStart]	: gt_bv_objStart.y;
	gt_bv_endx		= gt_bv_endLine == "h"		? gt_bv_objEnd[gt_bv_posEnd]		: gt_bv_objEnd.x;
	gt_bv_endy		= gt_bv_endLine == "v"		? gt_bv_objEnd[gt_bv_posEnd]		: gt_bv_objEnd.y;
	
	// reduce arrow length to give the arrow head enough space
	if (gt_bv_posEnd == "t")
		gt_bv_endy	-= 0.75 * gv_arrowHead.length;

	if (gt_bv_posEnd == "b")
		gt_bv_endy	+= 0.75 * gv_arrowHead.length;

	if (gt_bv_posEnd == "l")
		gt_bv_endx	-= 0.75 * gv_arrowHead.length;

	if (gt_bv_posEnd == "r")
		gt_bv_endx	+= 0.75 * gv_arrowHead.length;
	
	// change arrow start to correct width of arrow (only necessary in Firefox on Linux)
	if (	navigator.appCodeName == "Mozilla" && navigator.appVersion.substr(0,3) == "5.0" &&
			navigator.platform.substr(0,5) == "Linux" && navigator.userAgent.indexOf("Firefox") > 0)
	{
		if (gt_bv_posEnd == "t")
			gt_bv_endy	-= 0.5 * gt_bv_arrowWidth;
	
		if (gt_bv_posEnd == "b")
			gt_bv_endy	+= 0.5 * gt_bv_arrowWidth;
	
		if (gt_bv_posEnd == "l")
			gt_bv_endx	-= 0.5 * gt_bv_arrowWidth;
	
		if (gt_bv_posEnd == "r")
			gt_bv_endx	+= 0.5 * gt_bv_arrowWidth;
			
		if (gt_bv_posStart == "t")
			gt_bv_starty	-= 0.5 * gt_bv_arrowWidth;
	
		if (gt_bv_posStart == "b")
			gt_bv_starty	+= 0.5 * gt_bv_arrowWidth;
	
		if (gt_bv_posStart == "l")
			gt_bv_startx	-= 0.5 * gt_bv_arrowWidth;
	
		if (gt_bv_posStart == "r")
			gt_bv_startx	+= 0.5 * gt_bv_arrowWidth;
	}
	
	// draw the arrow
	if (gt_bv_shape == "I")
	{
		gt_bv_labelCenter	= gf_drawArrowI(gv_bv_ctx, "bv", gt_bv_id, gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_arrowColor, gt_bv_arrowWidth);
	}
	else if (gt_bv_shape == "L")
	{
		gt_bv_labelCenter	= gf_drawArrowL(gv_bv_ctx, "bv", gt_bv_id, gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_arrowColor, gt_bv_arrowWidth, gt_bv_firstLine);
	}
	else if (gt_bv_shape == "Z")
	{
		gt_bv_labelCenter	= gf_drawArrowZ(gv_bv_ctx, "bv", gt_bv_id, gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_arrowColor, gt_bv_arrowWidth, gt_bv_firstLine);
	}
	else if (gt_bv_shape == "U")
	{
		gt_bv_labelCenter	= gf_drawArrowU(gv_bv_ctx, "bv", gt_bv_id, gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_arrowColor, gt_bv_arrowWidth, gt_bv_firstLine, gt_bv_space1);
	}
	else if (gt_bv_shape == "G")
	{
		gt_bv_labelCenter	= gf_drawArrowG(gv_bv_ctx, "bv", gt_bv_id, gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_arrowColor, gt_bv_arrowWidth, gt_bv_firstLine, gt_bv_space1, gt_bv_space2);
	}
	else if (gt_bv_shape == "C")
	{
		gt_bv_labelCenter	= gf_drawArrowC(gv_bv_ctx, "bv", gt_bv_id, gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_arrowColor, gt_bv_arrowWidth, gt_bv_firstLine, gt_bv_space1, gt_bv_space2);
	}
	else if (gt_bv_shape == "S")
	{
		gt_bv_labelCenter	= gf_drawArrowS(gv_bv_ctx, "bv", gt_bv_id, gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_arrowColor, gt_bv_arrowWidth, gt_bv_firstLine, gt_bv_space1);
	}
	else if (gt_bv_shape == "UI")
	{
		gt_bv_labelCenter	= gf_drawArrowUI(gv_bv_ctx, "bv", gt_bv_id, gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_arrowColor, gt_bv_arrowWidth, gt_bv_firstLine, gt_bv_space1, gt_bv_space2);
	}
	else if (gt_bv_shape == "ZU")
	{
		gt_bv_labelCenter	= gf_drawArrowZU(gv_bv_ctx, "bv", gt_bv_id, gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_arrowColor, gt_bv_arrowWidth, gt_bv_firstLine, gt_bv_space1, gt_bv_space2);
	}
	
	// draw the arrow head
	gf_drawArrowHead(gv_bv_ctx, gt_bv_objEnd, gt_bv_posEnd, gt_bv_arrowColor, gt_bv_headCorrection);
	
	// draw the arrow's label
	if (gt_bv_text != "")
	{
		var gt_bv_clickLabel = gf_drawLabel(gv_bv_ctx, gt_bv_labelCenter.x, gt_bv_labelCenter.y, gt_bv_text, gt_bv_styleText);
		
		var gt_bv_clickL	= gt_bv_clickLabel.left - gt_bv_clickLabel.stroke/2;
		var gt_bv_clickT	= gt_bv_clickLabel.top - gt_bv_clickLabel.stroke/2;
		var gt_bv_clickR	= gt_bv_clickLabel.left + gt_bv_clickLabel.width + gt_bv_clickLabel.stroke/2;
		var gt_bv_clickB	= gt_bv_clickLabel.top + gt_bv_clickLabel.height + gt_bv_clickLabel.stroke/2;
		
		gf_bv_storeClick(gt_bv_id, "e", gt_bv_clickL, gt_bv_clickT, gt_bv_clickR, gt_bv_clickB);
		gf_bv_storeLineObject(gt_bv_clickL, gt_bv_clickT, gt_bv_clickR, gt_bv_clickB);
	}
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
	
	var gt_bv_diffXreal	= gt_bv_startx - gt_bv_endx;
	var gt_bv_diffYreal	= gt_bv_starty - gt_bv_endy;
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
			gt_bv_intersects	= gf_bv_intersectsZU(gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_firstLine, gt_bv_space1, gt_bv_space2);
		}
		else
		{
			gt_bv_shape			= "U";
			gt_bv_intersects	= gf_bv_intersectsU(gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_firstLine, gt_bv_space1);
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
				gt_bv_intersects	= gf_bv_intersectsI(gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy);
			}
			else
			{
				if (gt_bv_diffX <= gv_bv_nodeSettings.distanceX / 2 || gt_bv_diffY <= gv_bv_nodeSettings.distanceY / 2)
				{
					gt_bv_shape			= "S";
					gt_bv_length		+= 4 * gv_bv_nodeSettings.arrowSpace;
					gt_bv_space1		= gt_bv_startPos == "l" || gt_bv_startPos == "t" ? 0 - gv_bv_nodeSettings.arrowSpace : gv_bv_nodeSettings.arrowSpace;
					gt_bv_intersects	= gf_bv_intersectsS(gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_firstLine, gt_bv_space1);
				}
				else
				{
					gt_bv_shape			= "Z";
					gt_bv_intersects	= gf_bv_intersectsZ(gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_firstLine);
				}
			}
		}
		else
		{
			gt_bv_shape			= "C";
			gt_bv_length		+= 6 * gv_bv_nodeSettings.arrowSpace;
			gt_bv_space2		= gt_bv_startPos == "l" || gt_bv_startPos == "t" ? 0 - gv_bv_nodeSettings.arrowSpace : gv_bv_nodeSettings.arrowSpace;
			gt_bv_intersects	= gf_bv_intersectsC(gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_firstLine, gt_bv_space1, gt_bv_space2);
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
				gt_bv_intersects	= gf_bv_intersectsUI(gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_firstLine, gt_bv_space1, gt_bv_space2);
			}
			else
			{
				gt_bv_shape			= "L";
				gt_bv_intersects	= gf_bv_intersectsL(gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_firstLine);
			}
		}
		else
		{
			gt_bv_shape			= "G";
			gt_bv_length		+= 4 * gv_bv_nodeSettings.arrowSpace;
			gt_bv_space1		= gt_bv_startPos == "l" || gt_bv_startPos == "t" ? 0 - gv_bv_nodeSettings.arrowSpace : gv_bv_nodeSettings.arrowSpace;
			gt_bv_space2		= gt_bv_startPos == "l" || gt_bv_startPos == "t" ? 0 - gv_bv_nodeSettings.arrowSpace : gv_bv_nodeSettings.arrowSpace;
			gt_bv_intersects	= gf_bv_intersectsG(gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_firstLine, gt_bv_space1, gt_bv_space2);
		}
	}
	
	return {shape: gt_bv_shape, length: gt_bv_length, space1: gt_bv_space1, space2: gt_bv_space2, intersects: gt_bv_intersects};
}

/*
 * INTERSECTIONS
 */

/*
 * stores a line to be checked for intersections
 */
function gf_bv_storeLine (gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy)
{	
	gv_bv_lines[gv_bv_lines.length] = {	start:	{x: gt_bv_startx,	y: gt_bv_starty},
										end:	{x: gt_bv_endx,		y: gt_bv_endy}};
}

/*
 * stores an object to be checked for intersections
 */
function gf_bv_storeLineObject (gt_bv_l, gt_bv_t, gt_bv_r, gt_bv_b)
{
	gf_bv_storeLine(gt_bv_l, gt_bv_t, gt_bv_r, gt_bv_t);
	gf_bv_storeLine(gt_bv_r, gt_bv_t, gt_bv_r, gt_bv_b);
	gf_bv_storeLine(gt_bv_l, gt_bv_b, gt_bv_r, gt_bv_b);
	gf_bv_storeLine(gt_bv_l, gt_bv_t, gt_bv_l, gt_bv_b);
}

/*
 * checks intersections
 */
function gf_bv_intersects (gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy)
{
	var gt_bv_intersects	= false;
	var gt_bv_pointStart	= {x: gt_bv_startx,		y: gt_bv_starty};
	var gt_bv_pointEnd		= {x: gt_bv_endx,		y: gt_bv_endy};
	
	for (var gt_bv_id in gv_bv_lines)
	{
		gt_bv_intersects		= gf_bv_intersectLineSegments(gt_bv_pointStart, gt_bv_pointEnd, gv_bv_lines[gt_bv_id].start, gv_bv_lines[gt_bv_id].end);
		if (gt_bv_intersects)
		{
			return true;
		}
	}
	
	for (var gt_bv_id in gv_bv_objects)
	{
		var gt_bv_object = gv_bv_objects[gt_bv_id];
		
		gt_bv_intersects		= gt_bv_intersects || gf_bv_intersectLineSegments(gt_bv_pointStart, gt_bv_pointEnd, {x: gt_bv_object.l, y: gt_bv_object.t}, {x: gt_bv_object.r, y: gt_bv_object.t});
		gt_bv_intersects		= gt_bv_intersects || gf_bv_intersectLineSegments(gt_bv_pointStart, gt_bv_pointEnd, {x: gt_bv_object.r, y: gt_bv_object.t}, {x: gt_bv_object.r, y: gt_bv_object.b});
		gt_bv_intersects		= gt_bv_intersects || gf_bv_intersectLineSegments(gt_bv_pointStart, gt_bv_pointEnd, {x: gt_bv_object.l, y: gt_bv_object.b}, {x: gt_bv_object.r, y: gt_bv_object.b});
		gt_bv_intersects		= gt_bv_intersects || gf_bv_intersectLineSegments(gt_bv_pointStart, gt_bv_pointEnd, {x: gt_bv_object.r, y: gt_bv_object.b}, {x: gt_bv_object.l, y: gt_bv_object.b});
		
		if (gt_bv_intersects)
		{
			return true;
		}
	}
	
	return gt_bv_intersects;
}

/*
 * checks intersections for an I shaped arrow
 */
function gf_bv_intersectsI (gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy)
{
	return gf_bv_intersects(gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy);
}

/*
 * checks intersections for an L shaped arrow
 */
function gf_bv_intersectsL (gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_firstLine)
{
	var gt_bv_intersects	= false;
	
	var gt_bv_x2	= gt_bv_firstLine == "v" ? gt_bv_startx : gt_bv_endx;
	var gt_bv_y2	= gt_bv_firstLine == "h" ? gt_bv_starty : gt_bv_endy;
	
	gt_bv_intersects = gt_bv_intersects || gf_bv_intersectsI(gt_bv_startx, gt_bv_starty, gt_bv_x2, gt_bv_y2);
	gt_bv_intersects = gt_bv_intersects || gf_bv_intersectsI(gt_bv_x2, gt_bv_y2, gt_bv_endx, gt_bv_endy);
	
	return gt_bv_intersects;
}

/*
 * checks intersections for a Z shaped arrow
 */
function gf_bv_intersectsZ (gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_firstLine)
{
	var gt_bv_intersects	= false;
	
	var gt_bv_x2	= (gt_bv_startx + gt_bv_endx) / 2;
	var gt_bv_y2	= (gt_bv_starty + gt_bv_endy) / 2;
	
	gt_bv_intersects = gt_bv_intersects || gf_bv_intersectsL (gt_bv_startx, gt_bv_starty, gt_bv_x2, gt_bv_y2, gt_bv_firstLine);
	gt_bv_intersects = gt_bv_intersects || gf_bv_intersectsL (gt_bv_x2, gt_bv_y2, gt_bv_endx, gt_bv_endy, gt_bv_firstLine == "h" ? "v" : "h");

	return gt_bv_intersects;
}

/*
 * checks intersections for an U shaped arrow
 */
function gf_bv_intersectsU (gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_firstLine, gt_bv_space)
{
	var gt_bv_intersects	= false;
	
	var gt_bv_x1 = gt_bv_space < 0 ? Math.min(gt_bv_startx, gt_bv_endx) : Math.max(gt_bv_startx, gt_bv_endx);
	var gt_bv_y1 = gt_bv_space < 0 ? Math.min(gt_bv_starty, gt_bv_endy) : Math.max(gt_bv_starty, gt_bv_endy);
	
	var gt_bv_x2 = gt_bv_firstLine == "h" ? gt_bv_x1 + gt_bv_space : (gt_bv_startx + gt_bv_endx) / 2;
	var gt_bv_y2 = gt_bv_firstLine == "v" ? gt_bv_y1 + gt_bv_space : (gt_bv_starty + gt_bv_endy) / 2;
	
	gt_bv_intersects = gt_bv_intersects || gf_bv_intersectsL (gt_bv_startx, gt_bv_starty, gt_bv_x2, gt_bv_y2, gt_bv_firstLine);
	gt_bv_intersects = gt_bv_intersects || gf_bv_intersectsL (gt_bv_x2, gt_bv_y2, gt_bv_endx, gt_bv_endy, gt_bv_firstLine == "h" ? "v" : "h");

	return gt_bv_intersects;
}

/*
 * checks intersections for a G shaped arrow
 */
function gf_bv_intersectsG (gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_firstLine, gt_bv_space1, gt_bv_space2)
{	
	var gt_bv_intersects	= false;
		
	// firstLine: v => space1: u | d ; space2: l | r
	// firstLine: h => space1: l | r ; space2: u | d
	
	var gt_bv_x2 = 0;
	var gt_bv_y2 = 0;
	
	if (gt_bv_firstLine == "v")
	{
		if (gt_bv_startx < gt_bv_endx && gt_bv_starty < gt_bv_endy && gt_bv_space1 > 0 && gt_bv_space2 < 0) gt_bv_space2 *= -1;
		if (gt_bv_startx > gt_bv_endx && gt_bv_starty > gt_bv_endy && gt_bv_space1 < 0 && gt_bv_space2 > 0) gt_bv_space2 *= -1;
		if (gt_bv_startx > gt_bv_endx && gt_bv_starty < gt_bv_endy && gt_bv_space1 > 0 && gt_bv_space2 > 0) gt_bv_space2 *= -1;
		if (gt_bv_startx < gt_bv_endx && gt_bv_starty > gt_bv_endy && gt_bv_space1 < 0 && gt_bv_space2 < 0) gt_bv_space2 *= -1;
		
		gt_bv_x2 = gt_bv_space2 < 0 ? Math.min(gt_bv_startx, gt_bv_endx) + gt_bv_space2 : Math.max(gt_bv_startx, gt_bv_endx) + gt_bv_space2;
		gt_bv_y2 = gt_bv_space1 < 0 ? Math.min(gt_bv_starty, gt_bv_endy) + gt_bv_space1 : Math.max(gt_bv_starty, gt_bv_endy) + gt_bv_space1;
	}
	else
	{
		if (gt_bv_startx < gt_bv_endx && gt_bv_starty < gt_bv_endy && gt_bv_space1 > 0 && gt_bv_space2 < 0) gt_bv_space2 *= -1;
		if (gt_bv_startx > gt_bv_endx && gt_bv_starty > gt_bv_endy && gt_bv_space1 < 0 && gt_bv_space2 > 0) gt_bv_space2 *= -1;
		if (gt_bv_startx > gt_bv_endx && gt_bv_starty < gt_bv_endy && gt_bv_space1 < 0 && gt_bv_space2 < 0) gt_bv_space2 *= -1;
		if (gt_bv_startx < gt_bv_endx && gt_bv_starty > gt_bv_endy && gt_bv_space1 > 0 && gt_bv_space2 > 0) gt_bv_space2 *= -1;
		
		gt_bv_x2 = gt_bv_space1 < 0 ? Math.min(gt_bv_startx, gt_bv_endx) + gt_bv_space1 : Math.max(gt_bv_startx, gt_bv_endx) + gt_bv_space1;
		gt_bv_y2 = gt_bv_space2 < 0 ? Math.min(gt_bv_starty, gt_bv_endy) + gt_bv_space2 : Math.max(gt_bv_starty, gt_bv_endy) + gt_bv_space2;
	}
	
	gt_bv_intersects = gt_bv_intersects || gf_bv_intersectsL (gt_bv_startx, gt_bv_starty, gt_bv_x2, gt_bv_y2, gt_bv_firstLine);
	gt_bv_intersects = gt_bv_intersects || gf_bv_intersectsL (gt_bv_x2, gt_bv_y2, gt_bv_endx, gt_bv_endy, gt_bv_firstLine);

	return gt_bv_intersects;
}

/*
 * checks intersections for a C shaped arrow
 */
function gf_bv_intersectsC (gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_firstLine, gt_bv_space1, gt_bv_space2)
{
	var gt_bv_intersects	= false;
	
	var gt_bv_x2 = gt_bv_firstLine == "v" ? gt_bv_startx + gt_bv_space2 : (gt_bv_startx + gt_bv_endx) / 2;
	var gt_bv_y2 = gt_bv_firstLine == "h" ? gt_bv_starty + gt_bv_space2 : (gt_bv_starty + gt_bv_endy) / 2;
	
	gt_bv_space1	= gt_bv_firstLine == "v" && gt_bv_starty < gt_bv_endy ? 0 - gt_bv_space1 : gt_bv_space1;
	gt_bv_space1	= gt_bv_firstLine == "h" && gt_bv_startx < gt_bv_endx ? 0 - gt_bv_space1 : gt_bv_space1;
	
	gt_bv_intersects = gt_bv_intersects || gf_bv_intersectsU (gt_bv_startx, gt_bv_starty, gt_bv_x2, gt_bv_y2, gt_bv_firstLine, gt_bv_space1);
	gt_bv_intersects = gt_bv_intersects || gf_bv_intersectsU (gt_bv_x2, gt_bv_y2, gt_bv_endx, gt_bv_endy, gt_bv_firstLine, 0-gt_bv_space1);

	return gt_bv_intersects;
}

/*
 * checks intersections for an S shaped arrow
 */
function gf_bv_intersectsS (gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_firstLine, gt_bv_space)
{
	var gt_bv_intersects	= false;
	
	var gt_bv_x2 = (gt_bv_startx + gt_bv_endx) / 2;
	var gt_bv_y2 = (gt_bv_starty + gt_bv_endy) / 2;
	
	gt_bv_intersects = gt_bv_intersects || gf_bv_intersectsU (gt_bv_startx, gt_bv_starty, gt_bv_x2, gt_bv_y2, gt_bv_firstLine, gt_bv_space);
	gt_bv_intersects = gt_bv_intersects || gf_bv_intersectsU (gt_bv_x2, gt_bv_y2, gt_bv_endx, gt_bv_endy, gt_bv_firstLine, 0-gt_bv_space);

	return gt_bv_intersects;
}

/*
 * checks intersections for an UI shaped arrow
 */
function gf_bv_intersectsUI (gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_firstLine, gt_bv_space1, gt_bv_space2)
{
	var gt_bv_intersects	= false;
		
	var gt_bv_x2 = gt_bv_endx;
	var gt_bv_y2 = gt_bv_endy;
	
	var gt_bv_labelCenter = {x: 0, y: 0};
	
	if (gt_bv_firstLine == "v")
	{
		if (gt_bv_startx > gt_bv_endx)
			gt_bv_x2 += Math.abs(gt_bv_space2);
		else
			gt_bv_x2 -= Math.abs(gt_bv_space2);
	}
	else
	{
		if (gt_bv_starty > gt_bv_endy)
			gt_bv_y2 += Math.abs(gt_bv_space2);
		else
			gt_bv_y2 -= Math.abs(gt_bv_space2);	
	}

	gt_bv_intersects = gt_bv_intersects || gf_bv_intersectsU (gt_bv_startx, gt_bv_starty, gt_bv_x2, gt_bv_y2, gt_bv_firstLine, gt_bv_space1);
	gt_bv_intersects = gt_bv_intersects || gf_bv_intersectsI (gt_bv_x2, gt_bv_y2, gt_bv_endx, gt_bv_endy);

	return gt_bv_intersects;
}

/*
 * checks intersections for a ZU shaped arrow
 */
function gf_bv_intersectsZU (gt_bv_startx, gt_bv_starty, gt_bv_endx, gt_bv_endy, gt_bv_firstLine, gt_bv_space1, gt_bv_space2)
{
	var gt_bv_intersects	= false;
	
	var gt_bv_x2 = gt_bv_firstLine == "h" ? (gt_bv_startx + gt_bv_endx) / 2 + gt_bv_space1 : gt_bv_startx;
	var gt_bv_y2 = gt_bv_firstLine == "v" ? (gt_bv_starty + gt_bv_endy) / 2 + gt_bv_space1 : gt_bv_starty;

	var gt_bv_firstZ = false;
	
	if (gt_bv_firstLine == "v")
	{		
		gt_bv_x2 = gt_bv_space2 > 0 ? Math.max(gt_bv_startx, gt_bv_endx) + gt_bv_space2 : Math.min(gt_bv_startx, gt_bv_endx) + gt_bv_space2;
		
		gt_bv_firstZ = (gt_bv_starty < gt_bv_endy && gt_bv_space1 > 0) || (gt_bv_starty > gt_bv_endy && gt_bv_space1 < 0);
	}
	else
	{
		gt_bv_y2 = gt_bv_space2 > 0 ? Math.max(gt_bv_starty, gt_bv_endy) + gt_bv_space2 : Math.min(gt_bv_starty, gt_bv_endy) + gt_bv_space2;
		
		gt_bv_firstZ = (gt_bv_startx < gt_bv_endx && gt_bv_space1 > 0) || (gt_bv_startx > gt_bv_endx && gt_bv_space1 < 0);
	}
	
	if (gt_bv_firstZ)
	{
		gt_bv_intersects = gt_bv_intersects || gf_bv_intersectsZ (gt_bv_startx, gt_bv_starty, gt_bv_x2, gt_bv_y2, gt_bv_firstLine);
		gt_bv_intersects = gt_bv_intersects || gf_bv_intersectsU (gt_bv_x2, gt_bv_y2, gt_bv_endx, gt_bv_endy, gt_bv_firstLine, gt_bv_space1);	
	}
	else
	{
		gt_bv_intersects = gt_bv_intersects || gf_bv_intersectsU (gt_bv_startx, gt_bv_starty, gt_bv_x2, gt_bv_y2, gt_bv_firstLine, gt_bv_space1);
		gt_bv_intersects = gt_bv_intersects || gf_bv_intersectsZ (gt_bv_x2, gt_bv_y2, gt_bv_endx, gt_bv_endy, gt_bv_firstLine);	
	}

	return gt_bv_intersects;
}

/*
 * checks intersections between two line segments
 */
function gf_bv_intersectLineSegments (gt_bv_pointStart1, gt_bv_pointEnd1, gt_bv_pointStart2, gt_bv_pointEnd2)
{
	
	var gt_bv_allowSame	= true;
	
	var gt_bv_intersection		= gf_bv_intersectionStraightLines(gt_bv_pointStart1, gt_bv_pointEnd1, gt_bv_pointStart2, gt_bv_pointEnd2);
	var gt_bv_intersects		= !gt_bv_intersection.parallel && gt_bv_intersection.s < 1 && gt_bv_intersection.s > 0 && gt_bv_intersection.t < 1 && gt_bv_intersection.t > 0;
	var gt_bv_isSame			= !gt_bv_allowSame && gt_bv_intersection.same;
	
	return gt_bv_isSame || gt_bv_intersects;
}

/*
 * checks intersections between two straights (two straights intersect if they are not parallel)
 */
function gf_bv_intersectStraightLines (gt_bv_pointStart1, gt_bv_pointEnd1, gt_bv_pointStart2, gt_bv_pointEnd2)
{
	return !gf_bv_intersectionStraightLines(gt_bv_pointStart1, gt_bv_pointEnd1, gt_bv_pointStart2, gt_bv_pointEnd2).parallel;
}

/*
 * calculates intersections between two straights / "identity" for two line segments
 */
function gf_bv_intersectionStraightLines (gt_bv_pointStart1, gt_bv_pointEnd1, gt_bv_pointStart2, gt_bv_pointEnd2)
{

	var gt_bv_s		= 0;
	var gt_bv_t		= 0;
	var gt_bv_parallel	= true;		// parallel lines
	var gt_bv_same		= false;	// same line
	
	if (gf_objectHasAttribute([gt_bv_pointStart1, gt_bv_pointEnd1, gt_bv_pointStart2, gt_bv_pointEnd2], ["x", "y"]))
	{
		
		var gt_bv_points = {1: gt_bv_pointStart1, 2: gt_bv_pointEnd1, 3: gt_bv_pointStart2, 4: gt_bv_pointEnd2};
		
		var gt_bv_p = new Array();
		
		var gt_bv_div = 1;
		
		for (var gt_bv_i in gt_bv_points)
		{
			for (var gt_bv_j in gt_bv_points)
			{
				// store x_{gt_i} * y_{gt_j}
				gt_bv_p[gt_bv_i + "," + gt_bv_j] = gt_bv_points[gt_bv_i].x * gt_bv_points[gt_bv_j].y;
			}
		}
		
		gt_bv_div = gt_bv_p["2,4"] + gt_bv_p["1,3"] + gt_bv_p["4,1"] + gt_bv_p["3,2"] - gt_bv_p["1,4"] - gt_bv_p["2,3"] - gt_bv_p["4,2"] - gt_bv_p["3,1"];
		
		if (gt_bv_div != 0)
		{
			gt_bv_s = (gt_bv_p["4,1"] + gt_bv_p["3,4"] + gt_bv_p["1,3"] - gt_bv_p["4,3"] - gt_bv_p["1,4"] - gt_bv_p["3,1"]) / gt_bv_div;
			gt_bv_t = (gt_bv_p["2,3"] + gt_bv_p["1,2"] + gt_bv_p["3,1"] - gt_bv_p["2,1"] - gt_bv_p["1,3"] - gt_bv_p["3,2"]) / (gt_bv_div * -1);
			gt_bv_parallel = false;
		}
		else
		{
			// check if two parallel straights are the "same" (either start or end point of second straight is on straight 1)
			var gt_bv_divX		= gt_bv_pointEnd1.x - gt_bv_pointStart1.x;
			var gt_bv_divY		= gt_bv_pointEnd1.y - gt_bv_pointStart1.y;
			var gt_bv_diffX1	= gt_bv_pointStart2.x - gt_bv_pointStart1.x;
			var gt_bv_diffY1	= gt_bv_pointStart2.y - gt_bv_pointStart1.y;
			var gt_bv_diffX2	= gt_bv_pointEnd2.x - gt_bv_pointStart1.x;
			var gt_bv_diffY2	= gt_bv_pointEnd2.y - gt_bv_pointStart1.y;
			
			if (gt_bv_divX == 0 && gt_bv_diffX1 == 0)
			{
				if (gt_bv_divY >= 0 && gt_bv_diffY1 >= 0 || gt_bv_divY < 0 && gt_bv_diffY1 < 0)
				{
					gt_bv_same = gt_bv_same || Math.abs(gt_bv_diffY1) <= Math.abs(gt_bv_divY);
				}
				if (gt_bv_divY >= 0 && gt_bv_diffY2 >= 0 || gt_bv_divY < 0 && gt_bv_diffY2 < 0)
				{
					gt_bv_same = gt_bv_same || Math.abs(gt_bv_diffY2) <= Math.abs(gt_bv_divY);
				}
			}
			else if (gt_bv_divY == 0 && gt_bv_diffY1 == 0)
			{
				if (gt_bv_divX >= 0 && gt_bv_diffX1 >= 0 || gt_bv_divX < 0 && gt_bv_diffX1 < 0)
				{
					gt_bv_same = gt_bv_same || Math.abs(gt_bv_diffX1) <= Math.abs(gt_bv_divX);
				}
				if (gt_bv_divX >= 0 && gt_bv_diffX2 >= 0 || gt_bv_divX < 0 && gt_bv_diffX2 < 0)
				{
					gt_bv_same = gt_bv_same || Math.abs(gt_bv_diffX2) <= Math.abs(gt_bv_divX);
				}
			}
		}
	}
	
	return {s: gt_bv_s, t: gt_bv_t, parallel: gt_bv_parallel, same: gt_bv_same};
}