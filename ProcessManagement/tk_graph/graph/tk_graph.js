/*
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
 * A Raphael rect.
 * This rect is needed for zoom and drag operations.
 * 
 * @private
 * @see Paper.rect() at the <a href="http://raphaeljs.com/reference.html#Paper.rect">Raphael documentation</a>
 * @type Element
 */
var gv_bgRect	= null;

/**
 * Information about the current viewbox.
 * The Object contains the following information:
 * <br />
 * - x: the x ordinate of the top left corner of the current view box<br />
 * - y: the y ordinate of the top left corner of the current view box<br />
 * - width: the width of the current view box<br />
 * - height: the height of the current view box<br />
 * - zoom: the uoom level of the current view box
 * 
 * @private
 * @type Object
 */
var gv_currentViewBox	= {x: 0, y: 0, width: 0, height: 0, zoom: 1};

/**
 * The position of the mouse cursor when the paper is dragged. (saved on the start of the drag action)
 * 
 * @private
 * @type Object
 */
var gv_mousePositionStart	= {x: 0, y: 0};

/**
 * A list of all children of all nodes.
 * The indexes of the object are the node ids.
 * The values of the object are arrays that contain all ids of the node's children.
 * 
 * @private
 * @type Object
 */
var gv_node_children	= {};	// relation: node => [children]

/**
 * A list of the parents to each node.
 * 
 * @private
 * @type Object
 */
var gv_node_parents		= {};	// relation: node => parent

/**
 * A flag set to true when a macro is called to avoid multiple redraws.
 * 
 * @private
 * @type boolean
 */
var gv_noRedraw	= false;

/**
 * A list of GCpaths on the paper.
 * The indexes are the paths' ids.
 * 
 * @private
 * @type Object
 */
var gv_objects_edges = {};

/**
 * A list of GClabels on the paper.
 * The indexes are the labels' ids.
 * 
 * @private
 * @type Object
 */
var gv_objects_nodes = {};

/**
 * Information about the original viewbox.
 * The Object contains the following information:
 * <br />
 * - x: the x ordinate of the top left corner of the original view box<br />
 * - y: the y ordinate of the top left corner of the original view box<br />
 * - width: the width of the original view box<br />
 * - height: the height of the original view box<br />
 * - zoom: the uoom level of the original view box
 * 
 * @private
 * @type Object
 */
var gv_originalViewBox	= {x: 0, y: 0, width: 0, height: 0, zoom: 1};

/**
 * The id of the currently loaded graph.
 * 
 * @private
 * @type String
 */
var gv_graphID	= "cv";

/**
 * Time measuring: times used for certain tasks
 * 
 * @private
 * @type Object
 */
var gv_times	= {};

/**
 * Time measuring: start times
 * 
 * @private
 * @type Object
 */
var gv_timeStart	= {};

/**
 * Checks the cardinality of a node.
 * Returns false when the limit for outgoing edges of a node is reached.
 * This avoids changing the type of an edge although it is not permitted.
 * Current limits:
 * - openIP / closeIP: one exit condition
 * - send: one exit condition + one timeout
 * - end: no edges
 * - all other: unlimited outgoing edges, only one exit condition + one timeout between the same nodes
 * 
 * @param {GCmacro} macro The currently loaded macro.
 * @param {int} start The ID of the start node.
 * @param {int} end The ID of the target node.
 * @param {String} desiredType The desired edge-type to check for availability.
 * @param {String} currentType The current edge-type to check against.
 * @param {String} action When set to "update" this method will return an alternative edge-type or null when no appropriate edge-type is available.
 * @returns {boolean|String} True, when the limit is not yet hit. The type of the clicked edge can still be changed. String when alternative is set to true.
 */
