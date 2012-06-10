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

var gv_graphID	= "cv";
var gv_bgRect	= null;
var gv_currentViewBox	= {width: 0, height: 0, x: 0, y: 0, zoom: 1};
var gv_originalViewBox	= {width: 0, height: 0, x: 0, y: 0, zoom: 1};

var gv_mousePositionStart	= {x: 0, y: 0};

var gv_objects_edges = {};
var gv_objects_nodes = {};

var gv_node_parents		= {};	// relation: node => parent
var gv_node_children	= {};	// relation: node => [children]

function gf_initPaper ()
{
	gv_objects_edges = {};
	gv_objects_nodes = {};
	gv_paper.clear();
	
	gv_bgRect = gv_paper.rect(-gv_paperSizes[gv_graphID + "_width"] * 5, -gv_paperSizes[gv_graphID + "_height"] * 5, gv_paperSizes[gv_graphID + "_width"] * 11, gv_paperSizes[gv_graphID + "_height"] * 11).attr({"opacity": 0, "fill": "#FF0000"}).drag(gf_paperDragMove, gf_paperDragStart, gf_paperDragEnd);
	$(gv_bgRect.node).bind('mousewheel', function(event, delta)
	{
	
		if (event.shiftKey)
		{
			var gt_paperPos	= gf_paperMousePosition(event);
			var gt_speed	= 2;
		
			if (delta > 0)
				gf_paperZoomIn(gt_speed, gt_paperPos);
			else
				gf_paperZoomOut(gt_speed, gt_paperPos);
				
			event.preventDefault();
		}
    });
    
    $(gv_paper.canvas).mousemove(function(event)
    {
    	if (event.shiftKey)
		{
			gv_bgRect.toFront();
		}
		else
		{
			gv_bgRect.toBack();
		}
    });
}

function gf_paperMousePosition (event)
{
	var gt_graphOuter	= gv_elements["graph" + gv_graphID.toUpperCase() + "outer"];
	var gt_outerOffset	= $('#' + gt_graphOuter).offset();
	var gt_outerScroll	= {left: $('#' + gt_graphOuter).scrollLeft(), top: $('#' + gt_graphOuter).scrollTop()};
	var gt_windowOffsetX	= gf_isset(window.pageXOffset) ? window.pageXOffset : 0;
	var gt_windowOffsetY	= gf_isset(window.pageYOffset) ? window.pageYOffset : 0;
	var	gt_endPosX		= event.pageX ? event.pageX : event.clientX;
	var	gt_endPosY		= event.pageY ? event.pageY : event.clientY;
	
	var gt_origX		= gv_currentViewBox.x + gt_outerOffset.left + gt_outerScroll.left + gt_windowOffsetX + gv_currentViewBox.width/2;
	var gt_origY		= gv_currentViewBox.y + gt_outerOffset.top + gt_outerScroll.top + gt_windowOffsetY + gv_currentViewBox.height/2;
	
	return {x: gt_endPosX - gt_origX, y: gt_endPosY - gt_origY};
}

function gf_paperChangeView (view)
{
	if (view == "bv")
	{
		gv_graphID	= "bv";
		gv_paper	= gv_bv_paper;
	}
	else
	{
		gv_graphID	= "cv";
		gv_paper	= gv_cv_paper;	
	}
	
	gv_originalViewBox.width	= gv_paperSizes[gv_graphID + "_width"];
	gv_originalViewBox.height	= gv_paperSizes[gv_graphID + "_height"];
	gv_originalViewBox.x		= 0;
	gv_originalViewBox.y		= 0;
	
	gf_initPaper();
	gf_paperZoomReset();
}

function gf_paperZoomReset ()
{
	gv_currentViewBox.width		= gv_originalViewBox.width;
	gv_currentViewBox.height	= gv_originalViewBox.height;
	gv_currentViewBox.x			= gv_originalViewBox.x;
	gv_currentViewBox.y			= gv_originalViewBox.y;
	gv_currentViewBox.zoom		= gv_originalViewBox.zoom;
	
	gv_paper.setViewBox(gv_originalViewBox.x, gv_originalViewBox.y, gv_originalViewBox.width, gv_originalViewBox.height, false);
}

function gf_paperZoomIn (zoomFactor, zoomPosition)
{	
	if (!gf_isset(zoomFactor))
		zoomFactor = 2;
	
	gv_currentViewBox.width		= gv_currentViewBox.width/zoomFactor;
	gv_currentViewBox.height	= gv_currentViewBox.height/zoomFactor;
	
	/*
	if (gf_isset(zoomPosition))
	{
		gv_currentViewBox.x			= gv_currentViewBox.x - zoomPosition.x - (gv_currentViewBox.width/2);
		gv_currentViewBox.y			= gv_currentViewBox.y + 0*zoomPosition.y + (gv_currentViewBox.height/2);
	}
	else
	{
		*/
		gv_currentViewBox.x			= gv_graphID == "cv" ? gv_currentViewBox.x : gv_currentViewBox.x + (gv_currentViewBox.width/2);
		gv_currentViewBox.y			= gv_graphID == "cv" ? gv_currentViewBox.y + (gv_currentViewBox.height/2) : gv_currentViewBox.y;
		/*
	}
	*/
	
	gv_currentViewBox.zoom		= gv_currentViewBox.zoom * zoomFactor;
	gv_paper.setViewBox(gv_currentViewBox.x, gv_currentViewBox.y, gv_currentViewBox.width, gv_currentViewBox.height, false);	
}

