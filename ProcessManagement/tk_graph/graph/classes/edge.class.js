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
 * The class GCedge contains all information about an edge between two nodes on the behavioral view.
 * 
 * @private
 * @class represents an edge in the graph
 * @param {GCbehavior} parent The parent instance of GCbehavior.
 * @param {int} start The id of the start node.
 * @param {int} end The id of the end node.
 * @param {String} text The label of the edge.
 * @param {Object} relatedSubject An object containing the id of the subject that is the sender or a receiver of the currently selected message. (further attributes: min, max, createNew)
 * @param {String} type The type of the edge. Either "exitcondition", "errorcondition" or "timeout".
 * @returns {void}
 */
function GCedge (parent, start, end, text, relatedSubject, type)
{
	// when no start node is given, set it to 0
	if (!gf_isset(start) || parseInt(start) != start)
		start = 0;
	
	// when no end node is given, set it to 0
	if (!gf_isset(end) || parseInt(end) != end)
		end = 0;
	
	// when no text is given set the text variable to an empty string
	if (!gf_isset(text))
		text = "";
	
	// when no relatedSubject is given, set it to null
	if (!gf_isset(relatedSubject))
		relatedSubject = null;
		
	/**
	 * The correlationId of the edge.
	 * Can either be "##cid##" (current ID), "##nid##" (new ID) or any variable.
	 * 
	 * @type String
	 */
	this.correlationId	= "";
	
	/**
	 * A flag to indicate whether or not the edge is deactivated. Deactivated edges are displayed in a different way.
	 * 
	 * @type boolean
	 */
	this.deactivated	= false;
	
	/**
	 * The id of the target node.
	 * 
	 * @type int
	 */
	this.end	= end;
	
	/**
	 * The text of an error condition.
	 * 
	 * @type String
	 */
	this.exception	= "";
	
	/**
	 * Manul timeout.
	 * 
	 * @type boolean
	 */
	this.manualTimeout	= false;
	
	/**
	 * A flag to indicate whether or not the edge is optional (needed for modal-split and modal-join).
	 * 
	 * @type boolean
	 */
	this.optional	= false;
	
	/**
	 * A reference to the parent instance of GCbehavior used for addressing the start and the end node.
	 * This is the only attribute of Edge that can not be modified.
	 * 
	 * @type GCbehavior
	 */
	this.parent	= parent;
	
	/**
	 * Set to a number >= 1.
	 * Only used for exitconditions with startNode = receive.
	 * 
	 * @type int
	 */
	this.priority	= 1;
	
	/**
	 * The id of the subject that is the sender or a receiver of the currently selected message.
	 * - id: the id of the relatedSubject
	 * - min: the min number of messages to receive / send (-1 = infinite)
	 * - max: the min number of messages to receive / send (-1 = infinite)
	 * - createNew: a boolean value that is currently not used
	 * - variable: a variable defined within an internal behavior which stores a set of messages (subjectprovider, message, correlationId)
	 * 
	 * @type Object
	 */
	this.relatedSubject	= {id: null, min: -1, max: -1, createNew: false, variable: null};
	
	/**
	 * The id of the start node of this edge.
	 * 
	 * @type int
	 */
	this.start	= start;
	
	/**
	 * The label of the edge.
	 * When the edge's start node is either a send or a receive node the text of this edge is the message sent from one subject to another.
	 * 
	 * @type String
	 */
	this.text	= text;
	
	/**
	 * An instance of GCtime that holds a timeout for a certain action.
	 * 
	 * @type GCtime
	 */
	this.timer	= new GCtime();
	
	/**
	 * The type of the edge.
	 * This can either be an exitcondition, errorcondition or a timeout.
	 * 
	 * @type String
	 */
	this.type	= "exitcondition";
	
	/**
	 * Variable of the edge.
	 * A variable is defined within an internal behavior and stores a set of messages (subjectprovider, message, correlationId).
	 */
	this.variable	= null;
	
	/**
	 * Activates an edge.
	 * 
	 * @returns {void}
	 */
	this.activate = function ()
	{
		this.deactivated = false;
	};
	
	/**
	 * Deactivates an edge.
	 * 
	 * @returns {void}
	 */
	this.deactivate = function ()
	{
		this.deactivated = true;
	};
	
	/**
	 * Returns the correlationId of the edge.
	 * 
	 * @returns {String} The edge's correlationId.
	 */
	this.getCorrelationId = function (type)
	{
		if (!gf_isset(type))
			type	= "id";
			
		if (type == "name")
		{
			var gt_variables	= this.parent.variables;
			
			if (this.correlationId == "##cid##")
				return "cID";
			else if (this.correlationId == "##nid##")
				return "nID"
			else if (this.correlationId != null && gf_isset(gt_variables[this.correlationId]))
				return gt_variables[this.correlationId];
		}
		
		return this.correlationId;
	};
	
	/**
	 * Returns the id of the edge's end node.
	 * 
	 * @returns {int} The id of the target node.
	 */
	this.getEnd = function ()
	{
		return this.end;
	};
	
	/**
	 * Returns the text of an error condition.
	 * 
	 * @returns {String} The text of an error condition.
	 */
	this.getException = function ()
	{
		return this.exception;
	};
	
	/**
	 * Returns the messageType associated with this edge (only for startNode = receive | send).
	 * 
	 * @returns {String} The associated messageType.
	 */
	this.getMessageType = function ()
	{
		var gt_messageTypeId	= this.getMessageTypeId();
		
		if (gf_isset(gv_graph.messageTypes[gt_messageTypeId]))
		{
			return gv_graph.messageTypes[gt_messageTypeId];
		}
		return "";
	};
	
	/**
	 * Returns the ID of the messageType associated with this edge (only for startNode = receive | send).
	 * 
	 * @returns {String} The ID of the associated messageType.
	 */
	this.getMessageTypeId = function ()
	{
		var gt_startNode		= this.parent.getNode(this.start);
		
		if (gt_startNode != null)
		{
			if (gt_startNode.getType() == "send" || gt_startNode.getType() == "receive")
			{
				return this.text.substr(0, 1) == "m" ? this.text : "unknown";
			}
		}
		return "";
	};
	
	/**
	 * Returns the priority of the edge (for exit conditions starting by receive nodes).
	 * 
	 * @returns {int} The edge's priority.
	 */
	this.getPriority = function ()
	{
		return this.priority;
	};
	
	/**
	 * Returns the related subject.
	 * 
	 * @see GCedge.relatedSubject
	 * @param {String} attribute Either "all", "id", "name", "min", "max", "createNew", "variable".
	 * @returns {String|Object|int} The id of the related subject, its name, the min- or max- number of messages or the whole object depending on "attribute".
	 */
	this.getRelatedSubject = function (attribute)
	{		
		var startNode		= this.parent.getNode(this.start);
		var relatedSubject	= this.relatedSubject;
		
		if (startNode == null || (startNode.getType() != "receive" && startNode.getType() != "send") || relatedSubject == null)
		{
			relatedSubject = null;
		}
		else if (relatedSubject.id == null || relatedSubject.id == "")
		{
			relatedSubject = null;
		}
		else
		{
			if (!gf_isset(attribute))
				attribute = "id";
			
			attribute	= attribute.toLowerCase();
				
			if (attribute == "id")
			{
				relatedSubject	= relatedSubject.id;
			}
			else if (attribute == "name")
			{
				var gt_relatedSubject	= relatedSubject.id;
				
				relatedSubject	= gf_isset(gv_graph.subjects[gt_relatedSubject]) ? gv_graph.subjects[gt_relatedSubject].getText() : gt_relatedSubject;
			}
			else if (attribute == "multi")
			{
				var gt_relatedSubject	= relatedSubject.id;
				
				relatedSubject	= gf_isset(gv_graph.subjects[gt_relatedSubject]) ? gv_graph.subjects[gt_relatedSubject].isMulti() : false;
			}
			else if (attribute == "min")
			{
				relatedSubject	= relatedSubject.min;
			}
			else if (attribute == "max")
			{
				relatedSubject	= relatedSubject.max;
			}
			else if (attribute == "createnew")
			{
				relatedSubject	= relatedSubject.createNew === true;
			}
			else if (attribute == "variable")
			{
				relatedSubject	= relatedSubject.variable;
			}
		}
		
		return relatedSubject;
	};
	
	/**
	 * Returns the id of the the start node.
	 * 
	 * @returns {int} The id of the start node.
	 */
	this.getStart = function ()
	{
		return this.start;
	};
	
	/**
	 * Returns the label of this edge.
	 * If the edge's type is set to "timeout" the timestamp of the edge's timer will be returned.
	 * 
	 * @returns {String} The label of the edge.
	 */
	this.getText = function (save)
	{
		if (this.type == "timeout" && gf_isset(save) && save === true)
		{
			return "" + this.timer.getTimestamp();
		}
		
		if (this.type == "errorcondition" && gf_isset(save) && save === true)
		{
			return "" + this.getException();
		}
		
		return this.text;
	};
	
	/**
	 * Returns the stored time as either a timestamp (timestamp) or a string (abbr, unit, full).
	 * By setting type to "example" an example timeString will be returned to demonstrate the use of the pattern.
	 * 
	 * @see GCtime::getTime(), GCtime::getTimestamp(), GCtime::getTimestring(), GCtime::getExample()
	 * 
	 * @param {String} type The type of the return (timestamp, abbr, unit, full, example)
	 * @returns {String|int} Returns the stored time as either a timestamp (int) or a String.
	 */
	this.getTimer = function (type)
	{
		if (!gf_isset(type))
			type = "unit";
			
		return this.timer.getTime(type);
	};
	
	/**
	 * Returns the type of the edge.
	 * 
	 * @returns {String} The type of the edge. Currently the following types are possible: timeout, exitcondition (default), errorcondition
	 */
	this.getType = function ()
	{
		if (this.type == "timeout" || this.type == "errorcondition")
		{
			return this.type;
		}
		else
		{
			return "exitcondition";
		}
	};
	
	/**
	 * Returns the type of the edge's startNode.
	 * 
	 * @returns {String} The type of the edge's startNode.
	 */
	this.getTypeOfStartNode = function ()
	{
		var startNode		= this.parent.getNode(this.start);
		
		return startNode == null ? "action" : startNode.getType();
	};
	
	/**
	 * Returns the variable of the edge.
	 * 
	 * @returns {void} The edge's variable.
	 */
	this.getVariable = function (type)
	{
		if (!gf_isset(type))
			type	= "id";
			
		if (type == "name")
		{
			var gt_variables	= this.parent.variables;
			if (this.variable != null && gf_isset(gt_variables[this.variable]))
				return gt_variables[this.variable];
		}
		
		return this.variable;
	}
	
	/**
	 * Returns the deactivate status of this edge.
	 * 
	 * @returns {boolean} True when the edge is deactivated.
	 */
	this.isDeactivated = function ()
	{
		return this.deactivated === true;
	};
	
	/**
	 * Returns true when the edge's timeout is set to manual.
	 * 
	 * @returns {boolean} True when timeout is manual.
	 */
	this.isManualTimeout = function ()
	{
		return this.manualTimeout === true;
	};
	
	/**
	 * Returns the optional status of this edge.
	 * 
	 * @returns {boolean} True when the edge is optional (used for modal split / modal join).
	 */
	this.isOptional = function ()
	{
		return this.optional === true && this.getTypeOfStartNode() == "modalsplit";
	};
	
	/**
	 * Update the correlationId of the edge.
	 * 
	 * @param {String} correlationId The new correlationId of the edge.
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
	 * Sets the id of the target node.
	 * 
	 * @param {int} end The end node.
	 * @returns {void}
	 */
	this.setEnd = function (end)
	{
		if (gf_isset(end) && parseInt(end) == end)
			this.end = end;
	};
	
	/**
	 * Sets the text of an error condition.
	 * 
	 * @param {String} text The updated text of an error condition.
	 * @returns {void} 
	 */
	this.setException = function (text)
	{
		if (gf_isset(text))
		{
			this.exception = text;
		}
	};
	
	/**
	 * Set a timeout edge to be manual.
	 * 
	 * @param {boolean} manual When set to true the timeout edge will be set to "manual".
	 * @returns {void}
	 */
	this.setManualTimeout = function (manual)
	{
		this.manualTimeout	= gf_isset(manual) && manual === true;
	};
	
	/**
	 * Set the edge as an optional edge.
	 * 
	 * @param {boolean} option When set to true the edge will be treated as an optional edge (modal split / modal join)
	 * 
	 */
	this.setOptional = function (optional)
	{
		this.optional = gf_isset(optional) && optional === true;
	};

	/**
	 * Update the edge's priority.
	 * 
	 * @param {int} priority The priority of the edge.
	 * @returns {void}
	 */
	this.setPriority = function (priority)
	{
		if (gf_isset(priority))
		{
			this.priority	= parseInt(priority) == priority ? parseInt(priority) : 1;
		}
	};

	/**
	 * Sets the related subject.
	 * 
	 * @see GCedge.relatedSubject
	 * @param {String|Object} relatedSubject The related subject.
	 * @returns {void}
	 */
	this.setRelatedSubject = function (relatedSubject)
	{
		var startNodeType		= this.getTypeOfStartNode();
		
		if (gf_isset(relatedSubject))
		{
			if (startNodeType == "receive" || startNodeType == "send")
			{
				if (relatedSubject != "" && relatedSubject != null)
				{
					if (typeof (relatedSubject) == 'string')
					{
						this.relatedSubject.id	= relatedSubject;
					}
					else
					{
						if (gf_isset(relatedSubject.id))
						{
							if (relatedSubject.id != "")
								this.relatedSubject.id	= relatedSubject.id;
							
							if (gf_isset(relatedSubject.min))
								this.relatedSubject.min	= relatedSubject.min;
							
							if (gf_isset(relatedSubject.max))
								this.relatedSubject.max	= relatedSubject.max;
								
							if (gf_isset(relatedSubject.createNew))
								this.relatedSubject.createNew	= relatedSubject.createNew === true;
								
							if (gf_isset(relatedSubject.variable))
								this.relatedSubject.variable	= relatedSubject.variable;
						}
					}
				}
			}
		}
	};
	
	/**
	 * Sets the id of the start node.
	 * 
	 * @param {int} start The id of the start node.
	 * @returns {void}
	 */
	this.setStart = function (start)
	{
		if (gf_isset(start) && parseInt(start) == start)
			this.start = start;
	};
	
	/**
	 * Sets the label of the edge.
	 * 
	 * @param {String} text The label of the edge.
	 * @returns {void}
	 */
	this.setText = function (text)
	{
		if (gf_isset(text))
			this.text = text;
	};
	
	/**
	 * Pass a timestamp or a proper timeString to the timer.
	 * 
	 * @see GCtime::setTime(), GCtime::setTimestamp(), GCtime::setTimeString()
	 * 
	 * @param {String|int} time Either a timestmap or a proper timeString.
	 * @returns {void}
	 */
	this.setTimer = function (time)
	{
		if (gf_isset(time))
			this.timer.setTime(time);
	};
	
	/**
	 * Sets the current type of the edge.
	 * Possible types are "exitcondition" (like messages), "timeout" (a timeout edge).
	 * 
	 * @param {String} type The type of the edge. This can be "exitCondition", "errorcondition" or "timeout".
	 * @returns {void}
	 */
	this.setType = function (type)
	{
		if (gf_isset(type))
		{
			type = type.toLowerCase();
			if (type == "exitcondition" || type == "timeout" || type == "errorcondition")
			{
				this.type = type;
			}
		}
	};
	
	/**
	 * Update the variable of the edge.
	 * 
	 * @param {String} variable The new variable of the edge.
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
	 * Returns the label of the edge including the reference to the related subject or timeout (if any).
	 * This will result in the following:<br /><br />
	 * <i>
	 * 		message<br />
	 * 		(from: subjectId)<br />
	 * </i>
	 * <br />
	 * or
	 * <br /><br />
	 * <i>
	 * 		message<br />
	 * 		(to: subjectId)<br />
	 * </i>
	 * <br />
	 * or (for Timeouts)
	 * <br /><br />
	 * <i>
	 * 		Timeout<br />
	 * 		(1w 2d 14h)<br />
	 * </i>
	 * <br />
	 * On edges where the start node is neither a receive nor a send node this method will only return the label of the edge or its timeout.
	 * 
	 * @returns {String} The label of the edge containing the relatedSubject (when the start node is either a send or a receive node) or the timeout
	 */
	this.textToString = function ()
	{
		var gt_text	= "";
		
		// return timeout
		if (this.type == "timeout")
		{
			return "Timeout" + (this.isManualTimeout() ? " (M)" : "") + "\n(" + this.timer.getTimeString("unit") + ")";
		}
		
		// return error condition
		if (this.type == "errorcondition")
		{
			return "Exception:\n" + this.exception;
		}
		
		// return exit condition
		else
		{
			var gt_startNode		= this.parent.getNode(this.start);
			var gt_variable			= this.getVariable();
			var gt_correlation		= this.getCorrelationId();
			
			if (gt_variable == null || gt_variable == "" || (gt_startNode.getType() != "send" && gt_startNode.getType() != "receive" && gt_startNode.getType() != "action"))
				gt_variable	= "";
			else
				gt_variable	= " =: " + this.getVariable("name");
			
			if (gt_correlation == null || gt_correlation == "")
				gt_correlation	= "";
			else
				gt_correlation	= " with (" + this.getCorrelationId("name") + ")"
			
			// messages
			if (gt_startNode.getType() == "send" || gt_startNode.getType() == "receive")
			{
				var gt_text				= this.getMessageType();
				var gt_relatedSubject	= this.getRelatedSubject("name");
				var gt_relatedMulti		= this.getRelatedSubject("multi");
				var gt_relatedMin		= this.getRelatedSubject("min");
				var gt_relatedMax		= this.getRelatedSubject("max");
				var gt_relatedMultiText	= "";
				var gt_relatedVariable	= this.getRelatedSubject("variable");
				
				if (gt_relatedMulti)
				{
					if (gt_relatedVariable != null && gt_relatedVariable != "")
					{
						var gt_variables	= this.parent.variables;
						if (gf_isset(gt_variables[gt_relatedVariable]))
						{							
								gt_relatedVariable = gt_variables[gt_relatedVariable];
						}
						gt_relatedMultiText = " (" + gt_relatedVariable + ")";
					}
					else if (gt_relatedMin == "-1" && gt_relatedMax == "-1")
					{
						gt_relatedMultiText = " (all)";
					}
					else
					{
						// gt_relatedMultiText = "\n(" + gt_relatedMin + " to " + gt_relatedMax + " messages)";
						gt_relatedMultiText = " (" + gt_relatedMin + " to " + gt_relatedMax + ")";
					}
				}
				else
				{
					gt_correlation	= "";
					gt_variable		= "";
				}
					
				if (gt_relatedSubject == "" || gt_relatedSubject == null)
				{
					return "" + gt_text + gt_variable;
				}
				return gt_text + "\n" + (gt_startNode.getType() == "receive" ? "(" + this.getPriority() + ") " : "") +
											(gt_startNode.getType() == "receive" ? "from" : "to") + ": " + gt_relatedSubject + gt_relatedMultiText + gt_correlation + gt_variable;
			}
			
			// all other exit conditions
			else
			{
				if (this.text.substr(0, 1) == "m")
				{
					return "message: " + (gf_isset(gv_graph.messageTypes[this.text]) ? gv_graph.messageTypes[this.text] : "unknown") + gt_variable;
				}
				
				return "" + this.text + gt_variable;
			}
			
			// return this.text + "\n(" + (gt_startNode.getType() == "receive" ? "from" : "to") + ": " + gt_relatedSubject + ")";
			// return this.text + (this.getRelatedSubject() != null ? "\n(" + (gt_startNode.getType() == "receive" ? "from" : "to") + ": " + gt_relatedSubject + ")" : "");
		}
	};
	
	
	// init
	this.setType(type);
	this.setRelatedSubject(relatedSubject);
	
	if (type == "timeout")
	{
		this.setTimer(text);
		this.text = "";
	}
	
	if (type == "errorcondition")
	{
		this.setException(text);
		this.text = "";
	}
}