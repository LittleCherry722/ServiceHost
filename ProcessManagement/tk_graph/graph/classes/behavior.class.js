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
 * Behavioral Graph
 * 
 * @class The graph information for an internal behavior.
 * @param {String} name The name of the subject this internal behavior belongs to.
 * @returns {void}
 */
function GCbehavior (name)
{
	
	/**
	 * Initialized with 0.
	 * This counter is used to give every macro an unique id.
	 * The counter is increased with every macro added.
	 * 
	 * @type int
	 */
	this.macroCounter = 0;
	
	/**
	 * This array contains all GCmacros of the internal behavior.
	 * The keys of this array are the values of macroCounter prefixed with "m" or "##main##" for the first macro added.
	 * 
	 * @type GCmacro[]
	 */
	this.macros	= {};
	
	/**
	 * The name of this behavioral view.
	 * It is the same as the ID of the corresponding subject and is used to identify the behavioral view in the drawing methods of tk_graph.
	 * 
	 * @type String
	 */
	this.name	= name;
	
	/**
	 * The currently selected conversation
	 * 
	 * @type String
	 */
	this.selectedConversation	= "##all##";
	
	/**
	 * The id (key of macros array) of the currently selected macro.
	 * 
	 * @type int
	 */
	this.selectedMacro	= "##main##";
	
	/**
	 * Initialized with 0.
	 * This counter is used to give every variable an unique id.
	 * The counter is increased with every variable added.
	 * 
	 * @type int
	 */
	this.variableCounter	= 0;
	
	/**
	 * List of variables used in the internal behavior.
	 * 
	 * @type Object
	 */
	this.variables		= {};
	
	/**
	 * Creates a new GCedge in the proper macro.
	 * 
	 * @see GCmacro.addEdge()
	 * @param {int} start The id of the start node.
	 * @param {int} end The id of the end node.
	 * @param {String} text The label of this edge. When this edge's start node is either a send or receive node this can also be a message type.
	 * @param {String} relatedSubject This is only set for edges whose start node is either a send or a receive node. It refers to the subject a message is sent to / received from.
	 * @param {String} type The edge's type (exitcondition, timeout, errorcondition).
	 * @param {boolean} [deactivated] The deactivation status of the edge. (default: false)
	 * @param {boolean} [optional] The optional status of the edge. (default: false)
	 * @returns {GCedge} The created edge or null on errors.  
	 */
	this.addEdge = function (start, end, text, relatedSubject, type, deactivated, optional)
	{
		this.getMacro().addEdge(start, end, text, relatedSubject, type, deactivated, optional);
 	};
	
	/**
	 * Creates a new GCedge (with type=exit condition and a proper messageType added) in the selected macro.
	 * 
	 * @param {int} start The id of the start node.
	 * @param {int} end The id of the end node.
	 * @param {String} message The messageType (text not ID) associated to this edge.
	 * @param {String} relatedSubject This is only set for edges whose start node is either a send or a receive node. It refers to the subject a message is sent to / received from.
	 * @param {boolean} [deactivated] The deactivation status of the edge. (default: false)
	 * @param {boolean} [optional] The optional status of the edge. (default: false)
	 * @returns {GCedge} The created edge or null on errors.  
	 */
	this.addEdgeMessage = function (start, end, message, relatedSubject, deactivated, optional)
	{
		return this.addEdge(start, end, gv_graph.addMessageType(message), relatedSubject, "exitcondition", deactivated, optional);
	};
	
	/**
	 * Creates a new GCnode in the selected macro.
	 * 
	 * @see GCmacro.addNode()
	 * @param {String} id The id of the node. New nodes will get a automatically generated unique id.
	 * @param {String} text The label of the node.
	 * @param {String} [type] The type of the node. Possible values are "send", "receive", "end", "action" (default: "action")
	 * @param {boolean} [start] When set to true the node will be handled as a start node. (default: false)
	 * @param {boolean} [end] When set to true the node will be handled as an end node. (default: false)
	 * @param {boolean} [deactivated] The deactivation status of the node. (default: false)
	 * @returns {int} The id that identifies the node in the nodes array.
	 */
	this.addNode = function (id, text, type, start, end, deactivated)
	{
		return this.getMacro().addNode(id, text, type, start, end, deactivated);
 	};
 	
 	/**
 	 * Adds a new variable to the variables array of this internal behavior.
 	 * This method can also be used to update variables.
 	 * 
 	 * @param {String} text The name of the variable.
 	 * @param {String} id The id of the variable (optional, when set an update will be done)
 	 * @returns {String} The id of the inserted or updated variable.
 	 */
 	this.addVariable = function (text, id)
 	{
 		if (!gf_isset(id))
			id = "##createNew##";
		
		var gt_variableId	= "";
		if (gf_isset(id, text))
		{
			if (id.substr(0, 1) == "v")
			{
				// when changing the text of a variable avoid duplicate variables
				var gt_variableExists	= false;
				for (var gt_vid in this.variables)
				{
					if (gf_replaceNewline(this.variables[gt_vid].toLowerCase(), " ") == gf_replaceNewline(text.toLowerCase(), " ") && gt_vid != id)
					{
						gt_variableExists	= true;
						break;
					}
				}
				
				// update variable
				if (!gt_variableExists && gf_isset(this.variables[id]) && this.variables[id] != text && text != "")
				{
					this.variables[id]	= text;
				}
				gt_variableId	= id;
			}
			
			// create new variable
			else if (id == "##createNew##")
			{
				var gt_variableExists	= false;
				
				// avoid duplicate variables
				for (var gt_vid in this.variables)
				{
					var gt_var	= this.variables[gt_vid];
					
					if (gf_replaceNewline(gt_var.toLowerCase(), " ") == gf_replaceNewline(text.toLowerCase(), " "))
					{
						gt_variableId	= gt_vid;
						gt_variableExists	= true;
						break;
					}
				}
				
				// create the new variable
				if (!gt_variableExists && text != "")
				{
					this.variables["v" + this.variableCounter] = text;
					gt_variableId	= "v" + this.variableCounter;
					this.variableCounter++;
				}
			}
			else
			{
				// (no variable selected)
			}
		}
		
		return gt_variableId;
 	};
	
	/**
	 * Clears the currently selected macro.
	 * 
	 * @see GCmacro.clearGraph()
	 * @returns {void}
	 */
	this.clearGraph = function ()
	{
		this.getMacro().clearGraph();
 	};
	
	/**
	 * Calls the connectNodes() method of the currently selected macro.
	 * 
	 * @see GCmacro.connectNodes()
	 * @returns {void}
	 */
	this.connectNodes = function ()
	{
		this.getMacro().connectNodes();
 	};
	
	/**
	 * This method creates a new edge in the currently selected macro.
	 * 
	 * @see GCmacro.createEdge()
	 * @param {String} start The id of the start node.
	 * @param {String} end The id of the end node.
	 * @param {String} type The type of the edge (timeout, exitcondition, errorcondition).
	 * @returns {void}
	 */
	this.createEdge = function (start, end, type)
	{
		this.getMacro().createEdge(start, end, type);
 	};
 	
 	/**
 	 * Creates a new macro and stores it in the macros array.
 	 * 
 	 * @param {String} name The name of the macro.
 	 * @returns {String} The ID of the new macro.
 	 */
 	this.createMacro = function (name)
 	{
 		if (!gf_isset(name))
 			name = "new Macro #" + this.macroCounter;
 			
 		var gt_macroId	= "m" + this.macroCounter;
 		var gt_macro	= new GCmacro(this, gt_macroId, name);
 		
 		// add start node to gt_macro
 		gt_macro.addNode("", "Macro Start", "action", true, false);
 		
 		this.macros[gt_macroId]	= gt_macro;
 		
 		// Publish news about updated macro list
		$.publish(gv_topics.general.macros, [{action: "add", id: gt_macroId, text: name}]);
 		
 		this.macroCounter++;
 		
 		return gt_macroId;
 	};
	
	/**
	 * Creates a new node in the currently selected macro.
	 * 
	 * @see GCmacro.createNode()
	 * @returns {int} The node's id.
	 */
	this.createNode = function ()
	{
		return this.getMacro().createNode();
 	};
	
	/**
	 * De- / activates the currently selected edge depending on its current deactivation status of the currently selected macro.
	 * 
	 * @see GCmacro.deactivateEdge()
	 * @returns {void}
	 */
	this.deactivateEdge = function ()
	{
		this.getMacro().deactivateEdge();
 	};
	
	/**
	 * De- / activates the currently selected node depending on its current deactivation status of the currently selected macro.
	 * 
	 * @see GCmacro.deactivateNode()
	 * @returns {void}
	 */
	this.deactivateNode = function ()
	{
		this.getMacro().deactivateNode();
 	};
	
	/**
	 * Removes the edge with the given id from the currently selected macro.
	 * 
	 * @see GCmacro.deleteEdge()
	 * @param {int} [id] The id of the edge to remove.
	 * @returns {void} 
	 */
	this.deleteEdge = function (id)
	{
		this.getMacro().deleteEdge(id);
 	};
	
	/**
	 * Removes the node with the given id from the currently selected macro.
	 * 
	 * @see GCmacro.deleteNode()
	 * @param {int} [id] The id of the node to remove.
	 * @returns {void} 
	 */
	this.deleteNode = function (id)
	{
		this.getMacro().deleteNode(id);
 	};
	
	/**
	 * Draws the graph of the currently selected macro.
	 * 
	 * @see GCmacro.draw()
	 * @returns {void}
	 */
	this.draw = function ()
	{
		this.getMacro().draw();
 	};
	
	/**
	 * Returns the GCedge with the given id.
	 * 
	 * @see GCmacro.getEdge()
	 * @param {int} [id] The id of the edge. When this is null or not set the id of selectedEdge will be taken.
	 * @returns {GCedge} The GCedge for the given id.
	 */
	this.getEdge = function (id)
	{
		return this.getMacro().getEdge(id);
 	};
	
	/**
	 * Returns the edges array of the currently selected macro.
	 * 
	 * @see GCmacro.getEdges()
	 * @returns {GCedge[]}
	 */
	this.getEdges = function ()
	{
		return this.getMacro().getEdges();
 	};
 	
 	/**
 	 * Returns the macro with the given ID or the currently selected macro, when no id is given.
 	 * 
 	 * @param {String} id The id of the macro. When not set, the currently selected Macro will be returned.
 	 * @returns {GCmacro} The macro with the given ID.
 	 */
 	this.getMacro = function (id)
 	{
 		if (!gf_isset(id))
 			id = this.selectedMacro;
 			
 		if (!gf_isset(this.macros[id]))
 			id = "##main##";
 			
 		return this.macros[id];
 	};
	
	/**
	 * Returns the macros array of the internal behavior.
	 * 
	 * @returns {GCmacro[]}
	 */
	this.getMacros = function ()
	{
		return this.macros;
 	};
	
	/**
	 * Returns the GCnode with the given internal id.
	 * 
	 * @see GCmacro.getNode()
	 * @param {int} [id] The id of the node. When this is null or not set the id of selectedNode will be taken.
	 * @returns {GCnode} The GCnode for the given id.
	 */
	this.getNode = function (id)
	{
		return this.getMacro().getNode(id);
 	};
	
	/**
	 * Returns the nodes array of the currently selected macro.
	 * 
	 * @see GCmacro.getNodes()
	 * @returns {GCnode[]}
	 */
	this.getNodes = function ()
	{
		return this.getMacro().getNodes();
 	};
	
	/**
	 * Select a conversation in the currently selected macro.
	 * 
	 * @see GCmacro.selectConversation()
	 * @param {String} conversation The name of the conversation to select. When set to "##all##" all nodes and edges will be displayed.
	 * @returns {void}
	 */
 	this.selectConversation = function (conversation)
 	{
 		this.getMacro().selectConversation(conversation);
 	};
	
	/**
	 * Selects the edge with the given id.
	 * 
	 * @see GCmacro.selectEdge()
	 * @param {int} id The id of the edge.
	 * @returns {void}
	 */
	this.selectEdge = function (id)
	{
		this.getMacro().selectEdge(id);
 	};
 	
 	/**
 	 * Selects the macro with the given id.
 	 * 
 	 * @param {String} id The id of the macro.
 	 * @returns {void}
 	 */
 	this.selectMacro = function (id)
 	{
 		if (!gf_isset(id))
 			id = "##main##";
 			
 		if (!gf_isset(this.macros[id]))
 			id = "##main##";
 			
 		this.selectedMacro = id;
 		
		$.publish(gv_topics.general.macros, [{action: "load", view: "bv"}]);
 		
 		this.draw();
 	};
 	
 	/**
 	 * Selects the macro by double-clicking a node.
 	 * 
 	 * @param {String} id The id of the node.
 	 * @returns {void}
 	 */
 	this.selectMacroByNode = function (id)
 	{
 		var gt_node	= this.getMacro().getNode(id);
 		
 		if (gt_node != null)
 		{
 			var gt_macroId	= gt_node.getMacro();
 			if (gt_node.getType() == "macro" && gt_macroId != null && gf_isset(this.macros[gt_macroId]))
 			{
 				this.selectMacro(gt_macroId);
 			}
 		}
 	};
	
	/**
	 * Selects the node with the given id.
	 * 
	 * @see GCmacro.selectNode()
	 * @param {int} id The id of the node
	 * @returns {void}
	 */
	this.selectNode = function (id)
	{
		this.getMacro().selectNode(id);
 	};
	
	/**
	 * Resets selection in the currently selected macro.
	 * 
	 * @see GCmacro.selectNothing()
	 * @returns {void}
	 */
	this.selectNothing = function ()
	{
		this.getMacro().selectNothing();
 	};
	
	/**
	 * Updates the currently selected edge of the currently selected macro.
	 * 
	 * @see GCmacro.updateEdge()
	 * @param {Object} values An object (gt_values) passed by the updateEdge method from GCcommunication.
	 * @returns {void}
	 */
	this.updateEdge = function (values)
	{
		this.getMacro().updateEdge(values);
 	};
	
	/**
	 * Updates the currently selected node of the currently selected macro.
	 * 
	 * @see GCmacro.updateNode()
	 * @param {Object} values The values array passed by the GCcommunication::updateNode() function.
	 * @returns {void}
	 */
	this.updateNode = function (values)
	{
		this.getMacro().updateNode(values);
 	};
 	
 	// init
 	this.macros["##main##"]	= new GCmacro(this, "##main##", "internal behavior");
 	this.selectMacro("##main##");
}