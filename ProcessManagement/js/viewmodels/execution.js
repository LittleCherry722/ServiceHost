define([
	"knockout",
	"app",
	"underscore"
], function( ko, App, _ ) {

	var ViewModel = function() {
		this.tabs = tabs;
		this.currentTab = currentTab;
	}

	var tabs = ['Graph', 'History' ];
	var currentTab = ko.observable();

	var currentProcess = ko.observable();

	currentProcess.subscribe(function( process ) {
		console.log("a new process has been loaded: " + process);
	});

	currentTab.subscribe(function( newTab ) {
		App.loadSubView( "execution/" + newTab.toLowerCase(), currentProcess );
	});


	var initialize = function( subSite ) {
		var viewModel;

		viewModel = new ViewModel();

		App.loadTemplate( "execution", viewModel, null, function() {
			currentTab( subSite || tabs[0] )
		});

	}
	
	// Everything in this object will be the public API
	return {
		init: initialize,
		currentTab: currentTab
	}
});


