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
 * This object contains a list of all DOM elements used within the API.
 * Do not change the indexes of the array as they are referred to in the API.
 * But you should adapt the values to the actual IDs of the elements on the page.
 * 
 * @type Object
 */
var gv_elements = {
	
	guiConversationSelect:			"graph_conversation_select",
	
	graphBVouter:				"graph_bv_outer",
	graphCVouter:				"graph_cv_outer",
	inputEdgeCorrelationId:		"ge_edge_correlationid",
	inputEdgeCorrelationIdO:	"ge_edge_correlationid_outer",
	inputEdgeExceptionText:		"ge_edge_exception_text",
	inputEdgeMessage:			"ge_edge_message",
	inputEdgeOptional:			"ge_edge_optional",
	inputEdgeOptionalO:			"ge_edge_optionalO",
	inputEdgeOuter:				"edge",
	inputEdgePriority:			"ge_edge_priority",
	inputEdgePriorityO:			"ge_edge_priority_outer",
	inputEdgeStoreOuter:		"ge_edge_store_outer",
	inputEdgeStoreVariable:		"ge_edge_store_variable",
	inputEdgeStoreVariableN:	"ge_edge_store_variable_new",
	inputEdgeStoreVariableNO:	"ge_edge_store_variable_new_outer",
	inputEdgeTarget:			"ge_edge_target",
	inputEdgeTargetO:			"ge_edge_target_outer",
	inputEdgeTargetMMin:		"ge_edge_target_multi_min",
	inputEdgeTargetMMax:		"ge_edge_target_multi_max",
	inputEdgeTargetMMMO:		"ge_edge_target_multi_minmax_outer",
	inputEdgeTargetMOuter:		"ge_edge_target_multi_outer",
	inputEdgeTargetNewO:		"ge_edge_target_new_outer",
	inputEdgeTargetNewName:		"ge_edge_target_new_name",
	inputEdgeTargetNewRole:		"ge_edge_target_new_role",
	inputEdgeText:				"ge_edge_text",
	inputEdgeTimeout:			"ge_edge_timeout",
	inputEdgeTimeoutEx:			"ge_edge_timeoutExample",
	inputEdgeTimeoutManual:		"ge_edge_timeoutManual",
	inputEdgeTransportMethod:	"ge_edge_transportMethod",
	inputEdgeTypeCondO:			"ge_edge_typeCOuter",
	inputEdgeTypeExceptO:		"ge_edge_typeEOuter",
	inputEdgeTypeTimeoutO:		"ge_edge_typeTOuter",
	inputNodeConversation:			"ge_node_conversation",
	inputNodeConversationNew:		"ge_node_conversation_new",
	inputNodeConversationNewOuter:	"ge_node_conversation_new_outer",
	inputNodeConversationOuter:		"ge_node_conversation_outer",
	inputNodeMajorStart:		"ge_node_major_start_node",
	inputNodeMajorStartOuter:	"ge_node_major_start_node_outer",
	inputNodeOptionsOuter:		"ge_node_options_outer",
	inputNodeOptConversation:		"ge_node_opt_conversation",
	inputNodeOptConversationOuter:	"ge_node_opt_conversation_outer",
	inputNodeOptCorrelationId:	"ge_node_opt_correlation_id",
	inputNodeOptCorrelationIdO:	"ge_node_opt_correlation_id_outer",
	inputNodeOptMessage:		"ge_node_opt_message",
	inputNodeOptMessageO:		"ge_node_opt_messageOuter",
	inputNodeOptState:			"ge_node_opt_state",
	inputNodeOptStateOuter:		"ge_node_opt_state_outer",
	inputNodeOptSubject:		"ge_node_opt_subject",
	inputNodeOptSubjectO:		"ge_node_opt_subjectOuter",
	inputNodeOuter:				"node",
	inputNodeStartOuter:		"ge_node_start_node_outer",
	inputNodeText:				"ge_text",
	inputNodeType:				"ge_type2",
	inputNodeVariable:			"ge_node_variable",
	inputNodeVariableO:			"ge_node_variable_outer",
	inputSubjectInputPool:		"ge_cv_inputPool",
	inputSubjectRelOuter:		"ge_cv_relatedOuter",
	inputSubjectRelProcess:		"ge_cv_relatedProcess",
	inputSubjectRelSubject:		"ge_cv_relatedSubject",
	inputSubjectRole:			"ge_cv_id",
	inputSubjectStartSubject:	"ge_cv_startSubject",
	inputSubjectText:			"ge_cv_text",
	
	// select elements
	inputNodeStart:		"ge_type_start",
	
	// subject types
	inputSubjectTypeMulti:		"ge_cv_type_multi",
	inputSubjectTypeExternal:	"ge_cv_type_external",
	
	// subject external types
	inputSubjectExtExternal:			"ge_cv_external_external",
	inputSubjectExtInterface:			"ge_cv_external_interface",
	inputSubjectExtInstantInterface:	"ge_cv_external_instantInterface",
	
	// edge multi target
	inputEdgeTargetMTypeA:		"ge_edge_target_multi_type_all",
	inputEdgeTargetMTypeL:		"ge_edge_target_multi_type_limit",
	inputEdgeTargetMTypeN:		"ge_edge_target_multi_type_new",
	inputEdgeTargetMTypeNO:		"ge_edge_target_multi_type_new_outer",
	inputEdgeTargetMTypeV:		"ge_edge_target_multi_type_var",
	inputEdgeTargetMTypeVO:		"ge_edge_target_multi_type_var_outer",
	inputEdgeTargetMVariable:	"ge_edge_target_multi_variable",
	inputEdgeTargetMVarText:	"ge_edge_target_multi_variable_text",
	inputEdgeTargetMVarTextO:	"ge_edge_target_multi_variable_text_outer",
	
	// edge types
	inputEdgeTypeBooleanFalse:	"ge_edge_type_boolean_false",
	inputEdgeTypeBooleanOuter:	"ge_edge_type_boolean_outer",
	inputEdgeTypeBooleanTrue:	"ge_edge_type_boolean_true",
	inputEdgeTypeCondition:		"ge_edge_type_condition",
	inputEdgeTypeException:		"ge_edge_type_exception",
	inputEdgeTypeNormalOuter:	"ge_edge_type_normal_outer",
	inputEdgeTypeTimeout:		"ge_edge_type_timeout",
	
	// variable manipulation
	inputNodeVarManOperation:	"ge_node_varman_operation",
	inputNodeVarManOuter:		"ge_node_varman_outer",
	inputNodeVarManVar1:		"ge_node_varman_var1",
	inputNodeVarManVar2:		"ge_node_varman_var2",
	inputNodeVarManVarStore:	"ge_node_varman_store",
	inputNodeVarManVarStoreN:	"ge_node_varman_store_new",
	inputNodeVarManVarStoreNO:	"ge_node_varman_store_new_outer",
	
	// macro
	inputNodeMacroOuter:		"ge_node_macro_outer",
	inputNodeMacro:				"ge_node_macro",
	inputNodeMacroNewOuter:		"ge_node_macro_new_outer",
	inputNodeMacroNew:			"ge_node_macro_new",
	
	// comment fields
	inputNodeComment:			"ge_node_comment",
	inputEdgeComment:			"ge_edge_comment",
	inputSubjectComment:		"ge_subject_comment"
};

