define([
	"knockout",
	"app",
	"underscore",
	"models/processInstance",
	"moment"
], function( ko, App, _, ProcessInstance, moment) {

	var ViewModel = function() {
		this.availableSubjects = ko.observableArray([]);
		this.currentSubject = currentSubject;
		this.processInstance = processInstance;
		this.processStarted = processStarted;
		this.processEnded = processEnded;
		this.historicEntries = historicEntries;
	}

	var processStarted = ko.observable();
	var processEnded = ko.observable();
	var newHistory = ko.observableArray([]);
	var historicEntries = ko.observableArray([]);

	var processInstance = ko.observable( new ProcessInstance() );
	processInstance.subscribe(function( process ) {
		updateHistory();
	});
	
	var currentSubject = ko.observable();
	currentSubject.subscribe(function( subjectId ) {
		historicEntries.removeAll();
		$.each( newHistory.entries, function ( i, value ) {
			if (currentSubject() && currentSubject() === value.subject ) {
				historicEntries.push(value);
				console.log( i + "--" + value.toSource());
			}
		} );
	});
	
	var updateHistory = function() {
		newHistory = setTimeFormat( processInstance().history() );

		processStarted = newHistory.processStarted;
		processEnded = newHistory.processEnded;
		// retrieve the current process by subject id
		if (!currentSubject() ) {
			historicEntries.removeAll();
			$.each( newHistory.entries, function ( i, value ) {
					historicEntries.push(value);
			} );
		}
	}

	var setTimeFormat = function( processHistory ){
		newHistory = processHistory;

		if( newHistory.hasOwnProperty( "processStarted" ) ){
			newHistory.processStarted.date = JSONtimestampToString( newHistory.processStarted.date );
		} else {
			newHistory.processStarted = { date: "Has not ended yet." }
		}

		if( newHistory.hasOwnProperty( "processEnded" ) ){
			newHistory.processEnded.date = JSONtimestampToString( newHistory.processEnded.date );
		} else {
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
		$(".state").click(showMessages);
		$(".state").live( 'click', showMessages);
		function showMessages() {
			if($('.message'+$(this).attr('id')).css('display')=="table-row") {
				$('.message'+$(this).attr('id')).css('display','none');
			} else {
				$('.message'+$(this).attr('id')).css('display','table-row');
			}
		}
		App.loadTemplate( "execution/history", viewModel, "executionContent", function() {
			App.loadSubView( "execution/actions", [instance, currentSubject] );
			$( "#slctSbj" ).chosen();
			
			var subject = subjectId;
			currentSubject( subject );
		});

	}
	
	// Everything in this object will be the public API
	return {
		init: initialize,
		setSubject: currentSubject
	}
});


