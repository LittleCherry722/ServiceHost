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
 * This object contains a list of all DOM elements used within the API.
 * Do not change the indexes of the array as they are referred to in the API.
 * But you should adapt the values to the actual IDs of the elements on the page.
 * 
 * @type Object
 */
var gv_elements = {
	graphBVouter:			"graph_bv_outer",
	graphCVouter:			"graph_cv_outer",
	inputEdgeText:			"ge_edge_text",
	inputEdgeTarget:		"ge_edge_target",
	inputEdgeTimeout:		"ge_edge_timeout",
	inputEdgeTimeoutEx:		"ge_edge_timeoutExample",
	inputEdgeMessage:		"ge_edge_message",
	inputEdgeMessageO:		"ge_edge_message_outer",
	inputEdgeOuter:			"edge",
	inputSubjectText:		"ge_cv_text",
	inputSubjectId:			"ge_cv_id",
	inputSubjectInputPool:	"ge_cv_inputPool",
	inputNodeText:			"ge_text",
	inputNodeType2:			"ge_type2",
	inputNodeOuter:			"node",
	
	// select elements
	inputNodeTypeStart:		"ge_type_start",
		
	inputNodeType2R:		"ge_type2_R",
	inputNodeType2S:		"ge_type2_S",
	inputNodeType2End:		"ge_type2_end",
	inputNodeType2Action:	"ge_type2_action",
	
	// subject types
	inputSubjectTypeSingle:		"ge_cv_type_single",
	inputSubjectTypeMulti:		"ge_cv_type_multi",
	inputSubjectTypeExternal:	"ge_cv_type_external",
	
	// edge types
	inputEdgeTypeLabel:			"ge_edge_type_label",
	inputEdgeTypeMessage:		"ge_edge_type_message",
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
		 * used to load available messages for a certain subject
		 * 
		 * @see GCcommunication::loadEdgeMessages(), gf_guiLoadEdgeMessages()
		 * @returns {void}
		 */
		loadEdgeMessages:	"",
		
		/**
		 * read input fields (edge) and returns an object with the values
		 * 
		 * @see GCcommunication::updateEdge(), gf_guiReadEdge()
		 * @returns {Object} Indizes: text, relatedSubject, type, timeout
		 */
		readEdge:			"",
		
		/**
		 * read input fields (node) and returns an object with the values
		 * 
		 * @see GCcommunication::updateNode(), gf_guiReadNode()
		 * @returns {Object} Indizes: text, isStart, type2
		 */
		readNode:			"",
		
		/**
		 * read input fields (subject) and returns an object with the values
		 * 
		 * @see GCcommunication::updateNode(), gf_guiReadSubject()
		 * @returns {Object} Indizes: text, id, type, inputPool
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
		subjectClickedHook:		"",
		
		/**
		 * called when a subject is double-clicked
		 * 
		 * @see tk_graph.js :: gf_paperDblClickNodeC()
		 * @param {String} ID The id of the double-clicked subject.
		 * @returns {void}
		 */
		subjectDblClicked:		"",
		
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
		changeViewBV:		"showtab1",
		
		/**
		 * lock / unlock the textarea for an edge's text
		 * 
		 * @see tk_graph_api.js :: gf_setEdgeMessage()
		 * @returns {void}
		 */
		toggleEdgeMessages:	""
	}
};