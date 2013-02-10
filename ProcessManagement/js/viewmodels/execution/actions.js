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
		
		this.isSendType = isSendType;
		
		this.isReceiveType = isReceiveType;
		
		this.action = action;
		
		this.send = send;
		
	
	}

	var processInstance = ko.observable();

	var availableActions;
	
	var availableSubjects;
	
	var currentSubject = ko.observable();
	
	var actionOfCurrentSubject = ko.observable();
	
	var messageText = ko.observable();
			
	var actionData;
	
	var isSendType;
	
	var isReceiveType;
	
 

	
	var action = function(action) {
		console.log("action: " + action)
		data = actionOfCurrentSubject()
		id = data.processInstanceID;
		data.actionData = action;
		data = JSON.stringify(data);
		console.log(data);

		$.ajax({
			url : '/ProcessManagement/scala/processinstance/' + id,
			type : "PUT",
			data : data,
			async : true, // defaults to false
			dataType : "json",
			contentType : "application/json; charset=UTF-8",
			success : function(data, textStatus, jqXHR) {
				console.log(data);
			},
			error : function(jqXHR, textStatus, error) {
				// Some error handling maybe?
			},
			complete : function(jqXHR, textStatus) {
				// Always after success or error
			}
		});

	};


	var send = function() {
		console.log("send: "+ messageText())
		data = actionOfCurrentSubject()
		id = data.processInstanceID;
			$.ajax({
			url : '/ProcessManagement/scala/processinstance/' + id,
			type : "PUT",
			data: data,
			async : true, // defaults to false
			dataType : "json",
			contentType : "application/json; charset=UTF-8",
			success : function(data, textStatus, jqXHR) {
				console.log(data);
			},
			error : function(jqXHR, textStatus, error) {
				// Some error handling maybe?
			},
			complete : function(jqXHR, textStatus) {
				// Always after success or error
			}
		});
		
	};

	
		
	var initialize = function( instance ) {
		console.log("init a");
		var viewModel;
		
		processInstance( instance );

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





		isSendType = ko.computed(function() {
			if (actionOfCurrentSubject() != undefined && actionOfCurrentSubject().stateType != undefined && actionOfCurrentSubject().stateType === "send") {
				return true
			} else {
				return false
			}
		}); 

		isReceiveType = ko.computed(function() {
			if (actionOfCurrentSubject() != undefined && actionOfCurrentSubject().stateType != undefined && actionOfCurrentSubject().stateType === "receive") {
				return true
			} else {
				return false
			}
		}); 




		actionData = ko.computed({
			deferEvaluation : true,
			read : function() {
				if (actionOfCurrentSubject() != undefined && actionOfCurrentSubject().actionData != undefined) {
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


