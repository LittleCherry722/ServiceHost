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

/*
 *	functions for handling the graph
 */
var gv_graph = new GFcommunication();
var gv_macros = {};

// array containing the name of the used elements
var gv_elements = {
	graphBV:			"graph_bv",		// TODO: remove
	graphCV:			"graph_cv",		// TODO: remove
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
	inputNodeType:		"ge_type",
	inputNodeType2:		"ge_type2",
	inputNodeOuter:		"node",
	
	// select elements
	inputNodeTypeNormal:	"ge_type_normal",
	inputNodeTypeStart:		"ge_type_start",
	inputNodeTypeEnd:		"ge_type_end",
		
	inputNodeType2R:		"ge_type2_R",
	inputNodeType2S:		"ge_type2_S",
	inputNodeType2End:		"ge_type2_end",
	inputNodeType2Action:	"ge_type2_action",
	
	// subject types
	inputSubjectTypeSingle:		"ge_cv_type_single",
	inputSubjectTypeMulti:		"ge_cv_type_multi",
	inputSubjectTypeExternal:	"ge_cv_type_external"
};

var gv_cv_paper = null;
var gv_bv_paper = null;
var gv_paper = null;

function gf_createMacro (id, text, type, type2, connect)
{
	if (gf_isset(id, type, text, connect) && !gf_isset(this.gv_macros[id]))
	{
		connect = connect === true ? true : false;
		gv_macros[id] = {id: id, type: type, type2: type2, text: text, connect: connect};
	}
}

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

gf_createMacro("newSendNode", "", "normal", "send", true);
gf_createMacro("newReceiveNode", "", "normal", "receive", true);
gf_createMacro("newActionNode", "internal action", "normal", "action", true);

/*
 * function called when an object in the communication view has been doubleClicked -> load the corresponding behavioral view
 */
function gf_clickedCVbehavior (graphId)
{
	gv_graph.drawBehavior(graphId);
	
	if (gf_isset(graphId))
	{
		showtab1();
	}
}

function gf_showInternalBehavior (jsonProcess, subject, node)
{
	gv_graph.init();
	gv_graph.loadFromJSON(jsonProcess);
	
	gv_graph.selectedSubject = null;
	
	var gt_behav = gv_graph.getBehavior(subject);
	
	if (gt_behav != null)
	{
		if (gf_isset(gt_behav.nodeIDs[node]))
		{
			var gt_nodeId = gt_behav.nodeIDs[node];
		
			gf_clickedCVnode(subject);
			showtab1();
			gf_paperClickNodeB(gt_nodeId);
		}	
	}
}

/*
 * function called when an object in the communication view has been clicked -> load the corresponding subject information
 */
function gf_clickedCVnode (nodeId)
{
	gv_graph.selectNode(nodeId);
}

/*
 * function called when a node in the behavioral view has been clicked -> load the corresponding node information
 */
function gf_clickedBVnode (nodeId)
{
	gv_graph.selectNode(nodeId);
}

/*
 * function called when an edge in the behavioral view has been clicked -> load the corresponding edge information
 */
function gf_clickedBVedge (edgeId)
{
	gv_graph.selectEdge(edgeId);
}

function gf_deactivateEdge ()
{
	gv_graph.deactivateEdge();	
}

function gf_deactivateNode ()
{
	gv_graph.deactivateNode();
}

/*
 * function to call the gv_graph's loadEdgeMessages() function
 */
function gf_edgeMessage ()
{
	gv_graph.loadEdgeMessages();
}

/*
 * sets the selected message
 */
function gf_setEdgeMessage ()
{
	if (gf_elementExists(gv_elements.inputEdgeText, gv_elements.inputEdgeMessage))
	{
		document.getElementById(gv_elements.inputEdgeText).value = document.getElementById(gv_elements.inputEdgeMessage).value;
	}
}

/**
 * Behavioral Graph
 * 
 * @param name
 * @returns void
 */
