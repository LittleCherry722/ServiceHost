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

// subscribe to changes of channel list
$.subscribe("/tk_graph/channels", function (args)
{
	if (gf_isset(gv_elements))
	{
		if (gf_isset(args.action) && args.action == "load" && gf_isset(args.view))
		{
			gf_guiLoadDropDownForChannelSelect(args.view);
		}
		else
		{
			gf_guiLoadDropDownForChannelSelect();
		}
	}
	
	if (!gf_isStandAlone() && gf_functionExists(gv_functions.communication.updateListOfChannels))
	{
		window[gv_functions.communication.updateListOfChannels]();
	}
}
);

/**
 * Hides an element (display="none").
 * 
 * @param {String} element The name of the DOM element to hide.
 * @returns {void}
 */
function gf_guiElementHide (element)
{
	if (gf_isset(element) && gf_elementExists(element))
		document.getElementById(element).style.display = "none";
}

/**
 * Reads the value of an element.
 * 
 * @param {String} element The name of the DOM element to read the value from.
 * @param {String} type The type of the value to read (bool [checked] or string [value]).
 * @param {String} defaultValue The default value of the element.
 * @returns {bool|String} The value of the element.
 */
function gf_guiElementRead (element, type, defaultValue)
{
	var value = "";
	if (gf_isset(element, type) && gf_elementExists(element))
	{
		if (type == "bool")
		{
			value = document.getElementById(element).checked === true;
		}
		else
		{
			value = document.getElementById(element).value;
		}
	}
	else
	{
		if (type == "bool")
		{
			value = gf_isset(defaultValue) ? defaultValue : false;
		}
		else
		{
			value = gf_isset(defaultValue) ? defaultValue : "";	
		}
	}
	return value;
}

/**
 * Shows an element (display="block").
 * 
 * @param {String} element The name of the DOM element to show.
 * @returns {void}
 */
function gf_guiElementShow (element)
{
	if (gf_isset(element) && gf_elementExists(element))
		document.getElementById(element).style.display = "block";
}

/**
 * Changes the value of an element.
 * 
 * @param {String} element The name of the DOM element.
 * @param {String} type The type of the value to set (bool [checked] or string [value]).
 * @param {bool|String} value The value to set.
 * @param {String} defaultValue The default value of the element.
 * @returns {void}
 */
function gf_guiElementWrite (element, type, value, defaultValue)
{
	if (gf_isset(element, type, value) && gf_elementExists(element))
	{
		if (type == "bool")
		{
			document.getElementById(element).checked = value === true;
		}
		else
		{
			document.getElementById(element).value = value == null ? (gf_isset(defaultValue) ? defaultValue : "") : value;
		}
	}
}

/**
 * Switch between communication view and behavioral view.
 * Show / hide the outer divs and center the canvas.
 * 
 * @see GCcommunication::changeView()
 * @param {Object} view
 * @returns {void}
 */
function gf_guiChangeView (view)
{
	if (gf_isset(view))
	{
		if (view == "cv")
		{
			if (gf_elementExists(gv_elements.graphBVouter))
				document.getElementById(gv_elements.graphBVouter).style.display = "none";
			
			document.getElementById(gv_elements.graphCVouter).style.display = "block";
			$('#' + gv_elements.graphCVouter).scrollTo( {left: '0px', top: '0px'});
			$('#' + gv_elements.graphCVouter).scrollTo( {left: '0px', top: '50%'});
		}
		else
		{
			if (gf_elementExists(gv_elements.graphCVouter))
				document.getElementById(gv_elements.graphCVouter).style.display = "none";
				
			document.getElementById(gv_elements.graphBVouter).style.display = "block";
			$('#' + gv_elements.graphBVouter).scrollTo( {left: '0px', top: '0px'});
			$('#' + gv_elements.graphBVouter).scrollTo( {left: '50%', top: '0px'});
		}
		
		gf_guiLoadDropDownForChannelSelect(view);
	}
}

/**
 * Empty some input fields.
 * 
 * @see GCcommunication::loadInformation(true)
 * @returns {void}
 */
function gf_guiClearInputFields ()
{
	if (gf_elementExists(gv_elements.inputNodeText))
		document.getElementById(gv_elements.inputNodeText).value = "";
	if (gf_elementExists(gv_elements.inputNodeType))
		document.getElementById(gv_elements.inputNodeType).value = "";
	
	if (gf_elementExists(gv_elements.inputSubjectText))
		document.getElementById(gv_elements.inputSubjectText).value = "";
	if (gf_elementExists(gv_elements.inputSubjectRole))
		document.getElementById(gv_elements.inputSubjectRole).value = "";
	if (gf_elementExists(gv_elements.inputSubjectInputPool))
		document.getElementById(gv_elements.inputSubjectInputPool).value = "";

	if (gf_elementExists(gv_elements.inputEdgeText))
		document.getElementById(gv_elements.inputEdgeText).value = "";
	if (gf_elementExists(gv_elements.inputEdgeTarget))
		document.getElementById(gv_elements.inputEdgeTarget).options.length = 0;
	if (gf_elementExists(gv_elements.inputEdgeTimeout))
		document.getElementById(gv_elements.inputEdgeTimeout).value = "";
	if (gf_elementExists(gv_elements.inputEdgeTimeoutEx))
		document.getElementById(gv_elements.inputEdgeTimeoutEx).innerHTML = "";
	if (gf_elementExists(gv_elements.inputEdgeTypeCondition))
		document.getElementById(gv_elements.inputEdgeTypeCondition).checked = false;
	if (gf_elementExists(gv_elements.inputEdgeTypeException))
		document.getElementById(gv_elements.inputEdgeTypeException).checked = false;
	if (gf_elementExists(gv_elements.inputEdgeTypeTimeout))
		document.getElementById(gv_elements.inputEdgeTypeTimeout).checked = false;
		
	if (gf_elementExists(gv_elements.inputEdgeTargetO))
		document.getElementById(gv_elements.inputEdgeTargetO).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeOptMessageO))
		document.getElementById(gv_elements.inputNodeOptMessageO).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeOptSubjectO))
		document.getElementById(gv_elements.inputNodeOptSubjectO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgePriorityO))
		document.getElementById(gv_elements.inputEdgePriorityO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeTargetMMMO))
		document.getElementById(gv_elements.inputEdgeTargetMMMO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeTargetMOuter))
		document.getElementById(gv_elements.inputEdgeTargetMOuter).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeTargetMTypeNO))
		document.getElementById(gv_elements.inputEdgeTargetMTypeNO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeTargetMTypeVO))
		document.getElementById(gv_elements.inputEdgeTargetMTypeVO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeTargetMVarTextO))
		document.getElementById(gv_elements.inputEdgeTargetMVarTextO).style.display = "none";
		
	if (gf_elementExists(gv_elements.inputNodeMajorStartOuter))
		document.getElementById(gv_elements.inputNodeMajorStartOuter).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeCorrelationIdO))
		document.getElementById(gv_elements.inputEdgeCorrelationIdO).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeOptChannelOuter))
		document.getElementById(gv_elements.inputNodeOptChannelOuter).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeOptCorrelationIdO))
		document.getElementById(gv_elements.inputNodeOptCorrelationIdO).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeOptStateOuter))
		document.getElementById(gv_elements.inputNodeOptStateOuter).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeOptionsOuter))
		document.getElementById(gv_elements.inputNodeOptionsOuter).style.display = "none";
		
	if (gf_elementExists(gv_elements.inputNodeChannelOuter))
		document.getElementById(gv_elements.inputNodeChannelOuter).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeChannelNewOuter))
		document.getElementById(gv_elements.inputNodeChannelNewOuter).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeVariableO))
		document.getElementById(gv_elements.inputNodeVariableO).style.display = "none";
		
	if (gf_elementExists(gv_elements.inputEdgeStoreOuter))
		document.getElementById(gv_elements.inputEdgeStoreOuter).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeStoreVariableNO))
		document.getElementById(gv_elements.inputEdgeStoreVariableNO).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeVarManOuter))
		document.getElementById(gv_elements.inputNodeVarManOuter).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeVarManVarStoreNO))
		document.getElementById(gv_elements.inputNodeVarManVarStoreNO).style.display = "none";
}

/**
 * Load the information of the selected edge into the input fields.
 * 
 * @see GCcommunication::loadInformationEdge()
 * @param {GCedge} node A reference to the selected edge.
 * @param {String} startType Type of the edge's start node.
 * @returns {void}
 */
