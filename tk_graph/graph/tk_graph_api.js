/**
 * S-BPM Groupware v0.9
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
 * GCcommunication instance.
 * This is the central access point to the API as all graph modifications are done using the methods of GCcommunication.
 * 
 * @type GCCommunication
 */
var gv_graph = new GCcommunication();

/**
 * GCgraphbv instance.
 * This class is responsible for drawing the graph for the internal behavior.
 * 
 * @type GCgraphbv
 */
var gv_graph_bv	= new GCgraphbv();

/**
 * GCgraphcv instance.
 * This class is responsible for drawing the graph for the subject interaction view.
 * 
 * @type GCgraphcv
 */
var gv_graph_cv	= new GCgraphcv();

/**
 * This object contains a list of all DOM elements used within the API.
 * Do not change the indexes of the array as they are referred to in the API.
 * But you should adapt the values to the actual IDs of the elements on the page.
 * 
 * @type Object
 */
var gv_elements = {
	graphBVouter:		"graph_bv_outer",
	graphCVouter:		"graph_cv_outer",
	inputEdgeText:		"ge_edge_text",
	inputEdgeTarget:	"ge_edge_target",
	inputEdgeMessage:	"ge_edge_message",
	inputEdgeMessageO:	"ge_edge_message_outer",
	inputEdgeOuter:		"edge",
	inputSubjectText:	"ge_cv_text",
	inputSubjectId:		"ge_cv_id",
	inputNodeText:		"ge_text",
	inputNodeType2:		"ge_type2",
	inputNodeOuter:		"node",
	
	// select elements
	inputNodeTypeStart:		"ge_type_start",
		
	inputNodeType2R:		"ge_type2_R",
	inputNodeType2S:		"ge_type2_S",
	inputNodeType2End:		"ge_type2_end",
	inputNodeType2Action:	"ge_type2_action",
	
	// subject types
	inputSubjectTypeSingle:		"ge_cv_type_single",
	inputSubjectTypeMulti:		"ge_cv_type_multi",
	inputSubjectTypeExternal:	"ge_cv_type_external"
};

/**
 * The tk_graph library provides methods to create macros for adding new nodes to the graph of the behavioral view.
 * These macros are stored in the gv_macros object.
 * 
 * @type Object
 */
var gv_macros = {};

/**
 * This variable contains a reference to either the gv_bv_paper or the gv_cv_paper variable.
 * It is used to create new SVG elements for a graph.
 * 
 * @type Paper (from RaphaelJS)
 */
var gv_paper = null;

/**
 * This variable contains a canvas created by RaphaelJS.
 * All elements created for the graph of the behavioral view will be displayed here.
 * The canvas is created within the div with the id graph_bv_outer.
 * 
 * @type Paper (from RaphaelJS)
 */
var gv_bv_paper = null;

/**
 * This variable contains a canvas created by RaphaelJS.
 * All elements created for the graph of the subject interaction view will be displayed here.
 * The canvas is created within the div with the id graph_cv_outer.
 * 
 * @type Paper (from RaphaelJS)
 */
var gv_cv_paper = null;

/**
 * Using this method you can insert a node to the graph with the settings stored in gv_macros.
 * 
 * @param {String} id The id of the macro.
 * @returns {void}
 */
function gf_callMacro (id)
{
	if (gf_isset(gv_macros[id]))
	{
		var gt_macro = gv_macros[id];
		
		if (gt_macro.connect)
			gv_graph.connectNodes();
			
		var gt_nodeId = gv_graph.createNode();
		var gt_behavior = gv_graph.getBehavior(gv_graph.selectedSubject);
		
		
		if (gt_behavior != null)
		{
			gt_behavior.selectedNode = gt_nodeId;
			gt_behavior.updateNode(gt_macro.text, gt_macro.type, gt_macro.type2);
		}
	}
}

/**
 * This method is called internally to select an edge within the behavioral view.
 * It is linked to the onClick event of the edge elements of the graph.
 * 
 * @see GCcommunication.selectEdge(id)
 * @param {int} edgeId The id of the edge.
 * @returns {void}
 */
