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

		this.JSONtimestampToString = JSONtimestampToString;
	}

	var processStarted = ko.observable();
	var processEnded = ko.observable();
	var historicEntries = ko.observableArray([]);
	//historicEntries = [{subject: "laPlome"}, {subject: "Calr Sagan"}];

	var processInstance = ko.observable( new ProcessInstance() );

	processInstance.subscribe(function( process ) {
		updateHistory();
	});

	var updateHistory = function() {
		
		startdate =  processInstance().history().processStarted;
		processStarted = JSONtimestampToString( startdate );

		enddate = processInstance().history().processEnded;
		processEnded = JSONtimestampToString( enddate );

		historicEntries = processInstance().history().entries;
	}

	var JSONtimestampToString = function( JSONtimestamp ){
		newDate = new Date( JSONtimestamp.date );
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


