/*
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
	if (gf_elementExists(gv_elements.inputNodeType2))
		document.getElementById(gv_elements.inputNodeType2).value = "";
	
	if (gf_elementExists(gv_elements.inputSubjectText))
		document.getElementById(gv_elements.inputSubjectText).value = "";
	if (gf_elementExists(gv_elements.inputSubjectId))
		document.getElementById(gv_elements.inputSubjectId).value = "";
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
	if (gf_elementExists(gv_elements.inputEdgeTypeTimeout))
		document.getElementById(gv_elements.inputEdgeTypeTimeout).checked = false;
		
	if (gf_elementExists(gv_elements.inputEdgeTargetO))
		document.getElementById(gv_elements.inputEdgeTargetO).style.display = "none";
	if (gf_elementExists(gv_elements.inputNodeOptionsO))
		document.getElementById(gv_elements.inputNodeOptionsO).style.display = "none";
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
		document.getElementById(gv_elements.inputEdgeText).value	= edge.getMessageTypeId() == "" ? edge.getText() : edge.getMessageType();
		document.getElementById(gv_elements.inputEdgeText).readOnly	= false;					
	}
	
	if (gf_elementExists(gv_elements.inputEdgeTimeout))
	{
		document.getElementById(gv_elements.inputEdgeTimeout).value	= edge.getTimer("timestamp") > 0 ? edge.getTimer() : "";					
	}
	
	if (gf_elementExists(gv_elements.inputEdgeTimeoutEx))
	{
		document.getElementById(gv_elements.inputEdgeTimeoutEx).innerHTML	= "(example: " + edge.getTimer("example") + ")";					
	}
	
	if (gf_elementExists(gv_elements.inputEdgeTargetO))
		document.getElementById(gv_elements.inputEdgeTargetO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeOptionalO))
		document.getElementById(gv_elements.inputEdgeOptionalO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeTypeCondO))
		document.getElementById(gv_elements.inputEdgeTypeCondO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeTypeTimeoutO))
		document.getElementById(gv_elements.inputEdgeTypeTimeoutO).style.display = "none";
		
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
			if (gf_elementExists(gv_elements.inputEdgeTypeTimeoutO))
				document.getElementById(gv_elements.inputEdgeTypeTimeoutO).style.display = gt_status ? "block" : "none";
		};
	if (gf_elementExists(gv_elements.inputEdgeTypeCondition))
		document.getElementById(gv_elements.inputEdgeTypeCondition).onclick = function () {
			var gt_status	= document.getElementById(gv_elements.inputEdgeTypeCondition).checked;
			
			if (gf_elementExists(gv_elements.inputEdgeTypeCondO))
				document.getElementById(gv_elements.inputEdgeTypeCondO).style.display = gt_status ? "block" : "none";
			if (gf_elementExists(gv_elements.inputEdgeTypeTimeoutO))
				document.getElementById(gv_elements.inputEdgeTypeTimeoutO).style.display = gt_status ? "none" : "block";
		};
	
	// disable radio buttons when type may not be changed
	if (gf_checkCardinality(gv_graph.selectedSubject, edge.getStart(), edge.getEnd()) == false)
	{
		if (gf_elementExists(gv_elements.inputEdgeTypeTimeout))
			document.getElementById(gv_elements.inputEdgeTypeTimeout).disabled = true;
			
		if (gf_elementExists(gv_elements.inputEdgeTypeCondition))
			document.getElementById(gv_elements.inputEdgeTypeCondition).disabled = true;
	}
	else
	{
		if (gf_elementExists(gv_elements.inputEdgeTypeTimeout))
			document.getElementById(gv_elements.inputEdgeTypeTimeout).disabled = false;
			
		if (gf_elementExists(gv_elements.inputEdgeTypeCondition))
			document.getElementById(gv_elements.inputEdgeTypeCondition).disabled = false;	
	}
	
	// create the drop down menu to select the related subject (only for receive and send nodes)
	if (startType == "send" || startType == "receive")
	{		
		
		if (gf_elementExists(gv_elements.inputEdgeTargetO))
			document.getElementById(gv_elements.inputEdgeTargetO).style.display = "block";
			
		document.getElementById(gv_elements.inputEdgeMessage).onchange			= gf_guiSetEdgeMessage;
		
		gf_guiLoadDropDown(gv_elements.inputEdgeMessage, gv_elements.inputEdgeTarget, gv_graph.selectedSubject, true);
		
		document.getElementById(gv_elements.inputEdgeTarget).value	= edge.getRelatedSubject();
		document.getElementById(gv_elements.inputEdgeMessage).value	= edge.getText();
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
	if (!gf_elementExists(gv_elements.inputNodeType2))
		return false;
	
	if (gf_elementExists(gv_elements.inputNodeText))
		document.getElementById(gv_elements.inputNodeText).value = node.getText();
	
	if (gf_elementExists(gv_elements.inputNodeTypeStart))
		document.getElementById(gv_elements.inputNodeTypeStart).checked = node.isStart();
		
	if (gf_elementExists(gv_elements.inputNodeOptionsO))
		document.getElementById(gv_elements.inputNodeOptionsO).style.display = "none";
		
	var gt_select_type			= document.getElementById(gv_elements.inputNodeType2);
	
	document.getElementById(gv_elements.inputNodeType2).onclick	= function () {
		if (gf_elementExists(gv_elements.inputNodeOptionsO))
			document.getElementById(gv_elements.inputNodeOptionsO).style.display = document.getElementById(gv_elements.inputNodeType2).value.substr(0, 1) == "$" ? "block" : "none";
	};
	
	$('#' + gv_elements.inputNodeType2).empty();
		
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
	
	gf_guiLoadDropDown(gv_elements.inputNodeMessage, gv_elements.inputNodeSubject, null, false, true);
	if (gt_type.substr(0, 1) == "$")
	{
		if (gf_elementExists(gv_elements.inputNodeOptionsO))
			document.getElementById(gv_elements.inputNodeOptionsO).style.display = "block";
			
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
		document.getElementById(gv_elements.inputSubjectText).value = subject.getText();
		
	if (gf_elementExists(gv_elements.inputSubjectId))
		document.getElementById(gv_elements.inputSubjectId).value = subject.getId();
		
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
}

/**
 * TODO
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
		
		// options for adding new messages
		if (newMessage === true)
		{
			gt_option			= document.createElement("option");
			gt_option.text		= "create a new message";
			gt_option.value		= "##createNewMsg##";
			gt_option.id		= elementMessage + "_00000.2";
			gt_select.add(gt_option);
		}
		
		// options for select all messages
		if (wildcard === true)
		{
			gt_option			= document.createElement("option");
			gt_option.text		= "all messages";
			gt_option.value		= "##all##";
			gt_option.id		= elementMessage + "_00000.all";
			gt_select.add(gt_option);
		}
		
		if (newMessage === true || wildcard === true)
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
			gt_option.text	= gt_messagesArray[gt_mid].text;
			gt_option.value	= gt_messagesArray[gt_mid].id;
			gt_option.id	= elementMessage + "_" + gt_mid;
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
				gt_subjectArray[gt_subjectArray.length]		= gv_graph.subjects[gt_sid].getText() + " (" + gt_sid + ")##;##" + gt_sid;
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
			gt_option.text	= gt_subjArray[0];
			gt_option.value = gt_subjID;
			gt_option.id	= elementSubject + "_" + gt_subjID;
			gt_select.add(gt_option);
		}
	}
}

/**
 * Read the values for the selected edge from the input fields.
 * 
 * @see GCcommunication::updateEdge()
 * @returns {Object} Indizes: text, relatedSubject, type, timeout, optional, messageType
 */