function gf_clickedBVedge (edgeId)
{
	gv_graph.selectEdge(edgeId);
}

/**
 * By calling this method a node within the behavioral view is selected.
 * It is linked to the onClick event of the node elements of the graph.
 * 
 * @see GCcommunication.selectNode(id)
 * @param {int} nodeId The id of the node.
 * @returns {void}
 */
function gf_clickedBVnode (nodeId)
{
	gv_graph.selectNode(nodeId);
}

/**
 * This method is called onDblClick on a subject element on the graph of the subject interaction view.
 * It loads and displays the behavioral view of the clicked subject.
 * When graphId is either not set or null, the internal behavior of the currently clicked subject is loaded.
 * 
 * @see GCcommunication.drawBehavior(id)
 * @param {String} [graphId] The ID of the subject whose internal behavior should be loaded.
 * @returns {void}
 */
function gf_clickedCVbehavior (graphId)
{
	gv_graph.drawBehavior(graphId);
	
	if (gf_isset(graphId))
	{
		showtab1();
	}
}

/**
 * The gf_clickedCVnode method is used to select a subject in the communication view.
 * It is linked to the onClick event of the subject elements of the graph.
 * 
 * @see GCcommunication.selectNode(id)
 * @param {String} nodeId The ID of the subject.
 * @returns {void}
 */
function gf_clickedCVnode (nodeId)
{
	gv_graph.selectNode(nodeId);
}

/**
 * This method adds a new macro to the gv_macro array.
 * You can execute the macro by calling gf_callMacro(id).
 * 
 * @param {String} id The id of the macro.
 * @param {String} text The label of the node to be inserted.
 * @param {String} type Possible values: "start", "end", "normal"; This will result in either a start node, an end node or a node without any specific marker.
 * @param {String} type2 The type of the node. Possible values are "send", "receive", "end" or "action" (default: "action")
 * @param {boolean} connect When set to true, the inserted node will automatically be connected to the selected node (if one).
 * @returns {void}
 */
function gf_createMacro (id, text, type, type2, connect)
{
	if (gf_isset(id, type, text, connect) && !gf_isset(this.gv_macros[id]))
	{
		connect = connect === true ? true : false;
		gv_macros[id] = {id: id, type: type, type2: type2, text: text, connect: connect};
	}
}

/**
 * This method de- / activates the currently selected edge.
 * 
 * @see GCcommunication.deactivateEdge()
 * @returns {void}
 */
function gf_deactivateEdge ()
{
	gv_graph.deactivateEdge();	
}

/**
 * This method de- / activates the currently selected node.
 * 
 * @see GCcommunication.deactivateNode()
 * @returns {void}
 */
function gf_deactivateNode ()
{
	gv_graph.deactivateNode();
}

/**
 * This method is called onChange of gv_elements.inputEdgeTarget.
 * It loads and displays all messages that can be received or send from the selected subject.
 * 
 * @see GCcommunication.loadEdgeMessages()
 * @returns {void}
 */
function gf_edgeMessage ()
{
	gv_graph.loadEdgeMessages();
}

/**
 * Select the left sibbling of the currently selected node.
 * When the current node is the most left sibbling the most right sibbling will be selected.
 * 
 * @returns {void}
 */
function gf_getNodeLeft ()
{
	var gt_selectedNode	= gv_graph.getSelectedNode();
	if (gt_selectedNode != null)
	{
		// get the parent node of the current node
		var gt_parent	= gf_getParentNode(gt_selectedNode);
		
		if (gt_parent != null)
		{
			// get the child nodes of this node's parent node
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
				
				// select the sibbling to the left of the current node
				var gt_nextID	= (gt_ownID < 1) ? gt_children.length - 1 : gt_ownID - 1;
				gf_paperClickNodeB(gt_children[gt_nextID]);
			}		
		}
	}
}

/**
 * Select the most left child of the current node.
 * When the current node is an end node the selection will not be changed.
 * 
 * @returns {void}
 */