function gf_guiDisplayEdge (edge, startType)
{
	if (gf_elementExists(gv_elements.inputEdgeText))
	{
		document.getElementById(gv_elements.inputEdgeText).value	= gf_replaceNewline(edge.getMessageTypeId() == "" ? edge.getText() : edge.getMessageType());
		document.getElementById(gv_elements.inputEdgeText).readOnly	= false;					
	}
	
	if (gf_elementExists(gv_elements.inputEdgeTimeout))
	{
		document.getElementById(gv_elements.inputEdgeTimeout).value	= edge.getTimer("timestamp") > 0 ? edge.getTimer() : "";					
	}
	
	if (gf_elementExists(gv_elements.inputEdgeTimeoutManual))
	{
		document.getElementById(gv_elements.inputEdgeTimeoutManual).checked	= edge.isManualTimeout();					
	}
	
	if (gf_elementExists(gv_elements.inputEdgeTimeoutEx))
	{
		document.getElementById(gv_elements.inputEdgeTimeoutEx).innerHTML	= "(example: " + edge.getTimer("example") + ")";					
	}
	
	if (gf_elementExists(gv_elements.inputEdgePriority))
	{
		document.getElementById(gv_elements.inputEdgePriority).value	= edge.getPriority();
	}
	
	if (gf_elementExists(gv_elements.inputEdgeTargetMVarText))
		document.getElementById(gv_elements.inputEdgeTargetMVarText).value = "";
	if (gf_elementExists(gv_elements.inputEdgeStoreVariableN))
		document.getElementById(gv_elements.inputEdgeStoreVariableN).value = "";
	
	if (gf_elementExists(gv_elements.inputEdgeTargetO))
		document.getElementById(gv_elements.inputEdgeTargetO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeOptionalO))
		document.getElementById(gv_elements.inputEdgeOptionalO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeTypeCondO))
		document.getElementById(gv_elements.inputEdgeTypeCondO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeTypeExceptO))
		document.getElementById(gv_elements.inputEdgeTypeExceptO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeTypeTimeoutO))
		document.getElementById(gv_elements.inputEdgeTypeTimeoutO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgePriorityO))
		document.getElementById(gv_elements.inputEdgePriorityO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeTargetMMMO))
		document.getElementById(gv_elements.inputEdgeTargetMMMO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeTargetMOuter))
		document.getElementById(gv_elements.inputEdgeTargetMOuter).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeTargetMTypeNO))
		document.getElementById(gv_elements.inputEdgeTargetMTypeNO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeTargetMTypeVO))
		document.getElementById(gv_elements.inputEdgeTargetMTypeVO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeTargetMVarTextO))
		document.getElementById(gv_elements.inputEdgeTargetMVarTextO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeCorrelationIdO))
		document.getElementById(gv_elements.inputEdgeCorrelationIdO).style.display = "none";
		
	if (gf_elementExists(gv_elements.inputEdgeStoreOuter))
		document.getElementById(gv_elements.inputEdgeStoreOuter).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeStoreVariableNO))
		document.getElementById(gv_elements.inputEdgeStoreVariableNO).style.display = "none";
		
	if (gf_elementExists(gv_elements.inputEdgeTarget))
		document.getElementById(gv_elements.inputEdgeTarget).onchange = function () {
			var gt_relatedSubjectID		= document.getElementById(gv_elements.inputEdgeTarget).value;
			var gt_relatedSubjectMulti	= gf_isset(gv_graph.subjects[gt_relatedSubjectID]) ? gv_graph.subjects[gt_relatedSubjectID].isMulti() : false;
			 
			if (gf_elementExists(gv_elements.inputEdgeTargetMOuter))
			{
				document.getElementById(gv_elements.inputEdgeTargetMOuter).style.display	= gt_relatedSubjectMulti ? "block" : "none";
				document.getElementById(gv_elements.inputEdgeTargetMTypeNO).style.display	= startType == "send" ? "block" : "none";
			}
		};
		
	// optional edges
	if (edge.getTypeOfStartNode() == "modalsplit")
	{
		if (gf_elementExists(gv_elements.inputEdgeOptionalO))
			document.getElementById(gv_elements.inputEdgeOptionalO).style.display = "block";
			
		if (gf_elementExists(gv_elements.inputEdgeOptional))
			document.getElementById(gv_elements.inputEdgeOptional).checked = edge.isOptional();
	}
	
	// mark type
	if (edge.getType() == "timeout")
	{
		if (gf_elementExists(gv_elements.inputEdgeTypeTimeout))
			document.getElementById(gv_elements.inputEdgeTypeTimeout).checked = true;
			
		if (gf_elementExists(gv_elements.inputEdgeTypeTimeoutO))
			document.getElementById(gv_elements.inputEdgeTypeTimeoutO).style.display = "block";
	}
	else if (edge.getType() == "errorcondition")
	{
		if (gf_elementExists(gv_elements.inputEdgeTypeException))
			document.getElementById(gv_elements.inputEdgeTypeException).checked = true;
			
		if (gf_elementExists(gv_elements.inputEdgeTypeExceptO))
			document.getElementById(gv_elements.inputEdgeTypeExceptO).style.display = "block";
	}
	else
	{
		if (gf_elementExists(gv_elements.inputEdgeTypeCondition))
			document.getElementById(gv_elements.inputEdgeTypeCondition).checked = true;
			
		if (gf_elementExists(gv_elements.inputEdgeTypeCondO))
			document.getElementById(gv_elements.inputEdgeTypeCondO).style.display = "block";
	}
	
	// add events
	if (gf_elementExists(gv_elements.inputEdgeTypeTimeout))
		document.getElementById(gv_elements.inputEdgeTypeTimeout).onclick = function () {
			var gt_status	= document.getElementById(gv_elements.inputEdgeTypeTimeout).checked;
			
			if (gf_elementExists(gv_elements.inputEdgeTypeCondO))
				document.getElementById(gv_elements.inputEdgeTypeCondO).style.display = gt_status ? "none" : "block";
			if (gf_elementExists(gv_elements.inputEdgeTypeExceptO))
				document.getElementById(gv_elements.inputEdgeTypeExceptO).style.display = gt_status ? "none" : "block";
			if (gf_elementExists(gv_elements.inputEdgeTypeTimeoutO))
				document.getElementById(gv_elements.inputEdgeTypeTimeoutO).style.display = gt_status ? "block" : "none";
		};
	if (gf_elementExists(gv_elements.inputEdgeTypeCondition))
		document.getElementById(gv_elements.inputEdgeTypeCondition).onclick = function () {
			var gt_status	= document.getElementById(gv_elements.inputEdgeTypeCondition).checked;
			
			if (gf_elementExists(gv_elements.inputEdgeTypeCondO))
				document.getElementById(gv_elements.inputEdgeTypeCondO).style.display = gt_status ? "block" : "none";
			if (gf_elementExists(gv_elements.inputEdgeTypeExceptO))
				document.getElementById(gv_elements.inputEdgeTypeExceptO).style.display = gt_status ? "none" : "block";
			if (gf_elementExists(gv_elements.inputEdgeTypeTimeoutO))
				document.getElementById(gv_elements.inputEdgeTypeTimeoutO).style.display = gt_status ? "none" : "block";
		};
		
	if (gf_elementExists(gv_elements.inputEdgeTypeException))
		document.getElementById(gv_elements.inputEdgeTypeException).onclick = function () {
			var gt_status	= document.getElementById(gv_elements.inputEdgeTypeException).checked;
			
			if (gf_elementExists(gv_elements.inputEdgeTypeCondO))
				document.getElementById(gv_elements.inputEdgeTypeCondO).style.display = gt_status ? "none" : "block";
			if (gf_elementExists(gv_elements.inputEdgeTypeExceptO))
				document.getElementById(gv_elements.inputEdgeTypeExceptO).style.display = gt_status ? "block" : "none";
			if (gf_elementExists(gv_elements.inputEdgeTypeTimeoutO))
				document.getElementById(gv_elements.inputEdgeTypeTimeoutO).style.display = gt_status ? "none" : "block";
		};
		
	if (gf_elementExists(gv_elements.inputEdgeTypeTimeout))
		document.getElementById(gv_elements.inputEdgeTypeTimeout).disabled = !gf_checkCardinality(edge.parent, edge.getStart(), edge.getEnd(), "timeout", edge.getType(), "update").allowed;
		
	if (gf_elementExists(gv_elements.inputEdgeTypeCondition))
		document.getElementById(gv_elements.inputEdgeTypeCondition).disabled = !gf_checkCardinality(edge.parent, edge.getStart(), edge.getEnd(), "exitcondition", edge.getType(), "update").allowed;
		
	if (gf_elementExists(gv_elements.inputEdgeTypeException))
		document.getElementById(gv_elements.inputEdgeTypeException).disabled = !gf_checkCardinality(edge.parent, edge.getStart(), edge.getEnd(), "errorcondition", edge.getType(), "update").allowed;
	
	// correlationID
	if (startType == "send" || startType == "receive" || startType == "action")
	{
		
		if (gf_elementExists(gv_elements.inputEdgeStoreOuter))
			document.getElementById(gv_elements.inputEdgeStoreOuter).style.display = "block";
		
		if (gf_elementExists(gv_elements.inputEdgeStoreVariable))
		{
			gf_guiLoadDropDownVariables(edge.parent, gv_elements.inputEdgeStoreVariable, true, false);
			document.getElementById(gv_elements.inputEdgeStoreVariable).onchange = function ()
			{
				var gt_selected = document.getElementById(gv_elements.inputEdgeStoreVariable).value;
				
				if (gf_elementExists(gv_elements.inputEdgeStoreVariableNO))
				{
					document.getElementById(gv_elements.inputEdgeStoreVariableNO).style.display = gt_selected == "##createNew##" ? "block" : "none";
				}
			}
		}
			
		if (gf_elementExists(gv_elements.inputEdgeStoreVariable))
			document.getElementById(gv_elements.inputEdgeStoreVariable).value	= edge.getVariable() == null ? "" : edge.getVariable();
	}
	
	
	// create the drop down menu to select the related subject (only for receive and send nodes)
	if (startType == "send" || startType == "receive")
	{		
		if (gf_elementExists(gv_elements.inputEdgeCorrelationIdO))
			document.getElementById(gv_elements.inputEdgeCorrelationIdO).style.display = "block";
			
		if (gf_elementExists(gv_elements.inputEdgeCorrelationId))
		{
			gf_guiLoadDropDownCorrelationIds(edge.parent, gv_elements.inputEdgeCorrelationId, true, false);
			document.getElementById(gv_elements.inputEdgeCorrelationId).value = edge.getCorrelationId() == null ? "" : edge.getCorrelationId();
		}
		
		if (gf_elementExists(gv_elements.inputEdgeTargetO))
			document.getElementById(gv_elements.inputEdgeTargetO).style.display = "block";
			
		if (gf_elementExists(gv_elements.inputEdgeTargetO))
			document.getElementById(gv_elements.inputEdgeMessage).onchange			= gf_guiSetEdgeMessage;
		
		gf_guiLoadDropDownMessageTypes(gv_elements.inputEdgeMessage, true, false);
		gf_guiLoadDropDownSubjects(gv_elements.inputEdgeTarget, gv_graph.selectedSubject, false);
		
		// show the radio buttons for types
		if (edge.getRelatedSubject("multi") && gf_elementExists(gv_elements.inputEdgeTargetMOuter))
			document.getElementById(gv_elements.inputEdgeTargetMOuter).style.display = "block";
			
		if (edge.getRelatedSubject("multi") && startType == "send" && gf_elementExists(gv_elements.inputEdgeTargetMTypeNO))
			document.getElementById(gv_elements.inputEdgeTargetMTypeNO).style.display = "block";
			
		var gt_isAll		= edge.getRelatedSubject("min") == "-1" && edge.getRelatedSubject("max") == "-1";
		var gt_createNew	= edge.getRelatedSubject("createNew");
		var gt_isVariable	= edge.getRelatedSubject("variable") != null && edge.getRelatedSubject("variable") != "";
		
		if (gf_elementExists(gv_elements.inputEdgeTargetMVariable))
		{
			gf_guiLoadDropDownVariables(edge.parent, gv_elements.inputEdgeTargetMVariable, false, false);
			document.getElementById(gv_elements.inputEdgeTargetMVariable).onchange = function ()
			{
				var gt_selected = document.getElementById(gv_elements.inputEdgeTargetMVariable).value;
				
				if (gf_elementExists(gv_elements.inputEdgeTargetMVarTextO))
				{
					document.getElementById(gv_elements.inputEdgeTargetMVarTextO).style.display = gt_selected == "##createNew##" ? "block" : "none";
				}
			}
		}
		
		
		if (!gt_isAll && !gt_isVariable && gf_elementExists(gv_elements.inputEdgeTargetMMMO))
			document.getElementById(gv_elements.inputEdgeTargetMMMO).style.display = "block";
			
		if (gt_isVariable && gf_elementExists(gv_elements.inputEdgeTargetMTypeVO))
			document.getElementById(gv_elements.inputEdgeTargetMTypeVO).style.display = "block";
		
		if (gf_elementExists(gv_elements.inputEdgeTarget))
			document.getElementById(gv_elements.inputEdgeTarget).value	= edge.getRelatedSubject() == null ? "" : edge.getRelatedSubject();
		
		if (gf_elementExists(gv_elements.inputEdgeMessage))
			document.getElementById(gv_elements.inputEdgeMessage).value	= edge.getText();
		
		if (gf_elementExists(gv_elements.inputEdgeTargetMTypeA))
			document.getElementById(gv_elements.inputEdgeTargetMTypeA).checked	= gt_isAll && !gt_isVariable;
		
		if (gf_elementExists(gv_elements.inputEdgeTargetMTypeL))
			document.getElementById(gv_elements.inputEdgeTargetMTypeL).checked	= !gt_isAll && !gt_createNew && !gt_isVariable;
			
		if (gf_elementExists(gv_elements.inputEdgeTargetMTypeN))
			document.getElementById(gv_elements.inputEdgeTargetMTypeN).checked	= !gt_isAll && gt_createNew && !gt_isVariable;
			
		if (gf_elementExists(gv_elements.inputEdgeTargetMTypeV))
			document.getElementById(gv_elements.inputEdgeTargetMTypeV).checked	= gt_isVariable;
		
		if (gf_elementExists(gv_elements.inputEdgeTargetMMin))
			document.getElementById(gv_elements.inputEdgeTargetMMin).value	= edge.getRelatedSubject("min");
		
		if (gf_elementExists(gv_elements.inputEdgeTargetMMax))
			document.getElementById(gv_elements.inputEdgeTargetMMax).value	= edge.getRelatedSubject("max");
		
		if (gf_elementExists(gv_elements.inputEdgeTargetMVariable))
			document.getElementById(gv_elements.inputEdgeTargetMVariable).value	= edge.getRelatedSubject("variable") == null ? "" : edge.getRelatedSubject("variable");
		
		// add event listeners
		if (gf_elementExists(gv_elements.inputEdgeTargetMTypeA))
			document.getElementById(gv_elements.inputEdgeTargetMTypeA).onclick	= function () {
				var gt_clickedValue	= document.getElementById(gv_elements.inputEdgeTargetMTypeA).checked;
				
				if (gf_elementExists(gv_elements.inputEdgeTargetMMMO))
					document.getElementById(gv_elements.inputEdgeTargetMMMO).style.display = gt_clickedValue ? "none" : "block";
					
				if (gf_elementExists(gv_elements.inputEdgeTargetMTypeVO))
					document.getElementById(gv_elements.inputEdgeTargetMTypeVO).style.display = "none";
			};
		if (gf_elementExists(gv_elements.inputEdgeTargetMTypeL))
			document.getElementById(gv_elements.inputEdgeTargetMTypeL).onclick	= function () {
				var gt_clickedValue	= document.getElementById(gv_elements.inputEdgeTargetMTypeL).checked;
				
				if (gf_elementExists(gv_elements.inputEdgeTargetMMMO))
					document.getElementById(gv_elements.inputEdgeTargetMMMO).style.display = gt_clickedValue ? "block" : "none";
					
				if (gf_elementExists(gv_elements.inputEdgeTargetMTypeVO))
					document.getElementById(gv_elements.inputEdgeTargetMTypeVO).style.display = "none";
			};
		if (gf_elementExists(gv_elements.inputEdgeTargetMTypeN))
			document.getElementById(gv_elements.inputEdgeTargetMTypeN).onclick	= function () {
				var gt_clickedValue	= document.getElementById(gv_elements.inputEdgeTargetMTypeN).checked;
				
				if (gf_elementExists(gv_elements.inputEdgeTargetMMMO))
					document.getElementById(gv_elements.inputEdgeTargetMMMO).style.display = gt_clickedValue ? "block" : "none";
					
				if (gf_elementExists(gv_elements.inputEdgeTargetMTypeVO))
					document.getElementById(gv_elements.inputEdgeTargetMTypeVO).style.display = "none";
			};
		if (gf_elementExists(gv_elements.inputEdgeTargetMTypeV))
			document.getElementById(gv_elements.inputEdgeTargetMTypeV).onclick	= function () {
				var gt_clickedValue	= document.getElementById(gv_elements.inputEdgeTargetMTypeV).checked;
				
				if (gf_elementExists(gv_elements.inputEdgeTargetMMMO))
					document.getElementById(gv_elements.inputEdgeTargetMMMO).style.display = gt_clickedValue ? "none" : "block";
					
				if (gf_elementExists(gv_elements.inputEdgeTargetMTypeVO))
					document.getElementById(gv_elements.inputEdgeTargetMTypeVO).style.display = "block";
			};
		
		// show the input field for priority
		if (startType == "receive" && gf_elementExists(gv_elements.inputEdgePriorityO))
		{
			document.getElementById(gv_elements.inputEdgePriorityO).style.display	= "block";
		}
	}
}

