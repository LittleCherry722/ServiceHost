define([
	"knockout",
	"app",
	"underscore",
	"models/history",
	"moment"
], function( ko, App, _, History, moment) {

	var ViewModel = function() {
		this.historicEntries = historyList;
		/// Filter
		this.selectedUser = selectedUser;
		this.selectedProcess = selectedProcess;
		this.selectedStatetype = selectedStatetype;
		this.selectedStart = selectedStart;	
		this.selectedEnd = selectedEnd;			
	}
	var historyList = ko.observableArray();
	var historys = ko.computed(function() {
		//historyList(History.all().slice(0));
		historyList.removeAll();
		$.each( History.all(), function ( i, value ) {
			if(value.transitionEvent() && value.transitionEvent().message) {
				console.log(value.transitionEvent().message.fromSubject);
			}			
			value.ts = JSONtimestampToString(value.timeStamp().date);
			historyList.push(value);
		});
	});
	
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
		historyList.removeAll();
		$.each( History.all(), function ( i, value ) {
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
				historyList.push(value);
			}
		});
	}
	
	
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