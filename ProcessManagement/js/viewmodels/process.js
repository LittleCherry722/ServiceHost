define([
	"knockout",
	"app",
	"notify",
	"router",
	"models/process",
	"async"
	// "tk_graph"
], function( ko, App, Notify, Router, Process, async ) {
	var ViewModel = function() {
		var self = this;

		// The current process Name
		this.processName = ko.observable("");

		this.currentProcess = currentProcess;

		this.assignedRoleText = ko.computed(function() {
			return currentProcess().isCase() ? "Assigned User" : "Assigned Role"
		});

		this.availableProcesses = Process.all;
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

	currentProcess.subscribe(function( process ) {
		gv_graph.clearGraph( true );
		
		if ( process.graph() ) {
			console.log( "loading existing graph" );
			gf_loadGraph( process.graph().graphString(), undefined );

			// var graph = JSON.parse(graphAsJson);
			// self.chargeVM.load(graph);

		} else {
			loadEmptyProcess( process );
		}

		$("#tab2").addClass("active");

		Notify.info("Information", "Process \""+ process.name() +"\" successfully loaded.");
	});

	var loadEmptyProcess = function( process ) {
		if ( process.isCase() ) {
			gf_createCase( App.currentUser().name() );
		} else {
			gv_graph.loadFromJSON("{}");
		}
	}

	// Initialize our View.
	// Includes loading the template and creating the viewModel
	// to be applied to the template.
	var initialize = function( processID, callback ) {
		var viewModel = new ViewModel();

		App.loadTemplate( "process", viewModel, null, function() {

			App.loadTemplates([
				[ "process/subject", "tab2_content" ],
				[ "process/internal", "tab1_content" ]
			], viewModel, function() {
				currentProcess( Process.find( processID ) )
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