/**
 * Load the information of the selected node into the input fields.
 * 
 * @see GCcommunication::loadInformation()
 * @param {GCnode} node A reference to the selected node.
 * @returns {void}
 */
function gf_guiDisplayNode (node)
{
	if (!gf_elementExists(gv_elements.inputNodeType))
		return false;
		
	if (gf_elementExists(gv_elements.inputNodeMajorStartOuter))
		document.getElementById(gv_elements.inputNodeMajorStartOuter).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeChannelOuter))
		document.getElementById(gv_elements.inputNodeChannelOuter).style.display = "block";
	if (gf_elementExists(gv_elements.inputNodeChannelNewOuter))
		document.getElementById(gv_elements.inputNodeChannelNewOuter).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeVariableO))
		document.getElementById(gv_elements.inputNodeVariableO).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeVarManOuter))
		document.getElementById(gv_elements.inputNodeVarManOuter).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeVarManVarStoreNO))
		document.getElementById(gv_elements.inputNodeVarManVarStoreNO).style.display = "none";
		
	if (gf_elementExists(gv_elements.inputNodeChannelNew))
		document.getElementById(gv_elements.inputNodeChannelNew).value = "";
		
	if (gf_elementExists(gv_elements.inputNodeText))
		document.getElementById(gv_elements.inputNodeText).value = gf_replaceNewline(node.getText());
	
	if (gf_elementExists(gv_elements.inputNodeStart))
		document.getElementById(gv_elements.inputNodeStart).checked = node.isStart();
		
	if (gf_elementExists(gv_elements.inputNodeMajorStart))
		document.getElementById(gv_elements.inputNodeMajorStart).checked = node.isStart() && node.isMajorStartNode();
		
	var gt_select_type			= document.getElementById(gv_elements.inputNodeType);
	
	if (gf_elementExists(gv_elements.inputNodeType))
		document.getElementById(gv_elements.inputNodeType).onclick	= function () {
			gf_guiLoadDropDownForNode(node.parent, document.getElementById(gv_elements.inputNodeType).value);
		};
		
	if (gf_elementExists(gv_elements.inputNodeStart))
		document.getElementById(gv_elements.inputNodeStart).onclick	= function () {
			var gt_isStart = document.getElementById(gv_elements.inputNodeStart).checked;
			
			if (gf_elementExists(gv_elements.inputNodeMajorStartOuter))
				document.getElementById(gv_elements.inputNodeMajorStartOuter).style.display = gt_isStart ? "block" : "none";
		};
		
	if (gf_elementExists(gv_elements.inputNodeChannel))
		document.getElementById(gv_elements.inputNodeChannel).onchange	= function ()
		{
			var gt_selected = document.getElementById(gv_elements.inputNodeChannel).value;
				
			if (gf_elementExists(gv_elements.inputNodeChannelNewOuter))
			{
				document.getElementById(gv_elements.inputNodeChannelNewOuter).style.display = gt_selected == "##createNew##" ? "block" : "none";
			}
		};
		
	if (gf_elementExists(gv_elements.inputNodeVarManVarStore))
		document.getElementById(gv_elements.inputNodeVarManVarStore).onchange	= function ()
		{
			var gt_selected = document.getElementById(gv_elements.inputNodeVarManVarStore).value;
				
			if (gf_elementExists(gv_elements.inputNodeVarManVarStoreNO))
			{
				document.getElementById(gv_elements.inputNodeVarManVarStoreNO).style.display = gt_selected == "##createNew##" ? "block" : "none";
			}
		};
		
	
	$('#' + gv_elements.inputNodeType).empty();
	gf_guiLoadDropDownChannels(gv_elements.inputNodeChannel, true, false);
	
	if (gf_elementExists(gv_elements.inputNodeMajorStartOuter))
		document.getElementById(gv_elements.inputNodeMajorStartOuter).style.display	= node.isStart() ? "block" : "none";
		
	if (gf_elementExists(gv_elements.inputNodeChannel))
		document.getElementById(gv_elements.inputNodeChannel).value = node.getChannel() == null ? "" : node.getChannel();
		
	var gt_type	= node.isEnd() ? "end" : node.getType();
		
	// base elements
	var gt_optgrp = document.createElement("optgroup");
		gt_optgrp.label	= "base elements";
		
	var gt_option = "";
	
	for (var gt_key in gv_nodeTypes)
	{
		gt_option = document.createElement("option");
		gt_option.text = gv_nodeTypes[gt_key].label;
		gt_option.value = gt_key;
		gt_option.id = gv_elements.inputEdgeTarget + "_" + gt_key;
		gt_option.selected = gt_type == gt_key;
		gt_optgrp.appendChild(gt_option);
	}
	gt_select_type.appendChild(gt_optgrp);
	
	// predefined actions
		gt_optgrp = document.createElement("optgroup");
		gt_optgrp.label	= "predefined actions";
		
	var gt_option = "";
	
	for (var gt_key in gv_predefinedActions)
	{
		gt_option = document.createElement("option");
		gt_option.text = gv_predefinedActions[gt_key].label;
		gt_option.value = "$" + gt_key;
		gt_option.id = gv_elements.inputEdgeTarget + "_$" + gt_key;
		gt_option.selected = gt_type == "$" + gt_key;
		gt_optgrp.appendChild(gt_option);
	}
	gt_select_type.appendChild(gt_optgrp);
	
	if (gt_type.substr(0, 1) == "$" || gt_type == "action")
	{
		gf_guiLoadDropDownForNode(node.parent, gt_type);
			
		var gt_options	= node.getOptions();
		
		if (gf_isset(gt_options.subject))
		{
			document.getElementById(gv_elements.inputNodeOptSubject).value	= gt_options.subject;
		}
		
		if (gf_isset(gt_options.message))
		{
			document.getElementById(gv_elements.inputNodeOptMessage).value	= gt_options.message;
		}
		
		if (gf_isset(gt_options.state))
		{
			document.getElementById(gv_elements.inputNodeOptState).value	= gt_options.state;
		}
		
		if (gf_isset(gt_options.channel))
		{
			document.getElementById(gv_elements.inputNodeOptChannel).value	= gt_options.channel;
		}
		
		if (gf_isset(gt_options.correlationId))
		{
			document.getElementById(gv_elements.inputNodeOptCorrelationId).value	= gt_options.correlationId;
		}
		
		if (gf_elementExists(gv_elements.inputNodeVariable))
		{
			document.getElementById(gv_elements.inputNodeVariable).value = node.getVariable() == null ? "" : node.getVariable();
		}
		
		
		if (gf_elementExists(gv_elements.inputNodeVarManVar1))
			document.getElementById(gv_elements.inputNodeVarManVar1).value	= node.getVarMan("var1");
			
		if (gf_elementExists(gv_elements.inputNodeVarManVar2))
			document.getElementById(gv_elements.inputNodeVarManVar2).value	= node.getVarMan("var2");
			
		if (gf_elementExists(gv_elements.inputNodeVarManVarStore))
			document.getElementById(gv_elements.inputNodeVarManVarStore).value	= node.getVarMan("storevar");
			
		if (gf_elementExists(gv_elements.inputNodeVarManOperation))
			document.getElementById(gv_elements.inputNodeVarManOperation).value	= node.getVarMan("operation");
	}
}