function GFbehavior (name)
{
	/**
	 * attributes
	 */
	this.edges	= {};
	this.nodes	= {};
	this.name	= name;
	
	// only used on creating the initial graph
	this.nodeIDs = {};
	
	this.nodeCounter = 0;
	this.edgeCounter = 0;
	
	this.selectedNode	= null;
	this.selectedEdge	= null;
	this.clickMode		= null;
	
	this.startNode		= null;
	
	/*
	 * adds an edge to this graph
	 */
	this.addEdge = function (start, end, text, relatedSubject, deactivated)
	{
		if (parseInt(start) != start && gf_isset(this.nodeIDs[start]))
			start = this.nodeIDs[start];
		
		if (parseInt(end) != end && gf_isset(this.nodeIDs[end]))
			end = this.nodeIDs[end];
		
		var gt_edge = new GFedge(this, start, end, text, relatedSubject);
				
		if (gt_edge != null)
		{
			if (deactivated === true)
				gt_edge.deactivate();
			
			this.edges["e" + this.edgeCounter++] = gt_edge;
		}
 	}
	
	/*
	 * adds a node to this graph
	 */
	this.addNode = function (id, text, type, start, end, deactivated)
	{
		if (!gf_isset(id) || id == "")
		{
			id = "n" + this.nodeCounter;
		}
		
		if (!gf_isset(type) || type == "")
			type = "action";
		
		var gt_node	= new GFnode(id, text, type);

		if (gt_node.getId() != "")
			this.nodeIDs[gt_node.getId()] = this.nodeCounter;
			
		if (gf_isset(start) && start === true)
			gt_node.setStart(true);
			
		if (gf_isset(end) && end === true)
			gt_node.setEnd(true);
			
		if (gf_isset(deactivated) && deactivated === true)
			gt_node.deactivate();
		
		this.nodes["n" + this.nodeCounter++] = gt_node;
		
		return this.nodeCounter - 1;
	}
	
	/*
	 * returns the edge with the given id
	 */
	this.getEdge = function (id)
	{
		if (!gf_isset(id) && this.selectedEdge != null)
			id = this.selectedEdge;
		
		if (gf_isset(this.edges["e" + id]))
			return this.edges["e" + id];
		return null;
	}
	
	/*
	 * returns the edges array
	 */
	this.getEdges = function ()
	{
		return this.edges;
	}
	
	/*
	 * returns the node with the given id
	 */
	this.getNode = function (id)
	{
		if (!gf_isset(id) && this.selectedNode != null)
			id = this.selectedNode;
		
		if (gf_isset(this.nodes["n" + id]))
			return this.nodes["n" + id];
		return null;
	}
	
	/*
	 * returns the nodes array
	 */
	this.getNodes = function ()
	{
		return this.nodes;
	}
	
	/*
	 * draws the graph
	 */
	this.draw = function ()
	{
		// convert all data to gv_bv_graphs[name]
		
		if (gf_isset(gv_bv_graphs[this.name]))
		{
			delete gv_bv_graphs[this.name];
		}
		
		gf_bv_addSubject(this.name);
		
		// add all nodes to the graph
		for (var gt_nid in this.nodes)
		{
			var gt_node = this.nodes[gt_nid];
			gf_bv_addNode(this.name, gt_nid.substr(1), gt_node, this.selectedNode == gt_nid.substr(1));
		}
		
		// add all edges to the graph
		for (var gt_eid in this.edges)
		{
			var gt_edge = this.edges[gt_eid];
			var gt_start = gt_edge.getStart();
			var gt_end = gt_edge.getEnd();
						
			if (gf_isset(this.nodes["n" + gt_start], this.nodes["n" + gt_end]))
			{
				gf_bv_addEdge(this.name, gt_eid.substr(1), gt_start, gt_end, gt_edge, this.selectedEdge == gt_eid.substr(1));
			}
		}
		
		gf_bv_drawGraph(this.name);
	}
	
	// functions for GUI
	/*
	 * remove all nodes and edges from the graph
	 */
	this.clearGraph = function ()
	{
		this.edges	= {};
		this.nodes	= {};
		
		this.nodeCounter = 0;
		this.edgeCounter = 0;
		
		this.selectedNode	= null;
		this.selectedEdge	= null;
		this.clickMode		= null;
		
		this.startNode		= null;
		
		this.draw();
	}
	
	/*
	 * switch the connectMode
	 */
	this.connectNodes = function ()
	{
		if (this.clickMode == "connect")
		{
			this.clickMode	= null;
			this.startNode	= null;
		}
		else
		{
			this.clickMode	= "connect";
			this.startNode	= this.selectedNode;
		}
	}
	
	/*
	 * creates a new edge between two nodes
	 */
	this.createEdge = function (start, end)
	{
		if (gf_isset(start, end))
		{
			this.addEdge(start, end, "");
			this.draw();
		}
	}
	
	/*
	 * adds a new node to the graph
	 */
	this.createNode = function ()
	{
		var gt_nodeId = this.addNode("", "new")
		gf_clickedBVnode(gt_nodeId);
		this.draw();
		return gt_nodeId;
	}
	
	this.deactivateEdge = function ()
	{
		var gt_edgeId	= this.selectedEdge;
			
		if (gt_edgeId != null)
		{
			if (this.edges["e" + gt_edgeId].isDeactivated())
			{
				this.edges["e" + gt_edgeId].activate();
				gv_objects_edges[gt_edgeId].activate();
			}
			else
			{
				this.edges["e" + gt_edgeId].deactivate();
				gv_objects_edges[gt_edgeId].deactivate();
			}
		}
	}
	
	this.deactivateNode = function ()
	{
		var gt_nodeId	= this.selectedNode;
			
		if (gt_nodeId != null)
		{
			if (this.nodes["n" + gt_nodeId].isDeactivated())
			{
				this.nodes["n" + gt_nodeId].activate();
				gv_objects_nodes[gt_nodeId].activate();
			}
			else
			{
				this.nodes["n" + gt_nodeId].deactivate();
				gv_objects_nodes[gt_nodeId].deactivate();
			}
		}
	}
	
	/*
	 * removes an edge
	 */
	this.deleteEdge = function (id)
	{
		if (!gf_isset(id) && this.selectedEdge != null)
			id = this.selectedEdge;
		
		if (gf_isset(this.edges["e" + id]))
			delete this.edges["e" + id];
		this.draw();
	}
	
	/*
	 * removes a node
	 */
	this.deleteNode = function (id)
	{
		if (!gf_isset(id) && this.selectedNode != null)
			id = this.selectedNode;
		
		if (gf_isset(this.nodes["n" + id]))
			delete this.nodes["n" + id];
		this.draw();
	}
	
	/*
	 * selects and marks a node (and creates a new edge if connectMode is active)
	 */
	this.selectNode = function (id)
	{
		if (!gf_isset(this.nodes["n" + id]) && gf_isset(this.nodeIDs[id]))
		{
			id = this.nodeIDs[id];
		}
		
		if (gf_isset(this.nodes["n" + id]))
		{
			if (this.clickMode == "connect" && this.startNode != null)
			{
				if (this.startNode != id)
				{
					var gt_edgeExists = false;
					for (var gt_edgeId in this.edges)
					{
						var gt_edge = this.edges[gt_edgeId];
						if (gt_edge.start == this.startNode && gt_edge.end == id)
						{
							gt_edgeExists = true;
							break;
						}
					}
					
					if (gt_edgeExists == false)
					{
						this.createEdge(this.startNode, id);
						this.connectNodes();
					}
				}
			}
			else
			{
				this.selectNothing();
				this.selectedNode = id;
			}
			// this.draw();
		}
	}
	
	/*
	 * selects and marks an edge
	 */
	this.selectEdge = function (id)
	{
		if (gf_isset(this.edges["e" + id]))
		{
			this.selectNothing();
			this.selectedEdge = id;
			// this.draw();
		}
	}
	
	/*
	 * deselects everything
	 */
	this.selectNothing = function ()
	{
		this.selectedEdge	= null;
		this.selectedNode	= null;
		this.clickMode		= null;
	}
	
	/*
	 * updates the selected edge's information
	 */
	this.updateEdge = function (text, relatedSubject)
	{
		if (this.selectedEdge != null && gf_isset(this.edges["e" + this.selectedEdge], text))
		{
			gt_edge = this.edges["e" + this.selectedEdge];
			
			gt_edge.setText(text);
			
			if (gf_isset(relatedSubject))
			{
				gt_edge.setRelatedSubject(relatedSubject);
			}
			
			this.draw();
		}
	}
	
	/*
	 * updates the selected node's information
	 */
	this.updateNode = function (text, startEnd, type)
	{
		
		if (this.selectedNode != null && gf_isset(this.nodes["n" + this.selectedNode], text, startEnd, type))
		{
			gt_node = this.nodes["n" + this.selectedNode];
			
			gt_node.setText(text);
			gt_node.setType(type);
			gt_node.setStart(startEnd == "start");
			gt_node.setEnd(startEnd == "end");
			
			this.draw();
		}
	}
}

