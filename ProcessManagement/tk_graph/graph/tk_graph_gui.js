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
	if (gf_elementExists(gv_elements.inputEdgeMessageO))
		document.getElementById(gv_elements.inputEdgeMessageO).style.display = "none";
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
		document.getElementById(gv_elements.inputEdgeText).value	= edge.getText();
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
	if (gf_elementExists(gv_elements.inputEdgeMessageO))
		document.getElementById(gv_elements.inputEdgeMessageO).style.display = "none";
	if (gf_elementExists(gv_elements.inputEdgeOptionalO))
		document.getElementById(gv_elements.inputEdgeOptionalO).style.display = "none";
		
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
	}
	else
	{
		if (gf_elementExists(gv_elements.inputEdgeTypeCondition))
			document.getElementById(gv_elements.inputEdgeTypeCondition).checked = true;
	}
	
	var gt_select_target		= gf_elementExists(gv_elements.inputEdgeTarget) ? document.getElementById(gv_elements.inputEdgeTarget).options : null;
	var gt_select_message		= gf_elementExists(gv_elements.inputEdgeMessage) ? document.getElementById(gv_elements.inputEdgeMessage).options : null;
	
	if (gt_select_target != null)
		gt_select_target.length		= 0;
		
	if (gt_select_message != null)
		gt_select_message.length	= 0;
	
	// create the drop down menu to select the related subject (only for receive and send nodes)
	if ((startType == "send" || startType == "receive") && gt_select_target != null && gt_select_message != null)
	{		
		
		if (gf_elementExists(gv_elements.inputEdgeTargetO))
			document.getElementById(gv_elements.inputEdgeTargetO).style.display = "block";
		//if (gf_elementExists(gv_elements.inputEdgeMessageO))
		//	document.getElementById(gv_elements.inputEdgeMessageO).style.display	= "block";
			
		document.getElementById(gv_elements.inputEdgeTarget).onchange			= gf_edgeMessage;
		document.getElementById(gv_elements.inputEdgeMessage).onchange			= gf_setEdgeMessage;
		
		var gt_option = document.createElement("option");
		gt_option.text = "please select";
		gt_option.value = "";
		gt_option.id = gv_elements.inputEdgeTarget + "_00000.0";
		gt_select_target.add(gt_option);
		
		var gt_option = document.createElement("option");
		gt_option.text = "----------------------------";
		gt_option.value = "";
		gt_option.id = gv_elements.inputEdgeTarget + "_00000.1";
		gt_select_target.add(gt_option);
		
		// read the subjects that can be related
		var gt_subjectArray = [];
		
		for (var gt_sid in gv_graph.subjects)
		{
			if (gt_sid != gv_graph.selectedSubject)
			{
				gt_subjectArray[gt_subjectArray.length]		= gv_graph.subjects[gt_sid].getText() + " (" + gt_sid + ")##;##" + gt_sid;
			}
		}
		
		// sort the subjects
		gt_subjectArray.sort();
		
		// add the subjects as options to the select field
		for (var gt_sid in gt_subjectArray)
		{						
			var gt_option		= document.createElement("option");
			var gt_subjArray	= gt_subjectArray[gt_sid].split("##;##");
			var gt_subjID		= gt_subjArray[1];
			gt_option.text	= gt_subjArray[0];
			gt_option.value = gt_subjID;
			gt_option.id	= gv_elements.inputEdgeTarget + "_" + gt_subjID;
			gt_select_target.add(gt_option);
			
			if (gt_subjID == edge.getRelatedSubject())
			{
				document.getElementById(gv_elements.inputEdgeTarget + "_" + gt_subjID).selected = true;
			}
		}
		
		
		/*
		 * read available messages
		 */
		gv_graph.loadEdgeMessages();
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
	if (gf_elementExists(gv_elements.inputNodeText))
		document.getElementById(gv_elements.inputNodeText).value = node.getText();
	
	if (gf_elementExists(gv_elements.inputNodeTypeStart))
		document.getElementById(gv_elements.inputNodeTypeStart).checked = node.isStart();
		
		
	var gt_select_type			= document.getElementById(gv_elements.inputNodeType2);
	
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
}

/**
 * Loads all messages that are sent from the selected subject to the current subject or those sent from the current subject to the selected subject.
 * 
 * @see GCcommunication::loadEdgeMessages()
 * @returns {void}
 */