/**
 * Load the information of the selected subject into the input fields.
 * 
 * @see GCcommunication::loadInformation()
 * @param {GCsubject} subject A reference to the selected subject.
 * @returns {void}
 */
function gf_guiDisplaySubject (subject)
{
	if (gf_elementExists(gv_elements.inputSubjectText))
		document.getElementById(gv_elements.inputSubjectText).value = gf_replaceNewline(subject.getText());
		
	if (gf_elementExists(gv_elements.inputSubjectRole))
		document.getElementById(gv_elements.inputSubjectRole).value = subject.getRole();
		
	if (gf_elementExists(gv_elements.inputSubjectInputPool))
		document.getElementById(gv_elements.inputSubjectInputPool).value = subject.getInputPool();

	if (gf_elementExists(gv_elements.inputSubjectTypeMulti))
		document.getElementById(gv_elements.inputSubjectTypeMulti).checked = subject.isMulti();
	
	if (gf_elementExists(gv_elements.inputSubjectTypeExternal))
		document.getElementById(gv_elements.inputSubjectTypeExternal).checked = subject.isExternal();
		
	if (gf_elementExists(gv_elements.inputSubjectRelProcess))
		document.getElementById(gv_elements.inputSubjectRelProcess).value = subject.getRelatedProcess();
		
	if (gf_elementExists(gv_elements.inputSubjectRelSubject))
		document.getElementById(gv_elements.inputSubjectRelSubject).value = subject.getRelatedSubject();
		
	if (gf_elementExists(gv_elements.inputSubjectRelOuter))
		document.getElementById(gv_elements.inputSubjectRelOuter).style.display = subject.isExternal() ? "block" : "none";
		
	if (gf_elementExists(gv_elements.inputSubjectTypeExternal) && gf_elementExists(gv_elements.inputSubjectRelOuter))
		document.getElementById(gv_elements.inputSubjectTypeExternal).onclick = function () {
			document.getElementById(gv_elements.inputSubjectRelOuter).style.display = document.getElementById(gv_elements.inputSubjectTypeExternal).checked ? "block" : "none";
		};
		
	if (gf_elementExists(gv_elements.inputSubjectExtExternal))
		document.getElementById(gv_elements.inputSubjectExtExternal).checked = subject.getExternalType() == "external";
		
	if (gf_elementExists(gv_elements.inputSubjectExtInterface))
		document.getElementById(gv_elements.inputSubjectExtInterface).checked = subject.getExternalType() == "interface";
		
	if (gf_elementExists(gv_elements.inputSubjectExtInstantInterface))
		document.getElementById(gv_elements.inputSubjectExtInstantInterface).checked = subject.getExternalType() == "instantinterface";
}

/**
 * This method is used to fill a select field with all available channels.
 * 
 * @param {String} elementChannel The ID of the select element that holds the available channels.
 * @param {boolean} newChannel When set to true an option will be added to create a new channel.
 * @param {boolean} wildcard When set to true an option will be added to the select field to select all channels (wildcard).
 * @param {boolean} selectChannels When set to true an option will be added to the select field to only display channels (used in CV).
 * @returns {void}
 */