/**
 * This object contains a list of all external functions used within the API.
 * Do not change the indexes of the array as they are referred to in the API.
 * But you should adapt the values to the actual names of the used functions.
 * You may create new functions that replace the internal methods and place their names here.
 * 
 * @type Object
 */
var gv_functions	= {
	/*
	 * functions used in GCbehavior
	 */
	behavior:
	{
		/**
		 * updates the list of available macros in the GUI
		 * 
		 * @returns {void}
		 */
		updateListOfMacros:	"updateListOfMacros"
	},
	
	/*
	 * functions used in GCcommunication
	 */
	communication:
	{
		/**
		 * used to change the view shown (toggle between behavioral view and communication view)
		 * 
		 * @see GCcommunication::changeView(), gf_guiChangeView()
		 * @param {String} view Either "bv" or "cv" depending on the selected view
		 * @returns {void}
		 */
		changeView:			"",
		
		/**
		 * hook called when changing the view
		 * 
		 * @param {String} view Either "bv" or "cv" depending on the selected view
		 * @returns {void}
		 */
		changeViewHook:		"gf_empty",
		
		/**
		 * clear the input fields for nodes and edges
		 * 
		 * @see GCcommunication::loadInformation(true), gf_guiClearInputFields()
		 * @returns {void}
		 */
		clearInputFields:	"",
		
		/**
		 * displays the information of the selected edge
		 * 
		 * @see GCcommunication::loadInformationEdge(), gf_guiDisplayEdge()
		 * @param {GCedge} edge A reference to the selected edge.
		 * @param {String} startType Type of the edge's start node.
		 * @returns {void}
		 */
		displayEdge:		"",
		
		/**
		 * displays the information of the selected node
		 * 
		 * @see GCcommunication::loadInformation(), gf_guiDisplayNode()
		 * @param {GCnode} node A reference to the selected node.
		 * @returns {void}
		 */
		displayNode:		"",
		
		/**
		 * displays the information of the selected subject
		 * 
		 * @see GCcommunication::loadInformation(), gf_guiDisplaySubject()
		 * @param {GCsubject} subject A reference to the selected subject.
		 * @returns {void}
		 */
		displaySubject:		"",
		
		/**
		 * read input fields (edge) and returns an object with the values
		 * 
		 * @see GCcommunication::updateEdge(), gf_guiReadEdge()
		 * @returns {Object} Indizes: text, relatedSubject, type, timeout, optional, messageType, priority, manualTimeout, exception, variable, variableText, correlationId, comment, transportMethod
		 */
		readEdge:			"",
		
		/**
		 * read input fields (node) and returns an object with the values
		 * 
		 * @see GCcommunication::updateNode(), gf_guiReadNode()
		 * @returns {Object} Indizes: text, isStart, type, options, isMajorStartNode, conversation, conversationText, variable, varMan, macro, macroText, comment
		 */
		readNode:			"",
		
		/**
		 * read input fields (subject) and returns an object with the values
		 * 
		 * @see GCcommunication::updateNode(), gf_guiReadSubject()
		 * @returns {Object} Indizes: text, role, type, inputPool, relatedProcess, relatedSubject, externalType, comment, startSubject
		 */
		readSubject:		"",
		
		/**
		 * toggle between form containing input fields for node manipulation and form containing input fields for edge manipulation
		 * 
		 * @see GCcommunication::loadInformation() | GCcommunication::loadInformationEdge(), gf_guiToggleNEForms()
		 * @param {String} type Either "n" (node) or "e" (edge) - the form that will be shown
		 * @returns {void}
		 */
		toggleNEForms:		"",
		
		/**
		 * updates the list of available conversations in the GUI
		 * 
		 * @returns {void}
		 */
		updateListOfConversations:	"",
		
		/**
		 * updates the list of available subjects in the GUI
		 * 
		 * @see GCcommunication::selectNode()
		 * @returns {void}
		 */
		updateListOfSubjects:	""
	},
	
	/*
	 * these functions are called when a certain event is fired
	 */
	events:
	{
		/**
		 * called when an edge is clicked
		 * 
		 * @see tk_graph.js :: gf_paperClickEdge()
		 * @param {int} ID The id of the clicked edge.
		 * @returns {void}
		 */
		edgeClicked:			"",
		
		/**
		 * called when an edge is clicked (pre-method-call-hook)
		 * 
		 * @see tk_graph.js :: gf_paperClickEdge()
		 * @param {int} ID The id of the clicked edge.
		 * @returns {void}
		 */
		edgeClickedHook:		"",
		
		/**
		 * called when a node is clicked
		 * 
		 * @see tk_graph.js :: gf_paperClickNodeB()
		 * @param {int} ID The id of the clicked node.
		 * @returns {void}
		 */
		nodeClicked:			"",
		
		/**
		 * called when a node is clicked (pre-method-call-hook)
		 * 
		 * @see tk_graph.js :: gf_paperClickNodeB()
		 * @param {int} ID The id of the clicked node.
		 * @returns {void}
		 */
		nodeClickedHook:		"",
		
		/**
		 * called when a subject is clicked
		 * 
		 * @see tk_graph.js :: gf_paperClickNodeC()
		 * @param {String} ID The id of the clicked subject.
		 * @returns {void}
		 */
		subjectClicked:			"",
		
		/**
		 * called when a subject is clicked (pre-method-call-hook)
		 * 
		 * @see tk_graph.js :: gf_paperClickNodeC()
		 * @param {String} ID The id of the clicked subject.
		 * @returns {void}
		 */
		subjectClickedHook:		"",
		
		/**
		 * called when an external subject is double-clicked
		 * 
		 * @see tk_graph.js :: gf_paperDblClickNodeC()
		 * @param {String} ID The id of the referenced process.
		 * @returns {void}
		 */
		subjectDblClickedExternal:		"gf_empty",
		
		/**
		 * called when an instant interface (external subject) is double-clicked
		 * 
		 * @see tk_graph.js :: gf_paperDblClickNodeC()
		 * @param {String} ID The id of the double-clicked subject.
		 * @returns {void}
		 */
		subjectDblClickedInstantInterface:		"",
		
		/**
		 * called when an interface (external subject) is double-clicked
		 * 
		 * @see tk_graph.js :: gf_paperDblClickNodeC()
		 * @param {String} ID The id of the double-clicked subject.
		 * @returns {void}
		 */
		subjectDblClickedInterface:		"",
		
		/**
		 * called when an internal subject is double-clicked
		 * 
		 * @see tk_graph.js :: gf_paperDblClickNodeC()
		 * @param {String} ID The id of the double-clicked subject.
		 * @returns {void}
		 */
		subjectDblClickedInternal:		"gf_empty",
		
		/**
		 * called when a subject is double-clicked (pre-method-call-hook)
		 * 
		 * @see tk_graph.js :: gf_paperDblClickNodeC()
		 * @param {String} ID The id of the double-clicked subject.
		 * @returns {void}
		 */
		subjectDblClickedHook:	""
	},
	
	/*
	 * functions used in tk_graph and others
	 */
	general:
	{
		/**
		 * used to change to the behavioral view
		 * 
		 * @see tk_graph.js :: gf_toggleBV()
		 * @returns {void}
		 */
		changeViewBV:		"gf_empty"
	}
};


