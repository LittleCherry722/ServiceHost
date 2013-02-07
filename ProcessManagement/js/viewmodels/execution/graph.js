define([
	"knockout",
	"app",
	"underscore",
	"models/processInstance"
], function( ko, App, _, ProcessInstance ) {

	var ViewModel = function() {
		this.availableSubjects = ko.observableArray([]);

		this.currentSubject = ko.observable();

		this.processInstance = processInstance;
	}

	var processInstance = ko.observable( new ProcessInstance() );

	processInstance.subscribe(function( process ) {
		reloadGraph();
	});

	var reloadGraph = function() {
		gv_graph.clearGraph();
		window.p = processInstance();
		gf_loadGraph( processInstance().graph() );
	}

	var initialize = function( instance ) {
		var viewModel;

		viewModel = new ViewModel();

		App.loadTemplate( "execution/graph", viewModel, "executionContent", function() {
			// App.loadSubView( "execution/actions", processInstance() );
			$( "#slctSbj" ).chosen();
			processInstance( instance )
		});

	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