function gf_guiLoadDropDownChannels (elementChannel, newChannel, wildcard, selectChannels)
{
	if (!gf_isset(newChannel) || newChannel !== true)
		newChannel = false;
		
	if (!gf_isset(wildcard) || wildcard !== true)
		wildcard = false;
		
	if (!gf_isset(selectChannels) || selectChannels !== true)
		selectChannels = false;
	
	// load channels	
	if (elementChannel != null && gf_elementExists(elementChannel))
	{
		var gt_select			= document.getElementById(elementChannel).options;
			gt_select.length	= 0;
		var gt_channelArray	= [];
		
		// create some entries to guide the user
		var gt_option			= document.createElement("option");
			gt_option.text		= "please select";
			gt_option.value		= "";
			gt_option.id		= elementChannel + "_00000.0";
			gt_select.add(gt_option);
		
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementChannel + "_00000.1";
			gt_select.add(gt_option);
		
		// options for select channels instead of messageTypes (CV only)
		if (selectChannels === true)
		{
			gt_option			= document.createElement("option");
			gt_option.text		= "display channels";
			gt_option.value		= "##channels##";
			gt_option.id		= elementChannel + "_00000.channels";
			gt_select.add(gt_option);
		
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementChannel + "_00000.321";
			gt_select.add(gt_option);
		}
		
		// options for select all channels
		if (wildcard === true)
		{
			gt_option			= document.createElement("option");
			gt_option.text		= "all channels";
			gt_option.value		= "##all##";
			gt_option.id		= elementChannel + "_00000.all";
			gt_select.add(gt_option);
		
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementChannel + "_00000.3";
			gt_select.add(gt_option);
		}
		
		// collect channels
		for (var gt_channelId in gv_graph.channels)
		{
			gt_channelArray[gt_channelArray.length]	= {id: gt_channelId, text: gv_graph.channels[gt_channelId]};
		}
		
		// sort the channels alphabetically
		gt_channelArray.sort(gf_guiSortArrayByText);
		
		// add the channels to the select field
		for (var gt_cid in gt_channelArray)
		{
			gt_option		= document.createElement("option");
			gt_option.text	= gf_replaceNewline(gt_channelArray[gt_cid].text, " ");
			gt_option.value	= gt_channelArray[gt_cid].id;
			gt_option.id	= elementChannel + "_" + gt_cid;
			gt_select.add(gt_option);
		}
		
		if (newChannel === true)
		{
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementChannel + "_00000.42";
			gt_select.add(gt_option);
		
			gt_option			= document.createElement("option");
			gt_option.text		= "create a new channel";
			gt_option.value		= "##createNew##";
			gt_option.id		= elementChannel + "_00000.new";
			gt_select.add(gt_option);
		}
	}
}

/**
 * This method is used to fill a select field with all available correlationIDs.
 * The correlationIDs will be "cID" (current ID), "nID" (new ID) and all available variables.
 * 
 * @param {GCbehavior} behavior The currently selected internal behavior (used to read the variables from).
 * @param {String} elementCorrelationId The ID of the select element that holds the available correlationIds.
 * @param {boolean} newCorrelationId When set to true an option will be added to use the nID (new correlation ID) function.
 * @param {boolean} wildcard When set to true an option will be added to the select field to select all correlationIds (wildcard).
 * @returns {void}
 */
function gf_guiLoadDropDownCorrelationIds (behavior, elementCorrelationId, newCorrelationId, wildcard)
{
	if (!gf_isset(newCorrelationId) || newCorrelationId !== true)
		newCorrelationId = false;
		
	if (!gf_isset(wildcard) || wildcard !== true)
		wildcard = false;
	
	// load correlationIds
	if (elementCorrelationId != null && gf_elementExists(elementCorrelationId))
	{
		var gt_select			= document.getElementById(elementCorrelationId).options;
			gt_select.length	= 0;
		var gt_varArray	= [];
		
		// create some entries to guide the user
		var gt_option			= document.createElement("option");
			gt_option.text		= "please select";
			gt_option.value		= "";
			gt_option.id		= elementCorrelationId + "_00000.0";
			gt_select.add(gt_option);
		
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementCorrelationId + "_00000.1";
			gt_select.add(gt_option);
		
		// options for select all correlationIds
		if (wildcard === true)
		{
			gt_option			= document.createElement("option");
			gt_option.text		= "all correlation IDs";
			gt_option.value		= "##all##";
			gt_option.id		= elementCorrelationId + "_00000.all";
			gt_select.add(gt_option);
		
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementCorrelationId + "_00000.3";
			gt_select.add(gt_option);
		}
		
		gt_option			= document.createElement("option");
		gt_option.text		= "current ID (cID)";
		gt_option.value		= "##cid##";
		gt_option.id		= elementCorrelationId + "_00000.cid";
		gt_select.add(gt_option);
	
		gt_option			= document.createElement("option");
		gt_option.text		= "----------------------------";
		gt_option.value		= "";
		gt_option.id		= elementCorrelationId + "_00000.1337";
		gt_select.add(gt_option);
		
		if (newCorrelationId === true)
		{		
			gt_option			= document.createElement("option");
			gt_option.text		= "new ID (nID)";
			gt_option.value		= "##nid##";
			gt_option.id		= elementCorrelationId + "_00000.new";
			gt_select.add(gt_option);
			
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementCorrelationId + "_00000.42";
			gt_select.add(gt_option);
		}
		
		// collect correlationIds from variables (list variables)
		for (var gt_varId in behavior.variables)
		{
			gt_varArray[gt_varArray.length]	= {id: gt_varId, text: behavior.variables[gt_varId]};
		}
		
		// sort the channels alphabetically
		gt_varArray.sort(gf_guiSortArrayByText);
		
		// add the channels to the select field
		for (var gt_vid in gt_varArray)
		{
			gt_option		= document.createElement("option");
			gt_option.text	= gf_replaceNewline(gt_varArray[gt_vid].text, " ");
			gt_option.value	= gt_varArray[gt_vid].id;
			gt_option.id	= elementCorrelationId + "_" + gt_vid;
			gt_select.add(gt_option);
		}
	}
}

/**
 * This method is used to fill a select field with all available messageTypes.
 * 
 * @param {String} elementMessage The ID of the select element that holds the available messaageTypes.
 * @param {boolean} newMessage When set to true an option will be added to create a new messageType.
 * @param {boolean} wildcard When set to true an option will be added to the select field to select all messageTypes (wildcard).
 * @returns {void}
 */
function gf_guiLoadDropDownMessageTypes (elementMessage, newMessage, wildcard)
{
	if (!gf_isset(newMessage) || newMessage !== true)
		newMessage = false;
		
	if (!gf_isset(wildcard) || wildcard !== true)
		wildcard = false;
	
	// load messages	
	if (elementMessage != null && gf_elementExists(elementMessage))
	{
		var gt_select			= document.getElementById(elementMessage).options;
			gt_select.length	= 0;
		var gt_messagesArray	= [];
		
		// create some entries to guide the user
		var gt_option			= document.createElement("option");
			gt_option.text		= "please select";
			gt_option.value		= "";
			gt_option.id		= elementMessage + "_00000.0";
			gt_select.add(gt_option);
		
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementMessage + "_00000.1";
			gt_select.add(gt_option);
		
		// options for select all messages
		if (wildcard === true)
		{
			gt_option			= document.createElement("option");
			gt_option.text		= "all messages";
			gt_option.value		= "##all##";
			gt_option.id		= elementMessage + "_00000.all";
			gt_select.add(gt_option);
		
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementMessage + "_00000.3";
			gt_select.add(gt_option);
		}
		
		// collect messageTypes
		for (var gt_messageType in gv_graph.messageTypes)
		{
			gt_messagesArray[gt_messagesArray.length]	= {id: gt_messageType, text: gv_graph.messageTypes[gt_messageType]};
		}
		
		// sort the messages alphabetically
		gt_messagesArray.sort(gf_guiSortArrayByText);
		
		// add the messages to the select field
		for (var gt_mid in gt_messagesArray)
		{
			gt_option		= document.createElement("option");
			gt_option.text	= gf_replaceNewline(gt_messagesArray[gt_mid].text, " ");
			gt_option.value	= gt_messagesArray[gt_mid].id;
			gt_option.id	= elementMessage + "_" + gt_mid;
			gt_select.add(gt_option);
		}
		
		if (newMessage === true)
		{
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementMessage + "_00000.42";
			gt_select.add(gt_option);
		
			gt_option			= document.createElement("option");
			gt_option.text		= "create a new message type";
			gt_option.value		= "##createNew##";
			gt_option.id		= elementMessage + "_00000.new";
			gt_select.add(gt_option);
		}
	}
}

/**
 * This method is used to fill a select field with all available start states of the current internal behavior.
 * 
 * @param {GCbehavior} behavior The currently selected internal behavior (used to read the states).
 * @param {String} elementState The ID of the select element that holds the available states.
 * @returns {void}
 */