/**
 * Communication graph
 * 
 * @returns
 */
function GFcommunication ()
{	
	this.messages	= {};	// 3-dim Array [from][to][]
	this.subjects	= {};
	
	this.selectedSubject	= null;
	this.selectedNode		= null;
	this.selectedEdge		= null;
	this.loadedSubject		= null;
	
	this.nodeCounter		= 0;
	
	this.clickMode			= null;
	
	/*
	 * adds a message to the communication view [internal use only]
	 */
	this.addMessage = function (from, to, message)
	{
		if (!gf_isset(this.messages[from]))
		{
			this.messages[from] = {};
		}
		
		if (!gf_isset(this.messages[from][to]))
		{
			this.messages[from][to] = [];	
		}
		
		this.messages[from][to][this.messages[from][to].length]	= message;
	}
	
	/*
	 * adds a subject to the communication view
	 */
	this.addSubject = function (id, title, type, deactivated)
	{
		if (gf_isset(id, title))
		{
			
			if (!gf_isset(type))
				type = "single";
			
			var gt_subject = new GFsubject(id, title, type);
				
			if (gf_isset(deactivated) && deactivated === true)
				gt_subject.deactivate();
			
			this.subjects[id] = gt_subject;
		}
	}
	
	/*
	 * calls the subject's behavior's draw function
	 */
	this.drawBehavior = function (id)
	{
		if (!gf_isset(id))
		{
			id = this.selectedNode;
		}
		
		var gt_behavior = this.getBehavior(id);
		
		if (gt_behavior != null)
		{
			gt_behavior.draw();
			this.selectedSubject = id;
			this.loadedSubject = id;
		}
	}
	
	/*
	 * gets the subject's behavior
	 */
	this.getBehavior = function (id)
	{
		if (gf_isset(this.subjects[id]))
			return this.subjects[id].getBehavior();
		return null;
	}
	
	/*
	 * returns the messages array
	 */
	this.getMessages = function (from, to)
	{
		if (gf_isset(this.messages[from]) && gf_isset(this.messages[from][to]) && gf_isArray(this.messages[from][to]))
		{
			return this.messages[from][to];
		}
		return [];
	}
	
	/*
	 * returns the messages between two subjects as a string connected by new lines; each messages is marked as a list element (if more than 1 message exists)
	 */
	this.implodeMessages = function (from, to)
	{
		var gt_messages = this.getMessages(from, to);
		var gt_implodedMessages = "";
		
		if (gt_messages.length == 1)
		{
			gt_implodedMessages = gt_messages[0];
		}
		else if (gt_messages.length > 0)
		{
			for (var gt_mi = 0; gt_mi < gt_messages.length; gt_mi++)
			{
				if (gt_mi > 0)
					gt_implodedMessages += "\n";
				
				gt_implodedMessages += "<li>" + gt_messages[gt_mi];
			}
		}
		
		return gt_implodedMessages;
	}
	
	/*
	 * draws the graph
	 */	
	this.draw = function ()
	{		
		// clear messages
		this.messages = {};
		
		// load messages from behavior
		for (var gt_bi in this.subjects)
		{
			var gt_behav = this.getBehavior(gt_bi);
			var gt_edges = gt_behav.getEdges();
			for (var gt_eid in gt_edges)
			{
				var gt_edge					= gt_edges[gt_eid];
				var gt_startNode			= gt_behav.getNode(gt_edge.getStart());
				var gt_endNode				= gt_behav.getNode(gt_edge.getEnd());
				var gt_relatedSubject		= gt_edge.getRelatedSubject();
				var gt_text					= gt_edge.getText();
				
				if (gt_startNode != null && gt_endNode != null && gt_relatedSubject != null && gt_text != "")
				{
					if (gf_isset(this.subjects[gt_relatedSubject]) && gt_startNode.getType() == "send")
					{
						this.addMessage(gt_bi, gt_relatedSubject, gt_text);
					}
				}
			}
		}
		
		// clear graph
		gv_cv_graph = {subjects: {}, messages: {}};
		
		// add subjects
		for (var gt_sid in this.subjects)
		{
			gf_cv_addSubject (this.subjects[gt_sid], this.selectedNode == gt_sid);
		}
		
		for (var gt_fromId in this.messages)
		{
			for (var gt_toId in this.messages[gt_fromId])
			{
				if (gt_fromId != gt_toId)
				{
					var gt_text = this.implodeMessages(gt_fromId, gt_toId);
					
					if (gt_text != "")
					{
						gf_cv_addMessage (gt_fromId, gt_toId, gt_text);
					}
				}
			}
		}
		
		gf_cv_drawGraph();
	}
	
	/*
	 * Below are functions for connection with the gui
	 */
	
	/*
	 * switch between communication view and behavioral view (used to clear temporary variables)
	 */
	this.changeView = function (view)
	{
		if (view == "cv")
		{
			gf_paperChangeView("cv");
			if (gf_elementExists(gv_elements.graphCVouter))
			{
				document.getElementById(gv_elements.graphCVouter).style.display="block";				
			
				this.selectedSubject	= null;
				this.selectedNode		= null;
				this.selectedEdge		= null;
				this.loadInformation(true);
				this.draw();
			}
		}
		else if (view == "bv")
		{
			gf_paperChangeView("bv");
			if (gf_elementExists(gv_elements.graphBVouter))
			{
				document.getElementById(gv_elements.graphBVouter).style.display="block";				
			
				this.selectedSubject = this.loadedSubject;
				
				if (gf_isset(this.subjects[this.selectedSubject]))
				{
					this.getBehavior(this.selectedSubject).selectNothing();
				}
			}
		}
		this.clickMode = null;
	}
	
	this.init = function ()
	{
		if (gf_elementExists(gv_elements.graphCVouter))
			gv_cv_paper = Raphael(gv_elements.graphCVouter, gv_paperSizes.cv_width, gv_paperSizes.cv_height);
		if (gf_elementExists(gv_elements.graphBVouter))
			gv_bv_paper = Raphael(gv_elements.graphBVouter, gv_paperSizes.bv_width, gv_paperSizes.bv_height);
			
		gf_paperChangeView("cv");
	}
	
	/*
	 * clear the selected graph
	 */
	this.clearGraph = function ()
	{
		if (this.selectedSubject == null)
		{
			
			if (gf_isset(this.subjects[this.loadedSubject]))
			{
				this.getBehavior(this.loadedSubject).clearGraph();
			}
			
			this.subjects	= {};
			
			this.selectedSubject	= null;
			this.selectedNode		= null;
			this.selectedEdge		= null;
			this.loadedSubject		= null;
			
			this.nodeCounter		= 0;
			
			this.clickMode			= null;
			
			this.draw();
		}
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				this.getBehavior(this.selectedSubject).clearGraph();
			}
		}
	}
	
	/*
	 * switch the connectionMode
	 */
	this.connectNodes = function ()
	{
		if (this.selectedSubject == null)
		{
			// does not exist in cv
		}
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				this.getBehavior(this.selectedSubject).connectNodes();
			}
		}
	}
	
	/*
	 * creates a new node / subject
	 */
	this.createNode = function ()
	{
		if (this.selectedSubject == null)
		{
			this.addSubject("new" + ++this.nodeCounter, "new node " + this.nodeCounter);
			this.draw();
		}
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				return this.getBehavior(this.selectedSubject).createNode();
			}
		}
	}
	
	this.deactivateEdge = function ()
	{
		if (this.selectedSubject == null)
		{
			// does not exist in cv
		}
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				this.getBehavior(this.selectedSubject).deactivateEdge();
			}
		}
	}
	
	this.deactivateNode = function ()
	{
		if (this.selectedSubject == null)
		{
			var gt_nodeId	= this.selectedNode;
			
			if (gt_nodeId != null)
			{
				if (this.subjects[gt_nodeId].isDeactivated())
				{
					this.subjects[gt_nodeId].activate();
					gv_objects_nodes[gt_nodeId].activate();
				}
				else
				{
					this.subjects[gt_nodeId].deactivate();
					gv_objects_nodes[gt_nodeId].deactivate();
				}
			}
		}
		else
		{			
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				this.getBehavior(this.selectedSubject).deactivateNode();
			}
		}
	}
	
	/*
	 * calls deleteEdge() of the selected subject's behavior
	 */
	this.deleteEdge = function ()
	{
		if (this.selectedSubject == null)
		{
			// does not exist in cv
		}
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				this.getBehavior(this.selectedSubject).deleteEdge();
				this.loadInformation(true);
			}
		}
	}
	
	/*
	 * removes a node from either the cv or the selected subject's behavior
	 */
	this.deleteNode = function ()
	{
		if (this.selectedSubject == null)
		{
			if (this.selectedNode != null)
			{
				var gt_subject = this.subjects[this.selectedNode];
				
				// remove references to this subject
				delete this.subjects[this.selectedNode];

				for (var gt_subId in this.subjects)
				{
					var gt_behav = this.subjects[gt_subId].getBehavior();
					var gt_edges = gt_behav.getEdges();
					
					for (var gt_edgeId in gt_edges)
					{
						var gt_edge = gt_edges[gt_edgeId];
						if (gt_edge.getRelatedSubject() == this.selectedNode)
							delete gt_edges[gt_edgeId];
					}
				}
				
				if (this.loadedSubject == this.selectedNode)
				{
					this.loadedSubject = null;
					gf_paperChangeView("bv");
				}
					
				if (this.selectedSubject == this.selectedNode)
				{
					this.selectedSubject = null;
				}
				
				if (this.loadedSubject != null && gf_isset(this.subjects[this.loadedSubject]))
				{
					this.subjects[this.loadedSubject].getBehavior().draw();
				}
				this.selectedNode = null;
				
				this.draw();
			}
		}
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				this.getBehavior(this.selectedSubject).deleteNode();
				this.loadInformation(true);
			}
		}
	}
	
	this.getSelectedNode = function ()
	{
		if (this.selectedSubject == null)
		{			
			// ignore
		}
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				return this.getBehavior(this.selectedSubject).selectedNode;
			}
		}
		return null;
	}	
	
	/*
	 * fill message selection with available messages
	 */
	this.loadEdgeMessages = function ()
	{
		if (!gf_elementExists(gv_elements.inputEdgeTarget, gv_elements.inputEdgeMessage))
			return false;
			
		var gt_selectedTarget	= document.getElementById(gv_elements.inputEdgeTarget).value;
		var gt_select_message	= document.getElementById(gv_elements.inputEdgeMessage).options;
		var gt_messagesArray	= [];
		
		gt_select_message.length	= 0;
		
		if (gt_selectedTarget != "" && gt_selectedTarget != this.selectedSubject)
		{
			
			var gt_option = document.createElement("option");
			gt_option.text = "please select";
			gt_option.value = "";
			gt_option.id = gv_elements.inputEdgeTarget + "_00000.0";
			gt_select_message.add(gt_option);
			
			var gt_option = document.createElement("option");
			gt_option.text = "----------------------------";
			gt_option.value = "";
			gt_option.id = gv_elements.inputEdgeTarget + "_00000.1";
			gt_select_message.add(gt_option);
			
			var gt_behav = this.getBehavior(gt_selectedTarget);
			var gt_edges = gt_behav.getEdges();
			for (var gt_eid in gt_edges)
			{
				var gt_edge					= gt_edges[gt_eid];
				var gt_startNode			= gt_behav.getNode(gt_edge.getStart());
				var gt_endNode				= gt_behav.getNode(gt_edge.getEnd());
				var gt_relatedSubject		= gt_edge.getRelatedSubject();
				var gt_text					= gt_edge.getText();
				
				if (gt_startNode != null && gt_endNode != null && gt_relatedSubject != null && gt_text != "")
				{
					if (gf_isset(this.subjects[gt_relatedSubject]) && gt_relatedSubject == this.selectedSubject && gt_startNode.getType() == "send")
					{
						gt_messagesArray[gt_messagesArray.length]	= gt_text;
					}
				}
			}
			
			gt_messagesArray.sort();
			
			for (var gt_mid in gt_messagesArray)
			{
				var gt_option = document.createElement("option");
				gt_option.text = gt_messagesArray[gt_mid];
				gt_option.value = gt_messagesArray[gt_mid];
				gt_option.id = gv_elements.inputEdgeMessage + "_" + gt_mid;
				gt_select_message.add(gt_option);
				
				if (gf_elementExists(gv_elements.inputEdgeText) && gt_messagesArray[gt_mid].replace("\\n", "") == document.getElementById(gv_elements.inputEdgeText).value.replace("\\n", ""))
				{
					document.getElementById(gv_elements.inputEdgeMessage + "_" + gt_mid).selected = true;
				}
			}
		}
	}
	
	/*
	 * loads the information of the selected edge (only applicable for bv)
	 */
	this.loadInformationEdge = function ()
	{
		if (this.selectedSubject == null)
		{
			// not available in cv
		}
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				var gt_edge = this.getBehavior(this.selectedSubject).getEdge();
				var gt_node = this.getBehavior(this.selectedSubject).getNode(gt_edge.getStart());
				
				if (gf_elementExists(gv_elements.inputEdgeText))
				{
					document.getElementById(gv_elements.inputEdgeText).value	= gt_edge.getText();
					document.getElementById(gv_elements.inputEdgeText).readOnly	= false;					
				}
				
				if (gf_elementExists(gv_elements.inputEdgeMessageO))
					document.getElementById(gv_elements.inputEdgeMessageO).style.display = "none";
		
				if (gf_elementExists(gv_elements.inputNodeOuter))
					document.getElementById(gv_elements.inputNodeOuter).style.display = "none";
				
				if (gf_elementExists(gv_elements.inputEdgeOuter))
					document.getElementById(gv_elements.inputEdgeOuter).style.display = "block";
				
				var gt_select_target		= gf_elementExists(gv_elements.inputEdgeTarget) ? document.getElementById(gv_elements.inputEdgeTarget).options : null;
				var gt_select_message		= gf_elementExists(gv_elements.inputEdgeMessage) ? document.getElementById(gv_elements.inputEdgeMessage).options : null;
				
				if (gt_select_target != null)
					gt_select_target.length		= 0;
					
				if (gt_select_message != null)
					gt_select_message.length	= 0;
				
				// create the drop down menu to select the related subject (only for receive and send nodes)
				if ((gt_node.getType() == "send" || gt_node.getType() == "receive") && gt_select_target != null && gt_select_message != null)
				{
					if (gt_node.getType() == "receive")
					{
						if (gf_elementExists(gv_elements.inputEdgeText))
							document.getElementById(gv_elements.inputEdgeText).readOnly				= true;
							
						if (gf_elementExists(gv_elements.inputEdgeMessageO))
							document.getElementById(gv_elements.inputEdgeMessageO).style.display	= "block";
							
						document.getElementById(gv_elements.inputEdgeTarget).onchange			= gf_edgeMessage;
						document.getElementById(gv_elements.inputEdgeMessage).onchange			= gf_setEdgeMessage;
					}
					else
					{
						document.getElementById(gv_elements.inputEdgeTarget).onchange	= null;
						document.getElementById(gv_elements.inputEdgeMessage).onchange	= null;
					}
					
					var gt_option = document.createElement("option");
					gt_option.text = "please select";
					gt_option.value = "";
					gt_option.id = gv_elements.inputEdgeTarget + "_00000.0";
					gt_select_target.add(gt_option);
					
					var gt_option = document.createElement("option");
					gt_option.text = "----------------------------";
					gt_option.value = "";
					gt_option.id = gv_elements.inputEdgeTarget + "_00000.1";
					gt_select_target.add(gt_option);
					
					// read the subjects that can be related
					var gt_subjectArray = [];
					var gt_subjectArrayIDs	= [];
					
					for (var gt_sid in this.subjects)
					{
						if (gt_sid != this.selectedSubject)
						{
							gt_subjectArrayIDs[gt_subjectArray.length]	= gt_sid;
							gt_subjectArray[gt_subjectArray.length]		= this.subjects[gt_sid].getText() + " (" + gt_sid + ")";
						}
					}
					
					// sort the subjects
					gt_subjectArray.sort();
					
					// add the subjects as options to the select field
					for (var gt_sid in gt_subjectArray)
					{						
						var gt_option = document.createElement("option");
						var gt_subjID = gt_subjectArray[gt_sid];
						gt_option.text = gt_subjID;
						gt_option.value = gt_subjectArrayIDs[gt_sid];
						gt_option.id = gv_elements.inputEdgeTarget + "_" + gt_subjID;
						gt_select_target.add(gt_option);
						
						if (gt_sid == gt_edge.getRelatedSubject())
						{
							document.getElementById(gv_elements.inputEdgeTarget + "_" + gt_subjID).selected = true;
						}
					}
					
					/*
					 * read available messages
					 */
					this.loadEdgeMessages();
				}
			}
		}
	}
	
	/*
	 * load the information of the selected node (either in bv or cv) into the corresponding elements; if clear is true the fields will be cleared
	 */
	this.loadInformation = function (clear)
	{		
		if (gf_isset(clear) && clear == true)
		{
			if (gf_elementExists(gv_elements.inputNodeText))
				document.getElementById(gv_elements.inputNodeText).value = "";
			if (gf_elementExists(gv_elements.inputNodeType))
				document.getElementById(gv_elements.inputNodeType).value = "";
			if (gf_elementExists(gv_elements.inputNodeType2))
				document.getElementById(gv_elements.inputNodeType2).value = "";
			
			if (gf_elementExists(gv_elements.inputSubjectText))
				document.getElementById(gv_elements.inputSubjectText).value = "";
			if (gf_elementExists(gv_elements.inputSubjectId))
				document.getElementById(gv_elements.inputSubjectId).value = "";

			if (gf_elementExists(gv_elements.inputEdgeText))
				document.getElementById(gv_elements.inputEdgeText).value = "";
			if (gf_elementExists(gv_elements.inputEdgeTarget))
				document.getElementById(gv_elements.inputEdgeTarget).options.length = 0;
			
		}
		else
		{			
			this.loadInformation(true);
			if (gf_elementExists(gv_elements.inputNodeOuter))
				document.getElementById(gv_elements.inputNodeOuter).style.display = "block";
			if (gf_elementExists(gv_elements.inputEdgeOuter))
				document.getElementById(gv_elements.inputEdgeOuter).style.display = "none";
		
			if (this.selectedSubject == null)
			{
				if (this.selectedNode != null && gf_isset(this.subjects[this.selectedNode]))
				{
					var gt_subject = this.subjects[this.selectedNode];		
					
					if (gf_elementExists(gv_elements.inputSubjectText))
						document.getElementById(gv_elements.inputSubjectText).value = gt_subject.getText();
						
					if (gf_elementExists(gv_elements.inputSubjectId))
						document.getElementById(gv_elements.inputSubjectId).value = gt_subject.getId();
						
					if (gf_elementExists(gv_elements.inputSubjectTypeSingle) && gt_subject.getType() == "single")
						document.getElementById(gv_elements.inputSubjectTypeSingle).checked = true;
				
					if (gf_elementExists(gv_elements.inputSubjectTypeMulti) && gt_subject.getType() == "multi")
						document.getElementById(gv_elements.inputSubjectTypeMulti).checked = true;
					
					if (gf_elementExists(gv_elements.inputSubjectTypeExternal) && gt_subject.getType() == "external")
						document.getElementById(gv_elements.inputSubjectTypeExternal).checked = true;
				}
			}
			else
			{
				if (gf_isset(this.subjects[this.selectedSubject]))
				{
					
					var gt_node = this.getBehavior(this.selectedSubject).getNode();
					
					if (gf_elementExists(gv_elements.inputNodeText))
						document.getElementById(gv_elements.inputNodeText).value = gt_node.getText();
					
					// clear selection
					if (gf_elementExists(gv_elements.inputNodeTypeNormal))
						document.getElementById(gv_elements.inputNodeTypeNormal).selected = !gt_node.isStart() && !gt_node.isEnd();
					if (gf_elementExists(gv_elements.inputNodeTypeStart))
						document.getElementById(gv_elements.inputNodeTypeStart).selected = gt_node.isStart();
					if (gf_elementExists(gv_elements.inputNodeTypeEnd))
						document.getElementById(gv_elements.inputNodeTypeEnd).selected = gt_node.isEnd();

					if (gf_elementExists(gv_elements.inputNodeType2R))
						document.getElementById(gv_elements.inputNodeType2R).selected = gt_node.getType() == "receive";
					if (gf_elementExists(gv_elements.inputNodeType2S))
						document.getElementById(gv_elements.inputNodeType2S).selected = gt_node.getType() == "send";
					if (gf_elementExists(gv_elements.inputNodeType2End))
						document.getElementById(gv_elements.inputNodeType2End).selected = gt_node.isEnd();
					if (gf_elementExists(gv_elements.inputNodeType2Action))
						document.getElementById(gv_elements.inputNodeType2Action).selected = !gt_node.isEnd() && gt_node.getType() == "action";
				}
			}
		}
	}
	
	/*
	 * transforms all subjects and their behaviors into an simple array that can easily be passed to another API to be stored in the DB
	 */
	this.save = function ()
	{
		var gt_array = [];
		
		var gt_arrayIndex	= 0;
		
		// transform subjects and the related behaviors
		for (var gt_sid in this.subjects)
		{
			gt_arrayIndex = gt_array.length;
			
			gt_array[gt_arrayIndex] = {	id: gt_sid,
										name: this.subjects[gt_sid].getText(),
										type: this.subjects[gt_sid].getType(),
										deactivated: this.subjects[gt_sid].isDeactivated()};
			
			var gt_behav = this.subjects[gt_sid].getBehavior();
			var gt_nodes = gt_behav.getNodes();
			var gt_edges = gt_behav.getEdges();
			var gt_newNodes = new Array();	// TODO: replace by {} ?
			var gt_newEdges = new Array();	// TODO: replace by {} ?
			
			// transform the behavior's nodes
			for (var gt_nid in gt_nodes)
			{
				var gt_node = gt_nodes[gt_nid];
				gt_newNodes[gt_nid.substr(1)] = {
						id:		gt_node.getId(),
						text:	gt_node.getText(),
						start:	gt_node.isStart(),
						end:	gt_node.isEnd(),
						type:	gt_node.getType(),
						deactivated: gt_node.isDeactivated()
				};
			}

			// transform the behavior's edges
			for (var gt_eid in gt_edges)
			{
				var gt_edge			= gt_edges[gt_eid];
				var gt_edgeEnd		= gt_edge.getEnd();
				var gt_edgeStart	= gt_edge.getStart();
				
				if (gt_edgeEnd != null && gt_edgeStart != null)
				{
					var gt_relatedSubject	= gt_edge.getRelatedSubject();
					var gt_edgeStartNode	= gt_behav.getNode(gt_edgeStart);
					var gt_edgeEndNode		= gt_behav.getNode(gt_edgeEnd);
					
					if (gt_edgeStartNode != null && gt_edgeEndNode != null)
					{
						gt_newEdges[gt_eid.substr(1)] = {
								start:	gt_edgeStartNode.getId(),
								end:	gt_edgeEndNode.getId(),
								text:	gt_edge.getText(),
								target: gt_relatedSubject == null ? "" : gt_relatedSubject,
								deactivated: gt_edge.isDeactivated()
						};
					}
				}
			}
			
			gt_array[gt_arrayIndex].nodes = gt_newNodes;
			gt_array[gt_arrayIndex].edges = gt_newEdges;
		}
		
		return gt_array;
	}
	
	/*
	 * select a node (either in bv or cv)
	 */
	this.selectNode = function (id)
	{
		if (this.selectedSubject == null)
		{			
			if (gf_isset(this.subjects[id]))
			{
				if (this.clickMode == "connect" && this.startNode != null)
				{
					// does not exist in cv
				}
				else
				{
					this.selectNothing();
					this.selectedNode = id;
					updateListOfSubjects();	// custom code
					// this.draw();
				}
			}
		}
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				this.getBehavior(this.selectedSubject).selectNode(id);
			}
		}
		this.loadInformation();
	}
	
	/*
	 * select an edge of the selected subject's behavior
	 */
	this.selectEdge = function (id)
	{
		if (this.selectedSubject == null)
		{			
			// not available in cv
		}
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				this.getBehavior(this.selectedSubject).selectEdge(id);
				this.loadInformationEdge();
			}
		}		
	}
	
	/*
	 * clear selection in cv
	 */
	this.selectNothing = function ()
	{
		this.selectedEdge	= null;
		this.selectedNode	= null;
		this.clickMode		= null;
		this.loadEdgeMessages();
	}
	
	/*
	 * update the selected node's information
	 */
	this.updateNode = function ()
	{
		if (this.selectedSubject == null)
		{
			if (this.selectedNode != null && gf_isset(this.subjects[this.selectedNode]))
			{
				var gt_subject = this.subjects[this.selectedNode];					
				
				var gt_text = gf_elementExists(gv_elements.inputSubjectText) ? document.getElementById(gv_elements.inputSubjectText).value : "";
				var gt_id = gf_elementExists(gv_elements.inputSubjectId) ? document.getElementById(gv_elements.inputSubjectId).value : "";
				
				var gt_type	= gt_subject.getType();
				
				if (gf_elementExists(gv_elements.inputSubjectTypeSingle) && document.getElementById(gv_elements.inputSubjectTypeSingle).checked === true)
					gt_type = "single";
				
				if (gf_elementExists(gv_elements.inputSubjectTypeMulti) && document.getElementById(gv_elements.inputSubjectTypeMulti).checked === true)
					gt_type = "multi";
				
				if (gf_elementExists(gv_elements.inputSubjectTypeExternal) && document.getElementById(gv_elements.inputSubjectTypeExternal).checked === true)
					gt_type = "external";
				
				
				if (gt_text.replace(" ", "") != "" && gt_id.replace(" ", "") != "")
				{
				
					gt_subject.setText(gt_text);
					gt_subject.setType(gt_type);					
					
					// update references to this subject
					if (this.selectedNode != gt_id && !gf_isset(this.subjects[gt_id]))
					{
						gt_subject.setId(gt_id);
						
						delete this.subjects[this.selectedNode];
	
						for (var gt_subId in this.subjects)
						{
							var gt_edges = this.subjects[gt_subId].getBehavior().getEdges();
							
							for (var gt_edgeId in gt_edges)
							{
								var gt_edge = gt_edges[gt_edgeId];
								if (gt_edge.getRelatedSubject() == this.selectedNode)
									gt_edge.setRelatedSubject(gt_id);
							}
						}
						
						gt_subject.getBehavior().name = gt_id;
						this.subjects[gt_id] = gt_subject;
						this.selectNode(gt_id);
						
						if (this.loadedSubject == this.selectedNode)
						{
							this.loadedSubject = gt_id;
						}
						
						if (this.selectedSubject == this.selectedNode)
						{
							this.selectedSubject = gt_id;
						}
						
						if (this.loadedSubject != null && gf_isset(this.subjects[this.loadedSubject]))
						{
							this.subjects[this.loadedSubject].getBehavior().draw();
						}
					}
					this.draw();
				}
			}
		}
		
		// if behavior is selected
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				// read the fields' values and pass to the bv
				var gt_text	= gf_elementExists(gv_elements.inputNodeText) ? document.getElementById(gv_elements.inputNodeText).value : "";
				var gt_type	= gf_elementExists(gv_elements.inputNodeType) ? document.getElementById(gv_elements.inputNodeType).value : "";
				var gt_type2 = gf_elementExists(gv_elements.inputNodeType2) ? document.getElementById(gv_elements.inputNodeType2).value.toLowerCase() : "";
				
				if (gt_type2 == "r")
					gt_type2 = "receive";
					
				if (gt_type2 == "s")
					gt_type2 = "send";
				
				this.getBehavior(this.selectedSubject).updateNode(gt_text, gt_type, gt_type2);
				this.loadInformation();
			}
		}
	}
	
	/*
	 * update the edge's information (bv only)
	 */
	this.updateEdge = function ()
	{
		if (this.selectedSubject == null)
		{
			// not available in cv
		}
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				// read information from fields and pass to bv
				var gt_text				= gf_elementExists(gv_elements.inputEdgeText) ? document.getElementById(gv_elements.inputEdgeText).value : "";
				var gt_relatedSubject	= gf_elementExists(gv_elements.inputEdgeTarget) ? document.getElementById(gv_elements.inputEdgeTarget).value : "";
				
				this.getBehavior(this.selectedSubject).updateEdge(gt_text, gt_relatedSubject);
				this.loadInformationEdge();
			}
		}
	}
	
	
	this.loadFromJSON = function (jsonString)
	{
		var gt_jsonObject = JSON.parse(jsonString);
		
		// 1. create subjects (replace <br /> by \n)
		for (var gt_subjectId in gt_jsonObject)
		{
			var gt_subject = gt_jsonObject[gt_subjectId];
			
			gv_graph.addSubject(gt_subject.id, gf_replaceNewline(gt_subject.name), gt_subject.type, gt_subject.deactivated);
		}
		
		// 2. add nodes + edges
		for (var gt_subjectId in gt_jsonObject)
		{
			var gt_subject	= gt_jsonObject[gt_subjectId];
			var gt_behav	= gv_graph.getBehavior(gt_subject.id);
			
			if (gt_behav != null)
			{
				// 2.1 nodes
				for (var gt_nodeId in gt_subject.nodes)
				{
					var gt_node	= gt_subject.nodes[gt_nodeId];
					gt_behav.addNode(gt_node.id, gf_replaceNewline(gt_node.text), gt_node.type, gt_node.start, gt_node.end, gt_node.deactivated);
				}
				
				// 2.2 edges
				for (var gt_edgeId in gt_subject.edges)
				{
					var gt_edge = gt_subject.edges[gt_edgeId];
					gt_behav.addEdge(gt_edge.start, gt_edge.end, gf_replaceNewline(gt_edge.text), gt_edge.target, gt_edge.deactivated);
				}
			}
		}	
	}
	
	this.saveToJSON = function ()
	{
		return JSON.stringify(this.save()).replace(/\\n/gi, "<br />");
	}
}