function gf_paperZoomOut (zoomFactor, zoomPosition)
{
	if (!gf_isset(zoomFactor))
		zoomFactor = 2;
		
	/*
	if (gf_isset(zoomPosition))
	{
		gv_currentViewBox.x			= gv_currentViewBox.x - zoomPosition.x + (gv_currentViewBox.width/2);
		gv_currentViewBox.y			= gv_currentViewBox.y - zoomPosition.y - (gv_currentViewBox.height/2);
	}
	else
	{*/
		gv_currentViewBox.x			= gv_graphID == "cv" ? gv_currentViewBox.x : gv_currentViewBox.x - (gv_currentViewBox.width/2);
		gv_currentViewBox.y			= gv_graphID == "cv" ? gv_currentViewBox.y - (gv_currentViewBox.height/2) : gv_currentViewBox.y;
		/*
	}*/
	
	gv_currentViewBox.width		= gv_currentViewBox.width*zoomFactor;
	gv_currentViewBox.height	= gv_currentViewBox.height*zoomFactor;
	gv_currentViewBox.zoom		= gv_currentViewBox.zoom / zoomFactor;
	gv_paper.setViewBox(gv_currentViewBox.x, gv_currentViewBox.y, gv_currentViewBox.width, gv_currentViewBox.height, false);	
}

function gf_paperDragStart ()
{
	if (event.shiftKey)
	{
		// back up current mouse position
		gt_event = event ? event : window.event;
		gv_mousePositionStart.x = gt_event.pageX ? gt_event.pageX : gt_event.clientX;
		gv_mousePositionStart.y = gt_event.pageY ? gt_event.pageY : gt_event.clientY;
	}
}

function gf_paperDragMove ()
{
	if (event.shiftKey)
	{
		gt_event	= event ? event : window.event;
		gt_endPosX	= gt_event.pageX ? gt_event.pageX : gt_event.clientX;
		gt_endPosY	= gt_event.pageY ? gt_event.pageY : gt_event.clientY;
		
		gt_diffX	= gv_mousePositionStart.x - gt_endPosX;
		gt_diffY	= gv_mousePositionStart.y - gt_endPosY;
		
		gv_paper.setViewBox(gv_currentViewBox.x + gt_diffX, gv_currentViewBox.y + gt_diffY, gv_currentViewBox.width, gv_currentViewBox.height, false);
	}
}

function gf_paperDragEnd ()
{
	if (event.shiftKey)
	{
		gt_event	= event ? event : window.event;
		gt_endPosX	= gt_event.pageX ? gt_event.pageX : gt_event.clientX;
		gt_endPosY	= gt_event.pageY ? gt_event.pageY : gt_event.clientY;
		
		gt_diffX	= gv_mousePositionStart.x - gt_endPosX;
		gt_diffY	= gv_mousePositionStart.y - gt_endPosY;
		
		gv_currentViewBox.x	= gv_currentViewBox.x + gt_diffX;
		gv_currentViewBox.y	= gv_currentViewBox.y + gt_diffY;
	}
}

function gf_deselectEdges ()
{
	for (edgeId in gv_objects_edges)
	{
		gv_objects_edges[edgeId].deselect();
	}
}

function gf_deselectNodes ()
{
	for (nodeId in gv_objects_nodes)
	{
		gv_objects_nodes[nodeId].deselect();
	}
}

function gf_getStrokeDasharray (strokeStyle)
{
	if (gf_isset(strokeStyle))
	{
		strokeStyle = strokeStyle.toLowerCase();
		
		if (strokeStyle == "dotted")
			return ".";
			
		if (strokeStyle == "dashed")
			return "-";
			
		if (strokeStyle == "solid" || strokeStyle == "double")
			return " ";
			
		if (strokeStyle == "none")
			return "none";
			
		return strokeStyle;
	}
	return " ";
}

function gf_paperClickEdge (id)
{	
	if (gf_isset(id) && gf_isset(gv_objects_edges[id]))
	{
		gf_deselectEdges();
		gf_deselectNodes();
		gv_objects_edges[id].select();
		gf_clickedBVedge(id);
	}
}

function gf_paperClickNodeB (id)
{
	if (gf_isset(id) && gf_isset(gv_objects_nodes[id]))
	{
		gf_deselectEdges();
		gf_deselectNodes();
		gv_objects_nodes[id].select();
		gf_clickedBVnode(id);
	}
}

function gf_paperClickNodeC (id)
{
	if (gf_isset(id) && gf_isset(gv_objects_nodes[id]))
	{
		gf_deselectEdges();
		gf_deselectNodes();
		gv_objects_nodes[id].select();
		gf_clickedCVnode(id);
	}
}

function gf_paperDblClickNodeC (id)
{
	if (gf_isset(id) && gf_isset(gv_objects_nodes[id]))
	{
		gf_paperClickNodeC(id);
		showtab1();
	}
}

function gf_getParentNode (id)
{
	if (gf_isset(id))
	{
		if (gf_isset(gv_node_parents["n" + id]))
		{
			return gv_node_parents["n" + id];
		}
	}
	
	return null;
}

function gf_getChildNodes (id)
{
	if (gf_isset(id))
	{
		if (gf_isset(gv_node_children["n" + id]) && gf_isArray(gv_node_children["n" + id]))
		{
			return gv_node_children["n" + id];
		}
	}
	
	return [];
}

function gf_getNodeLeft ()
{
	var gt_selectedNode	= gv_graph.getSelectedNode();
	if (gt_selectedNode != null)
	{
		var gt_parent	= gf_getParentNode(gt_selectedNode);
		
		if (gt_parent != null)
		{
			var gt_children	= gf_getChildNodes(gt_parent);

			if (gt_children.length > 0)
			{
				// get own ID in array
				var gt_ownID	= 0;
				for (gt_i = 0; gt_i < gt_children.length; gt_i++)
				{
					if (gt_children[gt_i] == gt_selectedNode)
						gt_ownID = gt_i;
				}
				
				var gt_nextID	= (gt_ownID < 1) ? gt_children.length - 1 : gt_ownID - 1;
				gf_paperClickNodeB(gt_children[gt_nextID]);
			}		
		}
	}
}

