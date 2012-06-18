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
 * The node class represents a node in a behavioral view.
 * 
 * @class represents a node in a behavioral view
 * @param {String} id The id of the node.
 * @param {String} text The label of the node.
 * @param {String} type The type of the node. Possible values are "send", "receive", "end", "action". (default: "action")
 * @returns {void}
 */
function GCnode (id, text, type)
{	
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
	 * Activates the node.
	 * 
	 * @returns {void}
	 */
	this.activate = function ()
	{
		this.deactivated = false;
	}
	
	/**
	 * Deactivates the node.
	 * 
	 * @returns {void}
	 */
	this.deactivate = function ()
	{
		this.deactivated = true;
	}
	
	/**
	 * Returns the id of this node.
	 * 
	 * @returns {String} The id of the node.
	 */
	this.getId = function ()
	{
		return this.id;
	}
	
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
		
		if (this.isEnd())
			type = "end";
		
		if (gf_isset(gv_nodeTypes[type]) && gf_isset(gv_nodeTypes[type].shape))
		{
			shape	= gv_nodeTypes[type].shape.toLowerCase();
		}
		return shape;
	}
	
	/**
	 * Returns the value of the node's text attribute.
	 * 
	 * @returns {String} The node's text.
	 */
	this.getText = function ()
	{
		return this.text;
	}
	
	/**
	 * Returns the node's label depending on its type.
	 * This method will return "S" for a send node, "R" for a receive node, "" for an end node and the node's text attribute for every other type.
	 * 
	 * @returns {String} The label of the node that will be displayed in the graph.
	 */
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
	
	/**
	 * Returns the node's type.
	 * This can either be "send", "receive", "end" or "action".
	 * 
	 * @returns {String} The node's type.
	 */
	this.getType = function ()
	{
		return this.type.toLowerCase();
	}
	
	/**
	 * Returns the deactivation status of this node.
	 * 
	 * @returns {boolean} True when this node is deactivated.
	 */
	this.isDeactivated = function ()
	{
		return this.deactivated === true;
	}
	
	/**
	 * Returns true if this node is an end node.
	 * 
	 * @returns {boolean} True when the node is an end node.
	 */
	this.isEnd = function ()
	{
		return this.end === true;
	}
	
	/**
	 * Returns true if this node is an start node.
	 * 
	 * @returns {boolean} True when the node is a start node.
	 */
	this.isStart = function ()
	{
		return this.start === true;
	}
	
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
	}
	
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
	}
	
	/**
	 * Mark this node as start node.
	 * 
	 * @param {boolean} start When set to true the node will be handled as a start node.
	 * @returns {void}
	 */
	this.setStart = function (start)
	{
		if (gf_isset(start))
		{
			this.start = start === true;
		}
	}
	
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
	}
	
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
	}
	
	// init
	this.setText(text);
	this.setId(id);
	this.setType(type);
}