function gf_checkCardinality (macro, start, end, desiredType, currentType, action)
{

	var gt_result	= {allowed: false, type: null};
	
	if (!gf_isset(currentType))
		currentType = "";
	
	if (!gf_isset(action))
		action = "add";
		
	if (gf_isset(macro, start, end, desiredType))
	{
		if (macro != null)
		{
			var gt_edges			= macro.getEdges();
			var gt_startNode		= macro.getNode(start);
			
			if (gt_startNode != null)
			{
				var gt_isEndNode		= gt_startNode.isEnd();
				var gt_startNodeType	= gt_startNode.type.toLowerCase();

				// counts: timeout, exception, condition; between two nodes: bnTimeout = true | false, bnException = true | false, bnCondition = true | false				
				var gt_countCondition	= 0;
				var gt_countException	= 0;
				var gt_countTimeout		= 0;
				var gt_countBoolTrue	= 0;
				var gt_countBoolFalse	= 0;
				var gt_bnCondition		= 0;
				var gt_bnException		= 0;
				var gt_bnTimeout		= 0;
				var gt_bnBoolTrue		= 0;
				var gt_bnBoolFalse		= 0;
				
				var gt_countTotal		= 0;
				var gt_bnTotal			= 0;
				
				var gt_typeCondition	= "exitcondition";
				var gt_typeException	= "errorcondition";
				var gt_typeTimeout		= "timeout";
				var gt_typeBooleanTrue	= "booltrue";
				var gt_typeBooleanFalse	= "boolfalse";
				
				// no edges for end nodes
				if (gt_isEndNode)
				{
					gt_result.allowed	= false;
					gt_result.type		= null;
				}
				
				// all other node types
				else
				{
					var gt_edge	= null;
					for (var gt_edgeId in gt_edges)
					{
						gt_edge	= gt_edges[gt_edgeId];
						
						if (gt_edge.start == start && gf_isset(macro.nodes["n" + gt_edge.end]))
						{
							
							if (gt_edge.getType() == gt_typeCondition)
								gt_countCondition++;
							if (gt_edge.getType() == gt_typeException)
								gt_countException++;
							if (gt_edge.getType() == gt_typeTimeout)
								gt_countTimeout++;
							if (gt_edge.getType() == gt_typeBooleanFalse)
								gt_countBoolFalse++;
							if (gt_edge.getType() == gt_typeBooleanTrue)
								gt_countBoolTrue++;

							if (gt_edge.end == end)
							{
								if (gt_edge.getType() == gt_typeCondition)
									gt_bnCondition++;
								if (gt_edge.getType() == gt_typeException)
									gt_bnException++;
								if (gt_edge.getType() == gt_typeTimeout)
									gt_bnTimeout++;
								if (gt_edge.getType() == gt_typeBooleanFalse)
									gt_bnBoolFalse++;
								if (gt_edge.getType() == gt_typeBooleanTrue)
									gt_bnBoolTrue++;
							}
						}
					}
					
					// sum up the counters
					gt_countTotal	= gt_countCondition + gt_countException + gt_countTimeout + gt_countBoolFalse + gt_countBoolTrue;
					gt_bnTotal		= gt_bnCondition + gt_bnException + gt_bnTimeout + gt_bnBoolFalse + gt_bnBoolTrue;
					
					// merge node
					if (gt_startNodeType == "merge")
					{
						var allowedC	= false;
						var allowedX	= false;
						var typeC		= null;
						var typeX		= null;
						
						// for add action
						allowedC	= gt_countTotal == 0;
						
						if (action == "update")
						{
							allowedC	= true;
						}
						
						typeC		= allowedC ? gt_typeCondition : null;
						typeX		= typeC;
						
						gt_result.allowed	= desiredType == gt_typeCondition ? allowedC : allowedX;
						gt_result.type		= desiredType == gt_typeCondition ? typeC : typeX;
					}
					
					// iSIPempty node
					else if (gt_startNodeType == "$isipempty")
					{
						var allowedBF	= false;
						var allowedBT	= false;
						var allowedX	= false;
						var typeBF		= null;
						var typeBT		= null;
						var typeX		= null;
						
						// for add action
						allowedBF	= gt_countTotal < 2 && gt_countBoolFalse == 0 && gt_bnTotal == 0;
						allowedBT	= gt_countTotal < 2 && gt_countBoolTrue == 0 && gt_bnTotal == 0;
						
						if (action == "update")
						{
							allowedBF	= allowedBF || gt_countTotal <= 2 && gt_bnTotal == 1 && currentType == gt_typeBooleanFalse || gt_countBoolFalse == 0;
							allowedBT	= allowedBT || gt_countTotal <= 2 && gt_bnTotal == 1 && currentType == gt_typeBooleanTrue || gt_countBoolTrue == 0;
						}
						
						typeBF	= allowedBF ? gt_typeBooleanFalse : (allowedBT ? gt_typeBooleanTrue : null);
						typeBT	= allowedBT ? gt_typeBooleanTrue : (allowedBF ? gt_typeBooleanFalse : null);
						typeX	= typeBT;
						
						gt_result.allowed	= desiredType == gt_typeBooleanFalse ? allowedBF : (desiredType == gt_typeBooleanTrue ? allowedBT : allowedX);
						gt_result.type		= desiredType == gt_typeBooleanFalse ? typeBF : (desiredType == gt_typeBooleanTrue ? typeBT : typeX);
					}
					
					// modal split, modal join
					else if (gt_startNodeType == "modalsplit" || gt_startNodeType == "modaljoin")
					{
						var allowedC	= false;
						var allowedX	= false;
						var typeC		= null;
						var typeX		= null;
						
						// for add action
						allowedC	= gt_bnTotal == 0;
						
						if (action == "update")
						{
							allowedC	= allowedC || gt_bnTotal == 1;
						}
						
						typeC		= allowedC ? gt_typeCondition : null;
						typeX		= typeC;
						
						gt_result.allowed	= desiredType == gt_typeCondition ? allowedC : allowedX;
						gt_result.type		= desiredType == gt_typeCondition ? typeC : typeX;
					}
					
					// predefined actions
					else if (gt_startNodeType.substr(0, 1) == "$" || gt_startNodeType == "macro")
					{
						var allowedC	= false;
						var allowedX	= false;
						var typeC		= null;
						var typeX		= null;
						
						// for add action
						allowedC	= gt_countTotal == 0;
						
						if (action == "update")
						{
							allowedC	= allowedC || gt_bnTotal == 1;
						}
						
						typeC		= allowedC ? gt_typeCondition : null;
						typeX		= typeC;
						
						gt_result.allowed	= desiredType == gt_typeCondition ? allowedC : allowedX;
						gt_result.type		= desiredType == gt_typeCondition ? typeC : typeX;
					}
					
					// send, receive, action
					else if (gt_startNodeType == "send" || gt_startNodeType == "receive" || gt_startNodeType == "action")
					{
						var allowedC	= false;
						var allowedE	= false;
						var allowedT	= false;
						var allowedB	= false;
						var typeC		= null;
						var typeE		= null;
						var typeT		= null;
						var typeB		= null;
						
						if (gt_startNodeType == "send")
						{
							// for add action
							allowedC	= gt_countCondition == 0 && gt_bnException == 0;
							allowedE	= gt_bnTotal == 0 && gt_countCondition > 0;
							allowedT	= gt_countTimeout == 0 && gt_bnException == 0 && gt_countCondition > 0;
							
							if (action == "update")
							{
								allowedC	= allowedC || gt_bnTotal <= 2 && currentType == gt_typeCondition && gt_countCondition == 1 || gt_bnTotal <= 1 && gt_countCondition == 0;
								allowedE	= (gt_countCondition > 1 || gt_countCondition == 1 && currentType != gt_typeCondition) && (allowedE || gt_bnTotal == 1);
								allowedT	= (gt_countCondition > 1 || gt_countCondition == 1 && currentType != gt_typeCondition) && (allowedT || gt_bnTotal <= 2 && currentType == gt_typeTimeout && gt_countTimeout == 1 && gt_bnException == 0 || gt_bnTotal == 1 && gt_countTimeout == 0);
							}
						}
						else if (gt_startNodeType == "receive")
						{
							// for add action
							allowedC	= gt_bnCondition == 0 && gt_bnException == 0;
							allowedE	= gt_bnTotal == 0 && gt_countCondition > 0;
							allowedT	= gt_countTimeout == 0 && gt_bnException == 0 && gt_countCondition > 0;
							
							if (action == "update")
							{
								allowedC	= allowedC || gt_bnException == 0 && currentType == gt_typeCondition && gt_bnCondition == 1 || gt_bnTotal == 1;
								allowedE	= (gt_countCondition > 1 || gt_countCondition == 1 && currentType != gt_typeCondition) && (allowedE || gt_bnTotal == 1);
								allowedT	= (gt_countCondition > 1 || gt_countCondition == 1 && currentType != gt_typeCondition) && (allowedT || gt_bnTotal <= 2 && currentType == gt_typeTimeout && gt_countTimeout == 1 && gt_bnException == 0 || gt_bnTotal == 1 && gt_countTimeout == 0);
							}
						}
						else
						{
							// for add action
							allowedC	= gt_bnException == 0 && gt_bnCondition == 0;
							allowedE	= gt_bnTotal == 0;
							allowedT	= gt_countTimeout == 0 && gt_bnException == 0;
							
							if (action == "update")
							{
								allowedC	= allowedC || gt_bnException == 0 && currentType == gt_typeCondition && gt_bnCondition == 1 || gt_bnTotal == 1;
								allowedE	= allowedE || gt_bnTotal == 1;
								allowedT	= allowedT || gt_bnTotal <= 2 && gt_bnException == 0 && currentType == gt_typeTimeout && gt_countTimeout == 1 || gt_bnTotal == 1 && gt_countTimeout == 0;
							}	
						}
						
						typeC		= allowedC ? gt_typeCondition : (allowedT ? gt_typeTimeout : (allowedE ? gt_typeException : null));
						typeE		= allowedE ? gt_typeException : (allowedC ? gt_typeCondition : (allowedT ? gt_typeTimeout : null));
						typeT		= allowedT ? gt_typeTimeout : (allowedC ? gt_typeCondition : (allowedE ? gt_typeException : null));
						typeB		= typeC;
						
						if (desiredType == gt_typeCondition)
						{
							gt_result.allowed	= allowedC;
							gt_result.type		= typeC;							
						}
						else if (desiredType == gt_typeException)
						{
							gt_result.allowed	= allowedE;
							gt_result.type		= typeE;							
						}
						else if (desiredType == gt_typeTimeout)
						{
							gt_result.allowed	= allowedT;
							gt_result.type		= typeT;							
						}
						else
						{
							gt_result.allowed	= allowedB;
							gt_result.type		= typeB;	
						}
					}
				}
			}
		}
	}
	
	return gt_result;
}

