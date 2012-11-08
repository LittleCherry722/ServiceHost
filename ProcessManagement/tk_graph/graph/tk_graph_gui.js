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

// subscribe to changes of macro list
$.subscribe("/tk_graph/macros", function (args)
{
	if (!gf_isStandAlone() && gf_functionExists(gv_functions.behavior.updateListOfMacros))
	{
		window[gv_functions.behavior.updateListOfMacros]();
	}
}
);

/**
 * Disables an element (readOnly=true | disabled=true).
 * 
 * @param {String} element The name of the DOM element to disable.
 * @param {String} attribute The attribute of the element to use for disabling the element ("readonly" or "disabled");
 * @param {bool} status (optional) When set to true, the element will be disabled. When set to false, the gf_guiElementEnable method will be called.
 * @returns {void}
 */
function gf_guiElementDisable (element, attribute, status)
{
	if (gf_isset(element, attribute) && gf_elementExists(element))
	{
		if (gf_isset(status) && status == false)
		{
			gf_guiElementEnable(element, attribute);
		}
		else
		{
			if (attribute == "readonly")
			{
				document.getElementById(element).readOnly = true;
			}
			else
			{
				document.getElementById(element).disabled = true;
			}
		}
	}
}

/**
 * Enables an element (readOnly=false | disabled=false).
 * 
 * @param {String} element The name of the DOM element to enable.
 * @param {String} attribute The attribute of the element to use for enabling the element ("readonly" or "disabled");
 * @param {bool} status (optional) When set to true, the element will be enabled. When set to false, the gf_guiElementDisable method will be called.
 * @returns {void}
 */
