define([
	"knockout",
	"app",
	"underscore",
	"models/processInstance",
	"notify"
], function( ko, App, _, ProcessInstance, Notify ) {

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

		this.googleDriveData = googleDriveData;

		this.currentSelectedFile = currentSelectedFile;

		this.selectFile = selectFile;

		this.executable = ko.computed(function() {
			console.log(this)
		});
	}

	var selectFile = function() {
		$('#googleDriveModal').modal('hide');
		currentSelectedFile( this.title );
	}

	var currentSelectedFile = ko.observable("");

	var processInstance = ko.observable(),
			messageText     = ko.observable(),
			currentSubject  = ko.observable(),
			serverDone      = ko.observable(true),
			actionOfCurrentSubject,
			availableActions,
			actionData,
			stateName,
			stateText,
			isTypeOf;
	
	var action = function(action) {
		serverDone(false);
		
		data = actionOfCurrentSubject()
		id = data.processInstanceID;
		data.actionData = action;
		data = JSON.stringify(data);
		$.ajax({
			url: '/processinstance/' + id,
			type: "PUT",
			data: data,
			async: true,
			dataType: "json",
			contentType: "application/json; charset=UTF-8",
			success: function(data, textStatus, jqXHR) {
				serverDone( true );
				currentSelectedFile("");
				processInstance().refresh();
			},
			error : function(jqXHR, textStatus, error) {
				// TODO: IMPROVE ERROR HANDLING!
				Notify.error( "Error", "Unable to send action. Please try again." );
			}
		});

	};


	var refresh = function(data){
		
	}

	var send = function() {
		var deArray;

		serverDone( false );
		data = actionOfCurrentSubject()

		deArray = data.actionData[ 0 ];
		deArray[ "messageContent" ] = messageText();
		data.actionData = deArray;

		id = data.processInstanceID;

		data = JSON.stringify( data );

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
				// TODO: IMPROVE ERROR HANDLING!
				Notify.error( "Error", "Unable to send action. Please try again." );
			},
			complete: function(jqXHR, textStatus) {
				serverDone( true );
				currentSelectedFile("");
			}
		});
	};


	var googleDriveData = [{
		"alternateLink": "https://docs.google.com/document/d/1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc/edit",
		"createdDate": "2013-03-20T13:12:21.854Z",
		"embedLink": "https://docs.google.com/document/d/1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc/preview",
		"iconLink": "https://ssl.gstatic.com/docs/doclist/images/icon_11_document_list.png",
		"id": "1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc",
		"mimeType": "application/vnd.google-apps.document",
		"thumbnailLink": "https://docs.google.com/feeds/vt?gd=true&id=1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc&v=793&s=AMedNnoAAAAAUURbZSQ7c-9rXNiaTxNH3LsNQ0VjpAex&sz=s220",
		"title": "Presentation Test File"
	}, {
		"alternateLink": "https://docs.google.com/document/d/1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc/edit",
		"createdDate": "2013-03-21T10:42:31.391Z",
		"embedLink": "https://docs.google.com/document/d/1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc/preview",
		"iconLink": "https://ssl.gstatic.com/docs/doclist/images/icon_11_presentation_list.png",
		"id": "1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc",
		"mimeType": "application/vnd.google-apps.document",
		"thumbnailLink": "https://docs.google.com/feeds/vt?gd=true&id=1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc&v=793&s=AMedNnoAAAAAUURbZSQ7c-9rXNiaTxNH3LsNQ0VjpAex&sz=s220",
		"title": "A simple presentation"
	}, {
		"alternateLink": "https://docs.google.com/document/d/1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc/edit",
		"createdDate": "2013-03-21T10:44:17.784Z",
		"embedLink": "https://docs.google.com/document/d/1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc/preview",
		"iconLink": "https://ssl.gstatic.com/docs/doclist/images/icon_11_spreadsheet_list.png",
		"id": "1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc",
		"mimeType": "application/vnd.google-apps.document",
		"thumbnailLink": "https://docs.google.com/feeds/vt?gd=true&id=1ZOlIA6UcgWfXE2GFbMWsyVObTjSeGFsr2NAJdSKi4jc&v=793&s=AMedNnoAAAAAUURbZSQ7c-9rXNiaTxNH3LsNQ0VjpAex&sz=s220",
		"title": "Another File"
	}]

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


		App.loadTemplate( "execution/actions", viewModel, "actions", function() {
		});
	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