function gf_getNodeNext ()
{
	var gt_selectedNode	= gv_graph.getSelectedNode();
	if (gt_selectedNode != null)
	{
		var gt_children	= gf_getChildNodes(gt_selectedNode);
		
		// select the most left child of the current node
		if (gt_children.length > 0)
		{
			gf_paperClickNodeB(gt_children[0]);
		}
	}
}

/**
 * Select the parent node of the current node.
 * When the current node is a start node the selection will not be changed.
 * 
 * @returns {void}
 */
function gf_getNodePrevious ()
{
	var gt_selectedNode	= gv_graph.getSelectedNode();
	if (gt_selectedNode != null)
	{
		// select parent node
		var gt_parent	= gf_getParentNode(gt_selectedNode);
		
		if (gt_parent != null)
		{
			gf_paperClickNodeB(gt_parent);
		}
	}
}

/**
 * Select the right sibbling of the currently selected node.
 * When the current node is the most right sibbling the most left sibbling will be selected.
 * 
 * @returns {void}
 */
function gf_getNodeRight ()
{
	var gt_selectedNode	= gv_graph.getSelectedNode();
	if (gt_selectedNode != null)
	{
		// get the parent node of the current node
		var gt_parent	= gf_getParentNode(gt_selectedNode);
		
		if (gt_parent != null)
		{
			// get the child nodes of this node's parent node
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
				
				// select the sibbling to the right of the current node
				var gt_nextID	= (gt_ownID >= gt_children.length - 1) ? 0 : gt_ownID + 1;
				gf_paperClickNodeB(gt_children[gt_nextID]);
			}		
		}
	}
}	

/**
 * Stop the drag operation and update the position of the current view box.
 * 
 * @returns {void}
 */
function gf_paperDragEnd ()
{
	// drag operation is only executed when the shift-key is pressed
	if (event.shiftKey)
	{
		gt_event	= event ? event : window.event;
		
		// get the current mouse position
		gt_endPosX	= gt_event.pageX ? gt_event.pageX : gt_event.clientX;
		gt_endPosY	= gt_event.pageY ? gt_event.pageY : gt_event.clientY;
		
		// calculate the ddifference to the original position
		gt_diffX	= (gv_mousePositionStart.x - gt_endPosX) / gv_currentViewBox.zoom;
		gt_diffY	= (gv_mousePositionStart.y - gt_endPosY) / gv_currentViewBox.zoom;
		
		// update the position of the current view box
		gv_currentViewBox.x	= gv_currentViewBox.x + gt_diffX;
		gv_currentViewBox.y	= gv_currentViewBox.y + gt_diffY;
	}
}

/**
 * Move the canvas corresponding to the mouse move.
 *  
 * @returns {void}
 */
function gf_paperDragMove ()
{
	// drag operation is only executed when the shift-key is pressed
	if (event.shiftKey)
	{
		gt_event	= event ? event : window.event;
		
		// get the current mouse position
		gt_endPosX	= gt_event.pageX ? gt_event.pageX : gt_event.clientX;
		gt_endPosY	= gt_event.pageY ? gt_event.pageY : gt_event.clientY;
		
		// calculate the ddifference to the original position
		gt_diffX	= (gv_mousePositionStart.x - gt_endPosX) / gv_currentViewBox.zoom;
		gt_diffY	= (gv_mousePositionStart.y - gt_endPosY) / gv_currentViewBox.zoom;
		
		// update the view box
		gv_paper.setViewBox(gv_currentViewBox.x + gt_diffX, gv_currentViewBox.y + gt_diffY, gv_currentViewBox.width, gv_currentViewBox.height, false);
	}
}

/**
 * Start the drag operation.
 * Backup the current mouse position as a reference for the move.
 * 
 * @returns {void}
 */