/**
 * Deselect all GCpath elements.
 * 
 * @private
 * @returns {void}
 */
function gf_deselectEdges ()
{
	for (edgeId in gv_objects_edges)
	{
		gv_objects_edges[edgeId].deselect();
	}
}

/**
 * Deselect all GClabel elements.
 * 
 * @private
 * @returns {void}
 */
function gf_deselectNodes ()
{
	for (nodeId in gv_objects_nodes)
	{
		gv_objects_nodes[nodeId].deselect();
	}
}

/**
 * Checks if the given element(s) exist in the DOM tree.
 * You can pass any number of element ids to this method.
 * If at least one of the elements is not present in the DOM tree this method returns false.
 * 
 * @private
 * @param {String} element Any number of DOM element ids.
 * @returns {boolean} False when at least one of the elements is not present in the DOM tree.
 */
function gf_elementExists ()
{
	var gt_argv = arguments;
	var gt_argc = gt_argv.length;

	for (var gt_i = 0; gt_i < gt_argc; gt_i++)
	{
		if (document.getElementById(gt_argv[gt_i]) === null)
		{
			return false;
		}
	}
	
	return true;
}

/**
 * Checks if the given function(s) exists.
 * You can pass any number of function names to this method.
 * If at least one of the functions is not present this method returns false.
 * 
 * @private
 * @param {String} function Any number of function names.
 * @returns {boolean} False when at least one of the functions is not present.
 */
