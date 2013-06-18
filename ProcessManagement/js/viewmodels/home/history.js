define([
	"knockout",
	"app",
	"underscore",
	"models/history",
], function( ko, App, _, History) {

	var ViewModel = function() {
		this.historicEntries = historicEntries;		
	}
	var historicEntries = ko.observableArray([]);
	var newHistory = ko.observableArray([]);

	var updateHistory = function() {
		newHistory = History.all();
		console.log("update");
		for( i=0; i<newHistory.length; i++ ){
			newHistory[i].timestamp = JSONtimestampToString(newHistory[i].timestamp);
			console.log(newHistory[i].timestamp);
		}
		
		historicEntries.removeAll();
		$.each( newHistory, function ( i, value ) {
			historicEntries.push(value);
		} );
	};

	var JSONtimestampToString = function( JSONtimestamp ){
		newDate = new Date( JSONtimestamp );
		return newDate.getDate()+'.'+(newDate.getMonth()+1)+'.'+newDate.getFullYear();
		//this.date( moment().format( "YYYY-MM-DD HH:mm:ss" ) );
	}

	var initialize = function( instance ) {
		var viewModel;
		
		updateHistory();
		viewModel = new ViewModel();
		App.loadTemplate( "home/history", viewModel, "executionContent", function() {
			
		});

	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});