function gf_getNodeRight ()
{
	var gt_selectedNode	= gv_graph.getSelectedNode();
	if (gt_selectedNode != null)
	{
		var gt_parent	= gf_getParentNode(gt_selectedNode);
		
		if (gt_parent != null)
		{
			var gt_children	= gf_getChildNodes(gt_parent);

			if (gt_children.length > 0)
			{	
				// get own ID in array
				var gt_ownID	= 0;
				for (gt_i = 0; gt_i < gt_children.length; gt_i++)
				{
					if (gt_children[gt_i] == gt_selectedNode)
						gt_ownID = gt_i;
				}
				
				var gt_nextID	= (gt_ownID >= gt_children.length - 1) ? 0 : gt_ownID + 1;
				gf_paperClickNodeB(gt_children[gt_nextID]);
			}		
		}
	}
}

function gf_getNodePrevious ()
{
	var gt_selectedNode	= gv_graph.getSelectedNode();
	if (gt_selectedNode != null)
	{
		var gt_parent	= gf_getParentNode(gt_selectedNode);
		
		if (gt_parent != null)
		{
			gf_paperClickNodeB(gt_parent);
		}
	}
}

function gf_getNodeNext ()
{
	var gt_selectedNode	= gv_graph.getSelectedNode();
	if (gt_selectedNode != null)
	{
		var gt_children	= gf_getChildNodes(gt_selectedNode);
		
		if (gt_children.length > 0)
		{
			gf_paperClickNodeB(gt_children[0]);
		}
	}
}

		/*
		 * status dependent styles
		 * 
		 * arrowColor
		 * arrowOpacity
		 * arrowStyle
		 * arrowWidth
		 * borderColor
		 * borderOpacity
		 * borderStyle
		 * borderWidth
		 * bacColor
		 * bgOpacity
		 * opacity
		 * fontColor
		 * fontOpacity
		 * fontWeight
		 */

