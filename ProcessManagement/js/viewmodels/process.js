define([
	"knockout",
	"app",
	"notify",
	"router",
	"models/process"
], function( ko, App, Notify, Router, Process ) {
	var ViewModel = function() {
		var self = this;

		// The current process Name
		this.processName = ko.observable("");

		this.currentProcess = currentProcess;

		this.activeView = ko.observable("Subject View");
	}

	var currentView = ko.observable();

	/*
	 * The current Process.

	 * Create a new Process (but do not save it yet) and let every other
	 * observable (name, isCase etc.) reference this process.
	 * That way everything is updated automatically.
	 *
	 * Example: processName = currentProcess().name()
	 */
	var currentProcess = ko.observable( new Process() );

	currentProcess.subscribe(function() {
		gv_graph.clearGraph(true);
	});

	// Initialize our View.
	// Includes loading the template and creating the viewModel
	// to be applied to the template.
	var initialize = function( processID, callback ) {
		var viewModel = new ViewModel();
		currentProcess( Process.find( processID ) )

		App.loadTemplate( "process", viewModel, null, function() {
			console.log( "main process View loaded" );

			App.loadTemplate( "process/subject", viewModel, "tab2_content", function() {
				console.log("subject view loaded")
			} );
			App.loadTemplate( "process/internal", viewModel, "tab1_content", function() {
				console.log("internal view loaded");
			});

			if ( typeof callback === "function" ) {
				callback.call( this );
			}
		});
	}
	
	// Everything in this object will be the public API
	return {
		init: initialize,
		currentProcess: currentProcess
	}
});
