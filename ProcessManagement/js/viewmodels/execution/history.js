define([
	"knockout",
	"app",
	"underscore",
	"models/processInstance"
], function( ko, App, _, ProcessInstance) {

	var ViewModel = function() {
		this.availableSubjects = ko.observableArray([]);

		this.currentSubject = ko.observable();

		this.processInstance = processInstance;

		this.processStarted = processStarted;
		this.processEnded = processEnded;
		this.historicEntries = historicEntries;
	}

	var processStarted = ko.observable();
	var processEnded = ko.observable();
	var historicEntries = ko.observableArray([]);

	var processInstance = ko.observable( new ProcessInstance() );

	processInstance.subscribe(function( process ) {
		updateHistory();
	});

	var updateHistory = function() {
		
		newHistory = setTimeFormat( processInstance().history() );

		processStarted = newHistory.processStarted;
		processEnded = newHistory.processEnded;
		/*
		startdate =  processInstance().history().processStarted;
		processStarted = JSONtimestampToString( startdate );

		enddate = processInstance().history().processEnded;
		processEnded = JSONtimestampToString( enddate );
		*/

		historicEntries = newHistory.entries;
	}

	var setTimeFormat = function( processHistory ){
		newHistory = processHistory;

		newHistory.processStarted.date = JSONtimestampToString( newHistory.processStarted.date );
		newHistory.processEnded.date = JSONtimestampToString( newHistory.processEnded.date );

		for( i=0; i<newHistory.entries.length; i++ ){
			newHistory.entries[i].timestamp.date = JSONtimestampToString( newHistory.entries[i].timestamp.date );
		}

		return newHistory;
	}

	var JSONtimestampToString = function( JSONtimestamp ){
		newDate = new Date( JSONtimestamp );
		return newDate.toGMTString();
	}

	var initialize = function( instance ) {
		var viewModel;

		processInstance( instance );

		viewModel = new ViewModel();

		App.loadTemplate( "execution/history", viewModel, "executionContent", function() {
			$( "#slctSbj" ).chosen();
			// App.loadSubView( "execution/actions", processInstance() );
		});

	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