function GFpath (startx, starty, endx, endy, shape, text, id)
{
	this.edgeCenter	= {x: 0, y: 0};
	this.path		= gv_paper.path("M0,0L0,0");
	this.pathStr	= "";
	
	this.shape		= "I";
	this.firstLine	= "v";
	this.space1		= 0;
	this.space2		= 0;
	
	this.id = "";
	
	this.label = new GFlabel(0, 0, text, "roundedrectangle", id, true);
	
	this.positionEnd = {x: 0, y: 0};
	this.positionStart = {x: 0, y: 0};
	
	/*
	 * common attributes
	 */
	this.deactive = false;
	this.selected = false;
	this.style = gv_defaultStyle;
	
	/*
	 * common functions
	 */
	this.click = function ()
	{
		id = this.id;
		this.path.click(function () {gf_paperClickEdge(id); });
		this.label.click("bv");
	}
	
	this.activate = function ()
	{
		this.deactive = false;
		this.label.activate();
		this.refreshStyle();
	}
	
	this.deactivate = function ()
	{
		this.deactive = true;
		this.label.deactivate();
		this.refreshStyle();
	}
		
	this.deselect = function ()
	{
		this.selected = false;
		this.label.deselect();
		this.refreshStyle();
	}
	
	this.select = function ()
	{
		this.selected = true;
		this.label.select();
		this.refreshStyle();
	}
	
	this.hide = function ()
	{
		this.path.hide();
		this.label.hide();
	}
	
	this.show = function ()
	{
		this.path.show();
		this.label.show();
	}
	
	this.readStyle = function (key, type)
	{
		return gf_getStyleValue(this.style, key, type)
	}
	
	this.refreshStyle = function ()
	{		
		/*
		 * status dependent styles
		 */
		var statusDependent = "";
		if (this.selected === true)
		{
			statusDependent = "Selected";
		}
		else if (this.deactive === true)
		{
			statusDependent = "Deactivated";
		}
		
		var strokeDasharray	= gf_getStrokeDasharray(this.readStyle("arrowStyle" + statusDependent, ""));
		var strokeWidth		= strokeDasharray == "none" ? 0 : this.readStyle("arrowWidth" + statusDependent, "int");
				
		this.path.attr("opacity", this.readStyle("opacity" + statusDependent, "float"));
		this.path.attr("stroke-dasharray", strokeDasharray);
		this.path.attr("stroke-opacity", this.readStyle("arrowOpacity" + statusDependent, "float"));
		this.path.attr("stroke-width", strokeWidth);
		this.path.attr("stroke", this.readStyle("arrowColor" + statusDependent, ""));
		
		this.path.attr("arrow-end", this.readStyle("arrowHeadType", "") + "-" + this.readStyle("arrowHeadWidth", "") + "-" + this.readStyle("arrowHeadLength", ""));
		
		this.path.attr("stroke-linecap", this.readStyle("arrowLinecap", ""));
		this.path.attr("stroke-linejoin", this.readStyle("arrowLinejoin", ""));
		// this.path.attr("stroke-miterlimit", this.readStyle("arrowMiterLimit", "int"));
	}
	
	this.setStyle = function (style)
	{
		this.style = gf_mergeStyles(gv_defaultStyle, style);
		this.label.setStyle(style);
		this.refreshStyle();
	}
	
	/*
	 * Path specific functions
	 */
	// I, L, U, Z, G, C, S, UI, ZU, SI
	this.calculateShape = function (x1, y1, x2, y2, shape, firstLine, space1, space2)
	{
		var cPath	= "";
		var cX		= 0;
		var cY		= 0;
		
		if (gf_isset(x1, x2, y1, y2, shape))
		{
			shape = shape.toUpperCase();
			
			firstLine	= gf_isset(firstLine) && firstLine.toLowerCase() == "h" ? "h" : "v";
			space1		= gf_isset(space1) ? space1 : 0;
			space2		= gf_isset(space2) ? space2 : 0;
			
			var diffX	= Math.round(x1 - x2);
			var diffY	= Math.round(y1 - y2);
			var absDiffX	= Math.abs(diffX);
			var absDiffY	= Math.abs(diffY);
			
			var rcX		= Math.round((x1 + x2) / 2);
			var rcY		= Math.round((y1 + y2) / 2);
			
			if (absDiffX > 0 || absDiffY > 0)
			{
				if (shape == "I" && absDiffX > 0 && absDiffY > 0)
				{
					shape = "L";
				}
								
				if (shape == "I")
				{
					cX		= rcX;
					cY		= rcY;
					cPath	= absDiffX > absDiffY ? "H" + x2 : "V" + y2;
				}
				else if (shape == "L")
				{
					cX		= firstLine == "v" ? x1 : x2;
					cY		= firstLine == "h" ? y1 : y2;
	
					// an L shaped arrow consists of two I shaped arrows
					var part1 = this.calculateShape(x1, y1, cX, cY, "I");
					var part2 = this.calculateShape(cX, cY, x2, y2, "I");
					
					cPath = part1.path + part2.path;
				}
				else if (shape == "Z")
				{
					cX		= rcX;
					cY		= rcY;
					
					var part1 = this.calculateShape(x1, y1, cX, cY, "L", firstLine);
					var part2 = this.calculateShape(cX, cY, x2, y2, "L", firstLine == "h" ? "v" : "h");
					
					cPath = part1.path + part2.path;
				}
				else if (shape == "U")
				{
					var tx	= space1 < 0 ? Math.min(x1, x2) : Math.max(x1, x2);
					var ty	= space1 < 0 ? Math.min(y1, y2) : Math.max(y1, y2);
					
					cX		= firstLine == "h" ? tx + space1 : rcX;
					cY		= firstLine == "v" ? ty + space1 : rcY;
	
					var part1 = this.calculateShape(x1, y1, cX, cY, "L", firstLine);
					var part2 = this.calculateShape(cX, cY, x2, y2, "L", firstLine == "h" ? "v" : "h");
					
					cPath = part1.path + part2.path;
				}
				else if (shape == "G")
				{
					// firstLine: v => space1: u | d ; space2: l | r
					// firstLine: h => space1: l | r ; space2: u | d
	
					if (firstLine == "v")
					{
						if (x1 < x2 && y1 < y2 && space1 > 0 && space2 < 0) space2 *= -1;
						if (x1 > x2 && y1 > y2 && space1 < 0 && space2 > 0) space2 *= -1;
						if (x1 > x2 && y1 < y2 && space1 > 0 && space2 > 0) space2 *= -1;
						if (x1 < x2 && y1 > y2 && space1 < 0 && space2 < 0) space2 *= -1;
						
						cX = space2 < 0 ? Math.min(x1, x2) + space2 : Math.max(x1, x2) + space2;
						cY = space1 < 0 ? Math.min(y1, y2) + space1 : Math.max(y1, y2) + space1;
					}
					else
					{
						if (x1 < x2 && y1 < y2 && space1 > 0 && space2 < 0) space2 *= -1;
						if (x1 > x2 && y1 > y2 && space1 < 0 && space2 > 0) space2 *= -1;
						if (x1 > x2 && y1 < y2 && space1 < 0 && space2 < 0) space2 *= -1;
						if (x1 < x2 && y1 > y2 && space1 > 0 && space2 > 0) space2 *= -1;
						
						cX = space1 < 0 ? Math.min(x1, x2) + space1 : Math.max(x1, x2) + space1;
						cY = space2 < 0 ? Math.min(y1, y2) + space2 : Math.max(y1, y2) + space2;
					}
	
					var part1 = this.calculateShape(x1, y1, cX, cY, "L", firstLine);
					var part2 = this.calculateShape(cX, cY, x2, y2, "L", firstLine);
					
					cPath = part1.path + part2.path;
				}
				else if (shape == "C")
				{
					
					// TODO: check
					
					cX = firstLine == "v" ? x1 + space2 : rcX;
					cY = firstLine == "h" ? y1 + space2 : rcY;
	
					space1	= firstLine == "v" && y1 < y2 ? 0 - space1 : space1;
					space1	= firstLine == "h" && x1 < x2 ? 0 - space1 : space1;
	
					var part1 = this.calculateShape(x1, y1, cX, cY, "U", firstLine, space1);
					var part2 = this.calculateShape(cX, cY, x2, y2, "U", firstLine, 0 - space1);
					
					cPath = part1.path + part2.path;
				}
				else if (shape == "S")
				{
					var cX = rcX;
					var cY = rcY;
					
					var part1 = this.calculateShape(x1, y1, cX, cY, "U", firstLine, space1);
					var part2 = this.calculateShape(cX, cY, x2, y2, "U", firstLine, 0 - space1);
					
					cPath = part1.path + part2.path;
				}
				else if (shape == "UI")
				{
					var tx = x2;
					var ty = y2;
					
					if (firstLine == "v")
					{
						if (x1 > x2)
							tx += Math.abs(space2);
						else
							tx -= Math.abs(space2);
					}
					else
					{
						if (y1 > y2)
							ty += Math.abs(space2);
						else
							ty -= Math.abs(space2);	
					}
					
					var part1 = this.calculateShape(x1, y1, tx, ty, "U", firstLine, space1);
					var part2 = this.calculateShape(tx, ty, x2, y2, "I");
					
					cX		= part1.x;
					cY		= part1.y;
					cPath	= part1.path + part2.path;
				}
				else if (shape == "ZU")
				{
					cX = firstLine == "h" ? rcX + space1 : x1;
					cY = firstLine == "v" ? rcY + space1 : y1;
				
					var zFirst = false;
					
					if (firstLine == "v")
					{		
						cX = space2 > 0 ? Math.max(x1, x2) + space2 : Math.min(x1, x2) + space2;
						
						zFirst = (y1 < y2 && space1 > 0) || (y1 > y2 && space1 < 0);
					}
					else
					{
						cY = space2 > 0 ? Math.max(y1, y2) + space2 : Math.min(y1, y2) + space2;
						
						zFirst = (x1 < x2 && space1 > 0) || (x1 > x2 && space1 < 0);
					}
					
					var part1 = null;
					var part2 = null;
					
					if (zFirst)
					{
						part1 = this.calculateShape(x1, y1, cX, cY, "Z", firstLine);
						part2 = this.calculateShape(cX, cY, x2, y2, "U", firstLine, space1);
					}
					else
					{
						part1 = this.calculateShape(x1, y1, cX, cY, "U", firstLine, space1);
						part2 = this.calculateShape(cX, cY, x2, y2, "Z", firstLine);
					}
					
					cPath = part1.path + part2.path;
				}
				else if (shape == "SI")
				{
					var tx = firstLine == "h" ? x2 : (x1 < x2 ? x2 + Math.abs(space1) : x2 - Math.abs(space1));
					var ty = firstLine == "v" ? y2 : (y1 < y2 ? y2 + Math.abs(space1) : y2 - Math.abs(space1));
					
					var part1 = this.calculateShape(x1, y1, tx, ty, "S", firstLine, space2);
					var part2 = this.calculateShape(tx, ty, x2, y2, "I");
					
					cX		= part1.x;
					cY		= part1.y;
					cPath	= part1.path + part2.path;
				}
				else
				{
					// unknown shape: draw "L" shape
					this.calculateShape(x1, y1, x2, y2, "L");
				}
			}
		}
		
		return {path: cPath, x: cX, y: cY};
	}
	
	this.checkIntersection = function (labelsOnly)
	{
		
		if (!gf_isset(labelsOnly) || labelsOnly !== true)
			labelsOnly = false;
			
		var thisLabelPath	= this.label.toPath();
		
		for (objId in gv_objects_edges)
		{
			if (objId != this.id)
			{
				var tObject	= gv_objects_edges[objId];
				
				var interPoints1	= labelsOnly ? [] : Raphael.pathIntersection(this.pathStr, tObject.pathStr);
				var interPoints2	= Raphael.pathIntersection(this.pathStr, tObject.label.toPath());
				var interPoints3	= []; // TODO: temp removed because of performance issues // Raphael.pathIntersection(thisLabelPath, tObject.pathStr);
				var interPoints4	= []; // TODO: temp removed because of performance issues // Raphael.pathIntersection(thisLabelPath, tObject.label.toPath());
				
				if (interPoints1.length > 0 || interPoints2.length > 0 || interPoints3.length > 0 || interPoints4.length > 0)
				{
					return true;					
				}
			}
		}
		
		for (objId in gv_objects_nodes)
		{
			var tObject	= gv_objects_nodes[objId];
			
			var interPoints1	= labelsOnly ? [] : Raphael.pathIntersection(this.pathStr, tObject.toPath());
			var interPoints2	= []; // TODO: temp removed because of performance issues // Raphael.pathIntersection(thisLabelPath, tObject.toPath());
			
			if (interPoints1.length > 0 || interPoints2.length > 0)
			{
				return true;
			}
		}
		
		return false;
	}
	
	this.getPositionEnd = function ()
	{
		return this.positionEnd;
	}
	
	this.getPositionStart = function ()
	{
		return this.positionStart;
	}
	
	this.setFirstLine = function (firstLine)
	{
		this.firstLine = firstLine;
	}
	
	this.setPositionEnd = function (x, y)
	{
		if (gf_isset(x, y))
		{
			this.positionEnd = {x: Math.round(x), y: Math.round(y)};
		}	
	}
	
	this.setPositionStart = function (x, y)
	{
		if (gf_isset(x, y))
		{
			this.positionStart = {x: Math.round(x), y: Math.round(y)};
		}
	}
	
	// I, L, U, Z, G, C, S, UI, ZU, SI
	this.setShape = function (shape)
	{
		this.shape = shape;
		this.updatePath();		
	}
	
	this.setSpace1 = function (space1)
	{
		this.space1 = space1;
	}
	
	this.setSpace2 = function (space2)
	{
		this.space2 = space2;
	}
	
	this.setText = function (text)
	{
		this.label.setText(text);
	}
	
	this.updatePath = function ()
	{
		var x1 = this.positionStart.x;
		var y1 = this.positionStart.y;
		var x2 = this.positionEnd.x;
		var y2 = this.positionEnd.y;
		
		var shape	= this.shape;
			
		var newPath	= this.calculateShape(x1, y1, x2, y2, shape, this.firstLine, this.space1, this.space2);
		
		this.pathStr	= "M" + x1 + "," + y1 + newPath.path;
		this.path.attr("path", this.pathStr);
		
		this.edgeCenter.x	= newPath.x;
		this.edgeCenter.y	= newPath.y;
		
		this.label.setPosition(newPath.x, newPath.y);
	}
	
	this.draw = function (path)
	{
		// if line-style == double: draw twice;
	}
	
	this.id = id;
	
	this.setPositionStart(startx, starty);
	this.setPositionEnd(endx, endy);
	this.setShape(shape);
	this.refreshStyle();
	this.setText(text);
	
	gv_objects_edges[id] = this;
}

