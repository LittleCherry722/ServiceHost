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
	graphBVouter:			"graph_bv_outer",
	graphCVouter:			"graph_cv_outer",
	inputEdgeMessage:		"ge_edge_message",
	inputEdgeOptional:		"ge_edge_optional",
	inputEdgeOptionalO:		"ge_edge_optionalO",
	inputEdgeOuter:			"edge",
	inputEdgePriority:		"ge_edge_priority",
	inputEdgePriorityO:		"ge_edge_priority_outer",
	inputEdgeTarget:		"ge_edge_target",
	inputEdgeTargetO:		"ge_edge_target_outer",
	inputEdgeTargetMMin:	"ge_edge_target_multi_min",
	inputEdgeTargetMMax:	"ge_edge_target_multi_max",
	inputEdgeTargetMMMO:	"ge_edge_target_multi_minmax_outer",
	inputEdgeTargetMOuter:	"ge_edge_target_multi_outer",
	inputEdgeText:			"ge_edge_text",
	inputEdgeTimeout:		"ge_edge_timeout",
	inputEdgeTimeoutEx:		"ge_edge_timeoutExample",
	inputEdgeTimeoutManual:	"ge_edge_timeoutManual",
	inputEdgeTypeCondO:		"ge_edge_typeCOuter",
	inputEdgeTypeTimeoutO:	"ge_edge_typeTOuter",
	inputNodeMessage:		"ge_node_message",
	inputNodeMessageO:		"ge_node_messageOuter",
	inputNodeOuter:			"node",
	inputNodeSubject:		"ge_node_subject",
	inputNodeSubjectO:		"ge_node_subjectOuter",
	inputNodeText:			"ge_text",
	inputNodeType:			"ge_type2",
	inputSubjectInputPool:	"ge_cv_inputPool",
	inputSubjectRelOuter:	"ge_cv_relatedOuter",
	inputSubjectRelProcess:	"ge_cv_relatedProcess",
	inputSubjectRelSubject:	"ge_cv_relatedSubject",
	inputSubjectRole:		"ge_cv_id",
	inputSubjectText:		"ge_cv_text",
	
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
	
	// edge types
	inputEdgeTypeCondition:		"ge_edge_type_condition",
	inputEdgeTypeTimeout:		"ge_edge_type_timeout"
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
		changeViewHook:		"mViewChanged",
		
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
		displayEdge:		"mDisplayEdge",
		
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
		 * @returns {Object} Indizes: text, relatedSubject, type, timeout, optional, messageType, priority, manualTimeout
		 */
		readEdge:			"",
		
		/**
		 * read input fields (node) and returns an object with the values
		 * 
		 * @see GCcommunication::updateNode(), gf_guiReadNode()
		 * @returns {Object} Indizes: text, isStart, type, options
		 */
		readNode:			"",
		
		/**
		 * read input fields (subject) and returns an object with the values
		 * 
		 * @see GCcommunication::updateNode(), gf_guiReadSubject()
		 * @returns {Object} Indizes: text, role, type, inputPool, relatedProcess, relatedSubject, externalType
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
		 * updates the list of available subjects in the GUI
		 * 
		 * @see GCcommunication::selectNode()
		 * @returns {void}
		 */
		updateListOfSubjects:	"updateListOfSubjects"
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
		edgeClickedHook:		"mEdgeClicked",
		
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
		nodeClickedHook:		"mNodeClicked",
		
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
		subjectClickedHook:		"mSubjectClicked",
		
		/**
		 * called when an external subject is double-clicked
		 * 
		 * @see tk_graph.js :: gf_paperDblClickNodeC()
		 * @param {String} ID The id of the referenced process.
		 * @returns {void}
		 */
		subjectDblClickedExternal:		"gf_guiLoadExternalProcess",
		
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
		subjectDblClickedInternal:		"",
		
		/**
		 * called when a subject is double-clicked (pre-method-call-hook)
		 * 
		 * @see tk_graph.js :: gf_paperDblClickNodeC()
		 * @param {String} ID The id of the double-clicked subject.
		 * @returns {void}
		 */
		subjectDblClickedHook:	"mSubjectClicked"
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
		changeViewBV:		"showtab1"
	}
};


/**
 * Contains a list of topics used for the publish/subscribe system.
 * 
 * @type Object
 */
var gv_topics	= {
	subjects:		"/tk_graph/subjects",
	states:			"/tk_graph/states",
	transitions:	"/tk_graph/transitions"
};