function gf_functionExists ()
{
	var gt_argv = arguments;
	var gt_argc = gt_argv.length;

	for (var gt_i = 0; gt_i < gt_argc; gt_i++)
	{
		if (typeof window[gt_argv[gt_i]] !== 'function')
		{
			return false;
		}
	}
	
	return gt_argc > 0;
}

/**
 * Retrieve the ids of the children of the node with the given id.
 * 
 * @private
 * @param {String} id The id of the node to retrieve the children nodes for.
 * @returns {String[]} Array of children ids or an empty Array when the node could not be found.
 */
function gf_getChildNodes (id)
{
	if (gf_isset(id))
	{
		if (gf_isset(gv_node_children["n" + id]) && gf_isArray(gv_node_children["n" + id]))
		{
			return gv_node_children["n" + id];
		}
	}
	
	return [];
}

/**
 * Returns the id of the parent node of the node with the given id.
 * 
 * @private
 * @param {String} id The id to retrieve the parent node for.
 * @returns {String} The id of the node's parent node or null when the node could not be found.
 */
function gf_getParentNode (id)
{
	if (gf_isset(id))
	{
		if (gf_isset(gv_node_parents["n" + id]))
		{
			return gv_node_parents["n" + id];
		}
	}
	
	return null;
}

/**
 * Maps the border-style attribute of a style set to Raphael's stroke-dasharray.
 * 
 * @private
 * @param {String} strokeStyle The borderStyle from a style set.
 * @returns {String} The corresponding stroke-dasharray value.
 */
function gf_getStrokeDasharray (strokeStyle)
{
	if (gf_isset(strokeStyle))
	{
		strokeStyle = strokeStyle.toLowerCase();
		
		if (strokeStyle == "dotted")
			return ".";
			
		if (strokeStyle == "dashed")
			return "-";
			
		if (strokeStyle == "solid" || strokeStyle == "double")
			return " ";
			
		if (strokeStyle == "none")
			return "none";
			
		return strokeStyle;
	}
	return " ";
}

/**
 * Reads the value for a given key from the given style set.
 * The type parameter is used to provide a default value in case the key is not found in the style set.
 * 
 * @private
 * @param {Object} style A style set.
 * @param {String} key The key of the style set.
 * @param {String} type The expected type of the value.
 * @returns {String|boolean|int|double} The value read from the style set.
 */
function gf_getStyleValue (style, key, type)
{
	if (!gf_isset(type))
		type = "";
	
	if (gf_isset(style, key))
	{
		return gf_isset(style[key]) ? style[key] : gv_defaultStyle[key];		
	}
	
	if (type == "bool")
		return false;
	
	if (type == "int")
		return 0;
	
	if (type == "float")
		return 0;
	
	return "";
}

/**
 * Translate the textAlign and textVAlign attribute from a style set to the corresponding Raphael values.
 * The resulting object contains the following information:
 * <br />
 * - align: the text alignment<br />
 * - valign: the vertical text alignment
 * 
 * @private
 * @param {String} textAlign The text-alignment. Possible values are "left", "right", "middle"
 * @param {String} textVAlign The vertical text-alignment. Possible values are "top", "bottom", "middle"
 * @returns {Object}
 */