function GFlabel (x, y, text, shape, id, belongsToPath)
{
	
	this.belongsToPath = false;
	
	this.x = 0;
	this.y = 0;	
	
	this.id = "";
	
	this.multiRR	= [];
	this.multiRR[3]	= gv_paper.rect(0, 0, 0, 0, 0);
	this.multiRR[2]	= gv_paper.rect(0, 0, 0, 0, 0);
	this.multiRR[1]	= gv_paper.rect(0, 0, 0, 0, 0);
	this.rectangle	= gv_paper.rect(0, 0, 0, 0, 0);
	this.ellipse	= gv_paper.ellipse(0, 0, 0, 0);
	this.text		= gv_paper.text(0, 0, "");
	
	this.bboxObj	= this.rectangle;
	
	this.shape		= "rectangle";
	
	/*
	 * common attributes
	 */
	this.deactive = false;
	this.selected = false;
	this.style = gv_defaultStyle;
	
	/*
	 * common functions
	 */
	this.click = function (graph)
	{
		if (gf_isset(graph))
		{
			graph = graph.toLowerCase();
			id = this.id;
			
			if (graph == "cv")
			{
				this.rectangle.click(function () {gf_paperClickNodeC(id); });
				this.ellipse.click(function () {gf_paperClickNodeC(id); });
				// this.text.click(function () {gf_paperClickNodeC(id); });
				
				this.rectangle.dblclick(function () {gf_paperDblClickNodeC(id); });
				this.ellipse.dblclick(function () {gf_paperDblClickNodeC(id); });
				// this.text.dblclick(function () {gf_paperDblClickNodeC(id); });
				
				for (rrId in this.multiRR)
	 			{
	 				this.multiRR[rrId].click(function () {gf_paperClickNodeC(id);});
	 				this.multiRR[rrId].dblclick(function () {gf_paperDblClickNodeC(id); });
	 			}
			}
			else if (graph == "bv")
			{
				if (this.belongsToPath)
				{
					this.rectangle.click(function () {gf_paperClickEdge(id); });
					this.ellipse.click(function () {gf_paperClickEdge(id); });
					// this.text.click(function () {gf_paperClickEdge(id); });
				}
				else
				{
					this.rectangle.click(function () {gf_paperClickNodeB(id); });
					this.ellipse.click(function () {gf_paperClickNodeB(id); });
					// this.text.click(function () {gf_paperClickNodeB(id); });
				}
			}
			$(this.text.node).css("pointer-events", "none");
		}
	}
	
	this.activate = function ()
	{
		this.deactive = false;
		this.refreshStyle();
	}
	
	this.deactivate = function ()
	{
		this.deactive = true;
		this.refreshStyle();
	}
		
	this.deselect = function ()
	{
		this.selected = false;
		this.refreshStyle();
	}
	
	this.select = function ()
	{
		this.selected = true;
		this.refreshStyle();
	}
	
	this.hide = function ()
	{
		this.hideObjects();
		this.text.hide();
	}
	
	this.hideObjects = function ()
	{
		for (rrId in this.multiRR)
 		{
			this.multiRR[rrId].hide();
		}
		this.rectangle.hide();
		this.ellipse.hide();
	}
	
	this.show = function ()
	{
		this.setShape(this.shape);
		this.text.show();
	}
	
	this.readStyle = function (key, type)
	{
		return gf_getStyleValue(this.style, key, type)
	}
	
	this.refreshStyle = function ()
	{
		
		/*
		 * status dependent styles
		 */
		var statusDependent = "";
		if (this.selected === true)
		{
			statusDependent = "Selected";	
		}
		else if (this.deactive === true)
		{
			statusDependent = "Deactivated";
		}
		
		var strokeDasharray	= gf_getStrokeDasharray(this.readStyle("borderStyle" + statusDependent, ""));
		var strokeWidth		= strokeDasharray == "none" ? 0 : this.readStyle("borderWidth" + statusDependent, "int");
		
		// rectangle
		this.rectangle.attr("opacity", this.readStyle("opacity" + statusDependent, "float"));
		this.rectangle.attr("stroke-opacity", this.readStyle("borderOpacity" + statusDependent, "float"));
		this.rectangle.attr("stroke-width", strokeWidth);
		this.rectangle.attr("stroke-dasharray", strokeDasharray);
		this.rectangle.attr("fill-opacity", this.readStyle("bgOpacity" + statusDependent, "float"));
		this.rectangle.attr("stroke", this.readStyle("borderColor" + statusDependent, ""));
		this.rectangle.attr("fill", this.readStyle("bgColor" + statusDependent, ""))
		
		// rr1-3
		for (rrId in this.multiRR)
	 	{
			this.multiRR[rrId].attr("opacity", this.readStyle("opacity" + statusDependent, "float"));
			this.multiRR[rrId].attr("stroke-opacity", this.readStyle("borderOpacity" + statusDependent, "float"));
			this.multiRR[rrId].attr("stroke-width", strokeWidth);
			this.multiRR[rrId].attr("stroke-dasharray", strokeDasharray);
			this.multiRR[rrId].attr("fill-opacity", this.readStyle("bgOpacity" + statusDependent, "float"));
			this.multiRR[rrId].attr("stroke", this.readStyle("borderColor" + statusDependent, ""));
			this.multiRR[rrId].attr("fill", this.readStyle("bgColor" + statusDependent, ""))
		}
		
		// ellipse
		this.ellipse.attr("opacity", this.readStyle("opacity" + statusDependent, "float"));
		this.ellipse.attr("stroke-opacity", this.readStyle("borderOpacity" + statusDependent, "float"));
		this.ellipse.attr("stroke-width", strokeWidth);
		this.ellipse.attr("stroke-dasharray", strokeDasharray);
		this.ellipse.attr("fill-opacity", this.readStyle("bgOpacity" + statusDependent, "float"));
		this.ellipse.attr("stroke", this.readStyle("borderColor" + statusDependent, ""));
		this.ellipse.attr("fill", this.readStyle("bgColor" + statusDependent, ""))
		
		// text
		this.text.attr("opacity", this.readStyle("opacity" + statusDependent, "float"));
		this.text.attr("fill-opacity", this.readStyle("fontOpacity" + statusDependent, "float"));
		this.text.attr("fill", this.readStyle("fontColor" + statusDependent, ""));
		this.text.attr("font-weight", this.readStyle("fontWeight" + statusDependent, ""));
		this.text.attr("font-size", this.readStyle("fontSize", "int"));
		this.text.attr("font-family", this.readStyle("fontFamily", ""));
		
		// textAlign: "left",	// TODO / remove?
		// textVAlign: "top",	// TODO / remove?
		
		this.updateBoundaries();
	}
	
	this.setStyle = function (style)
	{
		this.style = gf_mergeStyles(gv_defaultStyle, style);
		this.refreshStyle();
	}
	
	this.getBoundaries = function ()
	{
		var bbox = {x: 0, y: 0, top: 0, bottom: 0, left: 0, right: 0, width: 0, height: 0};
		
		if (this.shape == "roundedrectanglemulti")
		{
			var bbox1	= this.rectangle.getBBox();
			var bbox2	= this.multiRR[3].getBBox();
			
			bbox.top	= Math.round(bbox2.y);
			bbox.bottom	= Math.round(bbox1.y2);
			bbox.left	= Math.round(bbox1.x);
			bbox.right	= Math.round(bbox2.x2);
			bbox.width	= Math.round(bbox.right) - Math.round(bbox.left);
			bbox.height	= Math.round(bbox.bottom) - Math.round(bbox.top);
			bbox.x		= Math.round(bbox1.x) + Math.round(bbox1.width / 2);
			bbox.y		= Math.round(bbox1.y) + Math.round(bbox1.height / 2);
		}
		else
		{
			var bbox1	= this.bboxObj.getBBox();
			
			bbox.top	= Math.round(bbox1.y);
			bbox.bottom	= Math.round(bbox1.y2);
			bbox.left	= Math.round(bbox1.x);
			bbox.right	= Math.round(bbox1.x2);
			bbox.width	= Math.round(bbox1.width);
			bbox.height	= Math.round(bbox1.height);
			bbox.x		= Math.round(bbox1.x) + Math.round(bbox1.width / 2);
			bbox.y		= Math.round(bbox1.y) + Math.round(bbox1.height / 2);
		}
		
		return bbox;
	}
	
	this.setBoundaries = function ()
	{
		// TODO
	}
	
	this.getPosition = function ()
	{
		return {x: this.x, y: this.y};
	}
	
	this.setPosition = function (x, y)
	{
		if (gf_isset(x, y))
		{
			this.x = x;
			this.y = y;
			this.updateBoundaries();
		}
	}
	
	/*
	 * label specific
	 */	
	this.replaceNewline = function (text)
	{
		return gf_replaceNewline(text).replace(/<li>|<li \/>|<li\/>/gi, this.readStyle("liSymbol", ""));
	}
	 
	// shape: circle | rect || roundedRectangle
	this.setShape = function (shape)
	{
		
		if (gf_isset(shape))
		{
			shape = shape.toLowerCase();
			
			this.hideObjects();
			
			if (!(this.text.attr("text") == "" && this.belongsToPath === true))
			{
				if (shape == "circle" || shape == "ellipse")
				{
					this.shape		= shape;
					this.bboxObj	= this.ellipse;
					this.ellipse.show();
				}
				else if (shape == "rectangle" || shape == "roundedrectangle")
				{
					this.shape		= shape;
					this.bboxObj	= this.rectangle;
					this.rectangle.show();
				}
				else if (shape == "roundedrectanglemulti")
				{
					this.shape		= shape;
					
					for (rrId in this.multiRR)
		 			{
						this.multiRR[rrId].show();
					}
					this.rectangle.show();
				}
				
				this.refreshStyle();
			}
		}
		
	}
	
	this.setText = function (text)
	{
		this.text.attr("text", this.replaceNewline(text));
		this.refreshStyle();
	}
	
	this.toPath = function ()
	{
		var gt_bbox = this.getBoundaries();
		
		return "M" + gt_bbox.left + "," + gt_bbox.top + "H" + gt_bbox.right + "V" + gt_bbox.bottom + "H" + gt_bbox.left + "Z";
	}
	
	this.updateBoundaries = function ()
	{
		
		// TODO: some more options like apply padding and move the text according to the new position
		
		this.text.attr("x", this.x);
		this.text.attr("y", this.y);
		
		var paddingLeft		= this.readStyle("paddingLeft", "int");
		var paddingRight	= this.readStyle("paddingRight", "int");
		var paddingTop		= this.readStyle("paddingTop", "int");
		var paddingBottom	= this.readStyle("paddingBottom", "int");
		var styleWidth		= this.readStyle("width", "int");
		var styleHeight		= this.readStyle("height", "int");		
		
		
		// apply the width and height information
		var width	= Math.round(this.text.getBBox().width);
		var height	= Math.round(this.text.getBBox().height);
		var width2	= styleWidth > 0 ? styleWidth : Math.max(width + paddingLeft + paddingRight, this.readStyle("minWidth", "int"));
		var height2	= styleHeight > 0 ? styleHeight : Math.max(height + paddingTop + paddingBottom, this.readStyle("minHeight", "int"));
		var left	= this.text.getBBox().x;
		var top		= this.text.getBBox().y;
		var radiusx	= Math.round(width2 / 2);
		var radiusy	= Math.round(height2 / 2);
		var radius	= Math.max(radiusx, radiusy);
		var rectR	= this.shape == "roundedrectangle" || this.shape == "roundedrectanglemulti" ? this.readStyle("rectangleRadius", "int") : 0;
		
		if (this.shape == "circle")
		{
			this.ellipse.attr("cx", this.x);
			this.ellipse.attr("cy", this.y);
			this.ellipse.attr("rx", radius);
			this.ellipse.attr("ry", radius);
		}
		else if (this.shape == "ellipse")
		{
			this.ellipse.attr("cx", this.x);
			this.ellipse.attr("cy", this.y);
			this.ellipse.attr("rx", radiusx);
			this.ellipse.attr("ry", radiusy);
		}
		else if (this.shape == "roundedrectangle" || this.shape == "rectangle" || this.shape == "roundedrectanglemulti")
		{
			this.rectangle.attr("x", this.x - radiusx);
	 		this.rectangle.attr("y", this.y - radiusy);
	 		this.rectangle.attr("width", width2);
	 		this.rectangle.attr("height", height2);
	 		this.rectangle.attr("r", rectR);
		}
		
		if (this.shape == "roundedrectanglemulti")
		{
			var rrOverlap = 5;
			 		
	 		for (rrId in this.multiRR)
	 		{
				this.multiRR[rrId].attr("x", this.x - radiusx + rrId * rrOverlap);
		 		this.multiRR[rrId].attr("y", this.y - radiusy - rrId * rrOverlap);
		 		this.multiRR[rrId].attr("width", width2);
		 		this.multiRR[rrId].attr("height", height2);
		 		this.multiRR[rrId].attr("r", rectR);
	 		}			
		}
	}
	
	this.x = x;
	this.y = y;
	
	this.id = id;
	
	if (gf_isset(belongsToPath) && belongsToPath === true)
	{
		this.belongsToPath = true;
	}
	
	this.setText(text);
	this.setShape(shape);
	
	if (!this.belongsToPath)
		gv_objects_nodes[id] = this;
}

