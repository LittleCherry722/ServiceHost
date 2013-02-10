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
		gf_loadGraph( processInstance().graph() );
	}

	var initialize = function( instance ) {
		console.log("init g");
		var viewModel;

		viewModel = new ViewModel();

		App.loadTemplate( "execution/graph", viewModel, "executionContent", function() {
			App.loadSubView( "execution/actions", instance );
			$( "#slctSbj" ).chosen();
			processInstance( instance )
		});
	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


