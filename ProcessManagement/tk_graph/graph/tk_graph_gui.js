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
	if (gf_elementExists(gv_elements.inputNodeMessageO))
		document.getElementById(gv_elements.inputNodeMessageO).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeSubjectO))
		document.getElementById(gv_elements.inputNodeSubjectO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgePriorityO))
		document.getElementById(gv_elements.inputEdgePriorityO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeTargetMMMO))
		document.getElementById(gv_elements.inputEdgeTargetMMMO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeTargetMOuter))
		document.getElementById(gv_elements.inputEdgeTargetMOuter).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeTargetMTypeNO))
		document.getElementById(gv_elements.inputEdgeTargetMTypeNO).style.display = "none";
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
	
	
	// create the drop down menu to select the related subject (only for receive and send nodes)
	if (startType == "send" || startType == "receive")
	{		
		
		if (gf_elementExists(gv_elements.inputEdgeTargetO))
			document.getElementById(gv_elements.inputEdgeTargetO).style.display = "block";
			
		if (gf_elementExists(gv_elements.inputEdgeTargetO))
			document.getElementById(gv_elements.inputEdgeMessage).onchange			= gf_guiSetEdgeMessage;
		
		gf_guiLoadDropDown(gv_elements.inputEdgeMessage, gv_elements.inputEdgeTarget, gv_graph.selectedSubject, true);
		
		// show the radio buttons for types
		if (edge.getRelatedSubject("multi") && gf_elementExists(gv_elements.inputEdgeTargetMOuter))
			document.getElementById(gv_elements.inputEdgeTargetMOuter).style.display = "block";
			
		if (edge.getRelatedSubject("multi") && startType == "send" && gf_elementExists(gv_elements.inputEdgeTargetMTypeNO))
			document.getElementById(gv_elements.inputEdgeTargetMTypeNO).style.display = "block";
			
		var gt_isAll		= edge.getRelatedSubject("min") == "-1" && edge.getRelatedSubject("max") == "-1";
		var gt_createNew	= edge.getRelatedSubject("createNew");
		
		if (!gt_isAll && gf_elementExists(gv_elements.inputEdgeTargetMMMO))
			document.getElementById(gv_elements.inputEdgeTargetMMMO).style.display = "block";
		
		if (gf_elementExists(gv_elements.inputEdgeTarget))
			document.getElementById(gv_elements.inputEdgeTarget).value	= edge.getRelatedSubject();
		
		if (gf_elementExists(gv_elements.inputEdgeMessage))
			document.getElementById(gv_elements.inputEdgeMessage).value	= edge.getText();
		
		if (gf_elementExists(gv_elements.inputEdgeTargetMTypeA))
			document.getElementById(gv_elements.inputEdgeTargetMTypeA).checked	= gt_isAll;
		
		if (gf_elementExists(gv_elements.inputEdgeTargetMTypeL))
			document.getElementById(gv_elements.inputEdgeTargetMTypeL).checked	= !gt_isAll && !gt_createNew;
			
		if (gf_elementExists(gv_elements.inputEdgeTargetMTypeN))
			document.getElementById(gv_elements.inputEdgeTargetMTypeN).checked	= !gt_isAll && gt_createNew;
		
		if (gf_elementExists(gv_elements.inputEdgeTargetMMin))
			document.getElementById(gv_elements.inputEdgeTargetMMin).value	= edge.getRelatedSubject("min");
		
		if (gf_elementExists(gv_elements.inputEdgeTargetMMax))
			document.getElementById(gv_elements.inputEdgeTargetMMax).value	= edge.getRelatedSubject("max");
		
		// add event listeners
		if (gf_elementExists(gv_elements.inputEdgeTargetMTypeA))
			document.getElementById(gv_elements.inputEdgeTargetMTypeA).onclick	= function () {
				var gt_clickedValue	= document.getElementById(gv_elements.inputEdgeTargetMTypeA).checked;
				
				if (gf_elementExists(gv_elements.inputEdgeTargetMMMO))
					document.getElementById(gv_elements.inputEdgeTargetMMMO).style.display = gt_clickedValue ? "none" : "block";
			};
		if (gf_elementExists(gv_elements.inputEdgeTargetMTypeL))
			document.getElementById(gv_elements.inputEdgeTargetMTypeL).onclick	= function () {
				var gt_clickedValue	= document.getElementById(gv_elements.inputEdgeTargetMTypeL).checked;
				
				if (gf_elementExists(gv_elements.inputEdgeTargetMMMO))
					document.getElementById(gv_elements.inputEdgeTargetMMMO).style.display = gt_clickedValue ? "block" : "none";
			};
		if (gf_elementExists(gv_elements.inputEdgeTargetMTypeN))
			document.getElementById(gv_elements.inputEdgeTargetMTypeN).onclick	= function () {
				var gt_clickedValue	= document.getElementById(gv_elements.inputEdgeTargetMTypeN).checked;
				
				if (gf_elementExists(gv_elements.inputEdgeTargetMMMO))
					document.getElementById(gv_elements.inputEdgeTargetMMMO).style.display = gt_clickedValue ? "block" : "none";
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
	
	if (gf_elementExists(gv_elements.inputNodeText))
		document.getElementById(gv_elements.inputNodeText).value = gf_replaceNewline(node.getText());
	
	if (gf_elementExists(gv_elements.inputNodeStart))
		document.getElementById(gv_elements.inputNodeStart).checked = node.isStart();
		
	var gt_select_type			= document.getElementById(gv_elements.inputNodeType);
	
	if (gf_elementExists(gv_elements.inputNodeType))
		document.getElementById(gv_elements.inputNodeType).onclick	= function () {
			gf_guiLoadDropDownForNode(document.getElementById(gv_elements.inputNodeType).value);
		};
	
	$('#' + gv_elements.inputNodeType).empty();
		
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
	
	if (gt_type.substr(0, 1) == "$")
	{
		gf_guiLoadDropDownForNode(gt_type);
			
		var gt_options	= node.getOptions();
		
		if (gf_isset(gt_options.subject))
		{
			document.getElementById(gv_elements.inputNodeSubject).value	= gt_options.subject;
		}
		
		if (gf_isset(gt_options.message))
		{
			document.getElementById(gv_elements.inputNodeMessage).value	= gt_options.message;
		}
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
 * This method is used to fill two select fields with all available messageTypes and subjects.
 * 
 * @param {String} elementMessage The ID of the select element that holds the available messageTypes.
 * @param {String} elementSubject The ID of the select element that holds the available subjects.
 * @param {String} excludeSubject The subject to exclude from the list.
 * @param {boolean} newMessage When set to true an option will be added to create a new messageType.
 * @param {boolean} wildcard When set to true an option will be added to each of the both selects to select either all subjects / messageTypes (wildcard).
 * @returns {void}
 */
function gf_guiLoadDropDown (elementMessage, elementSubject, excludeSubject, newMessage, wildcard)
{
	if (!gf_isset(excludeSubject))
		excludeSubject	= null;
		
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
		}
		
		if (wildcard === true)
		{
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
		gt_messagesArray.sort(gf_guiSortMessageTypes);
		
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
		}
		
		
		// options for adding new messages
		if (newMessage === true)
		{
			gt_option			= document.createElement("option");
			gt_option.text		= "create a new message type";
			gt_option.value		= "##createNewMsg##";
			gt_option.id		= elementMessage + "_00000.2";
			gt_select.add(gt_option);
		}
	}
	
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
 * This method is used to fill drop downs for node settings.
 * 
 * @param {String} nodeType The type of the node.
 * @returns {void}
 */
function gf_guiLoadDropDownForNode (nodeType)
{
	if (!gf_isset(nodeType))
		nodeType	= "action";
		
	if (gf_elementExists(gv_elements.inputNodeMessageO))
		document.getElementById(gv_elements.inputNodeMessageO).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeSubjectO))
		document.getElementById(gv_elements.inputNodeSubjectO).style.display = "none";
		
	if (nodeType.substr(0, 1) == "$")
	{
		var gt_predefAction	= gf_isset(gv_predefinedActions[nodeType.substr(1)]) ? gv_predefinedActions[nodeType.substr(1)] : {relatedSubject: false, message: false, wildcard: false};
			
		if (gf_elementExists(gv_elements.inputNodeMessageO) && gt_predefAction.message)
			document.getElementById(gv_elements.inputNodeMessageO).style.display = "block";
			
		if (gf_elementExists(gv_elements.inputNodeSubjectO) && gt_predefAction.relatedSubject)
			document.getElementById(gv_elements.inputNodeSubjectO).style.display = "block";
			
		gf_guiLoadDropDown(gv_elements.inputNodeMessage, gv_elements.inputNodeSubject, null, false, gt_predefAction.wildcard);
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
		if (gf_isset(window.SBPM.VM))
			if (gf_isset(window.SBPM.VM.Mediator))
				if (gf_isset(window.SBPM.VM.Mediator.goToExternalSubject))
					window.SBPM.VM.Mediator.goToExternalSubject(process);
}

/**
 * Read the values for the selected edge from the input fields.
 * 
 * @see GCcommunication::updateEdge()
 * @returns {Object} Indizes: text, relatedSubject, type, timeout, optional, messageType, priority, manualTimeout, exception
 */
function gf_guiReadEdge ()
{
	var gt_result	= {text: "", relatedSubject: "", timeout: "", type: "", optional: false, messageType: "", priority: 1, manualTimeout: false, exception: ""};
	
	var gt_relatedSubject	= {id: "", min: -1, max: -1, createNew: false};
	
	var gt_text				= gf_elementExists(gv_elements.inputEdgeText) ? document.getElementById(gv_elements.inputEdgeText).value : "";
	var gt_exception		= gf_elementExists(gv_elements.inputEdgeExceptionText) ? document.getElementById(gv_elements.inputEdgeExceptionText).value : "";
	var gt_timeout			= gf_elementExists(gv_elements.inputEdgeTimeout) ? document.getElementById(gv_elements.inputEdgeTimeout).value : "";
	var gt_optional			= gf_elementExists(gv_elements.inputEdgeOptional) && document.getElementById(gv_elements.inputEdgeOptional).checked;
	var gt_messageType		= gf_elementExists(gv_elements.inputEdgeMessage) ? document.getElementById(gv_elements.inputEdgeMessage).value : "";
	var gt_priority			= gf_elementExists(gv_elements.inputEdgePriority) ? document.getElementById(gv_elements.inputEdgePriority).value : "1";
	var gt_manualTimeout	= gf_elementExists(gv_elements.inputEdgeTimeoutManual) ? document.getElementById(gv_elements.inputEdgeTimeoutManual).checked : false;
	
	var gt_isAll			= gf_elementExists(gv_elements.inputEdgeTargetMTypeA) ? document.getElementById(gv_elements.inputEdgeTargetMTypeA).checked : false;
	
	gt_relatedSubject.id		= gf_elementExists(gv_elements.inputEdgeTarget) ? document.getElementById(gv_elements.inputEdgeTarget).value : "";
	gt_relatedSubject.min		= gf_elementExists(gv_elements.inputEdgeTargetMMin) && !gt_isAll ? document.getElementById(gv_elements.inputEdgeTargetMMin).value : "-1";
	gt_relatedSubject.max		= gf_elementExists(gv_elements.inputEdgeTargetMMax) && !gt_isAll ? document.getElementById(gv_elements.inputEdgeTargetMMax).value : "-1";
	gt_relatedSubject.createNew	= gf_elementExists(gv_elements.inputEdgeTargetMTypeN) && document.getElementById(gv_elements.inputEdgeTargetMTypeN).checked;
	
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
	
	return gt_result;
}

/**
 * Read the values for the selected node from the input fields.
 * 
 * @see GCcommunication::updateNode()
 * @returns {Object} Indizes: text, isStart, type, options
 */
function gf_guiReadNode ()
{
	var gt_result	= {text: "", isStart: false, type: "", options: {subject: "", message: ""}};
	
	var gt_text		= gf_elementExists(gv_elements.inputNodeText) ? document.getElementById(gv_elements.inputNodeText).value : "";
	var gt_isStart	= gf_elementExists(gv_elements.inputNodeStart) && document.getElementById(gv_elements.inputNodeStart).checked;
	var gt_type 	= gf_elementExists(gv_elements.inputNodeType) ? document.getElementById(gv_elements.inputNodeType).value.toLowerCase() : "";
	var gt_subject 	= gf_elementExists(gv_elements.inputNodeSubject) ? document.getElementById(gv_elements.inputNodeSubject).value : "";
	var gt_message 	= gf_elementExists(gv_elements.inputNodeMessage) ? document.getElementById(gv_elements.inputNodeMessage).value : "";
	
	gt_result.text		= gt_text;
	gt_result.isStart	= gt_isStart;
	gt_result.type		= gt_type;
	gt_result.options	= {subject: gt_subject, message: gt_message};
	
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
		
		// when the entry "##createNewMsg##" is selected -> unlock the textarea and let the user define a new message
		if (gt_message == "##createNewMsg##")
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
function gf_guiSortMessageTypes (obj1, obj2)
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
