define([
	"knockout",
	"app",
	"underscore",
	"models/processInstance",
	"notify"
], function( ko, App, _, ProcessInstance, Notify ) {

	var ViewModel = function() {
		var self = this;

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

		this.currentSelectedFile = currentSelectedFile;

		this.selectFile = selectFile;

		this.executable = ko.computed(function() {

		});

		this.googleDriveData = ko.observable();
      
                this.selectUser = ko.observableArray(User.all());
                
                this.selectedUser = selectedUser;
               
      
		this.refreshGoogleDriveData = function() {
			$.ajax({
				cache: false,
				dataType: "json",
				type: "GET",
				url: "../googledrive/get_files?id=" + App.currentUser().id(),
				success: function( data, textStatus, jqXHR ) {
					self.googleDriveData( data.items );
				},
				error: function( jqXHR, textStatus, error ) {
					Notify.error("Error", "There has been an Error retrieving the file list." +
											"Please make sure you have the appropiate permissions.");
				}
			});
		}

	}

	var selectFile = function() {
		$('#googleDriveModal').modal('hide');
		currentSelectedFile( this );

		// TODO set file ID for actually sending the file.
	}

	var currentSelectedFile = ko.observable({});

	var processInstance = ko.observable(),
			messageText     = ko.observable(),
			currentSubject  = ko.observable(),
			serverDone      = ko.observable(true),
                        selectedUser    = ko.observable(),
			actionOfCurrentSubject,
			availableActions,
			actionData,
			stateName,
			stateText,
			isTypeOf,
                        selectUser;

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
				currentSelectedFile({});
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
		if( messageText ) {
			deArray.messageContent = messageText();
		} else {
			deArray.messageContent = "[empty message]";
		}
		
                if (selectedUser()) {
                    deArray.selectedUser = selectedUser().id();
                } else {
                    deArray.selectedUser = undefined;
                }
                
                data.actionData = deArray;
		data.actionData.fileId = currentSelectedFile().id;
                
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
				currentSelectedFile({});
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
		App.loadTemplate( "execution/actions", viewModel, "actions", function() {
		});
	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


