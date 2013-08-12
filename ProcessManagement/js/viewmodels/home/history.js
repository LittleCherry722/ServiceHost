define([
	"knockout",
	"app",
	"underscore",
	"models/history",
	"moment"
], function( ko, App, _, History, moment) {

	var ViewModel = function() {
		this.historys = historyList;
		/// Filter
		this.selectedUser = selectedUser;
		this.selectedProcess = selectedProcess;
		this.selectedStatetype = selectedStatetype;
		this.selectedStart = selectedStart;	
		this.selectedEnd = selectedEnd;
	}
	var historyList = ko.observableArray();
	
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

	var filter = ko.computed(function() {
		historyList.removeAll();
		$.each( History.all(), function ( i, value ) {
			value.ts = JSONtimestampToString(value.timeStamp().date);
			var filter = false;
			if (selectedUser() && selectedUser() !== value.userId ) {
				filter = true;
			}
			if (selectedProcess() && selectedProcess() !== value.process().processName ) {
				filter = true;
			}
			if (selectedStatetype() && selectedStatetype() !== value.transitionEvent().fromState.stateType && selectedStatetype() !== transitionEvent().toState.stateType  ) {
				filter = true;
			}
			if (selectedStart() && parseInt(selectedStart()) >= parseInt(moment(value.timeStamp().date).format('X'))) {
				filter = true;
			}
			if (selectedEnd() && parseInt(selectedEnd()) <= parseInt(moment(value.timeStamp().date).format('X'))) {
				filter = true;
			}
			if(filter!=true) {
				historyList.push(value);
			}
		});
	});
	
	
	var initialize = function( instance ) {
		var viewModel = new ViewModel();
		App.loadTemplate( "home/history", viewModel, "executionContent", function() {});
		History.fetch();
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