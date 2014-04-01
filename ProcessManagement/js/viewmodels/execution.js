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

    var gvGraphSubscription;
    var gvGraphDummy = ko.observable();     // will notify subscribers when gv_graph changes / properties become available

    // list of [subject id, subject name, boolean is internal subject]
	var subjects = ko.computed(function() {
        gvGraphDummy();
        if ( processInstance() && processInstance().process() ) {
			return _.map(processInstance().process().subjectsArray(), function(subject){
                var subjectId = subject[0].replace(/___/, " ");
                return [subject[0], subject[1], gv_graph.subjects[subjectId] && !gv_graph.subjects[subjectId].isExternal()];
            });
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
	};

	currentSubject.subscribe(function( subject ) {
		if ( currentSubView() && 'setSubject' in currentSubView()) {
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
            window.subjects = subjects
            window.pi = processInstance

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
	};

	var initialize = function( processInstanceId, subSite, subjectId ) {
		var viewModel;

		processInstance( ProcessInstance.find( processInstanceId ) );
		viewModel = new ViewModel();
		processInstance( ProcessInstance.find( processInstanceId ) );

        // notify subscribers that depend on gv_graph properties
        gvGraphSubscription = $.subscribe(gv_topics.general.conversations, function(){
            gvGraphDummy.notifySubscribers();
        });

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
	};

	var unload = function() {
        $.unsubscribe(gvGraphSubscription);
		unloadSubView();
		return true;
	};

	// Everything in this object will be the public API
	return {
		init: initialize,
		setView: setView,
		unload: unload
	}
});