function gf_guiLoadEdgeMessages ()
{
	// if either the element gv_elements.inputEdgeTarget or the element gv_elements.inputEdgeMessage does not exist, cancel the method call
	if (!gf_elementExists(gv_elements.inputEdgeTarget, gv_elements.inputEdgeMessage))
		return false;
		
	var gt_selectedTarget	= document.getElementById(gv_elements.inputEdgeTarget).value;
	var gt_select_message	= document.getElementById(gv_elements.inputEdgeMessage).options;
	var gt_messagesArray	= [];
	
	gt_select_message.length	= 0;
	
	if (gt_selectedTarget != "" && gt_selectedTarget != gv_graph.selectedSubject)
	{
		
		if (gf_elementExists(gv_elements.inputEdgeMessageO))
			document.getElementById(gv_elements.inputEdgeMessageO).style.display	= "block";
		
		// create some entries to guide the user
		var gt_option = document.createElement("option");
			gt_option.text = "please select";
			gt_option.value = "";
			gt_option.id = gv_elements.inputEdgeTarget + "_00000.0";
			gt_select_message.add(gt_option);
		
			gt_option = document.createElement("option");
			gt_option.text = "----------------------------";
			gt_option.value = "";
			gt_option.id = gv_elements.inputEdgeTarget + "_00000.1";
			gt_select_message.add(gt_option);
		
			gt_option = document.createElement("option");
			gt_option.text = "create a new message";
			gt_option.value = "##createNewMsg##";
			gt_option.id = gv_elements.inputEdgeTarget + "_00000.2";
			gt_select_message.add(gt_option);
		
			gt_option = document.createElement("option");
			gt_option.text = "----------------------------";
			gt_option.value = "";
			gt_option.id = gv_elements.inputEdgeTarget + "_00000.3";
			gt_select_message.add(gt_option);
			
		var gt_curEdge			= null;
		var gt_curEdgeID		= null;
		var gt_curStartNode		= null;
		var gt_curStartNodeType	= "receive";
		var gt_tmpStartNodeType	= "send";
		
		// determine the type of the start node of the current edge
		if (gv_graph.getBehavior(gv_graph.selectedSubject) != null)
		{
			gt_curEdgeID	= gv_graph.getBehavior(gv_graph.selectedSubject).selectedEdge;
			if (gt_curEdgeID != null)
			{
				gt_curEdge	= gv_graph.getBehavior(gv_graph.selectedSubject).getEdges()["e" + gt_curEdgeID];
				if (gt_curEdge != null)
				{
					gt_curStartNode	= gv_graph.getBehavior(gv_graph.selectedSubject).getNode(gt_curEdge.getStart());
					if (gt_curStartNode != null)
					{
						gt_curStartNodeType	= gt_curStartNode.getType();						
					}
				}
			}
		}
		
		// when the current edge's start node is a receive node, the messages sent by the other object have to be loaded; when it is a send node, the messages received by the other subject have to be loaded  
		gt_tmpStartNodeType	= gt_curStartNodeType == "send" ? "receive" : "send";
		
		// load the messages sent / received from subject gt_selectedTarget depending on the type of the startNode of the currently selected edge
		var gt_behav = gv_graph.getBehavior(gt_selectedTarget);
		var gt_edges = gt_behav.getEdges();
		for (var gt_eid in gt_edges)
		{
			var gt_edge					= gt_edges[gt_eid];
			var gt_startNode			= gt_behav.getNode(gt_edge.getStart());
			var gt_endNode				= gt_behav.getNode(gt_edge.getEnd());
			var gt_relatedSubject		= gt_edge.getRelatedSubject();
			var gt_text					= gt_edge.getText();
			
			if (gt_startNode != null && gt_endNode != null && gt_relatedSubject != null && gt_text != "")
			{
				if (gf_isset(gv_graph.subjects[gt_relatedSubject]) && gt_relatedSubject == gv_graph.selectedSubject && gt_startNode.getType() == gt_tmpStartNodeType)
				{
					gt_messagesArray[gt_messagesArray.length]	= gt_text;
				}
			}
		}
		
		// sort the messages alphabetically
		gt_messagesArray.sort();
		
		// add the messages to the select field
		for (var gt_mid in gt_messagesArray)
		{
			gt_option = document.createElement("option");
			gt_option.text = gt_messagesArray[gt_mid];
			gt_option.value = gt_messagesArray[gt_mid];
			gt_option.id = gv_elements.inputEdgeMessage + "_" + gt_mid;
			gt_select_message.add(gt_option);
			
			if (gf_elementExists(gv_elements.inputEdgeText) && gt_messagesArray[gt_mid].replace("\\n", "") == document.getElementById(gv_elements.inputEdgeText).value.replace("\\n", ""))
			{
				document.getElementById(gv_elements.inputEdgeMessage + "_" + gt_mid).selected = true;
			}
		}
	}
}