/*
 * Edge class
 */
GFedge = function (parent, start, end, text, relatedSubject)
{
	if (!gf_isset(start) || parseInt(start) != start)
		start = 0;
	
	if (!gf_isset(end) || parseInt(end) != end)
		end = 0;
	
	if (!gf_isset(text))
		text = "";
	
	if (!gf_isset(relatedSubject))
		relatedSubject = null;
	
	this.parent	= parent;
	this.end	= end;
	this.start	= start;
	this.text	= text;
	this.relatedSubject	= relatedSubject;
	this.deactivated	= false;
	
	this.activate = function ()
	{
		this.deactivated = false;
	}
	
	this.deactivate = function ()
	{
		this.deactivated = true;
	}
	
	/*
	 * returns the id of the end-node of this edge
	 */
	this.getEnd = function ()
	{
		return this.end;
	}
	
	/*
	 * returns the id of the start-node of this edge
	 */
	this.getStart = function ()
	{
		return this.start;
	}
	
	/*
	 * returns the id of the related subject (the subject a message is sent to / received from) or null, if no subject is related
	 */
	this.getRelatedSubject = function ()
	{
		var startNode		= this.parent.getNode(this.start);
		var relatedSubject	= this.relatedSubject;
		
		if (startNode == null || (startNode.getType() != "receive" && startNode.getType() != "send") || relatedSubject == "")
		{
			relatedSubject = null;
		}
		
		return relatedSubject;
	}
	
	/*
	 * returns the text of this edge
	 */
	this.getText = function ()
	{
		return this.text;
	}
	
	this.isDeactivated = function ()
	{
		return this.deactivated === true;
	}
	
	/*
	 * sets the id of the end node of this edge
	 */
	this.setEnd = function (end)
	{
		if (gf_isset(end) && parseInt(end) == end)
			this.end = end;
	}
	
	/*
	 * sets the id of the start node of this edge
	 */
	this.setStart = function (start)
	{
		if (gf_isset(start) && parseInt(start) == start)
			this.start = start;
	}
	
	/*
	 * sets the related subject (subject a message is sent to / received from)
	 */
	this.setRelatedSubject = function (relatedSubject)
	{
		var startNode		= this.parent.getNode(this.start);
		
		if (gf_isset(relatedSubject) && relatedSubject != "" && startNode != null && (startNode.getType() == "receive" || startNode.getType() == "send"))
			this.relatedSubject = relatedSubject;
	}
	
	/*
	 * sets the text of this edge
	 */
	this.setText = function (text)
	{
		if (gf_isset(text))
			this.text = text;
	}
	
	/*
	 * returns a string containing the edge's text and the name of the related subjects (if one) in brackets
	 */
	this.textToString = function ()
	{
		var startNode		= this.parent.getNode(this.start);
		
		return this.text + (this.getRelatedSubject() != null ? "\n(" + (startNode.getType() == "receive" ? "from" : "to") + ": " + this.relatedSubject + ")" : "");
	}
}

