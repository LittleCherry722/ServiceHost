/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
define([
	"knockout",
	"app",
	"underscore",
	"models/processInstance",
	"router",
], function( ko, App, _, ProcessInstance, Router ) {

	var ViewModel = function() {
		this.availableSubjects = ko.observableArray([]);
		this.currentSubject = currentSubject;
		this.processInstance = processInstance;
		subscribeAll()
	};

	var processInstance = ko.observable( new ProcessInstance() );

	var actions = ko.computed(function() {
		if(0 === processInstance().id()) {
			// uninitialized
			return [];
		}
		return processInstance().actions()
	});

	var subjects = ko.computed(function() {
		try{
			return processInstance().process().subjects();
		} catch( error ) {
			return [];
		}
	});
	var subjectsArray = ko.computed(function() {
		if ( processInstance() && processInstance().process() ) {
			return processInstance().process().subjectsArray();
		} else {
			return []
		}
	});

	var currentSubject = ko.observable();

	currentSubject.subscribe(function( subjectId ) {
		var newRoute;

		if( subjectId ) {
			subject = subjectId.replace(/___/, " ");
			if ( gv_graph.subjects[subject] && !gv_graph.subjects[subject].isExternal() ) {
				// let the graph know we want to go to the internal view of a subject.
				Router.goTo([ "processinstances", processInstance().id(), "Graph", subject ]);
				gv_graph.selectedSubject = null;
				gf_clickedCVnode( subject );
				loadBehaviorView( subject );
				$('#graph_cv_outer').show();
			} else if ( gv_graph.subjects[subject] && gv_graph.subjects[subject].isExternal() ) {
                alert('The selected subject is external and cannot be shown');
            }
		}

        previousSubject = currentSubject;
	});

	processInstance.subscribe(function( process ) {
		reloadGraph();
	});
	actions.subscribe(function(actions) {
		selectCurrentBehaviourStates();
	});

	var reloadGraph = function() {
        gf_clearGraph();
		gf_loadGraph( JSON.stringify( processInstance().graph().definition ) );
	}

	var viewChanged = function( view ) {
		if ( view === "cv" ) {
			currentSubject( undefined );
		}
	}

	var goToExternalProcess = function( process ) {
		Router.goTo( Process.findByName( process )[0] )
	}

	var loadBehaviorView = function( subject ) {
		gv_graph.selectedSubject = null;
		gf_clickedCVbehavior();
		selectCurrentBehaviourStates();
	}

	/**
	 * Selects the nodes of the current active (executable) states in the behaviour graph
	 */
	var selectCurrentBehaviourStates = function() {
		var subject = currentSubject(),
			currentStates = processInstance().getCurrentStates(subject),
			process = processInstance().getCurrentProcess(subject),
			nodes = [];

        gf_deselectNodes();

        if( process === null ) {
			if ( gv_objects_nodes.length > 0 ) {
				gv_objects_nodes[0].select();
			}
			return;
		}

		// retrieve the current node by the current process and current state
		$.each( process.macros[0].nodes, function( i, value ) {
			if (-1 !== $.inArray(value.id, currentStates)) {
				nodes.push(i);
			}
		} );

        $.each(nodes, function(k, node) {
            if(gv_objects_nodes[node] ){
                gv_objects_nodes[node].select();
            }
        });
	};

	/**
	 * @returns {String} The ID of a subject which can execute an action in the current process. If no subject can
	 * execute an action, the first subject ID will be returned. Ignores external subjects. If all subjects are external,
     * null will be returned
	 */
	var getActiveSubject = function() {
		var actions = processInstance().actions();
		for ( var i = 0; i < actions.length; i++ ) {
			var action = actions[i];
            if(gv_graph.subjects[action.subjectID].isExternal()) {
                continue;
            }
            if($.isArray(action.data)) {
                for ( var j = 0; j < action.data.length; j++ ) {
                    var data = action.data[j];
                    if ( data.executeAble === true ) {
                        return action.subjectID;
                    }
                }
            }
		}

        var internalSubjects = _.filter(subjectsArray(), function(subj) {
            return !gv_graph.subjects[subj[0]].isExternal();
        });
        if(internalSubjects.length > 0) {
            return internalSubjects[0][0];
        }
        return null;
	}

	var subscriptions = [];
	var subscribeAll = function() {
		subscriptions = [
			// $.subscribeOnce( "tk_graph/updateListOfSubjects", updateListOfSubjects ),
			$.subscribeOnce( "tk_graph/changeViewHook", viewChanged ),
			$.subscribeOnce( "tk_graph/changeViewBV", loadBehaviorView ),
			$.subscribeOnce( "tk_graph/subjectDblClickedInternal", currentSubject),
			$.subscribeOnce( "tk_graph/subjectDblClickedExternal", goToExternalProcess)
		]
	}

	var unload = function() {
		_( subscriptions ).each(function( element, list ) {
			$.unsubscribe( element );
		});
    	return true;
	}

	var initialize = function( instance, subjectId ) {
		var viewModel = new ViewModel();

		App.loadTemplate( "execution/graph", viewModel, "executionContent", function() {
			App.loadSubView( "execution/actions", [instance, currentSubject] );
			$( "#slctSbj" ).chosen();
			processInstance( instance )
			gf_paperZoomOut();
			gf_paperZoomOut();

			var subject = subjectId || getActiveSubject();
			currentSubject( subject )
		});
	}

	// Everything in this object will be the public API
	return {
		init: initialize,
		setSubject: currentSubject,
		unload: unload
	}
});
