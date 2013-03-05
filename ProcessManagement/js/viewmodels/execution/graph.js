define([
	"knockout",
	"app",
	"underscore",
	"models/processInstance",
	"router"
], function( ko, App, _, ProcessInstance, Router ) {

	var ViewModel = function() {
		this.availableSubjects = ko.observableArray([]);

		this.currentSubject = currentSubject;

		this.processInstance = processInstance;

		subscribeAll()
	}

	var processInstance = ko.observable( new ProcessInstance() );

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
			}
		}
	});

	processInstance.subscribe(function( process ) {
		reloadGraph();
	});

	var reloadGraph = function() {
		if( gv_paper ) {
			gv_graph.changeView('cv');
			gf_clearGraph();
		}
		gf_loadGraph( processInstance().graph() );
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

		var viewModel;

		viewModel = new ViewModel();

		App.loadTemplate( "execution/graph", viewModel, "executionContent", function() {
			App.loadSubView( "execution/actions", [instance, currentSubject] );
			$( "#slctSbj" ).chosen();
			processInstance( instance )
			gf_paperZoomOut();
			gf_paperZoomOut();
			
			var subject = subjectId || subjectsArray()[0][0];
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