function gf_guiLoadDropDownStates (behavior, elementState)
{	
	// load start states	
	if (elementState != null && gf_elementExists(elementState))
	{
		var gt_select			= document.getElementById(elementState).options;
			gt_select.length	= 0;
		var gt_statesArray	= [];
		
		// create some entries to guide the user
		var gt_option			= document.createElement("option");
			gt_option.text		= "please select";
			gt_option.value		= "";
			gt_option.id		= elementState + "_00000.0";
			gt_select.add(gt_option);
		
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementState + "_00000.1";
			gt_select.add(gt_option);
		
		// collect start states
		var gt_node	= null;
		for (var gt_nodeId in behavior.nodes)
		{
			gt_node	= behavior.nodes[gt_nodeId];
			if (gt_node.isStart() && !gt_node.isMajorStartNode())
			{
				gt_statesArray[gt_statesArray.length]	= {id: gt_nodeId, text: behavior.nodes[gt_nodeId].text};
			}
		}
		
		// sort the start states alphabetically
		gt_statesArray.sort(gf_guiSortArrayByText);
		
		// add the states to the select field
		for (var gt_nid in gt_statesArray)
		{
			gt_option		= document.createElement("option");
			gt_option.text	= gf_replaceNewline(gt_statesArray[gt_nid].text, " ");
			gt_option.value	= gt_statesArray[gt_nid].id;
			gt_option.id	= elementState + "_" + gt_nid;
			gt_select.add(gt_option);
		}
	}
}

/**
 * This method is used to fill a select field with all available subjects.
 * 
 * @param {String} elementSubject The ID of the select element that holds the available subjects.
 * @param {String} excludeSubject The subject to exclude from the list.
 * @param {boolean} wildcard When set to true an option will be added to the select field to select all subjects (wildcard).
 * @returns {void}
 */
function gf_guiLoadDropDownSubjects (elementSubject, excludeSubject, wildcard)
{
	if (!gf_isset(excludeSubject))
		excludeSubject	= null;
		
	if (!gf_isset(wildcard) || wildcard !== true)
		wildcard = false;
	
	
	// load subjects
	if (elementSubject != null && gf_elementExists(elementSubject))
	{
		var gt_select			= document.getElementById(elementSubject).options;
			gt_select.length	= 0;
		
		var gt_option			= document.createElement("option");
			gt_option.text		= "please select";
			gt_option.value		= "";
			gt_option.id		= elementSubject + "_00000.0";
			gt_select.add(gt_option);
		
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementSubject + "_00000.1";
			gt_select.add(gt_option);
		
		// options for selecting all subjects
		if (wildcard === true)
		{
			gt_option			= document.createElement("option");
			gt_option.text		= "all subjects";
			gt_option.value		= "##all##";
			gt_option.id		= elementSubject + "_00000.all";
			gt_select.add(gt_option);
			
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementSubject + "_00000.3";
			gt_select.add(gt_option);
		}
		
		// read the subjects that can be related
		var gt_subjectArray = [];
		
		for (var gt_sid in gv_graph.subjects)
		{
			if (gt_sid != excludeSubject)
			{
				gt_subjectArray[gt_subjectArray.length]		= gv_graph.subjects[gt_sid].getText() + " (" + gv_graph.subjects[gt_sid].getRole() + ")##;##" + gt_sid;
			}
		}
		
		// sort the subjects
		gt_subjectArray.sort();
		
		// add the subjects as options to the select field
		for (var gt_sid in gt_subjectArray)
		{						
			var gt_subjArray	= gt_subjectArray[gt_sid].split("##;##");
			var gt_subjID		= gt_subjArray[1];
			
			gt_option		= document.createElement("option");
			gt_option.text	= gf_replaceNewline(gt_subjArray[0], " ");
			gt_option.value = gt_subjID;
			gt_option.id	= elementSubject + "_" + gt_subjID;
			gt_select.add(gt_option);
		}
	}
}

/**
 * This method is used to fill a select field with all available variables of the current internal behavior.
 * 
 * @param {GCbehavior} behavior The currently selected internal behavior (used to read the variables).
 * @param {String} elementVariable The ID of the select element that holds the available variables.
 * @param {boolean} newVariable When set to true an option will be added to create a new variable.
 * @param {boolean} wildcard When set to true an option will be added to the select field to select all variables (wildcard).
 * @returns {void}
 */
function gf_guiLoadDropDownVariables (behavior, elementVariable, newVariable, wildcard)
{
	if (!gf_isset(newVariable) || newVariable !== true)
		newVariable = false;
		
	if (!gf_isset(wildcard) || wildcard !== true)
		wildcard = false;
	
	// load variables	
	if (elementVariable != null && gf_elementExists(elementVariable))
	{
		var gt_select			= document.getElementById(elementVariable).options;
			gt_select.length	= 0;
		var gt_variableArray	= [];
		
		// create some entries to guide the user
		var gt_option			= document.createElement("option");
			gt_option.text		= "please select";
			gt_option.value		= "";
			gt_option.id		= elementVariable + "_00000.0";
			gt_select.add(gt_option);
		
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementVariable + "_00000.1";
			gt_select.add(gt_option);
		
		// options for select all variables
		if (wildcard === true)
		{
			gt_option			= document.createElement("option");
			gt_option.text		= "all variables";
			gt_option.value		= "##all##";
			gt_option.id		= elementVariable + "_00000.all";
			gt_select.add(gt_option);
		
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementVariable + "_00000.3";
			gt_select.add(gt_option);
		}
		
		// collect variables
		for (var gt_varId in behavior.variables)
		{
			gt_variableArray[gt_variableArray.length]	= {id: gt_varId, text: behavior.variables[gt_varId]};
		}
		
		// sort the variables alphabetically
		gt_variableArray.sort(gf_guiSortArrayByText);
		
		// add the variables to the select field
		for (var gt_vid in gt_variableArray)
		{
			gt_option		= document.createElement("option");
			gt_option.text	= gf_replaceNewline(gt_variableArray[gt_vid].text, " ");
			gt_option.value	= gt_variableArray[gt_vid].id;
			gt_option.id	= elementVariable + "_" + gt_vid;
			gt_select.add(gt_option);
		}
		
		if (newVariable === true)
		{
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementVariable + "_00000.42";
			gt_select.add(gt_option);
		
			gt_option			= document.createElement("option");
			gt_option.text		= "create a new variable";
			gt_option.value		= "##createNew##";
			gt_option.id		= elementVariable + "_00000.new";
			gt_select.add(gt_option);
		}
	}
}

/**
 * Fills a select with all available boolean operations that can be used for the variable manipulation.
 * 
 * @param {String} elementVarMan The ID of the select element that holds the available variable operations.
 * @returns {void}
 */
function gf_guiLoadDropDownVarManOperations (elementVarMan)
{
	// load operations	
	if (elementVarMan != null && gf_elementExists(elementVarMan))
	{
		var gt_select			= document.getElementById(elementVarMan).options;
			gt_select.length	= 0;
		var gt_variableArray	= [];
		
		// create some entries to guide the user
		var gt_option			= document.createElement("option");
			gt_option.text		= "please select";
			gt_option.value		= "";
			gt_option.id		= elementVarMan + "_00000.0";
			gt_select.add(gt_option);
		
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementVarMan + "_00000.1";
			gt_select.add(gt_option);
		
			gt_option			= document.createElement("option");
			gt_option.text		= "assign new";
			gt_option.value		= "new";
			gt_option.id		= elementVarMan + "_new";
			gt_select.add(gt_option);
		
		// add the operations to the select field
		for (var gt_vmid in gv_varManOperations)
		{
			gt_option		= document.createElement("option");
			gt_option.text	= gv_varManOperations[gt_vmid];
			gt_option.value	= gt_vmid;
			gt_option.id	= elementVarMan + "_" + gt_vmid;
			gt_select.add(gt_option);
		}
	}
}

/**
 * Refresh the list of channels to select.
 * 
 * @param {String} view Just needed when the view is changed.
 * @returns {void}
 */
function gf_guiLoadDropDownForChannelSelect (view)
{
	if (!gf_isset(view))
		view = "";
	
	if (gf_elementExists(gv_elements.guiChannelSelect))
	{
		var gt_value	= view == "" ? gv_graph.getSelectedChannel() : gv_graph.getSelectedChannel(view);
			gt_value	= gt_value == null ? "##all##" : gt_value;
		
		gf_guiLoadDropDownChannels(gv_elements.guiChannelSelect, false, true, view == "cv");
		document.getElementById(gv_elements.guiChannelSelect).value = gt_value;
		
		document.getElementById(gv_elements.guiChannelSelect).onchange = function ()
		{
			var gt_selected	= document.getElementById(gv_elements.guiChannelSelect).value;
			
			gf_selectChannel(gt_selected);
		};
	}
}

/**
 * This method is used to fill drop downs for node settings.
 * 
 * @param {GCbehavior} behavior Used for collecting several information.
 * @param {String} nodeType The type of the node.
 * @returns {void}
 */
