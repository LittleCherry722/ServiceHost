define([
	"knockout",
	"app",
	"notify",
	"dialog",
	"models/process",
	"underscore"
	// "tk_graph"
], function( ko, App, Notify, Dialog, Process, _ ) {
	var ViewModel = function() {

		var self = this;

		self.processes = Process.all;
	}


	var initialize = function() {
		var viewModel = new ViewModel();
		App.loadTemplate( "processExecutionList", viewModel );
	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});

