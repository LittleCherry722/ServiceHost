define([
	"knockout",
	"app",
	"underscore"
], function( ko, App, _ ) {

	var ViewModel = function() {
		this.availableSubjects = ko.observableArray([]);

		this.currentSubject = ko.observable();
	}

	var currentProcess = ko.observable();

	currentProcess.subscribe(function( process ) {
		console.log("a new process has been loaded: " + process);
	});

	var initialize = function( process ) {
		var viewModel;

		currentProcess( process )

		viewModel = new ViewModel();

		App.loadTemplate( "execution/graph", viewModel, "executionContent", function() {
			App.loadSubView( "execution/actions", currentProcess() );
			$( "#slctSbj" ).chosen();
		});

	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


