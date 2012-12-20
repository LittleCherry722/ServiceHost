define([
	"knockout",
	"app",
	"underscore"
], function( ko, App, _ ) {

	var ViewModel = function() {
	}

	var currentProcess = ko.observable();

	currentProcess.subscribe(function( process ) {
		console.log("a new process has been loaded: " + process);
	});

	var initialize = function( subSite ) {
		var viewModel;

		viewModel = new ViewModel();

		App.loadTemplate( "execution/graph", viewModel, "executionContent", function() {
			// Maybe do something?
		});

	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


