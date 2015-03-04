/**
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
 * The node class represents a macro in a behavioral view.
 *
 * @private
 * @class represents a macro in a behavioral view
 * @param {GCbehavior} parent The parent instance of GCbehavior.
 * @param {String} id The id of the macro.
 * @param {String} name The name of the macro.
 * @returns {void}
 */
function GCmacro (parent, id, name)
{
	/**
	 * When connectMode is set to true and a new node is created it will automatically be connected to the previously selected node with the new node as the target node.
	 *
	 * @see GCmacro.connectNodes(), GCmacro.createNode()
	 * @type boolean
	 */
	this.connectMode	= false;

	/**
	 * Initialized with 0.
	 * This counter is used to give every edge an unique id.
	 * Thus the counter is increased with every new edge.
	 *
	 * @type int
	 */
	this.edgeCounter = 0;

	/**
	 * This is an object of GCedges.
	 * It contains all edges of the macro.
	 * The keys of this array are the values of edgeCounter prefixed with "e".
	 *
	 * @type GCedge[]
	 */
	this.edges	= {};

	/**
	 * The edge (key of edges array) of the edge that is currently selected for being assigned a new end Node.
	 *
	 * @type int
	 */
	this.endEdge		= null;

	/**
	 * The id of this macro.
	 *
	 * @type String
	 */
	this.id	= id;

	/**
	 * The name of this macro.
	 *
	 * @type String
	 */
	this.name	= name;

	/**
	 * Initialized with 0.
	 * This counter is used to give every node an unique id which is also used for the onClick event of the graph elements.
	 * The counter is increased with every node added.
	 * It is also used when a new node is added to the graph and thus has no id to store in the database.
	 *
	 * @type int
	 */
	this.nodeCounter = 0;

	/**
	 * This array is mainly used during load time of a graph.
	 * It is a map with a node's id attribute as the key and the unique id, that identifies the node within the macro, as the value.
	 *
	 * @type int[]
	 */
	this.nodeIDs = {};

	/**
	 * This array contains all GCnodes of the macro.
	 * The keys of this array are the values of nodeCounter prefixed with "n".
	 *
	 * @type GCnode[]
	 */
	this.nodes	= {};

	/**
	 * Parent GCbehavior instance.
	 *
	 * @type GCbehavior.
	 */
	this.parent	= parent;

	/**
	 * The id (key of edges array) of the currently selected edge.
	 *
	 * @type int
	 */
	this.selectedEdge	= null;

	/**
	 * The id (key of nodes array) of the currently selected node.
	 *
	 * @type int
	 */
	this.selectedNode	= null;

	/**
	 * When setEndEdgeMode is set to true and a node is selected, its target (end) node will be set to the node that is clicked on next, given that it is not the current start node.
	 *
	 * @type boolean
	 */
	this.setEndEdgeMode	= false;

	/**
	 * When setStartEdgeMode is set to true and a node is selected, its start node will be set to the node that is clicked on next, given that it is not the current target (end) of this edge and that the type of the new start node is the same as the type of the old start node.
	 *
	 * @type boolean
	 */
	this.setStartEdgeMode	= false;

	/**
	 * The edge (key of edges array) of the edge that is currently selected for being assigned a new start Node.
	 *
	 * @type int
	 */
	this.startEdge		= null;

	/**
	 * The id (key of nodes array) of the node that will be the start node of a new edge.
	 *
	 * @see GCmacro.connectNodes(), GCmacro.createNode()
	 * @type int
	 */
	this.startNode		= null;

	/**
	 * Creates a new GCedge and stores it in the edges array.
	 * The edge will be stored at key "e" + edgeCounter.
	 * The edgeCounter is increased by one.
	 *
	 * @param {int} start The id of the start node.
	 * @param {int} end The id of the end node.
	 * @param {String} text The label of this edge. When this edge's start node is either a send or receive node this can also be a message type.
	 * @param {String} relatedSubject This is only set for edges whose start node is either a send or a receive node. It refers to the subject a message is sent to / received from.
	 * @param {String} type The edge's type (exitcondition, timeout, cancelcondition).
	 * @param {boolean} [deactivated] The deactivation status of the edge. (default: false)
		 * @param {int} [manualPositionOffsetLabelX] The x position offset the user manually defined
		 * @param {int} [manualPositionOffsetLabelY] The y position offset the user manually defined
	 * @param {boolean} [optional] The optional status of the edge. (default: false)
	 * @returns {GCedge} The created edge or null on errors.
	 */
	this.addEdge = function (start, end, text, relatedSubject, type, deactivated, manualPositionOffsetLabelX, manualPositionOffsetLabelY, optional)
	{
		// read the startNode from nodeIDs when it is not numeric
		if (parseInt(start) != start && gf_isset(this.nodeIDs[start]))
			start = this.nodeIDs[start];

		// read the endNode from nodeIDs when it is not numeric
		if (parseInt(end) != end && gf_isset(this.nodeIDs[end]))
			end = this.nodeIDs[end];

		// create a new edge
		var gt_edge = new GCedge(this, this.parent, start, end, text, relatedSubject, type);

		if (gt_edge != null)
		{
			// apply the deactivation status to the edge
			if (deactivated === true)
				gt_edge.deactivate();

						if (typeof manualPositionOffsetLabelX === "number" && typeof manualPositionOffsetLabelY === "number")
								gt_edge.setManualPositionOffsetLabel({dx: manualPositionOffsetLabelX, dy: manualPositionOffsetLabelY});

			// apply the status for optional edges
			gt_edge.setOptional(optional);

			// store the edge
			this.edges["e" + this.edgeCounter++] = gt_edge;
		}

		return gt_edge;
	};

	/**
	 * Creates a new GCnode and stores it in the nodes array.
	 * The node will be stored at key "n" + nodeCounter.
	 * The nodeCounter is increased by one.
	 * When id is empty or not set a new id will be created as "n" + nodeCounter.
	 * This id is only used in the database to identify the node.
	 * The type parameter is optional and will mainly be passed by reading the nodes from the database.
	 *
	 * @param {String} id The id of the node. New nodes will get a automatically generated unique id.
	 * @param {String} text The label of the node.
	 * @param {String} [type] The type of the node. Possible values are "send", "receive", "end", "action" (default: "action")
	 * @param {boolean} [start] When set to true the node will be handled as a start node. (default: false)
	 * @param {boolean} [end] When set to true the node will be handled as an end node. (default: false)
	 * @param {int} [manualPositionOffsetX] The x position offset the user manually defined
	 * @param {int} [manualPositionOffsetY] The y position offset the user manually defined
	 * @param {boolean} [deactivated] The deactivation status of the node. (default: false)
	 * @param {boolean} [autoExecute] If set to true, the action is automatically executed by the backend
	 * @returns {int} The id that identifies the node in the nodes array.
	 */
	this.addNode = function (id, text, type, start, end, manualPositionOffsetX, manualPositionOffsetY, deactivated, autoExecute)
	{
		// create a new id if none is given
		if (!gf_isset(id) || id === "")
		{
			id = "n" + this.nodeCounter;
		}

		// set the node's type to "action" if type is not set
		if (!gf_isset(type) || type == "")
			type = "action";

		// create the new node
		var gt_node	= new GCnode(this, this.parent, this.nodeCounter, text, type);

		// backup the id -> id
		this.nodeIDs[id] = this.nodeCounter;

		// pass the start attribute to the node
		if (gf_isset(start) && start === true)
			gt_node.setStart(true);

		if (gf_isset(start) && start === true && this.id != "##main##" && !gf_isset(this.nodes["n0"]))
			gt_node.start = true;

		// pass the end attribute to the node
		if (gf_isset(end) && end === true)
			gt_node.setEnd(true);

		if (typeof manualPositionOffsetX === "number" && typeof manualPositionOffsetY === "number")
				gt_node.setManualPositionOffset({dx: manualPositionOffsetX, dy: manualPositionOffsetY});

		if (typeof autoExecute === "boolean")
				gt_node.setAutoExecute(autoExecute);

		// pass the deactivated attribute to the node
		if (gf_isset(deactivated) && deactivated === true)
				gt_node.deactivate();

		// store the node
		this.nodes["n" + this.nodeCounter++] = gt_node;

		// return the node's ID
		return this.nodeCounter - 1;
	};

	/**
	 * Clears the current macro.
	 * The nodes and edges arrays are emptied and all attributes are set to their defaults.
	 * The graph is redrawn and will result in an empty canvas.
	 *
	 * @returns {void}
	 */
	this.clearGraph = function ()
	{
		this.edges	= {};
		this.nodes	= {};

		this.nodeCounter = 0;
		this.edgeCounter = 0;

		this.selectedNode     = null;
		this.selectedEdge     = null;
		this.connectMode      = false;
		this.setStartEdgeMode	= false;
		this.setEndEdgeMode   = false;

		this.startNode		= null;

		this.draw();
	};

	/**
	 * When connectNodes() is called connectMode is toggled.
	 * When connectMode is set to true the connectMode is changed to false.
	 * When it is false connectMode is set to true and the currently selected node is backed up as the startNode.
	 *
	 * @returns {void}
	 */
	this.connectNodes = function ()
	{
		if (this.connectMode === true)
		{
			this.connectMode	= false;
			this.startNode		= null;
		}
		else
		{
			this.connectMode	= true;
			this.startNode		= this.selectedNode;
		}
	};

	/**
	 * This method creates a new edge from the node with the id given in parameter start to the node with the id given in parameter end by calling the addEdge (start, end, text, relatedSubject, type) method with an empty text and without a relatedSubject.
	 * The graph is redrawn.
	 *
	 * @param {String} start The id of the start node.
	 * @param {String} end The id of the end node.
	 * @param {String} type The type of the edge (timeout, exitcondition, cancelcondition).
	 * @returns {void}
	 */
	this.createEdge = function (start, end, type)
	{
		if (!gf_isset(type))
			type = "exitcondition";

		if (gf_isset(start, end))
		{
			this.addEdge(start, end, "", null, type);

			if (!gv_noRedraw)
				this.draw();
		}
	};

	/**
	 * Calls addNode(id, text, type) where id is an empty string, so addNode() will calculate a new id, and text is "new".
	 * The type is left blank – the node will be created as an internal action with type "normal".
	 * The resulting nodeId is then passed to gf_clickedBVnode(id) and the graph is redrawn.
	 *
	 * @returns {int} The node's id.
	 */
	this.createNode = function ()
	{
		// create the node
		var gt_nodeId = this.addNode("", "new");

		// select the inserted node
		gf_clickedBVnode(gt_nodeId);

		// redraw the graph to display the inserted node
		if (!gv_noRedraw)
			this.draw();

		// return the node's id
		return gt_nodeId;
	};

	/**
	 * De- / activates the currently selected edge depending on its current deactivation status.
	 *
	 * @returns {void}
	 */
	this.deactivateEdge = function ()
	{
		var gt_edgeId	= this.selectedEdge;

		if (gt_edgeId != null)
		{
			// when the edge is currently deactivated, activate it
			if (this.edges["e" + gt_edgeId].isDeactivated())
			{
				// update the deactivation status of the edge
				this.edges["e" + gt_edgeId].activate();

				// update the style of the edge on the graph
				gv_objects_edges[gt_edgeId].activate();
			}

			// when the edge is currently activated, deactivate it
			else
			{
				// update the deactivation status of the edge
				this.edges["e" + gt_edgeId].deactivate();

				// update the style of the edge on the graph
				gv_objects_edges[gt_edgeId].deactivate();
			}
		}
	};

	/**
	 * De- / activates the currently selected node depending on its current deactivation status.
	 *
	 * @returns {void}
	 */
	this.deactivateNode = function ()
	{
		var gt_nodeId	= this.selectedNode;

		if (gt_nodeId != null)
		{
			// when the node is currently deactivated, activate it
			if (this.nodes["n" + gt_nodeId].isDeactivated())
			{
				// update the deactivation status of the node
				this.nodes["n" + gt_nodeId].activate();

				// update the style of the node on the graph
				gv_objects_nodes[gt_nodeId].activate();
			}

			// when the node is currently activated, deactivate it
			else
			{
				// update the deactivation status of the node
				this.nodes["n" + gt_nodeId].deactivate();

				// update the style of the node on the graph
				gv_objects_nodes[gt_nodeId].deactivate();
			}
		}
	};

	/**
	 * Removes the edge with the given id from the macro.
	 * When no id is given the currently selectedEdge will be removed.
	 * The graph is redrawn.
	 *
	 * @param {int} [id] The id of the edge to remove.
	 * @returns {void}
	 */
	this.deleteEdge = function (id)
	{
		if (!gf_isset(id) && this.selectedEdge != null)
			id = this.selectedEdge;

		if (gf_isset(this.edges["e" + id]))
			delete this.edges["e" + id];
		this.draw();
	};

	/**
	 * Removes the node with the given id from the macro.
	 * When no id is given the currently selectedNode will be removed.
	 * The graph is redrawn.
	 *
	 * @param {int} [id] The id of the node to remove.
	 * @returns {void}
	 */
	this.deleteNode = function (id)
	{
		if (!gf_isset(id) && this.selectedNode != null)
			id = this.selectedNode;

		if (gf_isset(this.nodes["n" + id]) && (this.id == "##main##" || "n" + id != "n0"))
			delete this.nodes["n" + id];
		this.draw();
	};

	/**
	 * Draws the graph of this macro.
	 *
	 * @returns {void}
	 */
	this.draw = function ()
	{
		if (!gv_noRedraw)
		{

			var gt_edgePositions	= {};
			var gt_nodePositions	= {};

			/*
			 * create nodes (GCrenderNode)
			 * - set relative position change (setPositionRelative)
			 */

			// initialize nodes
			for (var gt_nid in this.nodes)
			{
				var gt_node 	= this.nodes[gt_nid],
					gt_nodeId	= gt_nid.substr(1);

				gt_nodePositions[gt_nodeId]	= new GCrenderNode(gt_nodeId, gt_node);
				gt_nodePositions[gt_nodeId].setPositionRelative(gt_node.getManualPositionOffset()['dx'], gt_node.getManualPositionOffset()['dy']);
			}

			if (this.selectedNode != null && gt_nodePositions[this.selectedNode])
				gt_nodePositions[this.selectedNode].selected = true;


			// initialize edges
			for (var gt_eid in this.edges)
			{
				var gt_edgeId	= gt_eid.substr(1);
								gt_edgePositions[gt_edgeId]	= new GCrenderEdge(gt_edgeId, this.edges[gt_eid]);
			}

			if (this.selectedEdge != null && gt_edgePositions[this.selectedEdge])
				gt_edgePositions[this.selectedEdge].selected = true;


			// convert all data to gv_bv_graphs[name]

			// linear time algorithm
			if (gv_layoutAlgorithm == "ltl" && gf_functionExists("LinearTimeLayout"))
			{
				gf_timeCalc("macro - draw (preparation)");
				var ltl	= new LinearTimeLayout();
					ltl.setRenderObjects(gt_nodePositions, gt_edgePositions);
					ltl.setSpaces({"x": gv_bv_nodeSettings.distanceX, "y": gv_bv_nodeSettings.distanceY});
					ltl.setAreaSize(gv_paperSizes.bv_width, gv_paperSizes.bv_height);

				for (var gt_nid in this.nodes)
				{
					var gt_node		= this.nodes[gt_nid];
					ltl.addNode(gt_nid, gt_node);
				}

				// add all edges to the graph
				for (var gt_eid in this.edges)
				{
					var gt_edge		= this.edges[gt_eid];
					var gt_start	= "n" + gt_edge.getStart();
					var gt_end		= "n" + gt_edge.getEnd();
					if (gf_isset(this.nodes[gt_start], this.nodes[gt_end]))
					{
						gt_edge.selected	= (this.selectedEdge == gt_eid.substr(1));
						ltl.addEdge(gt_eid, gt_start, gt_end, gt_edge);
					}

				}
				gf_timeCalc("macro - draw (preparation)");

				// for left-to-right orientation
				// ltl.orientation = "ltr";

				gf_timeCalc("macro - draw (drawGraph)");
				// if (this.edgeCounter > 0)
					ltl.layout();
				gf_timeCalc("macro - draw (drawGraph)");
			}

			// default layout algorithm
			else
			{
				gf_timeCalc("macro - draw (preparation)");

				gv_graph_bv.deleteSubject(this.parent.name);
				gv_graph_bv.addSubject(this.parent.name);
				gv_graph_bv.setRenderObjects(gt_nodePositions, gt_edgePositions);

				// add all nodes to the graph
				for (var gt_nid in this.nodes)
				{
					var gt_node = this.nodes[gt_nid];
					gv_graph_bv.addNode(this.parent.name, gt_nid.substr(1), gt_node, this.selectedNode == gt_nid.substr(1));
				}

				// add all edges to the graph
				for (var gt_eid in this.edges)
				{
					var gt_edge = this.edges[gt_eid];
					var gt_start = gt_edge.getStart();
					var gt_end = gt_edge.getEnd();

					if (gf_isset(this.nodes["n" + gt_start], this.nodes["n" + gt_end]))
					{
						gv_graph_bv.addEdge(this.parent.name, gt_eid.substr(1), gt_start, gt_end, gt_edge, this.selectedEdge == gt_eid.substr(1));
					}
				}
				gf_timeCalc("macro - draw (preparation)");

				gf_timeCalc("macro - draw (drawGraph)");
				gv_graph_bv.drawGraph(this.parent.name);
				gf_timeCalc("macro - draw (drawGraph)");


				// remove edge and node position objects to avoid redrawing
				gt_edgePositions	= null;
				gt_nodePositions	= null;
			}


			// draw nodes
			if (gf_isset(gt_nodePositions))
			{
				for (var n in gt_nodePositions)
				{
					gt_nodePositions[n].draw();
				}
			}


			// draw edges
			if (gf_isset(gt_edgePositions))
			{
				for (var e in gt_edgePositions)
				{
					gt_edgePositions[e].draw();
				}
			}

			gf_timeCalc("macro - draw (select conversation)");
			this.selectConversation(this.parent.selectedConversation);
			gf_timeCalc("macro - draw (select conversation)");
		}
	};

	/**
	 * Returns the GCedge with the given id.
	 * When the id is empty the currently selectedEdge will be returned.
	 * This method will most likely be called by GCcommunication.loadInformationEdge().
	 * returns the edge with the given id
	 *
	 * @param {int} [id] The id of the edge. When this is null or not set the id of selectedEdge will be taken.
	 * @returns {GCedge} The GCedge for the given id.
	 */
	this.getEdge = function (id)
	{
		if (!gf_isset(id) && this.selectedEdge != null)
			id = this.selectedEdge;

		if (gf_isset(this.edges["e" + id]))
			return this.edges["e" + id];
		return null;
	};

	/**
	 * Returns the edges array.
	 *
	 * @returns {GCedge[]}
	 */
	this.getEdges = function ()
	{
		return this.edges;
	};

	/**
	 * Returns the GCnode with the given internal id (not the database id).
	 * When id is empty the currently selectedNode will be returned.
	 *
	 * @param {int} [id] The id of the node. When this is null or not set the id of selectedNode will be taken.
	 * @returns {GCnode} The GCnode for the given id.
	 */
	this.getNode = function (id)
	{
		if (!gf_isset(id) && this.selectedNode != null)
			id = this.selectedNode;

		if (gf_isset(this.nodes["n" + id]))
			return this.nodes["n" + id];
		return null;
	};

	/**
	 * Returns the nodes array.
	 *
	 * @returns {GCnode[]}
	 */
	this.getNodes = function ()
	{
		return this.nodes;
	};

	/**
	 * Select a conversation.
	 * This will deactivate all nodes and edges that do not belong to the specified conversation.
	 *
	 * @param {String} conversation The name of the conversation to select. When set to "##all##" all nodes and edges will be displayed.
	 * @returns {void}
	 */
	this.selectConversation = function (conversation)
	{
		if (!gf_isset(conversation))
			conversation	= "##all##";

		this.parent.selectedConversation	= conversation;

		var gt_node			= null;
		var gt_edge			= null;
		var gt_deactivate	= false;

		// de- / activate nodes
		for (var gt_nid in this.nodes)
		{
			gt_node	= this.nodes[gt_nid];

			if (conversation == "##all##")
				gt_deactivate	= gt_node.isDeactivated(true);
			else
				gt_deactivate	= gt_node.getConversation() != conversation;

			if (gf_isset(gv_objects_nodes[gt_node.id]))
			{
				if (gt_deactivate && gv_objects_nodes[gt_node.id].deactive == false)
					gv_objects_nodes[gt_node.id].deactivate();
				else if (gv_objects_nodes[gt_node.id].deactive == true)
					gv_objects_nodes[gt_node.id].activate();
			}
		}

		// de- / activate edges
		for (var gt_eid in this.edges)
		{
			gt_edge	= this.edges[gt_eid];
			gt_node	= gf_isset(this.nodes["n" + gt_edge.getStart()]) ? this.nodes["n" + gt_edge.getStart()] : null;

			if (conversation == "##all##")
				gt_deactivate	= gt_edge.isDeactivated();
			else
				gt_deactivate	= gt_node == null ? true : gt_node.getConversation() != conversation;

			gt_eid	= gt_eid.substr(1);
			if (gf_isset(gv_objects_edges[gt_eid]))
			{
				if (gt_deactivate && gv_objects_edges[gt_eid].deactive == false)
					gv_objects_edges[gt_eid].deactivate();
				else if (gv_objects_edges[gt_eid].deactive == true)
					gv_objects_edges[gt_eid].activate();
			}
		}
	};

	/**
	 * Clears the current selection by calling GCmacro.selectNothing() and updates selectedEdge with given id.
	 *
	 * @param {int} id The id of the edge.
	 * @returns {void}
	 */
	this.selectEdge = function (id)
	{
		if (gf_isset(this.edges["e" + id]))
		{
			this.selectNothing();
			this.selectedEdge = id;
		}
	};

	/**
	 * When connectMode is set to true a new edge is created from the node with the id stored in startNode to the node with the given id,
	 * but only when no edge already exists from startNode to id.
	 * When connectMode is set to false the current selection is cleared using GCmacro.selectNothing() and selectedNode is set to id.
	 *
	 * @param {int} id The id of the node
	 * @returns {void}
	 */
	this.selectNode = function (id)
	{
    	var intId = parseInt(id, 10);
		if (!gf_isset(this.nodes["n" + id]) && gf_isset(this.nodeIDs[id]))
		{
			id = this.nodeIDs[id];
		}

		if (gf_isset(this.nodes["n" + id]))
		{
			// on connectMode == true -> create a new edge
			if (this.connectMode === true && this.startNode != null)
			{
				// no edge from the startNode to itself
				if (this.startNode != id)
				{
					var gt_result		= gf_checkCardinality(this, this.startNode, id, "exitcondition", "", "add");
					var gt_edgeExists	= gt_result.type == null;
					var gt_edgeType		= gt_result.type;

					// create the edge if it does not already exist
					if (gt_edgeExists == false)
					{
						this.createEdge(this.startNode, id, gt_edgeType);
						this.connectNodes();
					}
				}
			}
			else if (this.setStartEdgeMode === true && this.startEdge)
			{
				// no edges must create loops and start node type must be identical
				if (this.startEdge.start != intId && this.startEdge.end != intId
					 && this.startEdge.type == this.getEdge(intId).type)
				{
					this.startEdge.setStart(intId);
					this.setStartEdge();
          gv_graph.drawBehavior();
				}
			}
			else if (this.setEndEdgeMode === true && this.endEdge)
			{
				// no edges must create loops and start node type must be identical
				if (this.endEdge.start != intId && this.endEdge.end != intId)
				{
					this.endEdge.setEnd(intId);
					this.setEndEdge();
          gv_graph.drawBehavior();
				}
			}
			else
			{
				this.selectNothing();
				this.selectedNode = id;
			}
		}
	};

	/**
	 * Resets selectedEdge, selectedNode and connectMode to false.
	 *
	 * @returns {void}
	 */
	this.selectNothing = function ()
	{
		this.selectedEdge	= null;
		this.selectedNode	= null;
		this.connectMode	= false;
		this.setStartEdgeMode	= false;
		this.setEndEdgeMode	= false;
	};

	/**
	 * When setEndEdge() is called setEndEdgeMode is toggled.
	 * When setEndEdgeMode is set to true the setEndEdgeMode is changed to false.
	 * When it is false setEndEdgeMode is set to true and the currently selected edge is backed up as the endEdge.
	 *
	 * @returns {void}
	 */
	this.setEndEdge = function ()
	{
		if (this.setEndEdgeMode === true)
		{
			this.setEndEdgeModeonnectMode	= false;
			this.endEdge		= null;
		}
		else
		{
			this.setEndEdgeMode	= true;
			this.endEdge		= this.getEdge(this.selectedEdge);
		}
	};

	/**
	 * When setStartEdge() is called setStartEdgeMode is toggled.
	 * When setStartEdgeMode is set to true the setStartEdgeMode is changed to false.
	 * When it is false setStartEdgeMode is set to true and the currently selected edge is backed up as the startEdge.
	 *
	 * @returns {void}
	 */
	this.setStartEdge = function ()
	{
		if (this.setStartEdgeMode === true)
		{
			this.setStartEdgeMode	= false;
			this.startEdge		= null;
		}
		else
		{
			this.setStartEdgeMode	= true;
			this.startEdge		= this.getEdge(this.selectedEdge);
		}
	};

	/**
	 * Updates the information of the edge with the id stored in selectedEdge by calling the setText (text) and setRelatedSubject (relatedSubject) methods of GCedge.
	 * Redraws the graph.
	 *
	 * @param {Object} values An object (gt_values) passed by the updateEdge method from GCcommunication.
	 * @returns {void}
	 */
	this.updateEdge = function (values)
	{
		if (this.selectedEdge != null && gf_isset(this.edges["e" + this.selectedEdge], values))
		{
			var gt_edge		= this.edges["e" + this.selectedEdge];

			// (gt_text, gt_type, gt_relatedSubject, gt_timeout, gt_optional, gt_values);
			// (text, type, relatedSubject, timeout, optional, parameters)

			// read current settings
			var gt_curRelatedSubject	= gt_edge.getRelatedSubject();
			var gt_curType				= gt_edge.getType();
			var gt_curText				= gt_edge.getText();
			var gt_tmpEdge				= null;
			var gt_startNodeType		= gt_edge.getTypeOfStartNode();

			// collect new values
			var gt_text				= gf_isset(values.text)				? values.text				: "";
			var gt_relatedSubject	= gf_isset(values.relatedSubject)	? values.relatedSubject		: "";
			var gt_type				= gf_isset(values.type)				? values.type				: "label";
			var gt_timeout			= gf_isset(values.timeout)			? values.timeout			: "";
			var gt_exception		= gf_isset(values.exception)		? values.exception			: "";
			var gt_optional			= gf_isset(values.optional)			? values.optional			: false;
			var gt_messageTypeId	= gf_isset(values.messageType)		? values.messageType		: "";
			var gt_priority			= gf_isset(values.priority)			? values.priority			: "1";
			var gt_manualTimeout	= gf_isset(values.manualTimeout)	? values.manualTimeout		: false;
			var gt_storeVariable	= gf_isset(values.variable)			? values.variable			: "";
			var gt_storeVariableNew	= gf_isset(values.variableText)		? values.variableText		: "";
			var gt_correlationId	= gf_isset(values.correlationId)	? values.correlationId		: "";
			var gt_comment			= gf_isset(values.comment)			? values.comment			: "";
			var gt_transportMethod	= gf_isset(values.transportMethod)	? values.transportMethod	: ["internal"];

			if (gt_startNodeType == "send" || gt_startNodeType == "receive")
			{
				gt_text	= gv_graph.addMessageType(gt_text, gt_messageTypeId);
			}

			if (gf_isset(gt_relatedSubject.variable, gt_relatedSubject.variableText, gt_relatedSubject.useVariable))
			{
				if (gt_relatedSubject.useVariable === true)
					gt_relatedSubject.variable	= this.parent.addVariable(gt_relatedSubject.variableText, gt_relatedSubject.variable);
				else
					gt_relatedSubject.variable = "";
			}

			if (gf_isset(gt_relatedSubject.createNewSubject, gt_relatedSubject.createNewName, gt_relatedSubject.createNewRole))
			{
				if (gt_relatedSubject.createNewSubject === true && gt_relatedSubject.createNewName != "")
				{
					var gt_newSubjectRole	= gt_relatedSubject.createNewRole == "" ? gv_graph.getProcessText("noRole") : gt_relatedSubject.createNewRole;
					var gt_newSubjectId		= "Subj" + ++gv_graph.nodeCounter;

					gv_graph.addSubject(gt_newSubjectId, gt_relatedSubject.createNewName);
					gv_graph.subjects[gt_newSubjectId].setRole(gt_newSubjectRole);

					gt_relatedSubject.id	= gt_newSubjectId;
				}
			}

			// manipulate edge
			gt_edge.setPriority(gt_priority);
			gt_edge.setManualTimeout(gt_manualTimeout);
			gt_edge.setText(gt_text);
			gt_edge.setType(gt_type);
			gt_edge.setCorrelationId(gt_correlationId);
			gt_edge.setVariable(this.parent.addVariable(gt_storeVariableNew, gt_storeVariable));
			gt_edge.setRelatedSubject(gt_relatedSubject);
			gt_edge.setOptional(gt_optional);
			gt_edge.setComment(gt_comment);
			gt_edge.setTransportMethod(gt_transportMethod);

			if (gt_type == "timeout")
			{
				gt_edge.setTimer(gt_timeout);
			}

			if (gt_type == "cancelcondition")
			{
				gt_edge.setException(gt_exception);
			}

			if (!gv_noRedraw)
				this.draw();
		}
	};

	/**
	 * Updates the information of the node with the id that is stored in selectedNode by calling the setText (text), setType (type), setStart (start) and the setEnd (end) methods of GCnode.
	 * Redraws the graph.
	 *
	 * @param {Object} values The values array passed by the GCcommunication::updateNode() function.
	 * @returns {void}
	 */
	this.updateNode = function (values)
	{
		if (this.selectedNode != null && gf_isset(this.nodes["n" + this.selectedNode], values) && (this.id == "##main##" || "n" + this.selectedNode != "n0"))
		{
			gt_node = this.nodes["n" + this.selectedNode];
			
			var gt_text								= gf_isset(values.text)								? values.text								: "";
			var gt_isAutoExecute			= gf_isset(values.autoExecute)				? values.autoExecute				: false;
			var gt_isStart						= gf_isset(values.isStart)						? values.isStart						: false;
			var gt_type								= gf_isset(values.type)								? values.type								: "";
			var gt_isMajorStartNode		= gf_isset(values.isMajorStartNode)		? values.isMajorStartNode		: false;
			var gt_conversation				= gf_isset(values.conversation)				? values.conversation				: "";
			var gt_conversationText		= gf_isset(values.conversationText)		? values.conversationText		: "";
			var gt_variable						= gf_isset(values.variable)						? values.variable						: "";
			var gt_options						= gf_isset(values.options)						? values.options						: {};
			var gt_varMan							= gf_isset(values.varMan)							? values.varMan							: {};
			var gt_createSubjects			= gf_isset(values.createSubjects)			? values.createSubjects			: {};
			var gt_chooseAgentSubject = gf_isset(values.chooseAgentSubject) ? values.chooseAgentSubject : "";
			var gt_macro							= gf_isset(values.macro)							? values.macro							: "";
			var gt_blackboxname				= gf_isset(values.blackboxname)				? values.blackboxname				: "";
			var gt_comment						= gf_isset(values.comment)						? values.comment						: "";

			// check option entries
			if (!gf_isset(gt_options.message))
				gt_options.message	= "*";

			if (!gf_isset(gt_options.subject))
				gt_options.subject	= "*";

			if (!gf_isset(gt_options.correlationId))
				gt_options.correlationId	= "*";

			if (!gf_isset(gt_options.conversation))
				gt_options.conversation	= "*";

			if (!gf_isset(gt_options.state))
				gt_options.state	= "";

			if (gt_type == "r")
				gt_type = "receive";

			if (gt_type == "s")
				gt_type = "send";

			if (gf_isset(gt_varMan.storevar, gt_varMan.storevarText))
			{
				gt_varMan.storevar	= this.parent.addVariable(gt_varMan.storevarText, gt_varMan.storevar);
			}

			if (gf_isset(gt_createSubjects.storevar, gt_createSubjects.storevarText))
			{
				gt_createSubjects.storevar	= this.parent.addVariable(gt_createSubjects.storevarText, gt_createSubjects.storevar);
			}

			gt_node.setText(gt_text);
			gt_node.setType(gt_type);
			gt_node.setMajorStartNode(gt_isMajorStartNode);
			gt_node.setStart(gt_isStart);
			gt_node.setAutoExecute(gt_isAutoExecute);
			gt_node.setEnd(gt_type == "end");
			gt_node.setOptions(gt_options);
			gt_node.setVariable(gt_variable);
			gt_node.setConversation(gv_graph.addConversation(gt_conversationText, gt_conversation));
			gt_node.setVarMan(gt_varMan);
			gt_node.setCreateSubjects(gt_createSubjects);
			gt_node.setChooseAgentSubject(gt_chooseAgentSubject);
			gt_node.setComment(gt_comment);

			// macro
			if (gt_macro == "##createNew##" && gf_isset(values.macroText))
				gt_macro = this.parent.createMacro(values.macroText);

			if (gt_macro != "")
				gt_node.setMacro(gt_macro);


			// blackboxname

			if (gt_blackboxname == "##createNew##" && gf_isset(values.blackboxnameText))
				gt_blackboxname = values.blackboxnameText;

			gt_node.setBlackboxname(gt_blackboxname);

			if (!gv_noRedraw)
				this.draw();
		}
	};
}