/*
 * Node class
 */
GFnode = function (id, text, type)
{
	this.id		= "";
	this.text	= "";
	this.type	= "action";		// send, receive, action (default), ...
	this.start	= false;
	this.end	= false;
	this.deactivated	= false;
	
	this.activate = function ()
	{
		this.deactivated = false;
	}
	
	this.deactivate = function ()
	{
		this.deactivated = true;
	}
	
	/*
	 * returns the id of this node
	 */
	this.getId = function ()
	{
		return this.id;
	}
	
	this.getShape = function ()
	{
		var type	= this.type.toLowerCase();
		var shape	= "roundedrectangle";
		
		if (this.isEnd())
			type = "end";
		
		if (gf_isset(gv_nodeTypes[type]) && gf_isset(gv_nodeTypes[type].shape))
		{
			shape	= gv_nodeTypes[type].shape.toLowerCase();
		}
		return shape;
	}
	
	/*
	 * returns the text of this node
	 */
	this.getText = function ()
	{
		return this.text;
	}
	
	this.getTextGraph = function ()
	{
		var type	= this.type.toLowerCase();
		var text	= this.text;
		
		if (this.isEnd())
			type = "end";
		
		if (gf_isset(gv_nodeTypes[type]) && gf_isset(gv_nodeTypes[type].text))
		{
			text	= gv_nodeTypes[type].text;
		}
		return text;
	}
	
	/*
	 * returns the type (send, receive, action, ...) of this node
	 */
	this.getType = function ()
	{
		return this.type.toLowerCase();
	}
	
	this.isDeactivated = function ()
	{
		return this.deactivated === true;
	}
	
	this.isEnd = function ()
	{
		return this.end === true;
	}
	
	this.isStart = function ()
	{
		return this.start === true;
	}
	
	this.setEnd = function (end)
	{
		if (gf_isset(end))
		{
			this.end = end === true;
		}
	}
	
	/*
	 * sets the id of this node
	 */
	this.setId = function (id)
	{
		if (gf_isset(id))
		{
			this.id = id;
		}
	}
	
	this.setStart = function (start)
	{
		if (gf_isset(start))
		{
			this.start = start === true;
		}
	}
	
	/*
	 * sets the text of this node
	 */
	this.setText = function (text)
	{
		if (gf_isset(text))
		{
			this.text = text;
		}
	}
	
	/*
	 * sets the type (send, receive, action, ...) of this node
	 */
	this.setType = function (type)
	{
		if (gf_isset(type))
		{
			type = type.toLowerCase();
			
			this.type = type;
		}
	}
	
	// init
	this.setText(text);
	this.setId(id);
	this.setType(type);
}