function gf_paperDragStart ()
{
	// drag operation is only executed when the shift-key is pressed
	if (event.shiftKey)
	{
		gt_event = event ? event : window.event;
		
		// back up current mouse position
		gv_mousePositionStart.x = gt_event.pageX ? gt_event.pageX : gt_event.clientX;
		gv_mousePositionStart.y = gt_event.pageY ? gt_event.pageY : gt_event.clientY;
	}
}

/**
 * Zoom the paper in.
 * 
 * @param {double} zoomFactor The zoom factor.
 * @param {Object} zoomPosition The position that has to be used as the center for the zoom.
 * @returns {void}
 */
function gf_paperZoomIn (zoomFactor, zoomPosition)
{	
	if (!gf_isset(zoomFactor))
		zoomFactor = 2;
		
	// the dimension of the current view box
	var gt_oldWidth		= gv_currentViewBox.width;
	var gt_oldHeight	= gv_currentViewBox.height;
	
	// the dimension of the view box after the zoom
	var gt_newWidth		= gv_currentViewBox.width/zoomFactor;
	var gt_newHeight	= gv_currentViewBox.height/zoomFactor;
	
	// calculate the difference between the current and the new dimensions
	var gt_diffWidth	= gt_oldWidth - gt_newWidth;
	var gt_diffHeight	= gt_oldHeight - gt_newHeight;
	
	// when the zoom position is set
	if (gf_isset(zoomPosition))
	{		
		// adapt the mouse position to the current zoom level
		var gt_mouseDiffX	= zoomPosition.x / gv_currentViewBox.zoom;
		var gt_mouseDiffY	= zoomPosition.y / gv_currentViewBox.zoom;
		
		// update the position of the view box
		gv_currentViewBox.x		= gv_currentViewBox.x + gt_mouseDiffX * (1 - 1 / zoomFactor);
		gv_currentViewBox.y		= gv_currentViewBox.y + gt_mouseDiffY * (1 - 1 / zoomFactor);
	}
	else
	{
		// update the position of the view box
		gv_currentViewBox.x			= gv_graphID == "cv" ? gv_currentViewBox.x : gv_currentViewBox.x + (gt_diffWidth/2);
		gv_currentViewBox.y			= gv_graphID == "cv" ? gv_currentViewBox.y + (gt_diffHeight/2) : gv_currentViewBox.y;
	}
	
	// set the new dimensions
	gv_currentViewBox.width		= gt_newWidth;
	gv_currentViewBox.height	= gt_newHeight;
	
	// update the zoom level
	gv_currentViewBox.zoom		= gv_currentViewBox.zoom * zoomFactor;
	
	// apply the new settings
	gv_paper.setViewBox(gv_currentViewBox.x, gv_currentViewBox.y, gv_currentViewBox.width, gv_currentViewBox.height, false);
}

/**
 * Zoom the paper out.
 * 
 * @param {double} zoomFactor The zoom factor.
 * @param {Object} zoomPosition The position that has to be used as the center for the zoom.
 * @returns {void}
 */
function gf_paperZoomOut (zoomFactor, zoomPosition)
{
	if (!gf_isset(zoomFactor))
		zoomFactor = 2;
		
	// the dimension of the current view box
	var gt_oldWidth		= gv_currentViewBox.width;
	var gt_oldHeight	= gv_currentViewBox.height;
	
	// the dimension of the view box after the zoom
	var gt_newWidth		= gv_currentViewBox.width*zoomFactor;
	var gt_newHeight	= gv_currentViewBox.height*zoomFactor;
	
	// calculate the diff between the current and the new dimensions
	var gt_diffWidth	= gt_oldWidth - gt_newWidth;
	var gt_diffHeight	= gt_oldHeight - gt_newHeight;
	
	// when the zoom position is set
	if (gf_isset(zoomPosition))
	{
		// adapt the mouse position to the current zoom level
		var gt_mouseDiffX	= zoomPosition.x / gv_currentViewBox.zoom;
		var gt_mouseDiffY	= zoomPosition.y / gv_currentViewBox.zoom;
		
		// update the position of the view box
		gv_currentViewBox.x		= gv_currentViewBox.x + gt_mouseDiffX * (1 - 1 * zoomFactor);
		gv_currentViewBox.y		= gv_currentViewBox.y + gt_mouseDiffY * (1 - 1 * zoomFactor);
	}
	else
	{
		// update the position of the view box
		gv_currentViewBox.x			= gv_graphID == "cv" ? gv_currentViewBox.x : gv_currentViewBox.x + (gt_diffWidth/2);
		gv_currentViewBox.y			= gv_graphID == "cv" ? gv_currentViewBox.y + (gt_diffHeight/2) : gv_currentViewBox.y;
	}
	
	// set the new dimensions
	gv_currentViewBox.width		= gv_currentViewBox.width*zoomFactor;
	gv_currentViewBox.height	= gv_currentViewBox.height*zoomFactor;
	
	// update the zoom level
	gv_currentViewBox.zoom		= gv_currentViewBox.zoom / zoomFactor;
	
	// apply the new settings
	gv_paper.setViewBox(gv_currentViewBox.x, gv_currentViewBox.y, gv_currentViewBox.width, gv_currentViewBox.height, false);	
}