function gf_getTextPosition (textAlign, textVAlign)
{
	var align	= "middle";
	var valign	= "middle";
	
	if (gf_isset(textAlign))
	{
		if (textAlign.toLowerCase() == "left")
			align = "start";
			
		if (textAlign.toLowerCase() == "right")
			align = "end";
	}
	
	return {align: align, valign: valign};
}

/**
 * Initialize the paper.
 * 
 * @private
 * @returns {void}
 */
function gf_initPaper ()
{
	// clear the objects
	gv_objects_edges = {};
	gv_objects_nodes = {};
	
	// empty the canvas
	gv_paper.clear();
	
	// initialize the bgRect which is needed for drag and zoom operations
	gv_bgRect	= gv_paper.rect(-gv_paperSizes[gv_graphID + "_width"] * 5, -gv_paperSizes[gv_graphID + "_height"] * 5, gv_paperSizes[gv_graphID + "_width"] * 11, gv_paperSizes[gv_graphID + "_height"] * 11).attr({"opacity": 0, "fill": "#FF0000"}).drag(gf_paperDragMove, gf_paperDragStart, gf_paperDragEnd);
	
	// add the mousewheel event to the bgRect for zooming
	$(gv_bgRect.node).bind('mousewheel', function(event, delta)
	{
	
		// when the shift-key is pressed -> zoom
		if (event.shiftKey)
		{
			// calculate the mouse position
			var gt_paperPos	= gf_paperMousePosition(event);
			var gt_speed	= gv_zoomSettings.wheel;
		
			// zoom in / out depending on the mousewheel direction
			if (delta > 0)
				gf_paperZoomIn(gt_speed, gt_paperPos);
			else
				gf_paperZoomOut(gt_speed, gt_paperPos);
				
			event.preventDefault();
		}
    });
    
    // add the mousemove event to the canvas to bring the bgRect in front when the shift key is pressed; this is necessary to avoid problems on graph elements while zooming / dragging
    $(gv_paper.canvas).bind("mousemove", function (event)
    {
    	// bring the bgRect to front while the shift-key is pressed
    	if (event.shiftKey)
		{
			gv_bgRect.toFront();
		}
		
		// move the bgRect to the background when the shift-key is released
		else
		{
			gv_bgRect.toBack();
		}
    });
}

/**
 * Checks whether the given object is of the type "Array".
 * 
 * @private
 * @param {mixed} obj The object to check.
 * @returns {boolean} Whether the object is an Array.
 */
function gf_isArray (obj)
{
    return Object.prototype.toString.call(obj) === '[object Array]';
}

/**
 * Function to check if a variable / array-element is set.
 * Any number of variables can be passed to this method.
 * When at least one variable is not set this function returns false.
 * 
 * @private
 * @param {mixed} var Any number of variables.
 * @returns {boolean} False when at least one variable is not set.
 */
function gf_isset ()
{
    // http://kevin.vanzonneveld.net
    // +   original by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
    // +   improved by: FremyCompany
    // +   improved by: Onno Marsman
    // +   improved by: Rafał Kukawski
    var gt_a = arguments,
    	gt_l = gt_a.length,
    	gt_i = 0,
    	gt_undef;

    if (gt_l === 0)
    {
        throw new Error('Empty isset');
    }

    while (gt_i !== gt_l)
    {
        if (gt_a[gt_i] === gt_undef || gt_a[gt_i] === null)
        {
            return false;
        }
        gt_i++;
    }
    return true;
}

/**
 * Returns true when the tk_graph library is set to standAlone.
 * 
 * @private
 * @return {boolean} True when the tk_graph library's gv_standAlone is set to true; false otherwise.
 */
function gf_isStandAlone ()
{
	return gv_standAlone === true;
}

/**
 * Merge two or more style sets.
 * The attributes of the sets are merged together into one style set.
 * The method can take any number of style sets.
 * 
 * @private
 * @param {Object} styleSet Any number of style sets.
 * @returns {Object} The merged style set.
 */
function gf_mergeStyles ()
{
	var gt_args		= arguments;
	var gt_count	= gt_args.length;
	var gt_style	= {};
	
	if (gt_count > 1)
	{
		for (var gt_i in gt_args)
		{
			for (var gt_s in gt_args[gt_i])
			{
				gt_style[gt_s] = gt_args[gt_i][gt_s];
			}
		}
	}
	
	return gt_style;
}

/**
 * Converts a text with new lines into a CamelCase text without whitespaces.
 * 
 * @param {String} gt_text The input text.
 * @returns {String} The converted text.
 */