/*
 * Subject class
 */
// TODO: add function to set process to be loaded on dblClick on external subject
GFsubject = function (id, text, type)
{
	if (!gf_isset(text))
		text = "";
		
	if (!gf_isset(type))
		type = "single";		// single, multi, external
	
	this.id			= id;
	this.text		= text;
	this.type		= type;
	this.behavior	= new GFbehavior(id);
	this.deactivated	= false;
	
	this.activate = function ()
	{
		this.deactivated = false;
	}
	
	this.deactivate = function ()
	{
		this.deactivated = true;
	}
	
	/*
	 * returns the behavioral graph of this subject
	 */
	this.getBehavior = function ()
	{
		return this.behavior;
	}
	
	/*
	 * returns the id of this subject
	 */
	this.getId = function ()
	{
		return this.id;
	}
	
	/*
	 * returns the text of this subject
	 */
	this.getText = function ()
	{
		return this.text;
	}
	
	this.getType = function ()
	{
		return this.type.toLowerCase();
	}
	
	this.isDeactivated = function ()
	{
		return this.deactivated === true;
	}
	
	/*
	 * sets the id of this subject
	 */
	this.setId = function (id)
	{
		if (gf_isset(id))
		{
			this.id = id;
		}
	}
	
	/*
	 * sets the text of this subject
	 */
	this.setText = function (text)
	{
		if (gf_isset(text))
		{
			this.text = text;
		}
	}
	
	this.setType = function (type)
	{
		if (gf_isset(type))
		{
			type = type.toLowerCase();
			if (type == "single" || type == "multi" || type == "external")
			{
				this.type = type;
			}
		}
	}
	
	/*
	 * returns the text of this subject with it's id (in brackets)
	 */
	this.textToString = function ()
	{
		return this.text + "\n(" + this.id + ")";
	}
	
	// set the type
	this.setType(type);
}