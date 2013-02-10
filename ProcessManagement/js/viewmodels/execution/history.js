define([
	"knockout",
	"app",
	"underscore"
], function( ko, App, _ ) {

	var ViewModel = function() {
		this.availableSubjects = ko.observableArray([]);

		this.currentSubject = ko.observable();

		this.processInstance = processInstance;
	}

	var processInstance = ko.observable( new ProcessInstance() );

	processInstance.subscribe(function( process ) {
		// console.log("a new process has been loaded: " + process);
	});

	var initialize = function( instance ) {
		console.log("init h");
		var viewModel;

		processInstance( instance );

		viewModel = new ViewModel();

		App.loadTemplate( "execution/history", viewModel, "executionContent", function() {
			$( "#slctSbj" ).chosen();
			App.loadSubView( "execution/actions", instance );
		});

	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