function gf_guiReadEdge ()
{
	var gt_result	= {text: "", relatedSubject: "", timeout: "", type: "", optional: false, messageType: ""};
	
	var gt_text				= gf_elementExists(gv_elements.inputEdgeText) ? document.getElementById(gv_elements.inputEdgeText).value : "";
	var gt_relatedSubject	= gf_elementExists(gv_elements.inputEdgeTarget) ? document.getElementById(gv_elements.inputEdgeTarget).value : "";
	var gt_timeout			= gf_elementExists(gv_elements.inputEdgeTimeout) ? document.getElementById(gv_elements.inputEdgeTimeout).value : "";
	var gt_optional			= gf_elementExists(gv_elements.inputEdgeOptional) && document.getElementById(gv_elements.inputEdgeOptional).checked;
	var gt_messageType		= gf_elementExists(gv_elements.inputEdgeMessage) ? document.getElementById(gv_elements.inputEdgeMessage).value : "";
	
	var gt_type				= "exitcondition";
	
	if (gf_elementExists(gv_elements.inputEdgeTypeTimeout) && document.getElementById(gv_elements.inputEdgeTypeTimeout).checked)
		gt_type	= "timeout";
	
	gt_result.text				= gt_text;
	gt_result.relatedSubject	= gt_relatedSubject;
	gt_result.timeout			= gt_timeout;
	gt_result.type				= gt_type;
	gt_result.optional			= gt_optional;
	gt_result.messageType		= gt_messageType;
	
	return gt_result;
}