/**
 * Contains a list of topics used for the publish/subscribe system.
 * 
 * @type Object
 */
var gv_topics	= {
	/*
	 * topics used in GCbehavior
	 */
	behavior:
	{
		/**
		 * topic for updates on macro list
		 */
		updateListOfMacros:	"tk_graph/updateListOfMacros"
	},
	
	/*
	 * topics used in GCcommunication
	 */
	communication:
	{
		/**
		 * topic for toggles between behavioral view and communication view
		 * 
		 * @see GCcommunication::changeView(), gf_guiChangeView()
		 * @param {String} view Either "bv" or "cv" depending on the selected view
		 */
		changeView:			"",
		
		/**
		 * topic for toggles between behavioral view and communication view (called before the actual switch is done)
		 * 
		 * @param {String} view Either "bv" or "cv" depending on the selected view
		 */
		changeViewHook:		"tk_graph/changeViewHook",
		
		/**
		 * topic for clear-requests for all input fields
		 * 
		 * @see GCcommunication::loadInformation(true), gf_guiClearInputFields()
		 */
		clearInputFields:	"",
		
		/**
		 * topic on information about edges
		 * 
		 * @see GCcommunication::loadInformationEdge(), gf_guiDisplayEdge()
		 * @param {GCedge} edge A reference to the selected edge.
		 * @param {String} startType Type of the edge's start node.
		 */
		displayEdge:		"",
		
		/**
		 * topic on information about nodes
		 * 
		 * @see GCcommunication::loadInformation(), gf_guiDisplayNode()
		 * @param {GCnode} node A reference to the selected node.
		 */
		displayNode:		"",
		
		/**
		 * topic on information about subjects
		 * 
		 * @see GCcommunication::loadInformation(), gf_guiDisplaySubject()
		 * @param {GCsubject} subject A reference to the selected subject.
		 */
		displaySubject:		"",
		
		/**
		 * notifier topic for reading actions on edge input fields
		 * 
		 * @see GCcommunication::updateEdge(), gf_guiReadEdge()
		 */
		readEdge:			"",
		
		/**
		 * notifier topic for reading actions on node input fields
		 * 
		 * @see GCcommunication::updateNode(), gf_guiReadNode()
		 */
		readNode:			"",
		
		/**
		 * notifier topic for reading actions on subject input fields
		 * 
		 * @see GCcommunication::updateNode(), gf_guiReadSubject()
		 */
		readSubject:		"",
		
		/**
		 * toggle between form containing input fields for node manipulation and form containing input fields for edge manipulation
		 * 
		 * @see GCcommunication::loadInformation() | GCcommunication::loadInformationEdge(), gf_guiToggleNEForms()
		 * @param {String} type Either "n" (node) or "e" (edge) - the form that will be shown
		 */
		toggleNEForms:		"",
		
		/**
		 * notifier topic for updates the list of available conversations in the GUI
		 */
		updateListOfConversations:	"",
		
		/**
		 * notifier topic for updates the list of available subjects in the GUI
		 * 
		 * @see GCcommunication::selectNode()
		 */
		updateListOfSubjects:	"tk_graph/updateListOfSubjects"
	},
	
	/*
	 * these topics are called when a certain event is fired
	 */
	events:
	{
		/**
		 * notifier topic for when an edge is clicked
		 * 
		 * @see tk_graph.js :: gf_paperClickEdge()
		 * @param {int} ID The id of the clicked edge.
		 */
		edgeClicked:			"",
		
		/**
		 * notifier topic for when an edge is clicked (before the actual click is processed)
		 * 
		 * @see tk_graph.js :: gf_paperClickEdge()
		 * @param {int} ID The id of the clicked edge.
		 */
		edgeClickedHook:		"tk_graph/edgeClickedHook",
		
		/**
		 * notifier topic for when a node is clicked
		 * 
		 * @see tk_graph.js :: gf_paperClickNodeB()
		 * @param {int} ID The id of the clicked node.
		 */
		nodeClicked:			"",
		
		/**
		 * notifier topic for when a node is clicked (before the actual click is processed)
		 * 
		 * @see tk_graph.js :: gf_paperClickNodeB()
		 * @param {int} ID The id of the clicked node.
		 */
		nodeClickedHook:		"tk_graph/nodeClickedHook",
		
		/**
		 * notifier topic for when a subject is clicked
		 * 
		 * @see tk_graph.js :: gf_paperClickNodeC()
		 * @param {String} ID The id of the clicked subject.
		 */
		subjectClicked:			"",
		
		/**
		 * notifier topic for when a subject is clicked (before the actual click is processed)
		 * 
		 * @see tk_graph.js :: gf_paperClickNodeC()
		 * @param {String} ID The id of the clicked subject.
		 */
		subjectClickedHook:		"tk_graph/subjectClickedHook",
		
		/**
		 * notifier topic for when an external subject is double clicked
		 * 
		 * @see tk_graph.js :: gf_paperDblClickNodeC()
		 * @param {String} ID The id of the referenced process.
		 */
		subjectDblClickedExternal:		"tk_graph/subjectDblClickedExternal",
		
		/**
		 * notifier topic for when an instant interface (external subject) is double-clicked
		 * 
		 * @see tk_graph.js :: gf_paperDblClickNodeC()
		 * @param {String} ID The id of the double-clicked subject.
		 */
		subjectDblClickedInstantInterface:		"tk_graph/subjectDblClickedInternal",
		
		/**
		 * notifier topic for when an interface (external subject) is double-clicked
		 * 
		 * @see tk_graph.js :: gf_paperDblClickNodeC()
		 * @param {String} ID The id of the double-clicked subject.
		 */
		subjectDblClickedInterface:		"tk_graph/subjectDblClickedInternal",
		
		/**
		 * notifier topic for when an internal subject is double-clicked
		 * 
		 * @see tk_graph.js :: gf_paperDblClickNodeC()
		 * @param {String} ID The id of the double-clicked subject.
		 */
		subjectDblClickedInternal:		"tk_graph/subjectDblClickedInternal",
		
		/**
		 * notifier topic for when a subject is double-clicked (before the actual click is processed)
		 * 
		 * @see tk_graph.js :: gf_paperDblClickNodeC()
		 * @param {String} ID The id of the double-clicked subject.
		 */
		subjectDblClickedHook:	""
	},
	
	/*
	 * topics used in tk_graph and others
	 */
	general:
	{
		/**
		 * used when changing to behavioral view
		 * 
		 * @see tk_graph.js :: gf_toggleBV()
		 */
		changeViewBV:		"tk_graph/changeViewBV",
		
		/**
		 * topic for general updates on conversations
		 */
		conversations:			"/tk_graph/conversations",
		
		/**
		 * topic for general updates on macros
		 */
		macros:				"/tk_graph/macros",
		
		/**
		 * topic for general updates on states (nodes)
		 */
		states:				"/tk_graph/states",
		
		/**
		 * topic for general updates on subjects
		 */
		subjects:			"/tk_graph/subjects",
		
		/**
		 * topic for general updates on transitions (edges)
		 */
		transitions:		"/tk_graph/transitions"
	}
};