/**
 * Read the values for the selected edge from the input fields.
 * 
 * @see GCcommunication::updateEdge()
 * @returns {Object} Indizes: text, relatedSubject, type, timeout, optional
 */
function gf_guiReadEdge ()
{
	var gt_result	= {text: "", relatedSubject: "", timeout: "", type: "", optional: false};
	
	var gt_text				= gf_elementExists(gv_elements.inputEdgeText) ? document.getElementById(gv_elements.inputEdgeText).value : "";
	var gt_relatedSubject	= gf_elementExists(gv_elements.inputEdgeTarget) ? document.getElementById(gv_elements.inputEdgeTarget).value : "";
	var gt_timeout			= gf_elementExists(gv_elements.inputEdgeTimeout) ? document.getElementById(gv_elements.inputEdgeTimeout).value : "";
	var gt_optional			= gf_elementExists(gv_elements.inputEdgeOptional) && document.getElementById(gv_elements.inputEdgeOptional).checked;
	
	var gt_type				= "exitcondition";
	
	if (gf_elementExists(gv_elements.inputEdgeTypeTimeout) && document.getElementById(gv_elements.inputEdgeTypeTimeout).checked)
		gt_type	= "timeout";
	
	gt_result.text				= gt_text;
	gt_result.relatedSubject	= gt_relatedSubject;
	gt_result.timeout			= gt_timeout;
	gt_result.type				= gt_type;
	gt_result.optional			= gt_optional;
	
	return gt_result;
}

/**
 * Read the values for the selected node from the input fields.
 * 
 * @see GCcommunication::updateNode()
 * @returns {Object} Indizes: text, isStart, type2
 */
function gf_guiReadNode ()
{
	var gt_result	= {text: "", isStart: false, type2: ""};
	
	var gt_text		= gf_elementExists(gv_elements.inputNodeText) ? document.getElementById(gv_elements.inputNodeText).value : "";
	var gt_isStart	= gf_elementExists(gv_elements.inputNodeTypeStart) && document.getElementById(gv_elements.inputNodeTypeStart).checked;
	var gt_type2 	= gf_elementExists(gv_elements.inputNodeType2) ? document.getElementById(gv_elements.inputNodeType2).value.toLowerCase() : "";
	
	gt_result.text		= gt_text;
	gt_result.isStart	= gt_isStart;
	gt_result.type2		= gt_type2;
	
	return gt_result;
}

/**
 * Read the values for the selected subject from the input fields.
 * 
 * @see GCcommunication::updateSubject()
 * @returns {Object} Indizes: text, id, type
 */
function gf_guiReadSubject ()
{
	var gt_result	= {text: "", id: "", type: "", inputPool: ""};
	
	var gt_text			= gf_elementExists(gv_elements.inputSubjectText)		? document.getElementById(gv_elements.inputSubjectText).value		: "";
	var gt_id			= gf_elementExists(gv_elements.inputSubjectId)			? document.getElementById(gv_elements.inputSubjectId).value			: "";
	var gt_inputPool	= gf_elementExists(gv_elements.inputSubjectInputPool)	? document.getElementById(gv_elements.inputSubjectInputPool).value	: "";
	
	var gt_type	= "";
	
	if (gf_elementExists(gv_elements.inputSubjectTypeMulti)		&& document.getElementById(gv_elements.inputSubjectTypeMulti).checked		=== true)
		gt_type += "multi";
	
	if (gf_elementExists(gv_elements.inputSubjectTypeExternal)	&& document.getElementById(gv_elements.inputSubjectTypeExternal).checked	=== true)
		gt_type += "external";
		
	if (gt_type == "")
		gt_type = "single";
		
	gt_result.text		= gt_text;
	gt_result.id		= gt_id;
	gt_result.type		= gt_type;
	gt_result.inputPool	= gt_inputPool;
	
	return gt_result;
}

/**
 * This is called onChange of gv_elements.inputEdgeMessage.
 * It updates the value of gv_elements.inputEdgeText with the selected message so the edge can be updated correctly.
 * 
 * @see tk_graph_api.js :: gf_setEdgeMessage
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
		else
		{
			document.getElementById(gv_elements.inputEdgeText).readOnly	= true;
			document.getElementById(gv_elements.inputEdgeText).value 	= gt_message;
		}
	}
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
		
	if (gf_elementExists(gv_elements.inputEdgeMessageO))
		document.getElementById(gv_elements.inputEdgeMessageO).style.display = "none";
}
