define([
	"knockout",
	"app",
	"models/processInstance"
], function( ko, App, ProcessInstance ) {

	var ViewModel = function() {
	
		this.processInstance = processInstance;
		this.subjects = subjects;

		this.currentSubject = currentSubject;
		this.currentSubjectName = currentSubjectName;
		
		this.tabs = tabs;
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
	
	var tabs = ['Overview', 'Graph', 'History' ];
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
			currentSubView( viewModel );
      viewModel.init.apply( viewModel, [ processInstance(), currentSubject() ] );
		});

		if ( newTab === tabs[0] ) {
			$("#executionContent").addClass("first-tab-selected");
		} else {
			$("#executionContent").removeClass("first-tab-selected");
		}
	});


	var initialize = function( processInstanceId, subSite, subjectId ) {
		var viewmodel;

		processInstance( ProcessInstance.find( processInstanceId ) );

		viewModel = new ViewModel();


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

	// Everything in this object will be the public API
	return {
		init: initialize,
		setView: setView
	}
});


