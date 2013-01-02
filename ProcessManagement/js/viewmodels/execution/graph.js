define([
	"knockout",
	"app",
	"underscore"
], function( ko, App, _ ) {

	var ViewModel = function() {
		this.availableSubjects = ko.observableArray([]);
		this.availableChannels = ko.observableArray([]);
		this.availableMacros = ko.observableArray([]);

		this.currentSubject = ko.observable();
		this.currentChannel = ko.observable();
		this.currentMacro = ko.observable();
	}

	var currentProcess = ko.observable();

	currentProcess.subscribe(function( process ) {
		console.log("a new process has been loaded: " + process);
	});

	var initialize = function( subSite ) {
		var viewModel;

		viewModel = new ViewModel();

		App.loadTemplate( "execution/graph", viewModel, "executionContent", function() {
			$( "#slctSbj" ).chosen();
			$( "#slctChan" ).chosen();
			$( "#slctMacro" ).chosen();
		});

	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