/**
 * Read the values for the selected node from the input fields.
 * 
 * @see GCcommunication::updateNode()
 * @returns {Object} Indizes: text, isStart, type2, options
 */
function gf_guiReadNode ()
{
	var gt_result	= {text: "", isStart: false, type2: "", options: {subject: "", message: ""}};
	
	var gt_text		= gf_elementExists(gv_elements.inputNodeText) ? document.getElementById(gv_elements.inputNodeText).value : "";
	var gt_isStart	= gf_elementExists(gv_elements.inputNodeTypeStart) && document.getElementById(gv_elements.inputNodeTypeStart).checked;
	var gt_type2 	= gf_elementExists(gv_elements.inputNodeType2) ? document.getElementById(gv_elements.inputNodeType2).value.toLowerCase() : "";
	var gt_subject 	= gf_elementExists(gv_elements.inputNodeSubject) ? document.getElementById(gv_elements.inputNodeSubject).value : "";
	var gt_message 	= gf_elementExists(gv_elements.inputNodeMessage) ? document.getElementById(gv_elements.inputNodeMessage).value : "";
	
	gt_result.text		= gt_text;
	gt_result.isStart	= gt_isStart;
	gt_result.type2		= gt_type2;
	gt_result.options	= {subject: gt_subject, message: gt_message};
	
	return gt_result;
}

/**
 * Read the values for the selected subject from the input fields.
 * 
 * @see GCcommunication::updateSubject()
 * @returns {Object} Indizes: text, id, type, inputPool, relatedProcess, relatedSubject
 */
function gf_guiReadSubject ()
{
	var gt_result	= {text: "", id: "", type: "", inputPool: "", relatedProcess: "", relatedSubject: ""};
	
	var gt_text			= gf_elementExists(gv_elements.inputSubjectText)		? document.getElementById(gv_elements.inputSubjectText).value		: "";
	var gt_id			= gf_elementExists(gv_elements.inputSubjectId)			? document.getElementById(gv_elements.inputSubjectId).value			: "";
	var gt_inputPool	= gf_elementExists(gv_elements.inputSubjectInputPool)	? document.getElementById(gv_elements.inputSubjectInputPool).value	: "";
	var gt_relProcess	= gf_elementExists(gv_elements.inputSubjectRelProcess)	? document.getElementById(gv_elements.inputSubjectRelProcess).value	: "";
	var gt_relSubject	= gf_elementExists(gv_elements.inputSubjectRelSubject)	? document.getElementById(gv_elements.inputSubjectRelSubject).value	: "";
	
	var gt_type	= "";
	
	if (gf_elementExists(gv_elements.inputSubjectTypeMulti)		&& document.getElementById(gv_elements.inputSubjectTypeMulti).checked		=== true)
		gt_type += "multi";
	
	if (gf_elementExists(gv_elements.inputSubjectTypeExternal)	&& document.getElementById(gv_elements.inputSubjectTypeExternal).checked	=== true)
		gt_type += "external";
		
	if (gt_type == "")
		gt_type = "single";
		
	gt_result.text				= gt_text;
	gt_result.id				= gt_id;
	gt_result.type				= gt_type;
	gt_result.inputPool			= gt_inputPool;
	gt_result.relatedProcess	= gt_relProcess;
	gt_result.relatedSubject	= gt_relSubject;
	
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
