define([
	"knockout",
	"app",
	"models/processInstance"
], function( ko, App, ProcessInstance ) {

	var ViewModel = function() {
	
		this.processInstance = processInstance;

		this.availableSubjects = availableSubjects;
		this.currentSubject = currentSubject;
		
		this.tabs = tabs;
		this.currentTab = currentTab;
	}
	
	var processInstance = ko.observable();

	var availableSubjects = ko.observableArray( [] );
	var currentSubject = ko.observable();
	
	var tabs = ['Graph', 'History' ];
	var currentTab = ko.observable();


	var setView = function( id, tab ) {
		processInstance( ProcessInstance.find( id ) );
		currentTab( tab );
	}


	processInstance.subscribe(function( process ) {
		// console.log( "a new process has been loaded: " + process );
	});
	
	currentSubject.subscribe(function( subject ) {
		// console.log( "active subject was changed to: " + subject );
	});

	currentTab.subscribe(function( newTab ) {
		if ( !newTab ) {
			currentTab( tabs[0] );
			return;
		}

		App.loadSubView( "execution/" + newTab.toLowerCase(), [ processInstance() ] );
		if ( newTab === tabs[0] ) {
			$("#executionContent").addClass("first-tab-selected");
		} else {
			$("#executionContent").removeClass("first-tab-selected");
		}
	});


	var initialize = function( processInstanceId, subSite ) {
		var viewmodel;

		viewModel = new ViewModel();

		processInstance( ProcessInstance.find( processInstanceId ) );

		if ( !subSite ) {
			subSite = tabs[0]
		}

		App.loadTemplate( "execution", viewModel, null, function() {
			$( "#slctSbj" ).chosen();
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
		setView: setView
	}
});


