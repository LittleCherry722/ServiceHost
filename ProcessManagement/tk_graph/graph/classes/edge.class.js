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
 * The class GCedge contains all information about an edge between two nodes on the behavioral view.
 * 
 * @private
 * @class represents an edge in the graph
 * @param {GCbehavior} parent The parent instance of GCbehavior.
 * @param {int} start The id of the start node.
 * @param {int} end The id of the end node.
 * @param {String} text The label of the edge.
 * @param {String} relatedSubject The id of the subject that is the sender or a receiver of the currently selected message.
 * @param {String} type The type of the edge. Either "exitcondition" or "timeout".
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
	 * The id of the subject that is the sender or a receiver of the currently selected message.
	 * 
	 * @type String
	 */
	this.relatedSubject	= relatedSubject;
	
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
	 * This can either be an exitcondition or a timeout.
	 * 
	 * @type String
	 */
	this.type	= "exitcondition";
	
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
	 * Returns the id of the edge's end node.
	 * 
	 * @returns {int} The id of the target node.
	 */
	this.getEnd = function ()
	{
		return this.end;
	};
	
	/**
	 * Returns the related subject.
	 * 
	 * @see GCedge.relatedSubject
	 * @returns {String} The id of the related subject.
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
			return this.timer.getTimestamp();
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
	 * @returns {String} The type of the edge. Currently the following types are possible: timeout, exitcondition (default)
	 */
	this.getType = function ()
	{
		if (this.type == "timeout")
		{
			return this.type;
		}
		else
		{
			return "exitcondition";
		}
	};
	
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
	 * Returns the optional status of this edge.
	 * 
	 * @returns {boolean} True when the edge is optional (used for modal split / modal join).
	 */
	this.isOptional = function ()
	{
		return this.optional === true;
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
	 * Sets the related subject.
	 * 
	 * @see GCedge.relatedSubject
	 * @param {String} relatedSubject The related subject.
	 * @returns {void}
	 */
	this.setRelatedSubject = function (relatedSubject)
	{
		var startNode		= this.parent.getNode(this.start);
		
		if (gf_isset(relatedSubject) && relatedSubject != "" && startNode != null && (startNode.getType() == "receive" || startNode.getType() == "send"))
			this.relatedSubject = relatedSubject;
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
	 * @param {String} type The type of the edge. This can be "exitCondition" or "timeout".
	 * @returns {void}
	 */
	this.setType = function (type)
	{
		if (gf_isset(type))
		{
			type = type.toLowerCase();
			if (type == "exitcondition" || type == "timeout")
			{
				this.type = type;
			}
		}
	}
	
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
			return "Timeout" + "\n(" + this.timer.getTimeString("unit") + ")";
		}
		
		// return exit condition
		else
		{
			var gt_startNode		= this.parent.getNode(this.start);
			var gt_relatedSubject	= this.relatedSubject != null ? this.relatedSubject : "";
				gt_relatedSubject	= gf_isset(gv_graph.subjects[gt_relatedSubject]) ? gv_graph.subjects[gt_relatedSubject].getText() : gt_relatedSubject;
			// return this.text + "\n(" + (gt_startNode.getType() == "receive" ? "from" : "to") + ": " + gt_relatedSubject + ")";
			return this.text + (this.getRelatedSubject() != null ? "\n(" + (gt_startNode.getType() == "receive" ? "from" : "to") + ": " + gt_relatedSubject + ")" : "");
		}
		
		/*
		var gt_startNode		= this.parent.getNode(this.start);
		var gt_relatedSubject	= gf_isset(gv_graph.subjects[this.relatedSubject]) ? gv_graph.subjects[this.relatedSubject].getText() : this.relatedSubject;
		
		return this.text + (this.getRelatedSubject() != null ? "\n(" + (gt_startNode.getType() == "receive" ? "from" : "to") + ": " + gt_relatedSubject + ")" : "");
		*/
	};
	
	
	// init
	this.setType(type);
	
	if (type == "timeout")
	{
		this.setTimer(text);
		this.text = "";
	}
}