/**
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
 * Represents the communication view and routes all GUI requests either to its subjects or to the selected internal behavior.
 * 
 * @class The graph information for the communication view.
 * @returns {void}
 */
function GCcommunication ()
{	
	/**
	 * Array of messages between subjects.
	 * This array is only used by GCcommunication.draw () which collects all messages sent between subjects and stores them into the messages array in the following form:
	 * messages[fromSubjectId][toSubjectId][lastIndex] = messageText
	 * 
	 * @type Object
	 */
	this.messages	= {};	// 3-dim Array [from][to][]
	
	/**
	 * This counter is used to create a unique id for new subjects.
	 * 
	 * @type int
	 */
	this.nodeCounter		= 0;
	
	/**
	 * The currenctly selected subject-node in the communication view.
	 * 
	 * @type String
	 */
	this.selectedNode		= null;
	
	/**
	 * The currently selected subject.
	 * This attribute is used to route GUI calls to the currently loaded behavior.
	 * 
	 * @type String
	 */
	this.selectedSubject	= null;
	
	/**
	 * An array of GCsubjects.
	 * This array contains all subjects of the graph.
	 * 
	 * @type GCsubject[]
	 */
	this.subjects	= {};
	
	/**
	 * Adds a message to messages[sender][receiver][index].
	 * This method automatically initializes the array.
	 * This method is called by the GCcommunication.draw() method while collecting the messages that are sent between subjects.
	 * 
	 * @param {String} sender The sender subject.
	 * @param {String} receiver The receiver subject.
	 * @param {String} message The message sent from sender to receiver.
	 * @returns {void}
	 */
	this.addMessage = function (sender, receiver, message)
	{
		// initialize the array when the index is not set
		if (!gf_isset(this.messages[sender]))
		{
			this.messages[sender] = {};
		}
		
		// initialize the array when the index is not set
		if (!gf_isset(this.messages[sender][receiver]))
		{
			this.messages[sender][receiver] = [];	
		}
		
		// add the message to the messages array
		this.messages[sender][receiver][this.messages[sender][receiver].length]	= message;
	};
	
	/**
	 * Creates a new Subject with the given id, title and type and stores it to the subjects array.
	 * 
	 * @param {String} id The id of the subject.
	 * @param {String} title The label of the subject.
	 * @param {String} type The type of the subject. Possible values are "single", "multi", "external". (default: "single")
	 * @param {int} inputPool The size of the subject's input-pool
	 * @param {boolean} deactivated The deactivation status of the subject.
	 * @returns {void}
	 */
	this.addSubject = function (id, title, type, inputPool, deactivated)
	{
		if (gf_isset(id, title))
		{
			// initialize the canvas
			this.init();
			
			// the default subject type is "single"
			if (!gf_isset(type))
				type = "single";
			
			// create the subject
			var gt_subject = new GCsubject(id, title, type, inputPool);
				
			// apply the deactivation status to the subject
			if (gf_isset(deactivated) && deactivated === true)
				gt_subject.deactivate();
			
			// add the subject to the subjects array
			this.subjects[id] = gt_subject;
		}
	};
	
	/**
	 * Switches between the behavioral view and the communication view.
	 * When view is set to "cv" (communication view) all edges and nodes are deselected, the input fields are cleared and the graph is redrawn.
	 * When view is set to "bv" (behavioral view) the internal behavior of the subject stored in selectedSubject is loaded and the input fields are cleared.
	 * 
	 * @param {String} view The view to load. Possible values are "cv" and "bv".
	 * @returns {void}
	 */
	this.changeView = function (view)
	{
		// hook
		if (!gf_isStandAlone() && gf_functionExists(gv_functions.communication.changeViewHook))
		{
			window[gv_functions.communication.changeViewHook](view);
		}
		
		// change to the communication view
		if (view == "cv")
		{
			gf_paperChangeView("cv");
			if (gf_elementExists(gv_elements.graphCVouter))
			{
				
				if (!gf_isStandAlone() && gf_functionExists(gv_functions.communication.changeView))
				{
					window[gv_functions.communication.changeView]("cv");
				}
				else
				{
					gf_guiChangeView("cv");
				}
			
				this.selectedSubject	= null;
				this.selectedNode		= null;
				this.loadInformation(true);
				this.draw();
			}
		}
		
		// change to the behavioral view
		else if (view == "bv")
		{
			gf_paperChangeView("bv");
			if (gf_elementExists(gv_elements.graphBVouter))
			{
				if (!gf_isStandAlone() && gf_functionExists(gv_functions.communication.changeView))
				{
					window[gv_functions.communication.changeView]("bv");
				}
				else
				{
					gf_guiChangeView("bv");
				}
				
				if (gf_isset(this.subjects[this.selectedSubject]))
				{
					this.getBehavior(this.selectedSubject).selectNothing();
				}
			}
		}
	};
	
	/**
	 * When the selectedSubject attribute is set the GCbehavior.clearGraph() method of the corresponding behavior is called.
	 * When no subject is selected all attributes of GCcommunication are reset to their default values.
	 * The whole process is emptied.
	 * 
	 * @param {boolean} wholeProcess Optional parameter to clear the whole process instead of the current view.
	 * @returns {void}
	 */
	this.clearGraph = function (wholeProcess)
	{
		if (this.selectedSubject == null || (gf_isset(wholeProcess) && wholeProcess === true))
		{
			
			this.subjects	= {};
			
			this.selectedSubject	= null;
			this.selectedNode		= null;
			
			this.nodeCounter		= 0;
			
			this.draw();
		}
		
		// when a behavior is shown, call its clearGraph() method
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				this.getBehavior(this.selectedSubject).clearGraph();
			}
		}
	};
	
	/**
	 * When selectedSubject is set the GCbehavior.connectNodes() method of the loaded internal behavior is called.
	 * 
	 * @returns {void}
	 */
	this.connectNodes = function ()
	{
		if (this.selectedSubject == null)
		{
			// does not exist in cv
		}
		
		// when an internal behavior is shown, call its connectNodes() method
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				this.getBehavior(this.selectedSubject).connectNodes();
			}
		}
	};
	
	/**
	 * Creates a new process from a table containing subjects and messages sent between those subjects.
	 * 
	 * @param {String[]} subjects An array of subject names.
	 * @param {Object[]} messages A list of messages containing the attributes "message", "sender" (id of sender subject), "receiver" (id of receiver subject).
	 * @returns {void}
	 */
	this.createFromTable = function (subjects, messages)
	{
		// initialize the canvas
		this.init();
		this.clearGraph(true);
		
		// create the subjects
		for (var gt_subjectId in subjects)
		{
			var gt_subjectName	= subjects[gt_subjectId];
			this.addSubject(gt_subjectName.toLowerCase(), gt_subjectName, "single", -1, false);
		}
		
		// create the internal behaviors
		for (var gt_subjectId in subjects)
		{
			var gt_subjectName	= subjects[gt_subjectId].toLowerCase();
			var gt_behav		= this.getBehavior(gt_subjectName);
			
			// get messages
			var gt_msgS	= [];
			var gt_msgR	= [];
			
			for (var gt_msgId in messages)
			{
				var gt_msg	= messages[gt_msgId];
				if (gf_objectHasAttribute(gt_msg, ["message", "sender", "receiver"]))
				{
					if (gt_msg.sender != gt_msg.receiver)
					{
						if (gt_msg.sender == gt_subjectName)
						{
							gt_msgS[gt_msgS.length] = gt_msgId;
						}
						
						if (gt_msg.receiver == gt_subjectName)
						{
							gt_msgR[gt_msgR.length] = gt_msgId;
						}
					}
				}
			}
			
			// create the start node
			gt_behav.addNode("start", "What to do?", "action", true, false, false);
			
			// create nodes for sent messages
			if (gt_msgS.length > 0)
			{
				// add action node
				gt_behav.addNode("send", "create msg", "action", false, false, false);
				
				// add edges to start node
				gt_behav.addEdge("start", "send", "send", null, "label", false, false);
				gt_behav.addEdge("send", "start", "cancel", null, "label", false, false);
				
				// add sent messages
				for (var gt_msId in gt_msgS)
				{
					var gt_msgId	= gt_msgS[gt_msId];
					var gt_msg		= messages[gt_msgId];
					
					gt_behav.addNode("sM" + gt_msgId, "", "send", false, false, false);
					
					gt_behav.addEdge("send", "sM" + gt_msgId, "create msg", null, "label", false, false);
					gt_behav.addEdge("sM" + gt_msgId, "start", gt_msg.message, gt_msg.receiver, "message", false, false); 
				}
			}
			
			// create the end node
			gt_behav.addNode("end", "", "end", false, true, false);
			
			// connect start and end
			gt_behav.addEdge("start", "end", "end process", null, "label", false, false);
			
			// create nodes for received messages
			if (gt_msgR.length > 0)
			{
				// add receive node
				gt_behav.addNode("rcv", "", "receive", false, false, false);
				
				// add edges to start node
				gt_behav.addEdge("start", "rcv", "receive", null, "label", false, false);
				gt_behav.addEdge("rcv", "start", "cancel", null, "label", false, false);
				
				// add received messages
				for (var gt_mrId in gt_msgR)
				{
					var gt_msgId	= gt_msgR[gt_mrId];
					var gt_msg		= messages[gt_msgId];
					
					gt_behav.addNode("actM" + gt_msgId, "reaction msg " + gt_msgId, "action", false, false, false);
					
					gt_behav.addEdge("rcv", "actM" + gt_msgId, gt_msg.message, gt_msg.sender, "message", false, false);
					gt_behav.addEdge("actM" + gt_msgId, "start", "", null, "label", false, false); 
				}
			}
		}
		
		// draw the graph
		this.draw();
	};
	
	/**
	 * When selectedSubject is set the GCbehavior.createNode() method of the corresponding internal behavior is called.
	 * When no subject is selected a new subject is added using GCcommunication.addSubject(id, title) where id is the current value of the nodeCounter, prefixed with "new", and the title is the current value of the nodeCounter, prefixed with "new node".
	 * 
	 * @returns {void}
	 */
	this.createNode = function ()
	{
		// when the communication view is shown, add a new subject using addSubject()
		if (this.selectedSubject == null)
		{
			this.addSubject("new" + ++this.nodeCounter, "new node " + this.nodeCounter);
			this.draw();
		}
		
		// when an internal behavior is loaded, call its createNode method
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				return this.getBehavior(this.selectedSubject).createNode();
			}
		}
	};
	
	/**
	 * De- / activate the currently selected edge in the behavioral view.
	 * 
	 * @returns {void}
	 */
	this.deactivateEdge = function ()
	{
		if (this.selectedSubject == null)
		{
			// does not exist in cv
		}
		
		// when an internal behavior is loaded, call its deactivateEdge() method to deactivate the currently selected edge
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				this.getBehavior(this.selectedSubject).deactivateEdge();
			}
		}
	};
	
	/**
	 * De- / activate the currently selected node (either in the communication view or the behavioral view).
	 * 
	 * @returns {void}
	 */
	this.deactivateNode = function ()
	{
		// when the communication view is shown
		if (this.selectedSubject == null)
		{
			var gt_nodeId	= this.selectedNode;
			
			if (gt_nodeId != null)
			{
				// when the currently selected node is deactivated, activate it
				if (this.subjects[gt_nodeId].isDeactivated())
				{
					// update the deactivation status of the GCsubject
					this.subjects[gt_nodeId].activate();
					
					// mark the subject-node as active on the graph
					gv_objects_nodes[gt_nodeId].activate();
				}
				
				// when the currently selected node is activated, deactivate it
				else
				{
					// update the deactivation status of the GCsubject
					this.subjects[gt_nodeId].deactivate();
					
					// mark the subject-node as deactive on the graph
					gv_objects_nodes[gt_nodeId].deactivate();
				}
			}
		}
		
		// when the behavioral view is shown, call its deactivateNode() method to de- / activate the node
		else
		{			
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				this.getBehavior(this.selectedSubject).deactivateNode();
			}
		}
	};
	
	/**
	 * When a subject is selected the GCbehavior.deleteEdge(id) method of its behavior is called and the input fields for the edge are cleared.
	 * 
	 * @returns {void}
	 */
	this.deleteEdge = function ()
	{
		if (this.selectedSubject == null)
		{
			// does not exist in cv
		}
		
		// when an internal behavior is loaded, call its deleteEdge() method
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				this.getBehavior(this.selectedSubject).deleteEdge();
				this.loadInformation(true);
			}
		}
	};
	
	/**
	 * When a subject is selected the GCbehavior.deleteNode(id) method of its behavior is called and the input fields for nodes are cleared.
	 * When selectedSubject is null and a node in the communication view is selected (selectedNode != null) the subject and its behavior are deleted from the process.
	 * The edges of all nodes that have the deleted subject stored as their related subject are also removed. Both graphs are redrawn.
	 * 
	 * @returns {void}
	 */
	this.deleteNode = function ()
	{
		// when the communication view is loaded, the subject and its behavior are deleted
		if (this.selectedSubject == null)
		{
			if (this.selectedNode != null)
			{				
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
					
				if (this.selectedSubject == this.selectedNode)
				{
					this.selectedSubject = null;
				}
				this.selectedNode = null;
				
				this.draw();
			}
		}
		
		// when an internal behavior is loaded, call its deleteNode() method to delete the currently selected node
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				this.getBehavior(this.selectedSubject).deleteNode();
				this.loadInformation(true);
			}
		}
	};
	
	/**
	 * Draws the graph for the subject-interaction-view.
	 * Before passing all information to the appropriate drawing functions this method collects all necessary data – like the messages that are exchanged between subjects.
	 * 
	 * @returns {void}
	 */	
	this.draw = function ()
	{
		if (gv_cv_paper == null)
			this.init();
			
		if (gv_cv_paper != null)
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
					var gt_type					= gt_edge.getType();
					
					if (gt_startNode != null && gt_endNode != null && gt_relatedSubject != null && gt_text != "" && gt_type == "message")
					{
						if (gf_isset(this.subjects[gt_relatedSubject]) && gt_startNode.getType() == "send")
						{
							this.addMessage(gt_bi, gt_relatedSubject, gt_text);
						}
					}
				}
			}
			
			// clear graph
			gv_graph_cv.init();
			
			// add subjects
			for (var gt_sid in this.subjects)
			{
				gv_graph_cv.addSubject (this.subjects[gt_sid], this.selectedNode == gt_sid);
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
							gv_graph_cv.addMessage (gt_fromId, gt_toId, gt_text);
						}
					}
				}
			}
			
			gv_graph_cv.drawGraph();
		}
	};
	
	/**
	 * Draws the behavior of the subject with the given id by calling the GCbehavior.draw() method of the behavior.
	 * 
	 * @param {String} [id] The id of the subject to load the internal behavior for.
	 * @returns {void}
	 */
	this.drawBehavior = function (id)
	{
		// when no id is given, use the currently selectedNode
		if (!gf_isset(id))
		{
			id = this.selectedNode;
		}
		
		var gt_behavior = this.getBehavior(id);
		
		if (gt_behavior != null)
		{
			this.changeView("bv");
			gt_behavior.draw();
			this.selectedSubject = id;
		}
	};
	
	/**
	 * Returns the behavior of the subject with the given id or null if the subject does not exist.
	 * 
	 * @param {String} id The id of the subject.
	 * @returns {GCbehavior} The behavior of the given subject or null if the subject does not exist.
	 */
	this.getBehavior = function (id)
	{
		if (gf_isset(this.subjects[id]))
			return this.subjects[id].getBehavior();
		return null;
	};
	
	/**
	 * Returns the messages that are sent from the subject with the id that is passes in the sender parameter, to the subject whose id is passed in the receiver parameter.
	 * When no messages are sent in this direction between the two subjects an empty array is returned.
	 * 
	 * @param {String} sender The id of the sender subject.
	 * @param {String} receiver The id of the receiver subject.
	 * @returns {Array}
	 */
	this.getMessages = function (sender, receiver)
	{
		if (gf_isset(this.messages[sender]) && gf_isset(this.messages[sender][receiver]) && gf_isArray(this.messages[sender][receiver]))
		{
			return this.messages[sender][receiver];
		}
		return [];
	};
	
	/**
	 * Returns the id of the node currently selected depending on the current view.
	 * 
	 * @returns {int} The id of the selectedNode of the currently active view or null.
	 */
	this.getSelectedNode = function ()
	{
		if (this.selectedSubject == null)
		{			
			return this.selectedNode;
		}
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				return this.getBehavior(this.selectedSubject).selectedNode;
			}
		}
		return null;
	};
	
	/**
	 * Returns the IDs of all subjects of the graph.
	 * 
	 * @returns {String[]} An array of all subject IDs.
	 */
	this.getSubjectIDs = function ()
	{
		var gt_subjectIDs	= [];
		
		for (var gt_sid in this.subjects)
		{
			gt_subjectIDs[gt_subjectIDs.length]		= gt_sid;
		}
		
		return gt_subjectIDs;
	};
	
	/**
	 * Returns the names of all subjects of the graph.
	 * 
	 * @returns {String[]} An array of all subject names.
	 */
	this.getSubjectNames = function ()
	{
		var gt_subjectNames	= [];
		
		for (var gt_sid in this.subjects)
		{
			gt_subjectNames[gt_subjectNames.length]		= gf_replaceNewline(this.subjects[gt_sid].getText()).replace(/\n/gi, " ");
		}
		
		return gt_subjectNames;
	};
	
	/**
	 * Returns the subjects of the graph.
	 * 
	 * @returns {String[]} An array of all subjects.
	 */
	this.getSubjects = function ()
	{
		return this.subjects;
	};
	
	/**
	 * Calls GCcommunication.getMessages(sender, receiver) and implodes the resulting array to a string.
	 * When only one message is returned by GCcommunication.getMessages() this message is returned.
	 * When at least two messages are returned this method prefixes every message with an "li" html tag, which will be displayed as a list-bullet in the graph,
	 * indicating that multiple messages can be sent from one subject to another, and postfixed with "\n", so that every message will be printed in its own line.
	 * 
	 * @param {String} sender The id of the sender subject.
	 * @param {String} receiver The id of the receiver subject.
	 * @returns {String} The list of messages from sender to receiver imploded to one String.
	 */
	this.implodeMessages = function (sender, receiver)
	{
		var gt_messages = this.getMessages(sender, receiver);
		var gt_implodedMessages = "";
		
		// if only one message is sent from sender to receiver return this message as a String
		if (gt_messages.length == 1)
		{
			gt_implodedMessages = gt_messages[0];
		}
		
		// if more than one message is sent from sender to receiver return the messages imploded to a string
		else if (gt_messages.length > 0)
		{
			for (var gt_mi = 0; gt_mi < gt_messages.length; gt_mi++)
			{
				// add a new line character to each message
				if (gt_mi > 0)
					gt_implodedMessages += "\n";
				
				// prefix every message with <li> which will be replaced by a list-bullet
				gt_implodedMessages += "<li>" + gt_messages[gt_mi];
			}
		}
		
		return gt_implodedMessages;
	};
	
	/**
	 * Initialize the GCcommunication instance.
	 * 
	 * @returns {void}
	 */
	this.init = function ()
	{
		if (gv_bv_paper == null && gv_cv_paper == null)
		{
			// create the Raphael Paper object for the communication view
			if (gf_elementExists(gv_elements.graphCVouter))
				gv_cv_paper = Raphael(gv_elements.graphCVouter, gv_paperSizes.cv_width, gv_paperSizes.cv_height);
				
			// create the Raphael Paper object for the behavioral view
			if (gf_elementExists(gv_elements.graphBVouter))
				gv_bv_paper = Raphael(gv_elements.graphBVouter, gv_paperSizes.bv_width, gv_paperSizes.bv_height);
				
			// load the communication view
			if (gv_cv_paper != null)
				this.changeView("cv");
		}
	};
	
	/**
	 * When a relatedSubject is selected from a drop down this method is called.
	 * It loads all messages that are sent from the selected subject to the current subject or those sent from the current subject to the selected subject.
	 * The messages are added to gv_elements.inputEdgeMessage.
	 * 
	 * @returns {void}
	 */
	this.loadEdgeMessages = function ()
	{
		if (!gf_isStandAlone() && gf_functionExists(gv_functions.communication.loadEdgeMessages))
		{
			window[gv_functions.communication.loadEdgeMessages]();
		}
		else
		{
			gf_guiLoadEdgeMessages();
		}
	};
	
	/**
	 * Loads a process graph from a given JSON representation stored in the database.
	 * 
	 * @param {String} jsonString The JSON representation of a process.
	 * @returns {void}
	 */
	this.loadFromJSON = function (jsonString)
	{
		var gt_jsonObject = JSON.parse(jsonString);
		
		// 1. create subjects (replace <br /> by \n)
		for (var gt_subjectId in gt_jsonObject)
		{
			var gt_subject = gt_jsonObject[gt_subjectId];
			
			// provide compatibility to previous versions:
			var gt_inputPool	= gf_isset(gt_subject.inputPool) ? gt_subject.inputPool : -1;
			
			this.addSubject(gt_subject.id, gf_replaceNewline(gt_subject.name), gt_subject.type, gt_inputPool, gt_subject.deactivated);
		}
		
		// 2. add nodes + edges
		for (var gt_subjectId in gt_jsonObject)
		{
			var gt_subject	= gt_jsonObject[gt_subjectId];
			var gt_behav	= this.getBehavior(gt_subject.id);
			
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
					gt_behav.addEdge(gt_edge.start, gt_edge.end, gf_replaceNewline(gt_edge.text), gt_edge.target, gt_edge.type, gt_edge.deactivated, gt_edge.optional);
				}
			}
			
			// set the nodeCounter to avoid problems with new nodes
			gt_behav.nodeCounter = gt_subject.nodeCounter;
		}
		
		// draw the graph
		this.draw();
	};
	
	/**
	 * When clear is set to true all input fields are emptied.
	 * In all other cases the input fields will be filled with information about the currently selected node, depending on selectedSubject and selectedNode.
	 * When a subject is selected, the data is loaded from the selected node in the behavioral view.
	 * When selectedSubject is null, the information of the selected node of the communication view are loaded.
	 * 
	 * @param {boolean} clear When set to true all input fields are emptied.
	 * @returns {void}
	 */
	this.loadInformation = function (clear)
	{		
		// empty all input fields
		if (gf_isset(clear) && clear == true)
		{
			if (!gf_isStandAlone() && gf_functionExists(gv_functions.communication.clearInputFields))
			{
				window[gv_functions.communication.clearInputFields]();
			}
			else
			{
				gf_guiClearInputFields();
			}
		}
		
		// load the information of a selected node
		else
		{			
			this.loadInformation(true);
			if (!gf_isStandAlone() && gf_functionExists(gv_functions.communication.toggleNEForms))
			{
				window[gv_functions.communication.toggleNEForms]("n");
			}
			else
			{
				gf_guiToggleNEForms("n");
			}
		
			// when the communication view is shown load the information of the currently selected subject-node
			if (this.selectedSubject == null)
			{
				if (this.selectedNode != null && gf_isset(this.subjects[this.selectedNode]))
				{
					var gt_subject = this.subjects[this.selectedNode];		
					
					if (!gf_isStandAlone() && gf_functionExists(gv_functions.communication.displaySubject))
					{
						window[gv_functions.communication.displaySubject](gt_subject);
					}
					else
					{
						gf_guiDisplaySubject(gt_subject);
					}
				}
			}
			
			// when a behavioral view is shown the information of the currently selected node are loaded
			else
			{
				if (gf_isset(this.subjects[this.selectedSubject]))
				{
					var gt_node = this.getBehavior(this.selectedSubject).getNode();
					
					if (!gf_isStandAlone() && gf_functionExists(gv_functions.communication.displayNode))
					{
						window[gv_functions.communication.displayNode](gt_node);
					}
					else
					{
						gf_guiDisplayNode(gt_node);
					}
				}
			}
		}
	};
	
	/**
	 * When a subject is selected the information about the currently selected edge at the behavioral view is loaded and filled into the input fields.
	 * 
	 * @returns {void}
	 */
	this.loadInformationEdge = function ()
	{
		if (this.selectedSubject == null)
		{
			// not available in cv
		}
		
		// when an internal behavior is loaded, load the information of the currently seleccted edge
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				if (!gf_isStandAlone() && gf_functionExists(gv_functions.communication.toggleNEForms))
				{
					window[gv_functions.communication.toggleNEForms]("e");
				}
				else
				{
					gf_guiToggleNEForms("e");
				}
				
				var gt_edge = this.getBehavior(this.selectedSubject).getEdge();
				var gt_node = this.getBehavior(this.selectedSubject).getNode(gt_edge.getStart());
				var gt_type	= gf_isset(gt_node) ? gt_node.getType() : "";
				
				if (!gf_isStandAlone() && gf_functionExists(gv_functions.communication.displayEdge))
				{
					window[gv_functions.communication.displayEdge](gt_edge, gt_type);
				}
				else
				{
					gf_guiDisplayEdge(gt_edge, gt_type);
				}
			}
		}
	};
	
	/**
	 * This method returns an array containing the whole graph.
	 * 
	 * @returns {Array} A simplified representation of the graph.
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
										deactivated: this.subjects[gt_sid].isDeactivated(),
										inputPool: this.subjects[gt_sid].getInputPool()};
			
			var gt_behav = this.subjects[gt_sid].getBehavior();
			var gt_nodes = gt_behav.getNodes();
			var gt_edges = gt_behav.getEdges();
			var gt_newNodes = [];
			var gt_newEdges = [];
			
			// transform the behavior's nodes
			for (var gt_nid in gt_nodes)
			{				
				var gt_node = gt_nodes[gt_nid];
				gt_newNodes[gt_newNodes.length] = {
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
						gt_newEdges[gt_newEdges.length] = {
								start:	gt_edgeStartNode.getId(),
								end:	gt_edgeEndNode.getId(),
								text:	gt_edge.getText(true),
								type:	gt_edge.getType(),
								target: gt_relatedSubject == null ? "" : gt_relatedSubject,
								deactivated:	gt_edge.isDeactivated(),
								optional:		gt_edge.isOptional()
						};
					}
				}
			}
			
			gt_array[gt_arrayIndex].nodes		= gt_newNodes;
			gt_array[gt_arrayIndex].edges		= gt_newEdges;
			gt_array[gt_arrayIndex].nodeCounter	= gt_behav.nodeCounter;
		}
		
		return gt_array;
	};
	
	/**
	 * Returns the complete graph in JSON format.
	 * 
	 * @returns {String} The complete graph in JSON format.
	 */
	this.saveToJSON = function ()
	{
		return JSON.stringify(this.save()).replace(/\\n/gi, "<br />");
	};
	
	/**
	 * This method is not available at the moment.
	 * Save the graph as a pdf.
	 * 
	 * @returns {void}
	 */
	this.saveToPDF = function ()
	{
		// TODO
	};
	
	/**
	 * This method is not available at the moment.
	 * Save the graph as a SVG.
	 * 
	 * @returns {void}
	 */
	this.saveToSVG = function ()
	{
		// TODO
		// start with the most left and highest element, not with 0/0 and end with the most right and lowest element, not with width/height  
		
		// var uriContent	= "data:image/svg+xml," + encodeURIComponent(svg);
		// var newWindow	= window.open(uriContent, "test window");
	};
	
	/**
	 * When selectedSubject is set the GCbehavior.selectEdge(id) method of the currently active behavior is called and
	 * GCcommunication.loadInformationEdge() is used to display the information of the selected edge.
	 * 
	 * @param {int} id The id of the edge to select.
	 * @returns {void}
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
	};
	
	/**
	 * When a subject is selected the id will be passed to the GCbehavior.selectNode(id) method of the current behavior.
	 * When selectedSubject is null the selectedNode of the communication view is updated and the graph is redrawn.
	 * In both cases the GCcommunication.loadInformation(clear) is called without the clear parameter.
	 * 
	 * @param {String|int} id The id of the node to select. This will be of type int for nodes on the behavioral view and String for nodes on the communication view.
	 * @returns {void}
	 */
	this.selectNode = function (id)
	{
		// if communication view is shown, select the subject-node
		if (this.selectedSubject == null)
		{			
			if (gf_isset(this.subjects[id]))
			{
				this.selectNothing();
				this.selectedNode = id;
				
				if (!gf_isStandAlone() && gf_functionExists(gv_functions.communication.updateListOfSubjects))
				{
					window[gv_functions.communication.updateListOfSubjects]();
				}
			}
		}
		
		// if behavioral view is shown, select the node in the behavioral view
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				this.getBehavior(this.selectedSubject).selectNode(id);
			}
		}
		this.loadInformation();
	};
	
	/**
	 * Clears the selectedEdge and selectedNode of the communication view.
	 * 
	 * @returns {void}
	 */
	this.selectNothing = function ()
	{
		this.selectedNode	= null;
		this.loadEdgeMessages();
	};
	
	/**
	 * When selectedSubject is set the information from the input fields is loaded and passed to the GCbehavior.updateEdge(text, relatedSubject) method of the current behavior.
	 * 
	 * @returns {void}
	 */
	this.updateEdge = function ()
	{
		if (this.selectedSubject == null)
		{
			// not available in cv
		}
		
		// if an internal behavior is loaded, call its updateEdge() method and pass the information
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				// read information from fields and pass to bv
				var gt_values	= {};
				if (!gf_isStandAlone() && gf_functionExists(gv_functions.communication.readEdge))
				{
					gt_values	= window[gv_functions.communication.readEdge]();
				}
				else
				{
					gt_values	= gf_guiReadEdge();
				}
				
				var gt_text				= gf_isset(gt_values.text)				? gt_values.text			: "";
				var gt_relatedSubject	= gf_isset(gt_values.relatedSubject)	? gt_values.relatedSubject	: "";
				var gt_type				= gf_isset(gt_values.type)				? gt_values.type			: "label";
				var gt_timeout			= gf_isset(gt_values.timeout)			? gt_values.timeout			: "";
				
				this.getBehavior(this.selectedSubject).updateEdge(gt_text, gt_type, gt_relatedSubject, gt_timeout);
				this.loadInformationEdge();
			}
		}
	};
	
	/**
	 * When selectedSubject is set the input fields are read and the information is passed to the GCbehavior.updateNode(text, startEnd, type) method of the current behavior.
	 * When no subject is selected the input fields are read and the subject with the id stored in selectedNode is updated.
	 * When the id is changed the id is also changed on all messages that contain this subject as the related subject.
	 * The graph for the communication view is redrawn and the graph of the selected behavior is redrawn if one is loaded.
	 * 
	 * @returns {void}
	 */
	this.updateNode = function ()
	{
		// if the communication view is shown, update the selected subject node
		if (this.selectedSubject == null)
		{
			if (this.selectedNode != null && gf_isset(this.subjects[this.selectedNode]))
			{
				var gt_subject = this.subjects[this.selectedNode];					
								
				var gt_values	= {};
					
				if (!gf_isStandAlone() && gf_functionExists(gv_functions.communication.readSubject))
				{
					gt_values	= window[gv_functions.communication.readSubject]();
				}
				else
				{
					gt_values	= gf_guiReadSubject();
				}
				
				var gt_text			= gf_isset(gt_values.text)		? gt_values.text		: "";
				var gt_id			= gf_isset(gt_values.id)		? gt_values.id			: "";
				var gt_type			= gf_isset(gt_values.type)		? gt_values.type		: "";
				var gt_inputPool	= gf_isset(gt_values.inputPool)	? gt_values.inputPool	: "";
				
					gt_type	= gt_type != "" ? gt_type : gt_subject.getType();
				
				// allow the change of the label even though the id is emtpy
				if (gt_id.replace(" ", "") == "")
				{
					gt_id = gt_subject.getId();
				}
				
				if (gt_text.replace(" ", "") != "" && gt_id.replace(" ", "") != "")
				{
				
					gt_subject.setText(gt_text);
					gt_subject.setType(gt_type);
					gt_subject.setInputPool(gt_inputPool);
					
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
						
						if (this.selectedSubject == this.selectedNode)
						{
							this.selectedSubject = gt_id;
						}
					}
					this.draw();
				}
			}
		}
		
		// if behavior is selected, call the GCbehavior.updateNode() method and pass the information from the input fields
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				// read the fields' values and pass to the bv
				var gt_values	= {};
					
				if (!gf_isStandAlone() && gf_functionExists(gv_functions.communication.readNode))
				{
					gt_values	= window[gv_functions.communication.readNode]();
				}
				else
				{
					gt_values	= gf_guiReadNode();
				}

				var gt_type		= "normal";
				var gt_text		= gf_isset(gt_values.text)		? gt_values.text	: "";
				var gt_isStart	= gf_isset(gt_values.isStart)	? gt_values.isStart	: false;
				var gt_type2	= gf_isset(gt_values.type2)		? gt_values.type2	: "";
				
				if (gt_type2 == "r")
					gt_type2 = "receive";
					
				if (gt_type2 == "s")
					gt_type2 = "send";
					
				if (gt_isStart === true)
					gt_type = "start";					

				if (gt_type2 == "end")
					gt_type	= "end";
				
				this.getBehavior(this.selectedSubject).updateNode(gt_text, gt_type, gt_type2);
				this.loadInformation();
			}
		}
	};
}