function gf_newlineToCamelCase (gt_text)
{
	if (gf_isset(gt_text))
	{
		gt_text	= gf_replaceNewline(gt_text, " ");
		
		var gt_textArray	= gt_text.split(" ");
		var gt_result		= "";
		for (var gt_taid in gt_textArray)
		{
			var gt_textArrayEntry	= gt_textArray[gt_taid].toLowerCase();
			gt_result += gt_textArrayEntry.substr(0, 1).toUpperCase() + gt_textArrayEntry.substr(1);
		}
		
		return gt_result;
	}
	return "";
}

/**
 * Checks if the given object / list of objects contain a certain attribute / a list of attributes.
 * Either pass an Object / String or an array for each parameter.
 * The method will return false when either an object does not exist or an object does not contain a certain attribute.
 * 
 * @private
 * @param {Object|Object[]} obj Any object or an array of Objects.
 * @param {String|String[]} attribute The attribute(s) to check.
 * @returns {boolean} False if at least one of the attributes or objects does not exist.
 */
function gf_objectHasAttribute (gt_object, gt_attribute)
{
	if (!gf_isset(gt_attribute, gt_object))
		return false;
	
	if (!gf_isArray(gt_attribute))
		gt_attribute = [gt_attribute];
	
	if (!gf_isArray(gt_object))
		gt_object = [gt_object];
	
	for (var gt_o in gt_object)
	{
		if (!gf_isset(gt_object[gt_o]))
			return false;
		
		for (var gt_i in gt_attribute)
		{
			if (!gf_isset(gt_object[gt_o][gt_attribute[gt_i]]))
				return false;
		}
	}
	
	return true;
}

/**
 * Calculate the center position relative to the top left corner of the paper.
 *  
 * @private
 * @returns {Object}
 */
function gf_paperCenterPosition ()
{
	var gt_graphOuter	= gv_elements["graph" + gv_graphID.toUpperCase() + "outer"];
	var gt_outerScroll	= {left: $('#' + gt_graphOuter).scrollLeft(), top: $('#' + gt_graphOuter).scrollTop()};
	var	gt_dimensions	= {width: $('#' + gt_graphOuter).width(), height: $('#' + gt_graphOuter).height()};
	
	// calculate the mouse position
	var gt_centerX		= gt_dimensions.width/2 + gt_outerScroll.left;
	var gt_centerY		= gt_dimensions.height/2 + gt_outerScroll.top;
	
	return {x: gt_centerX, y: gt_centerY};
}

/**
 * Change the reference of gv_paper depending on the currently selected graph.
 * Initialize the paper and reset the zoom.
 * 
 * @private
 * @param {String} view The id of the graph. Possible values: "bv" (behavioral view), "cv" (communication view)
 * @returns {void}
 * 
 */
function gf_paperChangeView (view)
{
	// when the behavioral view is selected: update the gv_paper reference to gv_bv_paper
	if (view == "bv")
	{
		gv_graphID	= "bv";
		gv_paper	= gv_bv_paper;
	}
	
	// when the communication view is selected: update the gv_paper reference to gv_cv_paper
	else
	{
		gv_graphID	= "cv";
		gv_paper	= gv_cv_paper;	
	}
	
	// update the boundaries of the original view box
	gv_originalViewBox.width	= gv_paperSizes[gv_graphID + "_width"];
	gv_originalViewBox.height	= gv_paperSizes[gv_graphID + "_height"];
	gv_originalViewBox.x		= 0;
	gv_originalViewBox.y		= 0;
	
	// initialize the paper
	gf_initPaper();
	
	// reset the zoom
	gf_paperZoomReset();
}

/**
 * Called when an edge is clicked on the canvas of the behavioral view.
 * It calls the gf_clickedBVedge method to load the information about the edge.
 * 
 * @private
 * @param {String} id The id of the clicked edge.
 * @returns{void}
 */
function gf_paperClickEdge (id)
{	
	if (gf_isset(id) && gf_isset(gv_objects_edges[id]))
	{
		// deselect all edges and nodes
		gf_deselectEdges();
		gf_deselectNodes();
		
		// call the select method on the clicked edge
		gv_objects_edges[id].select();
		
		// hook
		if (!gf_isStandAlone() && gf_functionExists(gv_functions.events.edgeClickedHook))
		{
			window[gv_functions.events.edgeClickedHook](id);
		}
		
		// call the gf_clickedBVedge method
		if (!gf_isStandAlone() && gf_functionExists(gv_functions.events.edgeClicked))
		{
			window[gv_functions.events.edgeClicked](id);
		}
		else
		{
			gf_clickedBVedge(id);
		}
	}
}

/**
 * Called when a node is clicked on the canvas of the behavioral view.
 * It calls the gf_clickedBVnode method to load the information about the node.
 * 
 * @private
 * @param {String} id The id of the clicked node.
 * @returns{void}
 */
