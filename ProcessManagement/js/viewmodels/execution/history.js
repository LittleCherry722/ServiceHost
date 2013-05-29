define([
	"knockout",
	"app",
	"underscore",
	"models/processInstance",
	"moment"
], function( ko, App, _, ProcessInstance, moment) {

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

		if( newHistory.hasOwnProperty( "processStarted" ) ){
			newHistory.processStarted.date = JSONtimestampToString( newHistory.processStarted.date );
		}
		else {
			newHistory.processStarted = { date: "Has not ended yet." }
		}

		if( newHistory.hasOwnProperty( "processEnded" ) ){
			newHistory.processEnded.date = JSONtimestampToString( newHistory.processEnded.date );
		}
		else {
			newHistory.processEnded = { date: "Has not ended yet." };
		}

		
		for( i=0; i<newHistory.entries.length; i++ ){
			newHistory.entries[i].timestamp.date = JSONtimestampToString( newHistory.entries[i].timestamp.date );
		}

		return newHistory;
	}

	var JSONtimestampToString = function( JSONtimestamp ){
		newDate = new Date( JSONtimestamp );
		//return newDate.toGMTString();
		return newDate.getDate()+'.'+(newDate.getMonth()+1)+'.'+newDate.getFullYear();
		//this.date( moment().format( "YYYY-MM-DD HH:mm:ss" ) );
	}

	var initialize = function( instance ) {
		var viewModel;

		processInstance( instance );

		viewModel = new ViewModel();

		$(".state").live( 'click', function() {
			if($('.message'+$(this).attr('id')).css('display')=="table-row") {
				$('.message'+$(this).attr('id')).css('display','none');
			} else {
				$('.message'+$(this).attr('id')).css('display','table-row');
			}
		});
		App.loadTemplate( "execution/history", viewModel, "executionContent", function() {
			$( "#slctSbj" ).chosen();
			App.loadSubView( "execution/actions", [instance, subjectId] );
		});

	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


