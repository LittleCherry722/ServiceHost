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
		console.log( "a new process has been loaded: " + process.name() );
	});

	var initialize = function( processID ) {
		var viewModel, process;

		process = Process.find( processID )

		if ( process === currentProcess() ) {
			currentProcess.valueHasMutated();
		} else {
			currentProcess( process )
		}

		viewModel = new ViewModel();

		App.loadTemplate( "process/routes", viewModel, null, function() {
			console.log("template loaded")
		});
	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