function gf_paperClickNodeB (id)
{
	if (gf_isset(id) && gf_isset(gv_objects_nodes[id]))
	{
		// deselect all edges and nodes
		gf_deselectEdges();
		gf_deselectNodes();
		
		// call the select method on the clicked node
		gv_objects_nodes[id].select();
		
		// hook
		if (!gf_isStandAlone() && gf_functionExists(gv_functions.events.nodeClickedHook))
		{
			window[gv_functions.events.nodeClickedHook](id);
		}
		
		// call the gf_clickedBVnode method
		if (!gf_isStandAlone() && gf_functionExists(gv_functions.events.nodeClicked))
		{
			window[gv_functions.events.nodeClicked](id);
		}
		else
		{
			gf_clickedBVnode(id);
		}
	}
}

/**
 * Called when a subject is clicked on the canvas of the communication view.
 * It calls the gf_clickedCVnode method to load the information about the subject.
 * 
 * @private
 * @param {String} id The id of the clicked node.
 * @returns{void}
 */
function gf_paperClickNodeC (id)
{
	if (gf_isset(id) && gf_isset(gv_objects_nodes[id]))
	{
		// deselect all edges and nodes
		gf_deselectEdges();
		gf_deselectNodes();
		
		// call the select method on the clicked node
		gv_objects_nodes[id].select();
		
		
		// hook
		if (!gf_isStandAlone() && gf_functionExists(gv_functions.events.subjectClickedHook))
		{
			window[gv_functions.events.subjectClickedHook](id);
		}
		
		// call the gf_clickedCVnode method
		if (!gf_isStandAlone() && gf_functionExists(gv_functions.events.subjectClicked))
		{
			window[gv_functions.events.subjectClicked](id);
		}
		else
		{
			gf_clickedCVnode(id);
		}
	}
}

/**
 * Called when a macro node is dblclicked on the canvas of the behaviorla view.
 * It loads the proper macro.
 * 
 * @private
 * @param {String} id The id of the clicked node.
 * @returns {void}
 */
function gf_paperDblClickNodeB (id)
{
	if (gf_isset(id) && gf_isset(gv_objects_nodes[id]))
	{
		var gt_behav	= gv_graph.getBehavior(gv_graph.selectedSubject);
		
		if (gt_behav != null)
		{
			gt_behav.selectMacroByNode(id);
		}
	}
}

/**
 * Called when a subject is dblclicked on the canvas of the communication view.
 * It loads the internal behavior for the subject.
 * 
 * @private
 * @param {String} id The id of the clicked subject.
 * @returns {void}
 */
function gf_paperDblClickNodeC (id)
{
	
	if (gf_isset(id) && gf_isset(gv_objects_nodes[id]))
	{
		// hook
		if (!gf_isStandAlone() && gf_functionExists(gv_functions.events.subjectDblClickedHook))
		{
			window[gv_functions.events.subjectDblClickedHook](id);
		}
		
		// call actions depending on the subject's type
		
		var gt_subject	= null;
		if (gf_isset(gv_graph.subjects[id]))
			gt_subject	= gv_graph.subjects[id];
			
		var gt_type		= "internal";
		if (gt_subject != null)
			gt_type		= gt_subject.isExternal() ? gt_subject.getExternalType() : "internal";
			
		// internal subject
		if (gt_type == "internal")
		{
			// call the gf_clickedCVnode method
			if (!gf_isStandAlone() && gf_functionExists(gv_functions.events.subjectDblClickedInternal))
			{
				window[gv_functions.events.subjectDblClickedInternal](id);
			}
			else
			{
				// call the gf_paperClickNodeC method to select the node.
				gf_paperClickNodeC(id);
				
				// call the gf_toggleBV method to load the internal behavior
				gf_toggleBV();
			}
		}
		
		// external subject: instant interface
		else if (gt_type == "instantinterface")
		{
			// call the gf_clickedCVnode method
			if (!gf_isStandAlone() && gf_functionExists(gv_functions.events.subjectDblClickedInstantInterface))
			{
				window[gv_functions.events.subjectDblClickedInstantInterface](id);
			}
			else
			{
				// call the gf_paperClickNodeC method to select the node.
				gf_paperClickNodeC(id);
				
				// no further action
			}
		}
		
		// external subject: interface
		else if (gt_type == "interface")
		{
			// call the gf_clickedCVnode method
			if (!gf_isStandAlone() && gf_functionExists(gv_functions.events.subjectDblClickedInterface))
			{
				window[gv_functions.events.subjectDblClickedInterface](id);
			}
			else
			{
				// call the gf_paperClickNodeC method to select the node.
				gf_paperClickNodeC(id);
				
				// call the gf_toggleBV method to load the internal behavior
				gf_toggleBV();
			}
		}
		
		// external subject: interface
		else if (gt_type == "external")
		{
			var gt_process	= gt_subject != null ? gt_subject.getRelatedProcess() : "";
			
			// call the gf_clickedCVnode method
			if (!gf_isStandAlone() && gf_functionExists(gv_functions.events.subjectDblClickedExternal) && gt_process != "")
			{
				window[gv_functions.events.subjectDblClickedExternal](gt_process);
			}
			else
			{
				// call the gf_paperClickNodeC method to select the node.
				gf_paperClickNodeC(id);
				
				// no process can be loaded
				if (gt_process == "")
					console.log("Error on loading process! No process defined!");
				else
					console.log("Error on loading process '" + gt_process + "'! No handler available!");
			}
		}
	}
}

