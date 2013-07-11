define([
	"knockout",
	"app",
	"underscore",
	"models/history",
	"moment"
], function( ko, App, _, History, moment) {

	var ViewModel = function() {
		this.historicEntries = historicEntries;
		/// Filter
		this.selectedUser = selectedUser;
		this.selectedProcess = selectedProcess;
		this.selectedStatetype = selectedStatetype;
		this.selectedStart = selectedStart;	
		this.selectedEnd = selectedEnd;			
	}
	var historicEntries = ko.observableArray();
	var newHistory = ko.observableArray(History.all());

	var updateHistory = function() {
		historicEntries.removeAll();
		$.each( newHistory(), function ( i, value ) {
			value.timestamp = JSONtimestampToString(value.timestamp);
			historicEntries.push(value);
		} );
	};
	
	var JSONtimestampToString = function( JSONtimestamp ){
		return  moment(JSONtimestamp).format( "YYYY-MM-DD HH:mm" );
	}
	
	/* Filter Start */
	var selectedUser = ko.observable();
	var selectedProcess = ko.observable();
	var selectedStatetype = ko.observable();
	var selectedStart = ko.observable();
	var selectedEnd = ko.observable();
	selectedUser.subscribe(function() { filter(); });
	selectedProcess.subscribe(function() { filter(); });
	selectedStatetype.subscribe(function() { filter(); });
	selectedStart.subscribe(function() { filter(); });
	selectedEnd.subscribe(function() { filter();});
	
	var filter = function() {
		historicEntries.removeAll();
		$.each( newHistory(), function ( i, value ) {
			var filter = false;
			if (selectedUser() && selectedUser() !== value.userId ) {
				filter = true;
			}
			if (selectedProcess() && selectedProcess() !== value.processName ) {
				filter = true;
			}
			if (selectedStatetype() && selectedStatetype() !== value.transition.fromState.stateType && selectedStatetype() !== value.transition.toState.stateType  ) {
				filter = true;
			}
			if (selectedStart() && parseInt(selectedStart()) > parseInt(moment(value.processStarted).format('X'))) {
				filter = true;
			}
			if (selectedEnd() && parseInt(selectedEnd()) < parseInt(moment(value.processEnd).format('X'))) {
				filter = true;
			}
			if(filter!=true) {
				historicEntries.push(value);
			}
		});
	}
	
	
	var initialize = function( instance ) {	
		updateHistory();
		var viewModel = new ViewModel();
		App.loadTemplate( "home/history", viewModel, "executionContent", function() {
			
		});

	}
	
	// Everything in this object will be the public API
	return {
		init: initialize,
		setUser: selectedUser,
		setProcess: selectedProcess,
		setStatetype: selectedStatetype,
		setStart: selectedStart,
		setEnd: selectedEnd
	}
});