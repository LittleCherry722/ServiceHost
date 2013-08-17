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
		this.processInstance = processInstance;
		this.historicEntries = historicEntries;
	}
	var processStarted = ko.observable();
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
		if( processInstance().startedAt()){
			processStarted(moment( processInstance().startedAt().date ).format( "YYYY-MM-DD HH:mm" ));
		}
		
		historicEntries.removeAll();
		$.each( History.all() , function ( i, value ) {
			value.ts= moment(value.timeStamp().date).format( "YYYY-MM-DD HH:mm" );
			console.log(value.subject());
			if(value.process().processInstanceId==processInstance().id() && ((currentSubject() && currentSubject() == value.subject()) || !currentSubject()) ) {
				historicEntries.push(value);
			}
		} );
		
	});

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