/**
 * Calculate the mouse position relative to the top left corner of the paper.
 *  
 * @private
 * @param {Event} event The event to retrieve the mouse position from.
 * @returns {Object}
 */
function gf_paperMousePosition (event)
{
	var gt_graphOuter	= gv_elements["graph" + gv_graphID.toUpperCase() + "outer"];
	var gt_outerOffset	= $('#' + gt_graphOuter).offset();
	var gt_outerScroll	= {left: $('#' + gt_graphOuter).scrollLeft(), top: $('#' + gt_graphOuter).scrollTop()};
	var	gt_endPosX		= event.pageX ? event.pageX : event.clientX;
	var	gt_endPosY		= event.pageY ? event.pageY : event.clientY;
	
	// calculate the mouse position
	var gt_mouseX		= gt_endPosX + gt_outerScroll.left - gt_outerOffset.left;
	var gt_mouseY		= gt_endPosY + gt_outerScroll.top - gt_outerOffset.top;
	
	return {x: gt_mouseX, y: gt_mouseY};
}

/**
 * Replaces all new line characters of the given text with \n.
 * 
 * @private
 * @param {String} text The text to process.
 * @param {String} characeter A special character to replace all matching results with (optional).
 * @returns {String} The processed text.
 */
function gf_replaceNewline (text, character)
{
	if (!gf_isset(character))
		character = "\n";
	
	return text.replace(/<br>|<br \/>|<br\/>|\\r\\n|\\r|\\n|\n/gi, character);
}

/**
 * Calculate the time for a certain task.
 * 
 * @private
 * @param {String} type The task to calculate the time for.
 * @param {boolean} condition When set to false the time will not be counted.
 * @returns {void}
 */
function gf_timeCalc (type, condition)
{
	if (!gf_isset(condition) || condition != false)
		condition	= true;
	
	if (gf_isset(type) && condition)
	{
		if (!gf_isset(gv_times[type]))
			gv_times[type]	= 0;
			
		if (!gf_isset(gv_timeStart[type]))
			gv_timeStart[type]	= 0;
			
		if (gv_timeStart[type] == 0)
		{
			gv_timeStart[type]	= new Date();
		}
		else
		{
			var gt_timeEnd	= new Date();
			gv_times[type]	+= gt_timeEnd - gv_timeStart[type];				
			gv_timeStart[type]	= 0;
		}
	}
}

/**
 * Print the time taken for a certain task.
 * 
 * @private
 * @param {String} type The task to print the time for. If no type is given, all measured times will be printed.
 * @returns {void}
 */
function gf_timePrint (type)
{
	if (gv_printTimes)
	{
		if (gf_isset(type) && gf_isset(gv_times[type]))
		{
			console.log("time for '" + type + "': " + gv_times[type]/1000 + "s");
		}
		else
		{
			console.log("\nTime used:");
			
			var gt_timeStrings	= [];
			for (var gt_type in gv_times)
			{
				gt_timeStrings[gt_timeStrings.length] = "\t" + gt_type + ": " + gv_times[gt_type]/1000 + "s";
			}
			
			gt_timeStrings.sort()
			
			for (var gt_timeString in gt_timeStrings)
			{
				console.log(gt_timeStrings[gt_timeString]);
			}
		}
	}
}

/**
 * Reset the times taken for a certain task.
 * 
 * @private
 * @param {String} type The task to reset the time for. If no type is given, all measured times will be cleared.
 * @returns {void}
 */
function gf_timeReset (type)
{
	if (gf_isset(type))
	{
		if (!gf_isArray(type))
			type = [type];
			
		var gt_type	= "";
		for (var gt_typeId in type)
		{
			gt_type	= type[gt_typeId];
			
			if (gf_isset(gv_times[gt_type]))
			{
				gv_times[gt_type]		= 0;
				gv_timeStart[gt_type]	= 0;
			}
		}
	}
	else
	{
		gv_times		= {};
		gv_timeStart	= {};
	}
}

/**
 * Load the internal behavior and mark it in the GUI.
 * When no appropriate function is defined to mark the active graph in the GUI the behavioral view is loaded without further action.
 * 
 * @private
 * @returns {void}
 */
function gf_toggleBV ()
{
	if (!gf_isStandAlone() && gf_functionExists(gv_functions.general.changeViewBV))
	{
		window[gv_functions.general.changeViewBV]();
	}
	else
	{
		gf_clickedCVbehavior();
	}	
}