/*
 * function to check if a variable / array-index is set
 */
function gf_isset ()
{
    // http://kevin.vanzonneveld.net
    // +   original by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
    // +   improved by: FremyCompany
    // +   improved by: Onno Marsman
    // +   improved by: Rafał Kukawski
    var gt_a = arguments,
    	gt_l = gt_a.length,
    	gt_i = 0,
    	gt_undef;

    if (gt_l === 0)
    {
        throw new Error('Empty isset');
    }

    while (gt_i !== gt_l)
    {
        if (gt_a[gt_i] === gt_undef || gt_a[gt_i] === null)
        {
            return false;
        }
        gt_i++;
    }
    return true;
}

/*
 * function to count elements of an array / object (same as array.length but also works for non-numerical indexes)
 */
function gf_count (gt_array)
{
	var gt_count = 0;
	
	if (gf_isArray(gt_array))
	{
		for (var gt_key in gt_array)
		{
			gt_count++;
		}
	}
	
	return gt_count;
}

/*
 * replaces newline characters with \n
 */
function gf_replaceNewline (gt_text)
{
	return gt_text.replace(/<br>|<br \/>|<br\/>|\r\n|\r|\n/gi, "\n");
}

/*
 * checks if an object is an array
 */
function gf_isArray (gt_object)
{
    return Object.prototype.toString.call(gt_object) === '[object Array]';
}

