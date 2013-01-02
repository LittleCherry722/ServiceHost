define([
	"knockout",
	"app",
	"underscore"
], function( ko, App, _ ) {

	var ViewModel = function() {
		this.currentProcess = currentProcess;
	}

	var currentProcess = ko.observable();

	currentProcess.subscribe(function( process ) {
		console.log("a new process has been loaded: " + process);
	});

	var initialize = function( process ) {
		var viewModel;

		currentProcess( process )

		viewModel = new ViewModel();

		App.loadTemplate( "execution/actions", viewModel, "actions", function() {
			// Maybe do something here?
		});

	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


