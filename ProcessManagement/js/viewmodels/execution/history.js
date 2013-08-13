define([
	"knockout",
	"app",
	"underscore",
	"models/history",
	"moment"
], function( ko, App, _, History, moment) {

	var ViewModel = function() {
		this.availableSubjects = ko.observableArray([]);
		this.currentSubject = currentSubject;
		this.history = history;
		this.processStarted = processStarted;
		this.processEnded = processEnded;
		this.historicEntries = historicEntries;
		this.processInstance = processInstance;
		this.processStarted = processStarted;
		this.processEnded = processEnded;
		this.historicEntries = historicEntries;
	}
	var processStarted = ko.observable();
	var processEnded = ko.observable();
	var historicEntries = ko.observableArray();

	var processInstance = ko.observable( new ProcessInstance() );
	processInstance.subscribe(function( process ) {
		updateHistory();
	});
	
	var currentSubject = ko.observable();
	currentSubject.subscribe(function( subjectId ) {
		updateHistory();
	});
	
	var updateHistory = ko.computed(function() {
		//setTimeFormat();
		historicEntries.removeAll();
		$.each( History.all() , function ( i, value ) {
			value.ts= JSONtimestampToString( value.timeStamp().date);
			if(value.process().processInstanceId==processInstance().id() && ((currentSubject() && currentSubject() === value.subject) || !currentSubject()) ) {
				historicEntries.push(value);
			}
		} );
		
	});

	var setTimeFormat = function(){
/*
		if( newHistory.hasOwnProperty( "timestamp" ) ){
			newHistory.processStarted.date = JSONtimestampToString( newHistory.processStarted.date );
		} else {
			newHistory.processStarted = { date: "Has not ended yet." }
		}

		if( newHistory.hasOwnProperty( "processEnded" ) ){
			newHistory.processEnded.date = JSONtimestampToString( newHistory.processEnded.date );
		} else {
			newHistory.processEnded = { date: "Has not ended yet." };
		}
*/
	}
	var JSONtimestampToString = function( JSONtimestamp ){
		return  moment(JSONtimestamp).format( "YYYY-MM-DD HH:mm" );
	}

	var initialize = function( instance ) {
		var viewModel;
		viewModel = new ViewModel();
		updateHistory();
		History.fetch();
		processInstance( instance );
		
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
			//App.loadSubView( "execution/actions", [instance, currentSubject] );
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