/**
 * Reset the zoom level to 1.
 * 
 * @returns {void}
 */
function gf_paperZoomReset ()
{
	// backup the view box to the position and the dimensions of the original view box
	gv_currentViewBox.width		= gv_originalViewBox.width;
	gv_currentViewBox.height	= gv_originalViewBox.height;
	gv_currentViewBox.x			= gv_originalViewBox.x;
	gv_currentViewBox.y			= gv_originalViewBox.y;
	
	// reset the zoom level to the original zoom level
	gv_currentViewBox.zoom		= gv_originalViewBox.zoom;
	
	// update the view box
	gv_paper.setViewBox(gv_originalViewBox.x, gv_originalViewBox.y, gv_originalViewBox.width, gv_originalViewBox.height, false);
}

/**
 * This is called onChange of gv_elements.inputEdgeMessage.
 * It updates the value of gv_elements.inputEdgeText with the selected message so the edge can be updated correctly.
 * 
 * @returns {void}
 */
function gf_setEdgeMessage ()
{
	if (gf_elementExists(gv_elements.inputEdgeText, gv_elements.inputEdgeMessage))
	{
		var gt_message	= document.getElementById(gv_elements.inputEdgeMessage).value;
		
		// when the entry "##createNewMsg##" is selected -> unlock the textarea and let the user define a new message
		if (gt_message == "##createNewMsg##")
		{
			document.getElementById(gv_elements.inputEdgeText).readOnly	= false;
			document.getElementById(gv_elements.inputEdgeText).value 	= "";	
		}
		else
		{
			document.getElementById(gv_elements.inputEdgeText).readOnly	= true;
			document.getElementById(gv_elements.inputEdgeText).value 	= gt_message;
		}
	}
}

/**
 * Using this method an internal behavior can be loaded for a given subject with the current step being marked.
 * This draws the corresponding graph and marks the current step by selecting the node.
 * All you need to provide is a div element with the id "graph_bv_outer" and access to the function "showtab1()".
 * 
 * @param {String} jsonProcess The complete process graph in JSON format.
 * @param {String} subject  The currently active subject.
 * @param {String} node The currently active step in the graph. This node will be marked as active.
 * @returns {void}
 */
function gf_showInternalBehavior (jsonProcess, subject, node)
{
	// initialize the graph
	gv_graph.init();
	
	// load the process
	gv_graph.loadFromJSON(jsonProcess);
	
	gv_graph.selectedSubject = null;
	
	// get the internal behavior for the currently active subject
	var gt_behav = gv_graph.getBehavior(subject);
	if (gt_behav != null)
	{
		if (gf_isset(gt_behav.nodeIDs[node]))
		{
			var gt_nodeId = gt_behav.nodeIDs[node];
		
			// draw the graph for the internal behavior
			gf_clickedCVnode(subject);
			showtab1();
			
			// mark the currently selected node
			gf_paperClickNodeB(gt_nodeId);
		}	
	}
}