/*
 * checks if an HTML element with the given ID exists
 */
function gf_elementExists ()
{
	var gt_argv = arguments;
	var gt_argc = gt_argv.length;

	for (var gt_i = 0; gt_i < gt_argc; gt_i++)
	{
		if (document.getElementById(gt_argv[gt_i]) === null)
		{
			return false
		}
	}
	
	return true;
}

/*
 * merges style arrays (like PHP's array_merge)
 */
function gf_mergeStyles ()
{
	var gt_args		= arguments;
	var gt_count	= gt_args.length;
	var gt_style	= {};
	
	if (gt_count > 1)
	{
		for (var gt_i in gt_args)
		{
			for (var gt_s in gt_args[gt_i])
			{
				gt_style[gt_s] = gt_args[gt_i][gt_s];
			}
		}
	}
	
	return gt_style;
}

/*
 * reads a value from a given style array
 */
function gf_getStyleValue (gt_style, gt_key, gt_type)
{
	if (!gf_isset(gt_type))
		gt_type = "";
	
	if (gf_isset(gt_style, gt_key))
	{
		return gf_isset(gt_style[gt_key]) ? gt_style[gt_key] : gv_defaultStyle[gt_key];		
	}
	
	if (gt_type == "bool")
		return false;
	
	if (gt_type == "int")
		return 0;
	
	if (gt_type == "float")
		return 0;
	
	return "";
}

/*
 * checks whether a given object / array of objects has a certain attribute
 */
function gf_objectHasAttribute (gt_object, gt_attribute)
{
	if (!gf_isset(gt_attribute, gt_object))
		return false;
	
	if (!gf_isArray(gt_attribute))
		gt_attribute = [gt_attribute];
	
	if (!gf_isArray(gt_object))
		gt_object = [gt_object];
	
	for (var gt_o in gt_object)
	{
		if (!gf_isset(gt_object[gt_o]))
			return false;
		
		for (var gt_i in gt_attribute)
		{
			if (!gf_isset(gt_object[gt_o][gt_attribute[gt_i]]))
				return false;
		}
	}
	
	return true;
}