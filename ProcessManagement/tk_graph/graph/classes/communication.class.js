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
 * Represents the communication view and routes all GUI requests either to its subjects or to the selected internal behavior.
 *
 * @class The graph information for the communication view.
 * @returns {void}
 */
function GCcommunication ()
{
	/**
	 *
	 * Initialized with 0.
	 * This counter is used to give every conversation an unique id.
	 * The counter is increased with every conversation added.
	 *
	 * @type int
	 */
	this.conversationCounter	= 0;

	/**
	 * List of conversations used in the process.
	 *
	 * @type Object
	 */
	this.conversations	= {};

	/**
	 * Array of messages between subjects.
	 * This array is only used by GCcommunication.draw () which collects all messages sent between subjects and stores them into the messages array in the following form:
	 * messages[fromSubjectId][toSubjectId][lastIndex] = messageText
	 *
	 * @type Object
	 */
	this.messages	= {};	// 3-dim Array [from][to][]

	/**
	 * Message types available within the process.
	 *
	 * @type Object
	 */
	this.messageTypes	= {};

	/**
	 * Counter for unique IDs for message types.
	 *
	 * @type int
	 */
	this.messageTypeCounter	= 0;

	/**
	 * This counter is used to create a unique id for new subjects.
	 *
	 * @type int
	 */
	this.nodeCounter		= 0;

	/**
	 * Flag indicating whether the graph is a process or a case (to distinguish between roles and users).
	 *
	 * @type boolean
	 */
	this.processFlag	= true;

	/**
	 * The currently selected conversation
	 *
	 * @type String
	 */
	this.selectedConversation	= "##all##";

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
 	 * Adds a new conversation to the conversations array of this process.
 	 * This method can also be used to update conversations.
 	 *
 	 * @param {String} text The name of the conversation.
 	 * @param {String} id The id of the conversation (optional, when set an update will be done)
 	 * @returns {String} The id of the inserted or updated conversation.
 	 */
	this.addConversation = function (text, id)
	{
 		if (!gf_isset(id))
			id = "##createNew##";

		var gt_conversationId	= "";
		var gt_changesDone	= false;
		if (gf_isset(id, text))
		{
			if (id.substr(0, 1) == "c")
			{
				// when changing the text of a conversation avoid duplicate conversations
				var gt_conversationExists	= false;
				for (var gt_cid in this.conversations)
				{
					if (gf_replaceNewline(this.conversations[gt_cid].toLowerCase(), " ") == gf_replaceNewline(text.toLowerCase(), " ") && gt_cid != id)
					{
						gt_conversationExists	= true;
						break;
					}
				}

				// update conversation
				if (!gt_conversationExists && gf_isset(this.conversations[id]) && this.conversations[id] != text && text != "")
				{
					this.conversations[id]	= text;
					gt_changesDone	= true;
				}
				gt_conversationId	= id;
			}

			// create new conversation
			else if (id == "##createNew##")
			{
				var gt_conversationExists	= false;

				// avoid duplicate conversations
				for (var gt_cid in this.conversations)
				{
					var gt_ch	= this.conversations[gt_cid];

					if (gf_replaceNewline(gt_ch.toLowerCase(), " ") == gf_replaceNewline(text.toLowerCase(), " "))
					{
						gt_conversationId	= gt_cid;
						gt_conversationExists	= true;
						break;
					}
				}

				// create the new conversation
				if (!gt_conversationExists && text != "")
				{
					this.conversations["c" + this.conversationCounter] = text;
					gt_conversationId	= "c" + this.conversationCounter;
					this.conversationCounter++;
					gt_changesDone = true;
				}
			}
			else
			{
				// (no conversation selected)
			}

			// publish update
			if (gt_changesDone)
				$.publish(gv_topics.general.conversations, [{action: "add", id: gt_conversationId, text: text}]);
		}

		return gt_conversationId;
	};

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
		var gt_messageArray	= this.messages[sender][receiver];
		var gt_messageFound	= false;
		for (var gt_mtid in gt_messageArray)
		{
			if (gt_messageArray[gt_mtid] == message)
			{
				gt_messageFound	= true;
				break;
			}
		}

		if (!gt_messageFound)
			this.messages[sender][receiver][this.messages[sender][receiver].length]	= message;
	};

 	/**
 	 * Adds a new messageType to the messageTypes array of this process.
 	 * This method can also be used to update messageTypes.
 	 *
 	 * @param {String} text The name of the messageType.
 	 * @param {String} id The id of the messageType (optional, when set an update will be done)
 	 * @returns {String} The id of the inserted or updated messageType.
 	 */
	this.addMessageType = function (text, id)
	{

		if (!gf_isset(id))
			id = "##createNew##";

		var gt_messageTypeId	= "";
		if (gf_isset(id, text))
		{
			if (id.substr(0, 1) == "m")
			{
				// when changing the text of a messageType avoid duplicate messageTypes
				var gt_messageTypeExists	= false;
				for (var gt_mtid in this.messageTypes)
				{
					if (gf_replaceNewline(this.messageTypes[gt_mtid].toLowerCase(), " ") == gf_replaceNewline(text.toLowerCase(), " ") && gt_mtid != id)
					{
						gt_messageTypeExists	= true;
						break;
					}
				}

				// update message type
				if (!gt_messageTypeExists && gf_isset(this.messageTypes[id]) && this.messageTypes[id] != text && text != "")
				{
					this.messageTypes[id]	= text;
				}
				gt_messageTypeId	= id;
			}

			// create new message
			else if (id == "##createNew##")
			{
				var gt_messageTypeExists	= false;

				// avoid duplicate messageTypes
				for (var gt_mtid in this.messageTypes)
				{
					var gt_mt	= this.messageTypes[gt_mtid];

					if (gf_replaceNewline(gt_mt.toLowerCase(), " ") == gf_replaceNewline(text.toLowerCase(), " "))
					{
						gt_messageTypeId	= gt_mtid;
						gt_messageTypeExists	= true;
						break;
					}
				}

				// create the new message
				if (!gt_messageTypeExists && text != "")
				{
					this.messageTypes["m" + this.messageTypeCounter] = text;
					gt_messageTypeId	= "m" + this.messageTypeCounter;
					this.messageTypeCounter++;
				}
			}
			else
			{
				// (no messageType selected)
			}
		}

		return gt_messageTypeId;
	};

	/**
	 * Creates a new Subject with the given id, title and type and stores it to the subjects array.
	 *
	 * @param {String} id The id of the subject.
	 * @param {String} title The label of the subject.
	 * @param {String} type The type of the subject. Possible values are "single", "multi", "external", "multiexternal". (default: "single")
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

			// publish the addition of the subject
			$.publish(gv_topics.general.subjects, [{action: "add", id: id}]);

			// create the subject
			var gt_subject = new GCsubject(id, title, type, inputPool);

			// apply the deactivation status to the subject
			if (gf_isset(deactivated) && deactivated === true)
				gt_subject.deactivate();

			// ad the subject to the subjects array
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
		if (!gf_isset(view))
			view = "";

		// hook
		gf_callFunc("communication.changeViewHook", null, view);

		// change to the communication view
		if (view == "cv")
		{
			gf_paperChangeView("cv");
			if (gf_elementExists(gv_elements.graphCVouter))
			{
				gf_callFunc("communication.changeView", "gf_guiChangeView", "cv");

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
				gf_callFunc("communication.changeView", "gf_guiChangeView", "bv");

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

			this.messageTypes		= {};
			this.messageTypeCounter	= 0;

			this.processFlag		= true;

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
	 * Creates a new case.
	 *
	 * @param {String} userName The name of the current user.
	 * @returns {void}
	 */
	this.createCase = function (userName)
	{
		// initialize the canvas
		this.init("bv");
		this.clearGraph(true);
		this.processFlag	= false;

		// add the "me" Subject
		this.addSubject("me", userName, "single", -1, false);

		// add the internal behavior
		var gt_behav		= this.getBehavior("me");
		gt_behav.addNode("start", "What to do?", "action", true, false, false);
		gt_behav.selectNode("start");

		// toggle bv
		gf_toggleBV();

		this.drawBehavior("me");
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

		// collect messages
		var gt_messageTypes	= {};
		for (var gt_msgId in messages)
		{
			var gt_msg	= messages[gt_msgId].message;

			if (!gf_isset(gt_messageTypes[gt_msg]))
			{
				this.messageTypes["m" + this.messageTypeCounter] = gt_msg;
				gt_messageTypes[gt_msg] = "m" + this.messageTypeCounter;
				this.messageTypeCounter++;
			}

			if (gf_isset(gt_messageTypes[gt_msg]))
			{
				messages[gt_msgId].message = gt_messageTypes[gt_msg];
			}
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
				gt_behav.addEdge("start", "send", "send", null, "exitcondition", false);
				gt_behav.addEdge("send", "start", "cancel", null, "exitcondition", false);

				// add sent messages
				for (var gt_msId in gt_msgS)
				{
					var gt_msgId	= gt_msgS[gt_msId];
					var gt_msg		= messages[gt_msgId];

					gt_behav.addNode("sM" + gt_msgId, "", "send", false, false, false);

					gt_behav.addEdge("send", "sM" + gt_msgId, "create msg", null, "exitcondition", false);
					gt_behav.addEdge("sM" + gt_msgId, "start", gt_msg.message, gt_msg.receiver, "exitcondition", false);
				}
			}

			// create the end node
			gt_behav.addNode("end", "", "end", false, true, false);

			// connect start and end
			gt_behav.addEdge("start", "end", "end process", null, "exitcondition", false);

			// create nodes for received messages
			if (gt_msgR.length > 0)
			{
				// add receive node
				gt_behav.addNode("rcv", "", "receive", false, false, false);

				// add edges to start node
				gt_behav.addEdge("start", "rcv", "receive", null, "exitcondition", false);
				gt_behav.addEdge("rcv", "start", "cancel", null, "exitcondition", false);

				// add received messages
				for (var gt_mrId in gt_msgR)
				{
					var gt_msgId	= gt_msgR[gt_mrId];
					var gt_msg		= messages[gt_msgId];

					gt_behav.addNode("actM" + gt_msgId, "reaction msg " + gt_msgId, "action", false, false, false);

					gt_behav.addEdge("rcv", "actM" + gt_msgId, gt_msg.message, gt_msg.sender, "exitcondition", false);
					gt_behav.addEdge("actM" + gt_msgId, "start", "", null, "exitcondition", false);
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
			this.addSubject("Subj" + ++this.nodeCounter + ":" + Utilities.newUUID(), "new Subject " + this.nodeCounter);
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
				// publish the removal of the subject
				$.publish(gv_topics.general.subjects, [{action: "remove", id: this.selectedNode}]);

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
				var gt_subject	= this.subjects[gt_bi];
				if (!gt_subject.isExternal() || gt_subject.getExternalType() == "interface")
				{
					var gt_behav = this.getBehavior(gt_bi);
					var gt_edges = gt_behav.getEdges();
					for (var gt_eid in gt_edges)
					{
						var gt_edge					= gt_edges[gt_eid];
						var gt_startNode			= gt_behav.getNode(gt_edge.getStart());
						var gt_endNode				= gt_behav.getNode(gt_edge.getEnd());
						var gt_relatedSubject		= gt_edge.getRelatedSubject();
						var gt_text					= gt_edge.getMessageType();
						var gt_type					= gt_edge.getType();
						var gt_conversationId			= gt_startNode	== null ? "" : gt_startNode.getConversation();
						var gt_conversationName			= gt_startNode	== null ? "" : gt_startNode.getConversation("name");

						if (gt_startNode != null && gt_endNode != null && gt_relatedSubject != null && gt_text != "" && gt_type == "exitcondition")
						{
							if (this.selectedConversation == "##conversations##")
							{
								if (gf_isset(this.subjects[gt_relatedSubject]) && (gt_startNode.getType() == "send" || gt_startNode.getType() == "receive") && gt_conversationName != null)
								{
									if (gt_relatedSubject.toLowerCase() > gt_bi.toLowerCase())
										this.addMessage(gt_bi, gt_relatedSubject, gt_conversationName);
									else
										this.addMessage(gt_relatedSubject, gt_bi, gt_conversationName);
								}
							}
							else if (this.selectedConversation == "##all##" || this.selectedConversation == gt_conversationId || this.selectedConversation == null)
							{
								if (gf_isset(this.subjects[gt_relatedSubject]) && gt_startNode.getType() == "send")
								{
									this.addMessage(gt_bi, gt_relatedSubject, gt_text);
								}

								if (gf_isset(this.subjects[gt_relatedSubject]) && gt_startNode.getType() == "receive")
								{
									this.addMessage(gt_relatedSubject, gt_bi, gt_text);
								}
							}
						}
					}
				}
			}

			// clear graph
			gv_graph_cv.init(this.selectedConversation == "##conversations##");

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

			$.publish(gv_topics.general.conversations, [{action: "load", view: "cv"}]);
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
			gf_timeReset();
			gf_timeCalc("load internal behavior");

			this.changeView("bv");
			gt_behavior.selectMacro("##main##");
			this.selectedSubject = id;

			gf_timeCalc("load internal behavior");
			gf_timePrint();

			// request an update of the conversation and macro list
			$.publish(gv_topics.general.conversations, [{action: "load", view: "bv"}]);
			$.publish(gv_topics.general.macros, [{action: "load", view: "bv"}]);
		}
	};

	/**
	 * TODO
	 * Exports either the subject interaction view or the currently selected internal behavior / macro.
	 */
	this.exportCurrent = function ()
	{
		var gt_result	= {nodes: null, edges: null};

		var gt_styles	= [];

		// 1. nodes
		var gt_nodes	= [];
		for (var gt_objectId in gv_objects_nodes)
		{
			var gt_object	= gv_objects_nodes[gt_objectId];

			if (gt_object.belongsToPath !== true)
			{
				var gt_text		= {text: gt_object.textString};
				var gt_image	= {src: ""};

				if (gt_object.textString != "")
				{
					gt_text.x					= gt_object.text.attr("x");
					gt_text.y					= gt_object.text.attr("y");
					gt_text.textAlignAttribute	= gt_object.textAlignAttribute;
				}

				if (gt_object.img != null)
				{
					gt_image.src	= gt_object.img.attr("src");
					gt_image.x		= gt_object.img.attr("x");
					gt_image.y		= gt_object.img.attr("y");
					gt_image.width	= gt_object.img.attr("width");
					gt_image.height	= gt_object.img.attr("height");
				}

				gt_nodes[gt_nodes.length]	= {
					id: 				gt_object.id,
					text:				gt_text,
					style:				this.exportCurrentAddStyle(gt_styles, gt_object.style),
					statusDependent:	gt_object.getStatusDependent(),
					shape:				gt_object.shape,
					boundaries:			gt_object.getBoundaries(),
					image:				gt_image
				};
			}
		}

		// 2. edges
		var gt_edges	= [];
		for (var gt_objectId in gv_objects_edges)
		{
			var gt_object	= gv_objects_edges[gt_objectId];
			var gt_text		= {text: ""};
			var gt_image	= {src: ""};

			if (gt_object.label.textString != "")
			{
				gt_text.text				= gt_object.label.textString;
				gt_text.x					= gt_object.label.text.attr("x");
				gt_text.y					= gt_object.label.text.attr("y");
				gt_text.textAlignAttribute	= gt_object.label.textAlignAttribute;
			}

			if (gt_object.label.img != null)
			{
				gt_image.src	= gt_object.label.img.attr("src");
				gt_image.x		= gt_object.label.img.attr("x");
				gt_image.y		= gt_object.label.img.attr("y");
				gt_image.width	= gt_object.label.img.attr("width");
				gt_image.height	= gt_object.label.img.attr("height");
			}

			gt_edges[gt_edges.length]	= {
					id: 				gt_object.id,
					segments:			gt_object.pathSegments,
					path:				gt_object.pathStr,
					style:				this.exportCurrentAddStyle(gt_styles, gt_object.style),
					statusDependent:	gt_object.getStatusDependent(),
					label:
						{
							text:				gt_text,
							style:				null, // gt_object.label.style,
							statusDependent:	gt_object.getStatusDependent(),
							shape:				gt_object.label.shape,
							boundaries:			gt_object.label.getBoundaries(),
							image:				gt_image
						}
			};
		}

		gt_result.nodes		= gt_nodes;
		gt_result.edges		= gt_edges;
		gt_result.styles	= gt_styles;

		return gt_result;
	};

	/**
	 * TODO
	 */
	this.exportCurrentAddStyle	= function (styles, style)
	{
		var gt_id		= 0;

		if (styles.length == 0)
		{
			styles[0] = style;
		}
		else
		{
			var gt_diff		= gf_stylesDiff(styles[0], style);
			var gt_found	= false;

			for (var gt_styleId in styles)
			{
				if (gf_stylesCompare(styles[gt_styleId], gt_diff))
				{
					gt_found	= true;
					gt_id		= gt_styleId;
					break;
				}
			}

			if (!gt_found)
			{
				gt_id					= styles.length;
				styles[styles.length]	= gt_diff;
			}
		}

		return gt_id;
	};

	/**
	 * TODO
	 */
	this.exportCurrentToJSON = function ()
	{
		return JSON.stringify(this.exportCurrent()).replace(/\\n/gi, "<br />");
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
	 * Returns a proper string depending on the processFlag.
	 *
	 * @param {String} type Currently either "subject" (Subject / Subjectprovider) or "noRole" (noRole / noUser).
	 * @returns {String} A string depending on the processFlag.
	 */
	this.getProcessText = function (type)
	{
		var gt_result	= "";

		if (gf_isset(type))
		{
			type	= type.toLowerCase();

			if (type == "subject")
			{
				if (this.isProcess())
					gt_result	= "Subject";
				else
					gt_result	= "Subjectprovider";
			}
			else if (type == "norole")
			{
				if (this.isProcess())
					gt_result	= "noRole";
				else
					gt_result	= "noUser";
			}
		}

		return gt_result;
	};

	/**
	 * Returns the id of the selected conversation.
	 *
	 * @param {String} view Indicates when a view is changed.
	 * @returns {String} Id of the selected conversation.
	 */
	this.getSelectedConversation = function (view)
	{
		if (!gf_isset(view))
			view = null;

		if (this.selectedSubject == null && view != "bv" || view == "cv")
		{
			return this.selectedConversation;
		}
		else
		{
			if (this.selectedSubject != null && gf_isset(this.subjects[this.selectedSubject]))
			{
				return this.getBehavior(this.selectedSubject).selectedConversation;
			}
			else if (this.selectedNode != null && gf_isset(this.subjects[this.selectedNode]))
			{
				return this.getBehavior(this.selectedNode).selectedConversation;
			}
		}
		return null;
	};

	/**
	 * Returns the id of the node currently selected depending on the current view.
	 *
	 * @returns {int|null} The id of the selectedNode of the currently active view or null.
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
				return this.getBehavior(this.selectedSubject).getMacro().selectedNode;
			}
		}
		return null;
	};

    /**
     * @returns the id of the edge currently selected depending on the current view
     */
    this.getSelectedEdge = function ()
    {
        if (gf_isset(this.subjects[this.selectedSubject]))
        {
            return this.getBehavior(this.selectedSubject).getMacro().selectedEdge;
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
	 * Returns the names of all subjects of the graph.
	 *
	 * @returns {String[]} An array of all subject names.
	 */
	this.getSubjectRoles = function ()
	{
		var gt_subjectRoles	= [];

		for (var gt_sid in this.subjects)
		{
			var gt_subjectRole	= this.subjects[gt_sid].geRole();

			if (gt_subjectRole != null && gt_subjectRole != "")
				gt_subjectRoles[gt_subjectRoles.length]		= this.subjects[gt_sid].getRole();
		}

		return gt_subjectRoles;
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
			gt_messages.sort();

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
	 * @param {String} view Either bv or cv. (optional)
	 * @returns {void}
	 */
	this.init = function (view)
	{
		if (!gf_isset(view) || view != "bv")
			view = "cv";

		if (gf_elementExists(gv_elements.graphBVouter))
		{
			if (document.getElementById(gv_elements.graphBVouter).innerHTML == "")
				gv_bv_paper	= null;
		}

		if (gf_elementExists(gv_elements.graphCVouter))
		{
			if (document.getElementById(gv_elements.graphCVouter).innerHTML == "")
				gv_cv_paper	= null;
		}

		if (gv_bv_paper == null && gv_cv_paper == null)
		{
			// create the Raphael Paper object for the communication view
			if (gf_elementExists(gv_elements.graphCVouter))
				gv_cv_paper = Raphael(gv_elements.graphCVouter, gv_paperSizes.cv_width, gv_paperSizes.cv_height);

			// create the Raphael Paper object for the behavioral view
			if (gf_elementExists(gv_elements.graphBVouter))
				gv_bv_paper = Raphael(gv_elements.graphBVouter, gv_paperSizes.bv_width, gv_paperSizes.bv_height);

			// load the communication view
			if (gv_cv_paper != null && view == "cv")
				this.changeView(view);

			// load the behavioral view
			if (gv_bv_paper != null && view == "bv")
				this.changeView(view);
		}
	};

	/**
	 * Returns true when the processFlag is set to true.
	 *
	 * @returns {boolean} True when the graph is a process and no case.
	 */
	this.isProcess = function ()
	{
		return this.processFlag !== false;
	};

	/**
	 * Loads a process graph from a given JSON representation stored in the database.
	 *
	 * @param {String} jsonString The JSON representation of a process.
	 * @returns {void}
	 */
	this.loadFromJSON = function (jsonString)
	{
		var gt_messages		= {};		// messageText: messageID

		if (gf_isset(jsonString))
		{

			var gt_jsonObject	= JSON.parse(jsonString);
			var gt_jsonProcess	= gt_jsonObject;
			var gt_mapMessages	= false;
			var gt_countNodes	= true;

			var gt_useConversations	= false;
			var gt_useVariables	= false;

			if (gf_isset(gt_jsonObject.process, gt_jsonObject.messages, gt_jsonObject.messageCounter))
			{
				gt_jsonProcess			= gt_jsonObject.process;
				this.messageTypes		= gt_jsonObject.messages;
				this.messageTypeCounter	= gt_jsonObject.messageCounter;
			}
			else
			{
				// remove additional attributes for backwards compatibility
				if (gf_isset(gt_jsonObject.routings))
					delete gt_jsonObject["routings"];

				if (gf_isset(gt_jsonObject.responsibilities))
					delete gt_jsonObject["responsibilities"];

				gt_mapMessages				= true;
				this.messageTypeCounter		= 0;
			}

			if (gf_isset(gt_jsonObject.nodeCounter))
			{
				this.nodeCounter	= gt_jsonObject.nodeCounter;
				gt_countNodes		= false;
			}

			if (gf_isset(gt_jsonObject.conversations, gt_jsonObject.conversationCounter))
			{
				this.conversations		= gt_jsonObject.conversations;
				this.conversationCounter	= gt_jsonObject.conversationCounter;
				gt_useConversations		= true;
			}

			// reset conversation selection
			this.selectedConversation	= "##all##";

			// 1. create subjects (replace <br /> by \n)
			for (var gt_subjectId in gt_jsonProcess)
			{
				var gt_subject = gt_jsonProcess[gt_subjectId];

				// provide compatibility to previous versions:
				var gt_inputPool	= gf_isset(gt_subject.inputPool) ? gt_subject.inputPool : -1;
				var gt_subjectType	= gf_isset(gt_subject.subjectType) ? gt_subject.subjectType : gt_subject.type;

				this.addSubject(gt_subject.id, gf_replaceNewline(gt_subject.name), gt_subjectType, gt_inputPool, gt_subject.deactivated);

				if (gf_isset(this.subjects[gt_subject.id]))
				{
					var gt_role	= gf_isset(gt_subject.role) ? gt_subject.role : gt_subject.id;

					if (gf_isset(gt_subject.relatedSubject))
						this.subjects[gt_subject.id].setRelatedSubject(gt_subject.relatedSubject);

					if (gf_isset(gt_subject.externalType))
						this.subjects[gt_subject.id].setExternalType(gt_subject.externalType);

					if (gf_isset(gt_subject.comment))
						this.subjects[gt_subject.id].setComment(gt_subject.comment);

					if (gf_isset(gt_subject.startSubject))
						this.subjects[gt_subject.id].setStartSubject(gt_subject.startSubject);
            
					if (gf_isset(gt_subject.mergedSubjects))
						this.subjects[gt_subject.id].setMergedSubjects(gt_subject.mergedSubjects);

					if (gf_isset(gt_subject.blackboxname))
						this.subjects[gt_subject.id].setBlackboxname(gt_subject.blackboxname);

					if (gf_isset(gt_subject.relatedInterface))
						this.subjects[gt_subject.id].setRelatedInterface(gt_subject.relatedInterface);

					if (gf_isset(gt_subject.isImplementation))
						this.subjects[gt_subject.id].setIsImplementation(gt_subject.isImplementation);

					this.subjects[gt_subject.id].setRole(gt_role);
				}

				if (gt_countNodes)
				{
					this.nodeCounter++;
				}
			}

			// 2. add nodes + edges
			for (var gt_subjectId in gt_jsonProcess)
			{
				var gt_subject	= gt_jsonProcess[gt_subjectId];
				var gt_behav	= this.getBehavior(gt_subject.id);

				// check if variables are used
				if (gf_isset(gt_subject.variables, gt_subject.variableCounter))
				{
					gt_behav.variables			= gt_subject.variables;
					gt_behav.variableCounter	= gt_subject.variableCounter;
					gt_useVariables		= true;
				}

				if (gt_behav != null)
				{
					if (!gf_isset(gt_subject.macros))
					{
						gt_subject.macros	= [];

						gt_subject.macros[0] = {
								id:				"##main##",
								name:			"",
								nodes:			gt_subject.nodes,
								edges:			gt_subject.edges,
								nodeCounter:	gt_subject.nodeCounter
						};
					}

					if (!gf_isset(gt_subject.macroCounter))
						gt_subject.macroCounter = 0;

					for (var gt_mid in gt_subject.macros)
					{
						var gt_macroValues	= gt_subject.macros[gt_mid];

						if (!gf_isset(gt_behav.macros[gt_macroValues.id]))
							gt_behav.macros[gt_macroValues.id] = new GCmacro(gt_behav, gt_macroValues.id, gt_macroValues.name);

						var gt_macro	= gt_behav.macros[gt_macroValues.id];

						// 2.1 nodes
						for (var gt_nodeId in gt_macroValues.nodes)
						{
							var gt_node		= gt_macroValues.nodes[gt_nodeId];
							var gt_nodeType	= gf_isset(gt_node.nodeType) ? gt_node.nodeType : gt_node.type;
							var gt_nodeId	= gt_macro.addNode("loadedNode" + gt_node.id, gf_replaceNewline(gt_node.text), gt_nodeType, gt_node.start, gt_node.end, gt_node.manualPositionOffsetX, gt_node.manualPositionOffsetY, false, gt_node.autoExecute);
							// var gt_nodeId	= gt_macro.addNode("loadedNode" + gt_node.id, gf_replaceNewline(gt_node.text), gt_node.type, gt_node.start, gt_node.end, gt_node.deactivated);

							var gt_createdNode	= gt_macro.getNode(gt_nodeId);

							if (gt_createdNode != null)
							{
								if (gf_isset(gt_node.options))
									gt_createdNode.setOptions(gt_node.options);

								if (gf_isset(gt_node.conversation) && gt_useConversations)
									gt_createdNode.setConversation(gt_node.conversation);

								if (gf_isset(gt_node.variable) && gt_useVariables)
									gt_createdNode.setVariable(gt_node.variable);

								if (gf_isset(gt_node.majorStartNode))
									gt_createdNode.setMajorStartNode(gt_node.majorStartNode);

								if (gf_isset(gt_node.varMan))
									gt_createdNode.setVarMan(gt_node.varMan);

								if (gf_isset(gt_node.createSubjects))
									gt_createdNode.setCreateSubjects(gt_node.createSubjects);

								if (gf_isset(gt_node.chooseAgentSubject))
									gt_createdNode.setChooseAgentSubject(gt_node.chooseAgentSubject);

								if (gf_isset(gt_node.chooseAgentSubject))
									gt_createdNode.setChooseAgentSubject(gt_node.chooseAgentSubject);

								if (gf_isset(gt_node.macro))
									gt_createdNode.setMacro(gt_node.macro);

								if (gf_isset(gt_node.blackboxname))
									gt_createdNode.setBlackboxname(gt_node.blackboxname);

								if (gf_isset(gt_node.comment))
									gt_createdNode.setComment(gt_node.comment);
							}

						}

						// 2.1 a) fix those state ids to match the new generated id values
						for(var someId in gt_macro.nodes) {
							var gcNode = gt_macro.nodes[someId];
							if(gcNode.getOptions() && 'state' in gcNode.getOptions() && gcNode.getOptions().state) {
								var oldStateId = gcNode.getOptions().state;
								var newStateId = gt_macro.nodeIDs["loadedNode" + gcNode.getOptions().state];
								gcNode.options.state = newStateId;
							}
						}

						// 2.2 edges
						for (var gt_edgeId in gt_macroValues.edges)
						{
							var gt_edge				= gt_macroValues.edges[gt_edgeId];
							var gt_startNodeID		= gt_edge.start;

							if (parseInt(gt_startNodeID) != gt_startNodeID && gf_isset(gt_macro.nodeIDs[gt_startNodeID]))
							{
								gt_startNodeID = gt_macro.nodeIDs[gt_startNodeID];
							}

							var gt_startNode		= gt_macro.getNode(gt_startNodeID);
							var gt_startNodeType	= gt_startNode != null ? gt_startNode.getType() : "action";
							var gt_text				= gf_replaceNewline(gt_edge.text);

							if (gf_isset(gt_edge.edgeType))
								gt_edge.type = gt_edge.edgeType;

							// map messages to new system
							if (gt_mapMessages && (gt_startNodeType == "send" || gt_startNodeType == "receive") && gt_edge.type == "exitcondition")
							{
								gt_text	= this.addMessageType(gt_text);
							}

							var gt_createdEdge	= gt_macro.addEdge("loadedNode" + gt_edge.start, "loadedNode" + gt_edge.end, gt_text, gt_edge.target, gt_edge.type, gt_edge.deactivated, gt_edge.manualPositionOffsetLabelX, gt_edge.manualPositionOffsetLabelY, gt_edge.optional);

							if (gt_createdEdge != null)
							{
								if (gf_isset(gt_edge.priority))
								{
									gt_createdEdge.setPriority(gt_edge.priority);
								}

								if (gf_isset(gt_edge.comment))
								{
									gt_createdEdge.setComment(gt_edge.comment);
								}

								if (gf_isset(gt_edge.manualTimeout))
								{
									gt_createdEdge.setManualTimeout(gt_edge.manualTimeout);
								}

								if (gf_isset(gt_edge.transportMethod))
								{
									gt_createdEdge.setTransportMethod(gt_edge.transportMethod);
								}

								if (gt_useVariables)
								{
									if (gf_isset(gt_edge.variable))
									{
										gt_createdEdge.setVariable(gt_edge.variable);
									}

									if (gf_isset(gt_edge.correlationId))
									{
										gt_createdEdge.setCorrelationId(gt_edge.correlationId);
									}
								}
							}
						}
						// set the nodeCounter to avoid problems with new nodes
						gt_macro.nodeCounter = gt_macroValues.nodeCounter;
					}

					gt_behav.macroCounter	= gt_subject.macroCounter;
				}
			}

			// draw the graph
			this.draw();
		}
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
			gf_callFunc("communication.clearInputFields", "gf_guiClearInputFields");
		}

		// load the information of a selected node
		else
		{
			this.loadInformation(true);
			gf_callFunc("communication.toggleNEForms", "gf_guiToggleNEForms", "n");

			// when the communication view is shown load the information of the currently selected subject-node
			if (this.selectedSubject == null)
			{
				if (this.selectedNode != null && gf_isset(this.subjects[this.selectedNode]))
				{
					var gt_subject = this.subjects[this.selectedNode];

					gf_callFunc("communication.displaySubject", "gf_guiDisplaySubject", gt_subject);
				}
			}

			// when a behavioral view is shown the information of the currently selected node are loaded
			else
			{
				if (gf_isset(this.subjects[this.selectedSubject]))
				{
					var gt_node = this.getBehavior(this.selectedSubject).getNode();

					gf_callFunc("communication.displayNode", "gf_guiDisplayNode", gt_node);
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
				gf_callFunc("communication.toggleNEForms", "gf_guiToggleNEForms", "e");

				var gt_edge = this.getBehavior(this.selectedSubject).getEdge();
				var gt_node = this.getBehavior(this.selectedSubject).getNode(gt_edge.getStart());
				var gt_type	= gf_isset(gt_node) ? gt_node.getType() : "";

				gf_callFunc("communication.displayEdge", "gf_guiDisplayEdge", gt_edge, gt_type);
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
		var gt_processData	= {};

		var gt_array = [];

		var gt_arrayIndex	= 0;

		// transform subjects and the related behaviors
		for (var gt_sid in this.subjects)
		{
			gt_arrayIndex = gt_array.length;

			gt_array[gt_arrayIndex] = {
						id: gt_sid,
						name: this.subjects[gt_sid].getText(),
						type: this.subjects[gt_sid].getType(),
						subjectType: this.subjects[gt_sid].getType(),
						mergedSubjects: this.subjects[gt_sid].getMergedSubjects(),
						deactivated: this.subjects[gt_sid].isDeactivated(),
						inputPool: this.subjects[gt_sid].getInputPool(),
						blackboxname: this.subjects[gt_sid].getBlackboxname(),
						relatedInterface: this.subjects[gt_sid].getRelatedInterface(),
						relatedProcess: this.subjects[gt_sid].getRelatedProcess(),
						relatedSubject: this.subjects[gt_sid].getRelatedSubject(),
						isImplementation: this.subjects[gt_sid].getIsImplementation(),
						externalType: this.subjects[gt_sid].getExternalType(),
						role: this.subjects[gt_sid].getRole(),
						startSubject: this.subjects[gt_sid].isStartSubject(),
						implementations: this.subjects[gt_sid].getImplementations(),
						comment: this.subjects[gt_sid].getComment()
//                        manualPositionOffsetX: this.subjects[gt_sid].getManualPositionOffset().dx,
//                        manualPositionOffsetY: this.subjects[gt_sid].getManualPositionOffset().dy
			};

			var gt_behav 	= this.subjects[gt_sid].getBehavior();
			var gt_macros	= gt_behav.getMacros();
			var gt_newMacros	= [];

			for (var gt_mid in gt_macros)
			{
				var gt_macro    = gt_macros[gt_mid];
				var gt_nodes    = gt_macro.getNodes();
				var gt_edges    = gt_macro.getEdges();
				var gt_newNodes	= [];
				var gt_newEdges	= [];

				// transform the behavior's nodes
				for (var gt_nid in gt_nodes)
				{
					var gt_node = gt_nodes[gt_nid];
					gt_newNodes[gt_newNodes.length] = {
							id:                 gt_node.getId(),
							text:               gt_node.getText(),
							start:              gt_node.isStart(),
							autoExecute:        gt_node.isAutoExecute(),
							end:                gt_node.isEnd(),
							type:               gt_node.getType(),
							nodeType:           gt_node.getType(),
							options:            gt_node.getOptions(),
							deactivated:        gt_node.isDeactivated(),
							majorStartNode:     gt_node.isMajorStartNode(),
							conversation:       gt_node.getConversation(),
							variable:           gt_node.getVariable(),
							varMan:             gt_node.getVarMan("all"),
							createSubjects:     gt_node.getCreateSubjects("all"),
							chooseAgentSubject: gt_node.getChooseAgentSubject(),
							macro:              gt_node.getMacro(),
							blackboxname:       gt_node.getBlackboxname(),
							comment:            gt_node.getComment(),
							manualPositionOffsetX: gt_node.getManualPositionOffset().dx,
							manualPositionOffsetY: gt_node.getManualPositionOffset().dy
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
						var gt_relatedSubject	= gt_edge.getRelatedSubject("all");
						var gt_edgeStartNode	= gt_macro.getNode(gt_edgeStart);
						var gt_edgeEndNode		= gt_macro.getNode(gt_edgeEnd);

						if (gt_edgeStartNode != null && gt_edgeEndNode != null)
						{
							gt_newEdges[gt_newEdges.length] = {
									start:           gt_edgeStartNode.getId(),
									end:             gt_edgeEndNode.getId(),
									text:            gt_edge.getText(true),
									type:            gt_edge.getType(),
									edgeType:        gt_edge.getType(),
									target:          gt_relatedSubject == null ? "" : gt_relatedSubject,
									deactivated:     gt_edge.isDeactivated(),
									optional:        gt_edge.isOptional(),
									priority:        gt_edge.getPriority(),
									manualTimeout:   gt_edge.isManualTimeout(),
									variable:        gt_edge.getVariable(),
									correlationId:   gt_edge.getCorrelationId(),
									comment:         gt_edge.getComment(),
									transportMethod: gt_edge.getTransportMethod(),
                  manualPositionOffsetLabelX: gt_edge.getManualPositionOffsetLabel().dx,
                  manualPositionOffsetLabelY: gt_edge.getManualPositionOffsetLabel().dy
							};
						}
					}
				}

				gt_newMacros[gt_newMacros.length] = {
						id:          gt_macro.id,
						name:        gt_macro.name,
						nodes:       gt_newNodes,
						edges:       gt_newEdges,
						nodeCounter: gt_macro.nodeCounter
				};
			}

			gt_array[gt_arrayIndex].macros          = gt_newMacros;
			gt_array[gt_arrayIndex].macroCounter    = gt_behav.macroCounter;
			gt_array[gt_arrayIndex].variables       = gt_behav.variables;
			gt_array[gt_arrayIndex].variableCounter	= gt_behav.variableCounter;
		}

		gt_processData.process             = gt_array;
		gt_processData.messages            = this.messageTypes;
		gt_processData.messageCounter      = this.messageTypeCounter;
		gt_processData.nodeCounter         = this.nodeCounter;
		gt_processData.conversations       = this.conversations;
		gt_processData.conversationCounter = this.conversationCounter;

		return gt_processData;
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
	 * Select a conversation.
	 * When an internal behavior is selected the conversation name will be passed to the behavior's selectConversation method.
	 *
	 * @param {String} conversation The name of the conversation to select. When set to "##conversations##" the available conversations will be displayed in the CV. When set to "##all##" all conversations will be displayed.
	 * @returns {void}
	 */
	this.selectConversation = function (conversation)
	{
		if (this.selectedSubject == null)
		{
			if (!gf_isset(conversation))
				conversation = "##all##";

			this.selectedConversation	= conversation;

			this.draw();
		}
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				this.getBehavior(this.selectedSubject).selectConversation(conversation);
			}
		}
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
	 * When selectedSubject is set the GCbehavior.selectMacro(id) method of the currently active behavior is called.
	 *
	 * @see GCbehavior.selectMacro()
	 * @param {int} id The id of the macro to select.
	 * @returns {void}
	 */
	this.selectMacro = function (id)
	{
		if (this.selectedSubject == null)
		{
			// not available in cv
		}
		else
		{
			if (gf_isset(this.subjects[this.selectedSubject]))
			{
				this.getBehavior(this.selectedSubject).selectMacro(id);
			}
		}
	}

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

				gf_callFunc("communication.updateListOfSubjects", null);
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
				var gt_values	= gf_callFunc("communication.readEdge", "gf_guiReadEdge");

				this.getBehavior(this.selectedSubject).updateEdge(gt_values);

				this.loadInformationEdge();
			}
		}
	};

  this.updateEdgeStartNode = function()
  {
		if (this.selectedSubject && gf_isset(this.subjects[this.selectedSubject]))
		{
			this.getBehavior(this.selectedSubject).setStartEdge();
		}
  };

  this.updateEdgeEndNode = function()
  {
		if (this.selectedSubject && gf_isset(this.subjects[this.selectedSubject]))
		{
			this.getBehavior(this.selectedSubject).setEndEdge();
		}
  };

	/**
	 * When selectedSubject is set the input fields are read and the information is passed to the GCbehavior.updateNode() method of the current behavior.
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

				var gt_values	= gf_callFunc("communication.readSubject", "gf_guiReadSubject");

				var gt_text             = gf_isset(gt_values.text)             ? gt_values.text:             "";
				var gt_role             = gf_isset(gt_values.role)             ? gt_values.role:             "";
				var gt_type             = gf_isset(gt_values.type)             ? gt_values.type:             "";
				var gt_inputPool        = gf_isset(gt_values.inputPool)        ? gt_values.inputPool:        "";
				var gt_blackboxname     = gf_isset(gt_values.blackboxname)     ? gt_values.blackboxname:     "";
				var gt_relatedProcess   = gf_isset(gt_values.relatedProcess)   ? gt_values.relatedProcess:   "";
				var gt_relatedSubject   = gf_isset(gt_values.relatedSubject)   ? gt_values.relatedSubject:   "";
				var gt_isImplementation	= gf_isset(gt_values.isImplementation) ? gt_values.isImplementation: false;
				var gt_externalType     = gf_isset(gt_values.externalType)     ? gt_values.externalType:     "external";
				var gt_comment          = gf_isset(gt_values.comment)          ? gt_values.comment:          "";
				var gt_startSubject     = gf_isset(gt_values.startSubject)     ? gt_values.startSubject:     false;

        gt_type	= gt_type != "" ? gt_type : gt_subject.getType();

				if (gt_text.replace(" ", "") != "")
				{
					gt_subject.setRole(gt_role);
					gt_subject.setText(gt_text);
					gt_subject.setType(gt_type);
					gt_subject.setInputPool(gt_inputPool);
					gt_subject.setBlackboxname(gt_blackboxname);
					gt_subject.setRelatedProcess(gt_relatedProcess);
					gt_subject.setRelatedSubject(gt_relatedSubject);
					gt_subject.setIsImplementation(gt_isImplementation);
					gt_subject.setExternalType(gt_externalType);
					gt_subject.setComment(gt_comment);
					gt_subject.setStartSubject(gt_startSubject);

					// publish the update of the subject
					$.publish(gv_topics.general.subjects, [{action: "update", id: gt_subject.id}]);

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
				var gt_values	= gf_callFunc("communication.readNode", "gf_guiReadNode");

				var gt_behav	= this.getBehavior(this.selectedSubject);
					gt_behav.updateNode(gt_values);

				this.loadInformation();
			}
		}
	};

    /**
     * Finds an object by id and type, based on the current selected subject
     *
     * @param {int|string} id the id of the subject or node
     * @param {string} type 'node' or 'edgeLabel'
     */
    this.getObjectById = function(id, type) {
        if(null === this.selectedSubject) {
            if(type === 'node') {
                return this.getSubjects()[id];
            }
        } else {
            var behavior = this.getBehavior(this.selectedSubject);
            if(behavior) {
                if(type === 'node') {
                    return behavior.getNode(id);
                } else if (type === 'edgeLabel') {
                    return behavior.getEdge(id);
                }
            }
        }
    }
}
