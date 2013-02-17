define([
	"knockout",
	"app",
	"underscore",
	"models/processInstance"
], function( ko, App, _, ProcessInstance ) {

	var ViewModel = function() {
		this.processInstance = processInstance;
		
		this.availableSubjects = availableSubjects;
		
		this.availableActions = availableActions;

		this.currentSubject = currentSubject;
		
		this.actionOfCurrentSubject = actionOfCurrentSubject;
		
		this.actionData = actionData;
		
		this.messageText = messageText;
		
		this.action = action;
		
		this.send = send;
		
		this.stateName = stateName;
	
		this.isTypeOf = isTypeOf;
		
		this.serverDone = serverDone;
		
	}

	var processInstance = ko.observable();

	var availableActions;
	
	var availableSubjects;
	
	var currentSubject = ko.observable();
	
	var actionOfCurrentSubject = ko.observable();
	
	var messageText = ko.observable();
			
	var actionData;
	
	var stateName;

	var isTypeOf;
	
	var serverDone = ko.observable(true);
	
	var action = function(action) {
		serverDone(false);
		console.log("action: " + action)
		data = actionOfCurrentSubject()
		id = data.processInstanceID;
		data.actionData = action;
		data = JSON.stringify(data);
		$.ajax({
			url : '/processinstance/' + id,
			type : "PUT",
			data : data,
			async : true, // defaults to false
			dataType : "json",
			contentType : "application/json; charset=UTF-8",
			success : function(data, textStatus, jqXHR) {
				console.log("success")
				console.log(data);
				processInstance().refresh();
				
			},
			error : function(jqXHR, textStatus, error) {
				console.log("Error")
				console.log(error)
			},
			complete : function(jqXHR, textStatus) {
				console.log("complete")
				serverDone(true);
			}
		});

	};


	var refresh = function(data){
		
	}

	var send = function() {
		console.log("send: "+ messageText())
		serverDone(false);
		data = actionOfCurrentSubject()
		data.actionData = messageText();
		id = data.processInstanceID;
		data = JSON.stringify(data);
			$.ajax({
			url : '/processinstance/' + id,
			type : "PUT",
			data: data,
			async : true, // defaults to false
			dataType : "json",
			contentType : "application/json; charset=UTF-8",
			success : function(data, textStatus, jqXHR) {
				console.log("success")
				console.log(data);
				processInstance().refresh();
			},
			error : function(jqXHR, textStatus, error) {
				console.log("Error")
				console.log(error)
			},
			complete : function(jqXHR, textStatus) {
				console.log("complete")
				serverDone(true);
			}
		});
	};

	 
		
	var initialize = function( instance ) {
		console.log("init a");
		var viewModel;
		
		processInstance( instance );
		processInstance().refresh();

		console.log(processInstance().actions());

		availableActions = instance.actions;

		

		availableSubjects = ko.computed(function() {
			return availableActions().map(function(action) {
				return action.subjectID;
			});
		});

		//Only one currentSubject possible.
		actionOfCurrentSubject = ko.computed(function() {
			return availableActions().filter(function(action) {
				return action.subjectID === currentSubject();
			})[0];
		});


		isTypeOf = ko.computed(function() {
			if (actionOfCurrentSubject() !== undefined && actionOfCurrentSubject().stateType !== undefined) {
				return  actionOfCurrentSubject().stateType;
			} else {
				return undefined;
			}
		});

		stateName = ko.computed(function() {
			if (actionOfCurrentSubject() !== undefined) {
				return actionOfCurrentSubject().stateName;
			} else {
				return "";
			}
		});

		actionData = ko.computed({
			//deferEvaluation : false,
			read : function() {
				if (actionOfCurrentSubject() !== undefined && actionOfCurrentSubject().actionData !== undefined) {
					return actionOfCurrentSubject().actionData;
				} else {
					return [];

				}
			}
		});


		
		
		viewModel = new ViewModel();
		
		window.aView = viewModel;

		App.loadTemplate( "execution/actions", viewModel, "actions", function() {
		$( "#slctSbj" ).chosen();
		});


	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


