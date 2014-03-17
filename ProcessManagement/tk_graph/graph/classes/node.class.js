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
 * The node class represents a node in a behavioral view.
 * 
 * @private
 * @class represents a node in a behavioral view
 * @param {GCmacro} parentMacro The parent instance of GCmacro.
 * @param {GCbehavior} parentBehavior The parent instance of GCbehavior.
 * @param {String} id The id of the node.
 * @param {String} text The label of the node.
 * @param {String} type The type of the node. Possible values are "send", "receive", "end", "action". (default: "action")
 * @returns {void}
 */
function GCnode (parentMacro, parentBehavior, id, text, type)
{	
	/**
	 * The node's conversation.
	 * Conversations are used to group nodes and edges within an internal behavior.
	 * 
	 * @type String
	 */
	this.conversation	= null;
	
	/**
	 * Comment for this node.
	 * 
	 * @type String
	 */
	this.comment	= "";
	
	/**
	 * CorrelationId, only used for closeIP, openIP, isIPempty
	 * 
	 * @type String
	 */
	this.correlationId	= "##cid##";
	
	/**
	 * Settings for the predefined "create subjects" action.
	 * 
	 * @type Object
	 */
	this.createSubjects	= {subject: null, storevar: "", min: -1, max: -1};
	
	/**
	 * This attribute states whether the node is deactivated.
	 * 
	 * @type boolean
	 */
	this.deactivated	= false;
	
	/**
	 * When set to true this node will be handled as an end node.
	 * 
	 * @type boolean
	 */
	this.end	= false;
	
	/**
	 * The id of this node. This is not necessarily the same as the node's id in the graph.
	 * 
	 * @type String
	 */
	this.id		= "";
	
	/**
	 * The id of a macro that is associated with this node.
	 * 
	 * @type String
	 */
	this.macro	= "";
	
	/**
	 * Flag to indicate whether a start node is the major startNode of the internal behavior.
	 * 
	 * @type boolean
	 */
	this.majorStartNode	= false;
	
	/**
	 * Options for predefined actions.
	 * 
	 * @type Object
	 */
	this.options	= {message: "*", subject: "*", correlationId: "*", conversation: "*", state: ""};
	
	/**
	 * A reference to the parent instance of GCbehavior.
	 * 
	 * @type GCbehavior
	 */
	this.parentBehavior	= parentBehavior;
	
	/**
	 * A reference to the parent instance of GCmacro.
	 * 
	 * @type GCmacro
	 */
	this.parentMacro	= parentMacro;
	
	/**
	 * When this value is set to true this node will be handled as a start node for the internal behavior. 
	 * Caution: this setting can be overriden by setting the "end" attribute
	 * 
	 * @type boolean
	 */
	this.start	= false;
	
	/**
	 * The label of this node.
	 * When the type of this node is either send or receive, this value will not be displayed.
	 * Instead the letters "R" (receive) or "S" (send) will be displayed in the graph.
	 * When this is an end node, nothing will be displayed.
	 * In most cases this attribute will hold a short action description.
	 * 
	 * @type String
	 */
	this.text	= "";
	
	/**
	 * For internal actions only.
	 * The node can get a set of messages stored to a variable to filter them.
	 * 
	 * @type String
	 */
	this.variable	= null;
	
	/**
	 * Settings for the predefined variable manipulation action.
	 * 
	 * @type Object
	 */
	this.varMan	= {var1: "", var2: "", operation: "and", storevar: ""};
	
	/**
	 * This is the node's type.
	 * It can be set to "action", "send", "receive" or to "end".
	 * It defaults to action.
	 * When the type is set to either "send" or "receive" it will be displayed as a circle with the letter "S" or "R" in it on the graph.
	 * An action node will be displayed as a rectangle holding the text stored in the node's text attribute.
	 * 
	 * @type String
	 */
	this.type	= "action";

    /**
     * The user-defined manual offset for the node position
     *
     * @type {?{dx: int, dy: int}}
     */
    this.manualPositionOffset = null;

    /**
     * Indicates whether the action should be automatically executed or not
     * @type {boolean}
     */
    this.autoExecute = false;
	
	/**
	 * Activates the node.
	 * 
	 * @returns {void}
	 */
	this.activate = function ()
	{
		this.deactivated = false;
	};
	
	/**
	 * Deactivates the node.
	 * 
	 * @returns {void}
	 */
	this.deactivate = function ()
	{
		this.deactivated = true;
	};
	
	/**
	 * Returns the node's conversation.
	 * 
	 * @param {String} type Either "id" or "name".
	 * @returns {String} The node's conversation. Depending on the parameter type this will be either the conversation's id or its name.
	 */
	this.getConversation = function (type)
	{
		if (!gf_isset(type))
			type	= "id";
	
		if (type == "name")
		{
			var gt_conversations	= gv_graph.conversations;
			if (this.conversation != null && gf_isset(gt_conversations[this.conversation]))
				return gt_conversations[this.conversation];
		}
		
		return this.conversation;
	};
	
	/**
	 * Returns the node's comment.
	 * 
	 * @returns {String} The node's comment.
	 */
	this.getComment	= function ()
	{
		return this.comment;
	};
	
	/**
	 * Returns the correlationId of the node.
	 * 
	 * @returns {String} The node's correlation ID.
	 */
	this.getCorrelationId = function ()
	{
		return this.correlationId;
	};
	
	/**
	 * Returns either one entry of the varMan objct or the whole object.
	 * 
	 * @param {String} attribute The attribute to return. When set to "all" the whole object will be returned.
	 * @param {String} type An optional parameter. When set to "name" the name of the variable stored in subject or storevar will be returned instead of its id.
	 * @returns {String|Object} Either the whole createSubjects object or a single entry.
	 */
	this.getCreateSubjects = function (attribute, type)
	{
		var gt_result	= this.createSubjects;
		
		if (!gf_isset(type))
			type = "id";
		
		if (gf_isset(attribute))
		{
			attribute	= attribute.toLowerCase();
			
			if (attribute == "storevar")
			{
				gt_result	= gf_isset(this.createSubjects[attribute]) ? this.createSubjects[attribute] : "";
				
				if (type == "name")
				{
					var gt_variables	= this.parentBehavior.variables;
					if (gt_result != null && gf_isset(gt_variables[gt_result]))
						gt_result	= gt_variables[gt_result];	
				}
			}
			else if (attribute == "subject")
			{
				gt_result	= gf_isset(this.createSubjects[attribute]) ? this.createSubjects[attribute] : "";
				
				if (type == "name")
				{
					if (gt_result != null && gf_isset(gv_graph.subjects[gt_result]))
						gt_result	= gv_graph.subjects[gt_result].getText();	
				}
			}
			else if (attribute == "min" || attribute == "max")
			{
				gt_result	= gf_isset(this.createSubjects[attribute]) ? this.createSubjects[attribute] : -1;
			}
		}
		
		return gt_result;
	};
	
	/**
	 * Returns the id of this node.
	 * 
	 * @returns {String} The id of the node.
	 */
	this.getId = function (realId)
	{
		return this.id;
	};
	
	/**
	 * Returns the id of the macro associated with this node or an empty String when the node is no macro node.
	 * 
	 * @returns {String} The id of the macro associated with this node. 
	 */
	this.getMacro = function ()
	{
		return this.macro == null || this.getType() != "macro" ? "" : this.macro;
	};
	
	/**
	 * Returns options of predefined actions.
	 * 
	 * @returns {Object} The node's options.
	 */
	this.getOptions = function ()
	{
		return this.options;
	};
	
	/**
	 * Returns the shape of this node.
	 * This method returns either "roundedrectangle" or "circle" depending on the node's type.
	 * This value influences the look of the node on the graph.
	 * 
	 * @returns {String} Either "roundedrectangle" or "circle" depending on the node's type.
	 */
	this.getShape = function ()
	{
		var type	= this.type.toLowerCase();
		var shape	= "roundedrectangle";
		
		if (this.isEnd(true))
			type = "end";
		
		if (gf_isset(gv_nodeTypes[type]) && gf_isset(gv_nodeTypes[type].shape))
		{
			shape	= gv_nodeTypes[type].shape.toLowerCase();
		}
		return shape;
	};
	
	/**
	 * Returns the value of the node's text attribute.
	 * 
	 * @returns {String} The node's text.
	 */
	this.getText = function ()
	{
		return this.text;
	};
	
	/**
	 * Returns the node's type.
	 * This can either be "send", "receive", "end" or "action".
	 * 
	 * @param {boolean} map When set to true, predefined actions will be mapped to "action".
	 * @returns {String} The node's type.
	 */
	this.getType = function (map)
	{
		if (gf_isset(map) && map === true && this.type.length > 0 && this.type.charAt(0) == '$' && gf_isset(gv_predefinedActions[this.type.substr(1)]))
		{
			return "action";
		}
		else
		{
			return this.type.toLowerCase();	
		}
	};

    /**
     * The user-defined manual offset for the node position. If the user defined no offset, an offstet of 0 pixels in
     * each direction is returned
     *
     * @returns {{dx: int, dy: int}}
     */
    this.getManualPositionOffset = function ()
    {
        return this.manualPositionOffset || {dx: 0, dy: 0};
    };

    /**
     * @returns {boolean} true if the the node has a user-defined offset
     */
    this.hasManualPositionOffset = function ()
    {
        return this.manualPositionOffset !== null && 'dx' in this.manualPositionOffset && 'dy' in this.manualPositionOffset;
    };
	
	/**
	 * Returns the variable of the node.
	 * 
	 * @param {String} type Either "id" or "name".
	 * @returns {String} The node's variable. Depending on the parameter type this will be either the variable's id or its name.
	 */
	this.getVariable = function (type)
	{
		if (!gf_isset(type))
			type	= "id";
			
		if (type == "name")
		{
			var gt_variables	= this.parentBehavior.variables;
			if (this.variable != null && gf_isset(gt_variables[this.variable]))
				return gt_variables[this.variable];
		}
		
		return this.variable;
	};
	
	/**
	 * Returns either one entry of the varMan objct or the whole object.
	 * 
	 * @param {String} attribute The attribute to return. When set to "all" the whole object will be returned.
	 * @param {String} type An optional parameter. When set to "name" the name of the variable stored in var1, var2, storevar will be returned instead of its id.
	 * @returns {String|Object} Either the whole varMan object or a single entry.
	 */
	this.getVarMan = function (attribute, type)
	{
		var gt_result	= this.varMan;
		
		if (!gf_isset(type))
			type = "id";
		
		if (gf_isset(attribute))
		{
			attribute	= attribute.toLowerCase();
			
			if (attribute == "var1" || attribute == "var2" || attribute == "storevar")
			{
				gt_result	= gf_isset(this.varMan[attribute]) ? this.varMan[attribute] : "";
				
				if (type == "name")
				{
					var gt_variables	= this.parentBehavior.variables;
					if (gt_result != null && gf_isset(gt_variables[gt_result]))
						gt_result	= gt_variables[gt_result];	
				}
			}
			else if (attribute == "operation")
			{
				gt_result	= gf_isset(this.varMan.operation) ? this.varMan.operation : "&";
				
				if (type == "name")
				{
					if (gt_result != null && gf_isset(gv_varManOperations[gt_result]))
						gt_result	= gv_varManOperations[gt_result].label;	
				}
				else if (type == "hideSecondVar")
				{
					if (gt_result != null && gf_isset(gv_varManOperations[gt_result]))
						gt_result	= gv_varManOperations[gt_result].hideSecondVar;
				}
			}
		}
		
		return gt_result;
	};
	
	/**
	 * Returns true when the node has at least one child (node that is connected via an edge starting at this node).
	 * 
	 * @returns {boolean} True when the node has at least one child.
	 */
	this.hasChildren = function ()
	{
		var gt_edges	= this.parentMacro.getEdges();
		for (var gt_edgeId in gt_edges)
		{
			var gt_edge	= gt_edges[gt_edgeId];
			if (gt_edge.start	== this.id && gf_isset(this.parentMacro.nodes["n" + gt_edge.end]))
				return true;
		}
		return false;
	};
	
	/**
	 * Returns true when the node has a parent (node that is connected via an edge ending at this node).
	 * 
	 * @returns {boolean} True when the node has a parent.
	 */
	this.hasParent = function ()
	{
		var gt_edges	= this.parentMacro.getEdges();
		for (var gt_edgeId in gt_edges)
		{
			var gt_edge	= gt_edges[gt_edgeId];
			if (gt_edge.end	== this.id && gf_isset(this.parentMacro.nodes["n" + gt_edge.start]))
				return true;
		}
		return false;
	};

    /**
     * @returns {Array.<GCedge>} a list of edges that are connected to the node
     */
    this.getConnectedEdges = function ()
    {
        var gt_edges = this.parentMacro.getEdges(),
            connectedEdges = [];

        for (var gt_edgeId in gt_edges)
        {
            var gt_edge	= gt_edges[gt_edgeId];
            if (gt_edge.end	== this.id || gt_edge.start == this.id) {
                connectedEdges.push(gt_edge);
            }
        }
        return connectedEdges;
    };
	
	/**
	 * Returns the deactivation status of this node.
	 * 
	 * @param {boolean} draw When set to true the deactivation status will be returned also depending on the majorStartNode status.
	 * @returns {boolean} True when this node is deactivated.
	 */
	this.isDeactivated = function (draw)
	{
		if (this.isStart() && !this.isMajorStartNode() && gf_isset(draw) && draw === true)
			return true;
			
		return this.deactivated === true;
	};
	
	/**
	 * Returns true if this node is an end node.
	 * 
	 * @param {boolean} draw When set to true the node will also be checked for children.
	 * @returns {boolean} True when the node is an end node.
	 */
	this.isEnd = function (draw)
	{
		/*
		if (gf_isset(draw) && draw === true)
			return this.end === true || !this.hasChildren();
		*/
		return this.end === true;
	};
	
	/**
	 * Returns if the node is the major start node of the internal behavior.
	 * 
	 * @returns {boolean} Is major start node of internal behavior.
	 */
	this.isMajorStartNode = function ()
	{
		return this.majorStartNode === true;
	};
	
	/**
	 * Returns true if this node is an start node.
	 * 
	 * @param {boolean} draw When set to true the node will also be checked for a parent node.
	 * @returns {boolean} True when the node is a start node.
	 */
	this.isStart = function (draw)
	{
		/*
		if (gf_isset(draw) && draw === true)
			// return this.start === true || !this.hasParent();
			return !this.hasParent();
		*/
		return this.start === true;
	};

    /**
     * @returns {boolean} true if the action should be automatically executed
     */
    this.isAutoExecute = function ()
    {
        return this.autoExecute === true;
    };

    /**
     * @param val {boolean}
     */
    this.setAutoExecute = function (val)
    {
        this.autoExecute = val == true;
    };

    /**
     * @returns {boolean} true if the node supports automatic execution
     */
    this.isAutoExecuteSupported = function () {
        return this.type === "receive";
    };

	/**
	 * Updates the conversation of the node.
	 * 
	 * @param {String} conversation The new conversation.
	 * @returns {void}
	 */
	this.setConversation = function (conversation)
	{
		if (gf_isset(conversation))
		{
			this.conversation = conversation;
		}
	};
	
	/**
	 * Updates the node's comment.
	 * 
	 * @param {String} comment The new comment.
	 * @returns {void}
	 */
	this.setComment = function (comment)
	{
		this.comment = comment;
	};
	
	/**
	 * Updates the correlationId of the node.
	 * 
	 * @param {String} correlationId The new correlationId.
	 * @returns {void}
	 */
	this.setCorrelationId = function (correlationId)
	{
		if (gf_isset(correlationId))
		{
			this.correlationId = correlationId;
		}
	};
	
	/**
	 * Update the settings of the "create subjects" predefined action.
	 * 
	 * @param {Object} An object holding the necessary data for the subjects to be created.
	 * @returns {void}
	 */
	this.setCreateSubjects = function (createSubjects)
	{
		if (this.getType() == "$createsubjects" && gf_isset(createSubjects))
		{
			if (gf_isset(createSubjects.subject))
				this.createSubjects.subject		= createSubjects.subject;
			if (gf_isset(createSubjects.storevar))
				this.createSubjects.storevar	= createSubjects.storevar;
			if (gf_isset(createSubjects.min))
				this.createSubjects.min			= createSubjects.min;
			if (gf_isset(createSubjects.max))
				this.createSubjects.max			= createSubjects.max;
		}
	};
	
	/**
	 * Mark this node as end node.
	 * 
	 * @param {boolean} end When set to true the node will be handled as an end node.
	 * @returns {void}
	 */
	this.setEnd = function (end)
	{
		if (gf_isset(end))
		{
			this.end = end === true;
		}
	};
	
	/**
	 * Update the id attribute of this node.
	 * 
	 * @param {String} id The id of the node.
	 * @returns {void}
	 */
	this.setId = function (id)
	{
		if (gf_isset(id))
		{
			this.id = id;
		}
	};
	
	/**
	 * Updates the macro associated with this node.
	 * 
	 * @param {String} macro The id of the macro.
	 * @returns {void};
	 */
	this.setMacro = function (macro)
	{
		if (gf_isset(macro) && this.getType() == "macro")
		{
			this.macro = macro;
		}
	};
	
	/**
	 * Mark the node as the major startNode of the internal behavior.
	 * Only one major start node can be active per internal behavior.
	 * 
	 * @param {boolean} majorStartNode When set to true the node will be marked as the majoor start node of the internal behavior. On false the start node will be transformed to a minor start node.
	 */
	this.setMajorStartNode = function (majorStartNode)
	{
		if (gf_isset(majorStartNode) && this.parentMacro.id == "##main##")
		{
			if (majorStartNode === true)
			{
				var gt_nodes	= this.parentMacro.getNodes();
				
				// set all start nodes to minor start nodes
				for (var gt_nodeId in gt_nodes)
				{
					gt_nodes[gt_nodeId].setMajorStartNode(false);
				}
				
				this.majorStartNode = true;
			}
			else
			{
				this.majorStartNode = false;
			}
		}
	};
	
	/**
	 * Update the options of predefined actions.
	 * 
	 * @param {Object} options The options of a predefined action.
	 * @returns {void}
	 */
	this.setOptions = function (options)
	{
		if (gf_isset(options))
		{
			this.options = options;
		}
	};
	
	/**
	 * Mark this node as start node.
	 * 
	 * @param {boolean} start When set to true the node will be handled as a start node.
	 * @returns {void}
	 */
	this.setStart = function (start)
	{
		if (gf_isset(start) && this.parentMacro.id == "##main##")
		{
			if (start === true)
			{
				var gt_nodes		= this.parentMacro.getNodes();
				var gt_majorExists	= false;
				
				// check if already a start node in the internal behavior
				for (var gt_nodeId in gt_nodes)
				{
					if (gt_nodes[gt_nodeId].isMajorStartNode())
					{
						gt_majorExists	= true;
						break;
					}
				}
				
				if (!gt_majorExists)
					this.majorStartNode = true;
			}
			this.start = start === true;
		}
	};
	
	/**
	 * Update the node's text attribute.
	 * 
	 * @param {String} text The new label of the node.
	 * @returns {void}
	 */
	this.setText = function (text)
	{
		if (gf_isset(text))
		{
			this.text = text;
		}
	};
	
	/**
	 * Update the node's type. Possible values are: "send", "receive", "end", "action".
	 * 
	 * @param {String} type Type of the node.
	 * @returns {void}
	 */
	this.setType = function (type)
	{
		if (gf_isset(type))
		{
			type = type.toLowerCase();
			
			this.type = type;
		}
	};

    /**
     * Sets the user-defined manual offset for the node position
     *
     * @param {null|{dx: int, dy: int}} offset
     * @returns {void}
     */
    this.setManualPositionOffset = function (offset)
    {
        this.manualPositionOffset = offset;
    };

	/**
	 * Updates the variable of the node.
	 * 
	 * @param {String} variable The new variable.
	 * @returns {void}
	 */
	this.setVariable = function (variable)
	{
		if (gf_isset(variable))
		{
			this.variable = variable;
		}
	};
	
	/**
	 * Update the settings of the variable manipulation.
	 * 
	 * @param {Object} An object holding the necessary data for the variable manipulation.
	 * @returns {void}
	 */
	this.setVarMan = function (varMan)
	{
		if (this.getType() == "$variableman" && gf_isset(varMan))
		{
			if (gf_isset(varMan.var1))
				this.varMan.var1	= varMan.var1;
			if (gf_isset(varMan.var2))
				this.varMan.var2	= varMan.var2;
			if (gf_isset(varMan.storevar))
				this.varMan.storevar	= varMan.storevar;
			if (gf_isset(varMan.operation))
				this.varMan.operation	= varMan.operation;
		}
	};
	
	/**
	 * Returns the node's label depending on its type.
	 * This method will return "S" for a send node, "R" for a receive node, "" for an end node and the node's text attribute for every other type.
	 * 
	 * @returns {String} The label of the node that will be displayed in the graph.
	 */
	this.textToString = function ()
	{
		var type	= this.type.toLowerCase();
		var text	= this.text;
		
		if (this.isEnd(true))
			type = "end";
			
			// gf_newlineToCamelCase
			
		if (type == "macro")
		{
			var gt_macroName	= this.getMacro() != "" ? this.getMacro() : "";
				gt_macroName	= gf_isset(this.parentBehavior.macros[gt_macroName]) ? this.parentBehavior.macros[gt_macroName].name : gt_macroName;
			
			text = "Macro: " + gt_macroName;
		}
		else if (type == "$variableman")
		{
			if (text.replace(/\ /gi, "") == "")
				text	= "";
			else
				text	= text + " = ";
				
			if (this.getVarMan("operation", "hideSecondVar"))
			{
				if (this.getVarMan("operation") != "new")
				{
					text += this.getVarMan("operation", "name") + " ";
				}
				text += this.getVarMan("var1", "name");
			}
			else
			{
				text += this.getVarMan("var1", "name") + " " + this.getVarMan("operation", "name") + " " + this.getVarMan("var2", "name");
			}
			
			text += " =: " + this.getVarMan("storevar", "name");
		}
		else if (type == "$createsubjects")
		{
			text	= gv_predefinedActions[type.substr(1)].label + "\n";
				
			var gt_cs_subject	= this.getCreateSubjects("subject", "name");
			var gt_cs_min		= this.getCreateSubjects("min");
			var gt_cs_max		= this.getCreateSubjects("max");
			var gt_cs_variable	= this.getCreateSubjects("storevar", "name");
			
			if (gt_cs_min == "-1" && gt_cs_max == "-1")
			{
				text += "all of ";
			}
			else
			{
				text += gt_cs_min + " to " + gt_cs_max + " of ";
			}
			
			text += gt_cs_subject + " =: " + gt_cs_variable;
		}
		else if (type.length > 0 && type.charAt(0) == '$' && gf_isset(gv_predefinedActions[type.substr(1)]))
		{
			var gt_predefAction		= gv_predefinedActions[type.substr(1)];
			var gt_useWildcard		= gt_predefAction.wildcard;
			
			var gt_messageType		= gf_isset(this.options.message)		? this.options.message			: null;
			var gt_subject			= gf_isset(this.options.subject)		? this.options.subject			: null;
			var gt_conversation			= gf_isset(this.options.conversation)		? this.options.conversation			: null;
			var gt_correlationId	= gf_isset(this.options.correlationId)	? this.options.correlationId	: null;
			var gt_state			= gf_isset(this.options.state)			? "n" + this.options.state		: null;
			
			// message type
			if (gt_messageType != null && gf_isset(gv_graph.messageTypes[gt_messageType]))
				gt_messageType	= gf_newlineToCamelCase(gv_graph.messageTypes[gt_messageType]);
			else
				gt_messageType	= gt_useWildcard ? "*" : "";
				
			// subject
			if (gt_subject != null && gf_isset(gv_graph.subjects[gt_subject]))
				gt_subject	= gf_newlineToCamelCase(gv_graph.subjects[gt_subject].getText());
			else
				gt_subject	= gt_useWildcard ? "*" : "";
				
			// conversation
			if (gt_conversation != null && gf_isset(gv_graph.conversations[gt_conversation]))
				gt_conversation	= gf_newlineToCamelCase(gv_graph.conversations[gt_conversation]);
			else
				gt_conversation	= gt_useWildcard ? "*" : "";
				
			// correlationId
			if (gt_correlationId != null && gf_isset(this.parentBehavior.variables[gt_correlationId]))
				gt_correlationId	= gf_newlineToCamelCase(this.parentBehavior.variables[gt_correlationId]);
			else if (gt_correlationId == "##nid##")
				gt_correlationId	= "nID";
			else if (gt_correlationId == "##cid##")
				gt_correlationId	= "cID";
			else
				gt_correlationId	= gt_useWildcard ? "*" : "";
				
			// state
			if (gt_state != null && gf_isset(this.parentBehavior.getMacro("##main##").nodes[gt_state]))
				gt_state	= gf_newlineToCamelCase(this.parentBehavior.getMacro("##main##").nodes[gt_state].getText());
			else
				gt_state	= gt_useWildcard ? "*" : "";
			
			
			var gt_options	= [];
			if (gt_predefAction.message)
				gt_options[gt_options.length]	= gt_messageType;
				
			if (gt_predefAction.subject)
				gt_options[gt_options.length]	= gt_subject;
				
			if (gt_predefAction.correlationid && (gt_correlationId != "" && gt_correlationId != "*" || gt_predefAction.conversation && gt_conversation != "" && gt_conversation != "*"))
				gt_options[gt_options.length]	= gt_correlationId;
				
			if (gt_predefAction.conversation && gt_conversation != "" && gt_conversation != "*")
				gt_options[gt_options.length]	= gt_conversation;
				
			if (gt_predefAction.state)
				gt_options[gt_options.length]	= gt_state;
			
			text	= gt_predefAction.label;
			
			if (gt_options.length > 0)
			{
				var gt_optionsString	= "";
				for (var gt_optId in gt_options)
				{
					gt_optionsString += gt_options[gt_optId] + ", ";
				}
				
				if (gt_optionsString.length > 2)
					gt_optionsString	= gt_optionsString.substr(0, gt_optionsString.length-2);
				
				text += "\n(" + gt_optionsString + ")";
			}
		}
		
		if (gf_isset(gv_nodeTypes[type]) && gf_isset(gv_nodeTypes[type].text))
		{
			text	= gv_nodeTypes[type].text;
		}
		else if (type == "action" && this.getVariable() != null && this.getVariable() != "")
		{
			text	+= " (" + this.getVariable("name") + ")"
		}
		return text;
	};
	
	// init
	this.setText(text);
	this.setId(id);
	this.setType(type);
}