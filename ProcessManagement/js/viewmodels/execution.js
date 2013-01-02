define([
	"knockout",
	"app",
	"underscore",
	"router"
], function( ko, App, _, Router ) {

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
		if ( !newTab ) {
			currentTab( tabs[0] );
			return;
		}

		App.loadSubView( "execution/" + newTab.toLowerCase(), currentProcess() );
		if ( newTab === tabs[0] ) {
			$("#executionContent").addClass("first-tab-selected");
		} else {
			$("#executionContent").removeClass("first-tab-selected");
		}
	});


	var initialize = function( subSite ) {
		var viewModel= new ViewModel();

		if ( !subSite ) {
			subSite = tabs[0]
		}

		App.loadTemplate( "execution", viewModel, null, function() {
			if ( currentTab() == subSite ) {
				currentTab.valueHasMutated()
			} else {
				currentTab( subSite )
			}
		});
	}

	// Everything in this object will be the public API
	return {
		init: initialize,
		currentTab: currentTab
	}
});