function gf_guiLoadDropDownForNode (behavior, nodeType)
{
	if (!gf_isset(nodeType))
		nodeType	= "action";
		
	if (gf_elementExists(gv_elements.inputNodeOptMessageO))
		document.getElementById(gv_elements.inputNodeOptMessageO).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeOptSubjectO))
		document.getElementById(gv_elements.inputNodeOptSubjectO).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeOptChannelOuter))
		document.getElementById(gv_elements.inputNodeOptChannelOuter).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeOptCorrelationIdO))
		document.getElementById(gv_elements.inputNodeOptCorrelationIdO).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeOptStateOuter))
		document.getElementById(gv_elements.inputNodeOptStateOuter).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeOptionsOuter))
		document.getElementById(gv_elements.inputNodeOptionsOuter).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeVariableO))
		document.getElementById(gv_elements.inputNodeVariableO).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeVarManOuter))
		document.getElementById(gv_elements.inputNodeVarManOuter).style.display = "none";
		
	if (nodeType == "$variableman")
	{	
		if (gf_elementExists(gv_elements.inputNodeVarManOuter))
			document.getElementById(gv_elements.inputNodeVarManOuter).style.display = "block";
			
		gf_guiLoadDropDownVariables(behavior, gv_elements.inputNodeVarManVar1, false, false);
		gf_guiLoadDropDownVariables(behavior, gv_elements.inputNodeVarManVar2, false, false);
		gf_guiLoadDropDownVariables(behavior, gv_elements.inputNodeVarManVarStore, true, false);
		gf_guiLoadDropDownVarManOperations(gv_elements.inputNodeVarManOperation);
			
	}
	if (nodeType.substr(0, 1) == "$")
	{
		var gt_predefAction	= {subject: false, message: false, wildcard: false, channel: false, correlationid: false, options: false, state: false};
		
		
		if (gf_isset(gv_predefinedActions[nodeType.substr(1)]))
			gt_predefAction	= gv_predefinedActions[nodeType.substr(1)];
			
		var gt_showOptions	= false;
			
		if (gf_elementExists(gv_elements.inputNodeOptMessageO) && gt_predefAction.message)
		{
			gt_showOptions	= true;
			document.getElementById(gv_elements.inputNodeOptMessageO).style.display = "block";
		}
			
		if (gf_elementExists(gv_elements.inputNodeOptSubjectO) && gt_predefAction.subject)
		{
			gt_showOptions	= true;
			document.getElementById(gv_elements.inputNodeOptSubjectO).style.display = "block";
		}
			
		if (gf_elementExists(gv_elements.inputNodeOptChannelOuter) && gt_predefAction.channel)
		{
			gt_showOptions	= true;
			document.getElementById(gv_elements.inputNodeOptChannelOuter).style.display = "block";
		}
			
		if (gf_elementExists(gv_elements.inputNodeOptCorrelationIdO) && gt_predefAction.correlationid)
		{
			gt_showOptions	= true;
			document.getElementById(gv_elements.inputNodeOptCorrelationIdO).style.display = "block";
		}
			
		if (gf_elementExists(gv_elements.inputNodeOptStateOuter) && gt_predefAction.state)
		{
			gt_showOptions	= true;
			document.getElementById(gv_elements.inputNodeOptStateOuter).style.display = "block";
		}
			
		if (gf_elementExists(gv_elements.inputNodeOptionsOuter) && gt_showOptions)
			document.getElementById(gv_elements.inputNodeOptionsOuter).style.display = "block";
			
		if (gt_showOptions)
		{
			gf_guiLoadDropDownMessageTypes(gv_elements.inputNodeOptMessage, false, gt_predefAction.wildcard);
			gf_guiLoadDropDownSubjects(gv_elements.inputNodeOptSubject, null, gt_predefAction.wildcard);
			gf_guiLoadDropDownCorrelationIds(behavior, gv_elements.inputNodeOptCorrelationId, false, gt_predefAction.wildcard);
			gf_guiLoadDropDownChannels(gv_elements.inputNodeOptChannel, false, gt_predefAction.wildcard);
			gf_guiLoadDropDownStates(behavior, gv_elements.inputNodeOptState);
		}
	}
	else if (nodeType == "action")
	{
		if (gf_elementExists(gv_elements.inputNodeVariableO))
			document.getElementById(gv_elements.inputNodeVariableO).style.display = "block";
			
		gf_guiLoadDropDownVariables(behavior, gv_elements.inputNodeVariable, false, false);
	}
}

/**
 * Wrapper around window.SBPM.VM.Mediator.goToExternalSubject used to load processes referenced to by an external subject.
 * 
 * @param {String} process The name of the referenced process.
 * @returns {void}
 */
function gf_guiLoadExternalProcess (process)
{
	if (gf_isset(window.SBPM))
		if (gf_isset(window.SBPM.Mediator))
			if (gf_isset(window.SBPM.Mediator.goToExternalSubject))
				window.SBPM.Mediator.goToExternalSubject(process);
			else
				console.log("tk_graph: Error on loading external process: goToExternalSubject");
		else
			console.log("tk_graph: Error on loading external process: Mediator");
	else
		console.log("tk_graph: Error on loading external process: SBPM");
}

/**
 * Read the values for the selected edge from the input fields.
 * 
 * @see GCcommunication::updateEdge()
 * @returns {Object} Indizes: text, relatedSubject, type, timeout, optional, messageType, priority, manualTimeout, exception, variable, variableText, correlationId
 */
function gf_guiReadEdge ()
{
	var gt_result	= {text: "", relatedSubject: "", timeout: "", type: "", optional: false, messageType: "", priority: 1, manualTimeout: false, exception: "", variable: "", variableText: "", correlationId: ""};
	
	var gt_relatedSubject	= {id: "", min: -1, max: -1, createNew: false, variable: "", variableText: "", useVariable: false};
	
	var gt_text				= gf_elementExists(gv_elements.inputEdgeText) ? document.getElementById(gv_elements.inputEdgeText).value : "";
	var gt_exception		= gf_elementExists(gv_elements.inputEdgeExceptionText) ? document.getElementById(gv_elements.inputEdgeExceptionText).value : "";
	var gt_timeout			= gf_elementExists(gv_elements.inputEdgeTimeout) ? document.getElementById(gv_elements.inputEdgeTimeout).value : "";
	var gt_optional			= gf_elementExists(gv_elements.inputEdgeOptional) && document.getElementById(gv_elements.inputEdgeOptional).checked;
	var gt_messageType		= gf_elementExists(gv_elements.inputEdgeMessage) ? document.getElementById(gv_elements.inputEdgeMessage).value : "";
	var gt_priority			= gf_elementExists(gv_elements.inputEdgePriority) ? document.getElementById(gv_elements.inputEdgePriority).value : "1";
	var gt_manualTimeout	= gf_elementExists(gv_elements.inputEdgeTimeoutManual) ? document.getElementById(gv_elements.inputEdgeTimeoutManual).checked : false;
	
	var gt_targetVar		= gf_elementExists(gv_elements.inputEdgeTargetMVariable) ? document.getElementById(gv_elements.inputEdgeTargetMVariable).value : "";
	var gt_targetVarNew		= gf_elementExists(gv_elements.inputEdgeTargetMVarText) ? document.getElementById(gv_elements.inputEdgeTargetMVarText).value : "";
	var gt_storeVar			= gf_elementExists(gv_elements.inputEdgeStoreVariable) ? document.getElementById(gv_elements.inputEdgeStoreVariable).value : "";
	var gt_storeVarNew		= gf_elementExists(gv_elements.inputEdgeStoreVariableN) ? document.getElementById(gv_elements.inputEdgeStoreVariableN).value : "";
	
	var gt_correlationId	= gf_elementExists(gv_elements.inputEdgeCorrelationId) ? document.getElementById(gv_elements.inputEdgeCorrelationId).value : "";
	
	// var gt_var create new; var (store) |||| target create new; target var || bei create new: ##createNew##varText
	
	var gt_isVariable		= gf_elementExists(gv_elements.inputEdgeTargetMTypeV) && document.getElementById(gv_elements.inputEdgeTargetMTypeV).checked;
	var gt_isAll			= gf_elementExists(gv_elements.inputEdgeTargetMTypeA) && document.getElementById(gv_elements.inputEdgeTargetMTypeA).checked;
	
	gt_relatedSubject.id			= gf_elementExists(gv_elements.inputEdgeTarget) ? document.getElementById(gv_elements.inputEdgeTarget).value : "";
	gt_relatedSubject.min			= gf_elementExists(gv_elements.inputEdgeTargetMMin) && !gt_isAll ? document.getElementById(gv_elements.inputEdgeTargetMMin).value : "-1";
	gt_relatedSubject.max			= gf_elementExists(gv_elements.inputEdgeTargetMMax) && !gt_isAll ? document.getElementById(gv_elements.inputEdgeTargetMMax).value : "-1";
	gt_relatedSubject.createNew		= gf_elementExists(gv_elements.inputEdgeTargetMTypeN) && document.getElementById(gv_elements.inputEdgeTargetMTypeN).checked;
	gt_relatedSubject.variable		= gt_isVariable ? gt_targetVar : "";
	gt_relatedSubject.variableText	= gt_isVariable ? gt_targetVarNew : "";
	gt_relatedSubject.useVariable	= gt_isVariable;
	
	var gt_type				= "exitcondition";
	
	if (gf_elementExists(gv_elements.inputEdgeTypeTimeout) && document.getElementById(gv_elements.inputEdgeTypeTimeout).checked)
		gt_type	= "timeout";
		
	if (gf_elementExists(gv_elements.inputEdgeTypeException) && document.getElementById(gv_elements.inputEdgeTypeException).checked)
		gt_type	= "errorcondition";
	
	gt_result.text				= gt_text;
	gt_result.relatedSubject	= gt_relatedSubject;
	gt_result.timeout			= gt_timeout;
	gt_result.exception			= gt_exception;
	gt_result.type				= gt_type;
	gt_result.optional			= gt_optional;
	gt_result.messageType		= gt_messageType;
	gt_result.priority			= gt_priority;
	gt_result.manualTimeout		= gt_manualTimeout;
	gt_result.variable			= gt_storeVar;
	gt_result.variableText		= gt_storeVarNew;
	gt_result.correlationId		= gt_correlationId;
	
	return gt_result;
}

