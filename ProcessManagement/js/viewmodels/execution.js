define([
	"knockout",
	"app"
], function( ko, App ) {

	var ViewModel = function() {
	
		this.currentProcess = currentProcess;
		/*
		this.currentInstance = currentInstance;
		this.activeSubjects = activeSubjects;
		this.currentSubject = currentSubject;
		*/
		
		this.tabs = tabs;
		this.currentTab = currentTab;

		
	}
	
	var currentProcess = ko.observable();
	/*
	var currentInstance = ko.observable();
	var activeSubjects = ko.observableArray( [] );
	var currentSubject = ko.observable();
	*/
	
	var tabs = ['Graph', 'History' ];
	var currentTab = ko.observable();


	currentProcess.subscribe(function( process ) {
		console.log( "a new process has been loaded: " + process );
	});
	
	/*
	currentSubject.subscribe(function( subject ) {
		console.log( "active subject was changed to: " + subject );
	});
	*/

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


