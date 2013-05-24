define([
	"knockout",
	"app",
	"models/processInstance"
], function( ko, App, ProcessInstance ) {

	var ViewModel = function() {
		this.processInstance = processInstance;
		this.currentSubject = currentSubject;
		this.availableActions = availableActions;
		this.subjects = subjects;
		this.currentSubjectName = currentSubjectName;
		this.tabs = tabs;
		this.tabDescriptions = tabDescriptions;
		this.currentTab = currentTab;
	}

	var processInstance = ko.observable();

	var subjects = ko.computed(function() {
		if ( processInstance() && processInstance().process() ) {
			return processInstance().process().subjectsArray();
		} else {
			return []
		}
	});

	var currentSubject = ko.observable();

	var availableActions = ko.computed({
		deferEvaluation: true,
		read: function() {
			return processInstance().actions();
		}
	});

	var currentSubjectName = ko.computed({
		deferEvaluation: true,
		read: function() {
			var subject =  _( subjects() ).find(function( element ) {
				return element[0] == currentSubject();
			});
			if ( subject ) {
				return subject[1]
			}
		}
	});

	currentSubView = ko.observable();


	var tabs = [ 'Graph', 'History' ];
	var tabDescriptions = {
		'Graph': 'Here you can view the execution graph of the current subject internal behavior',
		'History': 'Here you can view the execution history of this process'
	};
	var currentTab = ko.observable();


	var setView = function( id, tab, subjectId ) {
		processInstance( ProcessInstance.find( id ) );
		currentTab( tab );
		currentSubject( subjectId );
	}


	currentSubject.subscribe(function( subject ) {
		if ( currentSubView() ) {
			currentSubView().setSubject( subject );
		}
	});

	currentTab.subscribe(function( newTab ) {
		if ( !newTab ) {
			currentTab( tabs[0] );
			return;
		}

    	// just load our new viewmodel and call the init method.
		require([ "viewmodels/execution/" + newTab.toLowerCase() ], function( viewModel ) {
			unloadSubView();
			currentSubView( viewModel );
      		viewModel.init.apply( viewModel, [ processInstance(), currentSubject() ] );
		});

		if ( newTab === tabs[0] ) {
			$("#executionContent").addClass("first-tab-selected");
		} else {
			$("#executionContent").removeClass("first-tab-selected");
		}
	});


	var unloadSubView = function() {
		if ( currentSubView() && typeof currentSubView().unload === "function" ) {
			currentSubView().unload();
		}
	}



	var initialize = function( processInstanceId, subSite, subjectId ) {
		var viewModel;

		processInstance( ProcessInstance.find( processInstanceId ) );
		viewModel = new ViewModel();
		processInstance( ProcessInstance.find( processInstanceId ) );

		if ( !subSite ) {
			subSite = tabs[0]
		}

		currentSubject( subjectId );

		App.loadTemplate( "execution", viewModel, null, function() {
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

	// Everything in this object will be the public API
	return {
		init: initialize,
		setView: setView,
		unload: unload
	}
});