function gf_guiElementEnable (element, attribute, status)
{
	if (gf_isset(element, attribute) && gf_elementExists(element))
	{
		if (gf_isset(status) && status == false)
		{
			gf_guiElementDisable(element, attribute);
		}
		else
		{
			if (attribute == "readonly")
			{
				document.getElementById(element).readOnly = false;
			}
			else
			{
				document.getElementById(element).disabled = false;
			}
		}
	}
}

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
 * @param {String} type The type of the value to set (bool [checked], string [value] or html [innerHTML]).
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
		else if (type == "html")
		{
			document.getElementById(element).innerHTML = value;
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
			gf_guiElementHide(gv_elements.graphBVouter);
			gf_guiElementShow(gv_elements.graphCVouter);
			
			$('#' + gv_elements.graphCVouter).scrollTo( {left: '0px', top: '0px'});
			$('#' + gv_elements.graphCVouter).scrollTo( {left: '0px', top: '50%'});
		}
		else
		{			
			gf_guiElementHide(gv_elements.graphCVouter);
			gf_guiElementShow(gv_elements.graphBVouter);
			
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
	// hide elements
	gf_guiElementHide(gv_elements.inputEdgeCorrelationIdO);
	gf_guiElementHide(gv_elements.inputEdgeOptionalO);
	gf_guiElementHide(gv_elements.inputEdgeOuter);
	gf_guiElementHide(gv_elements.inputEdgePriorityO);
	gf_guiElementHide(gv_elements.inputEdgeStoreOuter);
	gf_guiElementHide(gv_elements.inputEdgeStoreVariableNO);
	gf_guiElementHide(gv_elements.inputEdgeTargetO);
	gf_guiElementHide(gv_elements.inputEdgeTargetMMMO);
	gf_guiElementHide(gv_elements.inputEdgeTargetMOuter);
	gf_guiElementHide(gv_elements.inputEdgeTypeCondO);
	gf_guiElementHide(gv_elements.inputEdgeTypeExceptO);
	gf_guiElementHide(gv_elements.inputEdgeTypeTimeoutO);
	gf_guiElementHide(gv_elements.inputNodeChannelNewOuter);
	gf_guiElementHide(gv_elements.inputNodeChannelOuter);
	gf_guiElementHide(gv_elements.inputNodeMajorStartOuter);
	gf_guiElementHide(gv_elements.inputNodeOptionsOuter);
	gf_guiElementHide(gv_elements.inputNodeOptChannelOuter);
	gf_guiElementHide(gv_elements.inputNodeOptCorrelationIdO);
	gf_guiElementHide(gv_elements.inputNodeOptMessageO);
	gf_guiElementHide(gv_elements.inputNodeOptStateOuter);
	gf_guiElementHide(gv_elements.inputNodeOptSubjectO);
	gf_guiElementHide(gv_elements.inputNodeOuter);
	gf_guiElementHide(gv_elements.inputNodeStartOuter);
	gf_guiElementHide(gv_elements.inputNodeVariableO);
	gf_guiElementHide(gv_elements.inputSubjectRelOuter);
	gf_guiElementHide(gv_elements.inputEdgeTargetMTypeNO);
	gf_guiElementHide(gv_elements.inputEdgeTargetMTypeVO);
	gf_guiElementHide(gv_elements.inputEdgeTargetMVarTextO);
	gf_guiElementHide(gv_elements.inputEdgeTypeBooleanOuter);
	gf_guiElementHide(gv_elements.inputEdgeTypeNormalOuter);
	gf_guiElementHide(gv_elements.inputNodeVarManOuter);
	gf_guiElementHide(gv_elements.inputNodeVarManVarStoreNO);
	gf_guiElementHide(gv_elements.inputNodeMacroOuter);
	gf_guiElementHide(gv_elements.inputNodeMacroNewOuter);
	
	// clear element values
	gf_guiElementWrite(gv_elements.guiChannelSelect, "string", "");
	gf_guiElementWrite(gv_elements.inputEdgeComment, "string", "");
	gf_guiElementWrite(gv_elements.inputEdgeCorrelationId, "string", "");
	gf_guiElementWrite(gv_elements.inputEdgeExceptionText, "string", "");
	gf_guiElementWrite(gv_elements.inputEdgeMessage, "string", "");
	gf_guiElementWrite(gv_elements.inputEdgePriority, "string", "");
	gf_guiElementWrite(gv_elements.inputEdgeStoreVariable, "string", "");
	gf_guiElementWrite(gv_elements.inputEdgeStoreVariableN, "string", "");
	gf_guiElementWrite(gv_elements.inputEdgeTarget, "string", "");
	gf_guiElementWrite(gv_elements.inputEdgeTargetMMin, "string", "");
	gf_guiElementWrite(gv_elements.inputEdgeTargetMMax, "string", "");
	gf_guiElementWrite(gv_elements.inputEdgeText, "string", "");
	gf_guiElementWrite(gv_elements.inputEdgeTimeout, "string", "");
	gf_guiElementWrite(gv_elements.inputNodeChannel, "string", "");
	gf_guiElementWrite(gv_elements.inputNodeChannelNew, "string", "");
	gf_guiElementWrite(gv_elements.inputNodeComment, "string", "");
	gf_guiElementWrite(gv_elements.inputNodeOptChannel, "string", "");
	gf_guiElementWrite(gv_elements.inputNodeOptCorrelationId, "string", "");
	gf_guiElementWrite(gv_elements.inputNodeOptMessage, "string", "");
	gf_guiElementWrite(gv_elements.inputNodeOptState, "string", "");
	gf_guiElementWrite(gv_elements.inputNodeOptSubject, "string", "");
	gf_guiElementWrite(gv_elements.inputNodeText, "string", "");
	gf_guiElementWrite(gv_elements.inputNodeType, "string", "");
	gf_guiElementWrite(gv_elements.inputNodeVariable, "string", "");
	gf_guiElementWrite(gv_elements.inputSubjectComment, "string", "");
	gf_guiElementWrite(gv_elements.inputSubjectInputPool, "string", "");
	gf_guiElementWrite(gv_elements.inputSubjectRelProcess, "string", "");
	gf_guiElementWrite(gv_elements.inputSubjectRelSubject, "string", "");
	gf_guiElementWrite(gv_elements.inputSubjectRole, "string", "");
	gf_guiElementWrite(gv_elements.inputSubjectText, "string", "");
	gf_guiElementWrite(gv_elements.inputEdgeTargetMTypeN, "string", "");
	gf_guiElementWrite(gv_elements.inputEdgeTargetMVariable, "string", "");
	gf_guiElementWrite(gv_elements.inputEdgeTargetMVarText, "string", "");
	gf_guiElementWrite(gv_elements.inputNodeVarManOperation, "string", "");
	gf_guiElementWrite(gv_elements.inputNodeVarManVar1, "string", "");
	gf_guiElementWrite(gv_elements.inputNodeVarManVar2, "string", "");
	gf_guiElementWrite(gv_elements.inputNodeVarManVarStore, "string", "");
	gf_guiElementWrite(gv_elements.inputNodeVarManVarStoreN, "string", "");
	gf_guiElementWrite(gv_elements.inputNodeMacro, "string", "");
	gf_guiElementWrite(gv_elements.inputNodeMacroNew, "string", "");
	
	// uncheck elements
	gf_guiElementWrite(gv_elements.inputEdgeOptional, "bool", false);
	gf_guiElementWrite(gv_elements.inputEdgeTimeoutManual, "bool", false);
	gf_guiElementWrite(gv_elements.inputNodeMajorStart, "bool", false);
	gf_guiElementWrite(gv_elements.inputNodeStart, "bool", false);
	gf_guiElementWrite(gv_elements.inputSubjectTypeMulti, "bool", false);
	gf_guiElementWrite(gv_elements.inputSubjectTypeExternal, "bool", false);
	gf_guiElementWrite(gv_elements.inputSubjectExtExternal, "bool", false);
	gf_guiElementWrite(gv_elements.inputSubjectExtInterface, "bool", false);
	gf_guiElementWrite(gv_elements.inputSubjectExtInstantInterface, "bool", false);
	gf_guiElementWrite(gv_elements.inputEdgeTargetMTypeA, "bool", false);
	gf_guiElementWrite(gv_elements.inputEdgeTargetMTypeL, "bool", false);
	gf_guiElementWrite(gv_elements.inputEdgeTargetMTypeV, "bool", false);
	gf_guiElementWrite(gv_elements.inputEdgeTypeBooleanFalse, "bool", false);
	gf_guiElementWrite(gv_elements.inputEdgeTypeBooleanTrue, "bool", false);
	gf_guiElementWrite(gv_elements.inputEdgeTypeCondition, "bool", false);
	gf_guiElementWrite(gv_elements.inputEdgeTypeException, "bool", false);
	gf_guiElementWrite(gv_elements.inputEdgeTypeTimeout, "bool", false);
	
	// enable elements
	gf_guiElementEnable(gv_elements.inputEdgeText, "readonly");
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
	if (!gf_isset(startType))
		startType	= edge.getTypeOfStartNode();
	
	// clear input fields
	gf_guiClearInputFields();
	
	// show menu
	gf_guiElementShow(gv_elements.inputEdgeOuter);
	
	// exit condition
	gf_guiElementWrite(gv_elements.inputEdgeText, "string", gf_replaceNewline(edge.getMessageTypeId() == "" ? edge.getText() : edge.getMessageType()));
	
	// timeout
	gf_guiElementWrite(gv_elements.inputEdgeTimeout, "string", edge.getTimer("timestamp") > 0 ? edge.getTimer() : "");
	gf_guiElementWrite(gv_elements.inputEdgeTimeoutEx, "html", "(example: " + edge.getTimer("example") + ")");
	gf_guiElementWrite(gv_elements.inputEdgeTimeoutManual, "bool", edge.isManualTimeout());
	
	// priority
	gf_guiElementWrite(gv_elements.inputEdgePriority, "string", edge.getPriority());
	
	// comment
	gf_guiElementWrite(gv_elements.inputEdgeComment, "string", edge.getComment());
	
	// optional edges
	if (startType == "modalsplit")
	{
		gf_guiElementShow(gv_elements.inputEdgeOptionalO);
		gf_guiElementWrite(gv_elements.inputEdgeOptional, "bool", edge.isOptional());
	}
	
	// show edge types startNode dependent
	if (startType == "$isipempty")
		gf_guiElementShow(gv_elements.inputEdgeTypeBooleanOuter);
	else
		gf_guiElementShow(gv_elements.inputEdgeTypeNormalOuter);
		
	// hide options when binary edge types, empty exit condition (merge node)
	if (startType != "$isipempty" && startType != "merge")
	{
		if (edge.getType() == "timeout")
			gf_guiElementShow(gv_elements.inputEdgeTypeTimeoutO);
		else if (edge.getType() == "errorcondition")
			gf_guiElementShow(gv_elements.inputEdgeTypeExceptO);
		else
			gf_guiElementShow(gv_elements.inputEdgeTypeCondO);
	}
	
	// select type
	gf_guiElementWrite(gv_elements.inputEdgeTypeBooleanFalse, "bool", edge.getType() == "boolfalse");
	gf_guiElementWrite(gv_elements.inputEdgeTypeBooleanTrue, "bool", edge.getType() == "booltrue");
	gf_guiElementWrite(gv_elements.inputEdgeTypeException, "bool", edge.getType() == "errorcondition");
	gf_guiElementWrite(gv_elements.inputEdgeTypeCondition, "bool", edge.getType() == "exitcondition");
	gf_guiElementWrite(gv_elements.inputEdgeTypeTimeout, "bool", edge.getType() == "timeout");
	
	// disable types
	gf_guiElementEnable(gv_elements.inputEdgeTypeBooleanFalse, "disabled", gf_checkCardinality(edge.parentMacro, edge.getStart(), edge.getEnd(), "boolfalse", edge.getType(), "update").allowed);
	gf_guiElementEnable(gv_elements.inputEdgeTypeBooleanTrue, "disabled", gf_checkCardinality(edge.parentMacro, edge.getStart(), edge.getEnd(), "booltrue", edge.getType(), "update").allowed);
	gf_guiElementEnable(gv_elements.inputEdgeTypeCondition, "disabled", gf_checkCardinality(edge.parentMacro, edge.getStart(), edge.getEnd(), "exitcondition", edge.getType(), "update").allowed);
	gf_guiElementEnable(gv_elements.inputEdgeTypeException, "disabled", gf_checkCardinality(edge.parentMacro, edge.getStart(), edge.getEnd(), "errorcondition", edge.getType(), "update").allowed);
	gf_guiElementEnable(gv_elements.inputEdgeTypeTimeout, "disabled", gf_checkCardinality(edge.parentMacro, edge.getStart(), edge.getEnd(), "timeout", edge.getType(), "update").allowed);
	
	// add events for edge types
	gf_guiEdgeTypeAddOnClick(gv_elements.inputEdgeTypeCondition, gv_elements.inputEdgeTypeCondO);
	gf_guiEdgeTypeAddOnClick(gv_elements.inputEdgeTypeException, gv_elements.inputEdgeTypeExceptO);
	gf_guiEdgeTypeAddOnClick(gv_elements.inputEdgeTypeTimeout, gv_elements.inputEdgeTypeTimeoutO);
	
	// handle fields that are only available for startNodes == send | receive | action
	if (startType == "send" || startType == "receive" || startType == "action")
	{
		// add event for variable select
		if (gf_elementExists(gv_elements.inputEdgeStoreVariable))
		{
			document.getElementById(gv_elements.inputEdgeStoreVariable).onchange = function ()
			{
				if (gf_guiElementRead(gv_elements.inputEdgeStoreVariable, "string") == "##createNew##")
					gf_guiElementShow(gv_elements.inputEdgeStoreVariableNO);
				else
					gf_guiElementHide(gv_elements.inputEdgeStoreVariableNO);
			};
		}
		
		// load variables
		gf_guiLoadDropDownVariables(edge.parentBehavior, gv_elements.inputEdgeStoreVariable, true, false);
		
		gf_guiElementShow(gv_elements.inputEdgeStoreOuter);
		gf_guiElementWrite(gv_elements.inputEdgeStoreVariable, "string", edge.getVariable(), "");
		
		
		// fields that are only available for startNode == send | receive
		if (startType == "send" || startType == "receive")
		{
			
			var gt_isAll		= edge.getRelatedSubject("min") == "-1" && edge.getRelatedSubject("max") == "-1";
			var gt_createNew	= edge.getRelatedSubject("createNew");
			var gt_isVariable	= edge.getRelatedSubject("variable") != null && edge.getRelatedSubject("variable") != "";
			
			// load drop down fields
			gf_guiLoadDropDownCorrelationIds(edge.parentBehavior, gv_elements.inputEdgeCorrelationId, true, false);
			gf_guiLoadDropDownMessageTypes(gv_elements.inputEdgeMessage, true, false);
			gf_guiLoadDropDownSubjects(gv_elements.inputEdgeTarget, gv_graph.selectedSubject, false);
			gf_guiLoadDropDownVariables(edge.parentBehavior, gv_elements.inputEdgeTargetMVariable, false, false);
		
			// show elements
			gf_guiElementShow(gv_elements.inputEdgeCorrelationIdO);
			gf_guiElementShow(gv_elements.inputEdgeTargetO);
			
			if (edge.getRelatedSubject("multi"))
				gf_guiElementShow(gv_elements.inputEdgeTargetMOuter);
				
			if (!gt_isAll && !gt_isVariable)
				gf_guiElementShow(gv_elements.inputEdgeTargetMMMO);
				
			if (gt_isVariable)
				gf_guiElementShow(gv_elements.inputEdgeTargetMTypeVO);
			
			// set values
			gf_guiElementWrite(gv_elements.inputEdgeCorrelationId, "string", edge.getCorrelationId(), "");
			gf_guiElementWrite(gv_elements.inputEdgeTarget, "string", edge.getRelatedSubject(), "");
			gf_guiElementWrite(gv_elements.inputEdgeMessage, "string", edge.getText());
			gf_guiElementWrite(gv_elements.inputEdgeTargetMMin, "string", edge.getRelatedSubject("min"));
			gf_guiElementWrite(gv_elements.inputEdgeTargetMMax, "string", edge.getRelatedSubject("max"));
			gf_guiElementWrite(gv_elements.inputEdgeTargetMVariable, "string", edge.getRelatedSubject("variable"), "");
			
			// set boolean values
			gf_guiElementWrite(gv_elements.inputEdgeTargetMTypeA, "bool", gt_isAll && !gt_isVariable);
			gf_guiElementWrite(gv_elements.inputEdgeTargetMTypeL, "bool", !gt_isAll && !gt_createNew && !gt_isVariable);
			gf_guiElementWrite(gv_elements.inputEdgeTargetMTypeN, "bool", !gt_isAll && gt_createNew && !gt_isVariable);
			gf_guiElementWrite(gv_elements.inputEdgeTargetMTypeV, "bool", gt_isVariable);
			
		
			// add event for relatedSubject select
			if (gf_elementExists(gv_elements.inputEdgeTarget))
			{
				document.getElementById(gv_elements.inputEdgeTarget).onchange = function ()
				{
					var gt_relatedSubjectID		= gf_guiElementRead(gv_elements.inputEdgeTarget, "string");
					
					if (gf_isset(gv_graph.subjects[gt_relatedSubjectID]) && gv_graph.subjects[gt_relatedSubjectID].isMulti())
						gf_guiElementShow(gv_elements.inputEdgeTargetMOuter);
					else
						gf_guiElementHide(gv_elements.inputEdgeTargetMOuter);
				};
			}
			
			// add onchange event for messageType DropDown
			if (gf_elementExists(gv_elements.inputEdgeMessage))
				document.getElementById(gv_elements.inputEdgeMessage).onchange			= gf_guiSetEdgeMessage;
				
			// add onChange event for variable drop down
			if (gf_elementExists(gv_elements.inputEdgeTargetMVariable))
			{
				document.getElementById(gv_elements.inputEdgeTargetMVariable).onchange = function ()
				{
					if (gf_guiElementRead(gv_elements.inputEdgeTargetMVariable, "string") == "##createNew##")
						gf_guiElementShow(gv_elements.inputEdgeTargetMVarTextO);
					else
						gf_guiElementHide(gv_elements.inputEdgeTargetMVarTextO);
				};
			}
			
			// add onCLick event for "all known subjects" option to hide additional options
			if (gf_elementExists(gv_elements.inputEdgeTargetMTypeA))
			{
				document.getElementById(gv_elements.inputEdgeTargetMTypeA).onclick	= function ()
				{
					gf_guiElementHide(gv_elements.inputEdgeTargetMMMO);
					gf_guiElementHide(gv_elements.inputEdgeTargetMTypeVO);
				};
			}
			
			// add onClick event for "limited # of subjects" option to show additional options
			if (gf_elementExists(gv_elements.inputEdgeTargetMTypeL))
			{
				document.getElementById(gv_elements.inputEdgeTargetMTypeL).onclick	= function ()
				{
					gf_guiElementShow(gv_elements.inputEdgeTargetMMMO);
					gf_guiElementHide(gv_elements.inputEdgeTargetMTypeVO);
				};
			}
			
			// add onClick event for "create new subjects" option to show additional options
			if (gf_elementExists(gv_elements.inputEdgeTargetMTypeN))
			{
				document.getElementById(gv_elements.inputEdgeTargetMTypeN).onclick	= function ()
				{
					gf_guiElementShow(gv_elements.inputEdgeTargetMMMO);
					gf_guiElementHide(gv_elements.inputEdgeTargetMTypeVO);
				};
			}
			
			// add onClick event for "load from variable" option to show additional options
			if (gf_elementExists(gv_elements.inputEdgeTargetMTypeV))
			{
				document.getElementById(gv_elements.inputEdgeTargetMTypeV).onclick	= function ()
				{
					gf_guiElementShow(gv_elements.inputEdgeTargetMTypeVO);
					gf_guiElementHide(gv_elements.inputEdgeTargetMMMO);
				};
			}
		}
		
		// fields that are only available for startNode == send
		if (startType == "send")
		{
			gf_guiElementShow(gv_elements.inputEdgeTargetMTypeNO);
		}
		
		// fields that are only available for startNode == receive
		if (startType == "receive")
		{
			gf_guiElementShow(gv_elements.inputEdgePriorityO);
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
	var gt_type	= node.isEnd() ? "end" : node.getType();
	
	// clear input fields
	gf_guiClearInputFields();
	
	// do not update elements when node is startNode of macro
	if (node.parentMacro.id != "##main##" && node.id == 0)
		return false;
	
	// show menu
	gf_guiElementShow(gv_elements.inputNodeOuter);
	
	// load dropdowns
	gf_guiLoadDropDownChannels(gv_elements.inputNodeChannel, true, false);
	gf_guiLoadDropDownForNode(node.parentBehavior, gt_type);
	gf_guiLoadDropDownNodeTypes(gv_elements.inputNodeType);
		
	// show divs
	gf_guiElementShow(gv_elements.inputNodeChannelOuter);
	
	if (node.parentMacro.id == "##main##")
		gf_guiElementShow(gv_elements.inputNodeStartOuter);
	
	if (node.isStart())
		gf_guiElementShow(gv_elements.inputNodeMajorStartOuter);
		
	// set values
	gf_guiElementWrite(gv_elements.inputNodeType, "string", gt_type);
	gf_guiElementWrite(gv_elements.inputNodeText, "string", gf_replaceNewline(node.getText()));
	gf_guiElementWrite(gv_elements.inputNodeChannel, "string", node.getChannel(), "");
	gf_guiElementWrite(gv_elements.inputNodeStart, "bool", node.isStart());
	gf_guiElementWrite(gv_elements.inputNodeMajorStart, "bool", node.isStart() && node.isMajorStartNode());
	
	// comment
	gf_guiElementWrite(gv_elements.inputNodeComment, "string", node.getComment());
	
	// add onChange listener to type selection
	if (gf_elementExists(gv_elements.inputNodeType))
	{
		document.getElementById(gv_elements.inputNodeType).onclick	= function ()
		{
			gf_guiLoadDropDownForNode(node.parentBehavior, document.getElementById(gv_elements.inputNodeType).value);
		};
	}
		
	// add onClick event to start node checkbox
	if (gf_elementExists(gv_elements.inputNodeStart))
	{
		document.getElementById(gv_elements.inputNodeStart).onclick	= function ()
		{
			if (gf_guiElementRead(gv_elements.inputNodeStart), "bool")
				gf_guiElementShow(gv_elements.inputNodeMajorStartOuter);
			else
				gf_guiElementHide(gv_elements.inputNodeMajorStartOuter);
		};
	}
	
	// add onChange listener to channel selection
	if (gf_elementExists(gv_elements.inputNodeChannel))
	{
		document.getElementById(gv_elements.inputNodeChannel).onchange	= function ()
		{
			if (gf_guiElementRead(gv_elements.inputNodeChannel, "string") == "##createNew##")
				gf_guiElementShow(gv_elements.inputNodeChannelNewOuter);
			else
				gf_guiElementHide(gv_elements.inputNodeChannelNewOuter);
		};
	}
	
	// add onChange listener to varStore Drop Down
	if (gf_elementExists(gv_elements.inputNodeVarManVarStore))
	{
		document.getElementById(gv_elements.inputNodeVarManVarStore).onchange	= function ()
		{
			if (gf_guiElementRead(gv_elements.inputNodeVarManVarStore, "string") == "##createNew##")
				gf_guiElementShow(gv_elements.inputNodeVarManVarStoreNO);
			else
				gf_guiElementHide(gv_elements.inputNodeVarManVarStoreNO);
		};
	}
	
	// add onChange listener to macro Drop Down
	if (gf_elementExists(gv_elements.inputNodeMacro))
	{
		document.getElementById(gv_elements.inputNodeMacro).onchange	= function ()
		{
			if (gf_guiElementRead(gv_elements.inputNodeMacro, "string") == "##createNew##")
				gf_guiElementShow(gv_elements.inputNodeMacroNewOuter);
			else
				gf_guiElementHide(gv_elements.inputNodeMacroNewOuter);
		};
	}
	
	// additional settings for internal actions, macros and predefined actions
	if (gt_type.substr(0, 1) == "$" || gt_type == "action" || gt_type == "macro")
	{
			
		var gt_options	= node.getOptions();
		
		// set values for optional settings
		if (gf_isset(gt_options.subject))
			gf_guiElementWrite(gv_elements.inputNodeOptSubject, "string", gt_options.subject, "");
		
		if (gf_isset(gt_options.message))
			gf_guiElementWrite(gv_elements.inputNodeOptMessage, "string", gt_options.message, "");
		
		if (gf_isset(gt_options.state))
			gf_guiElementWrite(gv_elements.inputNodeOptState, "string", gt_options.state, "");
		
		if (gf_isset(gt_options.channel))
			gf_guiElementWrite(gv_elements.inputNodeOptChannel, "string", gt_options.channel, "");
		
		if (gf_isset(gt_options.correlationId))
			gf_guiElementWrite(gv_elements.inputNodeOptCorrelationId, "string", gt_options.correlationId, "");
		
		// set settings for variables
		gf_guiElementWrite(gv_elements.inputNodeVariable, "string", node.getVariable(), "");
		gf_guiElementWrite(gv_elements.inputNodeVarManVar1, "string", node.getVarMan("var1"), "");
		gf_guiElementWrite(gv_elements.inputNodeVarManVar2, "string", node.getVarMan("var2"), "");
		gf_guiElementWrite(gv_elements.inputNodeVarManVarStore, "string", node.getVarMan("storevar"), "");
		gf_guiElementWrite(gv_elements.inputNodeVarManOperation, "string", node.getVarMan("operation"), "");
		
		// macro
		gf_guiElementWrite(gv_elements.inputNodeMacro, "string", node.getMacro());
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
	// clear input fields
	gf_guiClearInputFields();
	
	// show elements
	if (subject.isExternal())
		gf_guiElementShow(gv_elements.inputSubjectRelOuter);
	else
		gf_guiElementHide(gv_elements.inputSubjectRelOuter);
	
	// set values
	gf_guiElementWrite(gv_elements.inputSubjectText, "string", gf_replaceNewline(subject.getText()));
	gf_guiElementWrite(gv_elements.inputSubjectRole, "string", subject.getRole(), "");
	gf_guiElementWrite(gv_elements.inputSubjectInputPool, "string", subject.getInputPool(), "-1");
	gf_guiElementWrite(gv_elements.inputSubjectRelProcess, "string", subject.getRelatedProcess(), "");
	gf_guiElementWrite(gv_elements.inputSubjectRelSubject, "string", subject.getRelatedSubject(), "");
	gf_guiElementWrite(gv_elements.inputSubjectComment, "string", subject.getComment());
	
	// check checbkoxes / radio buttons
	gf_guiElementWrite(gv_elements.inputSubjectTypeMulti, "bool", subject.isMulti());
	gf_guiElementWrite(gv_elements.inputSubjectTypeExternal, "bool", subject.isExternal());
	gf_guiElementWrite(gv_elements.inputSubjectExtExternal, "bool", subject.getExternalType() == "external");
	gf_guiElementWrite(gv_elements.inputSubjectExtInterface, "bool", subject.getExternalType() == "interface");
	gf_guiElementWrite(gv_elements.inputSubjectExtInstantInterface, "bool", subject.getExternalType() == "instantinterface");
	
	// add onClick event to external subject type	
	if (gf_elementExists(gv_elements.inputSubjectTypeExternal))
	{
		document.getElementById(gv_elements.inputSubjectTypeExternal).onclick = function ()
		{
			if (gf_guiElementRead(gv_elements.inputSubjectTypeExternal, "bool"))
				gf_guiElementShow(gv_elements.inputSubjectRelOuter);
			else
				gf_guiElementHide(gv_elements.inputSubjectRelOuter);
		};
	}
}

/**
 * Adds an onclick event to the given element which will hide all settings-divs and show only the proper settings div.
 * 
 * @param {String} element The element to add the onClick event to.
 * @param {String} elementToShow The ID of the settings div to show.
 */
function gf_guiEdgeTypeAddOnClick (element, elementToShow)
{
	if (gf_elementExists(element))
		document.getElementById(element).onclick = function () {

			gf_guiElementHide(gv_elements.inputEdgeTypeCondO);
			gf_guiElementHide(gv_elements.inputEdgeTypeExceptO);
			gf_guiElementHide(gv_elements.inputEdgeTypeTimeoutO);
			
			if (gf_guiElementRead(element, "bool"))
				gf_guiElementShow(elementToShow);
		};
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
 * This method is used to fill a select field with all available macros of the current internal behavior.
 * 
 * @param {GCbehavior} behavior The currently selected internal behavior (used to read the macros).
 * @param {String} elementMacro The ID of the select element that holds the available macros.
 * @param {boolean} newMacro When set to true an option will be added to create a new macro.
 * @returns {void}
 */
function gf_guiLoadDropDownMacros (behavior, elementMacro, newMacro)
{
	if (!gf_isset(newMacro) || newMacro !== true)
		newMacro = false;
	
	// load macros	
	if (elementMacro != null && gf_elementExists(elementMacro))
	{
		var gt_select			= document.getElementById(elementMacro).options;
			gt_select.length	= 0;
		var gt_macroArray		= [];
		
		// create some entries to guide the user
		var gt_option			= document.createElement("option");
			gt_option.text		= "please select";
			gt_option.value		= "";
			gt_option.id		= elementMacro + "_00000.0";
			gt_select.add(gt_option);
		
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementMacro + "_00000.1";
			gt_select.add(gt_option);
		
		// collect macros
		for (var gt_mid in behavior.macros)
		{
			if (gt_mid != "##main##" && behavior.selectedMacro != gt_mid)
				gt_macroArray[gt_macroArray.length]	= {id: gt_mid, text: behavior.macros[gt_mid].name};
		}
		
		// sort the macros alphabetically
		gt_macroArray.sort(gf_guiSortArrayByText);
		
		// add the macros to the select field
		for (var gt_mid in gt_macroArray)
		{
			gt_option		= document.createElement("option");
			gt_option.text	= gf_replaceNewline(gt_macroArray[gt_mid].text, " ");
			gt_option.value	= gt_macroArray[gt_mid].id;
			gt_option.id	= elementMacro + "_" + gt_mid;
			gt_select.add(gt_option);
		}
		
		if (newMacro === true)
		{
			gt_option			= document.createElement("option");
			gt_option.text		= "----------------------------";
			gt_option.value		= "";
			gt_option.id		= elementMacro + "_00000.42";
			gt_select.add(gt_option);
		
			gt_option			= document.createElement("option");
			gt_option.text		= "create a new macro";
			gt_option.value		= "##createNew##";
			gt_option.id		= elementMacro + "_00000.new";
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
 * This method is used to fill a select field with all available node types.
 * 
 * @param {String} elementNodeTypes The ID of the select element that holds the available node types.
 * @returns {void}
 */
function gf_guiLoadDropDownNodeTypes (elementNodeTypes)
{	
	// load node types
	if (elementNodeTypes != null && gf_elementExists(elementNodeTypes))
	{
		
		$('#' + elementNodeTypes).empty();
		
		var gt_select			= document.getElementById(elementNodeTypes);
		var gt_nodeTypesArray	= [];
		var gt_option			= "";
		
		// base elements
		var gt_optgrp = document.createElement("optgroup");
			gt_optgrp.label	= "base elements";
			
		// collect the base node types
		for (var gt_key in gv_nodeTypes)
		{
			gt_nodeTypesArray[gt_nodeTypesArray.length]	= {id: gt_key, text: gv_nodeTypes[gt_key].label};
		}
		
		// sort the node types alphabetically
		gt_nodeTypesArray.sort(gf_guiSortArrayByText);
		
		// add the node types to the optgroup
		for (var gt_nid in gt_nodeTypesArray)
		{
			gt_option		= document.createElement("option");
			gt_option.text	= gt_nodeTypesArray[gt_nid].text;
			gt_option.value	= gt_nodeTypesArray[gt_nid].id;
			gt_option.id	= elementNodeTypes + "_" + gt_nid;
			gt_optgrp.appendChild(gt_option);
		}
		gt_select.appendChild(gt_optgrp);
		
		
		// predefined actions
			gt_optgrp = document.createElement("optgroup");
			gt_optgrp.label	= "predefined actions";
			gt_nodeTypesArray	= [];
		
		// collect the predefined actions
		for (var gt_key in gv_predefinedActions)
		{
			gt_nodeTypesArray[gt_nodeTypesArray.length]	= {id: gt_key, text: gv_predefinedActions[gt_key].label};
		}
		
		// sort the node types alphabetically
		gt_nodeTypesArray.sort(gf_guiSortArrayByText);
		
		// add the node types to the optgroup
		for (var gt_nid in gt_nodeTypesArray)
		{
			gt_option		= document.createElement("option");
			gt_option.text	= gt_nodeTypesArray[gt_nid].text;
			gt_option.value	= "$" + gt_nodeTypesArray[gt_nid].id;
			gt_option.id	= elementNodeTypes + "_$" + gt_nid;
			gt_optgrp.appendChild(gt_option);
		}
		gt_select.appendChild(gt_optgrp);
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
		for (var gt_nodeId in behavior.getMacro().nodes)
		{
			gt_node	= behavior.getMacro().nodes[gt_nodeId];
			if (gt_node.isStart() && !gt_node.isMajorStartNode())
			{
				gt_statesArray[gt_statesArray.length]	= {id: gt_nodeId, text: behavior.getMacro().nodes[gt_nodeId].text};
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
			gt_option.text	= gv_varManOperations[gt_vmid].label + " (" + gv_varManOperations[gt_vmid].desc + ")";
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
		
	gf_guiElementHide(gv_elements.inputNodeOptMessageO);
	gf_guiElementHide(gv_elements.inputNodeOptSubjectO);
	gf_guiElementHide(gv_elements.inputNodeOptChannelOuter);
	gf_guiElementHide(gv_elements.inputNodeOptCorrelationIdO);
	gf_guiElementHide(gv_elements.inputNodeOptStateOuter);
	gf_guiElementHide(gv_elements.inputNodeOptionsOuter);
	gf_guiElementHide(gv_elements.inputNodeVariableO);
	gf_guiElementHide(gv_elements.inputNodeVarManOuter);
	gf_guiElementHide(gv_elements.inputNodeMacroOuter);
		
	if (nodeType == "$variableman")
	{	
		gf_guiElementShow(gv_elements.inputNodeVarManOuter);
			
		gf_guiLoadDropDownVariables(behavior, gv_elements.inputNodeVarManVar1, false, false);
		gf_guiLoadDropDownVariables(behavior, gv_elements.inputNodeVarManVar2, false, false);
		gf_guiLoadDropDownVariables(behavior, gv_elements.inputNodeVarManVarStore, true, false);
		gf_guiLoadDropDownVarManOperations(gv_elements.inputNodeVarManOperation);
			
	}
	else if (nodeType.substr(0, 1) == "$")
	{
		var gt_predefAction	= {subject: false, message: false, wildcard: false, channel: false, correlationid: false, options: false, state: false};
		
		
		if (gf_isset(gv_predefinedActions[nodeType.substr(1)]))
			gt_predefAction	= gv_predefinedActions[nodeType.substr(1)];
			
		var gt_showOptions	= false;
			
		if (gt_predefAction.message)
		{
			gt_showOptions	= true;
			gf_guiElementShow(gv_elements.inputNodeOptMessageO);
		}
			
		if (gt_predefAction.subject)
		{
			gt_showOptions	= true;
			gf_guiElementShow(gv_elements.inputNodeOptSubjectO);
		}
			
		if (gt_predefAction.channel)
		{
			gt_showOptions	= true;
			gf_guiElementShow(gv_elements.inputNodeOptChannelOuter);
		}
			
		if (gt_predefAction.correlationid)
		{
			gt_showOptions	= true;
			gf_guiElementShow(gv_elements.inputNodeOptCorrelationIdO);
		}
			
		if (gt_predefAction.state)
		{
			gt_showOptions	= true;
			gf_guiElementShow(gv_elements.inputNodeOptStateOuter);
		}
			
		if (gt_showOptions)
			gf_guiElementShow(gv_elements.inputNodeOptionsOuter);
			
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
		gf_guiElementShow(gv_elements.inputNodeVariableO);
		gf_guiLoadDropDownVariables(behavior, gv_elements.inputNodeVariable, false, false);
	}
	else if (nodeType == "macro")
	{
		gf_guiElementShow(gv_elements.inputNodeMacroOuter);
		gf_guiLoadDropDownMacros(behavior, gv_elements.inputNodeMacro, true);
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
 * @returns {Object} Indizes: text, relatedSubject, type, timeout, optional, messageType, priority, manualTimeout, exception, variable, variableText, correlationId, comment
 */
function gf_guiReadEdge ()
{
	var gt_result	= {text: "", relatedSubject: "", timeout: "", type: "", optional: false, messageType: "", priority: 1, manualTimeout: false, exception: "", variable: "", variableText: "", correlationId: "", comment: ""};
	
	var gt_relatedSubject	= {id: "", min: -1, max: -1, createNew: false, variable: "", variableText: "", useVariable: false};
	
	var gt_text				= gf_guiElementRead(gv_elements.inputEdgeText, "string", "");
	var gt_exception		= gf_guiElementRead(gv_elements.inputEdgeExceptionText, "string", "");
	var gt_timeout			= gf_guiElementRead(gv_elements.inputEdgeTimeout, "string", "");
	var gt_optional			= gf_guiElementRead(gv_elements.inputEdgeOptional, "bool", false);
	var gt_messageType		= gf_guiElementRead(gv_elements.inputEdgeMessage, "string", "");
	var gt_priority			= gf_guiElementRead(gv_elements.inputEdgePriority, "string", "1");
	var gt_manualTimeout	= gf_guiElementRead(gv_elements.inputEdgeTimeoutManual, "bool", false);
	var gt_comment			= gf_guiElementRead(gv_elements.inputEdgeComment, "string", "");
	
	var gt_targetVar		= gf_guiElementRead(gv_elements.inputEdgeTargetMVariable, "string", "");
	var gt_targetVarNew		= gf_guiElementRead(gv_elements.inputEdgeTargetMVarText, "string", "");
	var gt_storeVar			= gf_guiElementRead(gv_elements.inputEdgeStoreVariable, "string", "");
	var gt_storeVarNew		= gf_guiElementRead(gv_elements.inputEdgeStoreVariableN, "string", "");
	
	var gt_correlationId	= gf_guiElementRead(gv_elements.inputEdgeCorrelationId, "string", "");
	
	var gt_isVariable		= gf_guiElementRead(gv_elements.inputEdgeTargetMTypeV, "bool", false);
	var gt_isAll			= gf_guiElementRead(gv_elements.inputEdgeTargetMTypeA, "bool", false);
	
	gt_relatedSubject.id			= gf_guiElementRead(gv_elements.inputEdgeTarget, "string", "");
	gt_relatedSubject.min			= !gt_isAll ? gf_guiElementRead(gv_elements.inputEdgeTargetMMin, "string", "-1") : "-1";
	gt_relatedSubject.max			= !gt_isAll ? gf_guiElementRead(gv_elements.inputEdgeTargetMMax, "string", "-1") : "-1";
	gt_relatedSubject.createNew		= gf_guiElementRead(gv_elements.inputEdgeTargetMTypeN, "bool", false);
	gt_relatedSubject.variable		= gt_isVariable ? gt_targetVar : "";
	gt_relatedSubject.variableText	= gt_isVariable ? gt_targetVarNew : "";
	gt_relatedSubject.useVariable	= gt_isVariable;
	
	var gt_type				= "exitcondition";
	
	if (gf_guiElementRead(gv_elements.inputEdgeTypeTimeout, "bool", false))
		gt_type	= "timeout";
		
	if (gf_guiElementRead(gv_elements.inputEdgeTypeException, "bool", false))
		gt_type	= "errorcondition";
		
	if (gf_guiElementRead(gv_elements.inputEdgeTypeBooleanFalse, "bool", false))
		gt_type	= "boolfalse";
		
	if (gf_guiElementRead(gv_elements.inputEdgeTypeBooleanTrue, "bool", false))
		gt_type	= "booltrue";
	
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
	gt_result.comment			= gt_comment;
	
	return gt_result;
}

/**
 * Read the values for the selected node from the input fields.
 * 
 * @see GCcommunication::updateNode()
 * @returns {Object} Indizes: text, isStart, type, options, isMajorStartNode, channel, channelText, variable, varMan, macro, macroText, comment
 */
function gf_guiReadNode ()
{
	var gt_result	= {text: "", isStart: false, type: "", options: {subject: "", message: "", correlationId: "", channel: "", state: ""}, isMajorStartNode: false, channel: "", channelText: "", variable: "", varMan: {var1: "", var2: "", storevar: "", operation: "", storevarText: ""}, macro: "", macroText: "", comment: ""};
	
	var gt_text					= gf_guiElementRead(gv_elements.inputNodeText, "string", "");
	var gt_isStart				= gf_guiElementRead(gv_elements.inputNodeStart, "bool", false);
	var gt_type 				= gf_guiElementRead(gv_elements.inputNodeType, "string", "").toLowerCase();
	var gt_opt_subject 			= gf_guiElementRead(gv_elements.inputNodeOptSubject, "string", "");
	var gt_opt_message 			= gf_guiElementRead(gv_elements.inputNodeOptMessage, "string", "");
	var gt_opt_channel 			= gf_guiElementRead(gv_elements.inputNodeOptChannel, "string", "");
	var gt_opt_correlationId 	= gf_guiElementRead(gv_elements.inputNodeOptCorrelationId, "string", "");
	var gt_opt_state 			= gf_guiElementRead(gv_elements.inputNodeOptState, "string", "");
	var gt_isMajorStartNode		= gf_guiElementRead(gv_elements.inputNodeMajorStart, "bool", false);
	var gt_comment				= gf_guiElementRead(gv_elements.inputNodeComment, "string", "");
	
	
	var gt_channel			= gf_guiElementRead(gv_elements.inputNodeChannel, "string", "");
	var gt_channelNew		= gf_guiElementRead(gv_elements.inputNodeChannelNew, "string", "");
	var gt_variable			= gf_guiElementRead(gv_elements.inputNodeVariable, "string", "");
	
	
	var gt_varMan	= {};
		gt_varMan.var1			= gf_guiElementRead(gv_elements.inputNodeVarManVar1, "string", "");
		gt_varMan.var2			= gf_guiElementRead(gv_elements.inputNodeVarManVar2, "string", "");
		gt_varMan.storevar		= gf_guiElementRead(gv_elements.inputNodeVarManVarStore, "string", "");
		gt_varMan.operation		= gf_guiElementRead(gv_elements.inputNodeVarManOperation, "string", "");
		gt_varMan.storevarText	= gf_guiElementRead(gv_elements.inputNodeVarManVarStoreN, "string", "");
		
	var gt_macro		= gf_guiElementRead(gv_elements.inputNodeMacro, "string", "");
	var gt_macroText	= gf_guiElementRead(gv_elements.inputNodeMacroNew, "string", "");
	
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
	gt_result.macro				= gt_macro;
	gt_result.macroText			= gt_macroText;
	gt_result.comment			= gt_comment;
	
	return gt_result;
}

/**
 * Read the values for the selected subject from the input fields.
 * 
 * @see GCcommunication::updateSubject()
 * @returns {Object} Indizes: text, role, type, inputPool, relatedProcess, relatedSubject, externalType, comment
 */
function gf_guiReadSubject ()
{
	var gt_result	= {text: "", role: "", type: "", inputPool: "", relatedProcess: "", relatedSubject: "", externalType: "", comment: ""};
	
	var gt_text			= gf_guiElementRead(gv_elements.inputSubjectText, "string", "");
	var gt_role			= gf_guiElementRead(gv_elements.inputSubjectRole, "string", "");
	var gt_inputPool	= gf_guiElementRead(gv_elements.inputSubjectInputPool, "string", "");
	var gt_relProcess	= gf_guiElementRead(gv_elements.inputSubjectRelProcess, "string", "");
	var gt_relSubject	= gf_guiElementRead(gv_elements.inputSubjectRelSubject, "string", "");
	var gt_comment		= gf_guiElementRead(gv_elements.inputSubjectComment, "string", "");
	
	var gt_type	= "";
	var gt_externalType	= "external";
	
	if (gf_guiElementRead(gv_elements.inputSubjectTypeMulti, "bool", false)		=== true)
		gt_type += "multi";
	
	if (gf_guiElementRead(gv_elements.inputSubjectTypeExternal, "bool", false)	=== true)
		gt_type += "external";
		
	if (gt_type == "")
		gt_type = "single";
	
	if (gf_guiElementRead(gv_elements.inputSubjectExtInstantInterface, "bool", false)	=== true)
		gt_externalType	= "instantinterface";
		
	if (gf_guiElementRead(gv_elements.inputSubjectExtInterface, "bool", false)			=== true)
		gt_externalType	= "interface";
	
	gt_result.text				= gt_text;
	gt_result.role				= gt_role;
	gt_result.type				= gt_type;
	gt_result.inputPool			= gt_inputPool;
	gt_result.relatedProcess	= gt_relProcess;
	gt_result.relatedSubject	= gt_relSubject;
	gt_result.externalType		= gt_externalType;
	gt_result.comment			= gt_comment;
	
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
			gf_guiElementEnable(gv_elements.inputEdgeText, "readonly");
			gf_guiElementWrite(gv_elements.inputEdgeText, "string", "");
		}
		else if (gt_message.substr(0, 1) == "m")
		{
			gf_guiElementEnable(gv_elements.inputEdgeText, "readonly");
			gf_guiElementWrite(gv_elements.inputEdgeText, "string", gv_graph.messageTypes[gt_message]);
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
	
	if (type == "n")
		gf_guiElementShow(gv_elements.inputNodeOuter);
	else
		gf_guiElementHide(gv_elements.inputNodeOuter);
		
	if (type == "2")
		gf_guiElementShow(gv_elements.inputEdgeOuter);
	else
		gf_guiElementHide(gv_elements.inputEdgeOuter);
}
