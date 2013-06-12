define([
	"knockout",
	"app",
	"underscore",
	"models/processInstance"
], function( ko, App, _, ProcessInstance ) {

	var ViewModel = function() {
		this.processInstance = processInstance;
		this.tabs = tabs;
		this.tabDescriptions = tabDescriptions;
		this.currentTab = currentTab;
	}
	var processInstance = ko.observable();
	currentSubView = ko.observable();
	
	var tabs = [ 'Actions', 'History' ];
	var tabDescriptions = {
		'Actions': 'Here you can view the execution graph of the current subject internal behavior',
		'History': 'Here you can view the execution history of this process'
	};
	var currentTab = ko.observable();
	
	var setView = function( tab ) {
		//processInstance( ProcessInstance.find( id ) );
		currentTab( tab );
		//currentSubject( subjectId );
	}
	
	currentTab.subscribe(function( newTab ) {
		if ( !newTab ) {
			currentTab( tabs[0] );
			return;
		}

    	// just load our new viewmodel and call the init method.
		require([ "viewmodels/home/" + newTab.toLowerCase() ], function( viewModel ) {
			unloadSubView();
			currentSubView( viewModel );
      		viewModel.init.apply( viewModel, [ processInstance() ] );
		});

		if ( newTab === tabs[0] ) {
			$("#executionContent").addClass("first-tab-selected");
		} else {
			$("#executionContent").removeClass("first-tab-selected");
		}
	});
	
	var initialize = function( subSite ) {
		var viewModel;

		processInstance( ProcessInstance.fetchAll );
		alert(ProcessInstance.fetchAll );
		viewModel = new ViewModel();
		//processInstance( ProcessInstance.find( processInstanceId ) );

		if ( !subSite ) {
			subSite = tabs[0];
		}

		//currentSubject( subjectId );

		App.loadTemplate( "home", viewModel, null, function() {
			if ( currentTab() == subSite ) {
				currentTab.valueHasMutated()
			} else {
				currentTab( subSite )
			}
		});
	}
	
	var unload = function() {
		unloadSubView();
		return true;
	}
	
	var unloadSubView = function() {
		if ( currentSubView() && typeof currentSubView().unload === "function" ) {
			currentSubView().unload();
		}
	}
	
	// Everything in this object will be the public API
	return {
		init: initialize,
		setView: setView,
		unload: unload
	}
});


