define([
	"knockout",
	"app",
	"underscore",
	"models/processInstance"
], function( ko, App, _, ProcessInstance ) {

	var ViewModel = function() {
		this.processInstance = processInstance;
		
		this.availableActions = availableActions;

		this.currentSubject = currentSubject;
		
		this.actionOfCurrentSubject = actionOfCurrentSubject;
		
		this.actionData = actionData;
		
		this.messageText = messageText;
		
		this.action = action;
		
		this.send = send;
		
		this.stateName = stateName;
		
		this.stateText = stateText;
	
		this.isTypeOf = isTypeOf;
		
		this.serverDone = serverDone;
		
	}

	var processInstance = ko.observable();

	var availableActions;
	
	var currentSubject= ko.observable();
	
	var actionOfCurrentSubject;
	
	var messageText = ko.observable();
			
	var actionData;
	
	var stateName;

	var stateText;

	var isTypeOf;
	
	var serverDone = ko.observable(true);
	
	var action = function(action) {
		serverDone(false);
		
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
				
				processInstance().refresh();
				
			},
			error : function(jqXHR, textStatus, error) {
				
			},
			complete : function(jqXHR, textStatus) {
				
				serverDone(true);
			}
		});

	};


	var refresh = function(data){
		
	}

	var send = function() {
		
		var deArray;
		serverDone(false);
		data = actionOfCurrentSubject()
		deArray = data.actionData[0];
		deArray["messageContent"] = messageText();
		data.actionData=deArray;
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
			
				processInstance().refresh();
			},
			error : function(jqXHR, textStatus, error) {
			
			},
			complete : function(jqXHR, textStatus) {
			
				serverDone(true);
			}
		});
	};

	 




	 
	 
		
	var initialize = function( instance, subjectId ) {
		
		var viewModel;
		
		processInstance( instance );
		processInstance().refresh();
		availableActions = instance.actions;
		currentSubject = subjectId;

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

		stateText = ko.computed(function() {
			if (actionOfCurrentSubject() !== undefined) {
				return actionOfCurrentSubject().stateText;
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
		
		//window.aView = viewModel;

		App.loadTemplate( "execution/actions", viewModel, "actions", function() {
		});


	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