/**
 * Read the values for the selected node from the input fields.
 * 
 * @see GCcommunication::updateNode()
 * @returns {Object} Indizes: text, isStart, type, options, isMajorStartNode, channel, channelText, variable, varMan
 */
function gf_guiReadNode ()
{
	var gt_result	= {text: "", isStart: false, type: "", options: {subject: "", message: "", correlationId: "", channel: "", state: ""}, isMajorStartNode: false, channel: "", channelText: "", variable: "", varMan: {var1: "", var2: "", storevar: "", operation: "", storevarText: ""}};
	
	var gt_text					= gf_elementExists(gv_elements.inputNodeText) ? document.getElementById(gv_elements.inputNodeText).value : "";
	var gt_isStart				= gf_elementExists(gv_elements.inputNodeStart) && document.getElementById(gv_elements.inputNodeStart).checked;
	var gt_type 				= gf_elementExists(gv_elements.inputNodeType) ? document.getElementById(gv_elements.inputNodeType).value.toLowerCase() : "";
	var gt_opt_subject 			= gf_elementExists(gv_elements.inputNodeOptSubject) ? document.getElementById(gv_elements.inputNodeOptSubject).value : "";
	var gt_opt_message 			= gf_elementExists(gv_elements.inputNodeOptMessage) ? document.getElementById(gv_elements.inputNodeOptMessage).value : "";
	var gt_opt_channel 			= gf_elementExists(gv_elements.inputNodeOptChannel) ? document.getElementById(gv_elements.inputNodeOptChannel).value : "";
	var gt_opt_correlationId 	= gf_elementExists(gv_elements.inputNodeOptCorrelationId) ? document.getElementById(gv_elements.inputNodeOptCorrelationId).value : "";
	var gt_opt_state 			= gf_elementExists(gv_elements.inputNodeOptState) ? document.getElementById(gv_elements.inputNodeOptState).value : "";
	var gt_isMajorStartNode		= gf_elementExists(gv_elements.inputNodeMajorStart) && document.getElementById(gv_elements.inputNodeMajorStart).checked;
	
	
	var gt_channel			= gf_elementExists(gv_elements.inputNodeChannel) ? document.getElementById(gv_elements.inputNodeChannel).value : "";
	var gt_channelNew		= gf_elementExists(gv_elements.inputNodeChannelNew) ? document.getElementById(gv_elements.inputNodeChannelNew).value : "";
	var gt_variable			= gf_elementExists(gv_elements.inputNodeVariable) ? document.getElementById(gv_elements.inputNodeVariable).value : "";
	
	
	var gt_varMan	= {};
		gt_varMan.var1			= gf_elementExists(gv_elements.inputNodeVarManVar1) ? document.getElementById(gv_elements.inputNodeVarManVar1).value : "";
		gt_varMan.var2			= gf_elementExists(gv_elements.inputNodeVarManVar2) ? document.getElementById(gv_elements.inputNodeVarManVar2).value : "";
		gt_varMan.storevar		= gf_elementExists(gv_elements.inputNodeVarManVarStore) ? document.getElementById(gv_elements.inputNodeVarManVarStore).value : "";
		gt_varMan.operation		= gf_elementExists(gv_elements.inputNodeVarManOperation) ? document.getElementById(gv_elements.inputNodeVarManOperation).value : "";
		gt_varMan.storevarText	= gf_elementExists(gv_elements.inputNodeVarManVarStoreN) ? document.getElementById(gv_elements.inputNodeVarManVarStoreN).value : "";
	
	var gt_options		= {};
	
	gt_options.subject			= gt_opt_subject;
	gt_options.message			= gt_opt_message;
	gt_options.channel			= gt_opt_channel;
	gt_options.correlationId	= gt_opt_correlationId;
	gt_options.state			= gt_opt_state;
	
	gt_result.text				= gt_text;
	gt_result.isStart			= gt_isStart;
	gt_result.type				= gt_type;
	gt_result.options			= gt_options;
	gt_result.isMajorStartNode	= gt_isMajorStartNode;
	gt_result.channel			= gt_channel;
	gt_result.channelText		= gt_channelNew;
	gt_result.variable			= gt_variable;
	gt_result.varMan			= gt_varMan;
	
	return gt_result;
}

/**
 * Read the values for the selected subject from the input fields.
 * 
 * @see GCcommunication::updateSubject()
 * @returns {Object} Indizes: text, role, type, inputPool, relatedProcess, relatedSubject, externalType
 */
function gf_guiReadSubject ()
{
	var gt_result	= {text: "", role: "", type: "", inputPool: "", relatedProcess: "", relatedSubject: "", externalType: ""};
	
	var gt_text			= gf_elementExists(gv_elements.inputSubjectText)		? document.getElementById(gv_elements.inputSubjectText).value		: "";
	var gt_role			= gf_elementExists(gv_elements.inputSubjectRole)		? document.getElementById(gv_elements.inputSubjectRole).value		: "";
	var gt_inputPool	= gf_elementExists(gv_elements.inputSubjectInputPool)	? document.getElementById(gv_elements.inputSubjectInputPool).value	: "";
	var gt_relProcess	= gf_elementExists(gv_elements.inputSubjectRelProcess)	? document.getElementById(gv_elements.inputSubjectRelProcess).value	: "";
	var gt_relSubject	= gf_elementExists(gv_elements.inputSubjectRelSubject)	? document.getElementById(gv_elements.inputSubjectRelSubject).value	: "";
	
	var gt_type	= "";
	var gt_externalType	= "external";
	
	if (gf_elementExists(gv_elements.inputSubjectTypeMulti)		&& document.getElementById(gv_elements.inputSubjectTypeMulti).checked		=== true)
		gt_type += "multi";
	
	if (gf_elementExists(gv_elements.inputSubjectTypeExternal)	&& document.getElementById(gv_elements.inputSubjectTypeExternal).checked	=== true)
		gt_type += "external";
		
	if (gt_type == "")
		gt_type = "single";
	
	if (gf_elementExists(gv_elements.inputSubjectExtInstantInterface)	&& document.getElementById(gv_elements.inputSubjectExtInstantInterface).checked	=== true)
		gt_externalType	= "instantinterface";
		
	if (gf_elementExists(gv_elements.inputSubjectExtInterface)			&& document.getElementById(gv_elements.inputSubjectExtInterface).checked		=== true)
		gt_externalType	= "interface";
	
	gt_result.text				= gt_text;
	gt_result.role				= gt_role;
	gt_result.type				= gt_type;
	gt_result.inputPool			= gt_inputPool;
	gt_result.relatedProcess	= gt_relProcess;
	gt_result.relatedSubject	= gt_relSubject;
	gt_result.externalType		= gt_externalType;
	
	return gt_result;
}

/**
 * This is called onChange of gv_elements.inputEdgeMessage.
 * It updates the value of gv_elements.inputEdgeText with the selected message so the edge can be updated correctly.
 * 
 * @returns {void}
 */
function gf_guiSetEdgeMessage ()
{
	if (gf_elementExists(gv_elements.inputEdgeText, gv_elements.inputEdgeMessage))
	{
		var gt_message	= document.getElementById(gv_elements.inputEdgeMessage).value;
		
		// when the entry "##createNew##" is selected -> unlock the textarea and let the user define a new message
		if (gt_message == "##createNew##")
		{
			document.getElementById(gv_elements.inputEdgeText).readOnly	= false;
			document.getElementById(gv_elements.inputEdgeText).value 	= "";	
		}
		else if (gt_message.substr(0, 1) == "m")
		{
			document.getElementById(gv_elements.inputEdgeText).readOnly	= false;
			document.getElementById(gv_elements.inputEdgeText).value 	= gv_graph.messageTypes[gt_message];
		}
	}
}

/**
 * Sort messages ascending by text.
 * 
 * @param {Object} obj1
 * @param {Object} obj2
 * @returns {int}
 */
function gf_guiSortArrayByText (obj1, obj2)
{
	if (obj1.text.toLowerCase() > obj2.text.toLowerCase())
		return 1;
		
	if (obj1.text.toLowerCase() < obj2.text.toLowerCase())
		return -1;
		
	return 0;
}

/**
 * Show / hide the forms containing the input fields for either the node settings or the edge settings.
 * 
 * @see GCcommunication::loadInformation() | GCcommunication::loadInformationEdge()
 * @param {String} type Either "n" or "e" (node or edge)
 * @returns {void}
 */
function gf_guiToggleNEForms (type)
{
	if (!gf_isset(type))
		type = "n";
		
	type	= type.toLowerCase();
	
	if (gf_elementExists(gv_elements.inputNodeOuter))
		document.getElementById(gv_elements.inputNodeOuter).style.display = type == "n" ? "block" : "none";
	if (gf_elementExists(gv_elements.inputEdgeOuter))
		document.getElementById(gv_elements.inputEdgeOuter).style.display = type == "e" ? "block" : "none";
}
